package br.ufrj.coc.cec2015.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.Properties;

public class MatrixUtil {

	//public static RealMatrix getCovarianceMatrix(double[][] matrix) {
	//	RealMatrix realMatrix = new Array2DRowRealMatrix(matrix);
	//	Covariance covariance = new Covariance(realMatrix);
	//	return covariance.getCovarianceMatrix();
	//}

	public static EigenDecomposition getEigenDecomposition(Population population) {
		int D = Properties.ARGUMENTS.get().getIndividualSize();
		int NP = population.size();
		
		// Covariance matrix (12)
		double[][] C = new double[D][D];
		double[] m = new double[D];

		for (int j = 0; j < D; ++j) {
			m[j] = population.get(0).get(j);
		}

		for (int i = 1; i < NP; ++i) {
			for (int j = 0; j < D; ++j) {
				m[j] += population.get(i).get(j);
			}
		}

		for (int i = 0; i < D; ++i) {
			m[i] /= NP;
		}
		
		for (int i = 0; i < D; ++i) {
			for (int j = 0; j < D; ++j) {
				C[i][j] = 0;
				for (int k = 0; k < NP; ++k) {
					C[i][j] += (population.get(k).get(i) - m[i]) * (population.get(k).get(j) - m[j]);
				}
				C[i][j] /= (NP - 1);
			}
		}
		/*
		System.out.println("---------- POPULATION --------------");
		double[][] matrixPopulation = population.toMatrix();
		for (int index = 0; index < matrixPopulation.length; index++)
			System.out.println(Arrays.toString(matrixPopulation[index]));
		
		System.out.println("---------- COVARIANCE MATRIX --------------");
		for (int index = 0; index < C.length; index++)
			System.out.println(Arrays.toString(C[index]));
		*/
		// Eigendecomposition (14)
		RealMatrix RM_C = new Array2DRowRealMatrix(C);
		EigenDecomposition ED_Q = new EigenDecomposition(RM_C);
		/*
		System.out.println("\n---------- Q: EIGEN VECTOR --------------");
		double[][] eigenVector = ED_Q.getV().getData();
		for (int index = 0; index < eigenVector.length; index++)
			System.out.println(Arrays.toString(eigenVector[index]));
		System.out.println();
		
		System.out.println("\n---------- Q*: EIGEN VECTOR TRANSPOST --------------");
		double[][] eigenVectorT = ED_Q.getVT().getData();
		for (int index = 0; index < eigenVectorT.length; index++)
			System.out.println(Arrays.toString(eigenVectorT[index]));
		System.out.println();
		*/
		return ED_Q;
	}
}
