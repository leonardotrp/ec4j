package br.ufrj.coc.cec2015.algorithm.dpade;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.algorithm.de.DEHelper;

public class DPADEHelper extends DEHelper {
	private double propability_F = 0.5;
	private double propability_CR = 0.5;
	
	@Override
	public void initializeGeneration(Population population) {
		super.initializeGeneration(population);
	}
}
