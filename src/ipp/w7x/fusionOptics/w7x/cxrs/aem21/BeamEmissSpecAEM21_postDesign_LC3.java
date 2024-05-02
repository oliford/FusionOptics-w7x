package ipp.w7x.fusionOptics.w7x.cxrs.aem21;

import java.util.ArrayList;
import java.util.List;

import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import uk.co.oliford.jolu.OneLiners;
import otherSupport.SettingsManager;
import seed.matrix.DenseMatrix;
import algorithmrepository.Algorithms;
import algorithmrepository.exceptions.NotImplementedException;
import net.jafama.FastMath;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.EdmundOptics50mmAspheric;
import fusionOptics.lenses.Nikon135mmF28;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.lenses.ThorLabs100mmAspheric;
import fusionOptics.materials.BK7;
import fusionOptics.materials.IsotropicFixedIndexGlass;
import fusionOptics.materials.Sapphire;
import fusionOptics.optics.NodesAndElementsMesh;
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
public class BeamEmissSpecAEM21_postDesign_LC3 extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ e_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public String lightPathsSystemName = "AEM21";	
	
	// CAD from designer
	
	private boolean adjustedToLC3;
		
	//public double rotateLC3[][] = {	{ 1.009,  0.044,  0.004}, {0.061,  1.588, -0.001}, { -0.077, -0.759,  0.996} };
	public double rotateLC3[][] = {	
		{ 0.9999829389, 0.0002761317, -0.0058605577 },
		{ -0.0002674291, 0.9999986685, 0.0016920161 },
		{ 0.0058604423, -0.0016915735, 0.9999811728 } };

	
    public double offsetLC3[] = { 0.0063746567, 0.0039538344, 0.0025080040 };
   	
	public double portNormal[] = { 0.35536503, -0.14530594,  0.92336444 };
	//should transform to 0.34990725,  -0.14383813, 0.9256757
	//{ -0.23743994,  0.94225652,  0.23616716 } port left/right, parallel to front flange and tube plate, in LC3
	//transformed window centre should be -0.4775350799560547, 5.39110546875, 1.1594251098632813
	
	public double virtualObsPos[] = { -0.5830873174901741,	5.362082222103293,	1.1250303063301719 }; //closest approach of all LOSs, from lightAssesment
									  
	public double windowCentre[] = { -0.47792361,  5.38480054,  1.17044202 };
	
	/***** Mirror/Shutter *****/
	
	public double mirrorDiameter = 0.120;
	
	public double mirrorAngleAdjust = +3 * Math.PI / 180; // Adjust of shutter open angle. 0 is default open, -60 is closed, +3deg gives best throughput and direct light mitigation
	//front plate seems to be good for ang=+3 in LC3
	//public double mirrorAngleAdjust = +0.72 * Math.PI / 180; // Furthest we could get due to shutter housing 
	
	public double mirrorRingRotate = 0 * Math.PI / 180; //Adjustment of mirror mount ring
	
	public double mirrorCentrePos0[] = { -0.52209747,  5.40077637,  1.05967297 }; // shutter/mirror centre in default open position
	public double mirrorNormal0[] = { 0.95671975,  0.18248719,  0.22668426 }; // shutter/mirror normal in default open position	
	public double mirrorPivotCentre[] = { -0.53505125,  5.39330481,  1.07630972 }; //pivot of shutter/mirror to open/close -535 5393 1076
	public double mirrorPivotVector[] = { -0.23614408,  0.94177839,  0.23935211 }; //pivot of shutter/mirror to open/close -2362, 9418, 2394
	
	//rotate around shutter pivot
	public double mirrorCentrePos1[] = Util.plus(mirrorPivotCentre, 
										Algorithms.rotateVector(Algorithms.rotationMatrix(mirrorPivotVector, mirrorAngleAdjust), 
												Util.minus(mirrorCentrePos0, mirrorPivotCentre)));	
	public double mirrorNormal1[] = Algorithms.rotateVector(Algorithms.rotationMatrix(mirrorPivotVector, mirrorAngleAdjust), mirrorNormal0);
	
	//rotate around window (mouting ring)
	public double mirrorCentrePos[] = Util.plus(windowCentre, 
			Algorithms.rotateVector(Algorithms.rotationMatrix(portNormal, mirrorRingRotate), 
					Util.minus(mirrorCentrePos1, windowCentre)));

	public double mirrorNormal[] = Algorithms.rotateVector(Algorithms.rotationMatrix(portNormal, mirrorRingRotate), mirrorNormal1);

	
	public Disc mirror = new Disc("mirror", mirrorCentrePos, mirrorNormal, mirrorDiameter/2, Reflector.ideal());
	
	public STLMesh panelEdge = new STLMesh("panel", "/work/cad/aem21/panel-cutting-edge-channels-cut.stl");
	public STLMesh mirrorBlock = new STLMesh("mirrorBlock", "/work/cad/aem21/mirrorBlockSimpleOpen.stl");	
	public STLMesh mirrorClampRing = new STLMesh("mirrorClampRing", "/work/cad/aem21/mirrorRing-simple.stl");	
	public STLMesh blockPlate = new STLMesh("blockPlate", "/work/cad/aem21/blockPlate-grooved.stl");
	
	public STLMesh colar = new STLMesh("colar", "/work/cad/aem21/colar.stl");
	

	public double overrideObsPositions[][] = null;
	
	public double opticAxis[] = portNormal.clone();
	
	/***** Entry Window *****/
	
	
	public double windowDistBehindMirror = 0.170;
	public double entryWindowDiameter = 0.095; // 
	public double entryWindowThickness = 0.010; //
	public double entryWindowShift = 0.000;
	
	public double entryWindowFrontPos[] = Util.plus(windowCentre, Util.mul(opticAxis, entryWindowShift));
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	/**** Main Lens *****/

	public double lens1DistBehindWindow = 0.060;
	public double lens1Diameter = 0.075;
	public double lens1CentreThickness = 0.00874;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.10336;
	public double lens1ClearAperture = 0.0735;

	public double lens1CentrePos[] = Util.plus(windowCentre, Util.mul(opticAxis, lens1DistBehindWindow + lens1CentreThickness));
	
	//public Nikon50mmF11 objLens = new Nikon50mmF11(lensCentrePos, 0.100 / 0.050, opticAxis);
	//public Iris objLensIris = new Iris("objLensIris", lensCentrePos, opticAxis, 0.100, objLens.getCaseRadius()*0.99, null, null, Absorber.ideal());
		
	//public Nikon135mmF28 objLens = new Nikon135mmF28(lensCentrePos, 0.050 / 0.050, opticAxis);	
	//public Iris objLensIris = new Iris("objLensIris", lensCentrePos, opticAxis, 0.100, 0.050*0.48, null, null, Absorber.ideal());

	//public ThorLabs100mmAspheric objLens = new ThorLabs100mmAspheric(lensCentrePos, opticAxis);	
	//public Iris objLensIris = new Iris("objLensIris", lensCentrePos, opticAxis, 0.100, 0.050*0.48, null, null, Absorber.ideal());
		
	public Medium lensMedium = new Medium(new BK7());  
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	//public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lens1CentrePos,
											opticAxis,
											lens1Diameter/2, // radius
											lens1CurvatureRadius, // rad curv
