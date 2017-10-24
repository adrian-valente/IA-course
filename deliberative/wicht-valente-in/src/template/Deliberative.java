package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Deliberative implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }

	/* Environment */
	Topology topology;
	TaskDistribution td;

	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;

		// initialize the planner
		capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

		// ...
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		State state;
		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			state = ASTAR(vehicle, tasks);
			break;
		case BFS:
			state = BFS(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}

		List<Action> actions = state.getActions();
		double dist = state.d();
		for(Action a: actions) {
			System.out.println(a);
		}
		System.out.println(dist);
		Plan plan = new Plan(vehicle.getCurrentCity(), actions);
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		// TODO Auto-generated method stub

	}

	public List<State> transit(State s, int capacity) {
		List<State> states = new ArrayList<State>();
		City c = s.getCity();
		List<Task> carried = new ArrayList<Task>();

		
		for(Task t: s.getToDeliver()) {
			if(t.deliveryCity.equals(c)) {
				states.add(s.deliver(t));
			}
			else {
				carried.add(t);
			}
		}
		//if there is no task carried, try to move to each neighbor city
		if(carried.isEmpty()) {
			for(City n: c.neighbors()) {
				states.add(s.move(n));
			}
		}

		//try to move to the next city on the path to the delivery city of each task no taking any new task
		else {
			for(Task t: carried) {
				if(! t.deliveryCity.equals(c)) {
					City next = c.pathTo(t.deliveryCity).get(0);
					states.add(s.move(next));
				}
			}
		}

		int freeCharge = capacity - s.weight();
		//try to pickup each task available in the current city
		for(Task t: s.localPickupTasks()) {
			if(freeCharge >= t.weight) {
				states.add(s.pickup(t));
			}
		}

		//for each successor state, deliver tasks if possible
		for(State state: states) {
			List<Task> local = state.localDeliverTask();
			for(Task t: local) {
				state = state.deliver(t);
			}
		}


		return states;
	}


	private State BFS(Vehicle v, TaskSet t) {

		City city = v.getCurrentCity();
		ArrayList<Task> tasks = new ArrayList<Task>(t);
		ArrayList<Task> toDeliver = new ArrayList<Task>(v.getCurrentTasks());
		State init = new State(city, tasks, toDeliver, Collections.<Action>emptyList(), 0.0);

		//Q = initial state
		List<State> q = new ArrayList<State>();
		q.add(init);

		//C are the visited states
		List<State> c = new ArrayList<State>();

		while(! q.isEmpty()) {
			State node = q.get(0);
			q.remove(0);
			if(node.isFinal()) {
				return node;
			}

			//check if already passed at this state and put state in visited states
			boolean cycle = false;
			for(State s: c) {
				if(s.equals(node)) {
					cycle = true;
					break;
				}
			}
			if(! cycle) {
				c.add(node);
				q.addAll(transit(node, v.capacity()));
			}
		}
		return null;
	}

	private State ASTAR(Vehicle v, TaskSet t) {

		City city = v.getCurrentCity();
		ArrayList<Task> tasks = new ArrayList<Task>(t);
		ArrayList<Task> toDeliver = new ArrayList<Task>(v.getCurrentTasks());
		State init = new State(city, tasks, toDeliver, Collections.<Action>emptyList(), 0.0);

		//Q = initial state
		List<State> q = new ArrayList<State>();
		q.add(init);

		//C are the visited states
		List<State> c = new ArrayList<State>();

		while(! q.isEmpty()) {
			State node = q.get(0);
			q.remove(0);
			if(node.isFinal()) {
				return node;
			}

			//find copy of node in C if it exists
			State cCopy = null;
			for(State s: c) {
				if(s.equals(node)) {
					cCopy = s;
					break;
				}
			}

			if(cCopy == null || node.getDist() < cCopy.getDist()) {
				c.remove(cCopy);
				c.add(node);
				q.addAll(transit(node, v.capacity()));

				//sort q
				Collections.sort(q, new Comparator<State>() {
					public int compare(State s1, State s2) {
						return s1.d() < s2.d() ? -1 : s1.d() == s2.d() ? 0 : 1;
					}
				});
			}
		}
		return null;
	}
}
