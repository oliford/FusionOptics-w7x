package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21U_CISDual;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_LC3_tilt3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_obsolete;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET20_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7.Focus;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses_75mm_UVFS_3cmAperture;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_OneSmallFlatMirror;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_Parabolic;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_TwoFlatAndLenses;
import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEM41;
import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEW21;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import net.jafama.FastMath;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import binaryMatrixFile.AsciiMatrixFile;
import binaryMatrixFile.BinaryMatrixFile;
import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.BinarySTLFile;
import otherSupport.ColorMaps;
import otherSupport.ScientificNumberFormat;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.HitPositionAverage;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.collection.LightConeInfo;
import fusionOptics.collection.PlaneAngleInfo;
import fusionOptics.drawing.STLDrawer;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.optics.STLMesh;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Triangle;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Basic pictures for BeamEmissSpecAET21 model */
public class LightAssessmentW7X {
	
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static BeamEmissSpecAEA21U_CISDual sys = new BeamEmissSpecAEA21U_CISDual();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	//public static boolean forcePerpFibres = true; //telecentric-ish
	
	//public static BeamEmissSpecAET21_postDesign sys = new BeamEmissSpecAET21_postDesign();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	//public static boolean forcePerpFibres = true;
	
	//public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(true);
	
	public static BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7 sys = new BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7(false, false, Focus.BeamDump);	
	//public static BeamEmissSpecAET21_OP2_OneSmallFlatMirror sys = new BeamEmissSpecAET21_OP2_OneSmallFlatMirror();
	public static Surface mustHitToDraw = sys.lens1.getSurfaces().get(0);
	//public static Surface mustHitToDraw = sys.entryWindowFront;	
	public static boolean forcePerpFibres = false;

		
	//public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(true);
	//public static Surface mustHitToDraw = sys.fibrePlane;
	//public static boolean forcePerpFibres = true;
	
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static Surface mustHitToDraw = sys.entryWindowFront;
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	//public final static double R0 = 5.2, R1 = 5.9; //as sightlines in fromDesigner-201511076
	//public static boolean forcePerpFibres = false; //AEM41 has only one lens, so is not at all telecentric
	
	
	public static List<Surface> interestedSurfaces = new ArrayList<Surface>();
	
	public static Surface fibrePlane = sys.fibrePlane;

	//public static int beamSelection[] = { beams.BEAM_Q6, beams.BEAM_Q7 };  
	//public static int beamSelection[] = { beams.BEAM_Q6, beams.BEAM_Q8 }; //for AEM21, 6 and 8 are the extremes	
	//public static int beamSelection[] = { beams.BEAM_Q7, beams.BEAM_Q8 }; // OP1.2 beams (lower in plasma)
	//public static int beamSelection[] = { beams.BEAM_Q7 }; // OP1.2 beams (lower in plasma)
	//public static int beamSelection[] = { beams.BEAM_Q8 }; // just Q7
	//public static int beamSelection[] = { -2 }; // Box axis for K21
	//public static int beamSelection[] = { 0 }; // RuDIX
	//public static int beamSelection[] = { beams.BEAM_Q4 }; // for T20
	//public static int beamSelection[] = { -1 }; // Box axis for K20
	public static int beamSelection[] = sys.beamIdx;
	
	// For fast drawing/debugging
	//public static double pointR[] = { 5.50, 5.70, 5.90 };
	//public static double pointR[] = OneLiners.linSpace(5.40, 5.851, 0.05);
	//public static double pointR[] = OneLiners.linSpace(5.35, 5.88, 20); //for AET2x OP1.2
	public static double pointR[] = OneLiners.linSpace(5.50, 5.95, 5); //for AET2x OP2
		
	//public static double pointR[] = OneLiners.linSpace(5.45, 6.05, 50); // for AEM21
	//public static double pointR[] = OneLiners.linSpace(5.24, 6.05, 106); // for AEA21
	
	//public static double pointR[] = { 5.06, 5.07, 5.08, 5.09 };
	
	/* //For LOS finding for Maciej's high iota halpha
	public static double pointR[] = OneLiners.linSpace(6.03, 6.20, 5); // for AEM21 from divertor for Maciej
	public static double divTargetPhi = (93.7517444686941 + 2.0) * Math.PI / 180;
	public static double divTargetZ = -0.5957244262695313;
	//*/
	
