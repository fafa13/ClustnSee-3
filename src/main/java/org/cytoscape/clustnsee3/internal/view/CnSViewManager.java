/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 2 juin 2020
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.view;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.clustnsee3.internal.CyActivator;
import org.cytoscape.clustnsee3.internal.analysis.CnSCluster;
import org.cytoscape.clustnsee3.internal.analysis.CnSClusterLink;
import org.cytoscape.clustnsee3.internal.analysis.edge.CnSEdge;
import org.cytoscape.clustnsee3.internal.analysis.node.CnSNode;
import org.cytoscape.clustnsee3.internal.event.CnSEvent;
import org.cytoscape.clustnsee3.internal.event.CnSEventListener;
import org.cytoscape.clustnsee3.internal.event.CnSEventManager;
import org.cytoscape.clustnsee3.internal.gui.info.CnSInfoPanel;
import org.cytoscape.clustnsee3.internal.gui.results.CnSResultsPanel;
import org.cytoscape.clustnsee3.internal.network.CnSNetwork;
import org.cytoscape.clustnsee3.internal.network.CnSNetworkManager;
import org.cytoscape.clustnsee3.internal.partition.CnSPartition;
import org.cytoscape.clustnsee3.internal.partition.CnSPartitionManager;
import org.cytoscape.clustnsee3.internal.view.state.CnSUserViewState;
import org.cytoscape.clustnsee3.internal.view.state.CnSViewState;
import org.cytoscape.event.AbstractCyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.UnsetNetworkPointerEvent;
import org.cytoscape.model.events.UnsetNetworkPointerListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * 
 */
