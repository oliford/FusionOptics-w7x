package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_edgeVIS;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_obsolete;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_AsMeasured;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_imaging;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET20_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_asMeasuredOP12b;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEM41;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7XPelletsK41;
import ipp.w7x.neutralBeams.W7XPelletsL41;
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
import fusionOptics.drawing.STLDrawer;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.optics.STLMesh;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Sphere;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Backtrace fibres to find point on W7X wall that LOS hits
 * Modify something so that they fit the in-vessel measured positions
*/
public class BackgroundTargettingFit {
	
	//public static BeamEmissSpecAET21_postDesign sys = new BeamEmissSpecAET21_postDesign();	
	//public static BeamEmissSpecAET21_asMeasuredOP12b sys = new BeamEmissSpecAET21_asMeasuredOP12b();
	//public static BeamEmissSpecAET20_postDesign_LC3 sys = new BeamEmissSpecAET20_postDesign_LC3();
	
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(false);
	public static BeamEmissSpecAEM21_postDesign_AsMeasured sys = new BeamEmissSpecAEM21_postDesign_AsMeasured(false); //not in LC3
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	
	//public static BeamEmissSpecAEK21_edgeVIS sys = new BeamEmissSpecAEK21_edgeVIS();
	//public static SimpleBeamGeometry beams = EdgePenetrationAEK41.def();
	
	//public static BeamEmissSpecAEK21_edgeUV sys = new BeamEmissSpecAEK21_edgeUV();
	//public static SimpleBeamGeometry beams = EdgePenetrationAEK41.def();	
	
	//public static BeamEmissSpecAEK21_pelletsK41 sys = new BeamEmissSpecAEK21_pelletsK41();
	//public static SimpleBeamGeometry beams = W7XPelletsK41.def();
	
	//public static BeamEmissSpecAEK21_pelletsL41 sys = new BeamEmissSpecAEK21_pelletsL41(); //need baffels and a panel
	//public static SimpleBeamGeometry beams = W7XPelletsL41.def();
	
	public static double fibreEffectiveNA = 0.22; //0.28; //f/4 = 0.124, f/6=0.083
	 
	public final static int nAttempts = 10;

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/background/";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static double losCyldRadius = 0.005;
	public static Surface startSurface = sys.mirror;
	
	/** Fibres that were lit */
	public static String[] targetNames = {
		"AEM21_S7:15",
		"AEM21_S8:11",
		"AEM21_X1:08",
		"AEM21_S7:46",
		"AEM21_X2:01",
		"AEM21_S8:37",
		"AEM21_S7:33",
		"AEM21_S8:44",
		"AEM21_X2:09",
		"AEM21_S7:54",
		"AEM21_S8:54",
	};
	
	/** Measured target coordinates */	
	public static double[][] targetCoords = {
			{	0.9052531128, 	6.2926098633	, 	-0.3715768738	 },
			{	0.6772341309, 	6.3165771484	, 	-0.3224136963	 },
			{	0.4764190674, 	6.2223681641	, 	-0.3258862915	 },
			{	1.2293759766, 	5.758859375		, 	-0.8312332153	 },
			{	1.4617521973, 	5.8532197266	, 	-0.7356548462	 },
			{	0.8290787964,	5.9353950195	,	-0.7067048340	 },
			{	1.0874625244, 	6.0117651367	, 	-0.6087009277	 },
			{	0.8777481689, 	5.8131010742	, 	-0.8319437256	 },
			{	0.5899093628, 	5.8579711914	, 	-0.7600310059	 },
			{	0.4898992615, 	5.4723950195	, 	-0.0459667435	 },
			{	0.317631897, 	5.510652832	, 	-0.1408085785	 },
	};
		
