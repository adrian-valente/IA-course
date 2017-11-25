package template;

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

public class Centralized implements CentralizedBehavior {
	
	private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
    private static int NMAX = 5;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
		// TODO Auto-generated method stub
		
		// this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
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
		long start = System.currentTimeMillis();
		//Build initial solution
		Solution curSol = null;
		double curCost = 0;
		Solution bestSol = null;
		double bestCost = Double.MAX_VALUE;
		boolean stop = false;
		int nTries = 0;
		int iterations = 0;
		
		while (System.currentTimeMillis() - start < this.timeout_plan - 1000){
			stop = false;
			curSol = new Solution(vehicles, tasks);
			curCost = curSol.cost();
			
			while(!stop && (System.currentTimeMillis() - start < this.timeout_plan - 1000)){
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
					System.out.println(min);
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
		
		curSol = bestSol;
		curCost = bestCost;
		
		List<Plan> plans = curSol.generatePlan();
		
		System.out.println("Final solution:");
		System.out.println("Cost: "+curCost);
		System.out.println("Costs per vehicle: ");
		
		System.out.println("\n"+iterations+" iterations");
		System.out.println("Computation time: "+(System.currentTimeMillis() - start));
		return plans;
	}
}
