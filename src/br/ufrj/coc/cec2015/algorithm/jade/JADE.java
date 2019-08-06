package br.ufrj.coc.cec2015.algorithm.jade;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.de.DE;
import br.ufrj.coc.cec2015.algorithm.de.DEHelper;

public class JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new JADEHelper();
	}
	@Override
	public String[] getVariants() {
		return new String[] {"DE/current-to-pbest/1/Eig"};
	}
	public DEHelper getDEHelper() {
		return (JADEHelper) super.getDEHelper();
	}
}
