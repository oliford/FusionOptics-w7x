package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import net.jafama.FastMath;
import oneLiners.OneLiners;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7XPelletsK41;
import ipp.w7x.neutralBeams.W7XPelletsL41;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Square;

public class BeamEmissSpecAEK21_pelletsL41 extends BeamEmissSpecAEK21_base {
	public double designWavelenth = 530e-9; // Vis centre

	public static double fibreEndDiameter = 0.000400; // as AUG
	public static double fibreSpacing = 0.000500;
	
	public static double fibre0R = 0.0057;
	public static double fibre0U = 0.0335;
	public static double axisAngleToUp = 49.0 * Math.PI / 180; 
	
	public static double focusAdjust = -0.012;
	
	public String lightPathsSystemName = "AEK41_PelletsL";
	
	@Override
	protected void setupFibrePositions() {
		
		int nChans = 10;
		
		beamIdx = new int[]{ 0 };
		channelR = new double[][]{ OneLiners.linSpace(5.4, 5.5, nChans) }; // Nonsense
			
		int nBeams = 1;
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		fibreFocus = new double[][]{ OneLiners.fillArray(focusAdjust, nChans) };
		
		int iB=0;
		int nFibres = channelR[iB].length;
		fibreEndPos[iB] = new double[nFibres][];
		fibreEndNorm[iB] = new double[nFibres][];
		 
		double dR = fibreSpacing * FastMath.cos(axisAngleToUp);
		double dU = fibreSpacing * FastMath.sin(axisAngleToUp);
		
		for(int iF=0; iF < nFibres; iF++){
			double u = -fibre0U - (iF - (nFibres-1.0)/2) * dR;
			double r = -fibre0R - (iF - (nFibres-1.0)/2) * dU;
			
			fibreEndPos[iB][iF] = Util.plus(Util.plus(fibrePlanePos, Util.mul(fibresXVec, u)),
									 Util.mul(fibresYVec, r));
					
			fibreEndNorm[iB][iF] = Util.mul(fibrePlane.getNormal().clone(), -1);
		}
		
		if(fibreFocus != null){
			for(int iF=0; iF < nFibres; iF++){
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibrePlane.getNormal(), fibreFocus[iB][iF]));
			}	
		}
		//*/
		/*
		beamIdx = new int[]{ 0 };
		channelR = new double[][]{ { 5.2, 5.25, 5.3, 5.35, 5.4 }}; 
		fibreEndPos = new double[][][] { { 
			{ -5.258930550176042, -6.644419706905329, 0.028315713487048356 },
			{ -5.259859174571016, -6.6446638805311915, 0.027703116367058103 },
			{ -5.260250100311829, -6.644242001725668, 0.027104700906280598 },
			{ -5.261144339827509, -6.644400771043949, 0.026447070187280323 },
			{ -5.26173636816293, -6.644123588571671, 0.02578927804167362 },
					}, 	}; 
		fibreEndNorm = new double[][][]{ { 
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					}, 	};
			//*/
	}
	

	public double targetObsPos[] = W7XPelletsL41.def().getPosOfBeamAxisAtR(0, 5.9);
	public double beamAxis[] = W7XPelletsL41.def().uVec(0);
	
	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 1.500, 2.000, NullInterface.ideal());

	
	public String getDesignName() { return "aek41-pelletsL41";	}
}
