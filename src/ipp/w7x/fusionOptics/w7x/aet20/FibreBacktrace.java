package ipp.w7x.fusionOptics.w7x.aet20;

import ipp.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import jafama.FastMath;

import java.util.List;

import oneLiners.OneLiners;
import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.ColorMaps;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Basic pictures for BeamEmissSpecAET21 model */
public class FibreBacktrace {
	
	//public static BeamEmissSpecAET21 sys = new BeamEmissSpecAET21();
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static BeamEmissSpecAEB20 sys = new BeamEmissSpecAEB20();
	//public static SimpleBeamGeometry beams = W7xNBI.def();

	public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	public static SimpleBeamGeometry beams = W7XRudix.def();
	 
	// For fast drawing/debugging
	//public final static int nPoints = 10;
	//public final static double R0 = 5.2;  
	//public final static double R1 = 5.9;
	public final static int nAttempts = 1000;
	//*/
	
	// For calc
	/*public final static int nPoints = 20;
	public final static double R0 = 5.3;
	public final static double R1 = 6.1;
	public final static int nAttempts = 5000;
	public final static int beamSelect = -1;
	//*/

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/fibreTrace";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static void main(String[] args) {
				
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace-"+sys.getDesignName()+".vrml", 1.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		vrmlOut.setSkipRays(nAttempts*sys.nFibres / 500);
		double col[][] = ColorMaps.jet(sys.nFibres);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4); 
				
		//Need to get through the fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());
		
		for(int iP=0; iP < sys.nFibres; iP++){
			
			
			double startPos[] = sys.fibreEndPos[iP];
			int nHit = 0;
			
			double sumI=0, sumIR=0;
			for(int i=0; i < nAttempts; i++){
				RaySegment ray = new RaySegment();
				ray.startPos = startPos;
				ray.dir = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget);
				ray.wavelength = sys.designWavelenth;
				ray.E0 = new double[][]{{1,0,0,0}};
				ray.up = Util.createPerp(ray.dir);
						
				Tracer.trace(sys, ray, 100, 0, false);
				
				ray.processIntersections(null, intensityInfo);
								
				List<Intersection> hits = ray.getIntersections(sys.beamPlane);
				if(hits.size() > 0){
					double p[] = hits.get(0).pos;
					double R = FastMath.sqrt(p[0]*p[0] + p[1]*p[1]);
					sumI += 1;
					sumIR += R * 1;
					
					
					nHit++;
				}
				vrmlOut.drawRay(ray, col[iP]);
				
				Pol.recoverAll();
			}
			double R = sumIR / sumI;
			
			double dir[] = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget, true);
			double targetSolidAngle = Util.length(dir);
			
			double solidAngleFP = intensityInfo.getSourceSolidAng(sys.fibrePlane, targetSolidAngle, nAttempts); 
			lightInfoOut.writeRow(-1, iP, R, solidAngleFP);
			
			System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
			System.out.println("P=" + iP + "(R=" + R + "):\t " + nHit + " of " + nAttempts + " attempts hit and have been drawn");
			//intensityInfo.dump();
			System.out.println("SR = " + solidAngleFP*1e6 + " µSR");
			intensityInfo.reset();
			
			
		}
	
		
		lightInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
}
