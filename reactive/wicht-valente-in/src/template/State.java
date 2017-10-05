package template;
import logist.task.Task;
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


}
