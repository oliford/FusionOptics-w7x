package ipp.w7x.fusionOptics.w7x.cxrs.aem21;

import java.util.ArrayList;
import java.util.List;

import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import seed.matrix.DenseMatrix;
import algorithmrepository.Algorithms;
import algorithmrepository.exceptions.NotImplementedException;
import jafama.FastMath;
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
public class BeamEmissSpecAEM21_postDesign_obsolete extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ e_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public String lightPathsSystemName = "AEM21_???";
	
	// CAD from designer
	
	
	public double portNormal[] = { 0.35536503, -0.14530594,  0.92336444 };
	
	public double virtualObsPos[] = { -0.5830873174901741,	5.362082222103293,	1.1250303063301719 }; //closest approach of all LOSs, from lightAssesment
									  
	public double windowCentre[] = { -0.47792361,  5.38480054,  1.17044202 };
	
	/***** Mirror/Shutter *****/
	
	public double mirrorDiameter = 0.120;
	
	public double mirrorAngleAdjust = +3 * Math.PI / 180; // Adjust of shutter open angle. 0 is default open, -60 is closed, +3deg gives best throughput and direct light mitigation
	
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
	
	public STLMesh panelEdge = new STLMesh("panel", "/work/ipp/w7x/cad/aem21/panel-cutting-edge-channels-cut.stl");
	public STLMesh mirrorBlock = new STLMesh("mirrorBlock", "/work/ipp/w7x/cad/aem21/mirrorBlockSimpleOpen.stl");	
	public STLMesh mirrorClampRing = new STLMesh("mirrorClampRing", "/work/ipp/w7x/cad/aem21/mirrorRing-simple.stl");	
	public STLMesh blockPlate = new STLMesh("blockPlate", "/work/ipp/w7x/cad/aem21/blockPlate-grooved.stl");
	

	
	public double opticAxis[] = portNormal;
	
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

	public double lensCentrePos[] = Util.plus(windowCentre, Util.mul(opticAxis, lens1DistBehindWindow + lens1CentreThickness));
	
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
											lensCentrePos,
											opticAxis,
											lens1Diameter/2, // radius
											lens1CurvatureRadius, // rad curv
//											lens1FocalLength, // rad curv
											lens1CentreThickness,  
											lensMedium, 
											IsoIsoInterface.ideal());//,
