package br.ufrj.coc.ec4j.algorithm.ipop_jade;

import br.ufrj.coc.ec4j.algorithm.AlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.de.DE;
import br.ufrj.coc.ec4j.util.Properties;

public class IPOP_JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new IPOP_JADEHelper();
	}
	public IPOP_JADEHelper getDEHelper() {
		return (IPOP_JADEHelper) Properties.HELPER.get();
	}
}
