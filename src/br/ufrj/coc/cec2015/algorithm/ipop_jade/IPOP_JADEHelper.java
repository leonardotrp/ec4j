package br.ufrj.coc.cec2015.algorithm.ipop_jade;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.jade.JADEHelper;
import br.ufrj.coc.cec2015.util.Helper;

public class IPOP_JADEHelper extends JADEHelper {
	public IPOP_JADEHelper() {
		super();
		this.initialize();
	}

	protected void initialize() {
		super.initialize();
	}

	public void initializeGeneration(Population population) {
		super.initializeGeneration(population);
		
		double funcValDifference = Helper.getFunctionValueDifference(population);
		double maxDistance = Helper.getMaxDistance(population);
	}
}