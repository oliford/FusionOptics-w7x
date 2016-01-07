package ipp.w7x.fusionOptics.w7x.cxrs;

import ipp.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import jafama.FastMath;

import java.util.List;

import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.ColorMaps;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Basic pictures for BeamEmissSpecAET21 model */
public class LightAssessmentW7X {
	
	//public static BeamEmissSpecAET21 sys = new BeamEmissSpecAET21();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	public static BeamEmissSpecAEB20 sys = new BeamEmissSpecAEB20();
	public static Surface mustHitToDraw = sys.fibrePlane;
	public static SimpleBeamGeometry beams = W7xNBI.def();

	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static Surface mustHitToDraw = sys.entryWindowFront;
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	//public final static double R0 = 5.2, R1 = 5.9; //as sightlines in fromDesigner-201511076 
	public final static int nPoints = sys.R.length;//as sightlines in fromDesigner-201511076
	
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

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName();
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static void main(String[] args) {
				
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/lightAssess-"+sys.getDesignName()+".vrml", 1.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		vrmlOut.setSkipRays(nAttempts*nPoints / 20000);
		double col[][] = ColorMaps.jet(nPoints);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4); 
		
		for(int iP=0; iP < nPoints; iP++){
			
			//double R = R0 + iP * (R1 - R0) / (nPoints - 1.0);
			double R = sys.R[iP];
			double startPos[] = beams.getPosOfBoxAxisAtR(1, R);
			R = FastMath.sqrt(startPos[0]*startPos[0] + startPos[1]*startPos[1]);
			int nHit = 0;
			
			for(int i=0; i < nAttempts; i++){
				RaySegment ray = new RaySegment();
				ray.startPos = startPos;
				ray.dir = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget);
				ray.wavelength = sys.designWavelenth;
				ray.E0 = new double[][]{{1,0,0,0}};
				ray.up = Util.createPerp(ray.dir);
						
				Tracer.trace(sys, ray, 100, 0, false);
				
				ray.processIntersections(null, intensityInfo);
								
				List<Intersection> hits = ray.getIntersections(mustHitToDraw);
				if(hits.size() > 0){
					vrmlOut.drawRay(ray, col[iP]);
					nHit++;
				}
			}
			
			double dir[] = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget, true);
			double targetSolidAngle = Util.length(dir);
			
			double solidAngleFP = intensityInfo.getSourceSolidAng(sys.fibrePlane, targetSolidAngle, nAttempts); 
			lightInfoOut.writeRow(-1, iP, R, solidAngleFP);
			
			System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
			System.out.println("P=" + iP + "(R=" + R + "):\t " + nHit + " of " + nAttempts + " attempts hit " + mustHitToDraw.getName() + " and have been drawn");
			intensityInfo.dump();
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
