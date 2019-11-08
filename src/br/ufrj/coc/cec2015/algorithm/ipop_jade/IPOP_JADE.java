package br.ufrj.coc.cec2015.algorithm.ipop_jade;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.de.DE;
import br.ufrj.coc.cec2015.util.Properties;

public class IPOP_JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new IPOP_JADEHelper();
	}
	public IPOP_JADEHelper getDEHelper() {
		return (IPOP_JADEHelper) Properties.HELPER.get();
	}
}
