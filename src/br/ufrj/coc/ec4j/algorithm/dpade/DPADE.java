package br.ufrj.coc.ec4j.algorithm.dpade;

import br.ufrj.coc.ec4j.algorithm.AlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.jade.JADE;
import br.ufrj.coc.ec4j.util.Properties;

public class DPADE extends JADE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new DPADEHelper();
	}
	@Override
	public DPADEHelper getDEHelper() {
		return (DPADEHelper) Properties.HELPER.get();
	}
}
