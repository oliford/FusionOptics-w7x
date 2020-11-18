package ipp.w7x.fusionOptics.w7x.cxrs;

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
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposureAEA21_Triangles extends RadiationExposureTriangles {
	
	public BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	
	@Override
	public void start() {
		
		inPath = "/work/cad/aea21/radExposure3";
		outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure3-testThreads/";
		
		testElements = new Element[]{
				new STLMesh(inPath + "/mirrorBlock.stl"),
				new STLMesh(inPath + "/cap1.stl"),
				new STLMesh(inPath + "/cap2.stl"),
				new STLMesh(inPath + "/strap1.stl"),
				new STLMesh(inPath + "/strap2.stl"),			
				new STLMesh(inPath + "/cover.stl"),  

		};
		
		thingsInWay = new Element[]{	

				new STLMesh(inPath + "/frontPlateWithRim.stl"),
				new STLMesh(inPath + "/panelTL-simplified.stl"),
				new STLMesh(inPath + "/panelTR-simplified.stl"),
				new STLMesh(inPath + "/backHoleClosure.stl"),
				new STLMesh(inPath + "/pipeHoleClosure.stl"),
				new STLMesh(inPath + "/sideClosure.stl"),

				/*new STLMesh(inPath + "/shutterDriveMech1.stl"),
				new STLMesh(inPath + "/shutterDriveMech2.stl"),
				new STLMesh(inPath + "/shutterDriveMech3.stl"),
				new STLMesh(inPath + "/strap1m.stl"),
				new STLMesh(inPath + "/strap2m.stl"),*/
		};

		nRays = 1000000;
		nRaysToDraw = 1000;
		nThreads = 12;
		
		sys.removeElement(sys.mirror);
		sys.removeElement(sys.beamPlane);
		sys.removeElement(sys.catchPlane);
		sys.removeElement(sys.strayPlane);
		
		super.start();
	}
	
	@Override
	public Optic sys() { return sys; }
	@Override
	public Square radSurf() { return sys.radSurface; }
	@Override
	public String designName() { return sys.getDesignName(); }
	
	public static void main(String[] args) {
		new RadiationExposureAEA21_Triangles().start();		
	}
}
