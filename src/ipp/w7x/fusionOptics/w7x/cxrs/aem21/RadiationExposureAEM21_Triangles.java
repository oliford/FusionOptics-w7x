package ipp.w7x.fusionOptics.w7x.cxrs.aem21;

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
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposureAEM21_Triangles extends RadiationExposureTriangles {

	//AEA21
	public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(false);
	
	// Direct power from plasma 
	@Override
	public Square radSurf() { return sys.radSurface; }
	
	private STLMesh targ = new STLMesh("/work/cad/aem21/radExposure50/MirrorClosed.stl");
	public Element tracingTarget() { return targ; }
	
	public double powerDensity = 100e3;
	//*/
	
	
	// Reradiated power from front plate at max temperature
	/*@Override
	public Square radSurf() { return sys.frontPlateRadiator; }
	
	private STLMesh targ = new STLMesh("/work/cad/aem21/radExposure50/Tube.stl");
	@Override
	public Element tracingTarget() {  return targ; }
	
	public double powerDensity = sys.fprPowerDensity;
	//*/
	
	

	
	@Override
	public void start() {
		
		powerAngularDensity = powerDensity / 2 / Math.PI; // Reradiation from <T> = 350'C surface
		
				
		inPath = "/work/cad/aem21/radExposure50/";
		outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure50-withStraps/";
		
		testElements = new Element[]{
				new STLMesh(inPath + "/FrontPlate.stl"),
				new STLMesh(inPath + "/Cup.stl"),
				new STLMesh(inPath + "/PortShield.stl"),
				new STLMesh(inPath + "/vessel.stl"),
				new STLMesh(inPath + "/Zwickel.stl"), 			
				new STLMesh(inPath + "/FrontPlatePipe.stl"),
				new STLMesh(inPath + "/PortShieldPipe.stl"),
				new STLMesh(inPath + "/MirrorClosed.stl"),
				new STLMesh(inPath + "/Cover1.stl"),
				new STLMesh(inPath + "/Cover2.stl"),
				new STLMesh(inPath + "/ProtectionCollar1.stl"),
				new STLMesh(inPath + "/ProtectionCollar2.stl"),
				new STLMesh(inPath + "/Tube.stl"),
				new STLMesh(inPath + "/MountArm.stl"),
				
				new STLMesh(inPath + "/portLiner-AEN21-spaltSchutz.stl"),
				new STLMesh(inPath + "/portLiner-AEN21-spaltSchutz2.stl"),

				new STLMesh(inPath + "/shutterDriveLeft.stl"),
				new STLMesh(inPath + "/shutterDriveRight.stl"),
				new STLMesh(inPath + "/shutterDriveAcross.stl"),
				new STLMesh(inPath + "/teWire1.stl"),
				new STLMesh(inPath + "/teWire2.stl"),
				
				new STLMesh(inPath + "/strapL.stl"),
				new STLMesh(inPath + "/strapR.stl"),
				
		};
		
		thingsInWay = new Element[]{	

				new STLMesh(inPath + "/panel1.stl"),
				new STLMesh(inPath + "/panel2.stl"),
				new STLMesh(inPath + "/panel3.stl"),

				new STLMesh(inPath + "/VesselSurround.stl"),
				new STLMesh(inPath + "/portLiner-AEN21-simple.stl"), 
		
	/*
					new STLMesh(inPath + "/shutterDriveMech1.stl"),
					new STLMesh(inPath + "/shutterDriveMech2.stl"),
					new STLMesh(inPath + "/shutterDriveMech3.stl"),
					new STLMesh(inPath + "/strap1m.stl"),
					new STLMesh(inPath + "/strap2m.stl"),
//*/
		};
		
		nRays = 10000000;
		nRaysToDraw = 1000;
		nThreads = 12;
		
		sys.removeElement(sys.mirror);
		sys.removeElement(sys.beamPlane);
		sys.removeElement(sys.catchPlane);
		sys.removeElement(sys.strayPlane);
		
		sys.removeElement(sys.panelEdge);
		sys.removeElement(sys.mirrorBlock);
		sys.removeElement(sys.mirrorClampRing);
		
		super.start();
	}
	
	@Override
	public Optic sys() { return sys; }
	
	public static void main(String[] args) {
		new RadiationExposureAEM21_Triangles().start();		
	}

	@Override
	public String designName() { return sys.getDesignName(); }
	
}
