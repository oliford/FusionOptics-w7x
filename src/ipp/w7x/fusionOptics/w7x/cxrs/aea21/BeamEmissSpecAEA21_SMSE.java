package ipp.w7x.fusionOptics.w7x.cxrs.aea21;

import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;

import java.lang.reflect.Array;
import java.util.Arrays;

import algorithmrepository.Algorithms;
import net.jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.EdmundOptics50mmAspheric;
import fusionOptics.lenses.ThorLabs100mmAspheric;
import fusionOptics.materials.BK7;
import fusionOptics.materials.Sapphire;
import fusionOptics.optics.STLMesh;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;
import fusionOptics.types.Surface;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAEA21_SMSE extends Optic {
	
	public String lightPathsSystemName = "AEA21-SMSE";
	
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ He_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public double portNormal[] = Util.reNorm(new double[]{ 0.7725425415039062, 2.3776411743164063, 1.52587890625e-08 });	// roughly , from CAD
	public double portEntryPos[] = new double[] { 2.0940919189453124, 6.0920563964843755, 0.3777649383544922 };  //point roughly in middle of end of immersion tube
	
	public double virtualObsPos[] = { 2.1224104306465854,	6.106432410473533,	0.3768450856770087 }; //closest approach of all LOSs, from lightAssesment (not yet known)
	
	/***** Observation target ****/
	//public int targetBeamIdx = 6; // 6 = Q7 = K21 lower radial   
	public double targetBeamR = 5.65;
	//public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double targetObsPos[] = W7xNBI.def().getPosOfBoxAxisAtR(1, targetBeamR);
	public double sourceNormal[] =  Util.reNorm(Util.minus(targetObsPos, portEntryPos));
	
	public double overrideObsPositions[][][] = null;
	
	//directions perp to port, sidesways and toward/away from source 
	public double portSourcePerp[] = Util.reNorm(Util.cross(sourceNormal, portNormal));
	public double portSourcePlane[] = Util.reNorm(Util.cross(portNormal, portSourcePerp));
	
	public double opticsTiltInPortSideways = 0 * Math.PI / 180;
	public double opticsTiltInPortToSource = 0 * Math.PI / 180; //+ve is away from source
	public double opticAxisA[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePerp, opticsTiltInPortToSource), portNormal));
	public double opticAxis[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePlane, opticsTiltInPortSideways), opticAxisA));
	
	/**** Mirror *****/
	public double mirrorDistIntoPort = 0.010;
	public double mirrorDistAwayFromSource = 0.000; //~toroidally in vessel
	public double mirrorDistSidewaysInPort = 0.000; //up/down in vessel
	public double mirrorCentrePos0[] = Util.plus(portEntryPos, Util.mul(opticAxis, mirrorDistIntoPort)); 
	public double mirrorCentrePos[] = Util.plus(Util.plus(mirrorCentrePos0, 
													Util.mul(portSourcePlane, -mirrorDistAwayFromSource)),
													Util.mul(portSourcePerp, mirrorDistSidewaysInPort));
	
	public double mirrorRotationInPlane = 0 * Math.PI / 180;
	public double mirrorWidth = 0.130; // [Made up ]
	public double mirrorHeight = 0.070;
					
	public double mirrorNormal[] = Util.reNorm(Util.plus(sourceNormal, opticAxis));

	public double mirrorA[] = Util.reNorm(Util.cross(sourceNormal, opticAxis));
	public double mirrorB[] =  Util.reNorm(Util.cross(mirrorA, mirrorNormal));
	public double mirrorX[] = Util.reNorm(Util.plus(Util.mul(mirrorA, FastMath.cos(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.sin(mirrorRotationInPlane))));
	public double mirrorY[] = Util.reNorm(Util.plus(Util.mul(mirrorA, -FastMath.sin(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.cos(mirrorRotationInPlane))));
	
	public Square mirror = new Square("mirror", mirrorCentrePos, mirrorNormal, mirrorX, mirrorHeight, mirrorWidth, Reflector.ideal());
	
	public final String backgroundSTLFiles[] = {
			"/work/cad/aea21/bg-targetting/baffle-m3.off-aea21-cut.stl",
			"/work/cad/aea21/bg-targetting/panel-m21.off-aea21-cut.stl",
			"/work/cad/aea21/bg-targetting/panel-m30.off-aea21-cut.stl",
			"/work/cad/aea21/bg-targetting/shield-m3.off-aea21-cut.stl",
	};
		
	//public NodesAndElementsMesh shieldTiles = new2 NodesAndElementsMesh("shield", "/work/ipp/w7x/cad/shield-m2", mirrorPos, 0.150);
	
	/***** Entry Window *****/
	public double windowDistBehindMirror = 0.068;
	public double entryWindowDiameter = 0.068; // DN100CF=98mm, DN63CF=68mm
	public double entryWindowThickness = 0.003; // [Made up]
	
	public double entryWindowFrontPos[] = Util.plus(mirrorCentrePos, Util.mul(opticAxis, windowDistBehindMirror));
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	/**** Main Lens *****/
	public double lens1DistBehindWindow = 0.062;
	
	/* ** Thor Labs, same as AEM21 ** */
	/*public double lens1Diameter = 0.095 + 0.001;	
	public double lens1CentreThickness = 0.00874;
	public double lens1FocalLength = 0.200;
	//public double lens1CurvatureRadius = 0.10336;
	public double lens1ClearAperture = 0.0735;
	*/
	
	/* ** OptoSigma SLB-80-200PM ** */
	public double lens1Diameter = 0.080 + 0.001;	
	public double lens1CentreThickness = 0.01100;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1038;
	public double lens1ClearAperture = 0.079;
	//*/
	
	/* Edmund Optics modified 27-501 */
	/*public double lens1Diameter = 0.090 + 0.001;	
	public double lens1CentreThickness = 0.01700;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1035;
	public double lens1ClearAperture = 0.089;
	*/
	
	/* Edmund Optics modified 27-501 */
	/*public double lens1Diameter = 0.080 + 0.001;	
	public double lens1CentreThickness = 0.01100;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1038;
	public double lens1ClearAperture = 0.079;
	//*/
	public double lens1CentrePos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, lens1DistBehindWindow));
	
	
	//public double focalLength = 0.200; // Would be better, NA~0.33, much better focus

	public Medium lensMedium = new Medium(new BK7());  
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	//public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lens1CentrePos,
											opticAxis,
											lens1Diameter/2, // radius
											lens1CurvatureRadius, // rad curv
											//lens1FocalLength, // rad curv
											lens1CentreThickness,  
											lensMedium, 
											IsoIsoInterface.ideal());//,
											//designWavelenth);
	
	//public ThorLabs100mmAspheric lens1 = new ThorLabs100mmAspheric(lensCentrePos, opticAxis, 2.0);
	
	
	
	public double lensIrisPos[] = Util.plus(lens1CentrePos, Util.mul(opticAxis, -0.002));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lens1Diameter, lens1ClearAperture*0.48, null, null, Absorber.ideal());
	
	
	/**** Lens2 *****/
	public double lens2DistBehindLens1 = 0.075; //as far as we get on the bolts in the CAD
	//public double lens2DistBehindLens1 = 0.060;
	
	/*public double lens2Diameter = 0.095 + 0.001;
	public double lens2CentreThickness = 0.00874;
	public double lens2FocalLength = 0.200;
	//public double lens2CurvatureRadius = 0.10336;
	public double lens2ClearAperture = 0.0735;
	*/
	

	/** As lens1 */
	public double lens2Diameter = lens1Diameter;	
	public double lens2CentreThickness = lens1CentreThickness;
	//public double lens2FocalLength = lens1FocalLength;
	public double lens2CurvatureRadius = lens1CurvatureRadius;
	public double lens2ClearAperture = lens1ClearAperture;
	
	
	
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(opticAxis, lens2DistBehindLens1));
	
	public double focalLength2 = 0.200; // Would be better, NA~0.33, much better focus
	
	public Medium lens2Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	//public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens2",
											lens2CentrePos,
											opticAxis,
											lens2Diameter/2, // radius
											lens2CurvatureRadius, // rad curv
											//lens2FocalLength, // rad curv
											lens2CentreThickness,  
											lens2Medium, 
											IsoIsoInterface.ideal());//,
											//designWavelenth);
	
	//public ThorLabs100mmAspheric lens2 = new ThorLabs100mmAspheric(lens2CentrePos, opticAxis, 2.0);
	
	public double lens2IrisPos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, -0.002));
	public Iris lens2Iris = new Iris("lens2Iris", lens2IrisPos, opticAxis, lens2Diameter, lens2Diameter*0.48, null, null, Absorber.ideal());
	

	/*** Fibres ****/
	public int beamIdx[] = null;
	public double[][] channelR = null;
	public double[][][] fibreEndPos = null;
	public double[][][] fibreEndNorm = null;
	public String lightPathRowName[] = null;
	
	public double[] channelZ;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.22; // [ written on the fibre bundle packing reel ]
	
	public double fibreEndDiameter = 0.0004; // 400um AUG-like fibres
	public double fibrePlaneBehindLens2 = 0.100;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public double beamAxis[] = W7xNBI.def().uVec(0);
	
	public double fibrePlanePos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, fibrePlaneBehindLens2)); 
	public double fibresXVec[] = Util.reNorm(Util.cross(Util.cross(beamAxis, opticAxis),opticAxis));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, opticAxis));	
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, opticAxis, fibresYVec, 0.100, 0.100, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(opticAxis, 0.050)), 
										opticAxis, fibresYVec, 0.300, 0.300, Absorber.ideal());

	public Disc strayPlane = new Disc("strayPlane", Util.plus(entryWindowFrontPos, Util.mul(portNormal, -0.300)),
									portNormal, 0.200, Absorber.ideal());
	
	//plane to cover up ports AEE30 and AEK30 for background targetting
	public double port30PlanePos[] = { -4.886710693359375, 4.122841796875, -0.09633171272277832 };
	public double port30PlaneNormal[] = { -0.73000834,  0.67882323, -0.07928962 };
	public double port30PlaneUp[] = Util.reNorm(Util.cross(Util.cross(port30PlaneNormal, globalUp), port30PlaneNormal));
	
	public Square port30Plane = new Square("port30Plane", port30PlanePos, port30PlaneNormal, port30PlaneUp, 1.00, 2.00, Absorber.ideal());


	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lens1CentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.600, NullInterface.ideal());

	/** Plasma radiating surface for heat-load analysis */
	public double[] radSurfaceCentre = { 1.68544196,  5.88435327,  0.43004889 };
	public double[] radSurfaceNormal = { -0.2822023 , -0.85883142, -0.42751662 };
	public double[] radUp = Util.createPerp(radSurfaceNormal);
	//public double radSurfWidth = 1.100; //for testing inner parts (window etc)
	//public double radSurfHeight = 0.900;

	public double radSurfWidth = 1.800; //for testing closed shutter
	public double radSurfHeight = 1.300;

	public Square radSurface = new Square("radSurface", radSurfaceCentre, radSurfaceNormal, radUp, radSurfHeight, radSurfWidth, NullInterface.ideal()); 
	
	
	public Element tracingTarget = mirror;
	public Surface checkSurface = mirror;
			
	public BeamEmissSpecAEA21_SMSE() {
		super("beamSpec-aea21");
		
		//addElement(new STLMesh("panel", "/work/ipp/w7x/cad/aea21/panel-cutting-aea21-edge-channels-cut-front.stl", portEntryPos, 0.500));
		
		addElement(mirror);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lensIris);
		addElement(lens1);
		addElement(lens2Iris);
		addElement(lens2);
		addElement(fibrePlane);
		addElement(beamPlane);
		//addElement(shieldTiles);
		
		setupFibrePositions();
		//makeLampFibre();
		setupFibrePlanes();
		
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
		/*fibrePlanes = new Square[nFibres];
		for(int i=0; i < nFibres; i++){
			//double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
			double norm[] = fibreEndNorm[i];
			double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
			double y[] = Util.reNorm(Util.cross(x, norm));
			fibrePlanes[i] = new Square("fibrePlane_" + i, fibreEndPos[i], norm, y, 0.007, 0.007, NullInterface.ideal());
			addElement(fibrePlanes[i]);
		}*/
			
		addElement(catchPlane);
		addElement(strayPlane);
		addElement(port30Plane);
		
		channelZ = new double[ channelR.length];
		for(int i=0; i < channelR.length; i++){
			channelZ[i] = W7xNBI.def().getPosOfBoxAxisAtR(0, channelR[0][i])[2];
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
		double cyldLen = 3.000;
		double cyldRadius = 0.0025;
		
		double u[] = Util.reNorm(Util.mul(opticAxis, -1));
		double p[] = Util.plus(mirrorCentrePos, Util.mul(u, -cyldLen));
		 
		System.out.println("Part.show(Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
				",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 +
				"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360)); #TubeAxisCylinder");
		
		u = Util.reNorm(Util.mul(sourceNormal, -1));
		p = Util.plus(mirrorCentrePos, Util.mul(u, -cyldLen));
		
		System.out.println("Part.show(Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
				",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 +
				"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360)); #SourceAxisCylinder");
		
		double adjustedTargetPos[] = Util.plus(targetObsPos, Util.mul(opticAxis, mirrorDistIntoPort));
		System.out.println("Part.show(Part.makeSphere("+(cyldRadius*1e3)+", FreeCAD.Vector("+adjustedTargetPos[0]*1e3+","+adjustedTargetPos[1]*1e3+","+adjustedTargetPos[2]*1e3 + "))); # adjustedTargetPos");
		
		System.out.println("Part.show(Part.makeSphere("+(cyldRadius*1e3)+", FreeCAD.Vector("+mirrorCentrePos[0]*1e3+","+mirrorCentrePos[1]*1e3+","+mirrorCentrePos[2]*1e3 + "))); # mirrorCentrePos");
		
	}

	public String getDesignName() { return "aea21-smse";	}

	public Element[] makeSimpleModel() {
		return new Element[0];
	}
	
	/** Set fibre positions according to 'mse5' CAD design
	 */
	private final int fibreCols = 16; 
	private final int fibreRows = 9;
	private double ferruleColumnTilt = 21.8 * Math.PI / 180;
	
	private double fibreSpacingH = 0.6e-3;
	private double fibreSpacingW = 0.7e-3;
	
	private double fibrePlateFromRodEnd = 0.088;
	private double fibreFirstRowBelowRodCentre = 0.65e-3;
	private double fibreFirstColumnLeftOfRodCentre = 8.52e-3;
	
	private int[][] skipFibres = {{15, 5}, {15, 6}, {15, 7}, { 14, 8 }, {15, 8}};
		
	private double rodAxis[] = opticAxis;
	private double rodEndPos[] = Util.plus(
			 						Util.plus(entryWindowBackPos, Util.mul(opticAxis, 0.2965)),
			 						Util.mul(globalUp, 0.0005) ); //not sure why :/
									
	private double rodLength = 1.000;
	private double rodCentre[] = Util.plus(rodEndPos, Util.mul(rodAxis, rodLength/2));
	public Cylinder rod = new Cylinder("rod", rodCentre, rodAxis, 0.005, rodLength, NullInterface.ideal());
	
	private double ferruleAngleToUp = (180-7.4) * Math.PI / 180;
	private double ferruleRight0[] = Util.reNorm(Util.cross(globalUp, rodAxis));
	private double ferruleUp0[] = Util.reNorm(Util.cross(rodAxis, ferruleRight0));
	
	private double ferruleUp[] = Util.reNorm(Util.plus(Util.mul(ferruleUp0, FastMath.cos(ferruleAngleToUp)), Util.mul(ferruleRight0, -FastMath.sin(ferruleAngleToUp))));
	private double ferruleRight[] = Util.reNorm(Util.plus(Util.mul(ferruleUp0, FastMath.sin(ferruleAngleToUp)), Util.mul(ferruleRight0, FastMath.cos(ferruleAngleToUp))));
	//private double rodUp[] 
	
	
	
	/* Adjustment to cope with other changes... */
	/*private double ferruleAdjustUp = 0.000;
	private double ferruleAdjustRight = 0.000;
	private double ferruleAdjustFocus = 0.000;	
	//*/
	
	// adjusted to match for original L1-L2 of 60mm, not sure whats different than the generation, but ... meh
	/*private double ferruleAdjustUp = -0.0001; 
	private double ferruleAdjustRight = -0.00017;
	private double ferruleAdjustFocus = -0.0055;
	//*/
	
	// for L1-L2 of 75mm
	private double ferruleAdjustUp = -0.0003; 
	private double ferruleAdjustRight = -0.00017;
	private double ferruleAdjustFocus = 0.0053;
	//*/

	// for L1-L2 of 120mm, just to see how far we can go
	// works, spot size is still mostly below 10mm, but then we lose the HFS measurements
	/*private double ferruleAdjustUp = -0.0001; 
	private double ferruleAdjustRight = -0.00017;
	private double ferruleAdjustFocus = 0.034;
	//*/

	private void setupFibrePositions() {
		int nBeams = 1;
		channelR = new double[nBeams][];
		beamIdx = new int[] { W7xNBI.BEAM_Q8 };
		lightPathRowName = new String[]{ "SMSE" };
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = fibreCols * fibreRows - skipFibres.length;
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			channelR[iB] = new double[nFibres];
		
			//find row origin (green/blue dots in diagram)
			double origin[] = rodEndPos.clone();
			origin = Util.plus(origin, Util.mul(rodAxis, -fibrePlateFromRodEnd));
			origin = Util.plus(origin, Util.mul(ferruleUp, ferruleAdjustUp));
			origin = Util.plus(origin, Util.mul(ferruleRight, ferruleAdjustRight));			
			origin = Util.plus(origin, Util.mul(rodAxis, ferruleAdjustFocus));
			
			int iF=0;
			for(int iY=0; iY < fibreRows; iY++ ) {
				double rowOrigin[] = Util.plus(origin, Util.mul(ferruleUp, -fibreFirstRowBelowRodCentre + iY * fibreSpacingH));
nextFibre:		for(int iX=0; iX < fibreCols; iX++) {
					for(int i=0; i < skipFibres.length; i++) {
						if(skipFibres[i][0] == iX && skipFibres[i][1] == iY)
							continue nextFibre;
					}
					
					channelR[iB][iF] = 5.4 + iX/100;
					double x = -fibreFirstColumnLeftOfRodCentre + iX * fibreSpacingW;
					x += (iY * fibreSpacingH) * FastMath.tan(ferruleColumnTilt);
					fibreEndPos[iB][iF] = Util.plus(rowOrigin, Util.mul(ferruleRight, x));
					//fibreEndPos[iB][iF] = origin.clone();
					fibreEndNorm[iB][iF] = Util.mul(fibrePlane.getNormal(), -1.0);
					iF++;
				}
			}

		}
		
	}
		
	
	private void setupFibrePlanes() {
		int nBeams = channelR.length;
		fibrePlanes = new Square[nBeams][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			fibrePlanes[iB] = new Square[nFibres];
		
			for(int iF=0; iF < nFibres; iF++){
	
				double norm[] = fibreEndNorm[iB][iF];
				double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
				double y[] = Util.reNorm(Util.cross(x, norm));
				fibrePlanes[iB][iF] = new Square("fibrePlane_Q" + (iB+1) + "_" + iF, fibreEndPos[iB][iF].clone(), norm, y, 0.007, 0.007, NullInterface.ideal());
				//addElement(fibrePlanes[i]);
			}
		}
	}
		

}
