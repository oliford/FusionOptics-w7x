package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21.Subsystem;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_baffleW;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeVIS;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeVIS_OP22;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeVIS_OP22_torScan;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_obsolete;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_OP2.CoordState;
import ipp.w7x.fusionOptics.w7x.cxrs.aem41.BeamEmissSpecAEM41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_OP2;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_imaging;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET20_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_asMeasuredOP12b;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_OneSmallFlatMirror2_BK7;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7.Focus;
import ipp.w7x.neutralBeams.EdgePenetrationAEK41;
import ipp.w7x.neutralBeams.W7XPelletsK41;
import ipp.w7x.neutralBeams.W7XPelletsL41;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import net.jafama.FastMath;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
 * Produce LOS cylinder and point instructions for FreeCAD
 * 
*/
public class BackgroundTargetting {
	
	//public static BeamEmissSpecAET21_postDesign sys = new BeamEmissSpecAET21_postDesign();	
	//public static BeamEmissSpecAET21_asMeasuredOP12b sys = new BeamEmissSpecAET21_asMeasuredOP12b();
	//public static BeamEmissSpecAET20_postDesign_LC3 sys = new BeamEmissSpecAET20_postDesign_LC3();
	
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21(Subsystem.CXRS);
	public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21(Subsystem.SMSE);
	//public static BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7 sys = new BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7(false, false, Focus.BeamDump);
	//public static BeamEmissSpecAET21_OP2_OneSmallFlatMirror2_BK7 sys = new BeamEmissSpecAET21_OP2_OneSmallFlatMirror2_BK7(false, false);	
	//public static BeamEmissSpecAEM21_OP2 sys = new BeamEmissSpecAEM21_OP2(CoordState.AsBuilt);
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	
	//public static BeamEmissSpecAEK41_edgeVIS_OP22_torScan sys = new BeamEmissSpecAEK41_edgeVIS_OP22_torScan();
	//public static BeamEmissSpecAEK41_edgeVIS_OP22 sys = new BeamEmissSpecAEK41_edgeVIS_OP22();	
	//public static BeamEmissSpecAEK41_baffleW sys = new BeamEmissSpecAEK41_baffleW();
	//public static BeamEmissSpecAEK41_edgeVIS sys = new BeamEmissSpecAEK41_edgeVIS();
	//public static BeamEmissSpecAEK41_pelletsK41 sys = new BeamEmissSpecAEK41_pelletsK41();
	//public static SimpleBeamGeometry beams = EdgePenetrationAEK41.def();
	
	//public static BeamEmissSpecAEK21_edgeUV sys = new BeamEmissSpecAEK21_edgeUV();
	//public static SimpleBeamGeometry beams = EdgePenetrationAEK41.def();	
	
	//public static BeamEmissSpecAEK41_pelletsK41 sys = new BeamEmissSpecAEK41_pelletsK41();
	//public static SimpleBeamGeometry beams = W7XPelletsK41.def();
	
	//public static BeamEmissSpecAEK21_pelletsL41 sys = new BeamEmissSpecAEK21_pelletsL41(); //need baffels and a panel
	//public static SimpleBeamGeometry beams = W7XPelletsL41.def();
	
	public static double fibreEffectiveNA = 0.22; //0.28; //f/4 = 0.124, f/6=0.083
	 
	public final static int nAttempts = 500;
	public static int nRaysToDraw = 50;
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/background/";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static String writeWRLForDesigner = null;//"20210817";
	
	public static double losCyldRadius = 0.005;
		
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println(outPath);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/backTrace-"+sys.getDesignName()+((writeWRLForDesigner != null) ? ("-" + writeWRLForDesigner + ".wrl") : ".vrml"), 5.005);
		if((writeWRLForDesigner == null)){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		}		
		
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		int totalFibres = sys.channelR.length*sys.channelR[0].length;
		vrmlOut.setSkipRays(nAttempts*totalFibres / nRaysToDraw);
		double col[][] = ColorMaps.jet(sys.channelR[0].length);
		
		BinaryMatrixWriter hitInfoOut = new BinaryMatrixWriter(outPath + "/hitInfo.bin", 12);
		
