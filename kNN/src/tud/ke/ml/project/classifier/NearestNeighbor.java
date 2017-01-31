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
		int class_index = getClassAttribute();
		for (Pair<List<Object>, Double> pair : subset){
			Object y = pair.getA().get(class_index);
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
		Map<Object, Double> votes = new TreeMap<Object, Double>();
		double vote_count;
		int class_index = getClassAttribute();
		double weight; // weighting for individual nearest neighbors
		double weight_sum = 0; // weighted sums will be devided by sum of all weights
		Map<Object, Double> weight_sums = new TreeMap<Object, Double>();
		double factor;
		for (Pair<List<Object>, Double> pair : subset){
			Object y = pair.getA().get(class_index);
			factor = pair.getB()*pair.getB();
			if (factor != 0)
				weight = 1 / factor;
			else // distance 0 means, that full weight should be applied
				weight = 1;
			vote_count = weight;
			weight_sum = weight;
			if (votes.containsKey(y)){
				vote_count += votes.get(y);
				weight_sum += weight_sums.get(y);
			}
			votes.put(y, vote_count);
			weight_sums.put(y, weight_sum);
		}
		
		// devide each vote by sum of weights - maybe not needed?
		/*for(Map.Entry<Object, Double> entry : votes.entrySet()){
			vote_count = entry.getValue() / weight_sums.get(entry.getKey());
			votes.put(entry.getKey(), vote_count);
		}*/
		
		return votes;
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
		Map<Object, Double> votes;
		if (!isInverseWeighting())
			votes = getWeightedVotes(subset);
		else
			votes = getUnweightedVotes(subset);
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
		
		double norm[][] = normalizationScaling();
		translation = norm[0];
		scaling = norm[1];
		
		for (List<Object> training_instance : training_data){
			if(getMetric() == 1)
				dist_tmp = determineEuclideanDistance(training_instance, test_instance);
			else
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
		double dist = 0; // distance between two instances
		for (int i = 0; i < instance1.size(); i++){
			if (i != getClassAttribute()){
				a1 = instance1.get(i); // value of attribue of first instance
				a2 = instance2.get(i); // value of attribue of second instance

				if(a1 instanceof Double){
					// distance between numerical attributes
					if(isNormalizing()){
						v1 = ((Double)a1 - translation[i]) * scaling[i];
						v2 = ((Double)a2 - translation[i]) * scaling[i];
					} else {
						v1 = (Double)a1;
						v2 = (Double)a2;
					}
					dist += Math.abs(v1 - v2);
				} else if (a1 instanceof String) {
					// distance between nominal attributes
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
		}

		return dist;
	}

	@Override
	protected double determineEuclideanDistance(List<Object> instance1, List<Object> instance2) {
		Object a1;
		Object a2;
		double v1;
		double v2;
		double dist = 0; // distance between two instances
		for (int i = 0; i < instance1.size(); i++){
			if (i != getClassAttribute()){
				a1 = instance1.get(i); // value of attribute of first instance
				a2 = instance2.get(i); // value of attribute of second instance
				
				if(a1 instanceof Double){
					// distance between numerical attributes
					if(isNormalizing()){
						v1 = ((Double)a1 - translation[i]) * scaling[i];
						v2 = ((Double)a2 - translation[i]) * scaling[i];	
					} else {
						v1 = (Double)a1;
						v2 = (Double)a2;
					}
					dist += Math.abs(v1 - v2) * Math.abs(v1 - v2);
				} else if (a1 instanceof String) {
					// distance between nominal attributes
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
		}

		// return euclidean distance
		return Math.pow(dist, 0.5);
	}

	@Override
	protected double[][] normalizationScaling() {
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
		
		int size = v_min.size(); // number of attributes
		double[][] norm = new double[2][size]; // 1st dim: translation, 2nd dim: scaling
		double factor;
		
		// extract translation and scaling values
		for (int i = 0; i < v_min.size(); i++){
			norm[0][i] = v_min.get(i); // translation values
			factor = (v_max.get(i) - v_min.get(i));
			if (factor != 0)
				norm[1][i] = 1 / factor; // scaling values
			else
				norm[1][i] = 0; // this should not happen -> max and min values are same
		}
		
		return norm;
	}

}
