package template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class Solution {
	
	//Task -> [vehicleNB, pickup position, delivery position]
	private Map<Task, int[]> actionsState;
	private List<List<TaskAction>> taskActions;
	private List<Vehicle> vehicles;
	boolean valid;
	
	public Solution(List<Vehicle> vehicles, Collection<Task> tasks) {
		actionsState = new HashMap<Task, int[]>();
		taskActions = new ArrayList<List<TaskAction>>();
		this.vehicles = vehicles;
		
		for (int i = 0; i < vehicles.size(); i++) {
			taskActions.add(new ArrayList<TaskAction>());
		}
		
		for(Task t : tasks) {
			int nV = (int) Math.floor(Math.random() * vehicles.size());
			taskActions.get(nV).add(new TaskAction(true, t));
			taskActions.get(nV).add(new TaskAction(false, t));
		}
		actionsState = buildStateFromLists(taskActions);
//		List<TaskAction> l;
		// for each vehicle, shuffle its tasks until we get a legal path (no overweight).
//		for(int i = 0; i < taskActions.size(); ++i) {
//			do {
//				l = taskActions.get(i);
//				Collections.shuffle(l);
//				taskActions.set(i, l);
//				actionsState = buildStateFromLists(taskActions);
//				taskActions = buildListFromStates(actionsState, vehicles.size());
//			}while(! pathIsGood(taskActions.get(i), vehicles.get(i).capacity()));
//		}
		valid = true;
//		if(taskActions.size() != vehicles.size()) {
//			valid = false;
//			return;
//		}
//		for(int i = 0; i < taskActions.size(); ++i) {
//			if(! pathIsGood(taskActions.get(i), vehicles.get(i).capacity())) {
//				valid = false;
//				break;
//			}
//		}
	}
	
	
	public Solution(List<List<TaskAction>> tA, List<Vehicle> vehicles) {
		this.vehicles = vehicles;
		actionsState = buildStateFromLists(tA);
		taskActions = buildListFromStates(actionsState, vehicles.size());
		valid = true;
		if(tA.size() != vehicles.size()) {
			valid = false;
			return;
		}
		for(int i = 0; i < tA.size(); ++i) {
			if(! pathIsGood(taskActions.get(i), vehicles.get(i).capacity())) {
				valid = false;
				break;
			}
		}
	}
	
	public double cost() {
		double c = 0.;
		for(int i = 0; i < vehicles.size(); ++i) {
			City last = vehicles.get(i).getCurrentCity();
			for(int j = 0; j < taskActions.get(i).size(); ++j) {
				City city = taskActions.get(i).get(j).city;
				c += last.distanceTo(city) * vehicles.get(i).costPerKm();
				last = city;
			}
		}
		return c;
	}
	
	
	private boolean pathIsGood(List<TaskAction> tA, int vW) {
		int w = 0;
		for(TaskAction t : tA) {
			if(t.is_pickup) {
				w += t.task.weight;
			}
			else {
				w -= t.task.weight;
			}
			if(w > vW) {
				return false;
			}
		}
		return true;
	}
	
	
	private Map<Task, int[]> buildStateFromLists(List<List<TaskAction>> taskActions) {
		
		int[] def = new int[] {-1,-1,-1};
		// Create empty:
		Map<Task, int[]> tA = new HashMap<Task, int[]>();
		for(int v = 0; v < taskActions.size(); ++v) {
			for(int a = 0; a < taskActions.get(v).size(); ++a) {
				TaskAction ta = taskActions.get(v).get(a);
				if(ta.is_pickup) {
					int d = tA.getOrDefault(ta.task, def)[2];
					tA.put(ta.task, new int[]{v,a,d});
							
				}
				else {
					int d = tA.getOrDefault(ta.task, def)[1];
					tA.put(ta.task, new int[]{v,d,a});
				}
			}
		}
		return tA;
		
	}
	
	
	private List<List<TaskAction>> buildListFromStates(Map<Task, int[]> states, int nV){
		int[] taskperV = new int[nV];
		for(int i = 0; i < nV; ++i) {
			taskperV[i] = 0;
		}
		// compute number of taskActions per vehicle
		for(Task t : states.keySet()) {
			taskperV[states.get(t)[0]] += 2;
		}
		
		
		List<List<TaskAction>> tA = new ArrayList<List<TaskAction>>();
		//initialize vehicule lists with null values
		for(int i = 0; i < taskperV.length; ++i) {
			int p = 0;
			ArrayList<TaskAction> list = new ArrayList<TaskAction>();
			while(p < taskperV[i]) {
				list.add(null);
				++p;
			}
			tA.add(list);
		}
		
		//construct the list in placing each taskAction at its place, controlling that a delivery happens after a pickup
		for(Task t : states.keySet()) {
			int vidx = states.get(t)[0];
			int pidx = states.get(t)[1];
			int didx = states.get(t)[2];
			List<TaskAction> lV = tA.get(vidx);
			if(pidx < didx) {
				
				lV.set(pidx, new TaskAction(true, t));
				lV.set(didx, new TaskAction(false, t));
			}
			else {
				lV.set(pidx, new TaskAction(false, t));
				lV.set(didx, new TaskAction(true, t));
				int[] arr = {vidx,didx,pidx};
				actionsState.put(t, arr);
			}
			//tA.set(vidx, lV);
		}
		
		return tA;
	}
	
	
	private int nearestVehicle(List<Vehicle> vehicles, Task t) {
		int nearest = 0;
		double dist = vehicles.get(nearest).getCurrentCity().distanceTo(t.pickupCity);
		if(vehicles.size() == 1) {
			return 0;
		}
		for(int i = 1; i < vehicles.size(); ++i) {
			double d = vehicles.get(i).getCurrentCity().distanceTo(t.pickupCity);
			if(d < dist) {
				dist = d;
				nearest = i;
			}
		}
		
		// check that nearest vehicle has enough capacity
		if(vehicles.get(nearest).capacity() < t.weight) {
			for(int i = 0; i < vehicles.size(); ++i) {
				if(vehicles.get(i).capacity() < t.weight) {
					nearest = i;
					break;
				}
			}
		}
		
		return nearest;
	}
	
	public List<Solution> changeVehicule(Task t){
		
		int v = actionsState.get(t)[0];
		int p = actionsState.get(t)[1];
		int d = actionsState.get(t)[2];
		
		// Remove from current vehicle
		List<TaskAction> vehicle = new ArrayList<TaskAction>(taskActions.get(v));
		vehicle.remove(d);
		vehicle.remove(p);
		//taskActions.set(v, vehicle);
		
		List<Solution> solutions = new ArrayList<Solution>();
		//try to put it in each other vehicle
		for(int i = 0; i < vehicles.size(); ++i) {
			List<List<TaskAction>> tA = new ArrayList<List<TaskAction>>(taskActions);
			tA.set(v, vehicle);
			if(t.weight < vehicles.get(i).capacity() && i != v) {
				List<TaskAction> a = new ArrayList<TaskAction>(tA.get(i));
				a.add(0, new TaskAction(false, t));
				a.add(0, new TaskAction(true, t));
				tA.set(i, a);
				Solution sol = new Solution(tA, vehicles);
				if(sol.valid) {
					solutions.add(sol);
				}
			}
		}
		return solutions;
	}
	
	private List<Solution> permutations(){
		List<Solution> solutions = new ArrayList<Solution>();
		
		for(int v = 0; v < taskActions.size(); ++v) {
			List<List<TaskAction>> tA = new ArrayList<List<TaskAction>>(taskActions);
			
			for(int i = 0; i < taskActions.get(v).size(); ++i) {
				for(int j = i; j < taskActions.get(v).size(); ++j) {
					List<TaskAction> list = new ArrayList<TaskAction>(taskActions.get(v));
					TaskAction t1 = taskActions.get(v).get(i);
					TaskAction t2 = taskActions.get(v).get(j);
					list.set(i, t2);
					list.set(j, t1);
					tA.set(v, list);
					Solution sol = new Solution(tA, vehicles);
					if(sol.valid) {
						solutions.add(sol);
					}
				}
			}
		}
		return solutions;
	}
	
	public List<Plan> generatePlan(){
		List<Plan> plans = new ArrayList<Plan>();
		for (int i = 0; i < taskActions.size(); ++i) {
			Vehicle v = vehicles.get(i);
			City curCity = v.getCurrentCity();
			Plan plan = new Plan(curCity);
			List<TaskAction> tActions = taskActions.get(i);
			for(TaskAction ta : tActions) {
				for (City c : curCity.pathTo(ta.city)){
					plan.appendMove(c);
				}

				if (ta.is_pickup){
					plan.appendPickup(ta.task);
				} else {
					plan.appendDelivery(ta.task);
				}
				curCity = ta.city;
			}
			plans.add(plan);
		}
		return plans;
	}
	
	public List<Solution> generateNeighbors(){
		List<Solution> solutions = new ArrayList<Solution>();
		Set<Task> tasks = actionsState.keySet();
		for(Task t: tasks) {
			solutions.addAll(changeVehicule(t));
		}
		solutions.addAll(permutations());
		return solutions;
	}
	
	public String toString(){
		String res = "";
		int i = 0;
		for (List<TaskAction> l : taskActions){
			res += "\n\nVEHICLE "+i;
			for (TaskAction t : l){
				res += "\n      "+t;
			}
			i++;
		}
		return res;
	}

}
