package br.ufrj.coc.cec2015.algorithm.cr_jade;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

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