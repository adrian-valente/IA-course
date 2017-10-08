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
	private HashMap<State,Action> best;	

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.discount = (double)agent.readProperty("discount-factor", Double.class,0.95);
		this.random = new Random();
		this.numActions = 0;
		this.myAgent = agent;
		this.best = new HashMap<State,Action>();
		
		learn(topology,td);
	}
	
	public void learn(Topology topology, TaskDistribution td){
		System.out.println("Learning...");
		ArrayList<State> states = generateStates(topology);
		//HashMap<State,HashMap<Action,Double>> Q = new HashMap<State,HashMap<Action,Double>>();
		HashMap<State,Double> V = new HashMap<State,Double>();
		double qmax;
		Action argmaxQ;
		//Build V and Q
		for (State s : states){
			V.put(s, new Double(0));
			//Q.put(s, new HashMap<Action,Double>());
		}
		
		//Learning
		for (int i=0; i<100; i++){
			System.out.println("Loop "+i);
			for (State s : states){
				qmax = Double.MIN_VALUE;
				argmaxQ = null;
				//Action pickup
				if (s.getTaskDest() != null){
					double q = td.reward(s.getPosition(), s.getTaskDest());;
					for (State s2 : states){
						if (s2.getPosition() == s.getTaskDest()){
							q += discount*td.probability(s.getTaskDest(), s2.getTaskDest())*V.get(s2);
						}
					}
					qmax = q;
					argmaxQ = new Action.Pickup(null);
					//Q.get(s).put(new Action.Pickup(null), new Double(q));
				}
				//Actions Move
				for (City dest : topology.cities()){
					double q = 0;
					for (State s2 : states){
						if (s2.getPosition() == dest){
							q += discount*td.probability(dest, s2.getTaskDest())*V.get(s2);
						}
					}
					if (q > qmax){
						qmax = q;
						argmaxQ = new Action.Move(dest);
					}
					//Q.get(s).put(new Action.Move(dest),new Double(q));
				}
				best.put(s, argmaxQ);
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
		State s1 = new State(vehicle.getCurrentCity(),null);
		Action action;
		System.out.println("Action "+numActions);
		if (availableTask == null)
			state = new State(vehicle.getCurrentCity(), null);
		else
			state = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
		if (best.containsKey(state))
			action = best.get(state);
		else{
			action = new Action.Move(state.getPosition().randomNeighbor(random));
			System.out.println("No mapping for state "+state);
		}
		System.out.println(s1.equals(state));
		System.out.println(s1);
		
		if (action instanceof Action.Pickup){
			action = new Pickup(availableTask);
		}
		
		System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		
		numActions++;
		
		return action;
	}
}