		Optic background = new Optic("background");
		for(String fileName : sys.backgroundSTLFiles){			
			String parts[] = fileName.split("/");
			System.out.print("Loading BG mesh " + fileName + "... ");
			background.addElement(new STLMesh("bg_"+parts[parts.length-1], fileName));
			System.out.println("OK");
		}
		Optic all = new Optic("all", new Element[]{ sys, background });
		//background.addElement(sys.port30Plane);
		//sys.addElement(background);
				
		//Need to get through the fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());		
		//sys.addElement(sys.beamPlane);
		
		double hitPoints[][][] = new double[sys.channelR.length][][];
		double startPoints[][][] = new double[sys.channelR.length][][];
		
		for(int iB=0; iB < sys.channelR.length; iB++){
			hitPoints[iB] = new double[sys.channelR[iB].length][];
			startPoints[iB] = new double[sys.channelR[iB].length][];
			
			for(int iP=0; iP < sys.channelR[iB].length; iP++){
							
				int nHit = 0, nStray = 0;
				
				double startPos[] = sys.fibreEndPos[iB][iP];
				
				double sumI=0, sumIX[]={0,0,0}, sumIX2[] = {0,0,0};
				double sumIXsp[]={0,0,0};
				for(int i=0; i < nAttempts; i++){
					//if((iP % 5) > 0)
						//continue;
					double x, y, rMax = sys.getFibreDiameter(iB, iP) / 2;
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
						
						Intersection startSurfaceHit = hits.get(0).incidentRay.findFirstEarlierIntersection(sys.losStartSurface);						
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
				hitPoints[iB][iP] = new double[]{ p[0], p[1], p[2], fwhm };
				
				double sp[] = new double[3];
				for(int j=0; j < 3; j++){
					sp[j] = sumIXsp[j] / sumI;					
				}				
				startPoints[iB][iP] = new double[]{ sp[0], sp[1], sp[2]};
				
				hitInfoOut.writeRow(iB, iP, sys.channelR[iB][iP], p[0], p[1], p[2], fwhm, (double)nHit / nAttempts, (double)nStray / nAttempts, sp[0], sp[1], sp[2]);
				
				System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
				System.out.println("P=" + iB + "." + iP + "(fwhm = " + fwhm + "):\t Beam: " + nHit + " / " + nAttempts + " = " + (100 * nHit / nAttempts) + 
																					" % \t Stray:" + nStray + " / " + nAttempts + " = " + (100 * nStray / nAttempts) + " %");
				
				for(Thing thing : Thing.values()){
					outputInfo(System.out, startPoints, hitPoints, iB, iP, thing);
				}
				
				System.out.println();	
			}
		}
		

		//output JSON LOS info
		PrintStream jsonOut = new PrintStream(outPath + "/lineOfSightDefs-"+sys.lightPathsSystemName()+".json");
		jsonOut.println("{ \"system\" : \""+sys.lightPathsSystemName()+"\", \"info\" : \"From raytracer "+sys.getDesignName()+" on "+
				((new SimpleDateFormat()).format(new Date()))+" \", \"los\" : [");
		for(int iB=0; iB < sys.channelR.length; iB++){
			for(int iP=0; iP < sys.channelR[iB].length; iP++){		
				outputInfo(jsonOut, startPoints, hitPoints, iB, iP, Thing.JSON_LOS);				
			}
		}
		jsonOut.println("]}");

		
		PrintStream textOut = new PrintStream(outPath + "/info.txt");
		//spit out build commands and LOS definitions in blocks
		for(Thing thing : Thing.values()){
			for(int iB=0; iB < sys.channelR.length; iB++){
				for(int iP=0; iP < sys.channelR[iB].length; iP++){			
					outputInfo(System.out, startPoints, hitPoints, iB, iP, thing);	
					outputInfo(textOut, startPoints, hitPoints, iB, iP, thing);					
				}
			}
		}
		textOut.close();
		
		hitInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
	
	private static enum Thing { FreeCADHitPos, FreeCADLOS, JSON_LOS, TXT_LOS_MM };
	private static void outputInfo(PrintStream stream, double startPoints[][][], double hitPoints[][][], int iB, int iP, Thing thing){

		boolean isLast = (iB == sys.channelR.length-1) && (iP == sys.channelR[iB].length-1);

		double rad = hitPoints[iB][iP][3] / 2;
		
		//System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Sphere\", \"bgHit_"+sys.getDesignName()+"_"+iB+"_"+iP+"\"); "+
		//			"o.Shape = Part.makeSphere("+rad*1e3+",FreeCAD.Vector("+b[0]*1e3+","+b[1]*1e3+","+b[2]*1e3 + "));");
		double u[] = Util.reNorm(Util.minus(hitPoints[iB][iP], startPoints[iB][iP]));
		double losLen = Util.length(Util.minus(hitPoints[iB][iP], startPoints[iB][iP]));
		
		//point on ray closest to beam axes
		double approach[][] = new double[8][];
		for(int jB=6; jB < 8; jB++){
			if(sys.beamIdx[iB] < 0)
				continue;
			double beamStart[] = beams.start(sys.beamIdx[iB]);
			double beamVec[] =  beams.uVec(sys.beamIdx[iB]);
			
			double aL = Algorithms.pointOnLineNearestAnotherLine(startPoints[iB][iP], u, beamStart, beamVec);
			approach[jB] = OneLiners.plus(startPoints[iB][iP], OneLiners.mul(u, aL));
		}
		
		//double start[] = sys.lens1.getBackSurface().getCentre();
		double uVec[] = Util.reNorm(Util.minus(hitPoints[iB][iP], startPoints[iB][iP]));
		String chanName = sys.getChanName(iB, iP);
	
		switch(thing){
			case FreeCADHitPos:		
				stream.println("Part.show(Part.makeSphere("+rad*1e3+",FreeCAD.Vector("+hitPoints[iB][iP][0]*1e3+","+hitPoints[iB][iP][1]*1e3+","+hitPoints[iB][iP][2]*1e3 + ")));"
						+ " FreeCAD.ActiveDocument.ActiveObject.Label=\"bgHit_"+sys.getDesignName()+"_"+chanName+"\"; g.addObject(FreeCAD.ActiveDocument.ActiveObject);");
				break;
				
			case FreeCADLOS:
				stream.println("Part.show(Part.makeCylinder("+losCyldRadius*1e3+","+losLen*1e3 +","										
						+"FreeCAD.Vector("+startPoints[iB][iP][0]*1e3+","+startPoints[iB][iP][1]*1e3+","+startPoints[iB][iP][2]*1e3+"), "
						+"FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+ "))); FreeCAD.ActiveDocument.ActiveObject.Label=\"los_"+sys.getDesignName()+"_"+chanName+"\";");
				break;
				
