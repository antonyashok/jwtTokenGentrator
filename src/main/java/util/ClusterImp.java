package util;

import java.util.ArrayList;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constant.ClusterAttribute;
import constant.ClusterType;
import model.ClusterStudent;
import storage.DataManager;
import weka.clusterers.Canopy;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class ClusterImp {


	public static Map<Integer, List<Integer>> clusterChooserForList(String algorithm, int clusterNum, List<String> choosenAttr) throws Exception{
		Map<Integer, List<Integer>> clusterResult = new HashMap<>();
		ArrayList<Attribute> attributes = new ArrayList<>();
		List<ClusterStudent> clusterData = DataManager.getDataInstance().getClusterData();


		for(int i = 0; i< ClusterAttribute.attributes().size(); i++) {
			attributes.add(new Attribute(ClusterAttribute.attributes().get(i)));
		}

		// build Instance from clusterData
		Instances data = new Instances("dataSet", attributes, 10000);
		for(ClusterStudent student: clusterData) {
			Instance instance = new DenseInstance(ClusterAttribute.attributes().size());
			instance.setValue(attributes.get(0), student.userId);
			instance.setValue(attributes.get(1), student.getExpectedMovement());
			instance.setValue(attributes.get(2), student.getBackwardMovement());
			instance.setValue(attributes.get(3), student.getForwardMovement());
			instance.setValue(attributes.get(4), student.getVisited());
			instance.setValue(attributes.get(5), student.getRevisited());
			instance.setValue(attributes.get(6), student.getViewByMovement());
			instance.setValue(attributes.get(7), student.getPostByStatement());
			instance.setValue(attributes.get(8), student.getStatementByView());
			data.add(instance);
		}

		int ignoreAttrSize = ClusterAttribute.attributes().size() - (choosenAttr.size() +1);
		int[] ignoreAttrs = new int[ignoreAttrSize+1];
		ignoreAttrs[0] = 0; // since the first column is id, we have to remove it automatically

		int n = 1;
		for(int i = 1; i< ClusterAttribute.attributes().size(); i++) {
			String attr = ClusterAttribute.attributes().get(i);
			if(!choosenAttr.contains(attr)) {
				ignoreAttrs[n] = i;
				n++;
			}
		} // ingoreAttrs is the list of all the ignored attributes index.

		
		//prepare filter data
		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(ignoreAttrs);
		removeFilter.setInvertSelection(false);
		removeFilter.setInputFormat(data);
		Instances filterData = Filter.useFilter(data, removeFilter);

		if(algorithm.equals(ClusterType.KMEAN)) {
			clusterResult = ClusterImp.kMean(data, filterData, clusterNum);
		}
		if(algorithm.equals(ClusterType.EM)) {
			clusterResult = ClusterImp.em(data, filterData, clusterNum);
		}
		if(algorithm.equals(ClusterType.CANOPY)) {
			clusterResult = ClusterImp.canopy(data, filterData, clusterNum);
		}
		
		return clusterResult;

	}
	
	
	
	public static Map<Integer, List<Integer>> kMean(Instances data, Instances filterData, int clusterNum) throws Exception {
		Map<Integer, List<Integer>> clusterResult = new HashMap<>();


		SimpleKMeans k = new SimpleKMeans();
		k.setNumClusters(clusterNum);
		k.buildClusterer(filterData);

		for(int i = 0; i< filterData.numInstances(); i++) {
			int cluster = k.clusterInstance(filterData.instance(i));
			int id = new Integer(data.instance(i).toString().split(",")[0]);

			if(clusterResult.get(cluster) == null) {
				List<Integer> idList = new ArrayList<Integer>();
				idList.add(id);
				clusterResult.put(cluster, idList);
			}else {
				List<Integer> idList = clusterResult.get(cluster);
				idList.add(id);
			}
		}
		return clusterResult;
	}

	
	public static Map<Integer, List<Integer>> em(Instances data, Instances filterData, int clusterNum) throws Exception{
		Map<Integer, List<Integer>> clusterResult = new HashMap<>();

		EM e = new EM();
		e.setNumClusters(clusterNum);
		e.buildClusterer(filterData);

		for(int i = 0; i< filterData.numInstances(); i++) {
			int cluster = e.clusterInstance(filterData.instance(i));
			int id = new Integer(data.instance(i).toString().split(",")[0]);

			if(clusterResult.get(cluster) == null) {
				List<Integer> idList = new ArrayList<Integer>();
				idList.add(id);
				clusterResult.put(cluster, idList);
			}else {
				List<Integer> idList = clusterResult.get(cluster);
				idList.add(id);
			}
		}
		return clusterResult;
	}
	
	
	public static Map<Integer, List<Integer>> canopy(Instances data, Instances filterData, int clusterNum) throws Exception{
		Map<Integer, List<Integer>> clusterResult = new HashMap<>();
		
		Canopy canopy = new Canopy();
		canopy.setNumClusters(clusterNum);
		canopy.buildClusterer(filterData);
		for(int i = 0; i< filterData.numInstances(); i++) {
			int cluster = canopy.clusterInstance(filterData.instance(i));
			int id = new Integer(data.instance(i).toString().split(",")[0]);

			if(clusterResult.get(cluster) == null) {
				List<Integer> idList = new ArrayList<Integer>();
				idList.add(id);
				clusterResult.put(cluster, idList);
			}else {
				List<Integer> idList = clusterResult.get(cluster);
				idList.add(id);
			}
		}
		return clusterResult;
	}
	 
}
