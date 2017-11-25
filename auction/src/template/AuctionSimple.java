package template;

//the list of imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.lang.Math;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;


@SuppressWarnings("unused")
public class AuctionSimple implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private double ratio1;
	private double ratio2;
	private static double alpha = 0.3;
	private static double G = 50.;
	private int remainingTasks;
	private double curCost;   //The cost of the current solution
	private double curMoney;  //The money earned so far
	private List<Task> tasks;  //The tasks that have been attributed to us
	private List<City> usableCities;  //The last city visited by each vehicle
	
	//Variables linked to the last bid (to help in auctionResult)
	private double lastBid_cost;
	private double lastBid_value;
	private boolean lastBid_ratio2Used;
	private List<City> lastBid_usableCities;
	
	//timeouts
	private long timeout_bid;
	private long timeout_plan;
	
	private static int NMAX = 5;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		System.out.println(distribution);
		this.agent = agent;
		this.ratio1 = 1.3;
		this.ratio2 = 1.0;
		this.remainingTasks = 19;
		this.curCost = 0.;
		this.curMoney = 0.;
		this.tasks = new ArrayList<Task>();
		this.usableCities = new ArrayList<City>();
		for (Vehicle v : agent.vehicles()){
			System.out.println(v.getCurrentCity());
			this.usableCities.add(v.getCurrentCity());
		}
		
		// this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        this.timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);
        this.timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

		long seed = -9019554669489983951L * agent.id();
		this.random = new Random(seed);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			curCost = lastBid_cost;
			curMoney += lastBid_value;
			tasks.add(previous);
			usableCities = lastBid_usableCities;
			System.out.println("Task "+previous+" won by AuctionSimple");
		}
		
		//Get the best bid (except for ours)
		long bestBid = Long.MAX_VALUE;
		for (long bid : bids){
			if (bid < bestBid && bid != ((long)Math.round(lastBid_value)))
				bestBid = bid;
		}
		if (this.lastBid_ratio2Used)
			this.ratio2 = Math.min(Math.max((alpha * (((double)bestBid) / lastBid_value)) + ((1. - alpha) * this.ratio2), 0.95), 1.6);
		else
			this.ratio1 = Math.min(Math.max((alpha * (((double)bestBid) / lastBid_value)) + ((1. - alpha) * this.ratio1), 1.01), 1.6);
	}
	
	public double computeProbaGoodTask(City objectiveCity){
		double sum = 0.;
		for (int i=0; i<this.usableCities.size(); i++){
			City c = usableCities.get(i);
			if (c == null)
				c = agent.vehicles().get(i).getCurrentCity();
			sum += this.distribution.probability(c, objectiveCity);
		}
		return 1. - Math.pow((1.-sum), remainingTasks);
	}
	
	@Override
	public Long askPrice(Task task) {

		//Finding the best solution with the new task
		List<Task> tasks = new ArrayList<Task>(this.tasks);
		tasks.add(task);
		Solution bestSolution = findBestSolution(agent.vehicles(), tasks, this.timeout_bid);
		
		//Definition of costs
		this.lastBid_cost = bestSolution.cost();
		double marginalCost = this.lastBid_cost - this.curCost;
		double transportCost = task.pathLength() * agent.vehicles().get(0).costPerKm();

		//Estimating the bid
		double bid;
		double minBid = 0.;
		double probaGoodTask = -1.;
		if (marginalCost < transportCost){
			bid = ratio2 * (transportCost - 1);
			this.lastBid_ratio2Used = true;
		}
		else{
			if (remainingTasks > 0){
				probaGoodTask = computeProbaGoodTask(task.pickupCity); 
				minBid = G + (transportCost * probaGoodTask) + (marginalCost * (1. - probaGoodTask));
			} else 
				minBid = marginalCost;
			bid = ratio1 * minBid;
			this.lastBid_ratio2Used = false;
		}
		
		//Updating attributes
		this.lastBid_value = bid;
		this.lastBid_usableCities = bestSolution.lastCities();
		this.remainingTasks--;
		
		
		System.out.println("[Agent AuctionSimple] Task "+task+" bid at "+bid+" cost of "+marginalCost);
		System.out.println("[Agent AuctionSimple] overhead "+(marginalCost - transportCost));
		System.out.println("[Agent AuctionSimple] MinBid "+minBid+"   ratio "+((this.lastBid_ratio2Used)?ratio2:ratio1)+"    probaGoodTask"+probaGoodTask);
		System.out.println("[Agent AuctionSimple] can realise a profit of "+(bid-marginalCost));
		

		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long debut = System.currentTimeMillis();
		
		Solution curSol = findBestSolution(vehicles, tasks, this.timeout_plan);
		
		//Finally, convert between Solution and Plan classes
		List<Plan> plans = curSol.generatePlan();
	
		System.out.println("Final solution:");
		System.out.println("Cost: "+curSol.cost());
		System.out.println("Money earned: "+curMoney);
		System.out.println("Computation time: "+(System.currentTimeMillis() - debut));
		return plans;
	}
	
	private Solution findBestSolution(List<Vehicle> vehicles, Collection<Task> tasks, long timeout){
		long start = System.currentTimeMillis();
		//Build initial solution
		Solution curSol = null;
		double curCost = 0;
		Solution bestSol = null;
		double bestCost = Double.MAX_VALUE;
		boolean stop = false;
		int nTries = 0;
		int iterations = 0;
		
		while (System.currentTimeMillis() - start < timeout - 1000){
			stop = false;
			curSol = new Solution(vehicles, tasks);
			curCost = curSol.cost();
			
			while(!stop && (System.currentTimeMillis() - start < timeout - 1000)){
				List<Solution> neighbors = curSol.generateNeighbors();
				iterations++;
				//Get the best solution:
				double min = Double.MAX_VALUE;
				Solution argmin = null;
	
				for (Solution sol : neighbors){
					if (sol.cost() < min){
						min = sol.cost();
						argmin = sol;
					}
				}
				if (min >= curCost){
					nTries++;
					if (nTries >= NMAX)
						stop = true;
				} else {
					curCost = min;
					curSol = argmin;
					nTries = 0;
				}
			}
			
			if (curCost < bestCost){
				bestSol = curSol;
				bestCost = curCost;
			}
		}
		
		return bestSol;
	}


}
