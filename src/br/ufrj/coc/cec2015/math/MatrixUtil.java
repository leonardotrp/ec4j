package br.ufrj.coc.cec2015.math;

import java.io.IOException;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import br.ufrj.coc.cec2015.algorithm.AlgorithmArguments;
import br.ufrj.coc.cec2015.algorithm.Individual;
import br.ufrj.coc.cec2015.algorithm.Initializable;
import br.ufrj.coc.cec2015.algorithm.Population;
import br.ufrj.coc.cec2015.util.FileUtil;
import br.ufrj.coc.cec2015.util.Helper;
import br.ufrj.coc.cec2015.util.Properties;

public class MatrixUtil {

	//public static RealMatrix getCovarianceMatrix(double[][] matrix) {
	//	RealMatrix realMatrix = new Array2DRowRealMatrix(matrix);
	//	Covariance covariance = new Covariance(realMatrix);
	//	return covariance.getCovarianceMatrix();
	//}

	public static Matrix getCovarianceMatrix(Population population) {
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
		//RealMatrix RM_C = new Array2DRowRealMatrix(C);
		//EigenDecomposition ED_Q = new EigenDecomposition(RM_C);
		return new Matrix(C);
	}
	
	public static EigenvalueDecomposition getEigenDecomposition(Population population) {
		Matrix M_C = getCovarianceMatrix(population);
		EigenvalueDecomposition ED_Q = M_C.eig();
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

	public static void main(String[] args) throws IOException {
		AlgorithmArguments arguments = new AlgorithmArguments("JADE", "", "", 1, 10);
		Properties.ARGUMENTS.set(arguments);

		Initializable initializable = new Initializable() {
			@Override
			public Individual newInitialized(double[] id) {
				return Helper.newIndividualInitialized(id);
			}
			
			@Override
			public Individual newInitialized() {
				return Helper.newIndividualInitialized();
			}
		};

		Population populationA = new Population(initializable, FileUtil.getInitialPopulationFile("populationA.csv"));
		EigenvalueDecomposition eigA = MatrixUtil.getEigenDecomposition(populationA);
		Matrix eigenvectorA = eigA.getV();
		
		Population populationB = new Population(initializable, FileUtil.getInitialPopulationFile("populationB.csv"));
		EigenvalueDecomposition eigB = MatrixUtil.getEigenDecomposition(populationB);
		Matrix eigenvectorB = eigB.getV();
		
		double sPCA = similarityPCA(eigenvectorA, eigenvectorB, 3);
		System.err.println(sPCA);
		/*
		for (int i = 0; i < 2; i++) {
			Population population = new Population(initializable);
			population.write(new File("population" + i + ".csv"));
		}
		*/
	}

	/**
	 * Krzanowski, W. J. Between-Groups Comparison of Principal Components. J. Amer. Stat. Assoc., 74(367), 703–707 (1979)
	 * @param eigenvectorA
	 * @param eigenvectorB
	 * @param k
	 * @return sPCA
	 */
	private static double similarityPCA(Matrix eigenvectorA, Matrix eigenvectorB, int k) {
		int size = eigenvectorA.getColumnDimension();
		double sPCA = 0.0;
		for (int i = 0; i < size; i++) {
			double numerator = 0.0;
			double sumPowA = 0.0, sumPowB = 0.0;
			for (int j = 0; j < k; j++) {
				double a = eigenvectorA.get(i, j);
				double b = eigenvectorB.get(i, j);
				numerator += a * b;
				sumPowA += Math.pow(a, 2);
				sumPowB += Math.pow(b, 2);
			}
			double denominator = Math.sqrt(sumPowA) * Math.sqrt(sumPowB);
			double cosAngle = numerator / denominator;
			sPCA += Math.pow(cosAngle, 2);
		}
		sPCA /= size;
		return sPCA;
	}
}
