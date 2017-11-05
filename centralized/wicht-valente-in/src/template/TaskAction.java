package template;

import logist.task.Task;
import logist.topology.Topology.City;

public class TaskAction {
	
	public boolean is_pickup;
	public Task task;
	public City city;
	
	
	public TaskAction(boolean is_pickup, Task task) {
		super();
		this.is_pickup = is_pickup;
		this.task = task;
		if (is_pickup){
			this.city = task.pickupCity;
		}
		else{
			this.city = task.deliveryCity;
		}
	}	

}
