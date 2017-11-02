package src.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logist.task.Task;
import logist.task.TaskSet;
import logist.simulation.Vehicle;


public class Solution {
	
	private HashMap<Vehicle,TaskAction> nextActionVehicles;
	private HashMap<TaskAction,TaskAction> nextActionTasks;
	private HashMap<TaskAction,Integer> time;
	private HashMap<Task,Vehicle> vehicle;
	//These two are helpers to map between Tasks and TaskActions
	private HashMap<Task,TaskAction> pickups;
	private HashMap<Task,TaskAction> deliveries;
	
	/*
	 * This constructor builds a naive solution by giving everything to the first vehicle
	 * in naive order
	 */
	public Solution(List<Vehicle> vehicles, TaskSet tasks){
		//First build the set of TaskActions
		pickups = new HashMap<Task,TaskAction>();
		deliveries = new HashMap<Task,TaskAction>();
		for (Task t : tasks){
			pickups.put(t, new TaskAction(true, t));
			deliveries.put(t, new TaskAction(false, t));
		}
		
		//Then give everything in order to the first vehicle
		//Building HashMaps
		nextActionVehicles = new HashMap<Vehicle,TaskAction>();
		nextActionTasks = new HashMap<TaskAction,TaskAction>();
		time = new HashMap<TaskAction,Integer>();
		vehicle = new HashMap<Task,Vehicle>();
		List<Task> tasksList = new ArrayList<Task>(tasks);
		//Putting tasks
		//First task is different : we first map from Vehicle to TaskAction
		Vehicle vehicle0 = vehicles.get(0);
		TaskAction curAction = pickups.get(tasksList.get(0));
		this.putNextAction(vehicle0, curAction);
		time.put(curAction, new Integer(0));
		vehicle.put(curAction.task, vehicle0);
		
		this.putNextAction(curAction, deliveries.get(tasksList.get(0)));
		curAction = deliveries.get(tasksList.get(0));
		time.put(curAction, new Integer(1));
		vehicle.put(curAction.task, vehicle0);
		
		for (int i=1; i<tasksList.size(); i++){
			this.putNextAction(curAction, pickups.get(tasksList.get(i)));
			curAction = pickups.get(tasksList.get(i));
			time.put(curAction, new Integer(2*i));
			vehicle.put(curAction.task, vehicle0);
			
			this.putNextAction(curAction, deliveries.get(tasksList.get(0)));
			curAction = deliveries.get(tasksList.get(0));
			time.put(curAction, new Integer(2*i+1));
			vehicle.put(curAction.task, vehicle0);
		}
		this.putNextAction(curAction, null);
		
		//Finally, put null for other vehicles.
		for (Vehicle v : vehicles){
			this.putNextAction(v, null);
		}
		
	}
	
	/*
	 * This constructor creates a copy of a Solution instance
	 */
	public Solution(Solution sol){
		this.nextActionVehicles = new HashMap<Vehicle,TaskAction>(sol.getNextActionVehicles());
		this.nextActionTasks = new HashMap<TaskAction,TaskAction>(sol.getNextActionTasks());
		this.time = new HashMap<TaskAction,Integer>(sol.getTime());
		this.vehicle = new HashMap<Task,Vehicle>(sol.getVehicle());
		this.pickups = sol.getPickups();
		this.deliveries = sol.getDeliveries();
	}
	
	/*
	 * Compute the cost of an solution
	 */
	public double cost(){
		double cost = 0.;
		for (Vehicle v : this.nextActionVehicles.keySet()){
			TaskAction curAction = nextActionVehicles.get(v);
			if (curAction == null)
				continue;
			cost += v.getCurrentCity().distanceTo(curAction.city);
			TaskAction nextAction = this.nextActionTasks.get(curAction);
			while (nextAction != null){
				cost += curAction.city.distanceTo(nextAction.city);
				curAction = nextAction;
				nextAction = this.nextActionTasks.get(curAction);
			}
		}
		return cost;
	}
	
	
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	public TaskAction getNextAction(Vehicle v){
		return nextActionVehicles.get(v);
	}
	
	public TaskAction getNextAction(TaskAction t){
		return nextActionTasks.get(t);
	}
	
	public void putNextAction(Vehicle v, TaskAction t){
		this.nextActionVehicles.put(v,t);
	}
	
	public void putNextAction(TaskAction t1, TaskAction t2){
		this.nextActionTasks.put(t1,t2);
	}

	public HashMap<Vehicle, TaskAction> getNextActionVehicles() {
		return nextActionVehicles;
	}

	public HashMap<TaskAction, TaskAction> getNextActionTasks() {
		return nextActionTasks;
	}

	public HashMap<TaskAction, Integer> getTime() {
		return time;
	}

	public HashMap<Task, Vehicle> getVehicle() {
		return vehicle;
	}

	public HashMap<Task, TaskAction> getPickups() {
		return pickups;
	}

	public HashMap<Task, TaskAction> getDeliveries() {
		return deliveries;
	}
	
	
}
