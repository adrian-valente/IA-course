import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;
	private int X_SIZE,Y_SIZE;
	
	public RabbitsGrassSimulationSpace(int x, int y) {
		grassSpace = new Object2DGrid(x,y);
		agentSpace = new Object2DGrid(x,y);
		for(int i = 0; i < x; ++i) {
			for(int j = 0; j < y; ++j) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
		X_SIZE = x;
		Y_SIZE = y;
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
	
	public boolean moveAgentAt(int x, int y, int newX, int newY){
	    boolean retVal = false;
	    newX = newX % X_SIZE;
	    if (newX<0)
	    	newX+=X_SIZE;
	    newY = newY % Y_SIZE;
	    if (newY<0)
	    	newY+=Y_SIZE;
	    System.out.println(newX+"  "+newY);
	    if(!isCellOccupied(newX, newY)){
	      RabbitsGrassSimulationAgent a = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
	      removeAgentAt(x,y);
	      a.setXY(newX, newY);
	      agentSpace.putObjectAt(newX, newY, a);
	      retVal = true;
	    }
	    return retVal;
	  }
	
	public void removeAgentAt(int x, int y){
		agentSpace.putObjectAt(x, y, null);
	}
	
	public int eatGrassAt(int x, int y){
		int grass = (int)grassSpace.getObjectAt(x, y);
		if (grass!=0){
			grassSpace.putObjectAt(x,y,grass-1);
		}
		return grass;
	}
}