	//public final static int nAttempts = 5000;
	
	public static boolean writeSolidAngeInfo = true;
	public static String writeWRLForDesigner = null;//"-20160826";
	public final static int nAttempts = 5000;
	//*/
	
	public static double wavelength = sys.designWavelenth;
	//public static double wavelength = 468e-9; //HeII
	//public static double wavelength = 530e-9; //C_VI
	//public static double wavelength = 656e-9; //HAlpha
	
	public static int nRaysToDraw = 5000;
	// For calc
	/*public static double pointR[] = sys.channelR;
	public final static int nAttempts = 20000;
	//*/
	
	public static double fibreCoreDiameter = 400e-6;
	public static double fibreNA = 0.22;
	
	
	public static class FibreInfo{		
		double R;
		double beamPos[];
		double viewPos[];
		double fibrePos[];
		
		/* Mean vector of delivery cone to fibre */
		double fibreMeanVec[];
		
		/** Number of fired rays which hit the fibre plane */
		int nHitPlane;				
		/** Solid angle into which rays are fired */
		double solidAngleFiring;
		/** Fraction of rays reaching plane that enter the fibre aligned with the mean ray */
		double fracInMeanCone;
		/** Fraction of rays reaching plane that enter the fibre aligned perp to the fibre plane*/
		double fracInPerpCone;
		
		/** Effective collected solid angle into fibre perp cone */
		double effectiveSolidAngle;
		
	}
	
	public static int nPointsPerBeam = pointR.length;
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName();
			
