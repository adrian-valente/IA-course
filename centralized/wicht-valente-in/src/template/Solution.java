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
		//Putting tasks
		//First task is different : we first map from Vehicle to TaskAction
		List<List<Task>> divided = divideTasks(tasks, vehicles.size());
		
		for(int j = 0; j< vehicles.size(); j++) {
			List<Task> tasksList = divided.get(j);
			Vehicle v = vehicles.get(j);
			TaskAction curAction = pickups.get(tasksList.get(0));
			this.putNextAction(v, curAction);
			time.put(curAction, new Integer(0));
			vehicle.put(curAction.task, v);
			
			this.putNextAction(curAction, deliveries.get(tasksList.get(0)));
			curAction = deliveries.get(tasksList.get(0));
			time.put(curAction, new Integer(1));
			vehicle.put(curAction.task, v);
			
			for (int i=1; i<tasksList.size(); i++){
				this.putNextAction(curAction, pickups.get(tasksList.get(i)));
				curAction = pickups.get(tasksList.get(i));
				time.put(curAction, new Integer(2*i));
				vehicle.put(curAction.task, v);
				
				this.putNextAction(curAction, deliveries.get(tasksList.get(i)));
				curAction = deliveries.get(tasksList.get(i));
				time.put(curAction, new Integer(2*i+1));
				vehicle.put(curAction.task, v);
			}
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
	
	
	/**
	 * Functions to help generate neighbors
	 */
	/*
	 * This function moves the task t from vehicle vsrc to the beginning of the 
	 * plan for vehicle vdest
	 */
	public boolean changeVehicle(Task t, Vehicle vsrc, Vehicle vdest){
		if (t.weight > vdest.capacity()){
			return false;
		}
		
		//First look for the pickup of the required task
		TaskAction curAction = getNextAction(vsrc);
		TaskAction prevAction = null;
		while (! (curAction.task.equals(t) && curAction.is_pickup)){
			prevAction = curAction;
			curAction = getNextAction(curAction);
		}
		//We found it: so we make the necessary changes to the nextAction table
		if (prevAction == null){
			putNextAction(vsrc,getNextAction(curAction));
		} else {
			putNextAction(prevAction,getNextAction(curAction));
		}
		TaskAction tmpAction = getNextAction(curAction); //the current action 
								//following curAction in vsrc's plan: we will need it later
		putNextAction(curAction,getNextAction(vdest));
		putNextAction(vdest,curAction);
		//We must also update the time table for all following actions
		time.put(curAction, 0);
		curAction = tmpAction;
		while(curAction != null){
			time.put(curAction, time.get(curAction) - 1);
			curAction = getNextAction(curAction);
		}
		
		//Now we start looking for the delivery action of the required task (should be
		//after the pickup)
		curAction = tmpAction;
		while (! (curAction.task.equals(t) && !curAction.is_pickup)){
			prevAction = curAction;
			curAction = getNextAction(curAction);
		}
		//We found it: we insert it in the second position of the plan of vdest
		if (prevAction == null){
			putNextAction(vsrc,getNextAction(curAction));
		} else {
			putNextAction(prevAction,getNextAction(curAction));
		}
		tmpAction = getNextAction(curAction);
		putNextAction(curAction,getNextAction(getNextAction(vdest)));
		putNextAction(getNextAction(vdest),curAction);
		time.put(curAction, 1);
		curAction = tmpAction;
		while(curAction != null){
			time.put(curAction, time.get(curAction) - 1);
			curAction = getNextAction(curAction);
		}
		
		//Finally, we need to update the vehicle table
		vehicle.put(t, vdest);
		
		return true;
		
	}
	
	/*
	 * Very important: we assume time(a1) < time(a2) !! 
	 */
	public boolean permuteActions(Vehicle v, TaskAction a1, TaskAction a2){
		//We first verify the integrity of the desired permutation:
		if (a1.task.equals(a2.task)){
			return false;
		}
		if (a1.is_pickup){
			if (time.get(deliveries.get(a1.task)) < time.get(a2)){
				return false;
			}
		}
		else {
			//Nothing to check if a1 is a delivery
		}
		if (a2.is_pickup){
			//Nothing to check
		}
		else {
			if (time.get(pickups.get(a2.task)) > time.get(a1)){
				return false;
			}
		}
		
		TaskAction prevAction1 = null;
		TaskAction nextAction1 = null;
		TaskAction prevAction2 = null;
		TaskAction nextAction2 = null;
		TaskAction curAction = getNextAction(v);
		TaskAction prevAction = null;
		
		boolean found1 = false;
		boolean found2 = false;
		while(!found1 || !found2){
			if (curAction.equals(a1)){
				prevAction1 = prevAction;
				nextAction1 = getNextAction(curAction);
				found1 = true;
			}
			if (curAction.equals(a2)){
				prevAction2 = prevAction;
				nextAction2 = getNextAction(curAction);
				found2 = true;
			}
			
			prevAction = curAction;
			curAction = getNextAction(curAction);
		}
		
		//Now we proceed to the permutation:
		if (prevAction1 == null){ //in case a1 is the first action
			putNextAction(v, a2);
		} else {
			putNextAction(prevAction1, a2);
		}
		if(nextAction1.equals(a2)) {
			putNextAction(a2,a1);
		}
		else {
			putNextAction(a2, nextAction1);
		}
		//a2 should not be the first action
		if(!prevAction2.equals(a1)) {
			putNextAction(prevAction2, a1);
		}
		putNextAction(a1, nextAction2);
		//update time
		int tmp = time.get(a1);
		time.put(a1, time.get(a2));
		time.put(a2, tmp);
		
		return true;
	}
	
	private List<List<Task>> divideTasks(TaskSet tasks, int i){
		List<List<Task>> res = new ArrayList<List<Task>>();
		for(int j = 0; j< i; ++j) {
			res.add(new ArrayList<Task>());
		}
		
		for(Task t: tasks) {
			int pos = (int) Math.round(Math.floor(Math.random()*i));
			res.get(pos).add(t);
		}
		return res;
	}
	
}
