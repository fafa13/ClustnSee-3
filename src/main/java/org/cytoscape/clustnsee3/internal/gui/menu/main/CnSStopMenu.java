/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 6 août 2020
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.gui.menu.main;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.clustnsee3.internal.CyActivator;
import org.cytoscape.clustnsee3.internal.event.CnSEvent;
import org.cytoscape.clustnsee3.internal.event.CnSEventManager;
import org.cytoscape.clustnsee3.internal.view.style.CnSStyleManager;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class CnSStopMenu extends AbstractCyAction {
	private static final long serialVersionUID = 7814074516214991713L;
	private static CnSStopMenu instance;
	
	private CnSStopMenu(BundleContext context, CyActivator ca) {
		super("Stop"); 						
		setPreferredMenu("Apps.Clust&see");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		CnSEvent ev = new CnSEvent(CnSStyleManager.REMOVE_CNS_STYLES, CnSEventManager.STYLE_MANAGER);
		CnSEventManager.handleMessage(ev);
		ev = new CnSEvent(CyActivator.STOP, CnSEventManager.CY_ACTIVATOR);
		CnSEventManager.handleMessage(ev);
	}
	public static CnSStopMenu getInstance(BundleContext context, CyActivator ca) {
		if (instance == null) instance = new CnSStopMenu(context, ca);
		return instance;
	}
}