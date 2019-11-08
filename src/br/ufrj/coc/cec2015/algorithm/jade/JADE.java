package br.ufrj.coc.cec2015.algorithm.jade;

import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;
import br.ufrj.coc.cec2015.algorithm.de.DE;
import br.ufrj.coc.cec2015.util.Properties;

public class JADE extends DE {
	@Override
	protected AlgorithmHelper newInstanceHelper() {
		return new JADEHelper();
	}
	public JADEHelper getDEHelper() {
		return (JADEHelper) Properties.HELPER.get();
	}
}
