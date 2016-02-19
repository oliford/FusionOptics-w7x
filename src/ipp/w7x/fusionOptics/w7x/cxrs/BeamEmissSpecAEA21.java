package ipp.w7x.fusionOptics.w7x.cxrs;

import ipp.w7x.neutralBeams.W7xNBI;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.materials.BK7;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

public class BeamEmissSpecAEA21 extends Optic {
	public double globalUp[] = new double[]{ 0,0,1 };
	public double designWavelenth = 500e-9;
	
	/***** Port *****/
	public double vesselHoleForPort[][] = {
			{1.786392333984375, 6.210712890625, -0.3816764831542969},  //bottom
			{2.213273681640625, 6.0711357421875, -0.3718493347167969},
			{1.7861307373046875, 6.210935546875, 0.37232568359375}, //top
			{2.21068896484375, 6.07581591796875, 0.3692406005859375}		
		};
	
	public double vesselHoleCentre[] = Util.mul(Util.plus(Util.plus(vesselHoleForPort[0], vesselHoleForPort[1]),
													Util.plus(vesselHoleForPort[2], vesselHoleForPort[3])), 1.0/4);
	
	public double portNormal[] = Util.reNorm(Util.minus(vesselHoleCentre, new double[]{ 0,0, vesselHoleCentre[2] })); // +radial
	public double portUp[] = globalUp.clone();
	public double portRight[] = Util.cross(portNormal, globalUp);
	
	public double portUpFromCentre = 0.400;
	public double portAsideFromCetre = 0.100;
	public double portInFromVesselHole = 0.050;
	
	public double approxMirrorPos[] = Util.plus(vesselHoleCentre, 
										Util.plus(Util.mul(portNormal, -portInFromVesselHole),
										Util.plus(Util.mul(portUp, portUpFromCentre),
												  Util.mul(portRight, portAsideFromCetre)
											)));
	
	/***** Observation target ****/
	public int targetBeamIdx = 4; //Q5
	public double targetBeamR = 5.8;
	public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, approxMirrorPos));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVec(targetBeamIdx), observationVec));
	
	/***** Mirror ****/
	
	public double mirrorNormal[] = Util.reNorm(Util.plus(observationVec, portNormal));
	public double mirrorUp[] = observationUp.clone();
	
	public double mirrorWidth = 0.130;
	public double mirrorHeight = 0.130;
	public Square mirror = new Square("mirror", approxMirrorPos, mirrorNormal, mirrorUp, mirrorHeight, mirrorWidth, Reflector.ideal());
	
	
	/**** First Lens ****/
	public double mirrorToLens1 = 0.100;
	public double lens1Pos[] = Util.plus(approxMirrorPos, Util.mul(portNormal, mirrorToLens1));
	
	public Medium lens1Medium = new Medium(new BK7()); 
	public SimplePlanarConvexLens lens = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lens1Pos,
											portNormal,
											0.075, // radius
											0.260, // focal length
											0.030, // centreThickness, 
											lens1Medium, 
											IsoIsoInterface.ideal(),
											designWavelenth);

	
	/****** Virtual image plane (which we call the fibre plane for now) ******/
	public double lens1ToFibrePlane = 0.280;
	public double fibrePlanePos[] = Util.plus(lens1Pos, Util.mul(portNormal, lens1ToFibrePlane));
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, portNormal, portUp, 0.150, 0.150, Absorber.ideal());	
	
	
	public double[] R = { 5.50, 5.55, 5.60, 5.65, 5.70, 5.75, 5.80, 5.85, 5.90, 5.95, 6.00 };	
	
	
	public Element tracingTarget = mirror;

	public BeamEmissSpecAEA21() {
		super("beamSpec-aea21");
		
		addElement(mirror);
		addElement(lens);
		addElement(fibrePlane);
	}
	
	public String getDesignName() { return "aea21";	}
	
}
