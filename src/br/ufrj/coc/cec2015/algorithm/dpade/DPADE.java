package br.ufrj.coc.cec2015.algorithm.dpade;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.de.DE;

public class DPADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new DPADEHelper();
	}
}
