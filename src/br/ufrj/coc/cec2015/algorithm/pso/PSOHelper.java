package br.ufrj.coc.cec2015.algorithm.pso;

import java.util.Collections;

import br.ufrj.coc.cec2015.algorithm.BaseAlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class PSOHelper extends BaseAlgorithmHelper {
	private Population swarm;
	private RIOHelper rioHelper;
	
	@Override
	public void initializeGeneration(Population swarm) {
		super.initializeGeneration(swarm);
		this.swarm = swarm;
		if (PSOProperties.isRIO())
			rioHelper = new RIOHelper(this.swarm);
	}
	
	public static void initializeVelocity(Individual particle) {
		double[] velocity = new double[Properties.ARGUMENTS.get().getIndividualSize()];
		double rangeValue = PSOHelper.getVelocityRangeValue();
		for (int d = 0; d < velocity.length; d++)
			velocity[d] = Helper.randomInRange(-rangeValue, rangeValue);
		particle.setVelocity(velocity);		
	}
	
	public static double getVelocityRangeValue() {
		double[] range = Properties.getSearchRange();
		double rangeValue = Math.abs(range[1] - range[0]);
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
		return (PSOProperties.W_START - PSOProperties.W_END) / Properties.ARGUMENTS.get().getCountEvaluations();
	}

	private static void calculateInertiaWeight(Individual particle, boolean isBest) {
		double inertiaWeight = particle.getInertiaWeight();
		if (PSOProperties.isSRPSO()) {
			double intervalIW = getIntervalInertiaWeight();
			inertiaWeight = isBest ? (inertiaWeight + intervalIW) : (inertiaWeight - intervalIW);
		}
		else if (PSOProperties.isInertiaFactoryPSO()) {
			double inertia_factor = Properties.ARGUMENTS.get().getEvolutionFactor();
			inertiaWeight = PSOProperties.W_END + (inertia_factor * (PSOProperties.W_START - PSOProperties.W_END)); /* 0.9 --> 0.4 */
		}
		particle.setInertiaWeight(inertiaWeight);
	}

	private static double getConstrictionCoefficient() {
		double constriction = 1;
		if (PSOProperties.isConstByEvalNum()) {
			constriction =  Properties.ARGUMENTS.get().getEvolutionFactor();
			constriction = PSOProperties.W_END + constriction * (PSOProperties.W_START - PSOProperties.W_END);
		}
		else if (PSOProperties.isConstByClerc()) {
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
	
	private Population sortedSwarm;
	private boolean isLastTwoWorstParticle(Individual particle) {
		int worstIndex = Properties.ARGUMENTS.get().getIndividualSize() - 1;
		return this.sortedSwarm.getIndividuals().get(worstIndex - 1).equals(particle) || this.sortedSwarm.getIndividuals().get(worstIndex).equals(particle);
	}
	
	private double[] centroidPosition;
	private void calculateCentroidPosition(Individual particle) throws Exception {
		if (PSOProperties.isImproveSRPSO()) {
			this.sortedSwarm = (Population) swarm.clone();
			Collections.sort(this.sortedSwarm.getIndividuals());
			
			Individual best2nd = this.sortedSwarm.getIndividuals().get(1);
			Individual best3nd = this.sortedSwarm.getIndividuals().get(2);
			Individual best4nd = this.sortedSwarm.getIndividuals().get(3);
			
			this.centroidPosition = new double[Properties.ARGUMENTS.get().getIndividualSize()]; // centroid position
			for (int d = 0; d < Properties.ARGUMENTS.get().getIndividualSize(); d++) {
				double centroidValue = (best2nd.get(d) + best3nd.get(d) + best4nd.get(d)) / 3;
				this.centroidPosition[d] = centroidValue;
			}
		}
	}
	
	private double getSelfPositionValue(Individual particle, int d) {
		return PSOProperties.isImproveSRPSO() && isLastTwoWorstParticle(particle) ? this.centroidPosition[d] : particle.getBestKnown(d);
	}
	
	private double getSocialPositionValue(Individual particle, int d) {
		return PSOProperties.isRIO() ? particle.getBestKnown(d) : this.swarm.getBest().get(d);
	}

	private double calculateVelocity(Individual particle, int d) {
		double selfCognitionValue = PSOProperties.FI_SELF * Helper.randomInRange(0.0, 1.0);
		double socialCognitionValue = PSOProperties.FI_SOCIAL * Helper.randomInRange(0.0, 1.0);
		
		if (PSOProperties.isSRPSO()) {

			boolean useBinaryDecision = !PSOProperties.isImproveSRPSO() || !isLastTwoWorstParticle(particle);

			double perceptionSelfCognition = (!useBinaryDecision || PSOProperties.isImproveSRPSO()) ? 1 : this.swarm.getBest().equals(particle) ? 0 : 1;
			selfCognitionValue *= perceptionSelfCognition;

			double gama = Helper.randomInRange(0.0, 1.0) > PSOProperties.THRESHOULD_VALUE ? 1 : 0;
			double perceptionSocialCognition = !useBinaryDecision ? 1 : swarm.getBest().equals(particle) ? 0 : gama;
			socialCognitionValue *= perceptionSocialCognition;
		}
		
		double velocity = particle.getInertiaWeight() * particle.getVelocity(d);
		velocity += selfCognitionValue * (getSelfPositionValue(particle, d) - particle.get(d)); // em RIO esta parte é conhecida por "find darkness"
		velocity += socialCognitionValue * (getSocialPositionValue(particle, d) - particle.get(d)); // em RIO esta parte é conhecida por "find friends"
		
		return velocity;
	}
	
	public void afterRun(Population swarm) {
		if (PSOProperties.isRIO() && PSOProperties.HUNGRY)
			rioHelper.incrementHungerCounters();
	}
	
	private boolean canMoveParticle(Individual particle, int index) {
		boolean canMove = true;
		if (PSOProperties.isRIO()) {
			rioHelper.socializing(index); // if RIO
			canMove = (particle.getHungerCount() < PSOProperties.HUNGER_INTERVAL);
			if (!canMove) {
				particle.setId(RIOHelper.randomFoodLocation());
				particle.setHungerCount(0);
			}
		}
		return canMove;
	}
	
	public void moveParticle(int index) throws Exception {
		Individual particle = this.swarm.get(index);
		calculateInertiaWeight(particle, this.swarm.getBest().equals(particle));
		double constriction = getConstrictionCoefficient();
		calculateCentroidPosition(particle); // if iSRPSO
		
		boolean canMove = canMoveParticle(particle, index);

		for (int d = 0; canMove && d < Properties.ARGUMENTS.get().getIndividualSize(); d++) {
			double velocity = calculateVelocity(particle, d);
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
