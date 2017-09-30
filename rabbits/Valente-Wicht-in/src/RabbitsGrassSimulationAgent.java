import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.Color;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	
	private int x;
	private int y;
	private int energy;
	private RabbitsGrassSimulationSpace space;
	private final static int BIRTH_ENERGY = 20;
	private final static int GRASS_ENERGY = 5;
	private final static int ENERGY_LOSS = 1;
	
	public RabbitsGrassSimulationAgent(){
		x = -1;
		y = -1;
		energy = 5;
	}

	public void draw(SimGraphics G) {
		G.drawFastRoundRect(Color.orange);
	}
	
	public void step(){
		//Move the agent
		boolean moved = false;
		int i = 0;
		while (moved == false && i<8){
			int direction = (int)(Math.random()*4);
			if (direction==0){
				if (space.moveAgentAt(x,y,x+1,y))
					moved = true;
			}
			else if (direction==1){
				if (space.moveAgentAt(x,y,x,y+1))
					moved = true;
			}
			else if (direction==2){
				if (space.moveAgentAt(x,y,x-1,y))
					moved = true;
			}
			else if (direction==1){
				if (space.moveAgentAt(x,y,x,y-1))
					moved = true;
			}
			i++;
		}
		System.out.println("Agent moved at "+x+"  "+y);
		
		//Eat the grass
		energy += GRASS_ENERGY*space.eatGrassAt(x,y);
		energy -= ENERGY_LOSS;
	}
	
	public void setSpace(RabbitsGrassSimulationSpace s){
		space = s;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int getEnergy(){
		return energy;
	}
	
	public void setXY(int X, int Y){
		x = X;
		y = Y;
	}
}
