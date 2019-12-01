package br.ufrj.coc.ec4j.algorithm.cr_jade;

import br.ufrj.coc.ec4j.algorithm.Population;
import br.ufrj.coc.ec4j.algorithm.jade.JADEHelper;
import br.ufrj.coc.ec4j.util.Helper;
import br.ufrj.coc.ec4j.util.Properties;

public class CR_JADEHelper extends JADEHelper {

	public void initializeGeneration(Population population) {
		super.initializeGeneration(population);
		
		double funcValDifference = Helper.getFunctionValueDifference(population);
		double maxDistance = Helper.getMaxDistance(population);
		if (funcValDifference < Properties.MIN_ERROR_VALUE && maxDistance < 1) {
			super.ipop(population);
			System.err.println(String.format("FUNCVAL_DIFF=%e / MAXDIST=%e", funcValDifference, maxDistance));
			population.incCountRestart();
			this.initializeGeneration(population);
		}
	}
}