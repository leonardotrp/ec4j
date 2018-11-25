package br.ufrj.coc.cec2015.algorithm.abc;

import java.util.Comparator;

import br.ufrj.coc.cec2015.algorithm.Individual;

public class ComparatorByProbability implements Comparator<Individual> {
	@Override
	public int compare(Individual foodSource1, Individual foodSource2) {
		if (foodSource1.getProbability() > foodSource2.getProbability())
			return -1;
		else if (foodSource1.getProbability() < foodSource2.getProbability())
			return 1;
		else
			return 0;
	}

}
