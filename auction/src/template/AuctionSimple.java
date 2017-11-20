package template;

//the list of imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

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
	private Vehicle vehicle;
	private City currentCity;
	private double curCost;
	private double curMoney;
	private List<Task> tasks;
	private double costLastBid;
	private double valueLastBid;
	private long timeout_bid;
	
	private static int NMAX = 5;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();
		this.curCost = 0.;
		this.tasks = new ArrayList<Task>();
		this.costLastBid = 0.;
		
		// this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			curCost = costLastBid;
			curMoney += valueLastBid;
			tasks.add(previous);
			System.out.println("Task "+previous+" won by "+winner);
		}
	}
	
	public static double max(double a, double b){
		return (a > b)? a : b;
	}
	
	@Override
	public Long askPrice(Task task) {

		if (vehicle.capacity() < task.weight)
			return null;

		List<Task> tasks = new ArrayList<Task>(this.tasks);
		tasks.add(task);
		Solution bestSolution = findBestSolution(agent.vehicles(), tasks);
		this.costLastBid = bestSolution.cost();
		
		double marginalCost = max(this.costLastBid - this.curCost, 0.);

		double ratio = 1.0 + (random.nextDouble() * 0.1 * task.id);
		double bid = ratio * marginalCost;
		
		valueLastBid = bid;
		
		System.out.println("[Agent "+agent.id()+"] Task "+task+" bid at "+bid+" cost of "+marginalCost);
		System.out.println("[DEBUG] ratio"+ratio);
		System.out.println("[Agent "+agent.id()+"] can realise a profit of "+(bid-marginalCost));

		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long debut = System.currentTimeMillis();
		
		Solution curSol = findBestSolution(vehicles, tasks);
		
		//Finally, convert between Solution and Plan classes
		List<Plan> plans = new ArrayList<Plan>();
		
		for (Vehicle v : vehicles){
			City curCity = v.getCurrentCity();
			Plan plan = new Plan(curCity);
			TaskAction curAction = curSol.getNextAction(v);
			
			while (curAction != null){
				for (City c : curCity.pathTo(curAction.city)){
					plan.appendMove(c);
				}
				if (curAction.is_pickup){
					plan.appendPickup(curAction.task);
				} else {
					plan.appendDelivery(curAction.task);
				}
				curCity = curAction.city;
				curAction = curSol.getNextAction(curAction);
			}
			plans.add(plan);
		}
		
		System.out.println("Final solution:");
		System.out.println("Cost: "+curSol.cost());
		System.out.println("Money earned: "+curMoney);
		System.out.println("Computation time: "+(System.currentTimeMillis() - debut));
		return plans;
	}
	
	private Solution findBestSolution(List<Vehicle> vehicles, Collection<Task> tasks){
		long start = System.currentTimeMillis();
		//Build initial solution
		Solution curSol = null;
		double curCost = 0;
		Solution bestSol = null;
		double bestCost = Double.MAX_VALUE;
		boolean stop = false;
		int nTries = 0;
		int iterations = 0;
		
		while (System.currentTimeMillis() - start < this.timeout_bid - 1000){
			stop = false;
			curSol = new Solution(vehicles, tasks);
			curCost = curSol.cost();
			
			while(!stop && (System.currentTimeMillis() - start < this.timeout_bid - 1000)){
				List<Solution> neighbors = generateNeighbors(curSol);
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
				curCost = bestCost;
			}
		}
		
		return bestSol;
	}

	private List<Solution> generateNeighbors(Solution curSol) {
		Vehicle v;
		do {
			v = chooseVehicle(agent.vehicles());
		}while(curSol.getNextAction(v) == null);
		
		List<Solution> neighbors = new ArrayList<Solution>();
		neighbors.addAll(changeVehicles(curSol,v));
		neighbors.addAll(changeOrderTasks(curSol,v));
		return neighbors;
	}
	
	/*
	 * Randomly chooses a vehicle
	 */
	private Vehicle chooseVehicle(List<Vehicle> vehicles) {
		long l = vehicles.size();
		long r = Math.round(Math.floor(Math.random()*l));
		
		return vehicles.get((int) r);	
	}
	
	private List<Solution> changeVehicles(Solution curSol, Vehicle v0) {
		TaskAction curAction = curSol.getNextAction(v0);
		List<Solution> res = new ArrayList<Solution>();
		
		// For each task carried by v0
		while (curAction != null){
			if (curAction.is_pickup){
				
				//Try to give it to each other vehicle
				for (Vehicle v: agent.vehicles()){
					if (! v.equals(v0)){
						Solution newSol = new Solution(curSol);
						if (newSol.changeVehicle(curAction.task, v0, v)){
							res.add(newSol);
						}
						
					}
				}
				
			}
			curAction = curSol.getNextAction(curAction);
		}
		
		return res;
	}
	
	private List<Solution> changeOrderTasks(Solution curSol, Vehicle v0) {
		List<Solution> res = new ArrayList<Solution>();
		
		//We generate a list from the NextAction table
		List<TaskAction> listActions = new ArrayList<TaskAction>();
		TaskAction curAction = curSol.getNextAction(v0);
		while(curAction != null){
			listActions.add(curAction);
			curAction = curSol.getNextAction(curAction);
		}
		
		for (int i=0; i<listActions.size(); i++){
			for (int j=i+1; j<listActions.size(); j++){
				Solution newSol = new Solution(curSol);
				if (newSol.permuteActions(v0, listActions.get(i), listActions.get(j))){
					res.add(newSol);
				}
			}
		}
		return res;
	}

}
