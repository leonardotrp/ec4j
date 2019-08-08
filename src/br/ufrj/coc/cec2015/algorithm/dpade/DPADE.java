package br.ufrj.coc.cec2015.algorithm.dpade;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.jade.JADE;

public class DPADE extends JADE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new DPADEHelper();
	}
}
