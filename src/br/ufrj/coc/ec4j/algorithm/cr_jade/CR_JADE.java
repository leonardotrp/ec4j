package br.ufrj.coc.ec4j.algorithm.cr_jade;

import br.ufrj.coc.ec4j.algorithm.AlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.de.DE;
import br.ufrj.coc.ec4j.util.Properties;

public class CR_JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new CR_JADEHelper();
	}
	public CR_JADEHelper getDEHelper() {
		return (CR_JADEHelper) Properties.HELPER.get();
	}
}
