package br.ufrj.coc.cec2015.algorithm.abc;

import java.util.ResourceBundle;

/**
 * @author Leonardo
 */
public class ABCProperties {
	static ResourceBundle bundle = ResourceBundle.getBundle("abc");
	
	public static int MAX_TRIAL = Integer.parseInt(bundle.getString("MAX_TRIAL"));
	public static double MR = Double.parseDouble(bundle.getString("MR")); // MCAB
	public static double C = Double.parseDouble(bundle.getString("C")); // GbABC
	public static String[] VARIANTS = bundle.getString("VARIANTS").split(",");
	public static String VARIANT;
	public static void setVariant(String variant) {
		VARIANT = variant;
	}
}