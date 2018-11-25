package br.ufrj.coc.cec2015.algorithm.pso;

import java.util.Collections;

import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class PSOHelper {
	public enum PSOVariant {
		PSO, 
		ConstByEvalNumPSO, 
		ConstByClercPSO /* Maurice Clerc 1999 */, 
		InertiaFactorPSO /* Shi and Eberhart 1998 */,
		ConstByEvalNumPSO_InertiaFactorPSO,
		ConstByClercPSO_InertiaFactorPSO,
		SRPSO /* M.R. Tanweer 2015 */,
		iSRPSO /* Improved SRPSO for CEC2015 - M.R. Tanweer 2015 */,
		OBLPSO
	}
	private static PSOVariant VARIANT;
	static {
		VARIANT = PSOVariant.valueOf(PSOProperties.VARIANT);
	}
	
	public static boolean isOBLPSO() {
		return PSOVariant.OBLPSO.equals(VARIANT);
	}
	
	public static double getVelocityRangeValue() {
		double rangeValue = Math.abs(Properties.SEARCH_RANGE[1] - Properties.SEARCH_RANGE[0]);
		return rangeValue;
	}
	
	public static double protectVelocityLimits(double velocity) {
		double rangeValue = getVelocityRangeValue();
		if (velocity < -rangeValue)
			velocity = -rangeValue;
		if (velocity > rangeValue)
			velocity = rangeValue;
		return velocity;
	}

	private static double getIntervalInertiaWeight() {
		return (PSOProperties.W_START - PSOProperties.W_END) / Helper.COUNT_EVALUATIONS;
	}

	private static void calculateInertiaWeight(Individual particle, boolean isBest) {
		double inertiaWeight = particle.getInertiaWeight();
		if (PSOVariant.SRPSO.equals(VARIANT) || PSOVariant.iSRPSO.equals(VARIANT)) {
			double intervalIW = getIntervalInertiaWeight();
			inertiaWeight = isBest ? (inertiaWeight + intervalIW) : (inertiaWeight - intervalIW);
		}
		else if (VARIANT.name().contains("InertiaFactor")) {
			double inertia_factor = 1 - (Helper.COUNT_EVALUATIONS / Properties.MAX_FES);
			inertiaWeight = PSOProperties.W_END + (inertia_factor * (PSOProperties.W_START - PSOProperties.W_END)); /* 0.9 --> 0.4 */
		}
		particle.setInertiaWeight(inertiaWeight);
	}

	private static double getConstrictionCoefficient() {
		double constriction = 1;
		if (VARIANT.name().contains("ConstByEvalNum")) {
			constriction =  1 - (Helper.COUNT_EVALUATIONS / Properties.MAX_FES);
			constriction = PSOProperties.W_END + constriction * (PSOProperties.W_START - PSOProperties.W_END);
		}
		else if (VARIANT.name().contains("ConstByClerc")) {
			double k = 1;
			double sigma = PSOProperties.FI_1 + PSOProperties.FI_2;
			if (sigma > 4) {
				double numerator = 2 * k;
				double denominator = sigma - 2 + Math.sqrt((Math.pow(sigma, 2) - 4 * sigma));
				constriction = numerator / denominator;
			}
			else {
				constriction = Math.sqrt(k);
			}
		}
		return constriction;
	}
	
	private static Population sortedSwarm;
	private static boolean isLastTwoWorstParticle(Individual particle) {
		int worstIndex = Properties.INDIVIDUAL_SIZE - 1;
		return sortedSwarm.getIndividuals().get(worstIndex - 1).equals(particle) || sortedSwarm.getIndividuals().get(worstIndex).equals(particle);
	}
	
	private static double[] centroidPosition;
	private static void calculateCentroidPosition(Population swarm, Individual particle) throws Exception {
		if (PSOVariant.iSRPSO.equals(VARIANT)) {
			sortedSwarm = (Population) swarm.clone();
			Collections.sort(sortedSwarm.getIndividuals());
			
			Individual best2nd = sortedSwarm.getIndividuals().get(1);
			Individual best3nd = sortedSwarm.getIndividuals().get(2);
			Individual best4nd = sortedSwarm.getIndividuals().get(3);
			
			centroidPosition = new double[Properties.INDIVIDUAL_SIZE]; // centroid position
			for (int d = 0; d < Properties.INDIVIDUAL_SIZE; d++) {
				double centroidValue = (best2nd.get(d) + best3nd.get(d) + best4nd.get(d)) / 3;
				centroidPosition[d] = centroidValue;
			}
		}
	}
	
	private static double getSelfPositionValue(Individual particle, int d) {
		return PSOVariant.iSRPSO.equals(VARIANT) && isLastTwoWorstParticle(particle) ? centroidPosition[d] : particle.getBestKnown(d);
	}

	private static double calculateVelocity(Population swarm, Individual particle, int d) {
		double selfCognitionValue = PSOProperties.FI_1 * Helper.randomInRange(0.0, 1.0);
		double socialCognitionValue = PSOProperties.FI_2 * Helper.randomInRange(0.0, 1.0);
		
		if (PSOVariant.SRPSO.equals(VARIANT) || PSOVariant.iSRPSO.equals(VARIANT)) {

			boolean useBinaryDecision = !PSOVariant.iSRPSO.equals(VARIANT) || !isLastTwoWorstParticle(particle);

			double perceptionSelfCognition = (!useBinaryDecision || PSOVariant.iSRPSO.equals(VARIANT)) ? 1 : swarm.getBest().equals(particle) ? 0 : 1;
			selfCognitionValue *= perceptionSelfCognition;

			double gama = Helper.randomInRange(0.0, 1.0) > PSOProperties.THRESHOULD_VALUE ? 1 : 0;
			double perceptionSocialCognition = !useBinaryDecision ? 1 : swarm.getBest().equals(particle) ? 0 : gama;
			socialCognitionValue *= perceptionSocialCognition;
		}
		
		double velocity = particle.getInertiaWeight() * particle.getVelocity(d);
		velocity += selfCognitionValue * (getSelfPositionValue(particle, d) - particle.get(d));
		velocity += socialCognitionValue * (swarm.getBest().get(d) - particle.get(d));
		
		return velocity;
	}
	
	public static void moveParticle(Population swarm, int index) throws Exception {
		Individual particle = swarm.get(index);
		calculateInertiaWeight(particle, swarm.getBest().equals(particle));
		double constriction = getConstrictionCoefficient();
		calculateCentroidPosition(swarm, particle); // if iSRPSO
		
		for (int d = 0; d < Properties.INDIVIDUAL_SIZE; d++) {

			double velocity = calculateVelocity(swarm, particle, d);

			velocity = constriction * velocity;
			
			velocity = protectVelocityLimits(velocity);
			
			particle.setVelocity(d, velocity);
			particle.set(d, particle.get(d) + velocity);
		}
	}
	
	public static void oblMutate(Population swarm, int index) {
		Individual particle = swarm.get(index);
		Individual best = swarm.getBest();
		double rangeValue = PSOHelper.getVelocityRangeValue();

		for (int d = 0; d < Properties.INDIVIDUAL_SIZE; d++) {
			particle.setVelocity(d, Helper.randomInRange(-rangeValue, rangeValue));
			
			double oppositeValue = Properties.SEARCH_RANGE[0] - Properties.SEARCH_RANGE[1] - best.get(d);
			particle.set(d, oppositeValue);
		}
	}
}
