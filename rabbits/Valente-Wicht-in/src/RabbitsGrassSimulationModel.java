import java.awt.Color;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Value2DDisplay;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		

		private static final int XSIZE = 20;
		private static final int YSIZE = 20;
		private static final int NUMAGENTS = 10;
		private static final int BIRTH_THRESHOLD = 3;
		private static final int GRASS_GROWTH_RATE = 5;
		
		private int x = XSIZE;
		private int y = YSIZE;
		private int numAgents = NUMAGENTS;
		private int birthThreshold = BIRTH_THRESHOLD;
		private int grassGrowth = GRASS_GROWTH_RATE;
		
		private Schedule schedule;
		
		private RabbitsGrassSimulationSpace space;
		
		private DisplaySurface surface;
	
		public static void main(String[] args) {
			
			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			init.loadModel(model, "", false);
			System.out.println("Rabbit skeleton");
			
		}
		
		public String getName() {
			return "My first Repast Model";
		}
		
		public void setup() {
			space = null;
			
			if(surface != null) {
				surface.dispose();
			}
			surface = null;
			
			surface = new DisplaySurface(this, "Rabbits love grass");
			registerDisplaySurface("Rabbits love grass", surface);
			
		}

		public void begin() {
			buildModel();
			buildSchedule();
			buildDisplay();	
			
			surface.display();
		}
		
		public void buildModel(){
			space = new RabbitsGrassSimulationSpace(x,y);
			space.spreadGrass(grassGrowth);
		}

		public void buildSchedule(){
		}

		public void buildDisplay(){
			ColorMap map = new ColorMap();
			for(int i = 1; i < 16; ++i) {
				map.mapColor(i, new Color(0, ((int)(127 + 8*i)), 0));
			}
			map.mapColor(0, Color.WHITE);
			Value2DDisplay displayGrass = new Value2DDisplay(space.getCurrentGrassSpace(), map);
			surface.addDisplayable(displayGrass, "Grass");
		}
		
		public Schedule getSchedule() {
			return schedule;
		}
		
		public int getNumAgents() {
			return numAgents;
		}
		
		public void setNumAgents(int na) {
			numAgents = na;
		}

		public String[] getInitParam() {
			String[] initParam = {"numAgents", "x", "y", "birthThreshold", "grassGrowth"};
			return initParam;
		}	
}
