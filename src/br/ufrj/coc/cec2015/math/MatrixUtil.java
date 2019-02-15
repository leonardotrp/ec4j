package br.ufrj.coc.cec2015.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

public class MatrixUtil {
	public enum EigenMethod {
		Reinsch, Jacobi
	}

	public static RealMatrix getCovarianceMatrix(double[][] matrix) {
		RealMatrix realMatrix = new Array2DRowRealMatrix(matrix);
		Covariance covariance = new Covariance(realMatrix);
		return covariance.getCovarianceMatrix();
	}

	/**
	 * Method getEigenDecomposition - Calculates the Eigen decomposition of the given real matrix 
	 * @param population
	 * @return
	 */
	public static RealMatrix getEigenDecomposition(RealMatrix realMatrix, EigenMethod method) {
		/*EigenDecomposition eigenDecomposition;
		if (method.equals(EigenMethod.Jacobi))
			eigenDecomposition = new JacobiEigenDecomposition(realMatrix);
		else if (method.equals(EigenMethod.Reinsch))
			eigenDecomposition = new ReinschEigenDecomposition(realMatrix);
		else
			throw new IllegalArgumentException("Invalid Eigen Decomposition Method!");
		*/
		EigenDecomposition eigenDecomposition = new EigenDecomposition(realMatrix);
		return eigenDecomposition.getV();
	}
}
