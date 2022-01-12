/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 10 déc. 2020
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.nodeannotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.clustnsee3.internal.analysis.CnSCluster;
import org.cytoscape.clustnsee3.internal.analysis.node.CnSNode;
import org.cytoscape.clustnsee3.internal.event.CnSEvent;
import org.cytoscape.clustnsee3.internal.event.CnSEventListener;import org.cytoscape.clustnsee3.internal.event.CnSEventManager;
import org.cytoscape.clustnsee3.internal.gui.partitionpanel.CnSPartitionPanel;
import org.cytoscape.clustnsee3.internal.nodeannotation.trie.CnSAnnotationTrie;
import org.cytoscape.clustnsee3.internal.nodeannotation.trie.CnSTrieNode;
import org.cytoscape.clustnsee3.internal.partition.CnSPartitionManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

/**
 * 
 */
public class CnSNodeAnnotationManager implements CnSEventListener {
	private static CnSNodeAnnotationManager instance = null;
	
	public static final int LOAD_ANNOTATIONS = 1;
	public static final int ADD_ANNOTATION = 2;
	public static final int REMOVE_ANNOTATION = 3;
	public static final int SET_NODE_ANNOTATION = 4;
	public static final int REMOVE_NODE_ANNOTATION = 5;
	public static final int GET_NODES = 6;
	public static final int GET_ANNOTATIONS = 7;
	public static final int PRINT_ANNOTATIONS = 8;
	public static final int PARSE_ANNOTATIONS = 9;
	public static final int LOOK_FOR_ANNOTATIONS = 10;
	public static final int GET_CLUSTER_ANNOTATIONS = 11;
	public static final int CLEAR_CLUSTERS_ANNOTATION = 12;
	public static final int GET_ANNOTATION = 13;
	public static final int GET_ANNOTATED_NODES = 14;
	public static final int GET_ANNOTATED_CLUSTERS = 15;
	public static final int UNLOAD_ANNOTATIONS = 16;
	public static final int ANNOTATE_NETWORK = 17;
	public static final int DEANNOTATE_NETWORK = 18;
	public static final int GET_NETWORK_ANNOTATIONS = 19;
	public static final int IS_NETWORK_ANNOTATED = 20;	
	
	public static final int VALUE = 1001;
	public static final int NODE = 1002;
	public static final int ANNOTATION = 1003;
	public static final int FILE = 1004;
	public static final int NETWORK = 1005;
	public static final int FROM_LINE = 1006;
	public static final int PREFIX = 1007;
	public static final int CLUSTER = 1008;
	public static final int ANNOTATION_FILE = 1009;
	
	private HashMap<CnSNodeAnnotation, CnSNodeNetworkSet> annotations;
	private HashMap<CnSNodeAnnotation, Vector<CnSCluster>> annotation2cluster;
	private HashMap<CyNode, Vector<CnSNodeAnnotation>> cyNodes;
	private HashMap<CnSCluster, Vector<CnSNodeAnnotation>> clusters;
	private Vector<CnSNodeAnnotationFile> files;
	private HashMap<CnSNodeAnnotationFile, Vector<CyNetwork>> annotatedNetworks;
	
	private CnSAnnotationTrie annotationTrie;
	
	private CnSNodeAnnotationManager() {
		super();
		annotations = new HashMap<CnSNodeAnnotation, CnSNodeNetworkSet>();
		cyNodes = new HashMap<CyNode, Vector<CnSNodeAnnotation>>();
		clusters = new HashMap<CnSCluster, Vector<CnSNodeAnnotation>>();
		annotation2cluster = new HashMap<CnSNodeAnnotation, Vector<CnSCluster>>();
		files = new Vector<CnSNodeAnnotationFile>();
		annotationTrie = new CnSAnnotationTrie();
		annotatedNetworks = new HashMap<CnSNodeAnnotationFile, Vector<CyNetwork>>();
	}
	