	public static void main(String[] args) {
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace-"+sys.getDesignName()+".vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		int totalFibres = sys.channelR.length*sys.channelR[0].length;
		vrmlOut.setSkipRays(nAttempts*totalFibres / 5000);
		double col[][] = ColorMaps.jet(sys.channelR[0].length);
		
		double rad = 0.005;
		for(int iT=0; iT < targetCoords.length; iT++){
			System.out.println("Part.show(Part.makeSphere("+rad*1e3+",FreeCAD.Vector("+targetCoords[iT][0]*1e3+","+targetCoords[iT][1]*1e3+","+targetCoords[iT][2]*1e3 + ")));"
					+ " FreeCAD.ActiveDocument.ActiveObject.Label=\"measHit_" + targetNames[iT] + "\";");
		}
		
		BinaryMatrixWriter hitInfoOut = new BinaryMatrixWriter(outPath + "/hitInfo.bin", 12);
		
		Optic background = new Optic("background");
		for(String fileName : sys.backgroundSTLFiles){			
			String parts[] = fileName.split("/");
			System.out.print("Loading BG mesh " + fileName + "... ");
			background.addElement(new STLMesh("bg_"+parts[parts.length-1], fileName));
			System.out.println("OK");
		}
		Optic all = new Optic("all", new Element[]{ sys, background });
		//sys.addElement(background);		
				
		//Need to get through the fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());		
		sys.addElement(sys.beamPlane);
		
		double hitPoints[][] = new double[targetNames.length][];
		double startPoints[][] = new double[targetNames.length][];
		
		for(int iT=0; iT < targetNames.length; iT++){ //foreach target (fibre/point)
						
			int nHit = 0, nStray = 0;
			
			//find the fibre
			String parts[] = targetNames[iT].split(":");			
					
			int iB=-1;				
			for(int jB=0; jB < sys.channelR.length; jB++){
				if(parts[0].equalsIgnoreCase(sys.lightPathsSystemName + "_" + sys.lightPathRowName[jB])){
					iB = jB;
					break;
				}
			}
			if(iB<0)
				throw new RuntimeException("Couldn't find fibre set for target '"+parts[0]+"'");
			
			int iP = Integer.parseInt(parts[1]) - 1; 
			
			double startPos[] = sys.fibreEndPos[iB][iP];
			
			double sumI=0, sumIX[]={0,0,0}, sumIX2[] = {0,0,0};
			double sumIXsp[]={0,0,0};
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
				double nV[] = sys.fibrePlanes[iB][iP].getNormal();
				double aV[] = sys.fibrePlanes[iB][iP].getUp();
				double bV[] = sys.fibrePlanes[iB][iP].getRight();
				
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
				ray.wavelength = 530e-9; //sys.designWavelenth;
				ray.E0 = new double[][]{{1,0,0,0}};
				ray.up = Util.createPerp(ray.dir);
						
				Tracer.trace(all, ray, 100, 0, false);
									
				//vrmlOut.drawRay(ray, col[iP]);
				List<Intersection> hits = ray.getIntersections(background);
				if(hits.size() > 0){
					double p[] = hits.get(0).pos;
					double R = FastMath.sqrt(p[0]*p[0] + p[1]*p[1]);
					sumI += 1;
					for(int j=0; j < 3; j++){
						sumIX[j] += p[j];
						sumIX2[j] += p[j]*p[j];
					}
					
					Intersection startSurfaceHit = hits.get(0).incidentRay.findFirstEarlierIntersection(startSurface);						
					for(int j=0; j < 3; j++){
						sumIXsp[j] += startSurfaceHit.pos[j];						
					}
					
					nHit++;
				}
				
				//if(ray.getIntersections(sys.strayPlane).size() > 0){
				//	nStray++;
				//}
				
				//if(ray.getIntersections(sys.strayPlane).size() > 0){					
					vrmlOut.drawRay(ray, col[iP]); //stray light						
				//}
				
				Pol.recoverAll();
			}
			double p[] = new double[3];
			double var = 0;
			for(int j=0; j < 3; j++){
				p[j] = sumIX[j] / sumI;
				var += 1.0/sumI*(sumIX2[j] - sumIX[j]*sumIX[j]/sumI);					
			}
			double fwhm = 2.35 * FastMath.sqrt(var);
			
			//sys.addElement(new Sphere("hitPoint_"+iB+"_"+iP, p, fwhm/2, NullInterface.ideal()));
			hitPoints[iT] = new double[]{ p[0], p[1], p[2], fwhm };
			
			double sp[] = new double[3];
			for(int j=0; j < 3; j++){
				sp[j] = sumIXsp[j] / sumI;					
			}				
			startPoints[iT] = new double[]{ sp[0], sp[1], sp[2]};
			
			hitInfoOut.writeRow(iB, iP, sys.channelR[iB][iP], p[0], p[1], p[2], fwhm, (double)nHit / nAttempts, (double)nStray / nAttempts, sp[0], sp[1], sp[2]);
			
			System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
			System.out.println("P=" + iB + "." + iP + "(fwhm = " + fwhm + "):\t Beam: " + nHit + " / " + nAttempts + " = " + (100 * nHit / nAttempts) + 
																				" % \t Stray:" + nStray + " / " + nAttempts + " = " + (100 * nStray / nAttempts) + " %");
			
			for(int j=0;j < 3; j++){				
				outputInfo(startPoints, hitPoints, iT, j);
			}
			
			System.out.println();	
		}
	
		
		//spit out build commands and LOS definitions in blocks
		for(int j=0;j < 3; j++){
			for(int iT=0; iT < targetNames.length; iT++){			
					outputInfo(startPoints, hitPoints, iT, j);
			}
		}
		
		hitInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
	
	private static void outputInfo(double startPoints[][], double hitPoints[][], int iT, int thing){
	

		double rad = hitPoints[iT][3] / 4;
		
		//System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Sphere\", \"bgHit_"+sys.getDesignName()+"_"+iB+"_"+iP+"\"); "+
		//			"o.Shape = Part.makeSphere("+rad*1e3+",FreeCAD.Vector("+b[0]*1e3+","+b[1]*1e3+","+b[2]*1e3 + "));");
		double u[] = Util.reNorm(Util.minus(hitPoints[iT], startPoints[iT]));
		double losLen = Util.length(Util.minus(hitPoints[iT], startPoints[iT]));
		
				
		//double start[] = sys.lens1.getBackSurface().getCentre();
		double uVec[] = Util.reNorm(Util.minus(hitPoints[iT], startPoints[iT]));
		String chanName = targetNames[iT];
	
		switch(thing){
			case 0:		
				System.out.println("Part.show(Part.makeSphere("+rad*1e3+",FreeCAD.Vector("+hitPoints[iT][0]*1e3+","+hitPoints[iT][1]*1e3+","+hitPoints[iT][2]*1e3 + ")));"
						+ " FreeCAD.ActiveDocument.ActiveObject.Label=\"bgHit_"+sys.getDesignName()+"_"+chanName+"\";");
				break;
				
			case 1:
				System.out.println("Part.show(Part.makeCylinder("+losCyldRadius*1e3+","+losLen*1e3 +","										
						+"FreeCAD.Vector("+startPoints[iT][0]*1e3+","+startPoints[iT][1]*1e3+","+startPoints[iT][2]*1e3+"), "
						+"FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+ "))); FreeCAD.ActiveDocument.ActiveObject.Label=\"los_"+sys.getDesignName()+"_"+chanName+"\";");
				break;
				
			case 2:
				System.out.print(chanName
						+ ", start={ " + String.format("%7.5g", startPoints[iT][0]) + ", " + String.format("%7.5g", startPoints[iT][1]) + ", " + String.format("%7.5g", startPoints[iT][2]) + "}"
						+ ", uVec={ " + String.format("%7.5g", uVec[0]) + ", " + String.format("%7.5g", uVec[1]) + ", " + String.format("%7.5g", uVec[2]) + "}");
										
				System.out.println(", wall={ "+ String.format("%7.5g", hitPoints[iT][0]) 
									+ ", " + String.format("%7.5g", hitPoints[iT][1]) 
									+ ", " + String.format("%7.5g", hitPoints[iT][2]) + "}"
						);
				break;
		}
		
		
		
		
	}
	
}
