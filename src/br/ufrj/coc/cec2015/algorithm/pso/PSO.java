package br.ufrj.coc.cec2015.algorithm.pso;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Initializable;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;
import br.ufrj.coc.cec2015.util.Statistic;

public class PSO extends Algorithm {

	@Override
	public String[] getVariants() {
		return PSOProperties.VARIANTS;
	}

	@Override
	public String getInfo() {
		return null;
	}
	
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new PSOHelper();
	}
	
	private PSOHelper getPSOHelper() {
		return (PSOHelper) Properties.HELPER.get();
	}
	
	@Override
	public Initializable getIntializable() {
		return new Initializable() {
			@Override
			public Individual newInitialized() {
				Individual particle = Helper.newIndividualInitialized();
				PSOHelper.initializeVelocity(particle);
				particle.setInertiaWeight(PSOProperties.W_STATIC);
				particle.setHungerCount(Helper.randomInRange(0, PSOProperties.HUNGER_INTERVAL - 1));
				return particle;
			}

			@Override
			public Individual newInitialized(double[] id) {
				return null;
			}
		};
	}	
	
	@Override
	public void run(Population swarm, Statistic statistic, int round) throws Exception {
		
		getPSOHelper().initializeGeneration(swarm);
		
		for (int index = 0; index < swarm.size(); index++) {
			Individual particle = swarm.get(index);
			
			getPSOHelper().moveParticle(index);

			double functionValue = Properties.ARGUMENTS.get().evaluateFunction(particle.getId());
			updateBest(swarm, particle, functionValue);
			statistic.verifyEvaluationInstant(round, swarm);
			/*
			if (PSOHelper.isOBLPSO()) {
				PSOHelper.oblMutate(swarm, index);

				functionValue = Helper.evaluate(particle.getId());
				updateBest(swarm, particle, functionValue);
				statistic.verifyEvaluationInstant(round, swarm);
			}
			*/
		}
		getPSOHelper().afterRun(swarm);
	}

	private void updateBest(Population swarm, Individual particle, double functionValue) throws CloneNotSupportedException {
		if (functionValue < particle.getFunctionValue()) {
			particle.setBestKnown(particle.getId().clone());
			particle.setFunctionValue(functionValue);
			swarm.updateBestError(particle);
		}
	}
}