	public static void main(String[] args) {
		int nBeams = beamSelection.length;
		
		
		/* // For LOS finding for Maciej's high iota halpha
		sys.catchPlane.setInterface(NullInterface.ideal());
		sys.strayPlane.setInterface(NullInterface.ideal());		
		sys.tracingTarget = sys.entryWindowFront;
		//*/
		
		//double u[] = sys.portAxis;
		//double cyldLen = sys.portTubeLength;
		//double p[] = Util.minus(sys.portTubeCentre, Util.mul(u, cyldLen/2));
		//double cyldRadius = sys.portTubeDiameter / 2;
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/lightAssess-"+sys.getDesignName()+ ((writeWRLForDesigner != null) ? ("-" + writeWRLForDesigner + ".wrl") : ".vrml"), 1.005);
		if((writeWRLForDesigner == null)){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		vrmlOut.setSkipRays((nAttempts*nPointsPerBeam*nBeams) / nRaysToDraw);
		double col[][] = ColorMaps.jet(nPointsPerBeam);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		LightConeInfo lightConeInfo = new LightConeInfo(sys.fibrePlane);
		
		Optic fibreCyldsParallel = new Optic("fibreCylds-parallel");
		Optic fibreCyldsAligned = new Optic("fibreCylds-aligned");
		
		for(Surface s : sys.getSurfacesAll()){
			if(!(s instanceof Triangle))
				interestedSurfaces.add(s);
		}
		
		if(sys.overrideObsPositions != null) {
			nBeams = sys.overrideObsPositions.length;
			nPointsPerBeam = sys.overrideObsPositions[0].length;			
		}
		
		FibreInfo fibre[][] = new FibreInfo[nBeams][nPointsPerBeam];
		for(int iB=0; iB < nBeams; iB++){
			int beamSel = beamSelection[iB];
			
			for(int iP=0; iP < nPointsPerBeam; iP++){
				fibre[iB][iP] = new FibreInfo();
				
				//double R = R0 + iP * (R1 - R0) / (nPoints - 1.0);
				double R = pointR[iP];
				fibre[iB][iP].R = R;
				
				if(sys.overrideObsPositions == null) {
					fibre[iB][iP].beamPos = (beamSel < 0) ? beams.getPosOfBoxAxisAtR((-beamSel)-1, R) : beams.getPosOfBeamAxisAtR(beamSel, R);
				
					// For LOS finding for Maciej's high iota halpha
					//fibre[iB][iP].beamPos = new double[] { FastMath.cos(divTargetPhi) * R, FastMath.sin(divTargetPhi) * R, divTargetZ }; 
				
					R = FastMath.sqrt(fibre[iB][iP].beamPos[0]*fibre[iB][iP].beamPos[0] + fibre[iB][iP].beamPos[1]*fibre[iB][iP].beamPos[1]);
					
				}else {
					fibre[iB][iP].beamPos = sys.overrideObsPositions[iB][iP];
				}
				
				int nHit = 0;
				
				fibre[iB][iP].viewPos = new double[3];
				for(int i=0; i < nAttempts; i++){
					RaySegment ray = new RaySegment();
					ray.startPos = fibre[iB][iP].beamPos.clone();
					ray.dir = Tracer.generateRandomRayTowardSurface(fibre[iB][iP].beamPos, sys.tracingTarget);
					ray.wavelength = wavelength;
					ray.E0 = new double[][]{{1,0,0,0}};
					ray.up = Util.createPerp(ray.dir);
							
					Tracer.trace(sys, ray, 100, 0, false);
					
					ray.processIntersections(null, intensityInfo, lightConeInfo);
					

					List<Intersection> hits = ray.getIntersections(fibrePlane);
					if(hits.size() > 0){
						Intersection fibrePlaneHit = hits.get(0);
						
						//Intersection mirrorHit = fibrePlaneHit.incidentRay.findFirstEarlierIntersection(sys.mirror);						
						//if(mirrorHit == null){
							//System.err.println("WTF: Hit fibre plane without hitting mirror");
						//}else{
							fibre[iB][iP].viewPos[0] += ray.endHit.pos[0];
							fibre[iB][iP].viewPos[1] += ray.endHit.pos[1];
							fibre[iB][iP].viewPos[2] += ray.endHit.pos[2];
						//}						
						nHit++;
					}
					
					
					hits = ray.getIntersections(mustHitToDraw);
					if(hits.size() > 0){												
						vrmlOut.drawRay(ray, col[iP]);
					}
				}

				fibre[iB][iP].nHitPlane = nHit;
				fibre[iB][iP].viewPos[0] /= nHit;
				fibre[iB][iP].viewPos[1] /= nHit;
				
				fibre[iB][iP].viewPos[2] /= nHit;
				
				double dir[] = Tracer.generateRandomRayTowardSurface(fibre[iB][iP].beamPos, sys.tracingTarget, true);
				fibre[iB][iP].solidAngleFiring = Util.length(dir);
				
				double fillNA = 0;
				if(lightConeInfo.getRayAngles().length > 2){
					fibre[iB][iP].fibrePos = lightConeInfo.getApproxFocusPos();
					fibre[iB][iP].fibreMeanVec = lightConeInfo.getMeanVector();
					//fibreCylds.addElement(lightConeInfo.makeFibreCylinder(0.010, 0.000250, sys.fibrePlane.getNormal()));
					fibreCyldsAligned.addElement(lightConeInfo.makeFibreCylinder(0.010, 0.000250, null));
					fibreCyldsParallel.addElement(lightConeInfo.makeFibreCylinder(0.010, 0.000250, sys.fibrePlane.getNormal()));
					
					//BinaryMatrixFile.mustWrite(outPath + "/angles-p_" + iP + "-R_" + R + ".bin", lightConeInfo.getRayAngles());
					
					fillNA = FastMath.asin(lightConeInfo.getCapturingAngle(0.90));					
					fibre[iB][iP].fracInPerpCone = lightConeInfo.getFractionInPlanePerpCone(FastMath.asin(fibreNA));
					fibre[iB][iP].fracInMeanCone = lightConeInfo.getFractionInMeanCone(FastMath.asin(fibreNA));
				}else{
					fibre[iB][iP].fibrePos = new double[]{ Double.NaN, Double.NaN, Double.NaN };
					fibre[iB][iP].fibreMeanVec = new double[]{ Double.NaN, Double.NaN, Double.NaN };
					fibre[iB][iP].fracInPerpCone = Double.NaN;
					fibre[iB][iP].fracInMeanCone = Double.NaN;
				}
				
				fibre[iB][iP].effectiveSolidAngle =  (forcePerpFibres ? fibre[iB][iP].fracInPerpCone : fibre[iB][iP].fracInMeanCone)
														* fibre[iB][iP].solidAngleFiring * nHit / nAttempts; 
				

				System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
				System.out.println("B=Q" + (beamSel+1) + "\tP=" + iP + "(R=" + R + "):\t " + nHit + " of " + nAttempts + " attempts hit " + mustHitToDraw.getName() + " and have been drawn");
				
				
				System.out.println("SR = " + fibre[iB][iP].effectiveSolidAngle*1e6 + " µSR, fillNA = " + fillNA 
						+ ", in cone = " + (fibre[iB][iP].fracInMeanCone*100) + "%"
						+ ", lost by perpCone = " + ((fibre[iB][iP].fracInMeanCone - fibre[iB][iP].fracInPerpCone)*100) + "%" );


				intensityInfo.reset();
				lightConeInfo.reset();
				
			}
		}
		System.out.println("\n------------------------------------------------------------------------------------\n");
		
		
		addLosSolids(fibre);		
		//processImaging(fibre);
		dumpFibreInfo(fibre);
		
				
		if( ((Object)sys) instanceof BeamEmissSpecAEW21) 
			sys.removeElement(((BeamEmissSpecAEW21)(Object)sys).shieldTiles);
		
		//sys.addElement(fibreCylds);
		
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
		makeSTLFiles(fibreCyldsAligned, fibreCyldsParallel);
	}


	private static void dumpFibreInfo(FibreInfo[][] fibre) {
		
		DecimalFormat fmt = new DecimalFormat("#.###");
		//for(int iP=0; iP < sys.nFibres; iP++)
		//	
		System.out.println("public double[][] channelR = { ");			
		for(int iB=0; iB < fibre.length; iB++){
			System.out.print("\t{ ");
			for(int iP=0; iP < pointR.length; iP++)
				System.out.print(fmt.format(pointR[iP])+ ", ");
			System.out.println("}, ");
		}
				
		System.out.print("}; \npublic double[][][] fibreEndPos = { ");
		for(int iB=0; iB < fibre.length; iB++) {
			System.out.println("{ ");
			for(int iP=0; iP < fibre[iB].length; iP++)
				System.out.println("\t\t\t{ " + fibre[iB][iP].fibrePos[0] + ", " + fibre[iB][iP].fibrePos[1] + ", " +fibre[iB][iP].fibrePos[2] + " },");
			System.out.print("\t\t}, ");
		}			
			
		System.out.print("\t}; \npublic double[][][] fibreEndNorm = { ");
		for(int iB=0; iB < fibre.length; iB++) {
			System.out.println("{ ");
			for(int iP=0; iP < fibre[iB].length; iP++){
				if(forcePerpFibres)
					System.out.println("\t\t{ " + -sys.fibrePlane.getNormal()[0] + ", " + -sys.fibrePlane.getNormal()[1] + ", " + -sys.fibrePlane.getNormal()[2] + " },");
				else
					System.out.println("\t\t{ " + -fibre[iB][iP].fibreMeanVec[0] + ", " + -fibre[iB][iP].fibreMeanVec[1] + ", " + -fibre[iB][iP].fibreMeanVec[2] + " },");
			}
			System.out.print("\t\t}, ");
		}
		System.out.println("\t};");
		
		//also write for the python processing
		for(int iB=0;iB<fibre.length; iB++){
			double posArr[][] = new double[fibre[iB].length][4];
			for(int iF=0; iF < fibre[iB].length; iF++){
				posArr[iF][0] = fibre[iB][iF].R;
				posArr[iF][1] =fibre[iB][iF].fibrePos[0];
				posArr[iF][2] =fibre[iB][iF].fibrePos[1];
				posArr[iF][3] =fibre[iB][iF].fibrePos[2];
			}
			AsciiMatrixFile.mustWrite(outPath + "/fibrePositions-Q"+(beamSelection[iB]+1)+".txt", posArr, false);
		}
	}


	private static void processImaging(FibreInfo fibre[][]) {
		NumberFormat fmt = new DecimalFormat("##.#####");
		

		double fibreEtendue = FastMath.pow2(FastMath.PI * fibreCoreDiameter * fibreNA) /4;
		System.out.println("Fibre: d=" + fmt.format(fibreCoreDiameter/1e-6) + "µm, NA=" + fibreNA + ", étendue="+ fmt.format(fibreEtendue*1e12) + " µm² SR");
		
		BinaryMatrixWriter lightInfoOut = writeSolidAngeInfo ? new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 5) : null;
		
		//calc magnification
		for(int iB=0; iB < fibre.length; iB++){
			int beamSel = beamSelection[iB];
			int iP0 = -1;
			int iP1 = -1*1;
			for(int iP=0; iP < nPointsPerBeam; iP++){
				if(!Double.isNaN(fibre[iB][iP].fibrePos[0]) && fibre[iB][iP].nHitPlane > 10){
					if(iP0 < 0) iP0 = iP;
					iP1 = iP;
				}
			}
			double lengthAtBeam = Util.length(Util.minus(fibre[iB][iP1].beamPos, fibre[iB][iP0].beamPos));
			double lengthAtFibrePlane = Util.length(Util.minus(fibre[iB][iP1].fibrePos, fibre[iB][iP0].fibrePos));
			double fibrePlaneCentre[] = Util.mul(Util.plus(fibre[iB][iP1].fibrePos, fibre[iB][iP0].fibrePos), 0.5);
			double magnification = lengthAtBeam/lengthAtFibrePlane;
			double imageCentre[] = Util.mul(Util.plus(fibre[iB][iP1].fibrePos, fibre[iB][iP0].fibrePos), 0.5);
			//double fibrePlaneLensDist = Util.length(Util.minus(sys.lens1.getBackSurface().getCentre(), imageCentre));
			//double effLensDiameter = 2 * fibreNA * fibrePlaneLensDist;
						
			System.out.println("Q"+(beamSel+1) 
					+ ", Lbeam = " + fmt.format(lengthAtBeam) 
					+ ", Lfibre = " + fmt.format(lengthAtFibrePlane) 
					+ ", M=" + fmt.format(magnification)
					+ ", dSpot=" + fmt.format(fibreCoreDiameter * magnification)
			//		+ ", a = " +  fmt.format(fibrePlaneLensDist)
			//		+ ", dlEff = " +  fmt.format(effLensDiameter)
					+ ":");
			
			for(int iP=0; iP < nPointsPerBeam; iP++){
				double etendue = FastMath.PI * FastMath.pow2(fibreCoreDiameter/2 * magnification) * fibre[iB][iP].effectiveSolidAngle;
				System.out.println("\tR="+fmt.format(pointR[iP])
									+ ", Df=" + fmt.format(Util.length(Util.minus(fibre[iB][iP].fibrePos, fibrePlaneCentre)))
									+ ", SR=" + fmt.format(fibre[iB][iP].effectiveSolidAngle*1e6) + "µSR"
									+ ", étd =" + fmt.format(etendue*1e12) + " µm² SR"
									+ ", " + fmt.format(etendue / fibreEtendue * 100) + "%, "
									+ ((etendue > fibreEtendue) ? "overfilled" : "** UNDERFILLED **"));
				
				if(lightInfoOut != null)
					lightInfoOut.writeRow(beamSel, iP, fibre[iB][iP].R, fibre[iB][iP].effectiveSolidAngle, etendue);
				
				
			}
			
		}
		
		lightInfoOut.close();
		
		
	}

