package template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Centralized implements CentralizedBehavior {
	
	private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
		// TODO Auto-generated method stub
		
		// this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config\\settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;

	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		//Build initial solution
		Solution curSol = new Solution(vehicles, tasks);
		double curCost = curSol.cost();
		boolean cont = true;
		
		while(cont){
			List<Solution> neighbors = generateNeighbors(curSol);
			
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
				cont = false;
			} else {
				curCost = min;
				curSol = argmin;
			}
		}
		
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
		
		return plans;
	}

	private List<Solution> generateNeighbors(Solution curSol) {
		Vehicle v;
		do{
			v = chooseVehicle(agent.vehicles());
		}while(curSol.getNextAction(v)==null);
		
		
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
