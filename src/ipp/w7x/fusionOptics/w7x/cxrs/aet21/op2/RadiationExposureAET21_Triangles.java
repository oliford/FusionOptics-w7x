package ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2;

import net.jafama.FastMath;
import oneLiners.OneLiners;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import binaryMatrixFile.AsciiMatrixFile;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.optics.STLMesh;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Square;
import fusionOptics.surfaces.Triangle;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;
import ipp.w7x.fusionOptics.w7x.cxrs.RadiationExposureTriangles;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposureAET21_Triangles extends RadiationExposureTriangles {
	
	public BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7 sys = new BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7();
	
	//@Override public Element tracingTarget() { return sys.portTubeCyld; }
	@Override public Element tracingTarget() { return sys.entryAperture; }
	
	@Override
	public void start() {
		
		inPath = "/work/cad/aet21/op2/radExposure2";
		outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure2-entryAperture-bugfix/";
		
		testElements = new Element[]{
				new STLMesh(inPath + "/hst-M1.stl"),
				new STLMesh(inPath + "/hst-M2.stl"),
				new STLMesh(inPath + "/hst-L1.stl"),
				
				new STLMesh(inPath + "/cxrs-M1.stl"),
				new STLMesh(inPath + "/cxrs-L1.stl"),
				
				new STLMesh(inPath + "/graphiteBottom.stl"),
				new STLMesh(inPath + "/graphiteTop.stl"),
				new STLMesh(inPath + "/frontPlateInner.stl"),
				
				new STLMesh(inPath + "/tube.stl"),
				
				//new STLMesh(inPath + "/shutterClosed.stl"),
				new STLMesh(inPath + "/shutterOpen.stl"),
				
				new STLMesh(inPath + "/coolingRing.stl"),
				new STLMesh(inPath + "/coolingRingPlate.stl"),
					
		};
		
		thingsInWay = new Element[]{	
				new STLMesh(inPath + "/panel-aet21-1.stl"),
				new STLMesh(inPath + "/panel-aet21-2.stl"),
				
				new STLMesh(inPath + "/port-aet21.stl"),
				new STLMesh(inPath + "/portLiner-aet21-simple.stl"),
				new STLMesh(inPath + "/shield-aet21.stl"),
				new STLMesh(inPath + "/baffle-aet21.stl"),
				new STLMesh(inPath + "/closure-aet21.stl"),	
		};

		nRays = 50000000;
		nRaysToDraw = 5000;
		nThreads =12;
		
		sys.removeElement(sys.beamPlane);
		
		super.start();
	}
	
	@Override
	public Optic sys() { return sys; }
	@Override
	public Square radSurf() { return sys.radSurface; }
	@Override
	public String designName() { return sys.getDesignName(); }
	
	public static void main(String[] args) {
		new RadiationExposureAET21_Triangles().start();		
	}
}