	private static void addLosSolids(FibreInfo fibre[][]) {
		//LOS Cylinders
		double cyldRadius = 0.01;
		for(int iB=0; iB < fibre.length; iB++){
			int beamSel = beamSelection[iB];
			for(int iP=0; iP < nPointsPerBeam; iP++){
				if(fibre[iB][iP].nHitPlane <= 0)
					continue;
				
				Cylinder losCyld = new Cylinder("los-Q"+(beamSel+1) +"-R" + pointR[iP], 
						Util.mul(Util.plus(fibre[iB][iP].beamPos, fibre[iB][iP].viewPos), 0.5),
						Util.reNorm(Util.minus(fibre[iB][iP].viewPos, fibre[iB][iP].beamPos)),
						0.002, 5.0, NullInterface.ideal());
				
				sys.addElement(losCyld);
				
				double u[] = Util.minus(fibre[iB][iP].beamPos, fibre[iB][iP].viewPos);
				double cyldLen = Util.length(u) * 1.2;
				u = Util.reNorm(u);
				//double p[] = Util.mul(Util.plus(fibre[iB][iP].beamPos, fibre[iB][iP].viewPos), 0.5);
				double p[] = Util.plus(fibre[iB][iP].viewPos, Util.mul(u, -0.2));
				 		
				System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Cylinder\", \"FibreEnd"+iB+"_"+iP+"\"); "+
						"o.Shape = Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
						",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 +
						"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360);");
			}		
		}
		
