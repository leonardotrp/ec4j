package br.ufrj.coc.cec2015.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import br.ufrj.coc.cec2015.algorithm.AlgorithmArguments;
import br.ufrj.coc.cec2015.algorithm.AlgorithmHelper;

/**
 * @author Leonardo
 */
public class Properties {
	static ResourceBundle bundle = ResourceBundle.getBundle("cec2015");

	public static ThreadLocal<AlgorithmHelper> HELPER = new ThreadLocal<>();
	public static ThreadLocal<AlgorithmArguments> ARGUMENTS = new ThreadLocal<>();

	public static double[] SEARCH_RANGE;
	static {
		double min = Double.parseDouble(bundle.getString("SEARCH_RANGE_MIN"));
		double max = Double.parseDouble(bundle.getString("SEARCH_RANGE_MAX"));
		SEARCH_RANGE = new double[] { min, max };
	}
	public static int MAX_RUNS = Integer.parseInt(bundle.getString("MAX_RUNS"));
	public static double MIN_ERROR_VALUE = 1.0 * Double.parseDouble(bundle.getString("MIN_ERROR_VALUE"));
	public static String RESULTS_ROOT = Properties.class.getResource("/") + bundle.getString("RESULTS_ROOT");

	// -------------------------------------------------------------------------------
	// -------------------- CEC 2015 FUNCTIONS ---------------------------------------
	// -------------------------------------------------------------------------------
	public static int[] FUNCTION_NUMBERS;
	static {
		String[] functionNumbers = bundle.getString("FUNCTIONS").split(",");
		FUNCTION_NUMBERS = new int[functionNumbers.length];
		for (int index = 0; index < functionNumbers.length; index++)
			FUNCTION_NUMBERS[index] = Integer.parseInt(functionNumbers[index]);
	}

	// -------------------------------------------------------------------------------
	// -------------------- ALGORITHMS -----------------------------------------------
	// -------------------------------------------------------------------------------
	public static String[] ALGORITHMS = bundle.getString("ALGORITHMS").split(",");
	public static Map<String, Integer> POPULATION_SIZES = new HashMap<>(ALGORITHMS.length);
	static {
		for (String algorithm : ALGORITHMS)
			POPULATION_SIZES.put(algorithm, Integer.parseInt(bundle.getString("POPULATION_SIZE." + algorithm)));
	}

	// -------------------------------------------------------------------------------
	// -------------------- DIMENSIONS -----------------------------------------------
	// -------------------------------------------------------------------------------
	public static int[] INDIVIDUAL_SIZES = Stream.of(bundle.getString("INDIVIDUAL_SIZE").split(",")).mapToInt(Integer::parseInt).toArray();
}