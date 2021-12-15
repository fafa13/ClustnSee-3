/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 25 mars 2021
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.gui.controlpanel.networkfiletree.nodes.root;

import java.awt.event.ActionEvent;
import java.util.Hashtable;

import org.cytoscape.clustnsee3.internal.gui.controlpanel.annotationfiletree.nodes.details.CnSAFTreeDetailsNodePanel;
import org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTreeNode;

/**
 * 
 */
public class CnSAFTreeNetworksRootNode extends CnSPanelTreeNode {
	public static final int TITLE = 1;
	public static final int DETAILS_NODE_PANEL = 2;
	
	private CnSAFTreeDetailsNodePanel detailsNodePanel;
	public CnSAFTreeNetworksRootNode(Hashtable<Integer, Object> v) {
		super(null, v);
		panel = new CnSAFTreeNetworksRootNodePanel(v.get(TITLE).toString());
		detailsNodePanel = (CnSAFTreeDetailsNodePanel)v.get(DETAILS_NODE_PANEL);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	public CnSAFTreeDetailsNodePanel getDetailsNodePanel() {
		return detailsNodePanel;
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTreeNode#getValue()
	 */
	@Override
	public Object getValue() {
		return ((CnSAFTreeNetworksRootNodePanel)panel).getValue();
	}
}
