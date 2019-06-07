package ipp.w7x.fusionOptics.w7x.cxrs.aem21;

import java.util.ArrayList;
import java.util.List;

import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
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
	
	private boolean adjustedToLC3 = true;
		
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
	
	//public double mirrorAngleAdjust = +3 * Math.PI / 180; // Adjust of shutter open angle. 0 is default open, -60 is closed, +3deg gives best throughput and direct light mitigation
	public double mirrorAngleAdjust = +0.72 * Math.PI / 180; // Furthest we could get due to shutter housing 
	
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
	
	public STLMesh panelEdge = new STLMesh("panel", "/home/oliford/rzg/w7x/cad/aem21/panel-cutting-edge-channels-cut.stl");
	public STLMesh mirrorBlock = new STLMesh("mirrorBlock", "/home/oliford/rzg/w7x/cad/aem21/mirrorBlockSimpleOpen.stl");	
	public STLMesh mirrorClampRing = new STLMesh("mirrorClampRing", "/home/oliford/rzg/w7x/cad/aem21/mirrorRing-simple.stl");	
	public STLMesh blockPlate = new STLMesh("blockPlate", "/home/oliford/rzg/w7x/cad/aem21/blockPlate-grooved.stl");
	

	
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
	
	/*** Fibres ****/
	public int beamIdx[] = null;
	public String lightPathRowName[] = null;
	public double[][] channelR = null;	
	public double[][][] fibreEndPos = null;
	public double[][][] fibreEndNorm = null;
	//*/
	/*public int beamIdx[] = { W7xNBI.BEAM_Q7, W7xNBI.BEAM_Q8, W7xNBI.BEAM_Q7, W7xNBI.BEAM_Q8 };
	public double[][] channelR = { 
			{ 5.450, 5.461, 5.473, 5.484, 5.495, 5.507, 5.518, 5.529, 5.541, 5.552, 5.563, 5.575, 5.586, 5.597, 5.608, 5.620, 5.631, 5.642, 5.654, 5.665, 5.676, 5.688, 5.699, 5.710, 5.722, 5.733, 5.744, 5.756, 5.767, 5.778, 5.790, 5.801, 5.812, 5.824, 5.835, 5.846, 5.858, 5.869, 5.880, 5.892, 5.903, 5.914, 5.925, 5.937, 5.948, 5.959, 5.971, 5.982, 5.993, 6.005, 6.016, 6.027, 6.039, 6.050,  },
			{ 5.450, 5.461, 5.473, 5.484, 5.495, 5.507, 5.518, 5.529, 5.541, 5.552, 5.563, 5.575, 5.586, 5.597, 5.608, 5.620, 5.631, 5.642, 5.654, 5.665, 5.676, 5.688, 5.699, 5.710, 5.722, 5.733, 5.744, 5.756, 5.767, 5.778, 5.790, 5.801, 5.812, 5.824, 5.835, 5.846, 5.858, 5.869, 5.880, 5.892, 5.903, 5.914, 5.925, 5.937, 5.948, 5.959, 5.971, 5.982, 5.993, 6.005, 6.016, 6.027, 6.039, 6.050,  }, 
			{ 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58,  },
			{ 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58,  },
		}; 
		public double[][][] fibreEndPos = { { 
			{ -0.3888854, 5.3478836, 1.3357688 },
			{ -0.3891366, 5.3487116, 1.3363914 },
			{ -0.3893931, 5.3495418, 1.3370001 },
			{ -0.3896548, 5.3503741, 1.3375950 },
			{ -0.3899217, 5.3512086, 1.3381761 },
			{ -0.3901939, 5.3520452, 1.3387433 },
			{ -0.3904713, 5.3528840, 1.3392967 },
			{ -0.3907539, 5.3537250, 1.3398363 },
			{ -0.3910418, 5.3545680, 1.3403620 },
			{ -0.3913349, 5.3554133, 1.3408738 },
			{ -0.3916332, 5.3562607, 1.3413718 },
			{ -0.3919368, 5.3571102, 1.3418560 },
			{ -0.3922456, 5.3579619, 1.3423263 },
			{ -0.3925596, 5.3588158, 1.3427828 },
			{ -0.3928789, 5.3596718, 1.3432255 },
			{ -0.3932034, 5.3605299, 1.3436543 },
			{ -0.3935331, 5.3613902, 1.3440692 },
			{ -0.3938681, 5.3622527, 1.3444703 },
			{ -0.3942083, 5.3631173, 1.3448576 },
			{ -0.3945537, 5.3639840, 1.3452310 },
			{ -0.3949043, 5.3648529, 1.3455906 },
			{ -0.3952602, 5.3657240, 1.3459364 },
			{ -0.3956214, 5.3665972, 1.3462683 },
			{ -0.3959877, 5.3674725, 1.3465863 },
			{ -0.3963593, 5.3683500, 1.3468905 },
			{ -0.3967361, 5.3692297, 1.3471809 },
			{ -0.3971182, 5.3701115, 1.3474574 },
			{ -0.3975055, 5.3709955, 1.3477201 },
			{ -0.3978980, 5.3718816, 1.3479689 },
			{ -0.3982958, 5.3727698, 1.3482039 },
			{ -0.3986988, 5.3736603, 1.3484251 },
			{ -0.3991070, 5.3745528, 1.3486324 },
			{ -0.3995204, 5.3754475, 1.3488258 },
			{ -0.3999391, 5.3763444, 1.3490055 },
			{ -0.4003630, 5.3772434, 1.3491712 },
			{ -0.4007922, 5.3781446, 1.3493232 },
			{ -0.4012266, 5.3790479, 1.3494613 },
			{ -0.4016662, 5.3799534, 1.3495855 },
			{ -0.4021110, 5.3808610, 1.3496959 },
			{ -0.4025611, 5.3817708, 1.3497925 },
			{ -0.4030164, 5.3826827, 1.3498752 },
			{ -0.4034770, 5.3835968, 1.3499441 },
			{ -0.4039428, 5.3845130, 1.3499991 },
			{ -0.4044138, 5.3854314, 1.3500403 },
			{ -0.4048900, 5.3863519, 1.3500676 },
			{ -0.4053715, 5.3872746, 1.3500811 },
			{ -0.4058582, 5.3881994, 1.3500808 },
			{ -0.4063502, 5.3891264, 1.3500666 },
			{ -0.4068473, 5.3900555, 1.3500386 },
			{ -0.4073497, 5.3909868, 1.3499967 },
			{ -0.4078574, 5.3919203, 1.3499410 },
			{ -0.4083703, 5.3928558, 1.3498715 },
			{ -0.4088884, 5.3937936, 1.3497881 },
			{ -0.4094117, 5.3947335, 1.3496908 },
				}, { 
					{ -0.3937020, 5.3448192, 1.3377327 },
					{ -0.3940543, 5.3455508, 1.3384750 },
					{ -0.3944128, 5.3462851, 1.3392006 },
					{ -0.3947777, 5.3470219, 1.3399094 },
					{ -0.3951489, 5.3477613, 1.3406016 },
					{ -0.3955263, 5.3485033, 1.3412771 },
					{ -0.3959101, 5.3492479, 1.3419360 },
					{ -0.3963002, 5.3499951, 1.3425781 },
					{ -0.3966966, 5.3507449, 1.3432036 },
					{ -0.3970993, 5.3514973, 1.3438124 },
					{ -0.3975083, 5.3522522, 1.3444045 },
					{ -0.3979236, 5.3530098, 1.3449799 },
					{ -0.3983452, 5.3537700, 1.3455387 },
					{ -0.3987731, 5.3545327, 1.3460807 },
					{ -0.3992073, 5.3552980, 1.3466061 },
					{ -0.3996479, 5.3560660, 1.3471148 },
					{ -0.4000947, 5.3568365, 1.3476068 },
					{ -0.4005478, 5.3576096, 1.3480821 },
					{ -0.4010073, 5.3583853, 1.3485408 },
					{ -0.4014730, 5.3591636, 1.3489827 },
					{ -0.4019451, 5.3599445, 1.3494080 },
					{ -0.4024235, 5.3607280, 1.3498166 },
					{ -0.4029081, 5.3615141, 1.3502085 },
					{ -0.4033991, 5.3623027, 1.3505838 },
					{ -0.4038964, 5.3630940, 1.3509423 },
					{ -0.4044000, 5.3638878, 1.3512842 },
					{ -0.4049099, 5.3646843, 1.3516094 },
					{ -0.4054261, 5.3654833, 1.3519179 },
					{ -0.4059486, 5.3662850, 1.3522097 },
					{ -0.4064774, 5.3670892, 1.3524849 },
					{ -0.4070125, 5.3678960, 1.3527433 },
					{ -0.4075539, 5.3687054, 1.3529851 },
					{ -0.4081017, 5.3695174, 1.3532102 },
					{ -0.4086557, 5.3703320, 1.3534186 },
					{ -0.4092160, 5.3711492, 1.3536103 },
					{ -0.4097827, 5.3719689, 1.3537854 },
					{ -0.4103556, 5.3727913, 1.3539437 },
					{ -0.4109349, 5.3736163, 1.3540854 },
					{ -0.4115205, 5.3744438, 1.3542104 },
					{ -0.4121123, 5.3752740, 1.3543188 },
					{ -0.4127105, 5.3761067, 1.3544104 },
					{ -0.4133150, 5.3769420, 1.3544853 },
					{ -0.4139258, 5.3777800, 1.3545436 },
					{ -0.4145429, 5.3786205, 1.3545852 },
					{ -0.4151663, 5.3794636, 1.3546101 },
					{ -0.4157960, 5.3803093, 1.3546183 },
					{ -0.4164320, 5.3811576, 1.3546099 },
					{ -0.4170743, 5.3820084, 1.3545847 },
					{ -0.4177230, 5.3828619, 1.3545429 },
					{ -0.4183779, 5.3837180, 1.3544844 },
					{ -0.4190391, 5.3845766, 1.3544092 },
					{ -0.4197067, 5.3854379, 1.3543174 },
					{ -0.4203805, 5.3863017, 1.3542088 },
					{ -0.4210607, 5.3871682, 1.3540836 },
				},{
					{ -0.3905556, 5.3837697, 1.3459959 },
					{ -0.3940217, 5.3820913, 1.3470453 },
					{ -0.3974878, 5.3804129, 1.3480947 },
					{ -0.4040656, 5.3769105, 1.3511308 },
					{ -0.4075317, 5.3752321, 1.3521802 },
					{ -0.4141095, 5.3717296, 1.3552164 },
					{ -0.4175756, 5.3700513, 1.3562658 },
					{ -0.4210417, 5.3683729, 1.3573152 },
				},{
					{ -0.3874850, 5.3604418, 1.3406331 },
					{ -0.3888852, 5.3591492, 1.3409616 },
					{ -0.3903577, 5.3578863, 1.3410987 },
					{ -0.3929411, 5.3552120, 1.3423295 },
					{ -0.3943413, 5.3539194, 1.3426579 },
					{ -0.3957414, 5.3526269, 1.3429863 },
					{ -0.3983972, 5.3499823, 1.3440258 },
					{ -0.3997973, 5.3486897, 1.3443542 },
					{ -0.4011975, 5.3473971, 1.3446826 },					
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
			{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
				}, { 
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
				},{
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
				},{
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
					{ -0.3499074607007624, 0.14383845361539127, -0.9256755739499004 },
				}
			};
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
				
	
	
	private double rodAxis[] = opticAxis;
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
	private double ferruleAdjustUp = 0.0081; 
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
	
	

}
