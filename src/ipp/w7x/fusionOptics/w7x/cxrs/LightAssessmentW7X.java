package ipp.w7x.fusionOptics.w7x.cxrs;

import ipp.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.neutralBeams.W7xNBI;
import jafama.FastMath;

import java.util.ArrayList;
import java.util.List;

import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.ColorMaps;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.HitPositionAverage;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Triangle;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Basic pictures for BeamEmissSpecAET21 model */
public class LightAssessmentW7X {
	
	public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	public static Surface mustHitToDraw = sys.fibrePlane;
	//public static BeamEmissSpecAET21 sys = new BeamEmissSpecAET21();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	//public static BeamEmissSpecAEM21 sys = new BeamEmissSpecAEM21();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	public static List<Surface> interestedSurfaces = new ArrayList<Surface>();

	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static Surface mustHitToDraw = sys.entryWindowFront;
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	//public final static double R0 = 5.2, R1 = 5.9; //as sightlines in fromDesigner-201511076 
	public final static int nPoints = sys.R.length;//as sightlines in fromDesigner-201511076
	
	// For fast drawing/debugging
	//public final static int nPoints = 10;
	//public final static double R0 = 5.2;  
	//public final static double R1 = 5.9;
	public final static int nAttempts = 5000;
	//*/
	
	// For calc
	/*public final static int nPoints = 20;
	public final static double R0 = 5.3;
	public final static double R1 = 6.1;
	public final static int nAttempts = 5000;
	public final static int beamSelect = -1;
	//*/
	
	public static boolean writeWRLForDesigner = false;

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName();
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static void main(String[] args) {
				
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/lightAssess-"+sys.getDesignName()+ (writeWRLForDesigner ? ".wrl" : ".vrml"), 1.005);
		if(!writeWRLForDesigner){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		vrmlOut.setSkipRays(nAttempts*nPoints / 200000);
		double col[][] = ColorMaps.jet(nPoints);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4);
		
		
		for(Surface s : sys.getSurfacesAll()){
			if(!(s instanceof Triangle))
				interestedSurfaces.add(s);
		}
		
		double los[][][] = new double[nPoints][2][];
		for(int iP=0; iP < nPoints; iP++){
			
			//double R = R0 + iP * (R1 - R0) / (nPoints - 1.0);
			double R = sys.R[iP];
			//double startPos[] = beams.getPosOfBoxAxisAtR(1, R);
			double startPos[] = beams.getPosOfBeamAxisAtR(beams.BEAM_Q8, R);
			R = FastMath.sqrt(startPos[0]*startPos[0] + startPos[1]*startPos[1]);
			int nHit = 0;
			
			los[iP][1] = new double[3];
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
					Intersection mirrorHit = hits.get(0).incidentRay.findFirstEarlierIntersection(sys.mirror);
					los[iP][1][0] += mirrorHit.pos[0];
					los[iP][1][1] += mirrorHit.pos[1];
					los[iP][1][2] += mirrorHit.pos[2];
					
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
			//intensityInfo.dump(interestedSurfaces, null, true, 0, 0);
			System.out.println("SR = " + solidAngleFP*1e6 + " µSR");
			intensityInfo.reset();
			
			los[iP][0] = startPos;
			los[iP][1][0] /= nHit;
			los[iP][1][1] /= nHit;
			los[iP][1][2] /= nHit;
			
			
		}
	
		/* virtual observation calc
		for(int iP=0; iP < nPoints; iP++){
			Cylinder losCyld = new Cylinder("los" + iP, 
					Util.mul(Util.plus(los[iP][0], los[iP][1]), 0.5),
					Util.reNorm(Util.minus(los[iP][1], los[iP][0])),
					0.005, 5.0, NullInterface.ideal());
			
			sys.addElement(losCyld);
		}
		
		double dL0[] = Util.reNorm(Util.minus(los[0][1], los[0][0]));
		double dLN[] = Util.reNorm(Util.minus(los[nPoints-1][1], los[nPoints-1][0]));
		
		double a = Algorithms.pointOnLineNearestAnotherLine(los[0][0], dL0, los[nPoints-1][0], dLN);
		double p0[] = Util.plus(los[0][0], Util.mul(dL0, a));
		a = Algorithms.pointOnLineNearestAnotherLine(los[nPoints-1][0], dLN, los[0][0], dL0);
		double pN[] = Util.plus(los[nPoints-1][0], Util.mul(dLN, a));
		double p[] = Util.mul(Util.plus(p0, pN), 0.5);
		
		System.out.print("Virtual obs pos: "); OneLiners.dumpArray(p);
		Sphere virtObs = new Sphere("virtObs", p, 0.02, NullInterface.ideal());
		sys.addElement(virtObs);
		*/
		
		lightInfoOut.close();
				
		if( ((Object)sys) instanceof BeamEmissSpecAEW21) 
			sys.removeElement(((BeamEmissSpecAEW21)(Object)sys).shieldTiles);
			
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
}
