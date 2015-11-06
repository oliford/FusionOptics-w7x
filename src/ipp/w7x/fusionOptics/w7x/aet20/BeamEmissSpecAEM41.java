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
	
	public double portNormal[] = {-0.20192623, 0.32638819, 0.92341569 };  // [fromDesigner-20151106] lens back plane normal, matches rod axis
	public double entryWindowFrontPos[] = { -2.821, -4.604, 1.218 }; //from Jürgen's sim 'Mittelpunkt des Fensters im AEM41'
	
	public double entryWindowDiameter = 0.150; //made up		
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, -0.001)); 
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, portNormal, entryWindowDiameter/2, null, null, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, portNormal, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
			
	/**** Lens *****/
	
	public double lensCentrePos[] = { -2.84897, -4.55875, 1.339 }; // [fromDesigner-20151106]
	public double lensDiameter = 0.075;
	//public double lensFrontPos[] = { -2.844, -4.565, 1.32053 }; // [fromDesigner-20151106]
	
	public double objectDist = 1.300;
	public double imageDist = 0.100;
	//public double focalLength = 1.0 / (1.0/objectDist + 1.0/imageDist);
	public double focalLength = 0.120;
	
	public Medium lensMedium = new Medium(new Sapphire()); 
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lensCentrePos,
											portNormal,
											lensDiameter/2, // radius
											focalLength, // focal length
											0.020, // centreThickness, 
											lensMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	public double lensIrisPos[] = Util.plus(lensCentrePos, Util.mul(portNormal, -0.015));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, portNormal, lensDiameter, lensDiameter*0.49, null, null, Absorber.ideal());
	

	/*** Fibres ****/
	
	public double fibre1EndPos[] = { -2.89649, -4.54998, 1.44268 }; // core channel, [fromDesigner-20151106] 
	public double fibre10EndPos[] = { -2.85912, -4.50896, 1.43616 }; // edge channel,  [fromDesigner-20151106]
	
	int nFibres = 10;
	public double fibreEndPos[][];
		
	public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5); 
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, portNormal, fibresYVec, 0.020, 0.070, Absorber.ideal());

	/***** Observation target ****/
	public int targetBeamIdx = 0; 
	public double targetBeamR = 5.6;
	public double targetObsPos[] = W7XRudix.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double beamAxis[] = W7XRudix.def().uVec(0);
	
	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.200, NullInterface.ideal());

	public Element tracingTarget = lens1;
		
	public BeamEmissSpecAEM41() {
		super("beamSpec-aem41");
		
		fibreEndPos = new double[nFibres][];
		double dp[] = Util.mul(Util.minus(fibre10EndPos, fibre1EndPos), 1.0 / (nFibres - 1));
		for(int i=0; i < nFibres; i++){
			fibreEndPos[i] = Util.plus(fibre1EndPos, Util.mul(dp, i));
		}
		
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(lensIris);
		addElement(lens1);
		addElement(fibrePlane);
		addElement(beamPlane);
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
	}

	public String getDesignName() { return "aem41";	}
	
	

}