	public static CnSNodeAnnotationManager getInstance() {
		if (instance == null) {
			instance = new CnSNodeAnnotationManager();
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.cytoscape.clustnsee3.internal.event.CnSEventListener#cnsEventOccured(org.cytoscape.clustnsee3.internal.event.CnSEvent)
	 */
	@Override
	public Object cnsEventOccured(CnSEvent event) {
		Object ret = null;
		String value, s;
		CnSNodeAnnotation annotation;
		File inputFile;
		int fromLine;
		BufferedReader br;
		String[] word, anno;
		Iterator<CyRow> it;
		int found_nodes, mapped_annotations;
		HashSet<String> nodes_in_file;
		CyNetwork network;
		Vector<String> annots;
		CnSCluster cluster;
		HashSet<String> annotations_in_file;
		CnSNodeAnnotationFile af;
		
		switch (event.getAction()) {
			case PARSE_ANNOTATIONS :
				inputFile = (File)event.getParameter(FILE);
				fromLine = (Integer)event.getParameter(FROM_LINE);
				network = (CyNetwork)event.getParameter(NETWORK);
				annots = new Vector<String>();
				try {
					br = new BufferedReader(new FileReader(inputFile));
					for (int i = 1; i < fromLine; i++) br.readLine();
					found_nodes = mapped_annotations = 0;
					nodes_in_file = new HashSet<String>();
					annotations_in_file = new HashSet<String>();
					while ((s = br.readLine()) != null) {
						word = s.split("\t");
						nodes_in_file.add(word[0]);
						anno = word[1].split(";");
						for (String q : anno)
							if (! q.equals("")) 
								annotations_in_file.add(q);
						it = network.getDefaultNodeTable().getMatchingRows("shared name", word[0]).iterator();
						if (it.hasNext()) {
							found_nodes++;
							//node = network.getNode(it.next().get(network.getDefaultNodeTable().getPrimaryKey().getName(), Long.class));
							for (String q : anno)
								if (! q.equals("")) 
									if (!annots.contains(q)) {
										mapped_annotations++;
										annots.addElement(q);
									}
						}
					}
					br.close();
					
					int[] results = new int[6];
					results[0] = nodes_in_file.size();
					results[1] = annotations_in_file.size();
					results[2] = found_nodes;
					results[3] = mapped_annotations;
					results[4] = network.getNodeCount();
					results[5] = annotations_in_file.size();
					
					ret = results;
					System.err.println("Total nodes in file : " + nodes_in_file.size());
					System.err.println("Total annotations in file : " + annotations_in_file.size());
					System.err.println("Total nodes in graph : " + network.getNodeCount());
					System.err.println("Found nodes in graph : " + found_nodes);
					System.err.println("Mapped annotations in graph : " + mapped_annotations);
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				break;
			
			case LOAD_ANNOTATIONS :
				inputFile = (File)event.getParameter(FILE);
				fromLine = (Integer)event.getParameter(FROM_LINE);
				CnSNodeAnnotationFile aif = new CnSNodeAnnotationFile(inputFile, fromLine);
				
				files.addElement(aif);
				
				System.err.println("Importing annotations from " + aif.getFile().getName());
				
				try {
					br = new BufferedReader(new FileReader(aif.getFile()));
					for (int i = 1; i < fromLine; i++) br.readLine();
					while ((s = br.readLine()) != null) {
						word = s.split("\t");
						anno = word[1].split(";");
						for (String q : anno)
							if (! q.equals("")) {
								CnSTrieNode w = annotationTrie.addWord(q);
								aif.addElement(w, word[0]);
							}
					}
					br.close();
					addAnnotations(aif);
					makeCyNodesHashMap();
					makeClustersHashMap();
					ret = aif;
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
			
			case ADD_ANNOTATION :
				value = (String)event.getParameter(VALUE);
				//node = (CyNode)event.getParameter(NODE);
				inputFile = (File)event.getParameter(FILE);
				//CnSTrieNode w = annotationTrie.addWord(value);
				//addAnnotation(w, node, inputFile);0102391
				break;
				
			case REMOVE_ANNOTATION :
				annotation = (CnSNodeAnnotation)event.getParameter(ANNOTATION);
				//annotations.remove(annotation);af
				break;
				
			case REMOVE_NODE_ANNOTATION :
				//node = (CyNode)event.getParameter(NODE);
				value = (String)event.getParameter(VALUE);
				//w = annotationTrie.get(data = v;value);
				//annotation = new CnSNodeAnnotation(w, null);
				//nodes = annotations.get(annotation);
				//if (nodes != null) nodes.removeElement(node);
				break;
				
			case GET_NODES :
				annotation = (CnSNodeAnnotation)event.getParameter(ANNOTATION);
				ret = annotations.get(annotation);
				if (ret == null) ret = new CnSNodeNetworkSet();
				break;
				
			case GET_ANNOTATIONS :
				Vector<CnSNodeAnnotation> v = new Vector<CnSNodeAnnotation>(annotations.keySet());
				v.sort(new Comparator<CnSNodeAnnotation>() {
					@Override
					public int compare(CnSNodeAnnotation a1, CnSNodeAnnotation a2) {
						return a1.getValue().compareTo(a2.getValue());
					}
				});
				ret = v;
				break;
				
			case PRINT_ANNOTATIONS :
				//printAnnotations();
				break;
				
			case LOOK_FOR_ANNOTATIONS :
				ret = annotationTrie.getAnnotations((String)event.getParameter(PREFIX));
				break;
				
			case GET_CLUSTER_ANNOTATIONS :
				cluster = (CnSCluster)event.getParameter(CLUSTER);
				if (clusters.get(cluster) == null) {
					Vector<CnSNodeAnnotation> annot = new Vector<CnSNodeAnnotation>();
					for (CnSNode cnsNode : cluster.getNodes()) {
						Vector<CnSNodeAnnotation> a = cyNodes.get(cnsNode.getCyNode());
						if (a != null)
							for (CnSNodeAnnotation a2 : a)
								if (!annot.contains(a2)) {
									annot.addElement(a2);
									if (annotation2cluster.get(a2) == null) {
										Vector<CnSCluster> vcl = new Vector<CnSCluster>();
										vcl.addElement(cluster);
										annotation2cluster.put(a2, vcl);
									}
									else if (! annotation2cluster.get(a2).contains(cluster))
										annotation2cluster.get(a2).addElement(cluster);
								}
					}
					clusters.put(cluster, annot);
				}
				ret = clusters.get(cluster);
				break;
				
			case CLEAR_CLUSTERS_ANNOTATION :
				clusters.clear();
				break;
				
			case GET_ANNOTATION :
				value = (String)event.getParameter(ANNOTATION);
				CnSTrieNode tn = annotationTrie.get(value);
				if (tn != null) 
					ret = tn.getAnnotation();
				else
					ret = null;
				break;
				
			case GET_ANNOTATED_NODES :
				ret = new Vector<CyNode>(cyNodes.keySet());
				break;
				
			case GET_ANNOTATED_CLUSTERS :
				annotation = (CnSNodeAnnotation)event.getParameter(ANNOTATION);
				if (annotation != null) ret = annotation2cluster.get(annotation);
				break;
				
			case UNLOAD_ANNOTATIONS :
				af = (CnSNodeAnnotationFile)event.getParameter(ANNOTATION_FILE);
				
				if (af != null) {
					removeAnnotations(af);
					makeCyNodesHashMap();
					makeClustersHashMap();
					files.removeElement(af);
					CnSEvent ev = new CnSEvent(CnSPartitionPanel.REFRESH, CnSEventManager.PARTITION_PANEL);
					CnSEventManager.handleMessage(ev);
				}
				break;
				
			case ANNOTATE_NETWORK :
				af = (CnSNodeAnnotationFile)event.getParameter(ANNOTATION_FILE);
				network = (CyNetwork)event.getParameter(NETWORK);
				System.err.println("ANNOTATE_NETWORK " + network);
				List<String> netNodes = network.getDefaultNodeTable().getColumn("shared name").getValues(String.class);
				
				Set<CyNode> nodes;
				CyNode cn;
				for (CnSNodeAnnotation na : annotations.keySet()) {
					for (CnSAnnotationTarget t : na.getTargets()) {
						if (t.getFiles().contains(af))
							if (netNodes.contains(t.getTarget())) {
								nodes = getNodesWithValue(network, network.getDefaultNodeTable(), "shared name", t.getTarget());
								cn = nodes.iterator().next();
								CnSNodeNetwork nn = new CnSNodeNetwork(network, cn, af); 
								if (!annotations.get(na).getNodeNetworks().contains(nn)) annotations.get(na).getNodeNetworks().addElement(nn);
							}
					}
				}
				makeCyNodesHashMap();
				makeClustersHashMap();
				if (annotatedNetworks.get(af) == null) annotatedNetworks.put(af, new Vector<CyNetwork>());
				annotatedNetworks.get(af).addElement(network);
				CnSEvent ev = new CnSEvent(CnSPartitionPanel.REFRESH, CnSEventManager.PARTITION_PANEL);
				CnSEventManager.handleMessage(ev);
				break;
				
			case IS_NETWORK_ANNOTATED :
				af = (CnSNodeAnnotationFile)event.getParameter(ANNOTATION_FILE);
				network = (CyNetwork)event.getParameter(NETWORK);
				ret = Boolean.valueOf(annotatedNetworks.containsKey(af));
				if ((Boolean)ret) ret = (Boolean)ret & Boolean.valueOf(annotatedNetworks.get(af).contains(network));
				break;
				
			case DEANNOTATE_NETWORK :
				System.err.println("DEANNOTATE_NETWORK");
				af = (CnSNodeAnnotationFile)event.getParameter(ANNOTATION_FILE);
				network = (CyNetwork)event.getParameter(NETWORK);
				List<String> netNodes2 = network.getDefaultNodeTable().getColumn("shared name").getValues(String.class);
				
				for (CnSNodeAnnotation na : annotations.keySet()) {
					for (CnSAnnotationTarget t : na.getTargets()) {
						if (t.getFiles().contains(af)) {
							if (netNodes2.contains(t.getTarget())) {
								nodes = getNodesWithValue(network, network.getDefaultNodeTable(), "shared name", t.getTarget());
								cn = nodes.iterator().next();
								CnSNodeNetwork nnet = new CnSNodeNetwork(network, cn, af);
								annotations.get(na).getNodeNetworks().removeElement(nnet);
							}
						}
					}
				}
				makeCyNodesHashMap();
				makeClustersHashMap();
				annotatedNetworks.get(af).removeElement(network);
				ev = new CnSEvent(CnSPartitionPanel.REFRESH, CnSEventManager.PARTITION_PANEL);
				CnSEventManager.handleMessage(ev);
				break;
				
			case GET_NETWORK_ANNOTATIONS :
				network = (CyNetwork)event.getParameter(NETWORK);
				//for (CnSNodeNetworkSet nns : annotations.values()) {
					//nns.getNodeNetworks().
				//}
				break;
		}
		return ret;
	}
	/**
     * Get all the nodes with a given attribute value.
     *
     * This method is effectively a wrapper around {@link CyTable#getMatchingRows}.
     * It converts the table's primary keys (assuming they are node SUIDs) back to
     * nodes in the network.
     *
     * Here is an example of using this method to find all nodes with a given name:
     *
     * {@code
     *   CyNetwork net = ...;CnSEvent ev = new CnSEvent(CnSNodeAnnotationManager.DEANNOTATE_NETWORK, CnSEventManager.ANNOTATION_MANAGER);
				ev.addParameter(CnSNodeAnnotationManager.ANNOTATION_FILE, getData(ANNOTATION_FILE));
				ev.addParameter(CnSNodeAnnotationManager.NETWORK, getData(NETWORK));
				CnSEventManager.handleMessage(ev);
				
				ev = new CnSEvent(CnSControlPanel.REMOVE_MAPPED_NETWORK, CnSEventManager.CONTROL_PANEL);
				ev.addParameter(CnSControlPanel.TREE_FILE_NODE, this);
				CnSEventManager.handleMessage(ev)
     *   String nodeNameToSearchFor = ...;
     *   Set<CyNode> nodes = getNodesWithValue(net, net.getDefaultNodeTable(), "name", nodeNameToSearchFor);
     *   // nodes now contains all CyNodes with the name specified by nodeNameToSearchFor
     * }
     * @param net The network that contains the nodes you are looking for.
     * @param table The node table that has the attribute value you are looking for;
     * the primary keys of this table <i>must</i> be SUIDs of nodes in {@code net}.
     * @param colname The name of the column with the attribute value
     * @param value The attribute value
     * @return A set of {@code CyNode}s with a matching value, or an empty set if no nodes match.
     */
    private static Set<CyNode> getNodesWithValue(final CyNetwork net, final CyTable table, final String colname, final Object value) {
        final Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
        final Set<CyNode> nodes = new HashSet<CyNode>();
        final String primaryKeyColname = table.getPrimaryKey().getName();
        
        for (final CyRow row : matchingRows) {
            final Long nodeId = row.get(primaryKeyColname, Long.class);
            if (nodeId == null) continue;
            final CyNode node = net.getNode(nodeId);
            if (node == null) continue;
            nodes.add(node);
        }
        return nodes;
    }
	
	private void addAnnotations(CnSNodeAnnotationFile aif) {
		Iterator<CnSTrieNode> it = aif.getAllAnnotations().iterator();
		CnSTrieNode ann;
		CnSNodeAnnotation nodeAnnotation;
		Vector<CnSNodeNetwork> nodeNetwork;
		while (it.hasNext()) {
			ann = it.next();
			if (ann.getAnnotation() == null) {
				nodeAnnotation = new CnSNodeAnnotation(ann);
				ann.setAnnotation(nodeAnnotation);
				nodeNetwork = new Vector<CnSNodeNetwork>();
			}
			else {
				nodeAnnotation = ann.getAnnotation();
				nodeNetwork = annotations.get(nodeAnnotation).getNodeNetworks();
			}
			for (String tar : aif.getTargets(ann)) nodeAnnotation.addTarget(tar, aif);
			
			annotations.put(nodeAnnotation, new CnSNodeNetworkSet(nodeNetwork));
		}
	}
	
	private void removeAnnotations(CnSNodeAnnotationFile aif) {
		Iterator<CnSTrieNode> it = aif.getAllAnnotations().iterator();
		CnSTrieNode ann;
		CnSNodeAnnotation nodeAnnotation;
		while (it.hasNext()) {
			ann = it.next();
			nodeAnnotation = ann.getAnnotation();
			annotations.remove(nodeAnnotation);
			ann.removeAnnotation();
		}
	}

	private void makeCyNodesHashMap() {
		cyNodes.clear();
		for (CnSNodeAnnotation annotation : annotations.keySet()) {
			Vector<CnSNodeNetwork> nodes = annotations.get(annotation).getNodeNetworks();
			for (CnSNodeNetwork node : nodes) {
				Vector<CnSNodeAnnotation> a = cyNodes.get(node.getNode());
				if (a == null) {
					Vector<CnSNodeAnnotation> b = new Vector<CnSNodeAnnotation>();
					b.addElement(annotation);
					cyNodes.put(node.getNode(), b);
				}
				else if (!a.contains(annotation))
					a.addElement(annotation);
			}
		}
	}
	
	private void makeClustersHashMap() {
		CnSEvent ev = new CnSEvent(CnSPartitionManager.GET_ALL_CLUSTERS, CnSEventManager.PARTITION_MANAGER);
		Vector<CnSCluster> all_clusters = (Vector<CnSCluster>)CnSEventManager.handleMessage(ev);
		clusters.clear();
		for (CnSCluster cl : all_clusters)
			if (clusters.get(cl) == null) {
				Vector<CnSNodeAnnotation> annot = new Vector<CnSNodeAnnotation>();
				for (CnSNode cnsNode : cl.getNodes()) {
					Vector<CnSNodeAnnotation> a = cyNodes.get(cnsNode.getCyNode());
					if (a != null)
						for (CnSNodeAnnotation a2 : a)
							if (!annot.contains(a2)) {
								annot.addElement(a2);
								if (annotation2cluster.get(a2) == null) {
									Vector<CnSCluster> vcl = new Vector<CnSCluster>();
									vcl.addElement(cl);
									annotation2cluster.put(a2, vcl);
								}
								else if (! annotation2cluster.get(a2).contains(cl))
									annotation2cluster.get(a2).addElement(cl);
							}
					clusters.put(cl, annot);
				}
				System.err.println("--- " + cl.getName() + " -> " + annot.size());
			}
	}
	public void printAnnotations() {
		int N = 0;
		for (CnSNodeAnnotation key : annotations.keySet()) {
			System.out.println(++N + " " + key.getValue());
			for (CnSNodeNetwork value : annotations.get(key).getNodeNetworks()) {
				System.out.println("  " + value.getNode().getSUID());
			}
		}
	}
}
