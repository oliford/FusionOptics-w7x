package ipp.w7x.fusionOptics.w7x.aet20;

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
	
	public static BeamEmissSpecAET21 sys = new BeamEmissSpecAET21();
	public static Surface mustHitToDraw = sys.entryWindowFront;
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static Surface mustHitToDraw = sys.mirror;
	
	// For fast drawing/debugging
	public final static int nPoints = 5;
	public final static double R0 = 5.5;
	public final static double R1 = 6.0;
	public final static int nAttempts = 1000;
	public final static int beamSelect = 7; //7 = Q8 = lower, more tangential (away from the other beams, towards from AET21 observation port)
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
				
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/"+sys.getDesignName()+".vrml", 1.005);
		vrmlOut.setRotationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		vrmlOut.setSkipRays(nAttempts*nPoints / 500);
		double col[][] = ColorMaps.jet(nPoints);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4); 
		
		for(int beamIdx=4; beamIdx < 8; beamIdx++){
			if(beamSelect >= 0 && beamSelect != beamIdx)
				continue;
			
			for(int iP=0; iP < nPoints; iP++){
				
				double R = R0 + iP * (R1 - R0) / (nPoints - 1.0);
				double startPos[] = W7XBeamDefsSimple.getPosOfBeamAxisAtR(beamIdx, R);
				
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
				lightInfoOut.writeRow(beamIdx, iP, R, solidAngleFP);
				
				System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
				System.out.println("Q" + (beamIdx+1) + "\tiP=" + iP + "(R=" + R + "):\t " + nHit + " of " + nAttempts + " attempts hit " + mustHitToDraw.getName() + " and have been drawn");
				//intensityInfo.dump();
				System.out.println("SR = " + solidAngleFP*1e6 + " µSR");
				intensityInfo.reset();
				
				
			}
		}
		
		lightInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
}
