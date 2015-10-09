package ipp.w7x.fusionOptics.w7x.aet20;

import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.materials.BK7;
import fusionOptics.materials.IsotropicFixedIndexGlass;
import fusionOptics.materials.Sapphire;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAEM41 extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9;
	
	public double entryWindowFrontPos[] = { -2.821, -4.604, 1.218 }; //from Jürgen's sim
	public double entryWindowDiameter = 0.050; //made up
	public double entryWindowNormal[] = { 0, 0, -1}; // made up	
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(entryWindowNormal, +0.001)); 
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, entryWindowNormal, entryWindowDiameter/2, null, null, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, entryWindowNormal, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	public double fibrePlanePos[] = Util.plus(entryWindowFrontPos, Util.mul(entryWindowNormal, -0.010)); 
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, entryWindowNormal, new double[]{1,0,0}, 0.150, 0.150, Absorber.ideal());	
	
	/***** Observation target ****/
	public int targetBeamIdx = 0; 
	public double targetBeamR = 5.8;
	public double targetObsPos[] = W7XRudix.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);

	
	
	
	public Element tracingTarget = entryWindowFront;
	
	
	public BeamEmissSpecAEM41() {
		super("beamSpec-aem41");
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(fibrePlane);
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
	}

	public String getDesignName() { return "aem41";	}
	
	

}