//											lens1FocalLength, // rad curv
											lens1CentreThickness,  
											lensMedium, 
											IsoIsoInterface.ideal());//,
//											designWavelenth);
	
	public double lensIrisPos[] = Util.plus(lens1CentrePos, Util.mul(opticAxis, -0.005));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lens1Diameter, lens1ClearAperture/2, null, null, Absorber.ideal());
	
	/**** Lens2 *****/
	public double lens2DistBehindLens1 = 0.060;
	
	//public double lens2Diameter = 0.100;
	//public double lens2FocalLength = 0.200; // Would be better, NA~0.33, much better focus
	//public double lens2CentreThickness = 0.017;
	//public double lens2ClearAperture = 0.095;
	//public double lens2CurvatureRadius = 0.10350;

	public double lens2Diameter = 0.075;
	//public double lens2FocalLength = 0.200;
	public double lens2CentreThickness = 0.00874;
	public double lens2CurvatureRadius = 0.10336;
	public double lens2ClearAperture = 0.0735;
	
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(opticAxis, lens2DistBehindLens1 + lens2CentreThickness));
	
	
	public Medium lens2Medium = new Medium(new BK7());  
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	//public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens2",
											lens2CentrePos,
											opticAxis,
											lens2Diameter/2, // radius
											lens2CurvatureRadius, // focal length
//											lens2FocalLength, // focal length
											lens2CentreThickness, // centreThickness  
											lens2Medium, 
											IsoIsoInterface.ideal());//,
