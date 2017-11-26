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
public class AuctionNoStrategy implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private static double alpha = 0.3;
	private double ratio;
	private double curCost;
	private List<Task> tasks;  //The tasks that have been attributed to us
	
	//Variables linked to the last bid (to help in auctionResult)
	private double lastBid_cost;
	private double lastBid_value;	
	
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
		this.ratio = 1.2;
		this.curCost = 0.;
		this.tasks = new ArrayList<Task>();
		
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
			tasks.add(previous);
			curCost = lastBid_cost;
			System.out.println("Task "+previous+" won by AuctionNoStrategy");
		}
		
		//Get the best bid (except for ours)
		long bestBid = Long.MAX_VALUE;
		for (long bid : bids){
			if (bid < bestBid && bid != ((long)Math.round(lastBid_value)))
				bestBid = bid;
		}
		
		this.ratio = Math.min(Math.max((alpha * (((double)bestBid) / lastBid_cost)) + ((1. - alpha) * this.ratio), 1.01), 1.6);
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

		//Estimating the bid
		double bid = ratio * marginalCost;
		if (marginalCost <= 0)
			bid = 100;
		
		//Updating attributes
		this.lastBid_value = bid;
		
		
		System.out.println("[Agent AuctionNoStrategy] Task "+task+" bid at "+bid+" cost of "+marginalCost);
		System.out.println("[Agent AuctionNoStrategy] can realise a profit of "+(bid-marginalCost));
		

		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long debut = System.currentTimeMillis();
		
		Solution curSol = findBestSolution(vehicles, tasks, this.timeout_plan);
		
		//Finally, convert between Solution and Plan classes
		List<Plan> plans = curSol.generatePlan();
	
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
