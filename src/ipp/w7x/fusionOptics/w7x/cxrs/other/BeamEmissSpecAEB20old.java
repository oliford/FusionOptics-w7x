package ipp.w7x.fusionOptics.w7x.cxrs.other;

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
public class BeamEmissSpecAEB20old extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9;
	
	//public double entryWindowFrontPos[] = { 2.122,  5.836,   0.912 }; // Crude est from CAD, entrace of port
	public double panelLevelPos[] = { 2.0689, 5.7923, 0.8198 }; //levels with panels, shifted off centre from port
	public double entryWindowNormal[] = { 0.21124654821000247, -0.06989548954606051, 0.9749305187604271 };
	public double entryWindowFrontPos[] = Util.plus(panelLevelPos, Util.mul(entryWindowNormal, -0.020)); 
			//
	public double entryWindowDiameter = 0.050; //made up
	//public double entryWindowNormal[] = { -0.216, -0.777, -0.592 }; // Crude est from CAD, entrace of port
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(entryWindowNormal, +0.001)); 
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, entryWindowNormal, entryWindowDiameter/2, null, null, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, entryWindowNormal, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	public double fibrePlanePos[] = Util.plus(entryWindowFrontPos, Util.mul(entryWindowNormal, -0.010)); 
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, entryWindowNormal, new double[]{1,0,0}, 0.150, 0.150, Absorber.ideal());	
	
	/***** Observation target ****/
	public int targetBeamIdx = 0; 
	public double targetBeamR = 5.8;
	public double targetObsPos[] = W7xNBI.def().getPosOfBoxAxisAtR(1, targetBeamR);
	
	public Element tracingTarget = entryWindowFront;
	
	
	/** Fibres, Observation volumes etc */
	public double[] R = { 5.50, 5.55, 5.60, 5.65, 5.70, 5.75, 5.80, 5.85, 5.90, 5.95, 6.00 };	
	
	
	public BeamEmissSpecAEB20old() {
		super("beamSpec-aeb20");
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(fibrePlane);
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
	}

	public String getDesignName() { return "aeb20";	}
	
	

}
