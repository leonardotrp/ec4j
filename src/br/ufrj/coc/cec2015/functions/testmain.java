package br.ufrj.coc.cec2015.functions;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

public class testmain {
	public static void main(String[] args) throws Exception {
		int j, index_data, dim, m, func_num;
		double[] f_out, x_in;
		File fpt;

		m = 2; // número de vetores de entrada, para que as funções sejam executadas 'm' vezes, uma para cada vetor x_in
		dim = 10; // dimensão 'D' que corresponde ao tamanho do vetor de entrada

		x_in = new double[m * dim]; // vetor de entrada de tamanho m * dim (o dobro da dimensão selecionada)
		f_out = new double[m]; // vetor de saída de tamanho m

		testfunc tf = new testfunc();

		// preparação e chamada das 15 funções implementadas
		for (func_num = 1; func_num <= 15; func_num++) {

			// @leonardo
			URL urlInput = testfunc.getInputData("shift_data_" + func_num + ".txt");
			fpt = new File(urlInput.toURI());
			Scanner input = null;
			try {
				input = new Scanner(fpt);
				input.useLocale(Locale.US);
	
				// carrega os 'dim' primeiros dados de entrada no vetor 'x_in' de tamanho 'n'
				for (index_data = 0; index_data < dim; index_data++) {
					x_in[index_data] = input.nextDouble();
				}
	
				input.close();
	
				// como o tamanho do vetor é 'm * dim' (no caso 2 x 10), preenche com 0.0 última metade do vetor ( x_in[10] ... x_in[19] )
				for (j = 0; j < dim; j++) {
					x_in[1 * dim + j] = 0.0;
				}
	
				//for (index_data = 0; index_data < 1; index_data++) {
	
					// executa a função uma única vez
					tf.test_func(x_in, f_out, dim, m, func_num);
	
					for (j = 0; j < m; j++) {
						// printa as duas saídas da função 'func_num'
						System.out.println("f" + func_num + "(x[" + (j + 1) + "])=" + f_out[j]);
					}
				//}
			}
			finally {
				if (input != null)
					input.close();
			}
		}
	}
}
