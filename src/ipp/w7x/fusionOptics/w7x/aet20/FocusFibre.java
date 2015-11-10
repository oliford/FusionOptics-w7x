package ipp.w7x.fusionOptics.w7x.aet20;

import ipp.neutralBeams.SimpleBeamGeometry;
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
import fusionOptics.drawing.SVGRayDrawing;
import fusionOptics.drawing.VRMLDrawer;
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
	
	//public static BeamEmissSpecAET21 sys = new BeamEmissSpecAET21();
	//public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	//public static BeamEmissSpecAEB20 sys = new BeamEmissSpecAEB20();
	//public static SimpleBeamGeometry beams = W7xNBI.def();

	public static BeamEmissSpecAEM41 sys = new BeamEmissSpecAEM41();
	public static SimpleBeamGeometry beams = W7XRudix.def();
	 
	// For fast drawing/debugging
	//public final static int nPoints = 10;
	//public final static double R0 = 5.2;  
	//public final static double R1 = 5.9;
	public final static int nAttempts = 1000;
	public final static int nRaysPerBeamPoint = 5000;
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
		vrmlOut.setSkipRays(nAttempts*sys.nFibres / 500);
		double col[][] = ColorMaps.jet(sys.nFibres);
		
		IntensityInfo intensityInfo = new IntensityInfo(sys);
		BinaryMatrixWriter lightInfoOut = new BinaryMatrixWriter(outPath + "/sourceSolidAng.bin", 4); 
				
		//Need to get through the single common fibre plane
		sys.fibrePlane.setInterface(NullInterface.ideal());
		sys.beamPlane.setInterface(NullInterface.ideal());
		
		double shift[] = new double[sys.nFibres];
		double R[] = new double[sys.nFibres];
		
		for(int iP=0; iP < sys.nFibres; iP++){
			sys.fibrePlanes[iP].setWidth(0.100);
			sys.fibrePlanes[iP].setHeight(0.100);
			
			OptimiseMulti optim = new OptimiseMulti();
			
			optim.setTracingElements(sys);
			
			double startPos[] = getBeamPlanePos(iP);
			
			R[iP] = FastMath.sqrt(startPos[0]*startPos[0] + startPos[1]*startPos[1]);
			
			RayBundle rayBundle = new RayBundle(iP); // bundle IDs are beam number * 1000, so 5th point of Q7 is 7004
			
			rayBundle.setImagePlane(sys.fibrePlanes[iP]);
			rayBundle.setSharpnessWeight(1.0); //we can weight the sharpness, intensity and targeting separately for each source point
			rayBundle.setIntensityWeight(0.0); 
			rayBundle.setTargetWeight(0.0);
			rayBundle.initRaysImaging(sys.lens1, startPos, sys.designWavelenth, nRaysPerBeamPoint);
			rayBundle.setMinimumHitsForStatistics(50);
			
			//ArrayList<Surface> seqList = sys.getFastSequentialTrace();
			//ArrayList<Surface> seqList = (ArrayList<Surface>)seqListToSallyImage.clone();
			//seqList.add(imagePlane);
			//rayBundle.setSequentialSurfaceList(seqList);
			optim.addRegularDrawing(outPath + "/optim-ch_"+iP, VRMLDrawer.class, 0);
			
			//try{
				rayBundle.setOptimiser(optim);
				rayBundle.findTarget(); //set target to current mean position
				optim.addRayBundle(rayBundle);
			//}catch(RuntimeException err){
			//	System.err.println("Not adding ray bundle for point " + iP + " at R = " + R[iP]);
			//}
			
			rayBundle.setMinimumHitsForStatistics(5);
			
			double fibreVec[] = Util.reNorm(Util.minus(sys.lensCentrePos, sys.fibreEndPos[iP]));
			MoveableElement p = new MoveableElement(sys.fibrePlanes[iP], fibreVec, -0.050, 0.050); p.setScale(0.001); optim.addParameter(p);
			
			Optimizer opt = new HookeAndJeeves(null);	
		    optim.setOptimiser(opt);

		    optim.setOutputPrefix(outPath + "/optim-ch_"+iP); 
			//optim.addRegularDrawing(outPath + "/optim-ch_"+iP, VRMLDrawer.class, 1);
			//optim.addRegularDrawing(outPath + "/optim-ch_"+iP, SVGRayDrawing.class, 1);
			optim.setOutputIterationPeriod(10);
					
			optim.eval(); //eval init position will set target positions at the image plane
			optim.dumpParams();
			optim.dumpRayBundles();
			optim.optimise(40);
			
			optim.dumpParams();
			optim.dumpRayBundles();
			
			shift[iP] = sys.shift[iP] + p.get(); //add to existing shift
			
		}
		
		DecimalFormat fmt = new DecimalFormat("#.###");
		System.out.print("public double[] R = { ");
		for(int iP=0; iP < sys.nFibres; iP++)
			System.out.print(fmt.format(R[iP])+ ", ");
		System.out.print("}; \npublic double[] shift = { ");
		for(int iP=0; iP < sys.nFibres; iP++)
			System.out.print(fmt.format(shift[iP])+ ", ");
		System.out.println("};");
		
		lightInfoOut.close();
				
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}

	private static double[] getBeamPlanePos(int iP) {
		//trace the central ray forwards and see where we hit the beam plane
		RaySegment ray = new RaySegment();
		ray.startPos = sys.fibreEndPos[iP];
		ray.dir = Util.reNorm(Util.minus(sys.lensCentrePos, sys.fibreEndPos[iP]));
		ray.wavelength = sys.designWavelenth;
		ray.E0 = new double[][]{{1,0,0,0}};
		ray.up = Util.createPerp(ray.dir);
				
		Tracer.trace(sys, ray, 100, 0, false);
		
		List<Intersection> hits = ray.getIntersections(sys.beamPlane);
		return hits.get(0).pos;
	}
}
