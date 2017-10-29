package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import jafama.FastMath;
import oneLiners.OneLiners;
import ipp.w7x.neutralBeams.W7XPelletsK41;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Square;

public class BeamEmissSpecAEK21_pelletsK41 extends BeamEmissSpecAEK21_base {
	public double designWavelenth = 530e-9; // VIS centre

	public static double fibreEndDiameter = 0.000400; // as AUG
	public static double fibreSpacing = 0.000500;
	
	public static double fibre0R = -0.0013;
	public static double fibre0U = 0.0185;
	public static double axisAngleToUp = 0;// 1.625 * Math.PI / 180; 

	public String lightPathsSystemName = "AEK41_PelletsK";
	
	@Override
	protected void setupFibrePositions() {
		int nChans = 10;
		
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
		
		for(int iF=0; iF < nFibres; iF++){
			double u = -fibre0U - (iF - (nFibres-1.0)/2)  * dR;
			double r = -fibre0R - (iF - (nFibres-1.0)/2)  * dU;
			
			fibreEndPos[iB][iF] = Util.plus(Util.plus(fibrePlanePos, Util.mul(fibresXVec, u)),
									 Util.mul(fibresYVec, r));
					
			fibreEndNorm[iB][iF] = Util.mul(fibrePlane.getNormal().clone(), -1);
		}
		
		if(fibreFocus != null){
			for(int iF=0; iF < nFibres; iF++){
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibrePlane.getNormal(), fibreFocus[iB][iF]));
			}	
		}
	
		
		/*beamIdx = new int[]{ 0 };
		channelR = new double[][] { 
				{ 5.6, 5.657, 5.714, 5.771, 5.829, 5.886, 5.943, 6, }, 
			}; 
			fibreEndPos = new double[][][] { { 
						{ -5.267306621477559, -6.642337278598608, 0.0190039435014635 },
						{ -5.267568578459989, -6.6426410809177865, 0.017972987397021332 },
						{ -5.267733091045658, -6.6427872289601675, 0.016902224938181967 },
						{ -5.26832737715623, -6.643451712777317, 0.015716382224422665 },
						{ -5.268386152566512, -6.643473714121546, 0.014561906352033353 },
						{ -5.268759837096962, -6.6438911613874785, 0.013290131143042706 },
						{ -5.2690309789246745, -6.644162986087092, 0.011982911344008331 },
						{ -5.26934029524865, -6.644479094223474, 0.010590691458417463 },
					}, 	}; 
			fibreEndNorm = new double[][][] { { 
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					{ 0.62109906, 0.74825539, 0.23313049 },
					}, 	};
			//*/
	}
	

	public double targetObsPos[] = W7XPelletsK41.def().getPosOfBeamAxisAtR(0, 5.9);
	public double beamAxis[] = W7XPelletsK41.def().uVec(0);
	
	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 1.500, 2.000, NullInterface.ideal());

	public final String backgroundSTLFiles[] = {
			"/work/ipp/w7x/cad/passive/bg-targetting/pumpslot-m4.off-aek41-cut.stl",
			"/work/ipp/w7x/cad/passive/bg-targetting/target-m4.off-aek41-cut.stl"
	};

	
	public String getDesignName() { return "aek41-pelletsK41";	}
}
