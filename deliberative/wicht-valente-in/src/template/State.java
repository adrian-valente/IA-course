package template;

import java.util.ArrayList;

import logist.task.Task;
import logist.topology.Topology.City;

public class State {
	
	private final City city;
	private final ArrayList<Task> tasks;
	private final ArrayList<Task> toDeliver;
	
	public State(City c, ArrayList<Task> t, ArrayList<Task> d) {
		this.city = c;
		this.tasks = t;
		this.toDeliver = d;
		
	}
	
	public boolean isFinal() {
		return tasks.isEmpty();
	}
	
	public boolean equals(State s) {
		return this.city.equals(s.getCity()) && this.tasks.equals(s.getTasks()) && this.toDeliver.equals(s.getToDeliver());
	}
	
	public double distance() {
		double dist = 0.0;
		for(Task t: this.tasks) {
			dist += t.pathLength();
		}
		for(Task t: this.toDeliver) {
			dist += this.city.distanceTo(t.deliveryCity);
		}
		return dist;
	}

	public City getCity() {
		return this.city;
	}
	
	public ArrayList<Task> getTasks(){
		return this.tasks;
	}
	
	public ArrayList<Task> getToDeliver() {
		return this.toDeliver;
	}
}
