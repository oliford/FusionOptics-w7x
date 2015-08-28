package ipp.w7x.fusionOptics.w7x.aet20;

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
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAET21 extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 600e-9;
	
	
	
	/**** Shutter ****/
	public double shutterPivotCentre[] = { -0.910603, 6.2042, -0.134334 };
	public double shutterPoints[][] = {
		{-0.995693, 6.16063, -0.1641200},
		{-0.820756, 6.22351, -0.1766120},
		{-0.940116, 6.20814, -0.0385021},
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
	public double entryWindowIrisDiameter = 0.060;
	public double entryWindowDiameter = 0.050;
	public double entryWindowAngularPosition = 116 * Math.PI / 180;
	public double entryWindowMoveIn = 0.0150;
	public double entryWindowThickness = 0.015;
	public double entryWindowCyldLength = 0.025;
	public double entryWindowIrisPos[] = Util.plus(shutterPivotCentre, 
										Util.plus( Util.plus( Util.mul(shutterUp, entryWindowRadiusOnShutter * FastMath.cos(entryWindowAngularPosition)),
												    		  Util.mul(shutterRight, entryWindowRadiusOnShutter * FastMath.sin(entryWindowAngularPosition))),
												   Util.mul(shutterNormal, -0.005)));
		 
	public double entryWindowPos[] = Util.plus(entryWindowIrisPos, Util.mul(shutterNormal, entryWindowMoveIn));
	
	public double entryWindowFrontPos[] = Util.plus(entryWindowPos, Util.mul(shutterNormal, -entryWindowThickness/2));
	public double entryWindowBackPos[] = Util.plus(entryWindowPos, Util.mul(shutterNormal, +entryWindowThickness/2));
	
	//public Medium entryWindowMedium = new Medium(new IsotropicFixedIndexGlass(1.2));
	public Medium entryWindowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindow", entryWindowFrontPos, shutterNormal, entryWindowDiameter/2, entryWindowMedium, null, IsoIsoInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindow", entryWindowBackPos, shutterNormal, entryWindowDiameter/2, null, entryWindowMedium, IsoIsoInterface.ideal());
	public Iris entryWindowFrontIris = new Iris("entryWindowIris", entryWindowIrisPos, shutterNormal, 0.080, entryWindowIrisDiameter/2, null, null, Absorber.ideal());
	public Cylinder entryWindowCyld = new Cylinder("entryWindowCyld", entryWindowPos, shutterNormal, entryWindowDiameter/2, entryWindowCyldLength, Absorber.ideal());
	
	
	/***** Observation target ****/
	public int targetBeamIdx = 4;
	public double targetBeamR = 5.8;
	public double targetObsPos[] = W7XBeamDefsSimple.getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, entryWindowPos));
	public double observationUp[] = Util.reNorm(Util.cross(W7XBeamDefsSimple.nbiUnit[targetBeamIdx], observationVec));
	
	public double asideVector[] = Util.reNorm(Util.cross(observationUp, shutterNormal));
	
	/*** Fibre plane 1 ****/
	public double fibrePlaneBackFromWindow = 0.260;
	public double fibrePlanePortRight = -0.020;	
	public double fibrePlanePortUp = 0.010;	
	public double fibrePlanePos[] = Util.plus(entryWindowPos, Util.plus(Util.mul(portNormal, fibrePlaneBackFromWindow),
																		Util.plus(Util.mul(portRight, fibrePlanePortRight),
																				  Util.mul(portUp, fibrePlanePortUp))));
	
	
	
	/**** Mirror ****/
	//public double mirrorWidth = 0.126;
	public double mirrorWidth = 0.100;
	public double mirrorHeight = 0.060;
	
	public double mirrorCentreBackFromWindow = 0.073;	
	public double mirrorCentreAsideFromWindow = 0.032;
	
	public double mirrorBuildPosA[] = Util.plus(entryWindowFrontPos, Util.mul(shutterNormal, mirrorCentreBackFromWindow));	
	public double mirrorCentrePos[] = Util.plus(mirrorBuildPosA, Util.mul(asideVector, mirrorCentreAsideFromWindow));
	
	
	public double mirrorToPortAngle = -21 * Math.PI / 180;
	
	public double mirrorUpZero[] = Util.reNorm(Util.cross(observationVec, portNormal));
	public double mirrorNormalA[] = Util.reNorm(Util.cross(mirrorUpZero, portNormal));
	
	public double mirrorNormal[] = Util.reNorm(Util.plus(
												Util.mul(mirrorNormalA, FastMath.cos(mirrorToPortAngle)),
												Util.mul(portNormal, FastMath.sin(mirrorToPortAngle))));
	
	public double mirrorUp[] = Util.reNorm(Util.cross(Util.cross(observationUp, mirrorNormal),mirrorNormal));
	
	public Square mirror = new Square("mirror", mirrorCentrePos, mirrorNormal, mirrorUp, mirrorHeight, mirrorWidth, Reflector.ideal());
	

		
	/**** Lens *****/
	public double effMirrorPos[] = Util.plus(mirrorCentrePos, Util.mul(mirror.getRight(), 0.020));
	
	public double inPortOpticalAxis[] = Util.reNorm(Util.minus(fibrePlanePos, effMirrorPos));
	
	public double lensFromMirror = 0.080;
	
	public double lensCentrePos[] = Util.plus(effMirrorPos, Util.mul(inPortOpticalAxis, lensFromMirror));
	
	public Medium lensMedium = new Medium(new BK7()); 
	public SimplePlanarConvexLens lens = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens",
											lensCentrePos,
											inPortOpticalAxis,
											0.035, // radius
											0.150, // focal length
											0.010, // centreThickness, 
											lensMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	/*** Fibre plane 2 ****/
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, inPortOpticalAxis, portUp, 0.070, 0.070, Absorber.ideal());
	
	public BeamEmissSpecAET21() {
		super("beamSpec-aet20");
		
		//make the window a prism
		entryWindowFront.rotate(entryWindowFront.getCentre(), Algorithms.rotationMatrix(observationUp, -5 * Math.PI / 180));
		entryWindowBack.rotate(entryWindowBack.getCentre(), Algorithms.rotationMatrix(observationUp, 5 * Math.PI / 180));
		
		addElement(shutter);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(entryWindowFrontIris);
		addElement(entryWindowCyld);
		addElement(mirror);
		addElement(lens);
		addElement(fibrePlane);
		//addElement(portTubeCyld);
		
		double lensFibreDist = Util.length(Util.minus(lens.getBackSurface().getCentre(), fibrePlane.getCentre()));
		System.out.println("Lens - Fibres distance = " + lensFibreDist*1000 + " / mm");
		System.out.println("Max f/" + (lensFibreDist/lens.getRadius()/2) + " ");
		
		
	}
	
	

}
