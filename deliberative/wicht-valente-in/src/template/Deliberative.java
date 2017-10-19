package template;

import java.util.ArrayList;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
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
	
	private State transit(State currentState, Action a) {
		if(a instanceof Move) {
			return transitMove(currentState,((Move) (a)));
		}
		if(a instanceof Action.Pickup) {
			return null;
		}
		return currentState;
		
	}
	
	public State transitMove(State currentState, Move m) {
		return currentState;
	}

}
