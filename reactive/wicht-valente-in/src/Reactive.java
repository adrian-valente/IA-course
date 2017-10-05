import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import template.State;
import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;


public class Reactive implements ReactiveBehavior{
	private Random random;
	private double discount;
	private int numActions;
	private Agent myAgent;
	private State state;
	private HashMap<State,Action> V;	

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.discount = (double)agent.readProperty("discount-factor", Double.class,0.95);
		this.random = new Random();
		this.numActions = 0;
		this.myAgent = agent;
		
		learn(topology,td);
	}
	
	public void learn(Topology topology, TaskDistribution td){
		ArrayList<State> states = generateStates(topology);
		HashMap<State,HashMap<Action,Double>> Q = new HashMap<State,HashMap<Action,Double>>();
		HashMap<State,Double> V = new HashMap<State,Double>();
		//Build V
		for (State s : states)
			V.put(s, new Double(0));
		
		//Learning
		for (State s : states){
			//Action pickup
			if (s.getTaskDest() != null){
				double q = td.reward(s.getPosition(), s.getTaskDest());;
				for (State s2 : states){
					if (s2.getPosition() == s.getTaskDest()){
						q += discount*td.probability(s.getTaskDest(), s2.getTaskDest())*V.get(s2);
					}
				}
				Q.get(s).put(new Action.Pickup(null), new Double(q));
			}
			//Actions Move
			for (City dest : topology.cities()){
				double q = 0;
				for (State s2 : states){
					if (s2.getPosition() == dest){
						q += discount*td.probability(dest, s2.getTaskDest())*V.get(s2);
					}
				}
				Q.get(s).put(new Action.Move(dest),new Double(q));
			}
		}
	}
	
	public ArrayList<State> generateStates(Topology topology){
		ArrayList<State> l = new ArrayList<State>();
		for (City pos : topology.cities()){
			l.add(new State(pos,null));
			for (City dest : topology.cities()){
				l.add(new State(pos,dest));
			}
		}
		return l;
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > 0.5) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
}
