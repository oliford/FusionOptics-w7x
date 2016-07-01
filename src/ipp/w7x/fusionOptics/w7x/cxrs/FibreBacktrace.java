package ipp.w7x.fusionOptics.w7x.cxrs;

import ipp.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEM41;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import jafama.FastMath;

import java.util.List;

import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;
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
	
	public static double fibreEffectiveNA = 0.125; //0.28; //f/4 = 0.124, f/6=0.083
	 
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
		//vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		vrmlOut.setSkipRays(nAttempts*sys.nFibres / 500);
		double col[][] = ColorMaps.jet(sys.nFibres);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4); 
				
		//Need to get through the fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());
		
		for(int iP=0; iP < sys.nFibres; iP++){
			
			
			int nHit = 0;
			
			double startPos[] = sys.fibreEndPos[iP];
			
			double sumI=0, sumIR=0, sumIR2 = 0;
			for(int i=0; i < nAttempts; i++){
				double x, y, rMax = sys.fibreEndDiameter / 2;
				do{
					x = RandomManager.instance().nextUniform(-rMax, rMax);
					y = RandomManager.instance().nextUniform(-rMax, rMax);				
				}while(FastMath.sqrt(x*x + y*y) > rMax);
				
				
				RaySegment ray = new RaySegment();
				ray.startPos = Util.plus(startPos, 
										Util.plus(
												Util.mul(sys.fibresXVec, x),
												Util.mul(sys.fibresYVec, y)
											));
				
				//generate ray from fibre (using it's direction and NA)
				double nV[] = sys.fibrePlanes[iP].getNormal();
				double aV[] = sys.fibrePlanes[iP].getUp();
				double bV[] = sys.fibrePlanes[iP].getRight();
				
				double sinMaxTheta = fibreEffectiveNA; //sys.fibreNA;
				double cosMaxTheta = FastMath.cos(FastMath.asin(sinMaxTheta)); //probably just 1-sinTheta, but... meh
				
				double cosTheta = 1 - RandomManager.instance().nextUniform(0, 1) * (1 - cosMaxTheta);
				double sinTheta = FastMath.sqrt(1 - cosTheta*cosTheta);
				
				double phi = RandomManager.instance().nextUniform(0, 1) * 2 * Math.PI;
				
				//generate in coord sys (a,b,c) with c as axis toward target 
				double a = sinTheta * FastMath.cos(phi);
				double b = sinTheta * FastMath.sin(phi);
				double c = cosTheta;
				
				ray.dir = Util.plus(Util.plus(Util.mul(aV, a), Util.mul(bV, b)), Util.mul(nV, c));
						
				//ray.dir = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget);
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
					sumIR2 += R * R * 1;

					vrmlOut.drawRay(ray, col[iP]);
					
					nHit++;
				}
				
				Pol.recoverAll();
			}
			double R = sumIR / sumI;
			
			double var = 1.0/sumI*(sumIR2 - sumIR*sumIR/sumI);
			double fwhmR = 2.35 * FastMath.sqrt(var);			
			
			lightInfoOut.writeRow(-1, iP, R, nHit / nAttempts);
			
			System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
			System.out.println("P=" + iP + "(R=" + R + ", fwhmR = " + fwhmR + "):\t " + nHit + " of " + nAttempts + " attempts hit and have been drawn");
			
			intensityInfo.reset();
		}
		
		
		//spit out FreeCAD instructions to create fibre end block cylinders
		double cyldLen = 0.030;
		double cyldRadius = 0.0025;
		
		for(int i=0; i < sys.nFibres; i++){
			 double u[] = sys.fibreEndNorm[i];
			 double p[] = Util.plus(sys.fibreEndPos[i], Util.mul(u, -cyldLen));
			 		
			System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Cylinder\", \"FibreEnd"+i+"\"); "+
						"o.Shape = Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
						",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 +
						"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360);");
			
		}
		
		lightInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
}
