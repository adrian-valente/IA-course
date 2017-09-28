import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		

		private Schedule schedule;
		private int numAgents;
	
		public static void main(String[] args) {
			
			System.out.println("Rabbit skeleton");
			
		}
		
		public String getName() {
			return "My first Repast Model";
		}
		
		public void setup() {
			// TODO Auto-generated method stub
			
		}

		public void begin() {
			buildModel();
			buildSchedule();
			buildDisplay();	
		}
		
		public void buildModel(){
		}

		public void buildSchedule(){
		}

		public void buildDisplay(){
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
			String[] initParam = {"numAgents"};
			return initParam;
		}	
}
