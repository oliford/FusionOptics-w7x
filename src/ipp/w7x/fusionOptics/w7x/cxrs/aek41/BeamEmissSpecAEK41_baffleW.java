package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import net.jafama.FastMath;
import uk.co.oliford.jolu.OneLiners;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7XPelletsK41;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Square;

public class BeamEmissSpecAEK41_baffleW extends BeamEmissSpecAEK41_base {
	public double designWavelenth = 530e-9; // VIS centre

	public static double fibreEndDiameter = 0.000400; // as AUG
	public static double fibreSpacing = 0.005;
	
	public static double fibre0R = 0.0100;
	public static double fibre0U = 0.0245;
	public static double axisAngleToUp = -55 * Math.PI / 180; 

	public String lightPathsSystemName() { return "AEK41"; };	
	public String[] lightPathRowNames() { return new String[]{ "BaffleW" }; };
	
	public double overrideObsPositions[][][] = {
			{ //red
				{-3.12250537109375, -3.871598876953125, 0.4270995788574219 },
				{-3.027144775390625, -3.89112841796875, 0.4087387390136719 },
				{-3.14693994140625, -3.89616357421875, 0.3238131103515625 },
				{-3.05343310546875, -3.913257568359375, 0.3027521667480469 },			
			}
	};
	
	
	@Override
	protected void setupFibrePositions() {
		int nRows = 4;
		int nCols = 6;
		int nChans = nRows * nCols;
		
		beamIdx = new int[]{ 0 };
		channelR = new double[][]{ OneLiners.linSpace(5.4, 5.5, nChans) }; // Nonsense
		
		fibreFocus = new double[][]{ OneLiners.fillArray(0.000000, nChans) };
			
		int nBeams = 1;
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		int iB=0;
		int nFibres = channelR[iB].length;
		fibreEndPos[iB] = new double[nFibres][];
		fibreEndNorm[iB] = new double[nFibres][];
		 
		double dR = fibreSpacing * FastMath.cos(axisAngleToUp);
		double dU = fibreSpacing * FastMath.sin(axisAngleToUp);
		
		for(int iR=0; iR < nRows; iR++){
			for(int iC=0; iC < nCols; iC++){
				int iF = iR * nCols + iC; 
					
				double u = -fibre0U  -(iR - (nRows-1.0)/2) * -dU -(iC - (nCols-1.0)/2) * dR;
				double r = -fibre0R -(iR - (nRows-1.0)/2) * dR -(iC - (nCols-1.0)/2) * dU;
				
				fibreEndPos[iB][iF] = Util.plus(Util.plus(fibrePlanePos, Util.mul(fibresXVec, u)),
										 Util.mul(fibresYVec, r));
						
				fibreEndNorm[iB][iF] = Util.mul(fibrePlane.getNormal().clone(), -1);
			}
		}
		
		if(fibreFocus != null){
			for(int iF=0; iF < nFibres; iF++){
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibrePlane.getNormal(), fibreFocus[iB][iF]));
			}	
		}
	
	}
	

	//plane through pellets line (OP1.2)
	//public double targetObsPos[] = W7XPelletsK41.def().getPosOfBeamAxisAtR(0, 5.9);
	//public double beamAxis[] = W7XPelletsK41.def().uVec(0);
	
	//plane from edge penetration
	public double targetObsPos[] = EdgePenetrationAEK41.def().start(0);
	public double beamAxis[] = EdgePenetrationAEK41.def().uVec(0);
		
	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
			
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 1.500, 2.000, NullInterface.ideal());

	public final String backgroundSTLFiles[] = {
			"/work/ipp/w7x/cad/passive/bg-targetting/pumpslot-m4.off-aek41-cut.stl",
			"/work/ipp/w7x/cad/passive/bg-targetting/target-m4.off-aek41-cut.stl",
			"/work/ipp/w7x/cad/passive/bg-targetting/baffle-m4.off-aek41-cut.stl",
			"/work/ipp/w7x/cad/passive/bg-targetting/shield-m4.off-aek41-cut.stl"
	};

	
	public String getDesignName() { return "aek41-baffleW-op2.2";	}
}
