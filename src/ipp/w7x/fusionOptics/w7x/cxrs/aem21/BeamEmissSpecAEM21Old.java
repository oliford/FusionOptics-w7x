package ipp.w7x.fusionOptics.w7x.cxrs.aem21;

import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEW21;
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
public class BeamEmissSpecAEM21Old extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ e_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public double portNormal[] = { 0.355, -0.145, 0.923 };	// [fromDesigner-20151106] lens back plane normal, rotated to M21
	//public double entryWindowFrontPos[] = { -0.424, 5.383, 1.218 }; // [ Jürgen's sim 'Mittelpunkt des Fensters im AEM41' rotated to module 21 ]
	public double portPos[] = new double[] {-0.4614180908203125, 5.39522119140625, 1.2168809204101563 }; //point along port, nearer back, from CAD
	public double entryWindowFrontPos[] = Util.plus(portPos, Util.mul(portNormal, 0.010));
	
	/***** Observation target ****/
	public int targetBeamIdx = 6; // 6 = Q7 = K21 lower radial   
	public double targetBeamR = 5.7;
	public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	
	/***** Entry Window *****/
	public double entryWindowDiameter = 0.120; // [Jurgen's Frascati poster + talking to Jurgen + plm/CAD ]
	public double entryWindowThickness = 0.010; // [Made up]
	
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, portNormal, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, portNormal, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, portNormal, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	/**** Mirror *****/
	public double mirrorToWindowDist = 0.250;
	public double mirrorPos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, -mirrorToWindowDist));
	
	public double mirrorRotation = 30 * Math.PI / 180;
	public double mirrorWidth = 0.200;
	public double mirrorHeight = 0.100;
					
	public double sourceNormal[] =  Util.reNorm(Util.minus(targetObsPos, mirrorPos));
	public double mirrorNormal[] = Util.reNorm(Util.plus(sourceNormal, portNormal));
	public double mirrorA[] = Util.reNorm(Util.cross(sourceNormal, portNormal));
	public double mirrorB[] =  Util.reNorm(Util.cross(mirrorA, mirrorNormal));
	public double mirrorX[] = Util.reNorm(Util.plus(Util.mul(mirrorA, FastMath.cos(mirrorRotation)), Util.mul(mirrorB, FastMath.sin(mirrorRotation))));
	public double mirrorY[] = Util.reNorm(Util.plus(Util.mul(mirrorA, -FastMath.sin(mirrorRotation)), Util.mul(mirrorB, FastMath.cos(mirrorRotation))));
	
	public Square mirror = new Square("mirror", mirrorPos, mirrorNormal, mirrorX, mirrorWidth, mirrorHeight, Reflector.ideal());
			
	/**** Lens *****/
	
	//public double lensCentrePosM41[] = {-0.375, 5.363, 1.339, }; // [CAD fromDesigner-20151106, rotated to M21]
	public double lensCentrePos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, 0.060));
	
	public double lensDiameter = 0.100 + 0.001;
	
	//public double focalLength = 0.100; // [J.Balzhuhn's eBANF for the lens]. Delivers NA~0.40 to central fibres
	public double focalLength = 0.150; // Would be better, NA~0.33, much better focus
	//public double focalLength = 0.130; // Would be better, NA~0.30, much better focus
	//public double focalLength = 0.140; // Would be better, NA~0.28, much better focus
	
	public Medium lensMedium = new Medium(new BK7());  // [J.Balzhuhn's eBANF for the lens]
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lensCentrePos,
											portNormal,
											lensDiameter/2, // radius
											focalLength, // focal length
											0.030, // centreThickness [ fromDesigner CAD for glass - although it's curvature doesn't match Jurgen's eBANF's focal length] 
											lensMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lensIrisPos[] = Util.plus(lensCentrePos, Util.mul(portNormal, -0.002));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, portNormal, lensDiameter, lensDiameter*0.48, null, null, Absorber.ideal());
	

	/*** Fibres ****/
	int nFibres = 10;
	
	public double fibre1EndPos[] = { -2.89649, -4.54998, 1.44268 }; // core channel, [fromDesigner-20151106] 
	public double fibre10EndPos[] = { -2.85912, -4.50896, 1.43616 }; // edge channel,  [fromDesigner-20151106]
		
	public double[] R = { 5.50, 5.55, 5.60, 5.65, 5.70, 5.75, 5.80, 5.85, 5.90, 5.95, 6.00 };	
	
	public double[][] fibreEndPos = { 
			{ -2.8858372192382813, -4.553509700927734, 1.4182009045410156 },
			{ -2.8858725486682864, -4.544170403817291, 1.4327007119598933 },
			{ -2.881634884407829, -4.537143890560845, 1.4355888800060466 },
			{ -2.877117408050361, -4.530367474369523, 1.4375979351367065 },
			{ -2.8722289338673828, -4.524490300678137, 1.4375428674983708 },
			{ -2.8685806344943576, -4.520687937282985, 1.436558426276313 },
			{ -2.8650675876871743, -4.517360445149739, 1.4350639623006185 },
			{ -2.861818899400499, -4.51400216840956, 1.4341508200412325 },			
			{ -2.8583163592215355, -4.512142818043448, 1.4304308811217898 },
			{ -2.8551192170538777, -4.510505395735583, 1.4269533444439038 },
			
		}; 
	public double[][] fibreEndNorm = { 
			{ 0.3170934875229932,  -0.21285073924843653, -0.9242003478531586 },
			{ 0.2975662811064887,  -0.23530400943835703, -0.9252493347691139 },
			{ 0.2741704645186036,  -0.260515694491969, -0.9257224904413958 },
			{ 0.2509472182196351,  -0.2873821142552679, -0.924357622391896 },
			{ 0.21706372294218446, -0.320247924460108, -0.9221304718213532 },
			{ 0.18530745073596588, -0.3567338030754802, -0.9156430212943482 },
			{ 0.1652075585587437,  -0.38050365396483893, -0.909902979396411 },
			{ 0.15173825624623644, -0.39824329718787105, -0.9046423480228442 },			
			{ 0.1396887554149075, -0.4110136814389558, -0.9008633665993033 },
			{ 0.12786645556381948, -0.4237374462750325, -0.8967144172844885 }
		};

	
	public double[] Z;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.28; // [ written on the fibre bundle packing reel ]
	
	public double fibreEndDiameter = 0.001; // roughly 1mm diameter [ looking at the fibres, and it agrees with 540x total / 10 = 54x per bundle. 54x * jacket size = ~1mm area ]
		
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public double beamAxis[] = W7xNBI.def().uVec(0);
	
	public double fibrePlanePos[] = Util.plus(lensCentrePos, Util.mul(portNormal, 0.060)); 
	public double fibresXVec[] = Util.reNorm(Util.cross(Util.cross(beamAxis, portNormal),portNormal));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));	
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, portNormal, fibresYVec, 0.020, 0.070, NullInterface.ideal());
	public Square fibrePlanes[];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(portNormal, 0.050)), 
											portNormal, fibresYVec, 0.300, 0.300, Absorber.ideal());


	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.200, NullInterface.ideal());

	public Element tracingTarget = mirror;
		
	public BeamEmissSpecAEM21Old() {
		super("beamSpec-aem21");
		
		addElement(mirror);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lensIris);
		addElement(lens1);
		addElement(fibrePlane);
		addElement(beamPlane);
		/*
		fibreEndPos = new double[nFibres][];
		fibrePlanes = new Square[nFibres];
		double dp[] = Util.mul(Util.minus(fibre10EndPos, fibre1EndPos), 1.0 / (nFibres - 1));
		for(int i=0; i < nFibres; i++){
			
			fibreEndPos[i] = Util.plus(fibre1EndPos, Util.mul(dp, i));
			
			double losVec[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));			
			fibreEndPos[i] = Util.plus(fibreEndPos[i], 
									Util.mul(losVec, shift[i]*1e-3));
			
			double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
			double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
			double y[] = Util.reNorm(Util.cross(x, norm));
			fibrePlanes[i] = new Square("fibrePlane_" + i, fibreEndPos[i], norm, y, 0.007, 0.007, NullInterface.ideal());
			addElement(fibrePlanes[i]);
		}
		*/
		fibrePlanes = new Square[nFibres];
		for(int i=0; i < nFibres; i++){
			//double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
			double norm[] = fibreEndNorm[i];
			double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
			double y[] = Util.reNorm(Util.cross(x, norm));
			fibrePlanes[i] = new Square("fibrePlane_" + i, fibreEndPos[i], norm, y, 0.007, 0.007, NullInterface.ideal());
			addElement(fibrePlanes[i]);
		}
			
		addElement(catchPlane);
		
		Z = new double[nFibres];
		for(int i=0; i < nFibres; i++){
			Z[i] = W7xNBI.def().getPosOfBoxAxisAtR(0, R[i])[2];
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());		
	}

	public String getDesignName() { return "aem21";	}
	
	public static void main(String[] args) {
		BeamEmissSpecAEW21 sys = new BeamEmissSpecAEW21();
		double c[] = sys.mirror.getCentre();
		double u[] = Util.mul(sys.mirror.getUp(), sys.mirror.getHeight()/2);
		double d[] = Util.mul(sys.mirror.getUp(), -sys.mirror.getHeight()/2);
		double r[] = Util.mul(sys.mirror.getRight(), sys.mirror.getWidth()/2);
		double l[] = Util.mul(sys.mirror.getRight(), -sys.mirror.getWidth()/2);
		OneLiners.dumpArray(Util.plus(Util.plus(c, u), l));
		OneLiners.dumpArray(Util.plus(Util.plus(c, u), r));
		OneLiners.dumpArray(Util.plus(Util.plus(c, d), l));
		OneLiners.dumpArray(Util.plus(Util.plus(c, d), r));
		
	}	

}
