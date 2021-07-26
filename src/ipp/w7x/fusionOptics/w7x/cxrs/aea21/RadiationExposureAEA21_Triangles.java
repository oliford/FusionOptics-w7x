package ipp.w7x.fusionOptics.w7x.cxrs.aea21;

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
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21.Subsystem;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposureAEA21_Triangles extends RadiationExposureTriangles {
	
	public BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21(Subsystem.CXRS);
	
	
	@Override
	public Square radSurf() { return sys.radSurface; }
	
	private Optic targ = new STLMesh("/work/cad/aea21/radExposure4/cxrsHousing.stl");
	@Override public Element tracingTarget() { return targ; }
	//*/
	
	/*
	@Override
	public Square radSurf() { return sys.housingSurface; }
	
	private Optic targ = new STLMesh("/work/cad/aea21/radExposure4/shutter.stl");
	@Override public Element tracingTarget() { return targ; }
	//*/
	
	@Override
	public void start() {
		
		inPath = "/work/cad/aea21/radExposure4";
		outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure4-open-morePipe/";
		
		//powerAngularDensity = 15e3 / 2 / Math.PI;  //reradiation from 450°C at emissivity=1
		
		testElements = new Element[]{

				
				new STLMesh(inPath + "/cxrsHousing-20mm.stl"),
				new STLMesh(inPath + "/frontPlateRim.stl"),
				new STLMesh(inPath + "/frontPlate.stl"),
				new STLMesh(inPath + "/hingeBottom.stl"),
				new STLMesh(inPath + "/hingePlate.stl"),
				new STLMesh(inPath + "/hingeTop.stl"),
				new STLMesh(inPath + "/housingGapClosure.stl"),
				new STLMesh(inPath + "/pipe1More.stl"),
				new STLMesh(inPath + "/pipe2.stl"),
				new STLMesh(inPath + "/portCatch.stl"),
				new STLMesh(inPath + "/ringClamp.stl"),
				new STLMesh(inPath + "/tube.stl"),
				new STLMesh(inPath + "/window.stl"),
				
				//closed
				/*new STLMesh(inPath + "/shutter.stl"),
				new STLMesh(inPath + "/strapB.stl"),
				new STLMesh(inPath + "/strapT.stl"),
				new STLMesh(inPath + "/cover1.stl"),
				new STLMesh(inPath + "/cover2.stl"),
				new STLMesh(inPath + "/gapFoil.stl"),
				//*/
				
				//open
				new STLMesh(inPath + "/driveBottom-open.stl"),
				new STLMesh(inPath + "/driveTop-open.stl"),
				new STLMesh(inPath + "/shutter-open.stl"),
				new STLMesh(inPath + "/mirror-open.stl"),
				new STLMesh(inPath + "/thermocoupleWire.stl"),
				new STLMesh(inPath + "/cover1Copper-open.stl"),
				//*/
				
		};
		
		thingsInWay = new Element[]{	
				
				new STLMesh(inPath + "/panelTL-simplified.stl"),
				new STLMesh(inPath + "/panelTR-simplified.stl"),
				new STLMesh(inPath + "/notInterested.stl"),
		};
		
		nRays = 10000000;
		nRaysToDraw = 100;
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
	public String designName() { return sys.getDesignName(); }
	
	public static void main(String[] args) {
		new RadiationExposureAEA21_Triangles().start();		
	}
}
