package br.ufrj.coc.ec4j.algorithm.de;

import java.util.ResourceBundle;

import br.ufrj.coc.ec4j.util.Properties;

/**
 * @author Leonardo
 */
public class DEProperties {
	static ResourceBundle bundle = ResourceBundle.getBundle("de");	
	public static double CROSSOVER_RATE = Double.parseDouble(bundle.getString("CROSSOVER_RATE"));
	public static double DIFFERENTIAL_WEIGHT = Double.parseDouble(bundle.getString("DIFFERENTIAL_WEIGHT"));
	public static String[] VARIANTS = bundle.getString("VARIANTS").split(",");
	public static String INFO = bundle.getString("INFO");
	public static boolean EXTERNAL_ARCHIVE = Boolean.parseBoolean(bundle.getString("EXTERNAL_ARCHIVE"));
	public static double ADAPTATION_RATE = Double.parseDouble(bundle.getString("ADAPTATION_RATE"));
	public static double GREEDINESS = Double.parseDouble(bundle.getString("GREEDINESS"));
	public static double EIG_RATE = Double.parseDouble(bundle.getString("EIG_RATE"));
	public enum EigRateAdaptation {
		ASC, DESC
	}
	private static String EIG_RATE_ADAPTATIVE = bundle.getString("EIG_RATE_ADAPTATION");
	public static EigRateAdaptation EIG_RATE_ADAPTATION;
	static {
		boolean useEigRateAdaptation = EIG_RATE_ADAPTATIVE.equals("True") || EIG_RATE_ADAPTATIVE.equals("Asc") || EIG_RATE_ADAPTATIVE.equals("Desc");
		if (useEigRateAdaptation) {
			if (EIG_RATE_ADAPTATIVE.equals("True"))
				EIG_RATE_ADAPTATION = EigRateAdaptation.ASC;
			else
				EIG_RATE_ADAPTATION = EigRateAdaptation.valueOf(EIG_RATE_ADAPTATIVE.toUpperCase());
		}
	}

	public static double CR_MAX_FUNCVAL = Double.parseDouble(bundle.getString("CR_MAX_FUNCVAL"));
	public static double CR_MAX_DIST = Double.parseDouble(bundle.getString("CR_MAX_DIST"));
	public static double CR_MAXFES_INTERVAL = Double.parseDouble(bundle.getString("CR_MAXFES_INTERVAL"));

	public enum Strategy {
		BEST, RAND, RE_BASE, RE_ALL, // BEST_2_OPT, RAND_2_OPT, RE_2_OPT
		CURRENT_TO_BEST,
		CURRENT_TO_RAND,
		CURRENT_TO_pBEST /* JADE */
	}
	public enum Crossover {
		BINARY, EIGENVECTOR
	}

	private String variant;
	private Strategy strategy;
	private Crossover crossover;
	private int mutationDifferenceCount;
	
	public DEProperties() {
		super();
		this.loadVariant();
	}

	private void loadVariant() {
		this.variant = Properties.ARGUMENTS.get().getVariant();

		boolean valid = true;
		String[] variants = this.variant.split("/");
		if (variants[1].equals("best"))
			this.strategy = Strategy.BEST;
		else if (variants[1].equals("rand"))
			this.strategy = Strategy.RAND;
		else if (variants[1].equals("current-to-best"))
			this.strategy = Strategy.CURRENT_TO_BEST;
		else if (variants[1].equals("current-to-rand"))
			this.strategy = Strategy.CURRENT_TO_RAND;
		else if (variants[1].equals("current-to-pbest"))
			this.strategy = Strategy.CURRENT_TO_pBEST;
		else if (variants[1].equals("re-base"))
			this.strategy = Strategy.RE_BASE;
		else if (variants[1].equals("re-all"))
			this.strategy = Strategy.RE_ALL;
		else
			valid = false;

		int differenceCount = Integer.parseInt(variants[2]);
		if (differenceCount > 0 && differenceCount < 3)
			this.mutationDifferenceCount = differenceCount;
		else
			valid = false;
		
		if (variants.length <= 4) {
			if (variants[3].equals("Bin"))
				this.crossover = Crossover.BINARY;
			else if (variants[3].equals("Eig"))
				this.crossover = Crossover.EIGENVECTOR;
			else
				valid = false;
		}
		else
			valid = false;
		
		if (!valid)
			throw new IllegalArgumentException(String.format("This variance %s isn't supported", this.variant));
	}
	
	public int getMutationDifferenceCount() {
		return this.mutationDifferenceCount;
	}
	
	public boolean isRouletteAllStrategy() {
		return this.strategy.equals(Strategy.RE_ALL);
	}

	public boolean isCurrentToStrategy() {
		return isCurrentToBestStrategy() || isCurrentToRandStrategy() || isJADE();
	}

	public boolean isCurrentToBestStrategy() {
		return this.strategy.equals(Strategy.CURRENT_TO_BEST);
	}
	
	public boolean isCurrentToRandStrategy() {
		return this.strategy.equals(Strategy.CURRENT_TO_RAND);
	}
	
	public boolean isBestStrategy() {
		return this.strategy.equals(Strategy.BEST);
	}
	
	public boolean isRandStrategy() {
		return this.strategy.equals(Strategy.RAND);
	}

	public boolean isRouletteStrategy() {
		return this.strategy.equals(Strategy.RE_BASE) || isRouletteStrategyOthers();
	}
	
	public boolean isRouletteStrategyOthers() {
		return this.strategy.equals(Strategy.RE_ALL);
	}
	
	public boolean isJADE() {
		return this.strategy.equals(Strategy.CURRENT_TO_pBEST);
	}

	public boolean isJADEWithArchieve() {
		return isJADE() && DEProperties.EXTERNAL_ARCHIVE;
	}
	
	public boolean isEigenvectorCrossover() {
		return this.crossover.equals(Crossover.EIGENVECTOR);
	}
}