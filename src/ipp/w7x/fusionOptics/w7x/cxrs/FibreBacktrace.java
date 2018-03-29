package ipp.w7x.fusionOptics.w7x.cxrs;


import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_edgeVIS;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_edgeVIS;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_LC3_tilt3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_obsolete;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_imaging;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET20_postDesign_LC3;
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
import fusionDefs.neutralBeams.SimpleBeamGeometry;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.STLDrawer;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Basic pictures for BeamEmissSpecAET21 model */
public class FibreBacktrace {
	
	public static BeamEmissSpecAET20_postDesign_LC3 sys = new BeamEmissSpecAET20_postDesign_LC3();
	//public static BeamEmissSpecAET21_postDesign sys = new BeamEmissSpecAET21_postDesign();
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static BeamEmissSpecAEB20 sys = new BeamEmissSpecAEB20();
	//public static BeamEmissSpecAEM21_postDesign sys = new BeamEmissSpecAEM21_postDesign();
	//public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(true);
	//public static BeamEmissSpecAEM21_LC3_tilt3 sys = new BeamEmissSpecAEM21_LC3_tilt3();
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	//public static BeamEmissSpecAEK21_edgeUV sys = new BeamEmissSpecAEK21_edgeUV();
	//public static BeamEmissSpecAEK21_edgeVIS sys = new BeamEmissSpecAEK21_edgeVIS();
	//public static SimpleBeamGeometry beams = EdgePenetrationAEK41.def();
	
	//public static BeamEmissSpecAEK21_pelletsK41 sys = new BeamEmissSpecAEK21_pelletsK41();
	//public static SimpleBeamGeometry beams = W7XPelletsK41.def();
	
	//public static BeamEmissSpecAEK21_pelletsL41 sys = new BeamEmissSpecAEK21_pelletsL41();
	//public static SimpleBeamGeometry beams = W7XPelletsL41.def();
		
	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	

	//public static BeamEmissSpecAEK21_edgeUV sys = new BeamEmissSpecAEK21_edgeUV();
	//public static BeamEmissSpecAEK21_edgeVIS sys = new BeamEmissSpecAEK21_edgeVIS();
	//public static SimpleBeamGeometry beams = EdgePenetrationAEK41.def();
	
	//public static BeamEmissSpecAEK21_pelletsK41 sys = new BeamEmissSpecAEK21_pelletsK41();
	//public static SimpleBeamGeometry beams = W7XPelletsK41.def();
	
	//public static BeamEmissSpecAEK21_pelletsL41 sys = new BeamEmissSpecAEK21_pelletsL41();
	//public static SimpleBeamGeometry beams = W7XPelletsL41.def();
	
	public final static double traceWavelength = sys.designWavelenth;
	
	public static double fibreEffectiveNA = 0.22; //0.28; //f/4 = 0.124, f/6=0.083
	 
	public final static int nAttempts = 1000;

	public static String writeWRLForDesigner = null;//20170717";
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/fibreTrace/"+((int)(traceWavelength/1e-9))+"nm";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static void main(String[] args) {
		makeFibreCyldSTL(); //		System.exit(0);
		
		System.out.println(outPath);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/fibresTrace-"+sys.getDesignName()+((writeWRLForDesigner != null) ? ("-" + writeWRLForDesigner + ".wrl") : ".vrml"), 5.005);
		if((writeWRLForDesigner == null)){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		int totalFibres = sys.channelR.length*sys.channelR[0].length;
		vrmlOut.setSkipRays(nAttempts*totalFibres / 10000);
		int maxChans=-1;
		for(int iB=0; iB < sys.channelR.length; iB++)
			if(sys.channelR[iB].length > maxChans)
				maxChans = sys.channelR[iB].length;
		double col[][] = ColorMaps.jet(maxChans);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);

		BinaryMatrixWriter fibreInfoOut = new BinaryMatrixWriter(outPath + "/fibreInfo.bin", 15); 
				
		//Need to get through the fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());		
		sys.addElement(sys.beamPlane);
		
