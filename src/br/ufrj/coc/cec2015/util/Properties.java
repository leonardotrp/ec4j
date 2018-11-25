package br.ufrj.coc.cec2015.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Leonardo
 */
public class Properties {
	static ResourceBundle bundle = ResourceBundle.getBundle("cec2015");

	public static double[] SEARCH_RANGE;
	static {
		double min = Double.parseDouble(bundle.getString("SEARCH_RANGE_MIN"));
		double max = Double.parseDouble(bundle.getString("SEARCH_RANGE_MAX"));
		SEARCH_RANGE = new double[] { min, max };
	}

	public static int MAX_RUNS = Integer.parseInt(bundle.getString("MAX_RUNS"));
	public static double MIN_ERROR_VALUE = 1.0 * Double.parseDouble(bundle.getString("MIN_ERROR_VALUE"));

	public static int FUNCTION_NUMBER;
	public static int[] FUNCTIONS;
	static {
		String[] functionNumbers = bundle.getString("FUNCTIONS").split(",");
		FUNCTIONS = new int[functionNumbers.length];
		for (int index = 0; index < functionNumbers.length; index++)
			FUNCTIONS[index] = Integer.parseInt(functionNumbers[index]);
	}

	public static String[] ALGORITHMS = bundle.getString("ALGORITHMS").split(",");
	private static Map<String, Integer> POPULATION_SIZES = new HashMap<>(ALGORITHMS.length);
	static {
		for (String algorithm : ALGORITHMS)
			POPULATION_SIZES.put(algorithm, Integer.parseInt(bundle.getString("POPULATION_SIZE." + algorithm)));
	}
	private static String CURRENT_ALGORITHM;
	public static int POPULATION_SIZE;
	public static void setCurrentAlgorithm(String algorithm) {
		CURRENT_ALGORITHM = algorithm;
		POPULATION_SIZE = POPULATION_SIZES.get(CURRENT_ALGORITHM);
	}
	public static String[] INDIVIDUAL_SIZES = bundle.getString("INDIVIDUAL_SIZE").split(",");
	public static int INDIVIDUAL_SIZE;
	public static int MAX_FES;
	public static void setCurrentIndividualSize(int individualSize) {
		INDIVIDUAL_SIZE = individualSize;
		MAX_FES = 10000 * INDIVIDUAL_SIZE;
	}

	public static String RESULTS_ROOT = Properties.class.getResource("/") + bundle.getString("RESULTS_ROOT");
}