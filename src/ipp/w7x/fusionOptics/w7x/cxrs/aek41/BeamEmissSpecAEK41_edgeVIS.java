package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import oneLiners.OneLiners;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7xNBI;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Square;

/** Jürgens 5-channel edge 'passive CXRS' */ 
public class BeamEmissSpecAEK41_edgeVIS extends BeamEmissSpecAEK41_base {
	public double designWavelenth = 530e-9; // VIS centre
	
	public double fibreEndDiameter = 0.000400; // Standard CXRS (as AUG)
	
	public String lightPathsSystemName = "AEK41";	
	public String lightPathRowName[] = { "EdgeVIS" };
	
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
			double dY = 0.007; // Made up, about as close as we might get it
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

	
	public final String backgroundSTLFiles[] = {
			"/work/ipp/w7x/cad/passive/bg-targetting/pumpslot-m4.off-aek41-cut.stl",
			"/work/ipp/w7x/cad/passive/bg-targetting/target-m4.off-aek41-cut.stl"
	};

	
	public String getDesignName() { return "aek41-edgeVIS";	}
	
	public double getFibreNA(int iB, int iP) { return fibreNA;	}
	public double getFibreDiameter(int iB, int iP) { return fibreEndDiameter; }
}
