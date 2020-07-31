package ipp.w7x.fusionOptics.w7x.bremstrahlung;

import net.jafama.FastMath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import algorithmrepository.Algorithms;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.HitPositionAverage;
import fusionOptics.collection.IntersectionProcessor;
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
	
	static AEN31AsDesignInPlace sys = new AEN31AsDesignInPlace();	
	
	
	static double f[][]  = sys.bremstrahlungFibres; //new double[][]{ lens.bremstrahlungFibres[0] };
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
		Optic all = new Optic("all", new Element[]{ sys, imgPlane, /*wall*/ });	// build the optical system
		
				
		Square testPlane = new Square("groundPlane",
				Util.plus(sys.frontLensFront, Util.mul(sys.opticAxis, -3.0)),
				new double[]{ 0,0,1 }, new double[]{ 1,0,0 }, 5, 5, Absorber.ideal());
		
		all.addElement(testPlane);
	
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace" + (writeWRLForCATIA ? ".vrml" : ".wrl"), 0.01);
		if(!writeWRLForCATIA){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		vrmlOut.setDrawPolarisationFrames(false);
		
		double col[][] = ColorMaps.jet(f.length);
		double fibreEffectiveNA = 0.22;
		double fibreEndDiameter = 0.000400;
		
		double windowPoints[][] = new double[f.length][];
		double testPoints[][] = new double[f.length][];
		double radius[] = new double[f.length];
							
		for(int iF=0; iF < f.length; iF++){
		//for(int iF : new int[]{ 0,  3,  6,  9, 12, 14, 17, 20, 23, 26 }){
			
			HitPositionAverage windowHits = new HitPositionAverage();
			HitPositionAverage testHits = new HitPositionAverage();
			
			for(int iR=0; iR < nAttempts; iR++){
				//generate ray from fibre (using it's direction and NA)
				double nV[] = sys.fibrePlaneNorm;
				double aV[] = sys.fibrePlaneUp;
				double bV[] = sys.fibrePlaneRight;
				
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
				
				ray.processIntersections(testPlane, testHits);
				ray.processIntersections(sys.windowFront, windowHits);
				
			}
			System.out.println(iF + ": " + windowHits.sumI + " / " + nAttempts);
			windowPoints[iF] = sys.windowFront.planeRUToPosXYZ(windowHits.getMeanPosRU());
			testPoints[iF] = testPlane.planeRUToPosXYZ(testHits.getMeanPosRU());
			//radius as average of RMS spot size at window and at test plane
			radius[iF] = FastMath.sqrt(FastMath.pow2(windowHits.getSigmaRR()) + FastMath.pow2(windowHits.getSigmaUU()))
						+ FastMath.sqrt(FastMath.pow2(testHits.getSigmaRR()) + FastMath.pow2(testHits.getSigmaUU())) / 2;
		}
		
		System.out.println("Done");
		

		PrintStream textOut = new PrintStream(outPath + "/info.txt");
		//spit out build commands and LOS definitions in blocks
		for(Thing j : Thing.values()) {
			for(int iF=0; iF < sys.bremstrahlungFibres.length; iF++){				
				outputInfo(System.out, windowPoints, testPoints, radius, iF, j, false);	
				outputInfo(textOut, windowPoints, testPoints, radius, iF, j, false);					
				
			}
		}
		textOut.close();
		
		//output JSON LOS info
		PrintStream jsonOut = new PrintStream(outPath + "/lineOfSightDefs-"+sys.lightPathsSystemName+".json");
		jsonOut.println("{ \"system\" : \""+sys.lightPathsSystemName+"\", \"info\" : \"From raytracer "+sys.getDesignName()+" on "+
				((new SimpleDateFormat()).format(new Date()))+" \", \"los\" : [");
		for(int iF=0; iF < sys.bremstrahlungFibres.length; iF++){	
			boolean isLast = (iF == sys.bremstrahlungFibres.length-1);
			outputInfo(jsonOut, windowPoints, testPoints, radius, iF, Thing.JSON_LOS, isLast);				
			
		}
		jsonOut.println("]}");
		
		vrmlOut.drawOptic(all);
		vrmlOut.destroy();
		
	}
	

	public static double losCyldRadius = 0.005;
	/**
	 * @param stream
	 * @param startPoints
	 * @param testPoints
	 * @param iF
	 * @param thing
	 * @param supressComma
	 */
	private static enum Thing { FreeCADHitPos, FreeCADLOS, JSON_LOS };
	private static void outputInfo(PrintStream stream, double startPoints[][], double testPoints[][], double radius[], int iF, Thing thing, boolean supressComma){

		double extendLOSCylds = 0.400; // extend 200mm in each direction

		double u[] = Util.reNorm(Util.minus(testPoints[iF], startPoints[iF]));
		double losLen = Util.length(Util.minus(testPoints[iF], startPoints[iF]));
		
		//double start[] = sys.lens1.getBackSurface().getCentre();
		double uVec[] = Util.reNorm(Util.minus(testPoints[iF], startPoints[iF]));
		String chanName = sys.lightPathsSystemName 
				+ ":" + String.format("%02d", iF+1);
	
		double p[] = Util.minus(startPoints[iF], Util.mul(u, extendLOSCylds/2));
		
		switch(thing){
			case FreeCADHitPos:		
				stream.println("Part.show(Part.makeSphere("+radius[iF]*1e3+",FreeCAD.Vector("+testPoints[iF][0]*1e3+","+testPoints[iF][1]*1e3+","+testPoints[iF][2]*1e3 + ")));"
						+ " FreeCAD.ActiveDocument.ActiveObject.Label=\"beamApproach_"+sys.getDesignName()+"_"+chanName+"\";");
				break;
				
			case FreeCADLOS:
				stream.println("Part.show(Part.makeCylinder("+losCyldRadius*1e3+","+(losLen + extendLOSCylds)*1e3 +","										
						+"FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3+"), "
						+"FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+ "))); FreeCAD.ActiveDocument.ActiveObject.Label=\"los_"+sys.getDesignName()+"_"+chanName+"\";");
				break;
				
			case JSON_LOS:
				stream.print("{ \"id\" : \"" + chanName
						+ "\", \"start\":[ " + String.format("%7.5g", startPoints[iF][0]) + ", " + String.format("%7.5g", startPoints[iF][1]) + ", " + String.format("%7.5g", startPoints[iF][2]) + "]"
						+ ", \"uVec\":[ " + String.format("%7.5g", uVec[0]) + ", " + String.format("%7.5g", uVec[1]) + ", " + String.format("%7.5g", uVec[2]) + "]"
						+ ", \"rmsRadius\": " + String.format("%7.5g", radius[iF]) + "");
				
						
				stream.println(", \"testPlaneHit\":[ "+ String.format("%7.5g", testPoints[iF][0]) 
									+ ", " + String.format("%7.5g", testPoints[iF][1]) 
									+ ", " + String.format("%7.5g", testPoints[iF][2]) + "]"
								+ "}" + (supressComma ? "" : ", ")
						);
				break;
		}
	}
}