		for(int iB=0; iB < sys.channelR.length; iB++){
			double beamStart[] = beams.start(sys.beamIdx[iB]);
			double beamVec[] =  beams.uVec(sys.beamIdx[iB]);
			
			for(int iP=0; iP < sys.channelR[iB].length; iP+=1){
							
				int nHit = 0, nStray = 0;
				
				double startPos[] = sys.fibreEndPos[iB][iP];
				
				double sumI=0, sumIR=0, sumIR2 = 0, beamPlanePos[] = new double[3], sumDistToBeam=0;
				double closestApproachPos[] = new double[3];
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
					ray.wavelength = traceWavelength;
					ray.E0 = new double[][]{{1,0,0,0}};
					ray.up = Util.createPerp(ray.dir);
							
					Tracer.trace(sys, ray, 100, 0, false);
					
					ray.processIntersections(null, intensityInfo);
	
					
					//vrmlOut.drawRay(ray, col[iP]);
					List<Intersection> hits = ray.getIntersections(sys.beamPlane);
					if(hits.size() > 0){
						double p[] = hits.get(0).pos;
						double R = FastMath.sqrt(p[0]*p[0] + p[1]*p[1]);
						sumI += 1;
						sumIR += R * 1;
						sumIR2 += R * R * 1;
						
						
						RaySegment hitRay = hits.get(0).incidentRay;
						double aL = Algorithms.pointOnLineNearestAnotherLine(hitRay.startPos, hitRay.dir, beamStart, beamVec);
						double p1[] = OneLiners.plus(hitRay.startPos, OneLiners.mul(hitRay.dir, aL));
						
						aL = Algorithms.pointOnLineNearestAnotherLine(beamStart, beamVec, hitRay.startPos, hitRay.dir);
						double p2[] = OneLiners.plus(beamStart, OneLiners.mul(beamVec, aL));
						
						for(int j=0; j<3; j++){
							beamPlanePos[j] += p[j];
							closestApproachPos[j] += p1[j];
						}
						
						sumDistToBeam += OneLiners.length(OneLiners.minus(p2, p1));
						nHit++;
					}
					
					if(sys.strayPlane != null && ray.getIntersections(sys.strayPlane).size() > 0){
						nStray++;
					}
					
					//if(ray.getIntersections(sys.strayPlane).size() > 0){					
						vrmlOut.drawRay(ray, col[iP]); //stray light						
					//}
					
					Pol.recoverAll();
				}
				double R = sumIR / sumI;
				
				double var = 1.0/sumI*(sumIR2 - sumIR*sumIR/sumI);
				double fwhmR = 2.35 * FastMath.sqrt(var);
				for(int j=0; j<3; j++){
					beamPlanePos[j] /= nHit;
					closestApproachPos[j] /= nHit;
				}
				sumDistToBeam /= nHit;  
				
				fibreInfoOut.writeRow(iB, iP, sys.channelR[iB][iP], R, fwhmR, (double)nHit / nAttempts, (double)nStray / nAttempts, R, sumDistToBeam, beamPlanePos, closestApproachPos);
				
				System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
				System.out.println("P=" + iB + "." + iP + "(R=" + R + ", fwhmR = " + (fwhmR*1e3) + " mm):\t Beam: " + nHit + " / " + nAttempts + " = " + (100 * nHit / nAttempts) + 
																					" % \t Stray:" + nStray + " / " + nAttempts + " = " + (100 * nStray / nAttempts) + " %");

				System.out.println("Part.show(Part.makeSphere("+(fwhmR*1e3)+", FreeCAD.Vector("+beamPlanePos[0]*1e3+","+beamPlanePos[1]*1e3+","+beamPlanePos[2]*1e3 + "));");
				
				intensityInfo.reset();
			}
		}
		
		//spit out FreeCAD instructions to create fibre end block cylinders
		double cyldLen = 0.030;
		double cyldRadius = 0.0025;
		
		for(int iB=0; iB < sys.channelR.length; iB++){
			for(int i=0; i < sys.channelR[iB].length; i++){
				double u[] = sys.fibreEndNorm[iB][i];
				 double p[] = Util.plus(sys.fibreEndPos[iB][i], Util.mul(u, -cyldLen));
				 		
				System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Cylinder\", \"FibreEnd"+i+"\"); "+
							"o.Shape = Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
							",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 +
							"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360);");
			}
		}
		
		fibreInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
	
	private static void makeFibreCyldSTL() {
		Optic fibreCylds = new Optic("fibreCylds");
		double l = 0.050;
		for(int iB = 0; iB < sys.channelR.length; iB++){
			for(int iF=0; iF < sys.channelR[iB].length; iF++){

				double c[] = Util.plus(sys.fibreEndPos[iB][iF], Util.mul(sys.fibreEndNorm[iB][iF], -l/2));
				fibreCylds.addElement(new Cylinder("fibre_"+iB+"_"+iF, c, sys.fibreEndNorm[iB][iF], sys.fibreEndDiameter/2, l, NullInterface.ideal()));
			}
		}
		
		STLDrawer stlDrawer = new STLDrawer(outPath + "/fibreCylds-"+sys.getDesignName()+".stl");		
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
		stlDrawer.drawOptic(fibreCylds);
		stlDrawer.destroy();
		
		stlDrawer = new STLDrawer(outPath + "/rodCyld-"+sys.getDesignName()+".stl");		
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		if(sys.rod != null)
			stlDrawer.drawOptic(new Optic("rodOptic", new Element[]{ sys.rod }));
		stlDrawer.destroy();
	}
}
