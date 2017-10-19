package template;

import java.util.ArrayList;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Deliberative implements DeliberativeBehavior {

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		// TODO Auto-generated method stub
		
	}
	
	public ArrayList<State> transit(State s, Vehicle v) {
		ArrayList<State> states = new ArrayList<State>();
		City c = s.getCity();
		int freeCharge = 0;
		for(City n: c.neighbors()) {
			states.add(s.move(n));
		}
		for(Task t: s.localPickupTasks()) {
			if()
		}
		
		return states;
	}
}
