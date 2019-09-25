package br.ufrj.coc.cec2015.util;

import java.util.Arrays;
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
	public static Map<Integer, double[]> SEARCH_RANGE_BY_FUNCTION = new HashMap<>();
	public static double[] getSearchRange() {
		int functionNumber = ARGUMENTS.get().getFunctionNumber();
		double[] searchRange = SEARCH_RANGE_BY_FUNCTION.get(functionNumber);
		try {
			if (searchRange == null) {
				double min = Double.parseDouble(bundle.getString("SEARCH_RANGE_MIN." + functionNumber));
				double max = Double.parseDouble(bundle.getString("SEARCH_RANGE_MAX." + functionNumber));
				searchRange = new double[] {min, max};
				SEARCH_RANGE_BY_FUNCTION.put(functionNumber, searchRange);
			}
		}
		catch (Exception e) {
			searchRange = SEARCH_RANGE;
		}
		return searchRange;
	}
	public static int MAX_RUNS = Integer.parseInt(bundle.getString("MAX_RUNS"));
	public static double MIN_ERROR_VALUE = 1.0 * Double.parseDouble(bundle.getString("MIN_ERROR_VALUE"));
	public static int MAX_GENERATIONS = Integer.parseInt(bundle.getString("MAX_GENERATIONS"));
	
	static String STOP_CRITERIAS[] = bundle.getString("STOP_CRITERIAS").split(",");
	public static boolean STOP_BY_MIN_ERROR = Arrays.stream(STOP_CRITERIAS).anyMatch("MinError"::equals);
	public static boolean STOP_BY_MAX_FES = Arrays.stream(STOP_CRITERIAS).anyMatch("MaxFES"::equals);
	public static boolean STOP_BY_MAX_GEN = Arrays.stream(STOP_CRITERIAS).anyMatch("MaxGen"::equals);
	
	public static String RESULTS_ROOT = Properties.class.getResource("/") + bundle.getString("RESULTS_ROOT");
	public static String INITIAL_POPULATION_FILE = bundle.getString("INITIAL_POPULATION_FILE");
	
	public static boolean USE_PROJECTIONS = Boolean.parseBoolean(bundle.getString("USE_PROJECTIONS"));
	public static boolean SHOW_PROJECTIONS = Boolean.parseBoolean(bundle.getString("SHOW_PROJECTIONS"));
	enum UpdateProjections {
		ALL, INSTANT
	}
	private static UpdateProjections UPDATE_PROJECTIONS;
	static {
		String strExportProjections = bundle.getString("EXPORT_PROJECTIONS");
		if (strExportProjections != null)
			UPDATE_PROJECTIONS = UpdateProjections.valueOf(bundle.getString("UPDATE_PROJECTIONS").toUpperCase());
	}
	public static boolean isUpdateProjectionsAll() {
		return UpdateProjections.ALL.equals(UPDATE_PROJECTIONS) && (ARGUMENTS.get().getCountEvaluations() % EXPORT_BY_EVALUATIONS_MOD == 0);
	}
	public static boolean isUpdateProjectionsInstant() {
		return UpdateProjections.INSTANT.equals(UPDATE_PROJECTIONS);
	}
	public static boolean EXPORT_PROJECTIONS = Boolean.parseBoolean(bundle.getString("EXPORT_PROJECTIONS"));
	public static int EXPORT_BY_EVALUATIONS_MOD = Integer.parseInt(bundle.getString("EXPORT_BY_EVALUATIONS_MOD"));
	
	public static int[] FUNCTION_NUMBERS;
	static {
		String[] functionNumbers = bundle.getString("FUNCTIONS").split(",");
		FUNCTION_NUMBERS = new int[functionNumbers.length];
		for (int index = 0; index < functionNumbers.length; index++)
			FUNCTION_NUMBERS[index] = Integer.parseInt(functionNumbers[index]);
	}

	public static String[] ALGORITHMS = bundle.getString("ALGORITHMS").split(",");
	public static Map<String, Integer> POPULATION_SIZES = new HashMap<>(ALGORITHMS.length);
	static {
		for (String algorithm : ALGORITHMS)
			POPULATION_SIZES.put(algorithm, Integer.parseInt(bundle.getString("POPULATION_SIZE." + algorithm)));
	}

	public static int[] INDIVIDUAL_SIZES = Stream.of(bundle.getString("INDIVIDUAL_SIZE").split(",")).mapToInt(Integer::parseInt).toArray();

	public static boolean USE_PCA_SIMILARITY_FACTOR = Boolean.parseBoolean(bundle.getString("USE_PCA_SIMILARITY_FACTOR"));
	public static int K_PCA = Integer.parseInt(bundle.getString("K_PCA")); // <= INDIVIDUAL_SIZE
}