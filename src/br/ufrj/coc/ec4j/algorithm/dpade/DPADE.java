package br.ufrj.coc.ec4j.algorithm.dpade;

import br.ufrj.coc.ec4j.algorithm.AlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.jade.JADE;

public class DPADE extends JADE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new DPADEHelper();
	}
}
