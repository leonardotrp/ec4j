package br.ufrj.coc.ec4j.math.eigen;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class ReinschEigenDecomposition extends EigenDecomposition implements br.ufrj.coc.ec4j.math.eigen.EigenDecomposition {
	/**
	 * @param matrix
	 * @throws MathArithmeticException
	 */
	public ReinschEigenDecomposition(RealMatrix matrix) throws MathArithmeticException {
		super(matrix);
	}
}
