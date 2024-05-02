package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import uk.co.oliford.jolu.OneLiners;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7xNBI;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Square;

/** Jürgens 5-channel edge 'passive CXRS' */ 
public class BeamEmissSpecAEK41_edgeUV extends BeamEmissSpecAEK41_base {
	public double designWavelenth = 250e-9; // [ Jürgen wants to use primarily 220nm and 280nm I think ]
	
	public double fibreEndDiameter = 0.003000; // Bundle end in square case, plugged in 5.5mm round hole

	public String lightPathsSystemName() { return "AEK41"; };	
	public String[] lightPathRowNames() { return new String[]{ "EdgeUV" }; };
	
	/** Set fibre positions equal spacing in holder */
	protected void setupFibrePositions() {
		beamIdx = new int[]{ 0 };
		channelR = new double[][]{ { 5.44, 5.446, 5.451, 5.457, 5.463 }}; // These don't mean much
			
		fibreFocus = new double[][]{ OneLiners.fillArray(-0.020000, channelR[0].length) };
		
		int nBeams = channelR.length;
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			
			double dX = 0.004, dY = -0.007; // [Jürgen's Visio diagram]
			double x0 = -(nFibres-1.0)/2 * dX; 
			for(int iF=0; iF < nFibres; iF++){
				fibreEndPos[iB][iF] = Util.plus(fibrePlanePos, Util.mul(fibresXVec, x0 + iF * dX));
				
				if((iF % 2) == 1)
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

	
	public final String backgroundSTLFiles[] = {
			"/work/ipp/w7x/cad/passive/bg-targetting/pumpslot-m4.off-aek41-cut.stl",
			"/work/ipp/w7x/cad/passive/bg-targetting/target-m4.off-aek41-cut.stl"
	};

	
	public String getDesignName() { return "aek41-edgeUV";	}
	
	public double getFibreNA(int iB, int iP) { return fibreNA;	}
	public double getFibreDiameter(int iB, int iP) { return fibreDiameter; }
}
