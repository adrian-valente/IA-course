package template;

import java.util.ArrayList;
import java.util.Collections;

import logist.task.Task;
import logist.topology.Topology.City;

public class State {
	
	private final City city;
	private final ArrayList<Task> tasks;
	
	public State(City c, ArrayList<Task> t) {
		this.city = c;
		this.tasks = t;
	}
	
	public boolean isFinal() {
		return tasks.isEmpty();
	}
	
	public boolean equals(State s) {
		return this.city.equals(s.getCity()) && this.tasks.equals(s.getTasks());
	}
	
	public City getCity() {
		return this.city;
	}
	
	public ArrayList<Task> getTasks(){
		return (ArrayList<Task>) Collections.unmodifiableList(tasks);
	}

}
