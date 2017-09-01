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
public class BeamEmissSpecAEM21_postDesign_LC3 extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ e_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	// CAD from designer
		
	//public double rotateLC3[][] = {	{ 1.009,  0.044,  0.004}, {0.061,  1.588, -0.001}, { -0.077, -0.759,  0.996} };
	public double rotateLC3[][] = {	
		{ 0.9999829389, 0.0002761317, -0.0058605577 },
		{ -0.0002674291, 0.9999986685, 0.0016920161 },
		{ 0.0058604423, -0.0016915735, 0.9999811728 } };

	
    public double offsetLC3[] = { 0.0060746567, 0.0049538344, 0.0025080040 };
   	
	public double portNormal[] = { 0.35536503, -0.14530594,  0.92336444 };
	//should transform to 0.34990725,  -0.14383813, 0.9256757
	//{ -0.23743994,  0.94225652,  0.23616716 } port left/right, parallel to front flange and tube plate, in LC3
	//transformed window centre should be -0.4775350799560547, 5.39110546875, 1.1594251098632813
	
	public double virtualObsPos[] = { -0.5830873174901741,	5.362082222103293,	1.1250303063301719 }; //closest approach of all LOSs, from lightAssesment
									  
	public double windowCentre[] = { -0.47792361,  5.38480054,  1.17044202 };
	
	/***** Mirror/Shutter *****/
	
	public double mirrorDiameter = 0.120;
	
	public double mirrorAngleAdjust = +3 * Math.PI / 180; // Adjust of shutter open angle. 0 is default open, -60 is closed, +3deg gives best throughput and direct light mitigation
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
			{ 5.450, 5.461, 5.473, 5.484, 5.495, 5.507, 5.518, 5.529, 5.541, 5.552, 5.563, 5.575, 5.586, 5.597, 5.608, 5.620, 5.631, 5.642, 5.654, 5.665, 5.676, 5.688, 5.699, 5.710, 5.722, 5.733, 5.744, 5.756, 5.767, 5.778, 5.790, 5.801, 5.812, 5.824, 5.835, 5.846, 5.858, 5.869, 5.880, 5.892, 5.903, 5.914, 5.925, 5.937, 5.948, 5.959, 5.971, 5.982, 5.993, 6.005, 6.016, 6.027, 6.039, 6.050,  },
			{ 5.450, 5.461, 5.473, 5.484, 5.495, 5.507, 5.518, 5.529, 5.541, 5.552, 5.563, 5.575, 5.586, 5.597, 5.608, 5.620, 5.631, 5.642, 5.654, 5.665, 5.676, 5.688, 5.699, 5.710, 5.722, 5.733, 5.744, 5.756, 5.767, 5.778, 5.790, 5.801, 5.812, 5.824, 5.835, 5.846, 5.858, 5.869, 5.880, 5.892, 5.903, 5.914, 5.925, 5.937, 5.948, 5.959, 5.971, 5.982, 5.993, 6.005, 6.016, 6.027, 6.039, 6.050,  }, 
			{ 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58,  },
			{ 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58, 5.58,  },
		}; 
		public double[][][] fibreEndPos = { { 
			{ -0.3888870, 5.3478704, 1.3357398 },
			{ -0.3891372, 5.3486980, 1.3363652 },
			{ -0.3893926, 5.3495277, 1.3369767 },
			{ -0.3896533, 5.3503597, 1.3375742 },
			{ -0.3899193, 5.3511938, 1.3381577 },
			{ -0.3901906, 5.3520300, 1.3387273 },
			{ -0.3904672, 5.3528685, 1.3392829 },
			{ -0.3907490, 5.3537091, 1.3398246 },
			{ -0.3910362, 5.3545519, 1.3403523 },
			{ -0.3913286, 5.3553968, 1.3408660 },
			{ -0.3916262, 5.3562440, 1.3413657 },
			{ -0.3919292, 5.3570933, 1.3418515 },
			{ -0.3922374, 5.3579447, 1.3423233 },
			{ -0.3925509, 5.3587984, 1.3427811 },
			{ -0.3928697, 5.3596542, 1.3432250 },
			{ -0.3931938, 5.3605121, 1.3436549 },
			{ -0.3935232, 5.3613723, 1.3440709 },
			{ -0.3938578, 5.3622346, 1.3444728 },
			{ -0.3941977, 5.3630991, 1.3448608 },
			{ -0.3945429, 5.3639657, 1.3452349 },
			{ -0.3948934, 5.3648346, 1.3455950 },
			{ -0.3952492, 5.3657056, 1.3459411 },
			{ -0.3956102, 5.3665787, 1.3462732 },
			{ -0.3959765, 5.3674541, 1.3465914 },
			{ -0.3963481, 5.3683316, 1.3468956 },
			{ -0.3967250, 5.3692113, 1.3471858 },
			{ -0.3971071, 5.3700931, 1.3474621 },
			{ -0.3974946, 5.3709771, 1.3477244 },
			{ -0.3978873, 5.3718633, 1.3479727 },
			{ -0.3982853, 5.3727517, 1.3482071 },
			{ -0.3986885, 5.3736422, 1.3484275 },
			{ -0.3990971, 5.3745349, 1.3486339 },
			{ -0.3995109, 5.3754298, 1.3488264 },
			{ -0.3999300, 5.3763268, 1.3490049 },
			{ -0.4003544, 5.3772260, 1.3491694 },
			{ -0.4007841, 5.3781274, 1.3493200 },
			{ -0.4012190, 5.3790310, 1.3494566 },
			{ -0.4016593, 5.3799367, 1.3495792 },
			{ -0.4021048, 5.3808446, 1.3496879 },
			{ -0.4025556, 5.3817546, 1.3497826 },
			{ -0.4030116, 5.3826669, 1.3498633 },
			{ -0.4034730, 5.3835813, 1.3499301 },
			{ -0.4039396, 5.3844978, 1.3499829 },
			{ -0.4044115, 5.3854166, 1.3500217 },
			{ -0.4048887, 5.3863375, 1.3500466 },
			{ -0.4053711, 5.3872606, 1.3500575 },
			{ -0.4058589, 5.3881858, 1.3500544 },
			{ -0.4063519, 5.3891133, 1.3500374 },
			{ -0.4068502, 5.3900429, 1.3500064 },
			{ -0.4073538, 5.3909746, 1.3499614 },
			{ -0.4078627, 5.3919086, 1.3499025 },
			{ -0.4083768, 5.3928447, 1.3498295 },
			{ -0.4088962, 5.3937830, 1.3497427 },
			{ -0.4094209, 5.3947234, 1.3496418 },
				}, { 
					{ -0.3937115, 5.3448242, 1.3377110 },
					{ -0.3940627, 5.3455555, 1.3384559 },
					{ -0.3944203, 5.3462893, 1.3391841 },
					{ -0.3947843, 5.3470258, 1.3398954 },
					{ -0.3951546, 5.3477648, 1.3405899 },
					{ -0.3955312, 5.3485065, 1.3412675 },
					{ -0.3959142, 5.3492508, 1.3419284 },
					{ -0.3963036, 5.3499977, 1.3425724 },
					{ -0.3966993, 5.3507472, 1.3431996 },
					{ -0.3971014, 5.3514993, 1.3438101 },
					{ -0.3975099, 5.3522540, 1.3444036 },
					{ -0.3979246, 5.3530114, 1.3449804 },
					{ -0.3983458, 5.3537714, 1.3455404 },
					{ -0.3987733, 5.3545339, 1.3460835 },
					{ -0.3992072, 5.3552991, 1.3466098 },
					{ -0.3996474, 5.3560669, 1.3471193 },
					{ -0.4000940, 5.3568374, 1.3476120 },
					{ -0.4005469, 5.3576104, 1.3480879 },
					{ -0.4010062, 5.3583860, 1.3485470 },
					{ -0.4014718, 5.3591643, 1.3489892 },
					{ -0.4019439, 5.3599452, 1.3494146 },
					{ -0.4024222, 5.3607286, 1.3498232 },
					{ -0.4029069, 5.3615147, 1.3502150 },
					{ -0.4033980, 5.3623034, 1.3505900 },
					{ -0.4038954, 5.3630948, 1.3509482 },
					{ -0.4043992, 5.3638887, 1.3512895 },
					{ -0.4049094, 5.3646852, 1.3516140 },
					{ -0.4054259, 5.3654844, 1.3519217 },
					{ -0.4059487, 5.3662862, 1.3522126 },
					{ -0.4064779, 5.3670906, 1.3524867 },
					{ -0.4070135, 5.3678976, 1.3527440 },
					{ -0.4075554, 5.3687072, 1.3529844 },
					{ -0.4081037, 5.3695194, 1.3532081 },
					{ -0.4086584, 5.3703342, 1.3534149 },
					{ -0.4092194, 5.3711517, 1.3536049 },
					{ -0.4097867, 5.3719718, 1.3537780 },
					{ -0.4103604, 5.3727944, 1.3539344 },
					{ -0.4109405, 5.3736197, 1.3540739 },
					{ -0.4115269, 5.3744476, 1.3541967 },
					{ -0.4121197, 5.3752782, 1.3543026 },
					{ -0.4127188, 5.3761113, 1.3543917 },
					{ -0.4133243, 5.3769470, 1.3544640 },
					{ -0.4139362, 5.3777854, 1.3545194 },
					{ -0.4145544, 5.3786264, 1.3545581 },
					{ -0.4151790, 5.3794699, 1.3545799 },
					{ -0.4158099, 5.3803161, 1.3545849 },
					{ -0.4164472, 5.3811649, 1.3545731 },
					{ -0.4170908, 5.3820164, 1.3545445 },
					{ -0.4177408, 5.3828704, 1.3544991 },
					{ -0.4183971, 5.3837270, 1.3544368 },
					{ -0.4190598, 5.3845863, 1.3543578 },
					{ -0.4197289, 5.3854482, 1.3542619 },
					{ -0.4204043, 5.3863127, 1.3541492 },
					{ -0.4210861, 5.3871798, 1.3540197 },
				},{
					{ -0.3866973, 5.3655265, 1.3419784 },
					{ -0.3888628, 5.3638551, 1.3425372 },
					{ -0.3910283, 5.3621836, 1.3430961 },
					{ -0.3951177, 5.3587414, 1.3448530 },
					{ -0.3972833, 5.3570699, 1.3454118 },
					{ -0.4013727, 5.3536277, 1.3471687 },
					{ -0.4035382, 5.3519562, 1.3477275 },
					{ -0.4057037, 5.3502848, 1.3482864 },
				},{
					{ -0.3952751, 5.3873961, 1.3476738 },
					{ -0.3978539, 5.3858197, 1.3484036 },
					{ -0.4005848, 5.3843058, 1.3487312 },
					{ -0.4052864, 5.3809655, 1.3513977 },
					{ -0.4078652, 5.3793891, 1.3521275 },
					{ -0.4104441, 5.3778127, 1.3528574 },
					{ -0.4152977, 5.3745349, 1.3551216 },
					{ -0.4178766, 5.3729585, 1.3558514 },
					{ -0.4204554, 5.3713821, 1.3565813 },
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
	
	public BeamEmissSpecAEM21_postDesign_LC3() {
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
				//fibreEndPos[iB][iF] = newVec2;
			}			
		}
	}

	public String getDesignName() { return "aem21-lc3";	}

	public List<Element> makeSimpleModel() {
		return new ArrayList<Element>();
	}
	
	

}
