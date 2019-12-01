package br.ufrj.coc.ec4j.algorithm;

public interface Initializable {
	public Individual newInitialized();
	public Individual newInitialized(double[] id);
}
