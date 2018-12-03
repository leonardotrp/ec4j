package br.ufrj.coc.cec2015.algorithm.cmaes.fr.inria;

import br.ufrj.coc.cec2015.algorithm.cmaes.jama.EigenvalueDecomposition;
import br.ufrj.coc.cec2015.algorithm.cmaes.jama.Matrix;

/** Different methods for the CMA-ES to set a minimum mutation
 * for the mutation operator.
 *
 * @author dirk
 */
public class CMAMinStep {

  /** This methods selects which actual procedure applies.
   * 
   * @param diagD
   * @param Bmat
   * @param CoVar
   * @param sigma
   * @param lowStd
   * @return sigma  standard deviation
   */
  public static double setMinStep(double[] diagD, double [][] Bmat, double [][] CoVar, double sigma, double [] lowStd){
  
    int method = 2;   // TODO: finalize and use only one method
    switch (method){ 
      case 0:{ // preferred by Nikolaus Hansen, adjust global step size
        sigma = CMAMinStep.setMinStepSigma(CoVar, sigma, lowStd);
        break;
      }
      case 1:{ // proposed by Dirk, does not work for correlated functions as mutation ellipse may degrade to a line
        CMAMinStep.setMinStepCoVar1(CoVar, sigma, lowStd);
        break;
      }
      case 2:{ // implemented by Dirk, works for correlated functions
        CMAMinStep.setMinStepCoVar2(diagD, Bmat, CoVar, sigma, lowStd);
        break;
      }
      case 3:{
        CMAMinStep.setMinStepCoVar3(diagD, Bmat, CoVar, sigma, lowStd);
        break;
      }            
      case 4:{
        CMAMinStep.setMinStepCoVar4(diagD, Bmat, CoVar, sigma, lowStd);
        break;
      }            
      default:{
        throw new RuntimeException("wrong method in CMAMinStep.");
      }
    }
    return sigma;
  }
  
  /** Adjust the global step size sigma such that 
   * for all i, sqrt(C(i,i)) * sigma >= lowStd(i). <p>
   * Advantages:<br>
   * + correlation information and individual step sizes are maintained<p>
   * Disadvantages:<br>
   * - Does not work for highly degrades mutation ellipsoids:
   *     Covariance matrix might degrade for correlated functions into a line 
   *     such that
   *     variation is limited to only one space direction (the main eigenvalue)
   * 
   * @param CoVar
   * @param sigma
   * @param lowStd
   * @return sigma  global step size
   * @deprecated 
   */
  public static double setMinStepSigma(double[][] CoVar, double sigma, double[] lowStd){

    int N = CoVar.length;     // number of design variables
    for (int i = 0; i < N; ++i){
      double fac = lowStd[i]/(sigma * Math.sqrt(CoVar[i][i]));
      if (fac > 1){
        sigma *= fac;
      }
    }
    return sigma;
  }
  
