package org.cytoscape.clustnsee3.internal.gui.controlpanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.clustnsee3.internal.CyActivator;
import org.cytoscape.clustnsee3.internal.event.CnSEvent;
import org.cytoscape.clustnsee3.internal.event.CnSEventManager;
import org.cytoscape.clustnsee3.internal.gui.controlpanel.annotationfiletree.nodes.root.CnSAFTreeRootNode;
import org.cytoscape.clustnsee3.internal.gui.controlpanel.annotationfiletree.CnSAFTreeModel;
import org.cytoscape.clustnsee3.internal.gui.dialog.CnSAnnotationFileStatsDialog;
import org.cytoscape.clustnsee3.internal.gui.dialog.CnSLoadAnnotationFileDialog;
import org.cytoscape.clustnsee3.internal.gui.partitionpanel.CnSPartitionPanel;
import org.cytoscape.clustnsee3.internal.gui.resultspanel.CnSResultsPanel;
import org.cytoscape.clustnsee3.internal.gui.widget.CnSButton;
import org.cytoscape.clustnsee3.internal.gui.widget.CnSPanel;
import org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTree;
import org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTreeCellEditor;
import org.cytoscape.clustnsee3.internal.gui.widget.paneltree.CnSPanelTreeCellRenderer;
import org.cytoscape.clustnsee3.internal.nodeannotation.CnSNodeAnnotationFile;
import org.cytoscape.clustnsee3.internal.nodeannotation.CnSNodeAnnotationManager;
import org.cytoscape.clustnsee3.internal.partition.CnSPartition;
import org.cytoscape.clustnsee3.internal.task.CnSAnalyzeTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class CnSControlPanel extends CnSPanel implements CytoPanelComponent {
	private static final long serialVersionUID = -5798886682673421450L;

	private CnSControlScopePanel scopePanel;
	private CnSControlAlgorithmPanel algorithmPanel;
	private CnSPanel analyzePanel;
	private CnSButton analyzeButton;
	private CnSPanel importAnnotationPanel;
	
	private CnSPanel treeImportAnnotationPanel;
	private CnSAFTreeModel treeModel;
	
	private JTable annotationTable;
	private CnSButton addAnnotationButton, removeAnnotationButton;
	private CnSPanel mainPanel;
	
	private CnSPanelTree tree;
	private CnSAFTreeRootNode rootNode;
	
	public CnSControlPanel(String title) {
		super(title);
		initGraphics();
		initListeners();
	}
	
	public void initGraphics() {
		mainPanel = new CnSPanel(); 
		analyzePanel = new CnSPanel("Analyze", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
		scopePanel = new CnSControlScopePanel("Scope");
		analyzePanel.addComponent(scopePanel, 0, 0, 1, 1, 1.0, 0.0, NORTH, HORIZONTAL, 0, 0, 10, 0, 0, 0);
		algorithmPanel = new CnSControlAlgorithmPanel("Algorithm");
		analyzePanel.addComponent(algorithmPanel, 0, 1, 1, 1, 1.0, 1.0, NORTH, HORIZONTAL, 0, 0, 0, 0, 0, 0);
		analyzeButton = new CnSButton("Run");
		analyzePanel.addComponent(analyzeButton, 0, 2, 1, 1, 1.0, 1.0, EAST, NONE, 0, 0, 5, 0, 0, 0);
		analyzePanel.initGraphics();
		mainPanel.addComponent(analyzePanel, 0, 0, 1, 1, 1.0, 0.0, NORTH, HORIZONTAL, 0, 0, 0, 0, 0, 0);
		
		importAnnotationPanel = new CnSPanel("Import annotation", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
		addAnnotationButton = new CnSButton("Add");
		importAnnotationPanel.addComponent(addAnnotationButton, 0, 1, 1, 1, 1.0, 0.0, NORTH, NONE, 0, 0, 0, 0, 0, 0);
		removeAnnotationButton = new CnSButton("Remove");
		importAnnotationPanel.addComponent(removeAnnotationButton, 1, 1, 1, 1, 1.0, 0.0, NORTH, NONE, 0, 0, 0, 0, 0, 0);
		
		importAnnotationPanel.initGraphics();
		mainPanel.addComponent(importAnnotationPanel, 0, 1, 1, 1, 1.0, 1.0, NORTH, HORIZONTAL, 10, 0, 0, 0, 0, 0);
		
		treeImportAnnotationPanel = new CnSPanel("XXX", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
		Hashtable<Integer, Object> v= new Hashtable<Integer, Object>();
		v.put(CnSAFTreeRootNode.TITLE, "Imported annotation files");
		rootNode = new CnSAFTreeRootNode(v);
		rootNode.getPanel().deriveFont(Font.PLAIN, 14);
		rootNode.getPanel().initGraphics();
		treeModel = new CnSAFTreeModel(rootNode);
		tree = new CnSPanelTree(treeModel);
		
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new CnSPanelTreeCellRenderer());
		tree.setCellEditor(new CnSPanelTreeCellEditor());
		
		JScrollPane jsp2 = new JScrollPane(tree);
		jsp2.getViewport().setPreferredSize(new Dimension(0, 10*26));
		treeImportAnnotationPanel.addComponent(jsp2, 0, 0, 1, 1, 1.0, 1.0, NORTH, BOTH, 0, 0, 0, 0, 0, 0);
		treeImportAnnotationPanel.initGraphics();
		mainPanel.addComponent(treeImportAnnotationPanel, 0, 2, 1, 1, 1.0, 1.0, NORTH, HORIZONTAL, 10, 0, 0, 0, 0, 0);
		
		addComponent(mainPanel, 0, 0, 1, 1, 1.0, 1.0, NORTH, BOTH, 0, 0, 0, 0, 0, 0);
		
		super.initGraphics();
	}
	
	private void initListeners() {
		analyzeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CnSEvent ev = new CnSEvent(CyActivator.GET_APPLICATION_MANAGER, CnSEventManager.CY_ACTIVATOR);
				CyApplicationManager cam = (CyApplicationManager)CnSEventManager.handleMessage(ev);
				CyNetwork network = cam.getCurrentNetwork();
				if (network == null)
					JOptionPane.showMessageDialog(null, "You must select a network first !");
				else {
					ev = new CnSEvent(CyActivator.GET_TASK_MANAGER, CnSEventManager.CY_ACTIVATOR);
					DialogTaskManager dialogTaskManager = (DialogTaskManager)CnSEventManager.handleMessage(ev);
					TaskIterator ti = new TaskIterator();
					CnSAnalyzeTask task = new CnSAnalyzeTask(network);
					ti.append(task);
					dialogTaskManager.execute(ti);
				}
			}
		});
		
		addAnnotationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CnSEvent ev = new CnSEvent(CnSResultsPanel.GET_SELECTED_PARTITION, CnSEventManager.RESULTS_PANEL);
				CnSPartition partition = (CnSPartition)CnSEventManager.handleMessage(ev);
				CnSLoadAnnotationFileDialog dialog = CnSLoadAnnotationFileDialog.getInstance();
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				dialog.setLocation((screenSize.width - dialog.getWidth()) / 2, (screenSize.height - dialog.getHeight()) / 2);
				dialog.setVisible(true);
				ev = new CnSEvent(CyActivator.GET_APPLICATION_MANAGER, CnSEventManager.CY_ACTIVATOR);
				CyApplicationManager cam = (CyApplicationManager)CnSEventManager.handleMessage(ev);
				CyNetwork network;
				if (partition == null)
					network = cam.getCurrentNetwork();
				else
					network = partition.getInputNetwork();
					
				if (dialog.getExitOption() == CnSLoadAnnotationFileDialog.OK_OPTION) {
					if (! treeModel.contains(dialog.getSelectedFile())) {
						ev = new CnSEvent(CnSNodeAnnotationManager.PARSE_ANNOTATIONS, CnSEventManager.ANNOTATION_MANAGER);
						ev.addParameter(CnSNodeAnnotationManager.FILE, dialog.getSelectedFile());
						ev.addParameter(CnSNodeAnnotationManager.FROM_LINE, dialog.getFromLine());
						ev.addParameter(CnSNodeAnnotationManager.NETWORK, network);
						int[] results = (int[])CnSEventManager.handleMessage(ev);
						
						CnSAnnotationFileStatsDialog statsDialog = new CnSAnnotationFileStatsDialog(results[0], results[1], results[2]);
						statsDialog.setLocation((screenSize.width - statsDialog.getWidth()) / 2, (screenSize.height - statsDialog.getHeight()) / 2);
						statsDialog.setVisible(true);
						if (statsDialog.getExitOption() == CnSAnnotationFileStatsDialog.OK_OPTION) {
							ev = new CnSEvent(CnSNodeAnnotationManager.LOAD_ANNOTATIONS, CnSEventManager.ANNOTATION_MANAGER);
							ev.addParameter(CnSNodeAnnotationManager.FILE, dialog.getSelectedFile());
							ev.addParameter(CnSNodeAnnotationManager.FROM_LINE, dialog.getFromLine());
							ev.addParameter(CnSNodeAnnotationManager.NETWORK, network);
							CnSNodeAnnotationFile annotationFile = (CnSNodeAnnotationFile)CnSEventManager.handleMessage(ev);
						
							ev = new CnSEvent(CnSPartitionPanel.INIT_ANNOTATION_PANEL, CnSEventManager.PARTITION_PANEL);
							if (partition != null) ev.addParameter(CnSPartitionPanel.PARTITION, partition);
							CnSEventManager.handleMessage(ev);
							
							treeModel.addAnnotationFile(rootNode, annotationFile, results[1], results[0]);
							treeModel.mapAnnotation(network);
						}
					}
					else {
						JOptionPane.showMessageDialog(null, "Annotation file " + dialog.getSelectedFile().getName() + " is already loaded.");
					}
				}
			}
		});
		removeAnnotationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//CnSNodeAnnotationFile annotationFile = (CnSNodeAnnotationFile)annotationFileTableModel.getValueAt(annotationTable.getSelectedRow(), 0);
				CnSEvent ev = new CnSEvent(CnSNodeAnnotationManager.UNLOAD_ANNOTATIONS, CnSEventManager.ANNOTATION_MANAGER);
				//ev.addParameter(CnSNodeAnnotationManager.FILE, annotationFile);
				CnSEventManager.handleMessage(ev);
				ev = new CnSEvent(CnSPartitionPanel.INIT_ANNOTATION_PANEL, CnSEventManager.PARTITION_PANEL);
				CnSEventManager.handleMessage(ev);
				//annotationFileTableModel.removeItem(annotationFile);
				//annotationFileTableModel.fireTableDataChanged();
				annotationTable.repaint();
			}
		});
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * 
	 * @param
	 * @return
	 */
	public void setAnalysisEnabled(Boolean enable) {
		analyzeButton.setEnabled(enable);
	}
}