package template;
import logist.topology.Topology.City;

public class State {
	private City position;
	private City taskDest; //can be null if no task available

	public State(City position, City taskDest) {
		this.position = position;
		this.taskDest = taskDest;
	}

	public City getPosition() {
		return position;
	}

	public void setPosition(City position) {
		this.position = position;
	}

	public City getTaskDest() {
		return taskDest;
	}

	public void setTaskDest(City taskDest) {
		this.taskDest = taskDest;
	}
	
	@Override
	public String toString(){
		return "("+this.position+"("+this.position.id+")"+","+this.taskDest+")";
	}
	
	public boolean equals(State s){
		if (taskDest == null && s.getTaskDest() == null)
			return position.equals(s.getPosition());
		else if (taskDest != null && s.getTaskDest() != null)
			return (position.equals(s.getPosition())) && (taskDest.equals(s.getTaskDest()));
		else
			return false;
	}

	@Override
	public int hashCode(){
		if (taskDest == null)
			return 256*position.hashCode();
		else
			return 256*position.hashCode()+1+taskDest.hashCode();
	}

}
