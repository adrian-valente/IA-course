import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;
	
	public RabbitsGrassSimulationSpace(int x, int y) {
		grassSpace = new Object2DGrid(x,y);
		agentSpace = new Object2DGrid(x,y);
		for(int i = 0; i < x; ++i) {
			for(int j = 0; j < y; ++j) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}
	
	public void spreadGrass(int grass) {
		for(int i = 0; i < grass; ++i) {
			
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
			int y = (int)(Math.random()*(grassSpace.getSizeY()));
			
			int sum;
			if(grassSpace.getObjectAt(x, y)!=null) {
				sum = ((Integer)(grassSpace.getObjectAt(x, y))).intValue();
			}
			else {
				sum = 0;
			}
			
			grassSpace.putObjectAt(x, y, new Integer(sum+1));
		}
	}

	public Discrete2DSpace getCurrentGrassSpace() {
		return grassSpace;
	}
	
	public Discrete2DSpace getCurrentAgentSpace(){
		return agentSpace;
	}
	
	public boolean isCellOccupied(int x, int y){
		return (agentSpace.getObjectAt(x,y)!=null);
	}
	
	public boolean addAgent(RabbitsGrassSimulationAgent agent){
		boolean success = false;
		int count = 0;
		int countLimit = 10*agentSpace.getSizeX()*agentSpace.getSizeY();
		while (success==false && count < countLimit){
			int x = (int)(Math.random()*(agentSpace.getSizeX()));
			int y = (int)(Math.random()*(agentSpace.getSizeY()));
			if(isCellOccupied(x,y) == false){
				agentSpace.putObjectAt(x,y,agent);
				agent.setXY(x,y);
				success = true;
			}
			count++;
		}
		return success;
	}
}
