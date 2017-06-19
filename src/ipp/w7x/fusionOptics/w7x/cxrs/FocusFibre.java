package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.LightAssessmentW7X.FibreInfo;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEM41;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import jafama.FastMath;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;
import seed.optimization.HookeAndJeeves;
import seed.optimization.Optimizer;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.collection.IntersectionProcessor;
import fusionOptics.collection.PlaneAngleInfo;
import fusionOptics.drawing.SVGRayDrawing;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.optimisationMulti.MoveableElement;
import fusionOptics.optimisationMulti.OptimiseMulti;
import fusionOptics.optimisationMulti.RayBundle;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Basic pictures for BeamEmissSpecAET21 model */
public class FocusFibre {
	
	//public static BeamEmissSpecAET21_postDesign sys = new BeamEmissSpecAET21_postDesign();
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	public static BeamEmissSpecAEM21_postDesign sys = new BeamEmissSpecAEM21_postDesign();
	public static SimpleBeamGeometry beams = W7xNBI.def();
	
	//public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	//public static SimpleBeamGeometry beams = W7XRudix.def();
	 
	// For fast drawing/debugging
	//public final static int nPoints = 10;
	//public final static double R0 = 5.2;  
	//public final static double R1 = 5.9;
	
	public final static int nRaysPerBeamPoint = 200;
	
	//*/
	
