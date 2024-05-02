package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import net.jafama.FastMath;
import uk.co.oliford.jolu.OneLiners;

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
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposureAEK41_Triangles extends RadiationExposureTriangles {
	
	//public BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	public BeamEmissSpecAEK41_edgeVIS sys = new BeamEmissSpecAEK41_edgeVIS();
	
	public Optic portFlange = new STLMesh("/work/cad/aek41/radExposure1/portFlange-simplified.stl");
	@Override public Element tracingTarget() { return sys.entryWindowIris;	}
	
	@Override
	public void start() {
		
		inPath = "/work/cad/aek41/radExposure1";
		outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure1-targetWindow/";
		
		testElements = new Element[]{
				portFlange,
				new STLMesh(inPath + "/qsc-flange.stl"),
				//new STLMesh(inPath + "/port.stl"), //80k long triangles
				new STLMesh(inPath + "/portLinerLike.stl"), //20k
				new STLMesh(inPath + "/daughterFlange-cyld.stl"),
				//new STLMesh(inPath + "/daughterFlange-ring.stl"), //can't be hit				
		};
		
		thingsInWay = new Element[]{
				new STLMesh(inPath + "/panel-trimmmed.stl"),	

		};

		nRays = 1000000;
		nRaysToDraw = 1000;
		nThreads = 12;
		
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
		new RadiationExposureAEK41_Triangles().start();		
	}
}