//											designWavelenth);
	
	public double lensIrisPos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, -0.005));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lens1Diameter, lens1ClearAperture/2, null, null, Absorber.ideal());
	
	/**** Lens2 *****/
	public double lens2DistBehindLens1 = 0.040;
	
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
	
	public double lens2CentrePos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, lens2DistBehindLens1 + lens2CentreThickness));
	
	
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

	/**** Petzval *****/
	public double lensPVDistBehindLens1 = 0.110;
	public double lensPVDiameter = 0.100;
	
	public double lensPVCentrePos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, lensPVDistBehindLens1));
	
	public double focalLengthPV = -0.200; 
	
	public Medium lensPVMedium = new Medium(new BK7());  
	public SimplePlanarConvexLens lensPV = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lensPV",
											lensPVCentrePos,
											opticAxis,
											lensPVDiameter/2, // radius
											focalLengthPV, // focal length
											0.005, // centreThickness [ fromDesigner CAD for glass - although it's curvature doesn't match Jurgen's eBANF's focal length] 
											lensPVMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lensPVIrisPos[] = Util.plus(lensPVCentrePos, Util.mul(opticAxis, -0.005));
	public Iris lensPVIris = new Iris("lensPVIris", lensPVIrisPos, opticAxis, lensPVDiameter*2, lensPVDiameter*0.48, null, null, Absorber.ideal());
 	

	/*** Fibres ****/
	//public double[][] channelR = null;
	//public double[][][] fibreEndPos = null;
	//public double[][][] fibreEndNorm = null;
	
	public int beamIdx[] = { W7xNBI.BEAM_Q7, W7xNBI.BEAM_Q8, W7xNBI.BEAM_Q7, W7xNBI.BEAM_Q8 };
	
	public double[][] channelR = { 
			{ 5.45, 5.462, 5.474, 5.486, 5.498, 5.51, 5.522, 5.534, 5.546, 5.558, 5.57, 5.582, 5.594, 5.606, 5.618, 5.63, 5.642, 5.654, 5.666, 5.678, 5.69, 5.702, 5.714, 5.726, 5.738, 5.75, 5.762, 5.774, 5.786, 5.798, 5.81, 5.822, 5.834, 5.846, 5.858, 5.87, 5.882, 5.894, 5.906, 5.918, 5.93, 5.942, 5.954, 5.966, 5.978, 5.99, 6.002, 6.014, 6.026, 6.038, }, 
			{ 5.45, 5.462, 5.474, 5.486, 5.498, 5.51, 5.522, 5.534, 5.546, 5.558, 5.57, 5.582, 5.594, 5.606, 5.618, 5.63, 5.642, 5.654, 5.666, 5.678, 5.69, 5.702, 5.714, 5.726, 5.738, 5.75, 5.762, 5.774, 5.786, 5.798, 5.81, 5.822, 5.834, 5.846, 5.858, 5.87, 5.882, 5.894, 5.906, 5.918, 5.93, 5.942, 5.954, 5.966, 5.978, 5.99, 6.002, 6.014, 6.026, 6.038, },
			{ 5.0, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9 },//cross fibres, dumyy positions
			{ 5.0, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9 },//cross fibres, dumyy positions		
		}; 
		public double[][][] fibreEndPos = { {		
			/* // Ideal focus
				{ -0.4081654,  5.3852352,  1.3590665,  },
				{ -0.40762228,  5.3843297,  1.3590861,  },
				{ -0.40708335,  5.3834201,  1.3590937,  },
				{ -0.40654877,  5.3825067,  1.3590893,  },
				{ -0.40601873,  5.3815901,  1.3590729,  },
				{ -0.40549343,  5.3806704,  1.3590447,  },
				{ -0.40497303,  5.3797483,  1.3590046,  },
				{ -0.40445774,   5.378824,  1.3589527,  },
				{ -0.40394772,  5.3778979,  1.3588889,  },
				{ -0.40344316,  5.3769705,  1.3588134,  },
				{ -0.40294425,  5.3760421,  1.3587262,  },
				{ -0.40245117,  5.3751132,  1.3586272,  },
				{ -0.40196411,  5.3741841,  1.3585166,  },
				{ -0.40148325,  5.3732553,  1.3583943,  },
				{ -0.40100877,  5.3723271,  1.3582604,  },
				{ -0.40054086,  5.3713999,   1.358115,  },
				{ -0.4000797,  5.3704741,   1.357958,  },
				{ -0.39962547,  5.3695502,  1.3577895,  },
				{ -0.39917836,  5.3686285,  1.3576095,  },
				{ -0.39873856,  5.3677094,   1.357418,  },
				{ -0.39830624,  5.3667933,  1.3572151,  },
				{ -0.3978816,  5.3658807,  1.3570009,  },
				{ -0.39746481,  5.3649719,  1.3567753,  },
				{ -0.39705605,  5.3640672,  1.3565384,  },
				{ -0.39665553,  5.3631672,  1.3562902,  },
				{ -0.3962634,  5.3622722,  1.3560307,  },
				{ -0.39587987,  5.3613826,    1.35576,  },
				{ -0.39550512,  5.3604987,  1.3554782,  },
				{ -0.39513932,  5.3596211,  1.3551851,  },
				{ -0.39478266,  5.3587501,   1.354881,  },
				{ -0.39443533,   5.357886,  1.3545657,  },
				{ -0.39409751,  5.3570294,  1.3542394,  },
				{ -0.39376939,  5.3561805,  1.3539021,  },
				{ -0.39345115,  5.3553398,  1.3535538,  },
				{ -0.39314296,  5.3545077,  1.3531945,  },
				{ -0.39284503,  5.3536845,  1.3528243,  },
				{ -0.39255752,  5.3528707,  1.3524432,  },
				{ -0.39228063,  5.3520667,  1.3520512,  },
				{ -0.39201454,  5.3512729,  1.3516483,  },
				{ -0.39175943,  5.3504896,  1.3512347,  },
				{ -0.39151548,  5.3497173,  1.3508103,  },
				{ -0.39128289,  5.3489563,  1.3503752,  },
				{ -0.39106183,  5.3482071,  1.3499294,  },
				{ -0.39085248,  5.3474701,  1.3494729,  },
				{ -0.39065504,  5.3467456,  1.3490057,  },
				{ -0.39046969,   5.346034,   1.348528,  },
				{ -0.39029661,  5.3453358,  1.3480396,  },
				{ -0.39013598,  5.3446513,  1.3475408,  },
				{ -0.38998798,   5.343981,  1.3470314,  },
				{ -0.38985281,  5.3433251,  1.3465115,  },
			}, {
				{ -0.42056331,  5.3792516,  1.3630657,  },
				{ -0.41975972,  5.3783094,  1.3632403,  },
				{ -0.41896649,  5.3773654,  1.3633848,  },
				{ -0.41818365,  5.3764201,  1.3634997,  },
				{ -0.41741125,  5.3754739,  1.3635857,  },
				{ -0.41664934,  5.3745273,  1.3636435,  },
				{ -0.41589795,  5.3735806,  1.3636737,  },
				{ -0.41515714,  5.3726343,   1.363677,  },
				{ -0.41442693,  5.3716888,   1.363654,  },
				{ -0.41370738,  5.3707445,  1.3636053,  },
				{ -0.41299854,  5.3698019,  1.3635316,  },
				{ -0.41230043,  5.3688614,  1.3634335,  },
				{ -0.41161311,  5.3679234,  1.3633118,  },
				{ -0.41093662,  5.3669884,   1.363167,  },
				{ -0.41027101,  5.3660567,  1.3629997,  },
				{ -0.4096163,  5.3651288,  1.3628107,  },
				{ -0.40897256,  5.3642051,  1.3626005,  },
				{ -0.40833982,  5.3632861,  1.3623699,  },
				{ -0.40771813,  5.3623721,  1.3621194,  },
				{ -0.40710753,  5.3614637,  1.3618497,  },
				{ -0.40650805,  5.3605611,  1.3615615,  },
				{ -0.40591976,  5.3596649,  1.3612553,  },
				{ -0.40534268,  5.3587755,  1.3609319,  },
				{ -0.40477686,  5.3578933,  1.3605919,  },
				{ -0.40422235,  5.3570187,  1.3602359,  },
				{ -0.40367919,  5.3561521,  1.3598646,  },
				{ -0.40314742,   5.355294,  1.3594786,  },
				{ -0.40262708,  5.3544449,  1.3590786,  },
				{ -0.40211823,   5.353605,  1.3586652,  },
				{ -0.40162089,  5.3527749,   1.358239,  },
				{ -0.40113512,   5.351955,  1.3578008,  },
				{ -0.40066096,  5.3511457,  1.3573511,  },
				{ -0.40019845,  5.3503475,  1.3568906,  },
				{ -0.39974763,  5.3495607,  1.3564199,  },
				{ -0.39930855,  5.3487857,  1.3559397,  },
				{ -0.39888125,  5.3480231,  1.3554507,  },
				{ -0.39846578,  5.3472732,  1.3549534,  },
				{ -0.39806217,  5.3465365,  1.3544485,  },
				{ -0.39767047,  5.3458134,  1.3539367,  },
				{ -0.39729073,  5.3451043,  1.3534186,  },
				{ -0.39692299,  5.3444096,  1.3528949,  },
				{ -0.39656728,  5.3437298,  1.3523662,  },
				{ -0.39622366,  5.3430653,  1.3518331,  },
				{ -0.39589217,  5.3424164,  1.3512964,  },
				{ -0.39557285,  5.3417838,  1.3507565,  },
				{ -0.39526574,  5.3411677,  1.3502143,  },
				{ -0.39497088,  5.3405686,  1.3496702,  },
				{ -0.39468833,  5.3399869,  1.3491251,  },
				{ -0.39441812,  5.3394231,  1.3485794,  },
				{ -0.3941603,  5.3388775,   1.348034,  },
			}, { */
			//As CAD (circular), (Q7 adjusted to try to improve edge spot size, but didn't make much difference
				{ -0.408251113,  5.385295263,  1.358883417 },
				{ -0.407684889,  5.384382583,  1.358968132 },
				{ -0.407121102,  5.383460954,  1.359036614 },
				{ -0.406565435,  5.382528617,  1.359090086 },
				{ -0.406017205,  5.381609416,  1.359124401 },
				{ -0.405467830,  5.380679803,  1.359141788 },
				{ -0.404930242,  5.379740990,  1.359144653 },
				{ -0.404396173,  5.378813707,  1.359128535 },
				{ -0.403870248,  5.377875733,  1.359097357 },
				{ -0.403360266,  5.376950288,  1.359050912 },
				{ -0.402849295,  5.376014503,  1.358987144 },
				{ -0.402350270,  5.375079598,  1.358908474 },
				{ -0.401854079,  5.374145933,  1.358812578 },
				{ -0.401369702,  5.373213095,  1.358702119 },
				{ -0.400893602,  5.372279634,  1.358576279 },
				{ -0.400423667,  5.371358786,  1.358434579 },
				{ -0.399961917,  5.370427271,  1.358277728 },
				{ -0.399502748,  5.369506899,  1.358104326 },
				{ -0.399063959,  5.368586769,  1.357920088 },
				{ -0.398627708,  5.367667758,  1.357719396 },
				{ -0.398194015,  5.366749876,  1.357502201 },
				{ -0.397776872,  5.365840670,  1.357274124 },
				{ -0.397365511,  5.364933910,  1.357031169 },
				{ -0.396962069,  5.364026381,  1.356773760 },
				{ -0.396569388,  5.363129247,  1.356504520 },
				{ -0.396187297,  5.362242434,  1.356223889 },
				{ -0.395816203,  5.361356108,  1.355930807 },
				{ -0.395446778,  5.360480543,  1.355623524 },
				{ -0.395096940,  5.359604887,  1.355307448 },
				{ -0.394745386,  5.358738605,  1.354975966 },
				{ -0.394407872,  5.357874051,  1.354634117 },
				{ -0.394076358,  5.357027930,  1.354282784 },
				{ -0.393767256,  5.356182865,  1.353925303 },
				{ -0.393456111,  5.355347036,  1.353553257 },
				{ -0.393155539,  5.354511513,  1.353169846 },
				{ -0.392863553,  5.353695469,  1.352780212 },
				{ -0.392580933,  5.352889228,  1.352382331 },
				{ -0.392314045,  5.352081308,  1.351975656 },
				{ -0.392049836,  5.351294534,  1.351562108 },
				{ -0.391803085,  5.350516771,  1.351145255 },
				{ -0.391554942,  5.349738508,  1.350712141 },
				{ -0.391325870,  5.348979900,  1.350281500 },
				{ -0.391102411,  5.348229551,  1.349842346 },
				{ -0.390889146,  5.347499311,  1.349402746 },
				{ -0.390682635,  5.346767804,  1.348951681 },
				{ -0.390491344,  5.346054366,  1.348503082 },
				{ -0.390299856,  5.345350893,  1.348045069 },
				{ -0.390123166,  5.344665311,  1.347590606 },
				{ -0.389954304,  5.343988875,  1.347131848 },
				{ -0.389801425,  5.343320823,  1.346673571 },
			},{
				{ -0.420480939,  5.379246205,  1.363354982 },
				{ -0.419696916,  5.378310603,  1.363477427 },				
				{ -0.418914351,  5.377365659,  1.363576267 },				
				{ -0.418146179,  5.376428526,  1.363649962 },				
				{ -0.417388813,  5.375481798,  1.363701780 },				
				{ -0.416639086,  5.374534191,  1.363729976 },				
				{ -0.415887995,  5.373596117,  1.363731974 },				
				{ -0.415153231,  5.372646626,  1.363713753 },				
				{ -0.414429827,  5.371707794,  1.363672295 },				
				{ -0.413713271,  5.370757754,  1.363609248 },				
				{ -0.413013378,  5.369816455,  1.363525174 },				
				{ -0.412318403,  5.368877258,  1.363418579 },				
				{ -0.411626826,  5.367935450,  1.363289387 },				
				{ -0.410949242,  5.366995374,  1.363140098 },				
				{ -0.410287593,  5.366063738,  1.362971667 },				
				{ -0.409627005,  5.365142634,  1.362780749 },				
				{ -0.408985590,  5.364211282,  1.362572242 },				
				{ -0.408354062,  5.363289984,  1.362344308 },				
				{ -0.407723298,  5.362379094,  1.362094659 },				
				{ -0.407111275,  5.361467785,  1.361828552 },				
				{ -0.406508684,  5.360566339,  1.361544190 },				
				{ -0.405927900,  5.359665726,  1.361245400 },				
				{ -0.405347430,  5.358775332,  1.360926054 },				
				{ -0.404775979,  5.357894630,  1.360589514 },				
				{ -0.404222129,  5.357023036,  1.360239463 },				
				{ -0.403680801,  5.356152561,  1.359873106 },				
				{ -0.403139434,  5.355292158,  1.359487097 },				
				{ -0.402618461,  5.354441999,  1.359090301 },				
				{ -0.402117611,  5.353601971,  1.358683414 },				
				{ -0.401616339,  5.352771852,  1.358257854 },				
				{ -0.401126317,  5.351952323,  1.357819267 },				
				{ -0.400655883,  5.351142697,  1.357371954 },				
				{ -0.400196334,  5.350343505,  1.356912546 },				
				{ -0.399738106,  5.349564922,  1.356439900 },				
				{ -0.399308287,  5.348785753,  1.355961570 },				
				{ -0.398879423,  5.348027038,  1.355470937 },				
				{ -0.398463838,  5.347279722,  1.354971953 },				
				{ -0.398063636,  5.346540577,  1.354465128 },				
				{ -0.397675129,  5.345822170,  1.353954026 },				
				{ -0.397296319,  5.345113693,  1.353433865 },				
				{ -0.396928706,  5.344425743,  1.352910701 },				
				{ -0.396570433,  5.343747572,  1.352379388 },				
				{ -0.396232452,  5.343079555,  1.351847416 },				
				{ -0.395902022,  5.342430568,  1.351311896 },				
				{ -0.395581772,  5.341801679,  1.350775976 },				
				{ -0.395272976,  5.341183417,  1.350236370 },				
				{ -0.394977434,  5.340592397,  1.349704296 },				
				{ -0.394697307,  5.340003626,  1.349168242 },				
				{ -0.394413836,  5.339443496,  1.348630911 },				
				{ -0.394159621,  5.338893060,  1.348105501 },
			},{
			
			//*/
				
				// Cross fibres, col1
				{ -0.380688684, 5.359122372, 1.348891983 },
				{ -0.383679563, 5.357544515, 1.349794762 },
				{ -0.386670442, 5.355966659, 1.350697542 },
				{ -0.389661321, 5.354388803, 1.351600321 },
				{ -0.394587942, 5.351220820, 1.353647662 },
				{ -0.396841898, 5.349663990, 1.354270134 },
				{ -0.401551674, 5.346009218, 1.356157412 },
				{ -0.404286227, 5.343949663, 1.356885735 },
				{ -0.407020780, 5.341890108, 1.357614057 },
				{ -0.409755333, 5.339830553, 1.358342380 },
			},{
				// Cross fibres, col1			
				{ -0.390085429,  5.380425887,  1.354994626 },
				{ -0.393076307,  5.378848028,  1.355897405 },
				{ -0.396067185,  5.377270169,  1.356800183 },
				{ -0.399058063,  5.375692309,  1.357702962 },
				{ -0.404884136,  5.371974104,  1.360876282 },
				{ -0.408325851,  5.369979151,  1.361886934 },
				{ -0.414396541,  5.365839230,  1.364005033 },
				{ -0.417131103,  5.363779689,  1.364733361 },
				{ -0.419865664,  5.361720147,  1.365461690 },
				{ -0.422600225,  5.359660605,  1.366190018 },		
			//*/
			} };

		
		public double[][][] fibreEndNorm = { { 
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				}, { 
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				},{
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
				},{
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
					{ -0.35536503, 0.14530594, -0.92336444 },
				}	};
