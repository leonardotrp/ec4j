package br.ufrj.coc.cec2015;

import br.ufrj.coc.cec2015.algorithm.Algorithm;
import br.ufrj.coc.cec2015.util.Properties;

public class Main {
	public static void main(String[] args) throws Exception {
		for (String algorithmName : Properties.ALGORITHMS) { // loop algorithms
			Properties.setCurrentAlgorithm(algorithmName);
			for (String individualSize : Properties.INDIVIDUAL_SIZES) { // loop dimensions
				Properties.setCurrentIndividualSize(Integer.parseInt(individualSize));
				String className = Algorithm.class.getPackage().getName() + '.' + algorithmName.toLowerCase() + '.' + algorithmName;
				Algorithm algorithm = (Algorithm) Class.forName(className).newInstance();
				for (String variant : algorithm.getVariants()) {  // loop variants
					algorithm.setCurrentVariant(variant);
					algorithm.main();
				}
			}
		}
	}
}
