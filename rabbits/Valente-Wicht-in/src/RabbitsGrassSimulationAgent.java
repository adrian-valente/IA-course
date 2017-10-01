import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.Color;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	
	private final static int BIRTH_ENERGY = 10;
	private final static int GRASS_ENERGY = 1;
	private final static int ENERGY_LOSS = 1;
	
	private int x;
	private int y;
	private int energy;
	private RabbitsGrassSimulationSpace space;
	
	
	public RabbitsGrassSimulationAgent(){
		x = (int) (Math.random() * 20);
		y = (int) (Math.random() * 20);
		energy = BIRTH_ENERGY;
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
		//System.out.println("Agent moved at "+x+"  "+y);
		
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
	
	public void setEnergy(int dEnergy) {
		energy += dEnergy;
	}
	
	public int getBirthEnergy() {
		return BIRTH_ENERGY;
	}
	
	public void setXY(int X, int Y){
		x = X;
		y = Y;
	}
}
