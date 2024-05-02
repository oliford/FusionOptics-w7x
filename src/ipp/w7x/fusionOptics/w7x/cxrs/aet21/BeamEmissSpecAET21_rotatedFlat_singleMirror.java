package ipp.w7x.fusionOptics.w7x.cxrs.aet21;

import ipp.w7x.neutralBeams.W7xNBI;
import uk.co.oliford.jolu.OneLiners;
import algorithmrepository.Algorithms;
import net.jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.materials.BK7;
import fusionOptics.materials.IsotropicFixedIndexGlass;
import fusionOptics.materials.Sapphire;
import fusionOptics.materials.SchottSFL6;
import fusionOptics.optics.STLMesh;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAET21_rotatedFlat_singleMirror extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9;
	
	/**** Shutter ****/
	//public double shutterPivotCentre[] = { -0.910603, 6.2042, -0.134334 };
	/*public double shutterPoints[][] = { //as in original cad, sticking out a bit
		{-0.995693, 6.16063, -0.1641200},
		{-0.820756, 6.22351, -0.1766120},
		{-0.940116, 6.20814, -0.0385021},
	};*/
	public double shutterPivotCentre[] = { -0.9291632080078125, 6.19046044921875, -0.15695947265625002 };
	public double shutterPoints[][] = { //as in original cad, sticking out a bit
			{-0.8638120727539063, 6.19050244140625, -0.09048074340820313},
			{-1.0155765991210939, 6.193046875, -0.16402096557617188},
			{-0.8921322021484375, 6.18668798828125, -0.23543576049804688},
	};
	
	public double shutterDiameter = 0.200;
	
	public double shutterNormal[] = Util.reNorm(Util.cross(Util.minus(shutterPoints[0], shutterPoints[1]), Util.minus(shutterPoints[2], shutterPoints[1])));
	
	public Disc shutter = new Disc("shutterDisc", shutterPivotCentre, shutterNormal, shutterDiameter/2, NullInterface.ideal());
	
	public double shutterRight[] = Util.reNorm(Util.cross(shutterNormal, globalUp));
	public double shutterUp[] = Util.reNorm(Util.cross(shutterRight, shutterNormal));
	
	/**** Port Tube****/
	
	public double portNormal[] = Util.reNorm(new double[]{-0.07285036193952138, 0.9468680957270127, -0.31326288330981955});
	public double portRight[] = Util.reNorm(Util.cross(portNormal, globalUp));
	public double portUp[] = Util.reNorm(Util.cross(portRight, portNormal));
	
	
	public double portTubeDiameter = 0.220;
	public double portTubeLength = 1.000;
	public double portTubeCentre[] = Util.plus(shutterPivotCentre, Util.mul(portNormal, portTubeLength/2));
	public Cylinder portTubeCyld = new Cylinder("portTubeCyld", portTubeCentre, portNormal, portTubeDiameter/2, portTubeLength, Absorber.ideal());

	/**** Entry Window ****/
	public double entryWindowRadiusOnShutter = 0.058;
	public double entryWindowIrisDiameter = 0.065;
	public double entryWindowDiameter = 0.050;
	//public double entryWindowAngularPosition = 116 * Math.PI / 180;//near window
	public double entryWindowAngularPosition = -40 * Math.PI / 180; //top window 
	//public double entryWindowAngularPosition = -124 * Math.PI / 180; // bottom window
	public double entryWindowMoveIn = 0.010;
	public double entryWindowThickness = 0.003;
	public double entryWindowCyldLength = 0.020;
	public double entryWindowCyldPosAdjust = 0.015;
	public double entryWindowIrisPos[] = Util.plus(shutterPivotCentre, 
										Util.plus( Util.plus( Util.mul(shutterUp, entryWindowRadiusOnShutter * FastMath.cos(entryWindowAngularPosition)),
												    		  Util.mul(shutterRight, entryWindowRadiusOnShutter * FastMath.sin(entryWindowAngularPosition))),
												   Util.mul(shutterNormal, -0.000)));
		 
	public double entryWindowPos[] = Util.plus(entryWindowIrisPos, Util.mul(shutterNormal, entryWindowMoveIn));
	
	public double entryWindowFrontPos[] = Util.plus(entryWindowPos, Util.mul(shutterNormal, -entryWindowThickness/2));
	public double entryWindowBackPos[] = Util.plus(entryWindowPos, Util.mul(shutterNormal, +entryWindowThickness/2));
	
	public double entryWindowCyldPos[] = Util.plus(entryWindowIrisPos, Util.mul(shutterNormal, entryWindowCyldPosAdjust));
	
	
	//public Medium entryWindowMedium = new Medium(new IsotropicFixedIndexGlass(1.2));
	public Medium entryWindowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindow", entryWindowFrontPos, shutterNormal, entryWindowDiameter/2, entryWindowMedium, null, IsoIsoInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindow", entryWindowBackPos, shutterNormal, entryWindowDiameter/2, null, entryWindowMedium, IsoIsoInterface.ideal());
	public Iris entryWindowFrontIris = new Iris("entryWindowIris", entryWindowIrisPos, shutterNormal, 0.080, entryWindowIrisDiameter/2, null, null, Absorber.ideal());
	public Cylinder entryWindowCyld = new Cylinder("entryWindowCyld", entryWindowCyldPos, shutterNormal, entryWindowDiameter/2, entryWindowCyldLength, Absorber.ideal());
	
	//public STLMesh shutterPlate = new STLMesh("shutterPlate", "/work/ipp/w7x/cad/aet21/shutterFrontPlate-topWindow.stl");
	
	/***** Observation target ****/
	public int targetBeamIdx = 7; //Q8
	public double targetBeamR = 5.7;
	public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, entryWindowPos));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVec(targetBeamIdx), observationVec));
	
	public double asideVector[] = Util.reNorm(Util.cross(observationUp, shutterNormal));
	
	
	
	/***** Prism on back of window ****/
	public double prismRotationAngle = 0 * Math.PI / 180;
	public double prismUp[] = Algorithms.rotateVector(Algorithms.rotationMatrix(shutterNormal, prismRotationAngle), observationUp);
	
	public double backPrismSurfaceAngle = 20 * Math.PI / 180;
	public double backPrismBackNormal[] = Algorithms.rotateVector(Algorithms.rotationMatrix(prismUp, backPrismSurfaceAngle), shutterNormal);
	public double backPrismFrontPos[] = Util.plus(entryWindowBackPos, Util.mul(shutterNormal, 0.001));
	public double backPrismBackPos[] = Util.plus(entryWindowBackPos, Util.mul(shutterNormal, 0.026));
	public double backPrismIrisPos[] = Util.plus(entryWindowBackPos, Util.mul(shutterNormal, 0.010));
	
	public Medium prismMedium = new Medium(new SchottSFL6());
	public Disc backPrismFront = new Disc("backPrismFront", backPrismFrontPos, shutterNormal, entryWindowDiameter/2, prismMedium, null, IsoIsoInterface.ideal());
	public Disc backPrismBack = new Disc("backPrismBack", backPrismBackPos, backPrismBackNormal, entryWindowDiameter*0.55, null, prismMedium, IsoIsoInterface.ideal());
	public Iris backPrismIris = new Iris("backPrismIris", backPrismIrisPos, shutterNormal, 0.060, entryWindowDiameter*0.47, Absorber.ideal());
	
	
	/**** Mirror 1 ****/
	//public double mirrorWidth = 0.126;
	public double mirrorWidth = 0.050;
	public double mirrorHeight = 0.050;
	
	public double mirrorCentreBackFromWindow = 0.040;	
	public double mirrorCentreAsideFromWindow = 0.030;
		
	public double mirrorShiftUp = 0.000;
	
	public double mirrorBuildPosA[] = Util.plus(entryWindowFrontPos, Util.mul(shutterNormal, mirrorCentreBackFromWindow));	
	public double mirrorCentrePosA[] = Util.plus(mirrorBuildPosA, Util.mul(asideVector, mirrorCentreAsideFromWindow));
	
	
	public double mirrorToPortAngle = 0 * Math.PI / 180;
	
	public double mirrorTipAngle = 0 * Math.PI / 180; //tip to counter the effect of the prism rotation
	
	public double mirrorUpZero[] = Util.reNorm(Util.cross(observationVec, portNormal));
	public double mirrorNormalA[] = Util.reNorm(Util.cross(mirrorUpZero, portNormal));
	
	public double mirrorNormalB[] = Util.reNorm(Util.plus(
												Util.mul(mirrorNormalA, FastMath.cos(mirrorToPortAngle)),
												Util.mul(portNormal, FastMath.sin(mirrorToPortAngle))));
	
	public double mirrorNormal[] =  Util.reNorm(Algorithms.rotateVector(Algorithms.rotationMatrix(portNormal, mirrorTipAngle), mirrorNormalB));
	public double mirrorCentrePos[] =  Util.plus(mirrorCentrePosA, Util.mul(observationUp, mirrorShiftUp));
	
	public double mirrorUp[] = Util.reNorm(Util.cross(Util.cross(observationUp, mirrorNormal),mirrorNormal));
	
	public Square mirror = new Square("mirror", mirrorCentrePos, mirrorNormal, mirrorUp, mirrorHeight, mirrorWidth, Reflector.ideal());
	
	
	/**** Mirror 1 ****/
	//public double mirrorWidth = 0.126;
	/*public double mirror2Width = 0.050;
	public double mirror2Height = 0.050;	
	public Square mirror2 = new Square("mirror2", mirror2CentrePos, mirror2Normal, mirror2Up, mirror2Height, mirror2Width, Reflector.ideal());
	*/
		
	/**** Lens *****/

	//public double lensCentrePos[] = { -0.967, 6.310, -0.100 };
	
	public double lensCentrePos[] = { -0.970, 6.300, -0.115};
	public double lensFocalLength = 0.035;
	
	public double lensNormal[] = Util.reNorm(Util.minus(lensCentrePos, mirrorCentrePos));	
	public double lensDiameter = 0.045;
	
	/*public Medium lensMedium = new Medium(new SchottSFL6()); 
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lensCentrePos,
											lensNormal,
											0.51*lensDiameter, // radius
											0.050, // focal length
											0.010, // centreThickness, 
											lensMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lensIrisCentrePos[] =  Util.plus(lensCentrePos, Util.mul(lensNormal, -0.005));
	public Iris lensIris = new Iris("lensIris", lensIrisCentrePos, lensNormal, 2*lensDiameter/2, 0.5*lensDiameter, Absorber.ideal());
	*/
	Nikon50mmF11 lens1 = new Nikon50mmF11(lensCentrePos, lensFocalLength / 0.050, lensNormal);
	
	/*** Fibre plane 1 ****/
	public double fibrePlaneFromLens = 0.035;
	public double fibrePlanePortRightShift = -0.000;	
	public double fibrePlanePortUpShift = 0.000;	
	public double fibrePlanePos[] = Util.plus(lensCentrePos, Util.plus(Util.mul(lensNormal, fibrePlaneFromLens),
																		Util.plus(Util.mul(portRight, fibrePlanePortRightShift),
																				  Util.mul(portUp, fibrePlanePortUpShift))));
	
	public double fibrePlaneUp[] = Util.reNorm(Util.cross(Util.cross(lensNormal, globalUp), lensNormal));
	
	/*** Fibre plane 2 ****/
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, lensNormal, fibrePlaneUp, 0.080, 0.080, Absorber.ideal());
	
	
	public Element tracingTarget = entryWindowFront;
	
	
	/** Fibres, Observation volumes etc */
	public double[] R = { 5.5, 5.6, 5.7, 5.8, 5.9, 6.0 };	
	
	
	public BeamEmissSpecAET21_rotatedFlat_singleMirror() {
		super("beamSpec-aet21");
		
		//make the window a prism
		//entryWindowFront.rotate(entryWindowFront.getCentre(), Algorithms.rotationMatrix(observationUp, -5 * Math.PI / 180));
		//entryWindowBack.rotate(entryWindowBack.getCentre(), Algorithms.rotationMatrix(observationUp, 10 * Math.PI / 180));
		
		
		addElement(shutter);		
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(entryWindowFrontIris);
		addElement(entryWindowCyld);
		addElement(mirror);
		//addElement(lensIris);
		addElement(lens1);
		addElement(fibrePlane);
		//addElement(portTubeCyld);

		addElement(backPrismFront);
		addElement(backPrismBack);
		addElement(backPrismIris);
		
		double lensFibreDist = Util.length(Util.minus(lensCentrePos, fibrePlane.getCentre()));
		System.out.println("Lens - Fibres distance = " + lensFibreDist*1000 + " / mm");
		//System.out.println("Max f/" + (lensFibreDist/lens1.getRadius()/2) + " ");
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
	}

	public String getDesignName() { return "aet21";	}
	
	

}
