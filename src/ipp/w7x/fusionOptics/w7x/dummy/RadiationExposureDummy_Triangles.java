package ipp.w7x.fusionOptics.w7x.dummy;

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
public class RadiationExposureDummy_Triangles extends RadiationExposureTriangles {
	
	public BeamEmissSpecDummy sys = new BeamEmissSpecDummy();
		
	@Override
	public Square radSurf() { return sys.radSurface; }
	
	private Optic target = new STLMesh("/work/cad/radExposureDummy/target-m4-upper.stl");
	@Override public Element tracingTarget() { return target; }
	
	@Override
	public void start() {
		
		inPath = "/work/cad/radExposureDummy";
		outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure1/";
		
		powerAngularDensity = 100e3 / 2 / Math.PI;  //power flux from radiation surface [W m^-2 SR^-1]
		
		testElements = new Element[]{
				//things we will track the power load to individual triangles of
				new STLMesh(inPath + "/baffle-m4-upper.stl"),
				new STLMesh(inPath + "/target-m4-upper.stl"),
		};
		
		thingsInWay = new Element[]{	
				//things we don't care about
				new STLMesh(inPath + "/baffle-m4-lower.stl"),
				new STLMesh(inPath + "/closure-m4-cut.stl"),
				new STLMesh(inPath + "/pumpslot-m4-cut.stl"),
				new STLMesh(inPath + "/shield-m4-cut.stl"),
				new STLMesh(inPath + "/panel-m41-cut.stl"),
				new STLMesh(inPath + "/target-m4-lower.stl"),
		};
		
		nRays = 10000000;
		nRaysToDraw = 1000;
		nThreads = 12;
		
		super.start();
	}
	
	@Override
	public Optic sys() { return sys; }
	@Override
	public String designName() { return sys.getDesignName(); }
	
	public static void main(String[] args) {
		new RadiationExposureDummy_Triangles().start();		
	}
}
