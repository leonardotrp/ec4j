package br.ufrj.coc.cec2013.functions;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;


public class testmain {
	public static void main(String[] args) throws Exception{
		int i,j,k,n,m,func_num;
		double[] x;
		double[] f;

		URL urlInput = testfunc.getInputData("shift_data.txt");
		File fpt = new File(urlInput.toURI());
		Scanner input = new Scanner(fpt);
		input.useLocale(Locale.US);
		
		m=2;
		n=10;
		
		testfunc tf = new testfunc();
		
		x = new double[n*m];
		for(i=0;i<n;i++){
			x[i]=input.nextDouble();
		}
		for(i=0;i<n;i++){
			System.out.println(x[i]);
		}
		input.close();
		
		for(i=1;i<m;i++){
			for(j=0;j<n;j++){
				x[i*n+j]=0.0;
				System.out.println(x[i*n+j]);
			}
		}
		
		f= new double[m];
		for(i=0;i<28;i++){
			func_num=i+1;
			for(k=0;k<1;k++){
				tf.test_func(x,f,n,m,func_num);
				for(j=0;j<m;j++){
					System.out.println("f"+func_num+"(x["+(j+1)+"])="+f[j]);
				}
			}
		}
		
		
	}
}


		