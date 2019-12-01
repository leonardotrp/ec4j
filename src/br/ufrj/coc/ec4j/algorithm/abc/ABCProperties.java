package br.ufrj.coc.ec4j.algorithm.abc;

import java.util.ResourceBundle;

import br.ufrj.coc.ec4j.util.Properties;

/**
 * @author Leonardo
 */
public class ABCProperties {
	static ResourceBundle bundle = ResourceBundle.getBundle("abc");
	
	public static int MAX_TRIAL = Integer.parseInt(bundle.getString("MAX_TRIAL"));
	public static double MR = Double.parseDouble(bundle.getString("MR")); // MCAB
	public static double C = Double.parseDouble(bundle.getString("C")); // GbABC
	public static String[] VARIANTS = bundle.getString("VARIANTS").split(",");

	enum ABCVariant {
		ABC, MABC, GbABC, GbdABC, AloABC
	}
	
	public static boolean isABC() {
		return ABCVariant.ABC.equals(ABCVariant.valueOf(Properties.ARGUMENTS.get().getVariant()));
	}

	public static boolean isMABC() {
		return ABCVariant.MABC.equals(ABCVariant.valueOf(Properties.ARGUMENTS.get().getVariant()));
	}

	public static boolean isGbABC() {
		return ABCVariant.GbABC.equals(ABCVariant.valueOf(Properties.ARGUMENTS.get().getVariant()));
	}

	public static boolean isGbdABC() {
		return ABCVariant.GbdABC.equals(ABCVariant.valueOf(Properties.ARGUMENTS.get().getVariant()));
	}

	public static boolean isAloABC() {
		return ABCVariant.AloABC.equals(ABCVariant.valueOf(Properties.ARGUMENTS.get().getVariant()));
	}
}