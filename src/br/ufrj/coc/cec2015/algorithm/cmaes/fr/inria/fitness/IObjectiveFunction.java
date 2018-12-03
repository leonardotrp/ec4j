package br.ufrj.coc.cec2015.algorithm.cmaes.fr.inria.fitness;

/** Minimalistic interface of a single-objective function (fitness function) to be minimized. 
*/
public interface IObjectiveFunction {
    /** @param x  a point (candidate solution) in the pre-image of the objective function 
        @return  objective function value of the input search point  
     */
    double valueOf(double x[]);
    boolean isFeasible(double x[]);
}

