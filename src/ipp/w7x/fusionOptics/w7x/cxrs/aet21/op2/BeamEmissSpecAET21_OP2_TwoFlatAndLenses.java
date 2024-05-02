package ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2;

import ipp.w7x.neutralBeams.W7xNBI;
import uk.co.oliford.jolu.OneLiners;
import algorithmrepository.Algorithms;
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
import fusionOptics.surfaces.Dish;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Paraboloid;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAET21_OP2_TwoFlatAndLenses extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9;
	
	/** DIrection of port axis */
	public double portAxis[] = { 0.07272971344397348, -0.9468079681488022, 0.3134725829036636 };
	
	public double portRight[] = Util.reNorm(Util.cross(portAxis, globalUp));
	public double portUp[] = Util.reNorm(Util.cross(portRight, portAxis));
	
	/** Point on port axis, somewhere in middle of port */
	public double portMidCentre[] = { -1.0052220437467529, 7.2323006773619745, -0.46831944419294246 };
	
	/** Point on Baffel on far side of port from beam */
	public double baffelPoint[] = { -1.0722723388671875, 6.15255517578125, -0.030531103134155273  };
	
	
	public double frontDiscCentre[] = Util.plus(portMidCentre, Util.mul(portAxis, Algorithms.pointOnLineNearestPoint(portMidCentre, portAxis, baffelPoint)));
	
	public double frontDiscRadius = Util.length(Util.minus(baffelPoint, frontDiscCentre));
	
	public Disc frontDisc = new Disc("frontDisc", frontDiscCentre, portAxis, frontDiscRadius, NullInterface.ideal());
	
	/***** Observation target ****/
	public int targetBoxIdx = 1; //NI21
	public double targetBeamR = 5.75;
	//public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double targetObsPos[] = W7xNBI.def().getPosOfBoxAxisAtR(targetBoxIdx, targetBeamR);

	public Sphere targetSphere = new Sphere("targetSphere", targetObsPos, 0.025, NullInterface.ideal());

	
	/**** Port Tube****/
	
	public double portTubeDiameter = 0.300;
	public double portTubeLength = 1.000;
	public double portTubeCentre[] = Util.minus(frontDiscCentre, Util.mul(portAxis, 0.010 + portTubeLength / 2));
	public Cylinder portTubeCyld = new Cylinder("portTubeCyld", portTubeCentre, portAxis, portTubeDiameter/2, portTubeLength, Absorber.ideal());


	/*** Entry iris ***/
	public double entryApertureMirrorDist = 0.100;
	public double entryApertureDiameter = 0.130;
	
	/**** Mirror ****/	
	
	public double mirror1FromFront = 0.080;
	public double mirror1PortRightShift = 0.100;	
	public double mirror1PortUpShift = 0.040;
	public double mirror1Width = 0.080;
	public double mirror1Height = 0.050;
	public double mirror1CentrePos[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror1FromFront),
																		Util.plus(Util.mul(portRight, mirror1PortRightShift),
																				  Util.mul(portUp, mirror1PortUpShift))));
	
	public double mirror2FromFront = 0.160;
	public double mirror2PortRightShift = 0.020;	
	public double mirror2PortUpShift = 0.080;	
	public double mirror2Width = 0.150;
	public double mirror2Height = 0.050;
	public double mirror2CentrePos[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror2FromFront),
																		Util.plus(Util.mul(portRight, mirror2PortRightShift),
																				  Util.mul(portUp, mirror2PortUpShift))));
	
	/**** Lens ****/
	public double lens1FocalLength = 0.300;
	//public double lens1FocalLength = 0.050;
	public double lens1Thickness = 0.010;
	public double lens1Diameter = 0.100;
	public double lens1FromMirror = 0.110;
	public double lens1PortRightShift = 0.000;	
	public double lens1PortUpShift = 0.000;	

	public double lens2FocalLength = 0.300;
	public double lens2Thickness = 0.010;
	public double lens2Diameter = 0.100;
	public double lens2FromLens1 = 0.080;

	public double lens3FocalLength = 0.300;
	public double lens3Thickness = 0.010;
	public double lens3Diameter = 0.100;
	public double lens3FromLens2 = 0.080;
		
	public double lens4FocalLength = 0.025;
	public double lens4FromLens3 = 0.200;
	
	
	/*** Positions/vectors ****/
	
	//public double mirror1Angle = (90 + 45) * Math.PI / 180;
	//public double mirror1Normal[] = Util.reNorm(Algorithms.rotateVector(Algorithms.rotationMatrix(portUp, mirror1Angle), portAxis));	
		
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, mirror1CentrePos));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVecBox(targetBoxIdx), observationVec));
	
	public double mirror12Vec[] = Util.reNorm(Util.minus(mirror1CentrePos, mirror2CentrePos));
	
	
	public double lens1CentrePos[] = Util.plus(mirror2CentrePos, Util.plus(Util.mul(portAxis, -lens1FromMirror),
			Util.plus(Util.mul(portRight, lens1PortRightShift),
					  Util.mul(portUp, lens1PortUpShift))));

	public double lensNormal[] = Util.reNorm(Util.minus(lens1CentrePos, mirror2CentrePos));
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(lensNormal, lens2FromLens1));

	public double lens3CentrePos[] = Util.plus(lens2CentrePos, Util.mul(lensNormal, lens3FromLens2));

	public double lens4CentrePos[] = Util.plus(lens3CentrePos, Util.mul(lensNormal, lens4FromLens3));

	public double mirror1Normal[] = Util.reNorm(Util.mul(Util.plus(Util.mul(mirror12Vec, -1), observationVec), 0.5));
	public double mirror1Right[] = Util.reNorm(Util.cross(mirror1Normal, globalUp));
	public double mirror1Up[] = Util.reNorm(Util.cross(mirror1Right, mirror1Normal));
	
	public double mirror2Normal[] = Util.reNorm(Util.mul(Util.plus(lensNormal, mirror12Vec), 0.5));
	public double mirror2Right[] = Util.reNorm(Util.cross(mirror2Normal, globalUp));
	public double mirror2Up[] = Util.reNorm(Util.cross(mirror2Right, mirror2Normal));
	
	public double entryAperturePos[] = Util.plus(mirror1CentrePos, Util.mul(observationVec, entryApertureMirrorDist));
	public Iris entryAperture = new Iris("entryAperture", entryAperturePos, observationVec, 3*entryApertureDiameter/2, entryApertureDiameter*0.495, Absorber.ideal());
	public Disc entryTarget = new Disc("entryTarget", entryAperturePos, observationVec, 0.505*entryApertureDiameter, NullInterface.ideal());
	
	public Medium lens1Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens", 
			lens1CentrePos, lensNormal, lens1Diameter/2, lens1FocalLength, lens1Thickness, lens1Medium, IsoIsoInterface.ideal(), designWavelenth);

	public Medium lens2Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens", 
			lens2CentrePos, lensNormal, lens2Diameter/2, lens2FocalLength, lens2Thickness, lens2Medium, IsoIsoInterface.ideal(), designWavelenth);

	public Medium lens3Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens3 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens", 
			lens3CentrePos, lensNormal, lens3Diameter/2, lens3FocalLength, lens3Thickness, lens3Medium, IsoIsoInterface.ideal(), designWavelenth);
	
	Nikon50mmF11 lens4 = new Nikon50mmF11(lens4CentrePos, lens4FocalLength / 0.050, lensNormal);
	
	
	public Square mirror1 = new Square("mirror1", mirror1CentrePos, mirror1Normal, mirror1Up, mirror1Height, mirror1Width, Reflector.ideal());
	//public Disc mirror1 = new Disc("mirror1", mirror1CentrePos, mirror1Normal, mirror1Width/2, Reflector.ideal());
	//public Dish mirror1 = new Dish("mirror1", mirror1CentrePos, mirror1Normal, 0.380, mirror1Width/2, Reflector.ideal());

	public Square mirror2 = new Square("mirror2", mirror2CentrePos, mirror2Normal, mirror2Up, mirror2Height, mirror2Width, Reflector.ideal());
	//public Dish mirror2 = new Dish("mirror2", mirror2CentrePos, mirror2Normal, 0.100, mirror2Width/2, Reflector.ideal());

	/*** Fibre plane ****/
	public double fibrePlaneFromLens4 = lens4FocalLength + 0.005;
	public double fibrePlanePos[] = Util.plus(lens4CentrePos, Util.mul(lensNormal, fibrePlaneFromLens4));
	public double fibrePlaneSize = 0.050;
	
	public double fibrePlaneRight[] = Util.reNorm(Util.cross(lensNormal, mirror1Up));
	public double fibrePlaneUp[] = Util.reNorm(Util.cross(fibrePlaneRight, lensNormal));
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, lensNormal, fibrePlaneUp, fibrePlaneSize, fibrePlaneSize, Absorber.ideal());
	
	
	public STLMesh panelEdge = new STLMesh("panel", "/work/cad/aet21/conflicting-panel-aet21.stl");
	
	public Element tracingTarget = entryTarget;
	
	public BeamEmissSpecAET21_OP2_TwoFlatAndLenses() {
		super("beamSpec-aet21-op2");
		
		//addElement(frontDisc);
		addElement(panelEdge);
		addElement(targetSphere);
		addElement(entryTarget);
		addElement(entryAperture);
		//addElement(portTubeCyld);
		addElement(mirror1);
		addElement(mirror2);
		addElement(lens1);
		addElement(lens2);
		addElement(lens3);
		addElement(lens4);
		addElement(fibrePlane);
		//addElement(new Sphere("bSphere", mirror1.getBoundarySphereCentre(), mirror1.getBoundarySphereRadius(), NullInterface.ideal()));
		
	}

	public String getDesignName() { return "aet21-op2-twoFlat";	}
		

}
