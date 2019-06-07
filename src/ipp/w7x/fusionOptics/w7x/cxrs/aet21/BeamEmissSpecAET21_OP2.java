package ipp.w7x.fusionOptics.w7x.cxrs.aet21;

import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
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
public class BeamEmissSpecAET21_OP2 extends Optic {
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
	public double targetBeamR = 5.8;
	//public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double targetObsPos[] = W7xNBI.def().getPosOfBoxAxisAtR(targetBoxIdx, targetBeamR);

	public Sphere targetSphere = new Sphere("targetSphere", targetObsPos, 0.025, NullInterface.ideal());

	
	/**** Port Tube****/
	
	public double portTubeDiameter = 0.300;
	public double portTubeLength = 1.000;
	public double portTubeCentre[] = Util.minus(frontDiscCentre, Util.mul(portAxis, 0.010 + portTubeLength / 2));
	public Cylinder portTubeCyld = new Cylinder("portTubeCyld", portTubeCentre, portAxis, portTubeDiameter/2, portTubeLength, Absorber.ideal());


	
	
	/**** Lens ****/
	public double lensFocalLength = 0.100;
	public double lensThickness = 0.020;
	public double lensFromFront = 0.250;
	public double lensPortRightShift = 0.080;	
	public double lensPortUpShift = 0.040;	
	public double lensCentrePos[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -lensFromFront),
																		Util.plus(Util.mul(portRight, lensPortRightShift),
																				  Util.mul(portUp, lensPortUpShift))));

	
	/**** Mirror ****/	
	
	//public double mirror1Angle = (90 + 45) * Math.PI / 180;
	//public double mirror1Normal[] = Util.reNorm(Algorithms.rotateVector(Algorithms.rotationMatrix(portUp, mirror1Angle), portAxis));	
	
	public double mirror1FromFront = 0.080;
	public double mirror1PortRightShift = lensPortRightShift + 0.000;	
	public double mirror1PortUpShift = lensPortUpShift + 0.000;	
	public double mirrorWidth = 0.100;
	public double mirrorHeight = 0.050;	
	public double mirror1CentrePos[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror1FromFront),
																		Util.plus(Util.mul(portRight, mirror1PortRightShift),
																				  Util.mul(portUp, mirror1PortUpShift))));
	
	
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, mirror1CentrePos));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVecBox(targetBoxIdx), observationVec));
	
	public double lensNormal[] = Util.reNorm(Util.minus(lensCentrePos, mirror1CentrePos));
	
		
	public double lensDiameter = 0.050;
	
	//Nikon50mmF11 lens1 = new Nikon50mmF11(lensCentrePos, lensFocalLength / 0.050, lensNormal);
	public Medium lensMedium = new Medium(new BK7());
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens", 
			lensCentrePos, lensNormal, lensDiameter/2, lensFocalLength, lensThickness, lensMedium, IsoIsoInterface.ideal(), designWavelenth);
	
	public double mirror1Normal[] = Util.reNorm(Util.mul(Util.plus(lensNormal, observationVec), 0.5));
	public double mirror1Right[] = Util.reNorm(Util.cross(mirror1Normal, globalUp));
	public double mirror1Up[] = Util.reNorm(Util.cross(mirror1Right, mirror1Normal));
	
	public double mirrorFocusPos[] = Util.plus(mirror1CentrePos, Util.mul(lensNormal, 0.100));
	
	//public Square mirror1 = new Square("mirror1", mirror1CentrePos, mirror1Normal, mirror1Up, mirrorHeight, mirrorWidth, Reflector.ideal());
	//public Dish mirror1 = new Dish("mirror1", mirror1CentrePos, mirror1Normal, 1.000, 0.025, Reflector.ideal());
	public Paraboloid mirror1 = new Paraboloid("mirror1", mirror1CentrePos, mirrorFocusPos, Util.reNorm(Util.mul(observationVec, 1.0)), 0.030, null, null, Reflector.ideal());

	/*** Fibre plane ****/
	public double fibrePlaneFromLens1 = lensFocalLength + 0.000;
	public double fibrePlanePos[] = Util.plus(lensCentrePos, Util.mul(lensNormal, fibrePlaneFromLens1));
	public double fibrePlaneSize = 0.100;
	
	public double fibrePlaneRight[] = Util.reNorm(Util.cross(lensNormal, mirror1Up));
	public double fibrePlaneUp[] = Util.reNorm(Util.cross(fibrePlaneRight, lensNormal));
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, lensNormal, fibrePlaneUp, fibrePlaneSize, fibrePlaneSize, Absorber.ideal());
	
	
	public STLMesh panelEdge = new STLMesh("panel", "/work/cad/aet21/conflicting-panel-aet21.stl");
	
	public Element tracingTarget = mirror1;
	
	public BeamEmissSpecAET21_OP2() {
		super("beamSpec-aet21-op2");
		
		//addElement(frontDisc);
		addElement(panelEdge);
		addElement(targetSphere);
		addElement(portTubeCyld);
		addElement(mirror1);
		addElement(lens1);
		addElement(fibrePlane);
		
	}

	public String getDesignName() { return "aet21-op2";	}
		

}
