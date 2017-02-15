package ipp.w7x.fusionOptics.w7x.bremstrahlung;

import jafama.FastMath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.drawing.SVGRayDrawing;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.optimisationMulti.MoveableElement;
import fusionOptics.optimisationMulti.OptimiseMulti;
import fusionOptics.optimisationMulti.RayBundle;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Square;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import oneLiners.OneLiners;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;

public class FibresBacktrace {

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/thomson/";
	
	public static boolean writeWRLForCATIA = false;
	
	final static int nAttempts = 100;											// number of rays from every source
	final static double wavelen = 850e-9;
	
	static AEN31AsDesignInPlace lens = new AEN31AsDesignInPlace();	
	
	
	static double f[][]  = lens.bremstrahlungFibres; //new double[][]{ lens.bremstrahlungFibres[0] };
	//final static String elementsFileName = new String("/home/andrea/Downloads/component_389.elements");
	//final static String nodesFileName = new String("/home/andrea/Downloads/component_389.nodes");

	public static void main(String[] args) throws IOException {
		
		// **************** create the optical system *****************
		Square imgPlane = new Square("imgPlane", new double[]{ 0.46, 0, 0 }, 	// define target plane
				 new double[]{ -1, 0, 0 },
				 new double[]{ 0, 1, 0 }, 
				 0.0032, 0.0011, NullInterface.ideal());
		
		//Optic wall = new Optic("wall");
		//MyUtil.AddTriangles(wall, elementsFileName, nodesFileName);
		Optic all = new Optic("all", new Element[]{ lens, imgPlane, /*wall*/ });	// build the optical system
		
				
		Square groundPlane = new Square("groundPlane",
				Util.plus(lens.frontLensFront, Util.mul(lens.opticAxis, -3.5)),
				new double[]{ 0,0,1 }, new double[]{ 1,0,0 }, 5, 5, Absorber.ideal());
		
		all.addElement(groundPlane);
	
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace" + (writeWRLForCATIA ? ".vrml" : ".wrl"), 0.01);
		if(!writeWRLForCATIA){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		vrmlOut.setDrawPolarisationFrames(false);
		
		double col[][] = ColorMaps.jet(f.length);
		double fibreEffectiveNA = 0.22;
		double fibreEndDiameter = 0.000400;
					
		//for(int iF=0; iF < f.length; iF++){
		for(int iF : new int[]{ 0,  3,  6,  9, 12, 14, 17, 20, 23, 26 }){
			int nHit = 0;
			for(int iR=0; iR < nAttempts; iR++){
				//generate ray from fibre (using it's direction and NA)
				double nV[] = lens.fibrePlaneNorm;
				double aV[] = lens.fibrePlaneUp;
				double bV[] = lens.fibrePlaneRight;
				
				double x, y, rMax = fibreEndDiameter / 2;
				do{
					x = RandomManager.instance().nextUniform(-rMax, rMax);
					y = RandomManager.instance().nextUniform(-rMax, rMax);				
				}while(FastMath.sqrt(x*x + y*y) > rMax);
									
				RaySegment ray = new RaySegment();
				ray.startPos = Util.plus(f[iF], 
										Util.plus(
												Util.mul(aV, x),
												Util.mul(bV, y)
											));				
				
				double sinMaxTheta = fibreEffectiveNA; //sys.fibreNA;
				double cosMaxTheta = FastMath.cos(FastMath.asin(sinMaxTheta)); //probably just 1-sinTheta, but... meh
				
				double cosTheta = 1 - RandomManager.instance().nextUniform(0, 1) * (1 - cosMaxTheta);
				double sinTheta = FastMath.sqrt(1 - cosTheta*cosTheta);
				
				double phi = RandomManager.instance().nextUniform(0, 1) * 2 * Math.PI;
				
				//generate in coord sys (a,b,c) with c as axis toward target 
				double a = sinTheta * FastMath.cos(phi);
				double b = sinTheta * FastMath.sin(phi);
				double c = cosTheta;
				
				ray.dir = Util.plus(Util.plus(Util.mul(aV, a), Util.mul(bV, b)), Util.mul(nV, -c));
						
				//ray.dir = Tracer.generateRandomRayTowardSurface(ray.startPos, lens.lens1Front);
				ray.E0 = new double[][]{{1,0,0,0}};
				ray.up = Util.createPerp(ray.dir);
				ray.wavelength = wavelen;
				
				Tracer.trace(all, ray, 30, 0.01, true);
				vrmlOut.drawRay(ray, col[iF]);
				
				List<Intersection> hits = ray.getIntersections(groundPlane);
				if(!hits.isEmpty()){
					//vrmlOut.drawRay(ray, col[iF]);
					nHit++;
				}
			}
			System.out.println(iF + ": " + nHit + " / " + nAttempts);
		}
		
		System.out.println("Done");
		
		vrmlOut.drawOptic(all);
		vrmlOut.destroy();
		
	}
}