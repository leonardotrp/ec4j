package br.ufrj.coc.cec2015.algorithm.jade;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.de.DE;

public class JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new JADEHelper();
	}
}
