/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date Nov 12, 2018
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal;

import org.cytoscape.clustnsee3.internal.algorithm.CnSAlgorithmManager;

import java.util.Hashtable;
import java.util.Vector;

import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.clustnsee3.internal.algorithm.CnSAlgorithmEngine;
import org.cytoscape.clustnsee3.internal.event.CnSEvent;
import org.cytoscape.clustnsee3.internal.event.CnSEventListener;
import org.cytoscape.clustnsee3.internal.event.CnSEventManager;
import org.cytoscape.clustnsee3.internal.event.CnSEventResult;
import org.cytoscape.clustnsee3.internal.gui.controlpanel.CnSControlPanel;
import org.cytoscape.clustnsee3.internal.gui.infopanel.CnSInfoPanel;
import org.cytoscape.clustnsee3.internal.gui.menu.contextual.action.CnSMenuManager;
import org.cytoscape.clustnsee3.internal.gui.partitionpanel.CnSPartitionPanel;
import org.cytoscape.clustnsee3.internal.gui.resultspanel.CnSResultsPanel;
import org.cytoscape.clustnsee3.internal.gui.util.CnSSynchronizeClusterSelection;
import org.cytoscape.clustnsee3.internal.network.CnSNetworkManager;
import org.cytoscape.clustnsee3.internal.nodeannotation.CnSNodeAnnotationManager;
import org.cytoscape.clustnsee3.internal.partition.CnSPartitionManager;
import org.cytoscape.clustnsee3.internal.utils.CnSLogger;
import org.cytoscape.clustnsee3.internal.view.CnSViewManager;
import org.cytoscape.clustnsee3.internal.view.style.CnSStyleManager;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.model.events.UnsetNetworkPointerListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * 
 */
public class CnSClustnseePlugin implements CnSEventListener {
	public static final int GET_PANEL = 1;
	public static final int ENABLE_ANALYZIS = 2;
	
	public static final int ENABLE = 1000;
			
	private CnSAlgorithmManager algorithmManager;
	private CnSPartitionManager analysisManager;
	private CnSMenuManager menuManager;
	private CnSInfoPanel dataPanel;
	private CnSPartitionPanel partitionPanel;
	private CnSResultsPanel resultsPanel;
	private CnSAlgorithmEngine algorithmEngine;
	private CnSViewManager viewManager;
	private CnSNetworkManager networkManager;
	private CnSPartitionManager partitionManager;
	private CnSStyleManager styleManager;
	private Vector<ServiceRegistration<?>> ref;
	private CnSControlPanel controlPanel;
	private CnSNodeAnnotationManager nodeAnnotationManager;
	private static CnSClustnseePlugin instance;
	private BundleContext bc;
	private CnSSynchronizeClusterSelection synchonizeClusterSelection;
	
	private CnSClustnseePlugin(BundleContext context, CyActivator ca) {
		super();
		bc = context;
		algorithmManager = CnSAlgorithmManager.getInstance();
		analysisManager = CnSPartitionManager.getInstance();
		menuManager = CnSMenuManager.getInstance();
		dataPanel = CnSInfoPanel.getInstance();
		partitionPanel = CnSPartitionPanel.getInstance();
		resultsPanel = CnSResultsPanel.getInstance();
		algorithmEngine = CnSAlgorithmEngine.getInstance();
		viewManager = CnSViewManager.getInstance();
		networkManager = CnSNetworkManager.getInstance();
		partitionManager = CnSPartitionManager.getInstance();
		styleManager = CnSStyleManager.getInstance();
		nodeAnnotationManager = CnSNodeAnnotationManager.getInstance();
		synchonizeClusterSelection = new CnSSynchronizeClusterSelection();
		CnSEventManager.getCnsEventManager(this, analysisManager, menuManager, dataPanel, resultsPanel, algorithmManager, 
				algorithmEngine, viewManager, networkManager, partitionManager, styleManager, partitionPanel, 
				nodeAnnotationManager, synchonizeClusterSelection, ca);
		CnSEvent ev = new CnSEvent(CnSAlgorithmManager.INIT, CnSEventManager.ALGORITHM_MANAGER, this.getClass());
		CnSEventManager.handleMessage(ev, true);
		ev = new CnSEvent(CnSStyleManager.INIT, CnSEventManager.STYLE_MANAGER, this.getClass());
		CnSEventManager.handleMessage(ev, true);
	}
	
	public String getActionName(int k) {
		switch(k) {
			case GET_PANEL : return "GET_PANEL";
			case ENABLE_ANALYZIS : return "ENABLE_ANALYZIS";
			default : return "UNDEFINED_ACTION";
		}
	}

	public String getParameterName(int k) {
		switch(k) {
			case ENABLE : return "ENABLE";
			default : return "UNDEFINED_PARAMETER";
		}
	}

	public void registerServices() {
		CnSEvent ev = new CnSEvent(CnSClustnseePlugin.GET_PANEL, CnSEventManager.CLUSTNSEE_PLUGIN, this.getClass());
		controlPanel = (CnSControlPanel)CnSEventManager.handleMessage(ev, true).getValue();
		CnSEventManager.addControlPanel(controlPanel);
		
		ref = new Vector<ServiceRegistration<?>>();
		Hashtable<String, ?> dict = new Hashtable<String, Object>();
		ref.addElement(bc.registerService(CytoPanelComponent.class.getName(), controlPanel, dict));
		ref.addElement(bc.registerService(CytoPanelComponent.class.getName(), resultsPanel, dict));
		ref.addElement(bc.registerService(CytoPanelComponent.class.getName(), dataPanel, dict));
		ref.addElement(bc.registerService(CytoPanelComponent.class.getName(), partitionPanel, dict));
		ref.addElement(bc.registerService(AboutToRemoveNodesListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(AddedNodesListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(RemovedEdgesListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(AddedEdgesListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(NetworkViewAboutToBeDestroyedListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(NetworkAboutToBeDestroyedListener.class.getName(), networkManager, dict));
		ref.addElement(bc.registerService(UnsetNetworkPointerListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(SetCurrentNetworkViewListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(SelectedNodesAndEdgesListener.class.getName(), viewManager, dict));
		ref.addElement(bc.registerService(SessionLoadedListener.class.getName(), partitionManager, dict));
		ref.addElement(bc.registerService(SessionLoadedListener.class.getName(), controlPanel, dict));
	}
	
	public static CnSClustnseePlugin getInstance(BundleContext context, CyActivator ca) {
		if (instance == null) instance = new CnSClustnseePlugin(context, ca);
		return instance;
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.clustnsee3.internal.event.CnSEventListener#cnsEventOccured(org.cytoscape.clustnsee3.internal.event.CnSEvent)
	 */
	@Override
	public CnSEventResult<?> cnsEventOccured(CnSEvent event, boolean log) {
		CnSEventResult<?> ret = new CnSEventResult<Object>(null);
		
		if (log) CnSLogger.getInstance().LogCnSEvent(event, this);
		
		switch (event.getAction()) {
			case (GET_PANEL) :
				ret = new CnSEventResult<CnSControlPanel>(new CnSControlPanel("Clust&See"));
				break;
			case ENABLE_ANALYZIS :
				controlPanel.setAnalysisEnabled((Boolean)event.getParameter(ENABLE));
				break;
		}
		return ret;
	}
	public void stop() {
		for (ServiceRegistration<?> sr : ref) sr.unregister();
		ref.clear();
		instance = null;
	}
}