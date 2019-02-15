package br.ufrj.coc.cec2015.lab;

import java.util.Arrays;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufrj.coc.cec2015.math.MatrixUtil;
import br.ufrj.coc.cec2015.math.MatrixUtil.EigenMethod;

class TestEIG {
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {
		// fail("Not yet implemented");
		double[][] population = new double[5][2];
		
		// Dados do exemplo em https://pt.wikipedia.org/wiki/Covari%C3%A2ncia
		population[0][0] = 1.95;
		population[0][1] = 93.1; // Pedro
		population[1][0] = 1.96;
		population[1][1] = 93.9; // João
		population[2][0] = 1.95;
		population[2][1] = 89.9; // José
		population[3][0] = 1.98;
		population[3][1] = 95.1; // Renato
		population[4][0] = 2.10;
		population[4][1] = 100.2; // André
		/*
		// Dados do exemplo em https://www.youtube.com/watch?v=vsxljNBuZP0
		population[0][0] = 1;
		population[0][1] = 2;
		population[0][2] = 1;

		population[1][0] = 6;
		population[1][1] = -1;
		population[1][2] = 0;

		population[2][0] = -1;
		population[2][1] = -2;
		population[2][2] = -1;
		*/
		System.out.println("MATRIX");
		for (int index = 0; index < population.length; index++)
			System.out.println(Arrays.toString(population[index]));
		
		System.out.println("\nCOVARIANCE MATRIX");
		Covariance covariance = new Covariance(population);
		RealMatrix realMatrix_C = covariance.getCovarianceMatrix(); 
		double[][] matrix_C = realMatrix_C.getData();
		for (int index = 0; index < matrix_C.length; index++)
			System.out.println(Arrays.toString(matrix_C[index]));
		
		System.out.println("\nEIGEN DECOMPOSITION - REINSCH METHOD");
		RealMatrix reinsch = MatrixUtil.getEigenDecomposition(realMatrix_C, EigenMethod.Reinsch);
		double[][] matrix_Q = reinsch.getData();
		for (int index = 0; index < matrix_Q.length; index++)
			System.out.println(Arrays.toString(matrix_Q[index]));
		/*
		System.out.println("\nEIGEN DECOMPOSITION - JACOBI METHOD");
		RealMatrix jacobi = MatrixUtil.getEigenDecomposition(matrix_C, EigenMethod.Jacobi);
		double[][] matrix_Q2 = jacobi.getData();
		for (int index = 0; index < matrix_Q2.length; index++)
			System.out.println(Arrays.toString(matrix_Q2[index]));
		*/
	}
}