			case JSON_LOS:
				stream.print("{ \"id\" : \"" + chanName
						+ "\", \"start\":[ " + String.format("%7.5g", startPoints[iB][iP][0]) + ", " + String.format("%7.5g", startPoints[iB][iP][1]) + ", " + String.format("%7.5g", startPoints[iB][iP][2]) + "]"
						+ ", \"uVec\":[ " + String.format("%7.5g", uVec[0]) + ", " + String.format("%7.5g", uVec[1]) + ", " + String.format("%7.5g", uVec[2]) + "]");
				for(int jB=0; jB < approach.length; jB++){
					if(approach[jB] != null)
						stream.print(", \"approachQ"+(jB+1)+"\":[ " + String.format("%7.5g", approach[jB][0]) + ", " + String.format("%7.5g", approach[jB][1]) + ", " + String.format("%7.5g", approach[jB][2]) + "]");
				}
						
				stream.println(", \"wallHit\":[ "+ String.format("%7.5g", hitPoints[iB][iP][0]) 
									+ ", " + String.format("%7.5g", hitPoints[iB][iP][1]) 
									+ ", " + String.format("%7.5g", hitPoints[iB][iP][2]) + "]"
								+ "}" + (isLast ? "" : ", ")
						);				
				break;
								
			case TXT_LOS_MM:
				stream.println(String.format("%7.3f", startPoints[iB][iP][0]*1e3) + " " + String.format("%7.3f", startPoints[iB][iP][1]*1e3) + " " + String.format("%7.3f", startPoints[iB][iP][2]*1e3) + " "
							+ String.format("%7.3f", hitPoints[iB][iP][0]*1e3) + " " + String.format("%7.3f", hitPoints[iB][iP][1]*1e3) + " " + String.format("%7.3f", hitPoints[iB][iP][2]*1e3));
						
				break;
		}
		
		
		
		
	}
	
}