		//Effective virtual obs position: average of closest approach of each LOSes
		int n=0;
		double avgObsPos[] = new double[3];
		for(int iB=0; iB < fibre.length; iB++){
			for(int iP=0; iP < nPointsPerBeam; iP++){
				for(int iB2=0; iB2 < fibre.length; iB2++){
					for(int iP2=0; iP < nPointsPerBeam; iP++){
						if((iB2 == iB && iP2 == iP) || fibre[iB][iP].nHitPlane <= 0 || fibre[iB2][iP2].nHitPlane <= 0)
							continue;
						
						double dLa[] = Util.reNorm(Util.minus(fibre[iB][iP].viewPos, fibre[iB][iP].beamPos));
						double dLb[] = Util.reNorm(Util.minus(fibre[iB2][iP2].viewPos, fibre[iB2][iP2].beamPos));
						
						double a = Algorithms.pointOnLineNearestAnotherLine(fibre[iB][iP].viewPos, dLa, fibre[iB2][iP2].viewPos, dLb);
						double p0a[] = Util.plus(fibre[iB][iP].viewPos, Util.mul(dLa, a));
						a = Algorithms.pointOnLineNearestAnotherLine(fibre[iB2][iP2].viewPos, dLb, fibre[iB][iP].viewPos, dLa);
						double p0b[] = Util.plus(fibre[iB2][iP2].viewPos, Util.mul(dLb, a));
						double p[] = Util.mul(Util.plus(p0a, p0b), 0.5);
						avgObsPos[0] += p[0];
						avgObsPos[1] += p[1];
						avgObsPos[2] += p[2];
						n++;
					}	
				}
			}
		}
		avgObsPos[0] /= n; avgObsPos[1] /= n; avgObsPos[2] /= n;
		
