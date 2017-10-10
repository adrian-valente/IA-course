package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import template.State;
import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
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
	private ArrayList<State> states;
	private HashMap<State,City> best;
	private final double cost = 1;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.discount = (double)agent.readProperty("discount-factor", Double.class,0.95);
		this.random = new Random();
		this.numActions = 0;
		this.myAgent = agent;
		this.states = generateStates(topology);
		this.best = new HashMap<State,City>();
		
		learn(topology,td);
	}
	
	public void learn(Topology topology, TaskDistribution td){
		System.out.println(this.discount);
		HashMap<State,Double> V = new HashMap<State,Double>();
		double qmax;
		City argmaxQ;
		//Build V 
		for (State s : states){
			V.put(s, new Double(0));
		}
		
		boolean change = true;
		
		//Learning
		while(change){
			change = false;
			for (State s : states){
				qmax = Double.MIN_VALUE;
				argmaxQ = null;
				//Action pickup
				if (s.getTaskDest() != null){
					long q = td.reward(s.getPosition(), s.getTaskDest());
					q -= s.getPosition().distanceTo(s.getTaskDest()) * cost;
					for (State s2 : states){
						if (s2.getPosition() == s.getTaskDest()){
							q += discount*td.probability(s.getTaskDest(), s2.getTaskDest())*V.get(s2);
							
						}
					}
					qmax = q;
				}
				//Actions Move
				for (City dest : s.getPosition().neighbors()){
					double q = 0;
					q -= s.getPosition().distanceTo(dest) * cost;
					for (State s2 : states){
						if (s2.getPosition() == dest){
							q += discount*td.probability(dest, s2.getTaskDest())*V.get(s2);
							
						}
					}
					if (q > qmax){
						qmax = q;
						argmaxQ = dest;
					}
				}
				best.put(s, argmaxQ);
				if(V.get(s) != qmax) {
					change = true;
				}
				V.put(s, qmax);
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
		if (availableTask == null)
			state = findState(vehicle.getCurrentCity(), null);
		else {
			state = findState(vehicle.getCurrentCity(), availableTask.deliveryCity);
		}
		action = new Action.Move(state.getPosition().randomNeighbor(random));
		if(best.containsKey(state)) {
			if(best.get(state) == null) {
				action = new Pickup(availableTask);
			}	
		}
		System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		
		numActions++;
		
		return action;
	}
	
	private State findState(City src, City dest) {
		for(State s: states) {
			if(s.equals(new State(src,dest))) {
				return s;
			}
		}
		return null;
	}
}
