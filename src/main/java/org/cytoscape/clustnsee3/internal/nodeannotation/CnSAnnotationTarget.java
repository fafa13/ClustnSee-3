/**
/* Copyright (C) 2018 TAGC, Luminy, Marseille
/*
/* @author Fabrice Lopez (TAGC/BCF, Luminy, Marseille)
/* @date 30 nov. 2021
/*
/* with contributions from:
/* Lionel Spinelli (CIML/TAGC, Luminy, Marseille)
/* Christine Brun, Charles Chapple, Benoit Robisson (TAGC, Luminy, Marseille)
/* Alain Guénoche, Anaïs Baudot, Laurent Tichit (IML, Luminy, Marseille)
/* Philippe Gambette (LIGM, Marne-la-Vallée)
 */

package org.cytoscape.clustnsee3.internal.nodeannotation;

/**
 * 
 */
public class CnSAnnotationTarget {
	private CnSNodeAnnotationFile file;
	String target;
	
	public CnSAnnotationTarget(String node, CnSNodeAnnotationFile file) {
		target = node;
		this.file = file;
	}
	public String getTarget() {
		return target;
	}
	public CnSNodeAnnotationFile getFile() {
		return file;
	}
}