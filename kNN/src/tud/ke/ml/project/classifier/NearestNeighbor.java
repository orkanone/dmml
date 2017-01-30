package tud.ke.ml.project.classifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import tud.ke.ml.project.util.Pair;

/**
 * This implementation assumes the class attribute is always available (but probably not set).
 * 
 */
public class NearestNeighbor extends INearestNeighbor implements Serializable {
	private static final long serialVersionUID = 1L;

	protected double[] scaling;
	protected double[] translation;
//
	@Override
	public String getMatrikelNumbers() {
		//TODO
		return "";
	}

	@Override
	protected void learnModel(List<List<Object>> data) {
		
	}

	@Override
	protected Map<Object, Double> getUnweightedVotes(List<Pair<List<Object>, Double>> subset) {
		//TODO
		return null;
	}

	@Override
	protected Map<Object, Double> getWeightedVotes(List<Pair<List<Object>, Double>> subset) {
		//TODO
		return null;
	}

	@Override
	protected Object getWinner(Map<Object, Double> votes) {
		//TODO
		return null;
	}

	@Override
	protected Object vote(List<Pair<List<Object>, Double>> subset) {
		//TODO
		return null;
	}

	@Override
	protected List<Pair<List<Object>, Double>> getNearest(List<Object> data) {
		//TODO
		return null;
	}

	@Override
	protected double determineManhattanDistance(List<Object> instance1, List<Object> instance2) {
		//TODO
		return 0.0d;
	}

	@Override
	protected double determineEuclideanDistance(List<Object> instance1, List<Object> instance2) {
		//TODO
		return 0.0d;
	}

	@Override
	protected double[][] normalizationScaling() {
		//TODO
		return null;
	}

}
