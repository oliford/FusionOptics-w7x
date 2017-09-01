package ipp.w7x.fusionOptics.w7x.cxrs;

import jafama.FastMath;

import java.util.List;

import otherSupport.ColorMaps;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.optics.STLMesh;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Square;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Intersection;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposure {
	
	public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	
	
	
	
	public static double[] radSurfaceCentre = { 1.68544196,  5.88435327,  0.43004889 };
	public static double[] radSurfaceNormal = { -0.2822023 , -0.85883142, -0.42751662 };
	public static double[] radUp = Util.createPerp(radSurfaceNormal);
	public static double radSurfWidth = 1.100;
	public static double radSurfHeight = 0.900;
	
	//public static double a[] = Util.plus(radSurfaceCentre, Util.mul(radSurfaceNormal, -0.300)); 
	//public static Surface testSurface = new Disc("testdisc", a, radSurfaceNormal, 0.050, Absorber.ideal()); 
	
	public static Surface testSurface = sys.entryWindowFront;
			
	public static String[] thingsInWay = {		
		"/work/ipp/w7x/cad/aea21/radExposure/arms.stl",
		"/work/ipp/w7x/cad/aea21/radExposure/frontPlate.stl",
		"/work/ipp/w7x/cad/aea21/radExposure/mirrorBlock.stl",
		"/work/ipp/w7x/cad/aea21/radExposure/reducePlate.stl"
	};
	
	public static int nRaysPerPoint = 100;
	public static int nSkip = 10;
	
	public static Square radSurface = new Square("radSurface", radSurfaceCentre, radSurfaceNormal, radUp, radSurfHeight, radSurfWidth, NullInterface.ideal()); 
	
	public final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure/";
	
	/** Power emitted from radiating surface, Watts per square meter per Steradian */
	public static double powerDensity = 100e3 / 2 / Math.PI;  //100kW/m2
	
	public static void main(String[] args) { 
		System.out.println(outPath);
		
		sys.addElement(testSurface);
		
		
		int nX = 30, nY= 30;
		double dx = radSurfWidth / (nX - 1);
		double dy = radSurfHeight / (nY - 1);
			
		double col[][] = ColorMaps.jet(nX*nY);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace-"+sys.getDesignName()+".vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		vrmlOut.setSkipRays(nSkip);
		
		testSurface.setInterface(Absorber.ideal());
		
		sys.addElement(radSurface);
		vrmlOut.drawOptic(sys);
		
		for(String fileName : thingsInWay){
			String parts[] = fileName.split("/");			
			sys.addElement(new STLMesh("bg_"+parts[parts.length-1], fileName));			
		}
		
		double totalPower = 0;
		
		for(int iY = 0; iY < nY; iY++){
			double y = -radSurfHeight/2 + iY * dy;
			
			for(int iX = 0; iX < nX; iX++){
				double x = -radSurfWidth/2 + iX * dx;
				
				double startPos[] = Util.plus(radSurfaceCentre, 
										Util.plus(
												Util.mul(radSurface.getRight(), x),
												Util.mul(radSurface.getUp(), y) ));
				
				
				double solidAngle = Double.NaN;				
				int nHit = 0;
				
				for(int iR=0; iR < nRaysPerPoint; iR++){

					RaySegment ray = new RaySegment();
					ray.startPos = startPos;
					
					ray.dir = Tracer.generateRandomRayTowardSurface(startPos, testSurface, true);
					solidAngle = Util.length(ray.dir);
					ray.dir = Util.reNorm(ray.dir);
							
					//ray.dir = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget);
					ray.wavelength = 500e-9; //irrelevant
					ray.E0 = new double[][]{{1,0,0,0}};
					ray.up = Util.createPerp(ray.dir);
							
					Tracer.trace(sys, ray, 100, 0, false);
						
					List<Intersection> hits = ray.getIntersections(testSurface);
					if(hits.size() > 0){
						nHit++;
						vrmlOut.drawRay(ray, col[iY*nX+iX]);
					}
					
					Pol.recoverAll();
				}
				
				double power = powerDensity * dx * dy * solidAngle * ((double)nHit / nRaysPerPoint); 
				totalPower += power;
				System.out.println(iY + ", "+ iX+": " + nHit + " / " + nRaysPerPoint + " = " + power + "W, SR = " + solidAngle);
					
				
			}
			
			
			
		}

		System.out.println("dx*dy = " + (dx*dy));
		double targetArea = Math.PI * FastMath.pow2(((Disc)testSurface).getRadius());
		System.out.println("Total = " + totalPower + " W --> " + (totalPower / targetArea) + " W/m2");
		
		
		vrmlOut.destroy();
	}
}
