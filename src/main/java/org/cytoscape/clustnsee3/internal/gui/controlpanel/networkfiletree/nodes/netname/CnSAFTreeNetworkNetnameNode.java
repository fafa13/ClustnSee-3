/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 23 mars 2021
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.gui.controlpanel.networkfiletree.nodes.netname;

import java.awt.event.ActionEvent;
import java.util.Hashtable;

import org.cytoscape.clustnsee3.internal.gui.widget.CnSButton;
import org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTreeNode;
import org.cytoscape.model.CyNetwork;

/**
 * 
 */
public class CnSAFTreeNetworkNetnameNode extends CnSPanelTreeNode {
	public final static int NETWORK_NAME = 1;
	public static final int NETWORK = 2;
	
	private CyNetwork network;

	public CnSAFTreeNetworkNetnameNode(CnSPanelTreeNode parent, Hashtable<Integer, Object> v) {
		super(parent, v);
		network = (CyNetwork)v.get(NETWORK);
		panel = new CnSAFTreeNetworkNetnameNodePanel(network.toString());
		panel.initGraphics();
		((CnSAFTreeNetworkNetnameNodePanel)panel).getDeleteButton().addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof CnSButton) {
			if (((CnSButton)e.getSource()).getActionCommand().equals("remove_network")) {
				System.err.println("Removing network : " + network.toString());
			}
		}
	}
}
