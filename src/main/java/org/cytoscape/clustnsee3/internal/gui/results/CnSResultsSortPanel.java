/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 4 août 2020
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.gui.results;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.clustnsee3.internal.analysis.CnSCluster;
import org.cytoscape.clustnsee3.internal.event.CnSEvent;
import org.cytoscape.clustnsee3.internal.event.CnSEventManager;
import org.cytoscape.clustnsee3.internal.gui.widget.CnSPanel;
import org.cytoscape.clustnsee3.internal.partition.CnSPartition;

/**
 * 
 */
public class CnSResultsSortPanel extends CnSPanel {
	private static final long serialVersionUID = -3321569834084429992L;
	
	private JCheckBox sortCheckBox;
	private JComboBox<String> sortList;
	private JComboBox<Integer> clusterList;
	
	public CnSResultsSortPanel() {
		super();
		initGraphics();
		initListeners();
	}
	
	protected void initGraphics() {
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		sortCheckBox = new JCheckBox();
		sortCheckBox.setSelected(false);
		addComponent(sortCheckBox, 0, 0, 1, 1, 0.0, 1.0, WEST, NONE, 5, 5, 5, 0, 0, 0);
		addComponent(new JLabel("Sort clusters by "), 1, 0, 1, 1, 0.0, 1.0, WEST, NONE, 5, 5, 5, 0, 0, 0);
		sortList = new JComboBox<String>(CnSCluster.COMPARE_NAME);
		sortList.setEnabled(false);
		addComponent(sortList, 2, 0, 1, 1, 0.0, 1.0, WEST, HORIZONTAL, 5, 5, 5, 5, 0, 0);
		clusterList = new JComboBox<Integer>();
		addComponent(new JLabel("Go to cluster #"), 0, 1, 2, 1, 0.0, 1.0, WEST, NONE, 0, 5, 5, 0, 0, 0);
		addComponent(clusterList, 2, 1, 1, 1, 0.0, 1.0, WEST, NONE, 0, 5, 5, 5, 0, 0);
	}
	
	private void initListeners() {
		sortCheckBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ev) {
				if (sortCheckBox.isSelected()) {
					sortList.setEnabled(true);
				}
				else {
					sortList.setEnabled(false);
					sortList.setSelectedIndex(0);
				}
			}
			
		});
		sortList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					String choice = (String)event.getItem();
					CnSCluster.setCompareType(choice);
					CnSEvent ev = new CnSEvent(CnSResultsPanel.SORT_RESULTS, CnSEventManager.RESULTS_PANEL);
					CnSEventManager.handleMessage(ev);
				}
			}
		});
		clusterList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					CnSEvent ev = new CnSEvent(CnSResultsPanel.SELECT_CLUSTER, CnSEventManager.RESULTS_PANEL);
					ev.addParameter(CnSResultsPanel.CLUSTER_NAME, clusterList.getSelectedItem());
					CnSEventManager.handleMessage(ev);
				}
			}
		});
	}
	public void init(CnSPartition partition) {
		clusterList.removeAllItems();
		for (int i = 1; i <= partition.getClusters().size(); i++) clusterList.addItem(i);
	}
}