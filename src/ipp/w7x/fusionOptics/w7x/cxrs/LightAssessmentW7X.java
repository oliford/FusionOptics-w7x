package ipp.w7x.fusionOptics.w7x.cxrs;

import ipp.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.neutralBeams.W7xNBI;
import jafama.FastMath;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.ColorMaps;
import otherSupport.ScientificNumberFormat;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.HitPositionAverage;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Triangle;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Basic pictures for BeamEmissSpecAET21 model */
public class LightAssessmentW7X {
	
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	//public static BeamEmissSpecAET21 sys = new BeamEmissSpecAET21();
	//public static Surface mustHitToDraw = sys.fibrePlane;
	public static BeamEmissSpecAEM21 sys = new BeamEmissSpecAEM21();
	public static Surface mustHitToDraw = sys.fibrePlane;
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	public static List<Surface> interestedSurfaces = new ArrayList<Surface>();
	
	public static Surface fibrePlane = sys.fibrePlane;

	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static Surface mustHitToDraw = sys.entryWindowFront;
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	//public final static double R0 = 5.2, R1 = 5.9; //as sightlines in fromDesigner-201511076 
	
	//public static int beamSelection[] = { beams.BEAM_Q5, beams.BEAM_Q6, beams.BEAM_Q7, beams.BEAM_Q8 };  
	public static int beamSelection[] = { beams.BEAM_Q6, beams.BEAM_Q8 }; //for AEM21, 6 and 8 are the extremes
	
	// For fast drawing/debugging
	public static double pointR[] = { 5.50, 5.78, 6.05 };
	//public static double pointR[] = OneLiners.linSpace(5.5, 6.01, 0.10);
	//public static double pointR[] = OneLiners.linSpace(5.5, 6.051, 30);
	//public final static int nAttempts = 5000;
	
	public static boolean writeSolidAngeInfo = true;
	public static boolean writeWRLForDesigner = true;
	public final static int nAttempts = 1000;
	//*/
	
	public static double wavelength = 650e-9;
	// For calc
	/*public static double pointR[] = sys.channelR;
	public final static int nAttempts = 20000;
	//*/
	
	public static int nPointsPerBeam = pointR.length;
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName();
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
		