		System.out.print("Virtual obs pos: "); OneLiners.dumpArray(avgObsPos);
		if(!Double.isNaN(Util.length(avgObsPos))){
			Sphere virtObs = new Sphere("virtObs", avgObsPos, 0.005, NullInterface.ideal());
			sys.addElement(virtObs);
		}
		
	}
	

	private static void makeSTLFiles(Optic fibreCyldsAligned, Optic fibreCyldsParallel) {

		STLDrawer stlDrawer = new STLDrawer(outPath + "/fibreCylds-aligned-"+sys.getDesignName()+".stl");		
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
		stlDrawer.drawOptic(fibreCyldsAligned);
		stlDrawer.destroy();
		
		stlDrawer = new STLDrawer(outPath + "/fibreCylds-parallel-"+sys.getDesignName()+".stl");		
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
		stlDrawer.drawOptic(fibreCyldsParallel);
		stlDrawer.destroy();
		
		/*stlDrawer = new STLDrawer(outPath + "/model-"+sys.getDesignName()+".stl");
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
		stlDrawer.ignoreElementType(Iris.class);
		stlDrawer.ignoreElementType(STLMesh.class);
		stlDrawer.drawOptic(sys);
		stlDrawer.destroy(); //*/
		
		/*for(Element elem : sys.makeSimpleModel()){
			elem.setApproxDrawQuality(50);
			stlDrawer = new STLDrawer(outPath + "/simpleModel/" + elem.getName() + ".stl");			
			stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
			stlDrawer.ignoreElementType(Iris.class);
			stlDrawer.ignoreElementType(STLMesh.class);
			stlDrawer.drawElement(elem);
			stlDrawer.destroy();
		}*/
	}
}
