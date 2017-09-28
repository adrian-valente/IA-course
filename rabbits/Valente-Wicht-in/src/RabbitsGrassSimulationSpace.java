import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;
	
	public RabbitsGrassSimulationSpace(int x, int y) {
		grassSpace = new Object2DGrid(x,y);
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
}
