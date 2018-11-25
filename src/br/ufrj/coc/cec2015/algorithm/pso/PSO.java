package br.ufrj.coc.cec2015.algorithm.pso;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Statistic;

public class PSO extends Algorithm {

	@Override
	public String[] getVariants() {
		return PSOProperties.VARIANTS;
	}
	
	@Override
	public void setCurrentVariant(String variant) {
		PSOProperties.setVariant(variant);
	}
	
	@Override
	public String getVariant() {
		return PSOProperties.VARIANT;
	}
	
	@Override
	public void run(Population swarm, Statistic statistic, int round) throws Exception {
		for (int index = 0; index < swarm.size(); index++) {
			Individual particle = swarm.get(index);
			
			PSOHelper.moveParticle(swarm, index);

			double functionValue = Helper.evaluate(particle.getId());
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
	}

	private void updateBest(Population swarm, Individual particle, double functionValue) throws CloneNotSupportedException {
		if (functionValue < particle.getFunctionValue()) {
			particle.setBestKnown(particle.getId().clone());
			particle.setFunctionValue(functionValue);
			swarm.updateBestError(particle);
		}
	}
}
