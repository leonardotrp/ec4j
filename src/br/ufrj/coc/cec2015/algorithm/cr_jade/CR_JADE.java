package br.ufrj.coc.cec2015.algorithm.cr_jade;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.de.DE;
import br.ufrj.coc.cec2015.util.Properties;

public class CR_JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new CR_JADEHelper();
	}
	public CR_JADEHelper getDEHelper() {
		return (CR_JADEHelper) Properties.HELPER.get();
	}
}
