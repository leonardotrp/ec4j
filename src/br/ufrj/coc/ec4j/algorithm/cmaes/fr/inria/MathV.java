package br.ufrj.coc.ec4j.algorithm.cmaes.fr.inria;

/**
 * Mathematical functions for vectors.
 * Provides methods similar to class {@link java.lang.Math} but for vector 
 * arguments.
 *
 * @author Dirk Bueche
 */
public class MathV {

  /**
   * generates a vector of doubles that are linearly spaced within [min, max].
   *
   * @param min minimum
   * @param max maximum
   * @param N length of vector
   * @return
   */
  public static double[] linspace(double min, double max, int N) {
    double x[] = new double[N];
    for (int i = 0; i < N; i++) {
      x[i] = min + (max - min) / (N - 1) * i;
    }
    return x;
  }

  /**
   * generates a vector of doubles that are logarithmically spaced within [min,
   * max].
   *
   * @param min minimum
   * @param max maximum
   * @param N length of vector
   * @return
   */
  public static double[] logspace(double min, double max, int N) {
    double x[] = linspace(Math.log(min), Math.log(max), N);
    for (int i = 0; i < N; i++) {
      x[i] = Math.exp(x[i]);
    }
    return x;
  }

  /** Computes the length of a vector in Euclidean space (know as the L2-norm.
   * 
   * @param a vector
   * @return l2 norm of the vector.
   */
  public static double norm2(double[] a) {
    double l = 0;
    for (int i = 0; i < a.length; i++) {
      l += a[i]*a[i];
    }
    return Math.sqrt(l);
  }

  /**
   * computes exponential function for vector a and scalar b as: a^b
   *
   * @param a basis
   * @param b exponent
   * @return result of exponentiation
   */
  public static double[] pow(double a[], double b) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = Math.pow(a[i], b);
    }
    return r;
  }

  /**
   * computes the square root of each vector element.
   *
   * @param a vector
   * @return result of square root
   */
  public static double[] sqrt(double a[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = Math.sqrt(a[i]);
    }
    return r;
  }

  /**
   * computes tangents of a vector a.
   *
   * @param a values
   * @return tan(a)
   */
  public static double[] tan(double a[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = Math.tan(a[i]);
    }
    return r;
  }

  /**
   * multiplies each element of vector a with vector b and returns vector c = a.*b .
   *
   * @param a vector
   * @param b vector
   * @return result of multiplication
   */
  public static double[] multiply(double a[], double b[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[i] * b[i];
    }
    return r;
  }

  /**
   * Multiplies each element of vector a with a scalar number b and returns 
   * result vector c.
   *
   * @param a vector
   * @param b scalar
   * @return result of multiplication
   */
  public static double[] multiply(double a[], double b) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[i] * b;
    }
    return r;
  }

  /**
   * divides each element of vector a by vector b and returns vector c = a./b .
   *
   * @param a divident
   * @param b divisor
   * @return result of division
   */
  public static double[] divide(double a[], double b[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[i] / b[i];
    }
    return r;
  }

  /**
   * divides each element of vector a by scalar b and returns vector c = a./b .
   *
   * @param a divident
   * @param b divisor
   * @return result of division
   */
  public static double[] divide(double a[], double b) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[i] / b;
    }
    return r;
  }

  /**
   *  vectors c = a + b .
   *
   * @param a vector
   * @param b vector
   * @return result of addition
   */
  public static double[] plus(double a[], double b[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[i] + b[i];
    }
    return r;
  }

  /**
   *  vectors c = a + scalar.
   *
   * @param a vector
   * @param b scalar
   * @return result of addition
   */
  public static double[] plus(double a[], double b){
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[i] + b;
    }
    return r;
  }

  /**
   *  vectors c = a - b .
   *
   * @param a vector
   * @param b vector
   * @return result of substraction
   */
  public static double[] minus(double a[], double b[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[i] - b[i];
    }
    return r;
  }

  /**
   *  Returns maximum of a vector.
   *
   * @param a vector
   * @return m maximum value
   */
  public static double max(double a[]) {
    double m = a[0];
    for (int i = 0; i < a.length; i++){
      m = Math.max(a[i],m);
    }
    return m;
  }

  /**
   *  Returns maximum of a vector.
   *
   * @param a vector
   * @return m maximum value
   */
  public static double min(double a[]) {
    double m = a[0];
    for (int i = 0; i < a.length; i++){
      m = Math.min(a[i],m);
    }
    return m;
  }

  /**
   * computes the inverse of a vector a.
   *
   * @param a
   * @return inverse of each element of a
   */
  public static double[] invert(double a[]) {
    double[] r = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = 1. / a[i];
    }
    return r;
  }
  /**
   * computes the absolute value of a vector a.
   *
   * @param a
   * @return absolute value of each element of a
   */
  public static double[] abs(double a[]) {
    double[] r = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = Math.abs(a[i]);
    }
    return r;
  }

  /**
   * computes atan2 function for vector x and y, see {@link Math#atan2(double, double)}.
   *
   * @param y vector
   * @param x vector
   * @return result
   */
  public static double[] atan2(double y[], double x[]) {
    double r[] = new double[x.length];
    for (int i = 0; i < x.length; i++) {
      r[i] = Math.atan2(y[i], x[i]);
    }
    return r;
  }

  /**
   * computes atan function for vector a.
   *
   * @param a in radians
   * @return result
   */
  public static double[] atan(double a[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = Math.atan(a[i]);
    }
    return r;
  }
  /**
   * computes degree to radians for vector a.
   *
   * @param a in radians
   * @return result
   */
  public static double[] toRadians(double a[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = Math.toRadians(a[i]);
    }
    return r;
  }
  /**
   * computes radians to degree for vector a.
   *
   * @param a in degrees
   * @return result
   */
  public static double[] toDegrees(double a[]) {
    double r[] = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = Math.toDegrees(a[i]);
    }
    return r;
  }
}
