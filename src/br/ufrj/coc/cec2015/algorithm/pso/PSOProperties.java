package br.ufrj.coc.cec2015.algorithm.pso;

import java.util.ResourceBundle;

/**
 * @author Leonardo
 */
public class PSOProperties {
	static ResourceBundle bundle = ResourceBundle.getBundle(PSOProperties.class.getPackage().getName() + ".pso");
	
	public static double FI_1 = Double.parseDouble(bundle.getString("ACCELERATION_COEFFICIENT_1"));
	public static double FI_2 = Double.parseDouble(bundle.getString("ACCELERATION_COEFFICIENT_2"));
	public static double W_STATIC = Double.parseDouble(bundle.getString("INERTIA_WEIGHT"));
	public static double W_START = Double.parseDouble(bundle.getString("INERTIA_WEIGHT_START"));
	public static double W_END = Double.parseDouble(bundle.getString("INERTIA_WEIGHT_END"));
	public static double THRESHOULD_VALUE = Double.parseDouble(bundle.getString("THRESHOULD_VALUE"));
	public static String[] VARIANTS = bundle.getString("VARIANTS").split(",");
	public static String VARIANT;
	public static void setVariant(String variant) {
		VARIANT = variant;
	}
}