	// For calc
	/*public final static int nPoints = 20;
	public final static double R0 = 5.3;
	public final static double R1 = 6.1;
	public final static int nAttempts = 5000;
	public final static int beamSelect = -1;
	//*/

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/focusFibres";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static void main(String[] args) {
				
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/focusFibres-"+sys.getDesignName()+".vrml", 1.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		//vrmlOut.setSkipRays(nAttempts*sys.nFibres / 500);						
		double col[][] = ColorMaps.jet(sys.channelR[0].length);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4); 
				
		//Need to get through the single common fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());
		sys.beamPlane.setInterface(NullInterface.ideal());
		//public final static int beamIndex = W7xNBI.BEAM_Q7;
		
		double fibreNorm[][][] = new double[sys.channelR.length][][];
		double fibrePos[][][] = new double[sys.channelR.length][][];
		double R[][] = new double[sys.channelR.length][];
		
		for(int iB=0; iB < sys.channelR.length; iB++){
			int nFibres = sys.channelR[iB].length;
			fibreNorm[iB] = new double[nFibres][];
			fibrePos[iB] = new double[nFibres][];
			R[iB] = new double[nFibres];
			
			for(int iP=0; iP < nFibres; iP++){
				sys.fibrePlanes[iB][iP].setInterface(Absorber.ideal());
				sys.fibrePlanes[iB][iP].setWidth(0.100);
				sys.fibrePlanes[iB][iP].setHeight(0.100);
				sys.addElement(sys.fibrePlanes[iB][iP]);
				
				OptimiseMulti optim = new OptimiseMulti();
				
				optim.setTracingElements(sys);
				
				double startPos[] = getBeamPlanePos(iB, iP);
				if(startPos == null){
					fibrePos[iB][iP] = new double[]{ Double.NaN, Double.NaN, Double.NaN };
					fibreNorm[iB][iP] = new double[]{ Double.NaN, Double.NaN, Double.NaN };
					continue;
				}
				
				R[iB][iP] = FastMath.sqrt(startPos[0]*startPos[0] + startPos[1]*startPos[1]);
				
				RayBundle rayBundle = new RayBundle(iP);
				
				rayBundle.setImagePlane(sys.fibrePlanes[iB][iP]);
				//rayBundle.setSharpnessWeight(1.0); //we can weight the sharpness, intensity and targeting separately for each source point
				rayBundle.setSharpnessWeights(1.0, 0.0);
				rayBundle.setIntensityWeight(0.0); 
				rayBundle.setTargetWeight(1.0);
				rayBundle.initRaysImaging(sys.tracingTarget, startPos, sys.designWavelenth, nRaysPerBeamPoint);
				//rayBundle.setMinimumHitsForStatistics(50);
				
				//ArrayList<Surface> seqList = sys.getFastSequentialTrace();
				//ArrayList<Surface> seqList = (ArrayList<Surface>)seqListToSallyImage.clone();
				//seqList.add(imagePlane);
				//rayBundle.setSequentialSurfaceList(seqList);
				optim.addRegularDrawing(outPath + "/optim-ch_"+iP, VRMLDrawer.class, 0, vrmlScaleToAUGDDD, "}");
				
				//try{
					rayBundle.setOptimiser(optim);
					//rayBundle.findTarget(); //set target to current mean position
					rayBundle.setTargetPos(new double[]{0, 0});
					optim.addRayBundle(rayBundle);
				//}catch(RuntimeException err){
				//	System.err.println("Not adding ray bundle for point " + iP + " at R = " + R[iP]);
				//}
				
				rayBundle.setMinimumHitsForStatistics(5);
				
				//double fibreVec[] = Util.reNorm(Util.minus(sys.lensCentrePos, sys.fibreEndPos[iP]));
				double fibreVec[] = sys.fibreEndNorm[iB][iP];
				MoveableElement p = new MoveableElement(sys.fibrePlanes[iB][iP], fibreVec, -0.050, 0.050); p.setScale(0.001); optim.addParameter(p);
				//{MoveableElement p = new MoveableElement(sys.fibrePlanes[iP], new double[]{1,0,0}, -0.050, 0.050); p.setScale(0.001); optim.addParameter(p);}
				//{MoveableElement p = new MoveableElement(sys.fibrePlanes[iP], new double[]{0,1,0}, -0.050, 0.050); p.setScale(0.001); optim.addParameter(p);}
				//{MoveableElement p = new MoveableElement(sys.fibrePlanes[iP], new double[]{0,0,1}, -0.050, 0.050); p.setScale(0.001); optim.addParameter(p);}
				
				Optimizer opt = new HookeAndJeeves(null);	
			    optim.setOptimiser(opt);
	
			    optim.setOutputPrefix(outPath + "/optim-ch_"+iP); 
				optim.addRegularDrawing(outPath + "/optim-ch_"+iP, VRMLDrawer.class, 0, "Separator {\nScale { scaleFactor 1000 1000 1000 }", "}");
				//optim.addRegularDrawing(outPath + "/optim-ch_"+iP, SVGRayDrawing.class, 1);
				optim.setOutputIterationPeriod(10);
						
				optim.eval(); //eval init position will set target positions at the image plane
				optim.dumpParams();
				optim.dumpRayBundles();
				optim.optimise(20);
				
				optim.dumpParams();
				optim.dumpRayBundles();
				
				//shift[iP] = sys.shift[iP] + p.get(); //add to existing shift
				fibrePos[iB][iP] = sys.fibrePlanes[iB][iP].getCentre();
				
				//calc average of vector into plane, to find the best normal for the plane
				
				PlaneAngleInfo paInfo = new PlaneAngleInfo(sys.fibrePlanes[iB][iP]);
				rayBundle.setExtraIntersectionProcessor(paInfo);
				rayBundle.traceAndEvaluate(null);
				rayBundle.setExtraIntersectionProcessor(null);
				
				fibreNorm[iB][iP] = paInfo.getMeanVector();
				
				
				sys.fibrePlanes[iB][iP].setInterface(NullInterface.ideal());
				sys.fibrePlanes[iB][iP].setWidth(0.001);
				sys.fibrePlanes[iB][iP].setHeight(0.001);
				
				System.out.println("[" + iP + "]:p = \t\t{ " + fibrePos[iB][iP][0] + ", " +fibrePos[iB][iP][1] + ", " +fibrePos[iB][iP][2] + " },");
				System.out.println("[" + iP + "]:v = \t\t{ " + -fibreNorm[iB][iP][0] + ", " + -fibreNorm[iB][iP][1] + ", " + -fibreNorm[iB][iP][2] + " },");
				
				sys.removeElement(sys.fibrePlanes[iB][iP]);
			}
		}
		
		dumpFibreInfo(R, fibrePos, fibreNorm);
		
		lightInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
	

	private static void dumpFibreInfo(double R[][], double fibrePos[][][], double fibreNorm[][][]) {
		DecimalFormat fmt = new DecimalFormat("#.###");
	
		System.out.println("public double[][] channelR = { ");			
		for(int iB=0; iB < R.length; iB++){
			System.out.print("\t{ ");
			for(int iP=0; iP < R[iB].length; iP++)
				System.out.print(fmt.format(R[iB][iP])+ ", ");
			System.out.println("}, ");
		}
				
		System.out.print("}; \npublic double[][][] fibreEndPos = { ");
		for(int iB=0; iB < fibrePos.length; iB++) {
			System.out.println("{ ");
			for(int iP=0; iP < fibrePos[iB].length; iP++)
				System.out.println("\t\t\t{ " + fibrePos[iB][iP][0] + ", " +fibrePos[iB][iP][1] + ", " +fibrePos[iB][iP][2] + " },");
				
			System.out.print("\t\t}, ");
		}			
			
		System.out.print("\t}; \npublic double[][] fibreEndNorm = { ");
		for(int iB=0; iB < fibrePos.length; iB++) {
			System.out.println("{ ");
			for(int iP=0; iP < fibrePos[iB].length; iP++)
				System.out.println("\t\t\t{ " + -fibreNorm[iB][iP][0] + ", " + -fibreNorm[iB][iP][1] + ", " + -fibreNorm[iB][iP][2] + " },");		
			System.out.print("\t\t}, ");
		}
		
		
		System.out.println("\t}; \npublic double[][] fibreFocus = { ");
		for(int iB=0; iB < fibrePos.length; iB++) {
			System.out.println("{ ");
			for(int iP=0; iP < fibrePos[iB].length; iP++)
				System.out.println("\t\t\t " + (sys.fibreFocus[iB][iP] + Util.dot(Util.minus(fibrePos[iB][iP], sys.fibreEndPos[iB][iP]), sys.fibreEndNorm[iB][iP])) + " ,");
			System.out.print("\t\t}, ");
		}
		System.out.println("\t};");
	}


	private static double[] getBeamPlanePos(int iB, int iP) {
		
		//return beams.getPosOfBeamAxisAtR(beamIndex, sys.channelR[iP]);
		sys.addElement(sys.beamPlane);
		//trace the central ray forwards and see where we hit the beam plane
		List<Intersection> hits = null;
		for(int i=0; i < 1000; i++){
			
			RaySegment ray = new RaySegment();		 
			ray.startPos = Util.plus(sys.fibreEndPos[iB][iP], Util.mul(sys.fibreEndNorm[iB][iP], 1e-6));
			ray.dir = Tracer.generateRandomRayTowardSurface(sys.fibreEndPos[iB][iP], sys.mirror);
			ray.length = Double.POSITIVE_INFINITY;
			ray.wavelength = sys.designWavelenth;
			ray.E0 = new double[][]{{1,0,0,0}};
			ray.up = Util.createPerp(ray.dir);
					
			Tracer.trace(sys, ray, 100, 0, false);
			
			//ray.dumpPath();
			//VRMLDrawer.dumpRay("/tmp/fail.vrml", sys, ray, 0.005, vrmlScaleToAUGDDD, "}");
			
			hits = ray.getIntersections(sys.beamPlane);
		
			if(hits.size() > 0)
				return hits.get(0).pos; 
		}
		
		System.err.println("Failed to find start position for point " + iP);
		return null;
		
	}
}
