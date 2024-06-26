package ipp.w7x.fusionOptics.w7x.augSpec;

import java.util.List;

import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Intersection;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;
import net.jafama.FastMath;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;

public class AugSpecDev {
	public static AugSpec4 sys = new AugSpec4();
	public static Surface mustHitToDraw = sys.ccd;
	public static double traceWavelength = sys.designWavelenth;
	public static int nAttempts = 500;
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/augSpec/";
	
	public static double slitWidth = 100e-6;
	
	public static void main(String[] args) {
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/augSpecTrace.vrml", 0.010);
		
		//if((writeWRLForDesigner == null)){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		//}
		vrmlOut.setSkipRays(nAttempts*sys.nFibres / 5000);
			
		double col[][] = ColorMaps.jet(sys.nFibres);
			
		for(int iF=0; iF < sys.nFibres; iF+=5){
			
			int nHits = 0;
			for(int i=0; i < nAttempts; i++){
						
				double startPos[] = sys.fibrePos[iF];
				double fibresXVec[] = sys.globalUp;
				double fibresYVec[] = Util.cross(sys.inputAxis, sys.globalUp);
				
				double x, y, rMax = sys.fibreDiameter / 2;
				double yMax = Math.min(rMax, slitWidth/2);
				do{
					x = RandomManager.instance().nextUniform(-rMax, rMax);
					y = RandomManager.instance().nextUniform(-yMax, yMax);				
				}while(FastMath.sqrt(x*x + y*y) > rMax);
									
				RaySegment ray = new RaySegment();
				ray.startPos = Util.plus(startPos, 
										Util.plus(
												Util.mul(fibresXVec, x),
												Util.mul(fibresYVec, y)
											));
				
				//generate ray from fibre (using it's direction and NA)
				double nV[] = Util.mul(sys.inputAxis, -1.0);
				double aV[] = fibresXVec;
				double bV[] = fibresYVec;
				
				double sinMaxTheta = sys.fibreEffectiveNA;
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
				ray.wavelength = traceWavelength;
				ray.E0 = new double[][]{{1,0,0,0}};
				ray.up = Util.createPerp(ray.dir);
						
				Tracer.trace(sys, ray, 100, 0, false);
	
				if(mustHitToDraw == null){
					vrmlOut.drawRay(ray, col[iF]);
					nHits++;
				}else{
					List<Intersection> hits = ray.getIntersections(mustHitToDraw);
					if(hits.size() > 0){
						vrmlOut.drawRay(ray, col[iF]);
						nHits++;
					}
				}					
			}
			System.out.println(iF + ": " + nHits + "/" + nAttempts);
		}
		
		vrmlOut.drawOptic(sys);
		vrmlOut.destroy();
	}
}
