/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 11 mars 2021
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.gui.controlpanel.networkfiletree;

import org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTreeModel;
import org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTreeNode;
import org.cytoscape.model.CyNetwork;

/**
 * 
 */
public class CnSNetworksTreeModel extends CnSPanelTreeModel {
	private static final long serialVersionUID = -7718553296538201630L;

	public CnSNetworksTreeModel(CnSPanelTreeNode treeNode) {
		super(treeNode);
	}
	public void addNetwork(CnSPanelTreeNode parent, CyNetwork network, int nbAnnotations, int nbNodes) {
		
	}
}
