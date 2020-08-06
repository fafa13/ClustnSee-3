/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 24 juin 2020
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.gui.menu.contextual.action;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.cytoscape.clustnsee3.internal.CyActivator;
import org.cytoscape.clustnsee3.internal.analysis.CnSCluster;
import org.cytoscape.clustnsee3.internal.analysis.CnSClusterLink;
import org.cytoscape.clustnsee3.internal.analysis.edge.CnSEdge;
import org.cytoscape.clustnsee3.internal.analysis.node.CnSNode;
import org.cytoscape.clustnsee3.internal.event.CnSEvent;
import org.cytoscape.clustnsee3.internal.event.CnSEventManager;
import org.cytoscape.clustnsee3.internal.network.CnSNetwork;
import org.cytoscape.clustnsee3.internal.partition.CnSPartition;
import org.cytoscape.clustnsee3.internal.partition.CnSPartitionManager;
import org.cytoscape.clustnsee3.internal.view.CnSView;
import org.cytoscape.clustnsee3.internal.view.CnSViewManager;
import org.cytoscape.clustnsee3.internal.view.style.CnSStyleManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * 
 */
public class CnSExpandClusterNodeAction {
	public static final String ACTION = "Expand cluster";
	
	public void doAction(Long suid) {
		boolean expanded = false;
		
		CnSEvent ev = new CnSEvent(CnSViewManager.GET_SELECTED_VIEW, CnSEventManager.VIEW_MANAGER);
		CnSView view = (CnSView)CnSEventManager.handleMessage(ev);
		HashMap<CyNode, CyEdge> node2edgeMap = new HashMap<CyNode, CyEdge>();
		
		ev = new CnSEvent(CnSViewManager.GET_NETWORK, CnSEventManager.VIEW_MANAGER);
		ev.addParameter(CnSViewManager.VIEW, view);
		CnSNetwork network = (CnSNetwork)CnSEventManager.handleMessage(ev);
		
		ev = new CnSEvent(CnSPartitionManager.GET_PARTITION, CnSEventManager.PARTITION_MANAGER);
		ev.addParameter(CnSPartitionManager.NETWORK, network);
		CnSPartition partition = (CnSPartition)CnSEventManager.handleMessage(ev);
		if (partition == null) {
			ev = new CnSEvent(CnSViewManager.GET_VIEW_PARTITION, CnSEventManager.VIEW_MANAGER);
			ev.addParameter(CnSViewManager.VIEW, view);
			partition = (CnSPartition)CnSEventManager.handleMessage(ev);
		}
		
		CnSNode node = partition.getClusterNode(suid);
		
		CnSCluster cluster = null, linkedCluster;
		for (CnSCluster c : partition.getClusters()) {
			if (c.getCyNode() == node.getCyNode()) {
				cluster = c;
				break;
			}
		}
		
		if (cluster != null) {
			ev = new CnSEvent(CnSViewManager.GET_VIEW, CnSEventManager.VIEW_MANAGER);
			ev.addParameter(CnSViewManager.REFERENCE, cluster);
			CnSView clusterView = (CnSView)CnSEventManager.handleMessage(ev);
			
			double x0, y0, x, y, x_min = 1000000, y_min = 1000000, x_max = 0, y_max = 0;
			
			x0 = view.getView().getNodeView(cluster.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			y0 = view.getView().getNodeView(cluster.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			
			for (CnSNode cnsnode : cluster.getNodes()) {
				x = clusterView.getView().getNodeView(cnsnode.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				y = clusterView.getView().getNodeView(cnsnode.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				if (x_min > x) x_min = x;
				if (y_min > y) y_min = y;
				if (x_max < x) x_max = x;
				if (y_max < y) y_max = y;
			}
			double x_size = x_max - x_min + 1;
			double y_size = y_max - y_min + 1;
			double ratio = (Math.min(cluster.getNbNodes(), 100) * (50 - 4750 / 99) + 4750 / 99) / Math.max(x_size, y_size);
			
			view.setModifCluster(true);
			
			ev = new CnSEvent(CyActivator.GET_CY_EVENT_HELPER, CnSEventManager.CY_ACTIVATOR);
			CyEventHelper eh = (CyEventHelper)CnSEventManager.handleMessage(ev);
			for (CnSNode cnsnode : cluster.getNodes()) {
				if (!view.getView().getModel().containsNode(cnsnode.getCyNode())) {
					network.getNetwork().addNode(cnsnode.getCyNode());
					network.getNetwork().getRow(cnsnode.getCyNode()).set("CnS:size", null);
					view.getView().updateView();
					View<CyNode> nodeView = view.getView().getNodeView(cnsnode.getCyNode());
					x = (x0 - ratio * (x_max + x_min) / 2) + ratio * clusterView.getView().getNodeView(cnsnode.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					y = (y0 - ratio * (y_max + y_min) / 2) + ratio * clusterView.getView().getNodeView(cnsnode.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
				}
			}
			eh.flushPayloadEvents();
			
			// adding cluster internal edges
			for (CnSEdge cnsedge : cluster.getEdges()) {
				network.getNetwork().addEdge(cnsedge.getCyEdge());
				
			}
			
			eh.flushPayloadEvents();
			network.getNetwork().getRow(cluster.getCyNode()).set("selected", false);
			
			
			ev = new CnSEvent(CnSViewManager.SET_CLUSTER_LOCATION, CnSEventManager.VIEW_MANAGER);
			ev.addParameter(CnSViewManager.VIEW, view);
			ev.addParameter(CnSViewManager.CLUSTER, cluster);
			ev.addParameter(CnSViewManager.CLUSTER_LOCATION, new Point2D.Double(x0, y0));
			CnSEventManager.handleMessage(ev);
			
			Vector<CyNode> toRemove = new Vector<CyNode>();
			toRemove.addElement(node.getCyNode());
			network.getNetwork().removeNodes(toRemove);
			eh.flushPayloadEvents();
			
			for (CnSClusterLink cl : partition.getClusterLinks()) {
				ev = new CnSEvent(CnSViewManager.IS_EXPANDED, CnSEventManager.VIEW_MANAGER);
				ev.addParameter(CnSViewManager.VIEW, view);
				linkedCluster = null;
				if (cl.getSource() == cluster)
					linkedCluster = cl.getTarget();	
				else if (cl.getTarget() == cluster)
					linkedCluster = cl.getSource();
				if (linkedCluster != null && view.getClusters().contains(linkedCluster)) {
					ev.addParameter(CnSViewManager.CLUSTER, linkedCluster);
					expanded = (Boolean)CnSEventManager.handleMessage(ev);
					node2edgeMap.clear();
					HashMap<CyEdge, Double> edgeWidth = new HashMap<CyEdge, Double>();
					CyNode clusterNode = null;
					CyEdge edge = null;
					List<CyEdge> lce;
					
					for (CnSEdge ce : cl.getEdges())
						if (expanded) {
							network.getNetwork().addEdge(ce.getCyEdge());
						}
						else {
							clusterNode = null;
							for (CnSNode n : cluster.getNodes())
								if ((n.getCyNode() == ce.getCyEdge().getSource()) || (n.getCyNode() == ce.getCyEdge().getTarget())) {
									clusterNode = n.getCyNode();
									break;
								}
							if (!network.getNetwork().containsEdge(linkedCluster.getCyNode(), clusterNode) &&
									!network.getNetwork().containsEdge(clusterNode, linkedCluster.getCyNode())) {
								// adding node-cluster interaction link
								edge = network.getNetwork().addEdge(linkedCluster.getCyNode(), clusterNode, false);
								edgeWidth.putIfAbsent(edge, 1.0);
								
								
							}
							else {
								lce = network.getNetwork().getConnectingEdgeList(linkedCluster.getCyNode(), clusterNode, CyEdge.Type.ANY);
								lce.addAll(network.getNetwork().getConnectingEdgeList(clusterNode, linkedCluster.getCyNode(), CyEdge.Type.ANY));
								for (CyEdge e : lce)
									if ((e.getSource() == linkedCluster.getCyNode() && e.getTarget() == clusterNode) ||
											(e.getSource() == clusterNode && e.getTarget() == linkedCluster.getCyNode())) {
										edge = e;
										break;
									}
								if (edgeWidth.get(edge) != null)
									edgeWidth.put(edge, edgeWidth.get(edge) + 1);
								else {
									edgeWidth.putIfAbsent(edge, view.getView().getEdgeView(edge).getVisualProperty(BasicVisualLexicon.EDGE_WIDTH) + 1.0);
								}
							}
						}
					for (CnSNode n : cl.getNodes()) {
						if (!expanded) {
							if (!network.getNetwork().containsEdge(linkedCluster.getCyNode(), n.getCyNode()) && 
									!network.getNetwork().containsEdge(n.getCyNode(), linkedCluster.getCyNode())) {
								// adding node-cluster multiclass link
								CyEdge cyEdge = network.getNetwork().addEdge(linkedCluster.getCyNode(), n.getCyNode(), false);
								
								
								eh.flushPayloadEvents();
								network.getNetwork().getRow(cyEdge).set("CnS:isInteraction", false);
								eh.flushPayloadEvents();
							}
						}
					}
				}
			}
			
			eh.flushPayloadEvents();
			
			ev = new CnSEvent(CnSStyleManager.APPLY_CURRENT_STYLE, CnSEventManager.STYLE_MANAGER);
			CnSEventManager.handleMessage(ev);
			
			ev = new CnSEvent(CnSViewManager.SET_EXPANDED, CnSEventManager.VIEW_MANAGER);
			ev.addParameter(CnSViewManager.CLUSTER, cluster);
			ev.addParameter(CnSViewManager.VIEW, view);
			ev.addParameter(CnSViewManager.EXPANDED, true);
			CnSEventManager.handleMessage(ev);
			
			eh.flushPayloadEvents();
			for (CnSNode cnsnode : cluster.getNodes()) network.getNetwork().getRow(cnsnode.getCyNode()).set("selected", true);
			eh.flushPayloadEvents();
			
			view.setModifCluster(false);
		}
	}
}