	public static void main(String[] args) {
			
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/lightAssess-"+sys.getDesignName()+ (writeWRLForDesigner ? ".wrl" : ".vrml"), 1.005);
		if(!writeWRLForDesigner){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		vrmlOut.setSkipRays(nAttempts*nPointsPerBeam / 200000);
		double col[][] = ColorMaps.jet(nPointsPerBeam);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = writeSolidAngeInfo ? new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4) : null;
		
		
		for(Surface s : sys.getSurfacesAll()){
			if(!(s instanceof Triangle))
				interestedSurfaces.add(s);
		}
		
		int nBeams = beamSelection.length;
		double beamPos[][][] = new double[nBeams][nPointsPerBeam][];
		double viewPos[][][] = new double[nBeams][nPointsPerBeam][3];
		double fibrePos[][][] = new double[nBeams][nPointsPerBeam][3];	
		double solidAngleFP[][] = new double[nBeams][nPointsPerBeam];
		for(int iB=0; iB < nBeams; iB++){
			int beamSel = beamSelection[iB];
			
			for(int iP=0; iP < nPointsPerBeam; iP++){
				
				//double R = R0 + iP * (R1 - R0) / (nPoints - 1.0);
				double R = pointR[iP];
				
				beamPos[iB][iP] = (beamSel < 0) ? beams.getPosOfBoxAxisAtR(1, R) : beams.getPosOfBeamAxisAtR(beamSel, R);
				R = FastMath.sqrt(beamPos[iB][iP][0]*beamPos[iB][iP][0] + beamPos[iB][iP][1]*beamPos[iB][iP][1]);
				int nHit = 0;
				
				for(int i=0; i < nAttempts; i++){
					RaySegment ray = new RaySegment();
					ray.startPos = beamPos[iB][iP].clone();
					ray.dir = Tracer.generateRandomRayTowardSurface(beamPos[iB][iP], sys.tracingTarget);
					ray.wavelength = wavelength;
					ray.E0 = new double[][]{{1,0,0,0}};
					ray.up = Util.createPerp(ray.dir);
							
					Tracer.trace(sys, ray, 100, 0, false);
					
					ray.processIntersections(null, intensityInfo);
					

					List<Intersection> hits = ray.getIntersections(fibrePlane);
					if(hits.size() > 0){
						Intersection fibrePlaneHit = hits.get(0);
						fibrePos[iB][iP][0] += fibrePlaneHit.pos[0];
						fibrePos[iB][iP][1] += fibrePlaneHit.pos[1];
						fibrePos[iB][iP][2] += fibrePlaneHit.pos[2];
						
						Intersection mirrorHit = fibrePlaneHit.incidentRay.findFirstEarlierIntersection(sys.mirror);
						if(mirrorHit == null){
							System.err.println("WTF: Hit fibre plane without hitting mirror");
						}else{
							viewPos[iB][iP][0] += mirrorHit.pos[0];
							viewPos[iB][iP][1] += mirrorHit.pos[1];
							viewPos[iB][iP][2] += mirrorHit.pos[2];
						}
						
						nHit++;
					}
						
					
					hits = ray.getIntersections(mustHitToDraw);
					if(hits.size() > 0){												
						vrmlOut.drawRay(ray, col[iP]);
					}
				}
				
				double dir[] = Tracer.generateRandomRayTowardSurface(beamPos[iB][iP], sys.tracingTarget, true);
				double targetSolidAngle = Util.length(dir);
				
				solidAngleFP[iB][iP] = intensityInfo.getSourceSolidAng(sys.fibrePlane, targetSolidAngle, nAttempts);
				if(lightInfoOut != null)
					lightInfoOut.writeRow(beamSel, iP, R, solidAngleFP[iB][iP]);
				
				System.out.println("\n---------------------------------------- "+iP+" ----------------------------------------");
				System.out.println("B=Q" + (beamSel+1) + "\tP=" + iP + "(R=" + R + "):\t " + nHit + " of " + nAttempts + " attempts hit " + mustHitToDraw.getName() + " and have been drawn");
				//intensityInfo.dump(interestedSurfaces, null, true, 0, 0);
				System.out.println("SR = " + solidAngleFP[iB][iP]*1e6 + " µSR");
				intensityInfo.reset();
				
				fibrePos[iB][iP][0] /= nHit;
				fibrePos[iB][iP][1] /= nHit;
				fibrePos[iB][iP][2] /= nHit;

				viewPos[iB][iP][0] /= nHit;
				viewPos[iB][iP][1] /= nHit;
				viewPos[iB][iP][2] /= nHit;				
			}
		}
		
		//double fibreCoreDiameter = 400e-6;
		double fibreCoreDiameter = 400e-6;
		double fibreNA = 0.22;
		double fibreEtendue = FastMath.pow2(FastMath.PI * fibreCoreDiameter * fibreNA) /4;
		System.out.println("Fibre: d=" + fibreCoreDiameter/1e-6 + "µm, NA=" + fibreNA + ", étendue="+fibreEtendue);
		
		//calc magnification
		for(int iB=0; iB < nBeams; iB++){
			int beamSel = beamSelection[iB];			
			double lengthAtBeam = Util.length(Util.minus(beamPos[iB][nPointsPerBeam-1], beamPos[iB][0]));
			double lengthAtFibrePlane = Util.length(Util.minus(fibrePos[iB][nPointsPerBeam-1], fibrePos[iB][0]));
			double magnification = lengthAtBeam/lengthAtFibrePlane;
			double imageCentre[] = Util.mul(Util.plus(fibrePos[iB][nPointsPerBeam-1], fibrePos[iB][0]), 0.5);
			double fibrePlaneLensDist = Util.length(Util.minus(sys.lensCentrePos, imageCentre));
			double effLensDiameter = 2 * fibreNA * fibrePlaneLensDist;
			NumberFormat fmt = new DecimalFormat("##.###");
						
			System.out.println("Q"+(beamSel+1) 
					+ ", Lbeam = " + fmt.format(lengthAtBeam) 
					+ ", Lfibre = " + fmt.format(lengthAtFibrePlane) 
					+ ", M=" + fmt.format(magnification)
					+ ", dSpot=" + fmt.format(fibreCoreDiameter * magnification)
					+ ", a = " +  fmt.format(fibrePlaneLensDist)
					+ ", dlEff = " +  fmt.format(effLensDiameter)
					+ ":");
			
			for(int iP=0; iP < nPointsPerBeam; iP++){
				double etendue = FastMath.PI * FastMath.pow2(fibreCoreDiameter/2 * magnification) * solidAngleFP[iB][iP];
				System.out.println("\tR="+fmt.format(pointR[iP])
									+ ", SR=" + fmt.format(solidAngleFP[iB][iP]*1e6) + "µSR"
									+ ", étd =" + etendue
									+ ((etendue > fibreEtendue) ? ", overfilled" : ", ** UNDERFILLED **"));
				
			}
			
		}
		
	
		// virtual observation calc
		for(int iB=0; iB < nBeams; iB++){
			int beamSel = beamSelection[iB];
			for(int iP=0; iP < nPointsPerBeam; iP++){
				Cylinder losCyld = new Cylinder("los-Q"+(beamSel+1) +"-R" + pointR[iP], 
						Util.mul(Util.plus(beamPos[iB][iP], viewPos[iB][iP]), 0.5),
						Util.reNorm(Util.minus(viewPos[iB][iP], beamPos[iB][iP])),
						0.005, 5.0, NullInterface.ideal());
				
				sys.addElement(losCyld);
			}
		}
		
		int n=0;
		double avgObsPos[] = new double[3];
		for(int iB=0; iB < nBeams; iB++){
			for(int iP=0; iP < nPointsPerBeam; iP++){
				for(int iB2=0; iB2 < nBeams; iB2++){
					for(int iP2=0; iP < nPointsPerBeam; iP++){
						if(iB2 == iB && iP2 == iP)
							continue;
						
						double dLa[] = Util.reNorm(Util.minus(viewPos[iB][iP], beamPos[iB][iP]));
						double dLb[] = Util.reNorm(Util.minus(viewPos[iB2][iP2], beamPos[iB2][iP2]));
						
						double a = Algorithms.pointOnLineNearestAnotherLine(viewPos[iB][iP], dLa, viewPos[iB2][iP2], dLb);
						double p0a[] = Util.plus(viewPos[iB][iP], Util.mul(dLa, a));
						a = Algorithms.pointOnLineNearestAnotherLine(viewPos[iB2][iP2], dLb, viewPos[iB][iP], dLa);
						double p0b[] = Util.plus(viewPos[iB2][iP2], Util.mul(dLb, a));
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
		Sphere virtObs = new Sphere("virtObs", avgObsPos, 0.02, NullInterface.ideal());
		sys.addElement(virtObs);
		//*/
		
		if(lightInfoOut != null)
			lightInfoOut.close();
				
		if( ((Object)sys) instanceof BeamEmissSpecAEW21) 
			sys.removeElement(((BeamEmissSpecAEW21)(Object)sys).shieldTiles);
			
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
}
