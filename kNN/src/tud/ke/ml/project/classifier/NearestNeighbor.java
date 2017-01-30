package tud.ke.ml.project.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hamcrest.core.IsNull;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tud.ke.ml.project.util.Pair;

/**
 * This implementation assumes the class attribute is always available (but probably not set).
 * 
 */
public class NearestNeighbor extends INearestNeighbor implements Serializable {
	private static final long serialVersionUID = 1L;

	protected double[] scaling;
	protected double[] translation;
	
	protected List<List<Object>> training_data;
	protected List<Double> v_min;
	protected List<Double> v_max;

	@Override
	public String getMatrikelNumbers() {
		return "Mat NR here";
		//throw new NotImplementedException();
	}

	@Override
	protected void learnModel(List<List<Object>> data) {
		training_data = data;
	}

	@Override
	protected Map<Object, Double> getUnweightedVotes(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> votes = new TreeMap<Object, Double>();
		double vote_count;
		for (Pair<List<Object>, Double> pair : subset){
			Object y = pair.getA().get(pair.getA().size()-1);
			vote_count = 1.;
			if (votes.containsKey(y)){
				vote_count = votes.get(y) + 1.;
			}
			votes.put(y, vote_count);
		}
		
		return votes;
	}

	@Override
	protected Map<Object, Double> getWeightedVotes(List<Pair<List<Object>, Double>> subset) {
		throw new NotImplementedException();
	}

	@Override
	protected Object getWinner(Map<Object, Double> votes) {
		//Collection values = votes.values();
		Object winner = null;
		double class_count = 0;
		boolean first_run = true;
		for (Map.Entry<Object, Double> vote : votes.entrySet()){
			if(first_run){
				class_count = vote.getValue();
				winner = vote.getKey();
			}
			else if(vote.getValue() > class_count){
				class_count = vote.getValue();
				winner = vote.getKey();
			}
			first_run = false;
		}
		
		return winner;
	}

	@Override
	protected Object vote(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> votes = getUnweightedVotes(subset);
		Object winner = getWinner(votes);
		
		return winner;
	}

	@Override
	protected List<Pair<List<Object>, Double>> getNearest(List<Object> data) {
		List<Object> test_instance = data;
		List<Pair<List<Object>, Double>> NN = new ArrayList<Pair<List<Object>, Double>>();
		double dist_tmp;
		double greatest_dist;
		int greatest_dist_index;
		int k = getkNearest();
		boolean first_run = true;
		v_min = new ArrayList<Double>();
		v_max = new ArrayList<Double>();
		
		// get max and min values of attributes for normalization
		for (List<Object> training_instance : training_data){
			for (int i = 0; i < training_instance.size(); i++){
				Object value = training_instance.get(i);
				if(value instanceof Double && !first_run){
					if ((Double)value < v_min.get(i)) v_min.set(i, (Double)value);
					if ((Double)value > v_max.get(i)) v_max.set(i, (Double)value);
				} else if (value instanceof String && !first_run){
					v_min.add(0.);
					v_max.add(1.);
				} else if (value instanceof Double && first_run){ //first run
					v_min.add((Double)value);
					v_max.add((Double)value);
				} else if (value instanceof String && first_run){
					v_min.add(0.);
					v_max.add(1.);
				}
			}
			first_run = false;
		}
		
		for (List<Object> training_instance : training_data){
			dist_tmp = determineManhattanDistance(training_instance, test_instance);
			
			// only save dist of k nearest
			if(NN.size() >= k){
				greatest_dist = 0;
				greatest_dist_index = 0;
				for(Pair<List<Object>, Double> pair : NN){
					if(pair.getB() > greatest_dist){
						greatest_dist = pair.getB();
						greatest_dist_index = NN.indexOf(pair);
					}
				}
				if(dist_tmp < greatest_dist){
					NN.remove(greatest_dist_index);
					NN.add(new Pair<List<Object>, Double>(training_instance, dist_tmp));
				}
			} else {
				NN.add(new Pair<List<Object>, Double>(training_instance, dist_tmp));
			}
		}
		
		return NN;
	}

	@Override
	protected double determineManhattanDistance(List<Object> instance1, List<Object> instance2) {
		Object a1;
		Object a2;
		double v1;
		double v2;
		double dist = 0;
		for (int i = 0; i < instance1.size(); i++){
			a1 = instance1.get(i);
			a2 = instance2.get(i);
			
			if(a1 instanceof Double){
				v1 = ((Double)a1 - v_min.get(i)) / (v_max.get(i) - v_min.get(i));
				v2 = ((Double)a2 - v_min.get(i)) / (v_max.get(i) - v_min.get(i));	
				dist += Math.abs(v1 - v2);
			} else if (a1 instanceof String) {
				if (a1 != a2) dist += 1.;
			} else {
				try {
					throw new Exception("Instance attribute with index " + 
							i + " is neither number nor string");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return dist;
	}

	@Override
	protected double determineEuclideanDistance(List<Object> instance1, List<Object> instance2) {
		throw new NotImplementedException();
	}

	@Override
	protected double[][] normalizationScaling() {
		throw new NotImplementedException();
	}

}
