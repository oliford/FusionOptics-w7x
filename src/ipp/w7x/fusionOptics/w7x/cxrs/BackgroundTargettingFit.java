package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.FibreBacktrace.Thing;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeVIS;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_obsolete;
import ipp.w7x.fusionOptics.w7x.cxrs.aem41.BeamEmissSpecAEM41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem50.BeamEmissSpecAEM50;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_OP2;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_OP2.CoordState;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_AsMeasured;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_imaging;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET20_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_asMeasuredOP12b;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_OneSmallFlatMirror2_BK7;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7XPelletsK41;
import ipp.w7x.neutralBeams.W7XPelletsL41;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import net.jafama.FastMath;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;

import uk.co.oliford.jolu.OneLiners;
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
	
	//public static BeamEmissSpecAET21_OP2_OneSmallFlatMirror2_BK7 sys = new BeamEmissSpecAET21_OP2_OneSmallFlatMirror2_BK7(false, true);	
	
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(false);
	//public static BeamEmissSpecAEM21_postDesign_AsMeasured sys = new BeamEmissSpecAEM21_postDesign_AsMeasured(false); //not in LC3
	public static BeamEmissSpecAEM21_OP2 sys = new BeamEmissSpecAEM21_OP2(CoordState.LC3a);
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static BeamEmissSpecAEM50 sys = new BeamEmissSpecAEM50();
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
	 
	public final static int nAttempts = 500;

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/bgFit/";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static double losCyldRadius = 0.005;
	public static Surface startSurface = sys.losStartSurface;
	
	
	public static String targetNames[];
	public static double targetCoords[][];
	public static int[][] targetIndecies;
		
	public static void main(String[] args) throws FileNotFoundException {
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace-"+sys.getDesignName()+".vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		int totalFibres = sys.channelR.length*sys.channelR[0].length;
		vrmlOut.setSkipRays(nAttempts*totalFibres / 5000);
		double col[][] = ColorMaps.jet(sys.channelR[0].length);
		
		double rad = 0.005;
		targetNames = new String[sys.measured.size()];
		targetCoords = new double[sys.measured.size()][];
		int k=0;
		for(Entry<String, double[]> entry : sys.measured.entrySet()) {
			targetNames[k] = entry.getKey();
			targetCoords[k] = OneLiners.mul(entry.getValue(), 1e-3);

			System.out.println(String.format("\"%s\", %.6f, %.6f, %.6f",  targetNames[k], targetCoords[k][0], targetCoords[k][1], targetCoords[k][2]));
			
			k++;
		}
		
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
		
		/** iB and iP of each target */
		targetIndecies = new int[targetNames.length][2];
		
		for(int iT=0; iT < targetNames.length; iT++){ //foreach target (fibre/point)
						
			int nHit = 0, nStray = 0;
			
			//find the fibre
			String parts[] = targetNames[iT].split(":");			
					
			int iB=-1;				
			for(int jB=0; jB < sys.channelR.length; jB++){
				if(parts[0].equalsIgnoreCase(sys.lightPathsSystemName() + "_" + sys.lightPathRowName(jB))){
					iB = jB;
					break;
				}
			}
			if(iB<0)
				throw new RuntimeException("Couldn't find fibre set for target '"+parts[0]+"'");
			
			
			int iP = Integer.parseInt(parts[1]) - 1; 
			targetIndecies[iT] = new int[] { iB, iP };
			
			double startPos[] = sys.fibreEndPos[iB][iP];
			
			double sumI=0, sumIX[]={0,0,0}, sumIX2[] = {0,0,0};
			double sumIXsp[]={0,0,0};
			for(int i=0; i < nAttempts; i++){
				double x, y, rMax = sys.getFibreDiameter(0, 0) / 2;
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
			for(Thing thing : Thing.values()){
				if(thing == Thing.FreeCADBeamPlane)
					continue;
				outputTargetInfo(System.out, startPoints, hitPoints, iT, thing);
			}
			
			System.out.println();	
		}
	
		

		PrintStream textOut = new PrintStream(outPath + "/info.txt");
		//spit out build commands and LOS definitions in blocks
		for(Thing thing : Thing.values()){
			System.out.println("\n" + thing.toString() + ": ");
			for(int iT=0; iT < targetNames.length; iT++){ //foreach target (fibre/point)
				outputTargetInfo(System.out, startPoints, hitPoints, iT, thing);	
				outputTargetInfo(textOut, startPoints, hitPoints, iT, thing);	
			}
		}
		textOut.close();
		
		hitInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
	
	public static void outputTargetInfo(PrintStream stream, double startPoints[][], double hitPoints[][], int iT, Thing thing){
		int iB = targetIndecies[iT][0];
		int iP = targetIndecies[iT][1];
		//interface to [iB][iP] style arrays expected by FibreBacktrace
		double[][][] startPointsAll = new double[iB+1][iP+1][];
		double[][][] hitPointsAll = new double[iB+1][iP+1][];
		startPointsAll[iB][iP] = startPoints[iT];
		hitPointsAll[iB][iP] = hitPoints[iT];
		FibreBacktrace.outputInfo(stream, startPointsAll, hitPointsAll, null, iB, iP, thing);
	}
	
}
