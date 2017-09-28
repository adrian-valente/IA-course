import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.awt.Color;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	
	private int x;
	private int y;
	private int energy;
	
	public RabbitsGrassSimulationAgent(){
		x = -1;
		y = -1;
		energy = 1;
	}

	public void draw(SimGraphics G) {
		G.drawFastRoundRect(Color.orange);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public void setXY(int X, int Y){
		x = X;
		y = Y;
	}
}