//*/



	
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

	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 1.500, 2.000, NullInterface.ideal());
	
	double strayPos[] = Util.plus(mirrorCentrePos, Util.mul(portNormal, -0.250)); 
	
	public Disc strayPlane = new Disc("strayPlane", strayPos, portNormal, 0.200, Absorber.ideal());

	public Element tracingTarget = mirror;
	public Surface checkSurface = mirror;
	
	public final String backgroundSTLFiles[] = {
			"/work/ipp/w7x/cad/aem21/bg-targetting/baffles-cut.stl",
			"/work/ipp/w7x/cad/aem21/bg-targetting/panel1.stl",
			"/work/ipp/w7x/cad/aem21/bg-targetting/panel2.stl",
			"/work/ipp/w7x/cad/aem21/bg-targetting/panel3.stl",
			"/work/ipp/w7x/cad/aem21/bg-targetting/panel4.stl",
			"/work/ipp/w7x/cad/aem21/bg-targetting/panel5.stl",
			"/work/ipp/w7x/cad/aem21/bg-targetting/shield-cut.stl",
			"/work/ipp/w7x/cad/aem21/bg-targetting/target-cut.stl",
	};
		
	/** Set fibre positions equal spacing in holder */
	private void setupFibrePositions() {
		int nBeams = channelR.length;
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			
			double dX = -fibreEndDiameter;
			double x0 = -(nFibres-1)/2 * dX; 
			for(int iF=0; iF < nFibres; iF++){
				fibreEndPos[iB][iF] = Util.plus(fibrePlanePos, Util.mul(fibresXVec, x0 + iF * dX));	
						
				fibreEndNorm[iB][iF] = fibrePlane.getNormal().clone();
			}
			if(fibreFocus != null){
				for(int iF=0; iF < nFibres; iF++){
					fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibrePlane.getNormal(), fibreFocus[iB][iF]));
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
	
	public BeamEmissSpecAEM21_postDesign_obsolete() {
		super("beamSpec-aem21");
		
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
		
		//setupFibrePositions();
		adjustFibres();
		setupFibrePlanes();
		
		channelZ = new double[channelR.length][];
		for(int iB=0; iB < channelR.length; iB++){
			channelZ[iB] = new double[channelR[iB].length];
			for(int iF=0; iF < channelR[iB].length; iF++){
				channelZ[iB][iF] = W7xNBI.def().getPosOfBoxAxisAtR(0, channelR[iB][iF])[2];
			}
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());		
	}

	public String getDesignName() { return "aem21";	}

	public List<Element> makeSimpleModel() {
		ArrayList<Element> elements = new ArrayList<Element>();
		
		elements.add(mirror);
		elements.add(entryWindowFront);	
		elements.add(entryWindowBack);		
		elements.add(lens1);
		elements.add(lens2);
		
		return elements;
	}
	
	

}
