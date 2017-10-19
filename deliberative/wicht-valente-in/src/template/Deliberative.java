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
		int freeCharge = v.capacity()-s.weight();
		
		//if there is no task carried, try to move to each neighbor city
		if(s.getToDeliver().isEmpty()) {
			for(City n: c.neighbors()) {
				states.add(s.move(n));
			}
		}
		
		//try to move to the next city on the path to the delivery city of the first task no taking any new task
		else {
			Task t = s.getToDeliver().get(0);
			City next = c.pathTo(t.deliveryCity).get(0);
			states.add(s.move(next));
		}
		
		//try to pickup each task available in the current city
		for(Task t: s.localPickupTasks()) {
			if(freeCharge >= t.weight) {
				states.add(s.pickup(t));
			}
		}
		
		//for each successor state, deliver tasks if possible
		for(State state: states) {
			ArrayList<Task> local = state.localDeliverTask();
			for(Task t: local) {
				state = state.deliver(t);
			}
		}
		
		return states;
	}
}
