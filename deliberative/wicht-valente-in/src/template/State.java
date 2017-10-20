package template;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Action;
import logist.task.Task;
import logist.topology.Topology.City;

public class State {
	
	private final City city;
	private final List<Task> tasks;
	private final List<Task> toDeliver;
	private final List<Action> actions;
	private final double dist;
	
	
	
	public State(City c, List<Task> t, List<Task> d, List<Action> a, double dist) {
		this.city = c;
		this.tasks = t;
		this.toDeliver = d;
		this.actions = a;
		this.dist = dist;
		
	}
	
	public boolean isFinal() {
		return tasks.isEmpty() && toDeliver.isEmpty();
	}
	
	public boolean equals(State s) {
		return this.city.equals(s.getCity()) && this.tasks.equals(s.getTasks()) && this.toDeliver.equals(s.getToDeliver());
	}
	
	public double distanceToFinal() {
		double distance = 0.0;
		for(Task t: this.tasks) {
			distance += t.pathLength();
		}
		for(Task t: this.toDeliver) {
			distance += this.city.distanceTo(t.deliveryCity);
		}
		return distance;
	}
	
	public double d() {
		return this.dist + this.distanceToFinal();
	}

	public City getCity() {
		return this.city;
	}
	
	public List<Task> getTasks(){
		return this.tasks;
	}
	
	public List<Task> getToDeliver() {
		return this.toDeliver;
	}
	
	public List<Action> getActions(){
		return this.actions;
	}
	
	public double getDist() {
		return this.dist;
	}
	
	public List<Task> localPickupTasks(){
		List<Task> local = new ArrayList<Task>();
		for(Task t: this.tasks) {
			if(t.pickupCity.equals(this.city)) {
				local.add(t);
			}
		}
		return local;
	}
	
	public List<Task> localDeliverTask(){
		List<Task> local = new ArrayList<Task>();
		for(Task t: this.toDeliver) {
			if(t.pickupCity.equals(this.city)) {
				local.add(t);
			}
		}
		return local;
	}
	
	public int weight() {
		int w = 0;
		for(Task t: this.toDeliver) {
			w += t.weight;
		}
		return w;
	}
	
	public State move(City c) {
		List<Action> act = new ArrayList<Action>(this.actions);
		act.add(new Action.Move(c));
		return new State(c, this.tasks, this.toDeliver, act, this.dist + this.city.distanceTo(c));
	}
	
	public State deliver(Task t) {
		List<Task> deliver = new ArrayList<Task>(this.toDeliver);
		deliver.remove(t);
		List<Action> act = new ArrayList<Action>(this.actions);
		act.add(new Action.Delivery(t));
		return new State(this.city, this.tasks, deliver, act, this.dist);
	}
	
	public State pickup(Task t) {
		List<Task> newtasks = new ArrayList<Task>(this.tasks);
		List<Task> deliver = new ArrayList<Task>(this.toDeliver);
		List<Action> act = new ArrayList<Action>(this.actions);
		act.add(new Action.Pickup(t));
		newtasks.remove(t);
		deliver.add(t);
		return new State(this.city, newtasks, deliver, act, this.dist);
	}

}
