package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import uk.co.oliford.jolu.OneLiners;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_base.AlignmentState;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7xNBI;

import java.util.HashMap;

import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Square;

/** 40 "EdgeVIS" line during OP1.2, OP2.1 and OP2.2  (change the dY in setupFibrePositions() and the design name appropriately) */ 
public class BeamEmissSpecAEK41_edgeVIS extends BeamEmissSpecAEK41_base {
	
	public String getDesignName() { return "aek41-edgeVIS-op2.2-" + alignmentState.toString();	}

	public double designWavelenth = 530e-9; // VIS centre
	
	public double fibreEndDiameter = 0.000400; // Standard CXRS (as AUG)
	
	public String lightPathsSystemName() { return "AEK41"; };	
	public String[] lightPathRowNames() { return new String[]{ "EdgeVIS" }; };

	public BeamEmissSpecAEK41_edgeVIS(AlignmentState alignment) {
		super(alignment);
	}
	

	public static HashMap<String, double[]> measured = new HashMap<>();	
	static {
		// Post OP2.3 in-vessel alignment measurement 
		//FreeCAD command to give coordinates of selected balls :
		// for i in FreeCAD.Gui.Selection.getSelection() : print(i.Label + " " + str(i.Placement.Base.multiply(0.0001)))

		 measured.put("AEK41_EdgeVIS:01",  new double[] {  -0.28671800000000003, -0.385801, 0.07436999999999999});
		 measured.put("AEK41_EdgeVIS:05",  new double[] {  -0.28744800000000004, -0.38487099999999996, 0.0770999999999999});
		 measured.put("AEK41_EdgeVIS:09",  new double[] {  -0.28819800000000007, -0.38487099999999996, 0.07966});
		 measured.put("AEK41_EdgeVIS:21",  new double[] {  -0.28818800000000006, -0.37822100000000003, 0.08854000000000001});
		 measured.put("AEK41_EdgeVIS:39",  new double[] {  -0.31222800000000006, -0.39828100000000005, 0.0937});
			
	}
	
	/** Set fibre positions equal spacing in holder */
	protected void setupFibrePositions() {
		int nChans = 40;
		
		beamIdx = new int[]{ 0 };
		channelR = new double[][]{ OneLiners.linSpace(5.4, 5.5, nChans) }; // Nonsense
			
		int nBeams = 1;
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			
			double dX = 0.000500; //Fibre spacing, roughly what we got in the AET2x head
			double dY = 0.007; // Plate design. +ve for OP1.2, -ve for OP2.1 to avoid portliner (turned plate upside down) 
			double x0 = -(nFibres-1.0)/2 * dX; 
			for(int iF=0; iF < nFibres; iF++){
				fibreEndPos[iB][iF] = Util.plus(fibrePlanePos, Util.mul(fibresXVec, x0 + iF * dX));
				
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibresYVec, dY));
						
				fibreEndNorm[iB][iF] = Util.mul(fibrePlane.getNormal().clone(), -1);
			}
			if(fibreFocus != null){
				for(int iF=0; iF < nFibres; iF++){
					fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibrePlane.getNormal(), fibreFocus[iB][iF]));
				}	
			}
		}
	}
	
	public double targetObsPos[] = EdgePenetrationAEK41.def().start(0);
	public double beamAxis[] = EdgePenetrationAEK41.def().uVec(0);
	
	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 1.500, 2.000, NullInterface.ideal());


	
	public double getFibreNA(int iB, int iP) { return fibreNA;	}
	public double getFibreDiameter(int iB, int iP) { return fibreEndDiameter; }
}
