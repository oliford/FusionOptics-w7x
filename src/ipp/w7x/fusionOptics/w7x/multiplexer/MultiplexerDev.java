package ipp.w7x.fusionOptics.w7x.multiplexer;

import java.util.List;

import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.CyldDish;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Intersection;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;
import ipp.w7x.fusionOptics.w7x.augSpec.AugSpec4;
import net.jafama.FastMath;
import oneLiners.OneLiners;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;

public class MultiplexerDev {
	public static Multiplexer sys = new Multiplexer();

	public static Surface mustHitToDraw = sys.mirror;
	public static double traceWavelength = sys.designWavelength;
	public static int nAttempts = 200000;
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/multiplexer/";
	
	public static void main(String[] args) {
		System.out.println(outPath);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/augSpecTrace.vrml", 0.010);
		
		//if((writeWRLForDesigner == null)){
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		//
		
		vrmlOut.setSkipRays(nAttempts / 5000);

		double col[][] = ColorMaps.jet(nAttempts);
		
		//find average position and vector of all rays reaching fibre or it's iris,
		//for finding best pos/angle of fibre
		double avgPosAtSurface[] = new double[3];
		double avgVecAtSurface[] = new double[3];

		int nHits = 0; //hitting fibre or iris
		int nHitsInFibre = 0; //hitting fibre
		int nRaysInFibre = 0; //hitting fibre and inside it's NA
		for(int i=0; i < nAttempts; i++){
					
			double startPos[] = sys.commonFibrePos.clone();
			double fibresXVec[] = { 1, 0, 0 };
			double fibresYVec[] = { 0, 1, 0 };
			
			double x, y, rMax = sys.fibreDiameter / 2;			
			do{
				x = RandomManager.instance().nextUniform(-rMax, rMax);
				y = RandomManager.instance().nextUniform(-rMax, rMax);				
			}while(FastMath.sqrt(x*x + y*y) > rMax);
								
			RaySegment ray = new RaySegment();
			ray.startPos = Util.plus(startPos, 
									Util.plus(
											Util.mul(fibresXVec, x),
											Util.mul(fibresYVec, y)
										));
			
			//generate ray from fibre (using it's direction and NA)
			double nV[] = Util.mul(sys.up, 1.0);
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

			
			List<Intersection> hits = ray.getIntersections(sys.inputFibreEnds[0]);
			if(hits.size() > 0) {
				nHitsInFibre++;
				double cosAngle = FastMath.abs(Util.dot(hits.get(0).normal, hits.get(0).incidentRay.dir));
				double angle = FastMath.acos(cosAngle);
				if(angle < FastMath.atan(sys.fibreEffectiveNA)) {
					nRaysInFibre++;
				}
			}
			hits.addAll(ray.getIntersections(sys.fibreEndIris[0]));
			if(hits.size() > 0) {
				nHits++;
				for(int j=0;j < 3; j++) {
					avgPosAtSurface[j] += hits.get(0).pos[j];
					avgVecAtSurface[j] += hits.get(0).incidentRay.dir[j];
				}
			}
			
			if(mustHitToDraw == null){
				vrmlOut.drawRay(ray, col[i]);
			}else{
				List<Intersection> hits2 = ray.getIntersections(mustHitToDraw);
				if(hits2.size() > 0){
					vrmlOut.drawRay(ray, col[i]);
				}
			}					
		}
		
		System.out.println("Near fibre = " + nHits + "/" + nAttempts);
		System.out.println("At fibre = " + nHitsInFibre + "/" + nAttempts 
							+ " = " + ((int)(100*nHitsInFibre/nAttempts)) + "%");
		System.out.println("In fibre = " + nRaysInFibre + "/" + nAttempts 
				+ " = " + ((int)(100*nRaysInFibre/nAttempts)) + "%");

		avgPosAtSurface = Util.mul(avgPosAtSurface, 1.0/nHits);
		avgVecAtSurface = Util.reNorm(avgVecAtSurface);
		double angle = FastMath.atan(avgVecAtSurface[0]/avgVecAtSurface[2]);
		
		OneLiners.dumpArray(avgPosAtSurface);
		OneLiners.dumpArray(avgVecAtSurface);
		System.out.println(angle*180/Math.PI);
		vrmlOut.drawElement(new Cylinder("inputCalc",avgPosAtSurface,avgVecAtSurface,sys.fibreDiameter/2,0.500,NullInterface.ideal()));
	
		vrmlOut.drawOptic(sys);
		vrmlOut.destroy();
	}
}
