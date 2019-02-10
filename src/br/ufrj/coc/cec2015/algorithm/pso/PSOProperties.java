package br.ufrj.coc.cec2015.algorithm.pso;

import java.util.ResourceBundle;

/**
 * @author Leonardo
 */
public class PSOProperties {

	static ResourceBundle bundle = ResourceBundle.getBundle("pso");
	
	public static double FI_SELF = Double.parseDouble(bundle.getString("ACCELERATION_COEFFICIENT_SELF"));
	public static double FI_SOCIAL = Double.parseDouble(bundle.getString("ACCELERATION_COEFFICIENT_SOCIAL"));
	public static double W_STATIC = Double.parseDouble(bundle.getString("INERTIA_WEIGHT"));
	public static double W_START = Double.parseDouble(bundle.getString("INERTIA_WEIGHT_START"));
	public static double W_END = Double.parseDouble(bundle.getString("INERTIA_WEIGHT_END"));
	public static double THRESHOULD_VALUE = Double.parseDouble(bundle.getString("THRESHOULD_VALUE"));	
	public static String[] VARIANTS = bundle.getString("VARIANTS").split(",");
	public static String VARIANT;
	public static void setVariant(String variant) {
		VARIANT = variant;
	}

	// Parameters of RIO (Roach Infestation Optimization)
	public static double[] STOP_RATE_FOR_FRIENDS;
	static {
		String rates = bundle.getString("STOP_RATE_FOR_FRIENDS");
		rates = rates.substring(1, rates.length() - 1);
		String[] ratesArray = rates.split(",");
		STOP_RATE_FOR_FRIENDS = new double[ratesArray.length];
		for (int indexRate = 0; indexRate < ratesArray.length; indexRate++) {
			STOP_RATE_FOR_FRIENDS[indexRate] = Double.valueOf(ratesArray[indexRate].trim());
		}
	}
	public static final int HUNGER_INTERVAL = Integer.parseInt(bundle.getString("HUNGER_INTERVAL"));
	public static final boolean HUNGRY = Boolean.parseBoolean(bundle.getString("HUNGRY"));
}