public class CnSViewManager implements CnSEventListener, AboutToRemoveNodesListener, AddedNodesListener, 
RemovedEdgesListener, AddedEdgesListener, NetworkViewAboutToBeDestroyedListener, 
UnsetNetworkPointerListener, SetSelectedNetworkViewsListener, RowsSetListener, SetCurrentNetworkViewListener {
	public static final int ADD_VIEW = 1;
	public static final int DELETE_VIEW = 2;
	public static final int SET_SELECTED_VIEW = 3;
	public static final int GET_SELECTED_VIEW = 4;
	public static final int SET_STATE = 5;
	public static final int GET_VIEW = 6;
	public static final int SELECT_CLUSTER = 7;
	public static final int GET_NETWORK = 8;
	public static final int IS_EXPANDED = 9;
	public static final int SET_EXPANDED = 10;
	public static final int GET_CLUSTER_LOCATION = 11;
	public static final int SET_CLUSTER_LOCATION = 12;
	public static final int RECORD_CLUSTERS_LOCATION = 13;
	public static final int SET_VIEW_PARTITION = 14;
	public static final int GET_VIEW_PARTITION = 15;
	public static final int EXPAND_CLUSTER = 16;
	public static final int GET_CLUSTER_FROM_CY_NODE = 17;
	
	public static final int VIEW = 1000;
	public static final int STATE = 1001;
	public static final int REFERENCE = 1002;
	public static final int CLUSTER = 1003;
	public static final int NETWORK = 1004;
	public static final int EXPANDED = 1005;
	public static final int CLUSTER_LOCATION = 1006;
	public static final int PARTITION = 1007;
	public static final int SUID = 1008;
	public static final int CY_NODE = 1009;
	
	private Vector<CnSView> views;
	private CnSView selectedView;
	private HashMap<CnSView, CnSNetwork> view2networkMap;
	private HashMap<CnSNetwork, CnSView> network2viewMap;
	private HashMap<CnSView, CnSCluster> view2clusterMap;
	private HashMap<CnSCluster, CnSView> cluster2viewMap;
	private HashMap<CnSView, CnSPartition> view2partitionMap;
	private HashMap<CnSPartition, CnSView> partition2viewMap;
	
	private static CnSViewManager instance = null;
	
	private CnSViewManager() {
		super();
		views = new Vector<CnSView>();
		selectedView = null;
		view2networkMap = new HashMap<CnSView, CnSNetwork>();
		network2viewMap = new HashMap<CnSNetwork, CnSView>();
		view2clusterMap = new HashMap<CnSView, CnSCluster>();
		cluster2viewMap = new HashMap<CnSCluster, CnSView>();
		view2partitionMap = new HashMap<CnSView, CnSPartition>();
		partition2viewMap = new HashMap<CnSPartition, CnSView>();
	}
	
	public static CnSViewManager getInstance() {
		if (instance == null) {
			instance = new CnSViewManager();
		}
		return instance;
	}
	
	private CnSView getView(CyNetworkView v) {
		CnSView ret = null;
		
		for (CnSView cnsv : views)
			if (cnsv.getView() == v) {
				ret = cnsv;
				break;
			}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.cytoscape.clustnsee3.internal.event.CnSEventListener#cnsEventOccured(org.cytoscape.clustnsee3.internal.event.CnSEvent)
	 */
	@Override
	public Object cnsEventOccured(CnSEvent event) {
		Object ret = null;
		CnSView view;
		CnSNetwork network;
		CnSViewState state = null;
		CnSCluster cluster;
		boolean expanded;
		Double x, y;
		Point2D.Double location;
		CnSEvent ev;
		CnSPartition partition;
		View<CyNode> cnv;
		CyNode cyNode;
		
		switch(event.getAction()) {
			case ADD_VIEW :
				view = (CnSView)event.getParameter(VIEW);
				network = (CnSNetwork)event.getParameter(NETWORK);
				cluster = (CnSCluster)event.getParameter(CLUSTER);
				if (!views.contains(view)) {
					views.addElement(view);
					network2viewMap.putIfAbsent(network, view);
					view2networkMap.putIfAbsent(view, network);
					if (cluster != null) {
						view2clusterMap.putIfAbsent(view, cluster);
						cluster2viewMap.putIfAbsent(cluster, view);
					}
				}
				break;
				
			case DELETE_VIEW :
				view = (CnSView)event.getParameter(VIEW);
				views.removeElement(view);
				for (CnSView v : network2viewMap.values())
					if (view == v) {
						network2viewMap.remove(view2networkMap.get(v));
						break;
					}
				view2networkMap.remove(view);
				for (CnSView v : cluster2viewMap.values())
					if (view == v) {
						cluster2viewMap.remove(view2clusterMap.get(v));
						break;
					}
				view2clusterMap.remove(view);
				break;
				
			case SET_SELECTED_VIEW :
				selectedView = (CnSView)event.getParameter(VIEW);
				break;
				
			case GET_SELECTED_VIEW :
				ret = selectedView;
				break;
			
			case SET_STATE :
				view = (CnSView)event.getParameter(VIEW);
				state = (CnSViewState)event.getParameter(STATE);
				view.setViewState(state);
				break;
				
			case GET_VIEW :
				ret = getView(event.getParameter(REFERENCE), (CnSNetwork)event.getParameter(NETWORK), (CnSCluster)event.getParameter(CLUSTER));
				break;
					
			case SELECT_CLUSTER :
				cluster = (CnSCluster)event.getParameter(CLUSTER);
				for (CnSView v : views)
					if (v != null)
						if (v.getView().getNodeView(cluster.getCyNode()) != null) {
							Collection<CyRow> matchingRows = view2networkMap.get(v).getNetwork().getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getMatchingRows("selected", true);
							if (matchingRows.size() > 0) {
								for (CyRow row : matchingRows) row.set("selected", false);
								view2networkMap.get(v).getNetwork().getRow(cluster.getCyNode()).set("selected", true);
							}
						}
				break;
				
			case GET_NETWORK :
				view = (CnSView)event.getParameter(VIEW);
				ret = view2networkMap.get(view);
				break;
				
			case SET_EXPANDED :
				cluster = (CnSCluster)event.getParameter(CLUSTER);
				view = (CnSView)event.getParameter(VIEW);
				expanded = (Boolean)event.getParameter(EXPANDED);
				view.setExpanded(cluster, expanded);
				break;
				
			case IS_EXPANDED :
				cluster = (CnSCluster)event.getParameter(CLUSTER);
				view = (CnSView)event.getParameter(VIEW);
				ret = view.isExpanded(cluster);
				break;
				
			case GET_CLUSTER_LOCATION :
				view = (CnSView)event.getParameter(VIEW);
				cluster = (CnSCluster)event.getParameter(CLUSTER);
				ret = view.getClusterLocation(cluster);
				break;
				
			case SET_CLUSTER_LOCATION :
				view = (CnSView)event.getParameter(VIEW);
				cluster = (CnSCluster)event.getParameter(CLUSTER);
				location = (Point2D.Double)event.getParameter(CLUSTER_LOCATION);
				view.setLocation(cluster, location.x, location.y);
				break;
				
			case RECORD_CLUSTERS_LOCATION :
				view = (CnSView)event.getParameter(VIEW);
				ev = new CnSEvent(CnSPartitionManager.GET_PARTITION, CnSEventManager.PARTITION_MANAGER);
				ev.addParameter(CnSPartitionManager.VIEW, view);
				partition = (CnSPartition)CnSEventManager.handleMessage(ev);
				if (partition == null) {
					ev = new CnSEvent(CnSViewManager.GET_VIEW_PARTITION, CnSEventManager.VIEW_MANAGER);
					ev.addParameter(CnSViewManager.VIEW, view);
					partition = (CnSPartition)CnSEventManager.handleMessage(ev);
				}
				for (CnSCluster cl : partition.getClusters()) {
					if (cl.getCyNode() != null) { 
						cnv = view.getView().getNodeView(cl.getCyNode());
						if (cnv != null) {
							x = cnv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
							y = cnv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
							view.setLocation(cl, x, y);
						}
					}
				}
				break;
				
			case SET_VIEW_PARTITION :
				view = (CnSView)event.getParameter(VIEW);
				partition = (CnSPartition)event.getParameter(PARTITION);
				view2partitionMap.putIfAbsent(view, partition);
				partition2viewMap.putIfAbsent(partition, view);
				break;
				
			case GET_VIEW_PARTITION :
				view = (CnSView)event.getParameter(VIEW);
				ret = view2partitionMap.get(view);
				break;
				
			case EXPAND_CLUSTER :
				Long suid = (Long)event.getParameter(SUID);
				expandCluster(suid);
				break;
				
			case GET_CLUSTER_FROM_CY_NODE:
				cyNode = (CyNode)event.getParameter(CY_NODE);
				if (selectedView.getView().getNodeView(cyNode) != null) {
					for (CnSCluster cnsCluster : selectedView.getClusters()) {
						if (cnsCluster.getCyNode() == cyNode) {
							ret = cnsCluster;
							break;
						}
					}
				}
				break;
		}
		return ret;
	}
	
	private void eventOccured(AbstractCyEvent<CyNetwork> e) {
		CnSEvent ev = new CnSEvent(CyActivator.GET_NETWORK_VIEW_MANAGER, CnSEventManager.CY_ACTIVATOR);
        CyNetworkViewManager networkViewManager = (CyNetworkViewManager)CnSEventManager.handleMessage(ev);
        Collection<CyNetworkView> views = networkViewManager.getNetworkViews(e.getSource());
        CnSView cnsv = null;
        for (CyNetworkView v : views) {
        	cnsv = getView(v);
        	if (cnsv != null) break;
        }
        if (cnsv != null) {
        	
        	if (!cnsv.isUserView() && !cnsv.getModifCluster()) {
        		cnsv.setViewState(new CnSUserViewState());
        		ev = new CnSEvent(CnSNetworkManager.RENAME_NETWORK, CnSEventManager.NETWORK_MANAGER);
        		ev.addParameter(CnSNetworkManager.NETWORK, view2networkMap.get(cnsv));
        		ev.addParameter(CnSNetworkManager.NETWORK_NAME, "Copy of " + view2networkMap.get(cnsv).getName());
        		CnSEventManager.handleMessage(ev);
        		
        	}	
        }
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.model.events.AddedEdgesListener#handleEvent(org.cytoscape.model.events.AddedEdgesEvent)
	 */
	@Override
	public void handleEvent(AddedEdgesEvent e) {
		eventOccured(e);
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.model.events.RemovedEdgesListener#handleEvent(org.cytoscape.model.events.RemovedEdgesEvent)
	 */
	@Override
	public void handleEvent(RemovedEdgesEvent e) {
		eventOccured(e);
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.model.events.AddedNodesListener#handleEvent(org.cytoscape.model.events.AddedNodesEvent)
	 */
	@Override
	public void handleEvent(AddedNodesEvent e) {
		eventOccured(e);
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.model.events.NetworkAboutToBeDestroyedListener#handleEvent(org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent)
	 */
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		CnSEvent ev= new CnSEvent(CnSViewManager.DELETE_VIEW, CnSEventManager.VIEW_MANAGER);
		ev.addParameter(VIEW, getView(e.getNetworkView()));
		cnsEventOccured(ev);
    }

	/* (non-Javadoc)
	 * @see org.cytoscape.model.events.UnsetNetworkPointerListener#handleEvent(org.cytoscape.model.events.SetNetworkPointerEvent)
	 */
	@Override
	public void handleEvent(UnsetNetworkPointerEvent e) {
		CnSEvent ev = new CnSEvent(CnSNetworkManager.RENAME_NETWORK, CnSEventManager.NETWORK_MANAGER);
		for (CnSView v : views) {
			if (v.getView().getNodeView(e.getNode()) != null) {
				if (!v.isUserView() && !v.getModifCluster()) {
					v.setViewState(new CnSUserViewState());
					ev.addParameter(CnSNetworkManager.NETWORK, view2networkMap.get(v));
					ev.addParameter(CnSNetworkManager.NETWORK_NAME, "Copy of " + view2networkMap.get(v).getName());
					CnSEventManager.handleMessage(ev);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.application.events.SetSelectedNetworkViewsListener#handleEvent(org.cytoscape.application.events.SetSelectedNetworkViewsEvent)
	 */
	@Override
	public void handleEvent(SetSelectedNetworkViewsEvent e) {
		if (e.getNetworkViews().size() > 0) 
			selectedView = getView(e.getNetworkViews().get(0));
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.model.events.RowsSetListener#handleEvent(org.cytoscape.model.events.RowsSetEvent)
	 */
	@Override
	public void handleEvent(RowsSetEvent e) {
		Collection<RowSetRecord> rsr = e.getColumnRecords("selected");
		String primaryKeyColname = e.getSource().getPrimaryKey().getName();
		if (selectedView != null) {
			List<CyNode> cn = CyTableUtil.getNodesInState(view2networkMap.get(selectedView).getNetwork(), "selected", true);
			if (cn.size() == 1) {
				if (rsr.size() > 0)
					for (RowSetRecord r : rsr) {
						Long nodeId = r.getRow().get(primaryKeyColname, Long.class);
						if (nodeId == null) continue;
						CyNode node = view2networkMap.get(selectedView).getNetwork().getNode(nodeId);
						if (node != null)
							if ((Boolean)r.getRawValue() == true) {
								//CnSEvent ev = new CnSEvent(CnSResultsPanel.GET_SELECTED_CLUSTER, CnSEventManager.RESULTS_PANEL);
								//CnSCluster selectedCluster = (CnSCluster)CnSEventManager.handleMessage(ev);
								//if (selectedCluster == null) {
								CnSEvent ev = new CnSEvent(CnSResultsPanel.SELECT_CLUSTER, CnSEventManager.RESULTS_PANEL);
								ev.addParameter(CnSResultsPanel.CLUSTER, nodeId);
								CnSEventManager.handleMessage(ev);
								//}
								//else if (selectedCluster.getCyNode().getSUID() != nodeId) {
								//	ev = new CnSEvent(CnSResultsPanel.SELECT_CLUSTER, CnSEventManager.RESULTS_PANEL);
								//	ev.addParameter(CnSResultsPanel.CLUSTER, nodeId);
								//	CnSEventManager.handleMessage(ev);
								//}
							}
					}
			}
			else if (cn.size() >= 2 || cn.size() == 0) {
				CnSEvent ev = new CnSEvent(CnSResultsPanel.SELECT_CLUSTER, CnSEventManager.RESULTS_PANEL);
				CnSEventManager.handleMessage(ev);
			}
			
			List<CyEdge> ce = CyTableUtil.getEdgesInState(view2networkMap.get(selectedView).getNetwork(), "selected", true);
			if (ce.size() == 1) {
				if (rsr.size() > 0)
					for (RowSetRecord r : rsr) {
						Long edgeId = r.getRow().get(primaryKeyColname, Long.class);
						if (edgeId == null) continue;
						CyEdge edge = view2networkMap.get(selectedView).getNetwork().getEdge(edgeId);
						if (edge != null)
							if ((Boolean)r.getRawValue() == true) {
								CnSEvent ev = new CnSEvent(CnSPartitionManager.GET_CLUSTER_LINK, CnSEventManager.PARTITION_MANAGER);
								ev.addParameter(CnSPartitionManager.CY_EDGE, edge);
								CnSClusterLink clusterLink = (CnSClusterLink)CnSEventManager.handleMessage(ev);
								
								if (clusterLink != null) {
									ev = new CnSEvent(CnSInfoPanel.INIT, CnSEventManager.INFO_PANEL);
									ev.addParameter(CnSInfoPanel.EDGE, edge);
									ev.addParameter(CnSInfoPanel.CLUSTER_LINK, clusterLink);
									ev.addParameter(CnSInfoPanel.PANEL, CnSInfoPanel.EDGE_DETAILS);
									ev.addParameter(CnSInfoPanel.NETWORK, view2networkMap.get(selectedView));
									CnSEventManager.handleMessage(ev);
								
									ev = new CnSEvent(CnSInfoPanel.SELECT_PANEL, CnSEventManager.INFO_PANEL);
									ev.addParameter(CnSInfoPanel.PANEL, CnSInfoPanel.EDGE_DETAILS);
									CnSEventManager.handleMessage(ev);
								}
							}
					}
			}
			else if (ce.size() >= 2 || ce.size() == 0) {
				CnSEvent ev = new CnSEvent(CnSInfoPanel.CLEAR, CnSEventManager.INFO_PANEL);
				ev.addParameter(CnSInfoPanel.PANEL, CnSInfoPanel.EDGE_DETAILS);
				CnSEventManager.handleMessage(ev);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.application.events.SetCurrentNetworkViewListener#handleEvent(org.cytoscape.application.events.SetCurrentNetworkViewEvent)
	 */
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (e.getNetworkView() != null) {
			selectedView = getView(e.getNetworkView());
			if (selectedView != null) selectedView.updateNodeContextMenu();
		}
		else
			selectedView = null;
	}
	
	private void expandCluster(Long suid) {
		boolean expanded = false;
		CyNode n2;
		HashMap<CyNode, CyEdge> node2edgeMap = new HashMap<CyNode, CyEdge>();
		CnSNetwork network = view2networkMap.get(selectedView);
		
		CnSEvent ev = new CnSEvent(CnSPartitionManager.GET_PARTITION, CnSEventManager.PARTITION_MANAGER);
		ev.addParameter(CnSPartitionManager.NETWORK, network);
		CnSPartition partition = (CnSPartition)CnSEventManager.handleMessage(ev);
		
		if (partition == null) partition = view2partitionMap.get(selectedView);
		
		CnSNode node = partition.getClusterNode(suid);
		
		CnSCluster cluster = null, linkedCluster;
		for (CnSCluster c : partition.getClusters()) {
			if (c.getCyNode() == node.getCyNode()) {
				cluster = c;
				break;
			}
		}
		
		if (cluster != null) {
			CnSView clusterView = getView(null, null, cluster);
			
			double x0, y0, x, y, x_min = 1000000, y_min = 1000000, x_max = 0, y_max = 0;
			
			x0 = selectedView.getView().getNodeView(cluster.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			y0 = selectedView.getView().getNodeView(cluster.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			
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
			
			selectedView.setModifCluster(true);
			
			ev = new CnSEvent(CyActivator.GET_CY_EVENT_HELPER, CnSEventManager.CY_ACTIVATOR);
			CyEventHelper eh = (CyEventHelper)CnSEventManager.handleMessage(ev);
			for (CnSNode cnsnode : cluster.getNodes()) {
				if (!selectedView.getView().getModel().containsNode(cnsnode.getCyNode())) {
					network.getNetwork().addNode(cnsnode.getCyNode());
					selectedView.getView().updateView();
					View<CyNode> nodeView = selectedView.getView().getNodeView(cnsnode.getCyNode());
					x = (x0 - ratio * (x_max + x_min) / 2) + ratio * clusterView.getView().getNodeView(cnsnode.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					y = (y0 - ratio * (y_max + y_min) / 2) + ratio * clusterView.getView().getNodeView(cnsnode.getCyNode()).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, 10.0);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, 10.0);
				}
			}
			eh.flushPayloadEvents();
			for (CnSEdge cnsedge : cluster.getEdges())
				network.getNetwork().addEdge(cnsedge.getCyEdge());
			eh.flushPayloadEvents();
			network.getNetwork().getRow(cluster.getCyNode()).set("selected", false);
			for (CnSNode cnsnode : cluster.getNodes())
				network.getNetwork().getRow(cnsnode.getCyNode()).set("selected", true);
			eh.flushPayloadEvents();
			
			selectedView.setLocation(cluster, x0, y0);
			
			Vector<CyNode> toRemove = new Vector<CyNode>();
			toRemove.addElement(node.getCyNode());
			network.getNetwork().removeNodes(toRemove);
			eh.flushPayloadEvents();
			
			for (CnSClusterLink cl : partition.getClusterLinks()) {
				linkedCluster = null;
				if (cl.getSource() == cluster)
					linkedCluster = cl.getTarget();	
				else if (cl.getTarget() == cluster)
					linkedCluster = cl.getSource();
				if (linkedCluster != null && selectedView.getClusters().contains(linkedCluster)) {
					expanded = selectedView.isExpanded(linkedCluster);
					node2edgeMap.clear();
					
					for (CnSEdge ce : cl.getEdges())
						if (expanded)
							network.getNetwork().addEdge(ce.getCyEdge());
						else {
							n2 = null;
							for (CnSNode n : cluster.getNodes())
								if ((n.getCyNode() == ce.getCyEdge().getSource()) || (n.getCyNode() == ce.getCyEdge().getTarget())) {
									n2 = n.getCyNode();
									break;
								}
							if (node2edgeMap.get(n2) == null) {
								node2edgeMap.put(n2, network.getNetwork().addEdge(linkedCluster.getCyNode(), n2, false));
								eh.flushPayloadEvents();
							}
							else {
								double d = selectedView.getView().getEdgeView(node2edgeMap.get(n2)).getVisualProperty(BasicVisualLexicon.EDGE_WIDTH);
								selectedView.getView().getEdgeView(node2edgeMap.get(n2)).setVisualProperty(BasicVisualLexicon.EDGE_WIDTH, d < 10.0 ? d + 1.0 : d);
								eh.flushPayloadEvents();
							}
						}
					for (CnSNode n : cl.getNodes()) {
						if (!expanded) {
							if (!network.getNetwork().containsEdge(linkedCluster.getCyNode(), n.getCyNode()) && 
									!network.getNetwork().containsEdge(n.getCyNode(), linkedCluster.getCyNode())) {
								CyEdge cyEdge = network.getNetwork().addEdge(linkedCluster.getCyNode(), n.getCyNode(), false);
								eh.flushPayloadEvents();
								selectedView.getView().getEdgeView(cyEdge).setVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.green);
								eh.flushPayloadEvents();
							}
						}
					}
				}
			}
			
			eh.flushPayloadEvents();
			
			selectedView.setExpanded(cluster, true);
			selectedView.setModifCluster(false);
		}
	}
	private CnSView getView(Object reference, CnSNetwork network, CnSCluster cluster) {
		CnSView ret = null;
		
		if (reference != null) {
			for (CnSView v : views)
				if (v.getReference() != null)
					if (v.getReference() == reference) {
						ret = v;
						break;
					}
		}
		else {
			if (network != null)
				ret = network2viewMap.get(network);
			else {
				if (cluster != null)
					ret = cluster2viewMap.get(cluster);
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.model.events.AboutToRemoveNodesListener#handleEvent(org.cytoscape.model.events.AboutToRemoveNodesEvent)
	 */
	@Override
	public void handleEvent(AboutToRemoveNodesEvent e) {
		CnSEvent ev = new CnSEvent(CyActivator.GET_NETWORK_VIEW_MANAGER, CnSEventManager.CY_ACTIVATOR);
        CyNetworkViewManager networkViewManager = (CyNetworkViewManager)CnSEventManager.handleMessage(ev);
        Collection<CyNetworkView> views = networkViewManager.getNetworkViews(e.getSource());
        CnSView cnsv = null;
        for (CyNetworkView v : views) {
        	cnsv = getView(v);
        	if (cnsv != null) break;
        }
        if (cnsv != null) {
        	for (CyNode cyNode : e.getNodes()) {
        		CnSCluster cluster = null; 
        		for (CnSCluster cnsCluster : selectedView.getClusters())
					if (cnsCluster.getCyNode() == cyNode) {
						cluster = cnsCluster;
						break;
					}
				if (cluster != null) cnsv.removeCluster(cluster);
        	}
        	if (!cnsv.isUserView() && !cnsv.getModifCluster()) {
        		cnsv.setViewState(new CnSUserViewState());
        		ev = new CnSEvent(CnSNetworkManager.RENAME_NETWORK, CnSEventManager.NETWORK_MANAGER);
        		ev.addParameter(CnSNetworkManager.NETWORK, view2networkMap.get(cnsv));
        		ev.addParameter(CnSNetworkManager.NETWORK_NAME, "Copy of " + view2networkMap.get(cnsv).getName());
        		CnSEventManager.handleMessage(ev);
        	}	
        }
	}
}
