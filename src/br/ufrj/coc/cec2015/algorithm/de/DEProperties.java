package br.ufrj.coc.cec2015.algorithm.de;

import java.util.ResourceBundle;

/**
 * @author Leonardo
 */
public class DEProperties {
	static ResourceBundle bundle = ResourceBundle.getBundle(DEProperties.class.getPackage().getName() + ".de");
	
	public static double CROSSOVER_RATE = Double.parseDouble(bundle.getString("CROSSOVER_RATE"));
	public static double DIFFERENTIAL_WEIGHT = Double.parseDouble(bundle.getString("DIFFERENTIAL_WEIGHT"));
	public static String[] VARIANTS = bundle.getString("VARIANTS").split(",");
	public static String VARIANT;
	public static void setVariant(String variant) {
		VARIANT = variant;
		loadVariant();
	}
	public static boolean EXTERNAL_ARCHIVE = Boolean.parseBoolean(bundle.getString("EXTERNAL_ARCHIVE"));
	public static double ADAPTATION_RATE = Double.parseDouble(bundle.getString("ADAPTATION_RATE"));
	public static double GREEDINESS = Double.parseDouble(bundle.getString("GREEDINESS"));

	public enum Strategy {
		BEST, RAND, RE_BASE, RE_ALL, // BEST_2_OPT, RAND_2_OPT, RE_2_OPT
		CURRENT_TO_BEST,
		CURRENT_TO_RAND,
		CURRENT_TO_pBEST /* JADE */
	}
	public enum Crossover {
		BINARY, EIGENVECTOR
	}
	public static Strategy STRATEGY;
	public static Crossover CROSSOVER;
	public static int MUTATION_DIFFERENCES_COUNT;

	private static void loadVariant() {
		boolean valid = true;
		String[] variant = DEProperties.VARIANT.split("/");
		if (variant[1].equals("best"))
			STRATEGY = Strategy.BEST;
		/*else if (variant[1].equals("best-2-opt"))
			STRATEGY = Strategy.BEST_2_OPT;*/
		else if (variant[1].equals("rand"))
			STRATEGY = Strategy.RAND;
		else if (variant[1].equals("current-to-best"))
			STRATEGY = Strategy.CURRENT_TO_BEST;
		else if (variant[1].equals("current-to-rand"))
			STRATEGY = Strategy.CURRENT_TO_RAND;
		else if (variant[1].equals("current-to-pbest"))
			STRATEGY = Strategy.CURRENT_TO_pBEST;
		/*else if (variant[1].equals("rand-2-opt"))
			STRATEGY = Strategy.RAND_2_OPT;*/
		else if (variant[1].equals("re-base"))
			STRATEGY = Strategy.RE_BASE;
		else if (variant[1].equals("re-all"))
			STRATEGY = Strategy.RE_ALL;
		/*else if (variant[1].equals("re-2-opt"))
			STRATEGY = Strategy.RE_2_OPT;*/
		else
			valid = false;

		int differenceCount = Integer.parseInt(variant[2]);
		if (differenceCount > 0 && differenceCount < 3)
			MUTATION_DIFFERENCES_COUNT = differenceCount;
		else
			valid = false;
		
		if (variant.length <= 4) {
			if (variant[3].equals("Bin"))
				CROSSOVER = Crossover.BINARY;
			else if (variant[3].equals("Eig"))
				CROSSOVER = Crossover.EIGENVECTOR;
			else
				valid = false;
		}
		else
			valid = false;
		
		if (!valid)
			throw new IllegalArgumentException(String.format("This variance %s isn't supported", DEProperties.VARIANT));
	}
}