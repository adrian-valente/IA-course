import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
		//Default values
		private static final int XSIZE = 20;
		private static final int YSIZE = 20;
		private static final int NUMAGENTS = 20;
		private static final int BIRTH_THRESHOLD = 12;
		private static final int GRASS_GROWTH_RATE = 30;
		
		//Parameters
		private int x = XSIZE;
		private int y = YSIZE;
		private int numAgents = NUMAGENTS;
		private int birthThreshold = BIRTH_THRESHOLD;
		private int grassGrowth = GRASS_GROWTH_RATE;
		
		//Objects
		private Schedule schedule;
		private RabbitsGrassSimulationSpace space;
		private DisplaySurface surface;
		private ArrayList<RabbitsGrassSimulationAgent> agentList;
		private OpenSequenceGraph curNumRabbits;
		private OpenSequenceGraph curNumGrass;
		
		//Inner classes
		class RabbitsInSpace implements DataSource, Sequence {
			public Object execute(){
				return new Double(getSValue());
			}
			
			public double getSValue(){
				return (double)space.getTotalRabbits();
			}
		}
		
		class GrassInSpace implements DataSource, Sequence {
			public Object execute(){
				return new Double(getSValue());
			}
			
			public double getSValue(){
				return (double)space.getTotalGrass();
			}
		}
	
		
		public static void main(String[] args) {
			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			init.loadModel(model, "", false);
			System.out.println("Rabbit skeleton");
		}
		
		
		@SuppressWarnings("unchecked")
		public void setup() {
			//Tearing down if necessary
			space = null;
			if(surface != null) {
				surface.dispose();
			}
			surface = null;
			if (curNumRabbits!=null)
				curNumRabbits.dispose();
			curNumRabbits = null;
			if (curNumGrass!=null)
				curNumGrass.dispose();
			curNumGrass = null;
			
			//Create objects
			surface = new DisplaySurface(this, "Rabbits love grass");
			agentList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			curNumRabbits = new OpenSequenceGraph("Number of Rabbits in Space",this);
			curNumGrass = new OpenSequenceGraph("Amount of Grass in Space", this);
			
			//Register Displays
			registerDisplaySurface("Rabbits love grass", surface);
			this.registerMediaProducer("Plot", curNumRabbits);
			this.registerMediaProducer("Plot", curNumGrass);
			
			
			//Create the sliders for each parameter the user can set
			RangePropertyDescriptor dAgents = new RangePropertyDescriptor("NumAgents", 0, 40, 8);
			descriptors.put("NumAgents", dAgents);
			RangePropertyDescriptor dX = new RangePropertyDescriptor("X", 0, 30, 6);
			descriptors.put("X", dX);
			RangePropertyDescriptor dY = new RangePropertyDescriptor("Y", 0, 30, 6);
			descriptors.put("Y", dY);
			RangePropertyDescriptor dGrass = new RangePropertyDescriptor("GrassGrowth", 0, 60, 12);
			descriptors.put("GrassGrowth", dGrass);
			RangePropertyDescriptor dBirth = new RangePropertyDescriptor("BirthThreshold", 5, 15, 2);
			descriptors.put("BirthThreshold", dBirth);
			
		}

		public void begin() {
			buildModel();
			buildSchedule();
			buildDisplay();	
			
			surface.display();
			curNumRabbits.display();
			curNumGrass.display();
		}
		
		public void buildModel(){
			space = new RabbitsGrassSimulationSpace(x,y);
			space.spreadGrass(grassGrowth);
			for (int i=0; i<numAgents; i++)
				addNewAgent();
		}

		public void buildSchedule(){
			//Main action: move rabbits, grow grass...
			class RabbitsGrassStep extends BasicAction{
				public void execute(){
					SimUtilities.shuffle(agentList);
					for (int i=0;i<agentList.size();i++){
						RabbitsGrassSimulationAgent a = (RabbitsGrassSimulationAgent) agentList.get(i);
						a.step();
					}
					space.spreadGrass(grassGrowth);
					populationStep();
					surface.updateDisplay();
				}
			}
			schedule.scheduleActionBeginning(0, new RabbitsGrassStep());
			
			//Updating graphs action
			class UpdateGraphsStep extends BasicAction{
				public void execute(){
					curNumGrass.step();
					curNumRabbits.step();
				}
			}
			schedule.scheduleActionAtInterval(10, new UpdateGraphsStep());
		}

		
		public void populationStep(){
			for (int i=0;i<agentList.size();i++){
				RabbitsGrassSimulationAgent a = (RabbitsGrassSimulationAgent)agentList.get(i);
				int e = a.getEnergy();
				if (e<=0){
					space.removeAgentAt(a.getX(), a.getY());
					agentList.remove(i);
				}
				if (e>birthThreshold){
					addNewAgent();
					a.setEnergy(- a.getBirthEnergy());
				}
			}
		}
		
		
		public void buildDisplay(){
			ColorMap map = new ColorMap();
			for(int i = 1; i < 16; ++i) {
				map.mapColor(i, new Color(0, ((int)(127 + 8*i)), 0));
			}
			map.mapColor(0, Color.WHITE);
			Value2DDisplay displayGrass = new Value2DDisplay(space.getCurrentGrassSpace(), map);
			Object2DDisplay displayAgents = new Object2DDisplay(space.getCurrentAgentSpace());
			displayAgents.setObjectList(agentList);
			surface.addDisplayable(displayGrass, "Grass");
			surface.addDisplayable(displayAgents, "Rabbits");
			curNumRabbits.addSequence("Rabbits in Space", new RabbitsInSpace());
			curNumGrass.addSequence("Grass in Space", new GrassInSpace());
		}
		
		private void addNewAgent(){
			RabbitsGrassSimulationAgent agent = new RabbitsGrassSimulationAgent();
			agentList.add(agent);
			space.addAgent(agent);
			agent.setSpace(space);
		}
		
		/***** GETTERS & SETTERS ******/
		public String getName() {
			return "My first Repast Model";
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
		
		public int getX() {
			return x;
		}
		
		public void setX(int newX) {
			x = newX;
		}
		
		public int getY() {
			return y;
		}
		
		public void setY(int newY) {
			y = newY;
		}
		
		public int getBirthThreshold() {
			return birthThreshold;
		}
		
		public void setBirthThreshold(int newThreshold) {
			birthThreshold = newThreshold;
		}
		
		public int getGrassGrowth() {
			return grassGrowth;
		}
		
		public void setGrassGrowth(int newGrowth) {
			grassGrowth = newGrowth;
		}

		public String[] getInitParam() {
			String[] initParam = {"numAgents", "x", "y", "birthThreshold", "grassGrowth"};
			return initParam;
		}	
}
