package br.ufrj.coc.cec2015.algorithm.pso;

import java.util.ArrayList;
import java.util.List;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class RIOHelper {
	public static double[][] distanceMatrix = new double[Properties.POPULATION_SIZE][Properties.POPULATION_SIZE]; /* M */
	private static List<Double> distances = new ArrayList<>();
	public static double distanceMedian; /* d_g */

	private static double distance(Individual roachI, Individual roachK) {
		double distance = 0.0;
		for (int index = 0; index < roachI.size(); index++) {
			distance += Math.pow((roachI.get(index) - roachK.get(index)), 2);
		}
		return Math.sqrt(distance);
	}
	
	protected static void prepareCockroaches(Population cockroaches) {
		int N = Properties.POPULATION_SIZE;
		distances.clear();
		
		for (int indexJ = 0; indexJ < N - 1; indexJ++) {
			for (int indexK = indexJ + 1; indexK < N; indexK++) {
				Individual roachI = cockroaches.get(indexJ);
				Individual roachK = cockroaches.get(indexK);
				
				double distanceValue = distance(roachI, roachK);

				distanceMatrix[indexJ][indexK] = distanceValue;
				distances.add(distanceValue);
			}
		}
		distanceMedian = Statistic.calculateMedian(distances);
	}
	
	private static double getDistance(int indexI, int indexK) {
		return (indexI < indexK) ? distanceMatrix[indexI][indexK] : distanceMatrix[indexK][indexI];
	}
	
	public static void socializing(Population cockroaches, int indexI) {
		// If a cockroach agent comes within a detection radius of another cockroach agent, then there is
		// a probability of that these roaches will socialize (or group)
		List<Individual> neighbors = new ArrayList<>(); /* {j} */
		for (int indexK = 0; indexK < cockroaches.size(); indexK++) {
			if (indexI != indexK) {
				double distance = getDistance(indexI, indexK);
				if (distance < distanceMedian) {
					Individual neighbor = cockroaches.get(indexK);
					neighbors.add(neighbor);
				}
			}
		}
		
		// This socializing is emulated in the algorithm by a sharing of information, where this information is
		// the darkest known location. In essence, when two cockroach agents meet, there is a chance (stopRateForFriends) that 
		// they will communicate their knowledge of the search space to each other
		Individual agent = cockroaches.get(indexI);
		int stopRateForFriendsCount = PSOProperties.STOP_RATE_FOR_FRIENDS.length;
		for (int indexK = 0; indexK < neighbors.size() /* N_i */; indexK++) {
			double stopRateForFriends = (indexK < stopRateForFriendsCount) ? PSOProperties.STOP_RATE_FOR_FRIENDS[indexK] : PSOProperties.STOP_RATE_FOR_FRIENDS[stopRateForFriendsCount - 1];
			if (Helper.randomInRange(0.0, 1.0) < stopRateForFriends) {
				
				Individual neighbor = neighbors.get(indexK);
				if (Double.compare(agent.getFunctionValueBestKnow(), neighbor.getFunctionValueBestKnow()) < 0) {
					neighbor.setBestKnown(agent.getBestKnown());
					neighbor.setFunctionValueBestKnow(agent.getFunctionValueBestKnow());
				}
				else {
					agent.setBestKnown(neighbor.getBestKnown());
					agent.setFunctionValueBestKnow(neighbor.getFunctionValueBestKnow());
				}
			}
		}
	}
	
	public static double[] randomFoodLocation() {
		return new Individual(Properties.INDIVIDUAL_SIZE).getId();
	}

	public static void incrementHungerCounters(Population swarm) {
		for (Individual roach : swarm.getIndividuals()) {
			roach.incrementHungerCount();
		}
	}
}