  /** Adjusts the diagonal elements of the Covariance Matrix C(i,i) such that for all i
   * sqrt(C(i,i))*sigma >= lowStd(i).<p>
   * 
   * Advantages:<br>
   * + increases the overall mutation strength less than {@link #setMinStepSigma(double[][], double, double[])}
   *   as it increases only individual directions.<p>
   * Disadvantages:<br>
   * - correlation information and ratio of individual step sizes are not maintained<br>
   * - Does not work for highly degrades mutation ellipsoid (i.e. for functions):
   *     Covariance matrix might degrade for correlated functions into a line such that
   *     variation is limited to only one space direction (the main eigenvalue)
   * 
   * @param CoVar
   * @param sigma
   * @param lowStd
   * @return changed: boolean indicating that the covariance matrix has changed.
   * @deprecated 
   */
  public static boolean setMinStepCoVar1(double[][] CoVar, double sigma, double[] lowStd){

    int N = CoVar.length;     // number of design variables
    boolean changed = false;  // indicator
    for (int i = 0; i < N; ++i) {
      double fac = lowStd[i]/(sigma * Math.sqrt(CoVar[i][i]));
      if (fac > 1){
          CoVar[i][i] *= fac*fac;  // multiply diagonal with d^2, i.e. twice with d
          changed = true;
      }
    }
    return changed;
  }
  /** Adjusts diagonal elements of the Covariance Matrix C(i,i) such that in 
   * all axis directions xi, the mutation is larger than lowStd(i):<p>
   * 
   * sqrt(C(i,i))*sigma >= lowStd(i).<p>
   * 
   * works for correlated functions.
   * 
   * @param diagD
   * @param Bmat
   * @param CoVar
   * @param sigma
   * @param lowStd
   * @return 
   */
  public static boolean setMinStepCoVar2(double[] diagD, double[][] Bmat, double[][] CoVar, double sigma, double[] lowStd){

    int N = CoVar.length;     // number of design variables
    boolean changed = false;  // indicator
    /* Required random vector z to generate mutation in direction i with strength lowStd(i)
     *   sigma*B*D*z = lowStd(i)*e_i                    e_i is the unit vector
     *   <==> z = 1/sigma*D^-1*B' * lowStd(i)*e_i       simplifies further as vector e_i has only one non-zero entry
     * Hints:
     *   B^-1(i,j) =   B(j,i),   B^-1 = B'
     *   D^-1(i,i) = 1/D(i,i),   D is a diagonal matrix, thus for 
    */
    
    // loop over all design variable directions
    for (int i = 0; i < N; ++i) {
      double z = 0;
      double z2 = 0;
      for (int j = 0; j < N; ++j) {
        z += Math.pow(1/sigma/diagD[j]*Bmat[i][j]*lowStd[i], 2);   // compute directly vector length squared
        z2 += Math.pow(1/sigma/diagD[j]*Bmat[i][j], 2);   // compute directly vector length squared
      }
      if (z > 1){
        CoVar[i][i] *= z;   // multiply diagonal elements
        changed = true;
        // we need the full matrix
        double[][] Cn = new double[N][N];
        for (int k = 0; k < N; ++k) {
          for (int m = 0; m <= k; ++m) {
            Cn[k][m] = CoVar[k][m];
            Cn[m][k] = CoVar[k][m];
          }
        }
        EigenvalueDecomposition EV = new EigenvalueDecomposition(new Matrix(Cn));
        Matrix D = EV.getD();
        Matrix B = EV.getV();
        for (int m=0;m<N;m++){
          diagD[m] = Math.sqrt(D.get(m,m));
          for (int n=0;n<N;n++){
            Bmat[m][n] = B.get(m,n);
          }
        }
      }
    }

    return changed;
  }
  /**
   * 
   * @param diagD
   * @param Bmat
   * @param CoVar
   * @param sigma
   * @param lowStd
   * @return 
   */
  public static boolean setMinStepCoVar3(double[] diagD, double[][] Bmat, double[][] CoVar, double sigma, double[] lowStd){

    int N = CoVar.length;     // number of design variables
    boolean changed = false;  // indicator
    
    for (int i = 0; i < N; ++i) { // lower bound for mutation in direction i
      // minimum mutation in an axis direction e(i) should be larger than the minimum standard deviation
      // sigma*B*D*z = e(i)*LowStd(i)   <=>  z = 1/sigma*(D^-1*B')*e(i)*LowStd(i)
      
      double z[] = new double[N];
      for (int j = 0; j < N; ++j){ 
        z[j] = Math.abs(1/sigma/diagD[j]*Bmat[i][j]*lowStd[i]);
        if (z[j] > 1){
          //  diagD[j] *= z[j];   // TODO this is the old
          Bmat[i][j] *= z[j];   // TODO this is a new approach
          changed = true;
        }
      }
    }
    if (changed){
      //C = B*D*D*B'
      double[][] BD = new double[N][N];
      for (int i = 0; i < N; ++i) {
        for (int j = 0; j < N; ++j){
          BD[i][j] = Bmat[i][j]*diagD[j];
        } 
      }
      Matrix BDm = new Matrix(BD);
      Matrix Cm = BDm.times(BDm.transpose());
      for (int i = 0; i < N; ++i) {
        for (int j = 0; j < N; ++j){
        CoVar[i][j] = Cm.get(i,j);
        } 
      }
    }

    return changed;
  }    

  /** Computes the lowStd in the direction of each Eigenvector B(:,i) and
   * adjusts the covariance matrix, if the mutation is too small.
   * 
   * @param diagD
   * @param Bmat
   * @param CoVar
   * @param sigma
   * @param lowStd
   * @return 
   */
  public static boolean setMinStepCoVar4(double[] diagD, double[][] Bmat, double[][] CoVar, double sigma, double[] lowStd){

    int N = CoVar.length;     // number of design variables
    boolean changed = false;  // indicator
    for (int i = 0; i < N; ++i) {  // loop over all Eigenvectors B(:,i)
      // for each Eigenvector B(:,i) compute diag(UC)*z = B(:,i) <=> z = diag(UC^-1)*B(:,i)
      double[] z = new double[N];
      for (int j = 0; j < N; ++j){ 
        z[j] = 1/lowStd[j]*Bmat[j][i];
      }
      double d = MathV.norm2(z);
      double fac = d/(diagD[i]*sigma);
      if (fac > 1){
        for (int j = 0; j < N; ++j){ 
          for (int k = 0; k < N; ++k){ 
           CoVar[j][k] += (fac*fac - 1) * Math.pow(diagD[i],2) * Bmat[j][i]*Bmat[k][i];
          }
        diagD[i] *= fac;
        changed = true;
        }
      }
    }
    return changed;
  }                
}
