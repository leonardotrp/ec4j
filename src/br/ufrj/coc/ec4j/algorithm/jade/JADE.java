package br.ufrj.coc.ec4j.algorithm.jade;

import br.ufrj.coc.ec4j.algorithm.AlgorithmHelper;
import br.ufrj.coc.ec4j.algorithm.de.DE;
import br.ufrj.coc.ec4j.util.Properties;

public class JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new JADEHelper();
	}
	public JADEHelper getDEHelper() {
		return (JADEHelper) Properties.HELPER.get();
	}
}
