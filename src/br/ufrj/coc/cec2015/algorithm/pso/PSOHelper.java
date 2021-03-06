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
		/*OBLPSO,*/
		RIO /* Roach Infestation Optimization */,
		RIO_ConstByClercPSO
	}
	private static PSOVariant VARIANT;
	static {
		VARIANT = PSOVariant.valueOf(PSOProperties.VARIANT);
	}
	/*
	public static boolean isOBLPSO() {
		return PSOVariant.OBLPSO.equals(VARIANT);
	}
	*/
	public static boolean isRIO() {
		return PSOVariant.RIO.equals(VARIANT) || PSOVariant.RIO_ConstByClercPSO.equals(VARIANT);
	}

	public static boolean isSRPSO() {
		return PSOVariant.SRPSO.equals(VARIANT) || PSOVariant.iSRPSO.equals(VARIANT);
	}
	
	public static boolean isImproveSRPSO() {
		return PSOVariant.iSRPSO.equals(VARIANT);
	}
	
	private static boolean isConstByEvalNum() {
		return VARIANT.name().contains("ConstByEvalNum");
	}
	
	private static boolean isConstByClerc() {
		return VARIANT.name().contains("ConstByClerc");
	}
	
	private static boolean isInertiaFactoryPSO() {
		return VARIANT.name().contains("InertiaFactor");
	}
	
	public static void initializeVelocity(Individual particle) {
		double[] velocity = new double[Properties.INDIVIDUAL_SIZE];
		double rangeValue = PSOHelper.getVelocityRangeValue();
		for (int d = 0; d < velocity.length; d++)
			velocity[d] = Helper.randomInRange(-rangeValue, rangeValue);
		particle.setVelocity(velocity);		
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
		if (isSRPSO()) {
			double intervalIW = getIntervalInertiaWeight();
			inertiaWeight = isBest ? (inertiaWeight + intervalIW) : (inertiaWeight - intervalIW);
		}
		else if (isInertiaFactoryPSO()) {
			double inertia_factor = 1 - (Helper.COUNT_EVALUATIONS / Properties.MAX_FES);
			inertiaWeight = PSOProperties.W_END + (inertia_factor * (PSOProperties.W_START - PSOProperties.W_END)); /* 0.9 --> 0.4 */
		}
		particle.setInertiaWeight(inertiaWeight);
	}

	private static double getConstrictionCoefficient() {
		double constriction = 1;
		if (isConstByEvalNum()) {
			constriction =  1 - (Helper.COUNT_EVALUATIONS / Properties.MAX_FES);
			constriction = PSOProperties.W_END + constriction * (PSOProperties.W_START - PSOProperties.W_END);
		}
		else if (isConstByClerc()) {
			double k = 1;
			double sigma = PSOProperties.FI_SELF + PSOProperties.FI_SOCIAL;
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
		if (isImproveSRPSO()) {
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
		return isImproveSRPSO() && isLastTwoWorstParticle(particle) ? centroidPosition[d] : particle.getBestKnown(d);
	}
	
	private static double getSocialPositionValue(Population swarm, Individual particle, int d) {
		return isRIO() ? particle.getBestKnown(d) : swarm.getBest().get(d);
	}

	private static double calculateVelocity(Population swarm, Individual particle, int d) {
		double selfCognitionValue = PSOProperties.FI_SELF * Helper.randomInRange(0.0, 1.0);
		double socialCognitionValue = PSOProperties.FI_SOCIAL * Helper.randomInRange(0.0, 1.0);
		
		if (isSRPSO()) {

			boolean useBinaryDecision = !isImproveSRPSO() || !isLastTwoWorstParticle(particle);

			double perceptionSelfCognition = (!useBinaryDecision || isImproveSRPSO()) ? 1 : swarm.getBest().equals(particle) ? 0 : 1;
			selfCognitionValue *= perceptionSelfCognition;

			double gama = Helper.randomInRange(0.0, 1.0) > PSOProperties.THRESHOULD_VALUE ? 1 : 0;
			double perceptionSocialCognition = !useBinaryDecision ? 1 : swarm.getBest().equals(particle) ? 0 : gama;
			socialCognitionValue *= perceptionSocialCognition;
		}
		
		double velocity = particle.getInertiaWeight() * particle.getVelocity(d);
		velocity += selfCognitionValue * (getSelfPositionValue(particle, d) - particle.get(d)); // em RIO esta parte é conhecida por "find darkness"
		velocity += socialCognitionValue * (getSocialPositionValue(swarm, particle, d) - particle.get(d)); // em RIO esta parte é conhecida por "find friends"
		
		return velocity;
	}
	
	public static void beforeRun(Population swarm) {
		if (isRIO())
			RIOHelper.prepareCockroaches(swarm);
	}

	public static void afterRun(Population swarm) {
		if (isRIO() && PSOProperties.HUNGRY)
			RIOHelper.incrementHungerCounters(swarm);
	}
	
	private static boolean canMoveParticle(Population swarm, Individual particle, int index) {
		boolean canMove = true;
		if (isRIO()) {
			RIOHelper.socializing(swarm, index); // if RIO
			canMove = (particle.getHungerCount() < PSOProperties.HUNGER_INTERVAL);
			if (!canMove) {
				particle.setId(RIOHelper.randomFoodLocation());
				particle.setHungerCount(0);
			}
		}
		return canMove;
	}
	
	public static void moveParticle(Population swarm, int index) throws Exception {
		Individual particle = swarm.get(index);
		calculateInertiaWeight(particle, swarm.getBest().equals(particle));
		double constriction = getConstrictionCoefficient();
		calculateCentroidPosition(swarm, particle); // if iSRPSO
		
		boolean canMove = canMoveParticle(swarm, particle, index);

		for (int d = 0; canMove && d < Properties.INDIVIDUAL_SIZE; d++) {
			double velocity = calculateVelocity(swarm, particle, d);
			velocity = constriction * velocity;
			velocity = protectVelocityLimits(velocity);
			
			particle.setVelocity(d, velocity);
			particle.set(d, particle.get(d) + velocity);
		}
	}
	/*
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
	*/
}
