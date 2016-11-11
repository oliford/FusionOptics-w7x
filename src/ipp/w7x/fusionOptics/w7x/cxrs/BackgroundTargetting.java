package ipp.w7x.fusionOptics.w7x.cxrs;

import ipp.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_imaging;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
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

/** Basic pictures for BeamEmissSpecAET21 model */
public class BackgroundTargetting {
	
	public static BeamEmissSpecAET21_postDesign sys = new BeamEmissSpecAET21_postDesign();	
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static BeamEmissSpecAEM21_postDesign sys = new BeamEmissSpecAEM21_postDesign();
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	
	public static double fibreEffectiveNA = 0.22; //0.28; //f/4 = 0.124, f/6=0.083
	 
	public final static int nAttempts = 100;

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/background/";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static void main(String[] args) {
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace-"+sys.getDesignName()+".vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		int totalFibres = sys.channelR.length*sys.channelR[0].length;
		vrmlOut.setSkipRays(nAttempts*totalFibres / 5000);
		double col[][] = ColorMaps.jet(sys.channelR[0].length);
		
		BinaryMatrixWriter hitInfoOut = new BinaryMatrixWriter(outPath + "/hitInfo.bin", 9);
		
		Optic background = new Optic("background");
		for(String fileName : sys.backgroundSTLFiles){			
			String parts[] = fileName.split("/");
					
			background.addElement(new STLMesh("bg_"+parts[parts.length-1], fileName));			
		}
		Optic all = new Optic("all", new Element[]{ sys, background });
		//sys.addElement(background);		
				
		//Need to get through the fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());		
		sys.addElement(sys.beamPlane);
		
		double hitPoints[][][] = new double[sys.channelR.length][][];
		
		for(int iB=0; iB < sys.channelR.length; iB++){
			hitPoints[iB] = new double[sys.channelR[iB].length][];
			
			for(int iP=0; iP < sys.channelR[iB].length; iP++){
							
				int nHit = 0, nStray = 0;
				
				double startPos[] = sys.fibreEndPos[iB][iP];
				
				double sumI=0, sumIX[]={0,0,0}, sumIX2[] = {0,0,0};
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
					ray.wavelength = sys.designWavelenth;
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

				
				hitInfoOut.writeRow(iB, iP, sys.channelR[iB][iP], p[0], p[1], p[2], fwhm, (double)nHit / nAttempts, (double)nStray / nAttempts);
				
				System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
				System.out.println("P=" + iB + "." + iP + "(fwhm = " + fwhm + "):\t Beam: " + nHit + " / " + nAttempts + " = " + (100 * nHit / nAttempts) + 
																					" % \t Stray:" + nStray + " / " + nAttempts + " = " + (100 * nStray / nAttempts) + " %");
				{
					double b[] = { hitPoints[iB][iP][0], hitPoints[iB][iP][1], hitPoints[iB][iP][2] };
					double rad = hitPoints[iB][iP][3] / 2;
					System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Sphere\", \"bgHit_"+iB+"_"+iP+"\"); "+
								"o.Shape = Part.makeSphere("+rad*1e3+",FreeCAD.Vector("+b[0]*1e3+","+b[1]*1e3+","+b[2]*1e3 + "));");
					
				}
			}
		}
		
		//spit out FreeCAD instructions to create fibre end block cylinders
		
		for(int iB=0; iB < sys.channelR.length; iB++){
			for(int iP=0; iP < sys.channelR[iB].length; iP++){
				if(hitPoints[iB][iP] == null)
					continue;
				
				double p[] = { hitPoints[iB][iP][0], hitPoints[iB][iP][1], hitPoints[iB][iP][2] };
				double rad = hitPoints[iB][iP][3] / 2;
				 		
				System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Sphere\", \"bgHit_"+iB+"_"+iP+"\"); "+
							"o.Shape = Part.makeSphere("+rad*1e3+",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 + "));");
			}
		}
		
		hitInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
	
}
