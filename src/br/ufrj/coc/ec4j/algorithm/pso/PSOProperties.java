package br.ufrj.coc.ec4j.algorithm.pso;

import java.util.ResourceBundle;

import br.ufrj.coc.ec4j.util.Properties;

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

	private enum PSOVariant {
		PSO, 
		ConstByEvalNumPSO, 
		ConstByClercPSO /* Maurice Clerc 1999 */, 
		InertiaFactorPSO /* Shi and Eberhart 1998 */,
		ConstByEvalNumPSO_InertiaFactorPSO,
		ConstByClercPSO_InertiaFactorPSO,
		SRPSO /* M.R. Tanweer 2015 */,
		iSRPSO /* Improved SRPSO for CEC2015 - M.R. Tanweer 2015 */,
		/*OBLPSO,*/
		RIO /* Roach Infestation Optimization */,
		RIO_ConstByClercPSO
	}
	/*
	public static boolean isOBLPSO() {
		return PSOVariant.OBLPSO.equals(VARIANT);
	}
	*/
	public static boolean isRIO() {
		PSOVariant variant = PSOVariant.valueOf(Properties.ARGUMENTS.get().getVariant());
		return PSOVariant.RIO.equals(variant) || PSOVariant.RIO_ConstByClercPSO.equals(variant);
	}

	public static boolean isSRPSO() {
		PSOVariant variant = PSOVariant.valueOf(Properties.ARGUMENTS.get().getVariant());
		return PSOVariant.SRPSO.equals(variant) || PSOVariant.iSRPSO.equals(variant);
	}
	
	public static boolean isImproveSRPSO() {
		PSOVariant variant = PSOVariant.valueOf(Properties.ARGUMENTS.get().getVariant());
		return PSOVariant.iSRPSO.equals(variant);
	}
	
	public static boolean isConstByEvalNum() {
		return Properties.ARGUMENTS.get().getVariant().contains("ConstByEvalNum");
	}
	
	public static boolean isConstByClerc() {
		return Properties.ARGUMENTS.get().getVariant().contains("ConstByClerc");
	}
	
	public static boolean isInertiaFactoryPSO() {
		return Properties.ARGUMENTS.get().getVariant().contains("InertiaFactor");
	}
}