//											designWavelenth);
	
	public double lens2IrisPos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, -0.005));
	public Iris lens2Iris = new Iris("lens2Iris", lens2IrisPos, opticAxis, lens2Diameter*2, lens2ClearAperture/2, null, null, Absorber.ideal());
	
	/*** Fibres ****/
	//generated by setupFibrePositions according to head design
	public int beamIdx[] = null;
	public String lightPathRowName[] = null;
	public double[][] channelR = null;	
	public double[][][] fibreEndPos = null;
	public double[][][] fibreEndNorm = null;
	
	
	public double[][] fibreFocus = null;
	
	public double[][] channelZ;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.22; // As AUG
	
	public double fibreEndDiameter = 0.000480; // as AUG
	public double fibrePlaneBehindLens2 = 0.090;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public int targetBeamIdx = 7; //Q8
	public double targetBeamR = 5.7;
	public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double beamAxis[] = W7xNBI.def().uVec(targetBeamIdx);
	
	
	public double fibrePlanePos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, fibrePlaneBehindLens2)); 
	public double fibresXVec[] = Util.reNorm(Util.cross(Util.cross(beamAxis, opticAxis),opticAxis));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, opticAxis));	
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, opticAxis, fibresYVec, 0.300, 0.300, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(opticAxis, 0.050)), 
										opticAxis, fibresYVec, 0.300, 0.300, Absorber.ideal());

	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lens1CentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 1.500, 2.000, NullInterface.ideal());
	
	double strayPos[] = Util.plus(mirrorCentrePos, Util.mul(portNormal, -0.250)); 
	
	public Disc strayPlane = new Disc("strayPlane", strayPos, portNormal, 0.200, Absorber.ideal());
	
	/** Plasma radiating surface for heat-load analysis */
	public double[] radSurfaceCentre = { -0.3754414978027344, 5.5157724609375, 0.6975476684570312 };	
	public double[] radSurfaceNormal = { 0.28073852, -0.42956634,  -0.8582882 };
	public double[] radUp = Util.createPerp(radSurfaceNormal);
	//public double radSurfWidth = 1.100; //for testing inner parts (window etc)
	//public double radSurfHeight = 0.900;

	public double radSurfWidth = 1.400; //for testing closed shutter
	public double radSurfHeight = 1.100;

	public Square radSurface = new Square("radSurface", radSurfaceCentre, radSurfaceNormal, radUp, radSurfHeight, radSurfWidth, NullInterface.ideal());
	
	public double[] fprA = {  -0.7039207763671875, 5.52718408203125, 0.80253076171875 };
	public double[] fprB = {  -0.367311767578125, 5.34034814453125, 1.07567919921875 };
	public double[] fprC = {  -0.4654627075195313, 5.54656298828125, 0.9291983032226563 };
	
	public double[] fprCentre0 = Util.mul(Util.plus(fprA, fprB), 0.5);
	public double[] fprNormal = Util.reNorm(Util.cross(Util.minus(fprA, fprC), Util.minus(fprB, fprC)));
	
	public double fprPowerDensity = 4e3; //4kW/m^-2 from average of 240'C (averaged in T^4) over the FP area, for an area of ~175x175cm
	public double fprShiftIn = 0.010;
	public double fprLength = 0.300;
	public double fprWidth = 0.200;
	
	public double[] fprCentre = Util.plus(fprCentre0, Util.mul(fprNormal, fprShiftIn));
	
	public double[] fprUp0 = Util.reNorm(Util.minus(fprB, fprA));
	public double[] fprRight = Util.reNorm(Util.cross(fprNormal, fprUp0));
	public double[] fprUp = Util.reNorm(Util.cross(fprNormal, fprRight));
	
	public Square frontPlateRadiator = new Square("frontPlateRadiator", fprCentre, fprNormal, fprUp, fprLength, fprWidth, NullInterface.ideal());
		
	public Element tracingTarget = mirror;
	public Surface checkSurface = mirror;
	
	public final String backgroundSTLFiles[] = {
			"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/baffles-cut.stl",
		//"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/panel1.stl",
			"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/panel2-smallArea.stl",
			"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/panel3.stl",
			//"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/panel4.stl",
			//"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/panel5.stl",
			"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/shield-cut-smallArea.stl",
			"/home/oliford/rzg/w7x/cad/aem21/bg-targetting/target-cut-smallArea.stl",
	};
			
	/** Set fibre positions according to design sent to Ceramoptec.
	 * The design was set to match Q7 and Q8 in LC3 with the mirror at +3' (and does)
	 */
	private final int ferruleRowNFibres[] = { 54, 54, 8, 9 }; 
	private final double ferruleRowSidewaysOffset[] = { 0.00225, 0.00326 };
	private final double ferruleRowUpwardsOffset[] = { -0.0122, -0.002, -0.080, 0.040 };
	private final double ferruleRowAngle[] = { -9.22*Math.PI/180, 0, Math.PI, Math.PI /2};
	private final double ferruleRowCurvatureRadius[] = { 0.06896,  0.05826 };
	private final double ferruleCurvatureSidewaysOffset[] = { -0.00127, -0.00056 };
	private final double ferruleCurvatureCenterToRod[] = { 0.16373, 0.15115 };
	private int ferruleCrossFibreCrossSelect[][] = { { 16-1, 15-1 }, { 41-1, 41-1 } };
	private double ferruleCrossFibreSpacing[] = { 0.0028, 0.00311 };
	private double ferruleCrossFibreRelDists[][] = { 
				{ -4.5, -3.5, -2.5,    -0.5, 0.5,        2.5, 3.5, 4.5 },
				{ -5.0, -4.0, -3.0,    -1.0, 0.0, 1.0,   3.0, 4.0, 5.0 }
			};	
				
	
	
	private double rodAxis[] = opticAxis.clone();
	private double rodEndPos[] = Util.plus(entryWindowBackPos, Util.mul(opticAxis, 0.289500));
	private double rodLength = 1.000;
	private double rodCentre[] = Util.plus(rodEndPos, Util.mul(rodAxis, rodLength/2));
	public Cylinder rod = new Cylinder("rod", rodCentre, rodAxis, 0.005, rodLength, NullInterface.ideal());
	
	private double ferruleAngleToUp = 56.5 * Math.PI / 180;
	private double ferruleRight0[] = Util.reNorm(Util.cross(globalUp, rodAxis));
	private double ferruleUp0[] = Util.reNorm(Util.cross(rodAxis, ferruleRight0));
	
	private double ferruleUp[] = Util.reNorm(Util.plus(Util.mul(ferruleUp0, FastMath.cos(ferruleAngleToUp)), Util.mul(ferruleRight0, -FastMath.sin(ferruleAngleToUp))));
	private double ferruleRight[] = Util.reNorm(Util.plus(Util.mul(ferruleUp0, FastMath.sin(ferruleAngleToUp)), Util.mul(ferruleRight0, FastMath.cos(ferruleAngleToUp))));
	//private double rodUp[] 
	
	
	private double ferruleFibreSpacing = 0.001;
	
	/* Adjustment to cope with other changes... */
	//private double ferruleAdjustUp = 0.000;
	//private double ferruleAdjustRight = 0.000;
	//private double ferruleAdjustFocus = 0.000;
	
	// //adjusted because mirror at 0.72deg instead of 3 
	//private double ferruleAdjustUp = 0.0075; 
	//private double ferruleAdjustRight = -0.0025;
	//private double ferruleAdjustFocus = 0.000;
	
	// adjusted focus for 30mm l1-l2 distance instead of nominal 40mm
	//good for ang=0.72
	//private double ferruleAdjustUp = 0.0081; 
	//private double ferruleAdjustRight = 0.001;
	//private double ferruleAdjustFocus = 0.014;
	

	// adjusted focus for 30mm l1-l2 distance instead of nominal 40mm
	//good for ang=3.0
	private double ferruleAdjustUp = 0.001; 
	private double ferruleAdjustRight = 0.001;
	private double ferruleAdjustFocus = 0.014;
	
	private void setupFibrePositions() {
		int nBeams = ferruleRowNFibres.length;
		channelR = new double[nBeams][];
		lightPathRowName = new String[]{ "S7", "S8", "X1", "X2" };
		beamIdx = new int[] { W7xNBI.BEAM_Q7, W7xNBI.BEAM_Q8 , W7xNBI.BEAM_Q7, W7xNBI.BEAM_Q7 };
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		for(int iB=0; iB < 2; iB++){
			int nFibres = ferruleRowNFibres[iB];
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			channelR[iB] = new double[nFibres];
		
			
			//find row origin (green/blue dots in diagram)
			double origin[] = rodEndPos.clone();
			origin = Util.plus(origin, Util.mul(ferruleUp, ferruleRowUpwardsOffset[iB] + ferruleAdjustUp));
			origin = Util.plus(origin, Util.mul(ferruleRight, ferruleRowSidewaysOffset[iB] + ferruleAdjustRight));			
			origin = Util.plus(origin, Util.mul(rodAxis, -ferruleCurvatureCenterToRod[iB] + ferruleAdjustFocus));
			
			double rowAxis[] = Util.reNorm(Util.plus(Util.mul(ferruleUp, FastMath.sin(ferruleRowAngle[iB])), Util.mul(ferruleRight, FastMath.cos(ferruleRowAngle[iB]))));
											
			double dX = ferruleFibreSpacing;
			double x0 = -(nFibres-1.0)/2 * dX; 
			for(int iF=0; iF < nFibres; iF++){
				double distFromRowOrigin = x0 + iF * dX;
				
				channelR[iB][iF] = 5.4 + iF/100;
				fibreEndPos[iB][iF] = Util.plus(origin, Util.mul(rowAxis, distFromRowOrigin));
				
				double distFromCurvOrigin = distFromRowOrigin - ferruleCurvatureSidewaysOffset[iB];
				double depthFromCurvCentre = FastMath.sqrt(FastMath.pow2(ferruleRowCurvatureRadius[iB]) - distFromCurvOrigin*distFromCurvOrigin);
				
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(rodAxis, depthFromCurvCentre));
						
				fibreEndNorm[iB][iF] = Util.mul(fibrePlane.getNormal(), -1.0);
			}
			if(fibreFocus != null){
				for(int iF=0; iF < nFibres; iF++){
					fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibrePlane.getNormal(), fibreFocus[iB][iF]));
				}	
			}
		}
		
		for(int iX=0; iX < 2; iX++){
			int nFibres = ferruleRowNFibres[2+iX];
			fibreEndPos[2+iX] = new double[nFibres][];
			fibreEndNorm[2+iX] = new double[nFibres][];
			channelR[2+iX] = new double[nFibres];
		
			int iXR1 = ferruleCrossFibreCrossSelect[iX][0], iXR2 = ferruleCrossFibreCrossSelect[iX][1];
			double columnOrigin[] = Util.mul(Util.plus(fibreEndPos[1][iXR2], fibreEndPos[0][iXR1]), 0.5);
			double columnVector[] = Util.reNorm(Util.minus(fibreEndPos[1][iXR2], fibreEndPos[0][iXR1]));
			
			for(int iF=0; iF < nFibres; iF++){
				channelR[2+iX][iF] = 5.4 + iF/100;
				
				double distFromColOrigin = ferruleCrossFibreRelDists[iX][iF] * ferruleCrossFibreSpacing[iX];
				
				fibreEndPos[2+iX][iF] = Util.plus(columnOrigin, Util.mul(columnVector, distFromColOrigin));	

				fibreEndNorm[2+iX][iF] = Util.mul(fibrePlane.getNormal(), -1.0);
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
	
	private void adjustFibres(){
		double tiltAng = 0 * Math.PI / 180;
		double tiltAng2 = 0 * Math.PI / 180;
		//double shift = 0.015; //for lens2DistBehindLens1 = 60mm
		double shift = 0.000; //for lens2DistBehindLens1 = 40mm
		//double shift = -0.014; //for lens2DistBehindLens1 = 20mm
		double shiftR = 0.000, shiftU = 0.000; //for tolerance testing
		
		double r[] = Util.reNorm(Util.minus(fibreEndPos[0][fibreEndPos[0].length-1], fibreEndPos[0][0]));
		double u[] = Util.reNorm(Util.cross(r, portNormal));
		r = Util.reNorm(Util.cross(portNormal, u));
		
		for(int iB=0; iB < channelR.length; iB++){
			for(int iF=0; iF < channelR[iB].length; iF++){
				double a[] = OneLiners.rotateVectorAroundAxis(tiltAng, mirrorPivotVector, fibreEndNorm[iB][iF]);
				double bVec[] = Util.reNorm(Util.cross(a, mirrorPivotVector));				
				a = OneLiners.rotateVectorAroundAxis(tiltAng2, bVec, a);
				fibreEndNorm[iB][iF] = a;
				
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(portNormal, shift)); 
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(r, shiftR)); 
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(u, shiftU)); 
				
			}
		}
	}
	
	public BeamEmissSpecAEM21_postDesign_LC3(boolean adjustForLC3) {
		super("beamSpec-aem21");
		this.adjustedToLC3 = adjustForLC3;
		
		for(Surface s : blockPlate.getSurfaces())
			s.setInterface(Reflector.ideal());

		for(Element e : new Element[]{ mirrorBlock, mirrorClampRing, blockPlate }){
			e.rotate(mirrorCentrePos, Algorithms.rotationMatrix(portNormal, mirrorRingRotate));
			e.rotate(mirrorPivotCentre, Algorithms.rotationMatrix(mirrorPivotVector, mirrorAngleAdjust));
		}
				
		addElement(panelEdge);
		addElement(mirrorBlock);
		addElement(mirrorClampRing);
		//addElement(blockPlate);
		addElement(colar);
				
		addElement(mirror);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lensIris);
		addElement(lens1);
		addElement(lens2Iris);
		addElement(lens2);
		//addElement(objLens);
		//addElement(objLensIris);
		//addElement(lensPVIris);
		//addElement(lensPV);
		addElement(fibrePlane);
		//addElement(shieldTiles);
		addElement(catchPlane);
		
		addElement(strayPlane);
		addElement(rod);
		
		setupFibrePositions();
		adjustFibres();
		//setupFibresForMaciej();
		setupFibrePlanes();
		
		channelZ = new double[channelR.length][];
		for(int iB=0; iB < channelR.length; iB++){
			channelZ[iB] = new double[channelR[iB].length];
			for(int iF=0; iF < channelR[iB].length; iF++){
				channelZ[iB][iF] = W7xNBI.def().getPosOfBoxAxisAtR(0, channelR[iB][iF])[2];
			}
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
		if(adjustForLC3){
			rotate(new double[3], rotateLC3);
			shift(offsetLC3);
			
			
			for(int iB=0;iB<fibreEndPos.length;iB++){
				for(int iF=0; iF < fibreEndPos[iB].length; iF++){
				
					double newVec1[] = new double[3], newVec2[] = new double[3];
					for(int j=0; j < 3; j++){
						for(int k=0; k < 3; k++){
							newVec1[j] += rotateLC3[j][k] * fibreEndNorm[iB][iF][k];
							newVec2[j] += rotateLC3[j][k] * fibreEndPos[iB][iF][k];
						}
						
						newVec2[j] += offsetLC3[j];
					}
					fibreEndNorm[iB][iF] = newVec1;
					fibreEndPos[iB][iF] = newVec2;
				}			
			}
		}
		double portNormFinal[] = lensIris.getNormal(); //maybe adjuested to LC3
		System.out.println("Mirror deflection angle: " + 2*Math.acos(Util.dot(mirror.getNormal(), portNormFinal))*180/Math.PI + " deg");
		
		
	}

	public String getDesignName() { return "aem21" + (adjustedToLC3 ? "-lc3" : "");	}

	public List<Element> makeSimpleModel() {
		ArrayList<Element> elements = new ArrayList<Element>();
		
		elements.add(mirror);
		elements.add(entryWindowFront);	
		elements.add(entryWindowBack);		
		elements.add(lens1);
		elements.add(lens2);
		
		return elements;
	}

	
	/** Remove tube components for alignment of carriage */
	public void carriageOnly() {
		//removing tube stuff for in-lab alignment 
		removeElement(mirror);
		removeElement(catchPlane);
		removeElement(entryWindowIris);
		removeElement(colar);
		removeElement(blockPlate);
		removeElement(panelEdge);
		removeElement(strayPlane);
		removeElement(mirrorBlock);
		removeElement(mirrorClampRing);
		
		beamPlane.setCentre(Util.plus(lens1CentrePos, Util.mul(opticAxis , -1.500)));
		beamPlane.setNormal(opticAxis.clone());
		
		double upFinal[] = Util.reNorm(Util.cross(mirror.getNormal(), lensIris.getNormal())); //maybe adjuested to LC3 or other things
		beamPlane.setUp(upFinal);
		
		//outPath += "/carriageOnly/";	

	}
	
	
	/*//maybe not
	private void setupFibresForMaciej() {

		double p1[] = { -0.43243243408203125, 5.33058251953125, 1.35901611328125 };
		double p2[] = { -0.4332908020019531, 5.33930859375, 1.3619034423828125 };
		double u[] = Util.reNorm(Util.minus(p2, p1));
		double l = Util.length(Util.minus(p2, p1));
		double x[] = OneLiners.linSpace(0.0, 1.0, 5);
		
		for(int i=0; i)

	}
	*/	
	
}
