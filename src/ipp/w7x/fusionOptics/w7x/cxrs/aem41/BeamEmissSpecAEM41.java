package ipp.w7x.fusionOptics.w7x.cxrs.aem41;

import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import net.jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.materials.BK7;
import fusionOptics.materials.IsotropicFixedIndexGlass;
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

/** Passive Spectroscopy on AEM41, in principle looking at RuDIX beam */
public class BeamEmissSpecAEM41 extends Optic {
	
	public String lightPathsSystemName = "AEM41";	
	
	public String lightPathRowName[] = { "A", "B" };
	
	public Element strayPlane = null;
	
	
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ e_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public double portNormal[] = { -0.20192623, 0.32638819, 0.92341569 };  // [fromDesigner-20151106] lens back plane normal, matches rod axis, pointing out of machine
	//public double entryWindowFrontPos[] = { -2.821, -4.604, 1.218 }; // [ Jürgen's sim 'Mittelpunkt des Fensters im AEM41' ]
	public double entryWindowFrontPos[] = {-2.8316212158203125, -4.585900146484375, 1.2615272827148438 }; // CAD
	
	//public double entryWindowDiameter = 0.080; // [Jurgen's Frascati poster + talking to Jurgen + plm/CAD ]
	public double entryWindowDiameter = 0.096; // CAD: Window glass is d=96mm, aperture = 80mm 
	public double entryWindowThickness = 0.012; // CAD
		
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, entryWindowThickness));
	
	public double frontPlateAperturePos[] = { -2.8227093505859377, -4.6002958984375, 1.2208069458007813 };
	public double frontPlateApertureDiameter = 0.080;
	
	public Iris frontPlateAperture = new Iris("frontPlateAperture", frontPlateAperturePos, portNormal, frontPlateApertureDiameter*2, frontPlateApertureDiameter*0.49, null, null, Absorber.ideal());
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, portNormal, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, portNormal, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, portNormal, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
			
	/**** Lens *****/
	
	public double lens1CentrePos[] = { -2.84897, -4.55875, 1.339 }; // [CAD fromDesigner-20151106]
	
	/*public double lens1CentreThickness = 0.00874;
	//public double lens1CurvatureRadius = 0.10336;
	public double lens1Diameter = 0.075;
	public double lens1ClearAperture = 0.0735;
	//*/

	/* ** OptoSigma SLB-80-200PM ** */
	public double lens1Diameter = 0.080 + 0.001;	
	public double lens1CentreThickness = 0.01100;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1038;
	public double lens1ClearAperture = 0.079;
	
	//public double focalLength = 0.100; // [J.Balzhuhn's eBANF for the lens]. Delivers NA~0.40 to central fibres
	//public double lens1FocalLength = 0.120; // Would be better, NA~0.33, much better focus
	//public double focalLength = 0.130; // Would be better, NA~0.30, much better focus
	//public double focalLength = 0.140; // Would be better, NA~0.28, much better focus
	
	public double lens1FocalLength = 0.200; // attempt
	
	public Medium lens1Medium = new Medium(new BK7());  // [J.Balzhuhn's eBANF for the lens]
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lens1CentrePos,
											portNormal,
											lens1Diameter/2, // radius
											lens1FocalLength, // focal length
											lens1CentreThickness, 
											lens1Medium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lens1IrisPos[] = Util.plus(lens1CentrePos, Util.mul(portNormal, -0.002));
	public Iris lens1Iris = new Iris("lensIris", lens1IrisPos, portNormal, lens1Diameter, lens1ClearAperture/2, null, null, Absorber.ideal());

	public double lens2DistBehindLens1 = 0.040;
	
	public double lens2FocalLength = lens1FocalLength; // attempt
	public double lens2CentreThickness = lens1CentreThickness;
	public double lens2Diameter = lens1Diameter;
	public double lens2ClearAperture = lens1ClearAperture;
	
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(portNormal, lens2DistBehindLens1));
		
	public Medium lens2Medium = new Medium(new BK7());  // [J.Balzhuhn's eBANF for the lens]
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens2",
											lens2CentrePos,
											portNormal,
											lens2Diameter/2, // radius
											lens2FocalLength, // focal length
											lens2CentreThickness, // centreThickness 
											lens2Medium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lens2IrisPos[] = Util.plus(lens2CentrePos, Util.mul(portNormal, -0.002));
	public Iris lens2Iris = new Iris("lensIris", lens2IrisPos, portNormal, lens2Diameter, lens2ClearAperture/2, null, null, Absorber.ideal());
	

	public double lens3DistBehindLens2 = 0.060;
	
	public double lens3FocalLength = 0.300; // attempt
	public double lens3CentreThickness = lens1CentreThickness;
	public double lens3Diameter = lens1Diameter;
	public double lens3ClearAperture = lens1ClearAperture;
	
	public double lens3CentrePos[] = Util.plus(lens2CentrePos, Util.mul(portNormal, lens3DistBehindLens2));
		
	public Medium lens3Medium = new Medium(new BK7());  // [J.Balzhuhn's eBANF for the lens]
	public SimplePlanarConvexLens lens3 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens3",
											lens3CentrePos,
											portNormal,
											lens3Diameter/2, // radius
											lens3FocalLength, // focal length
											lens3CentreThickness, // centreThickness 
											lens3Medium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lens3IrisPos[] = Util.plus(lens3CentrePos, Util.mul(portNormal, -0.002));
	public Iris lens3Iris = new Iris("lens3Iris", lens3IrisPos, portNormal, lens3Diameter, lens3ClearAperture/2, null, null, Absorber.ideal());
	
	//public STLMesh portLiner = new STLMesh("portLiner", "/work/cad/aem41/portLiner-AEM41-201706.stl");
	public STLMesh portLiner = new STLMesh("portLiner", "/work/cad/aem41/portLiner-simple-edges-for-vignetting.stl");
	
	public double overrideObsPositions[][][] = null;
	
	
	public double fibre1EndPos[] = { -2.89649, -4.54998, 1.44268 }; // core channel, [fromDesigner-20151106] 
	public double fibre10EndPos[] = { -2.85912, -4.50896, 1.43616 }; // edge channel,  [fromDesigner-20151106]
		
	//with 100mm lens
	//public double[] R = { 5.263, 5.316, 5.372, 5.432, 5.495, 5.561, 5.631, 5.705, 5.783, 5.864, }; 
	//public double[] shift = { 48.509, 41.077, 34.787, 30.797, 28.446, 27.618, 27.531, 27.923, 28.864, 31.61, };
	//public double[] shift = {  0,0,0,0,0,0,0,0,0,0 }; 
	
	//with 120mm lens
	//public double[] R = { 5.254, 5.308, 5.366, 5.427, 5.491, 5.56, 5.631, 5.707, 5.787, 5.871, }; 
	//public double[] shift = { 25.869, 16.337, 9.758, 4.89, 2.571, 1.454, 1.151, 1.908, 3.073, 6.077, };
	
	//with 130mm lens
	//public double[] R = { 5.2  ,  5.283,  5.367,  5.45 ,  5.533,  5.617,  5.7,  5.783,  5.867,  5.95  }; 
	//public double[] shift = { 11.42, 3.214, -2.192, -7.756, -10.874, -11.638, -11.699, -11.075, -9.682, -6.013, };
	
	
	//with 140mm lens
	//public double[] R = { 5.248, 5.303, 5.362, 5.424, 5.489, 5.558, 5.632, 5.709, 5.79, 5.875, }; 
	//public double[] shift = { -0.586, -8.9, -16.132, -20.813, -23.698, -24.653, -24.819, -23.782, -22.044, -18.897, };

	
	//public double[] R = { 5.248, 5.304, 5.362, 5.424, 5.489, 5.559, 5.632, 5.708, 5.79, 5.875 }; 
	//public double[] shift = { 10,10,5,5,5,0,0,0,-5,-5 }; 

	//public double shift[] = new double[nFibres]; //set to 0 for optimisation

	public int beamIdx[] = { 0, 0 };
	
	//Design I did that M.Steffen made the first Fibre Holder from 
	/*public double[][] channelR = { { 5.250, 5.340, 5.430, 5.520, 5.610, 5.676, 5.742, 5.808, 5.874, 5.940, } }; 
	public double[][][] fibreEndPos = { 
			{{ -2.8858372192382813, -4.553509700927734, 1.4182009045410156 },
			{ -2.8858725486682864, -4.544170403817291, 1.4327007119598933 },
			{ -2.881634884407829, -4.537143890560845, 1.4355888800060466 },
			{ -2.877117408050361, -4.530367474369523, 1.4375979351367065 },
			{ -2.8722289338673828, -4.524490300678137, 1.4375428674983708 },
			{ -2.8685806344943576, -4.520687937282985, 1.436558426276313 },
			{ -2.8650675876871743, -4.517360445149739, 1.4350639623006185 },
			{ -2.861818899400499, -4.51400216840956, 1.4341508200412325 },			
			{ -2.8583163592215355, -4.512142818043448, 1.4304308811217898 },
			{ -2.8551192170538777, -4.510505395735583, 1.4269533444439038 }},
			
		}; 
	public double[][][] fibreEndNorm = { 
			{{ 0.3170934875229932,  -0.21285073924843653, -0.9242003478531586 },
			{ 0.2975662811064887,  -0.23530400943835703, -0.9252493347691139 },
			{ 0.2741704645186036,  -0.260515694491969, -0.9257224904413958 },
			{ 0.2509472182196351,  -0.2873821142552679, -0.924357622391896 },
			{ 0.21706372294218446, -0.320247924460108, -0.9221304718213532 },
			{ 0.18530745073596588, -0.3567338030754802, -0.9156430212943482 },
			{ 0.1652075585587437,  -0.38050365396483893, -0.909902979396411 },
			{ 0.15173825624623644, -0.39824329718787105, -0.9046423480228442 },			
			{ 0.1396887554149075, -0.4110136814389558, -0.9008633665993033 },
			{ 0.12786645556381948, -0.4237374462750325, -0.8967144172844885 }}
		};//*/
	
	 
	//Maximum channel spacing that gets past portLiner-201706
	/*public double[][] channelR = { 
			{ 5.15, 5.253, 5.357, 5.46, 5.563, 5.667, 5.77, 5.873, 5.977, 6.08, }, 
		}; 
		public double[][][] fibreEndPos = { { 
					{ -2.8906472322740897, -4.558206638647807, 1.4184311062467458 },
					{ -2.8898112573214534, -4.551128889560523, 1.4291405835591247 },
					{ -2.8860387053689465, -4.542704047119883, 1.435375697691949 },
					{ -2.88112338543887, -4.534342001145539, 1.439158010234487 },
					{ -2.8749741788900613, -4.527386355814355, 1.4380737930864902 },
					{ -2.8691962156856112, -4.520973082174108, 1.4372909649251777 },
					{ -2.8636121385176687, -4.515394314030346, 1.4354563102613043 },
					{ -2.8583809682429053, -4.5111668878738405, 1.4319544983025236 },
					{ -2.8533720295993787, -4.509750892499603, 1.4246205148166584 },
					{ -2.84973972931768, -4.510499810073653, 1.4172346685943336 },
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				{ 0.20192623, -0.32638819, -0.92341569 },
				}, 	};
	//*/
	
	//compromise to give portLiner design for out max view
	/*public double[][] channelR = { 
			{ 5.2, 5.294, 5.389, 5.483, 5.578, 5.672, 5.767, 5.861, 5.956, 6.05, }, 
		}; 
		public double[][][] fibreEndPos = { { 
					{ -2.8909056915611817, -4.555254191393281, 1.4246799869829576 },
					{ -2.8884967324925968, -4.547776727515737, 1.431852127542314 },
					{ -2.8844718517342818, -4.5401243032119565, 1.4362842773240898 },
					{ -2.8797588001147654, -4.532685664736943, 1.4389712428550958 },
					{ -2.8741095387657687, -4.526447958657366, 1.438063016958243 },
					{ -2.868863418387215, -4.520690211122519, 1.4370551038548935 },
					{ -2.863836728984682, -4.515459274426172, 1.4357063503668819 },
					{ -2.859053272611866, -4.511432044952181, 1.432741168032253 },
					{ -2.854509551071752, -4.508621494395093, 1.4286116417920418 },
					{ -2.85070796584163, -4.5103760764675265, 1.4184550492804635 },
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ 0.3201321172843118, -0.19256999576369724, -0.9275948599548368 },
				{ 0.3030730733066497, -0.21559751873811045, -0.9282588120510531 },
				{ 0.28577960839569067, -0.24739019079630797, -0.9258121347892195 },
				{ 0.26441055993153145, -0.2660391679530135, -0.9269898688289762 },
				{ 0.23413861602618902, -0.29693449229150165, -0.9257478143495284 },
				{ 0.18329212580148568, -0.3576558764401043, -0.9156889595637993 },
				{ 0.1670845012693307, -0.38077572414094335, -0.9094463246066363 },
				{ 0.1280464850853729, -0.39548835384896297, -0.9095015445985338 },
				{ 0.12608223189380832, -0.41643014714868654, -0.900382809335229 },
				{ 0.24796721897061816, -0.28946292044140104, -0.9245125613021775 },
				}, 	};
	//*/

	// ----------- Oct2021 for OP2 passive ------------- 
	public double[][] channelR = { 
			{ 5.2, 5.287, 5.373, 5.46, 5.547, 5.633, 5.72, 5.807, 5.893, 5.98, },
			{ 5.15, 5.168, 5.186, 5.203, 5.221, 5.239, 5.257, 5.274, 5.292, 5.31, 5.328, 5.345, 5.363, 5.381, 5.399, 5.416, 5.434, 5.452, 5.47, 5.487, 5.505, 5.523, 5.541, 5.558, 5.576, 5.594, 5.612, 5.629, 5.647, 5.665, 5.683, 5.7, 5.718, 5.736, 5.754, 5.771, 5.789, 5.807, 5.825, 5.842, 5.86, 5.878, 5.896, 5.913, 5.931, 5.949, 5.967, 5.984, 6.002, 6.02, }, 
		}; 
		public double[][][] fibreEndPos = { { 
					{ -2.897466008440165, -4.545585194158411, 1.453231917125102 },
					{ -2.893659841950125, -4.539485701765798, 1.455364439762582 },
					{ -2.889364919687961, -4.533346872825231, 1.4569350019050504 },
					{ -2.8847729154063018, -4.527179051615447, 1.4578612619342022 },
					{ -2.8799617585582213, -4.52142506389038, 1.4576857921222923 },
					{ -2.8751954665520634, -4.516265402322798, 1.4567160472174336 },
					{ -2.87063497028561, -4.511461751186183, 1.4555545525697842 },
					{ -2.8663189390336448, -4.5072242502649456, 1.4540107589798315 },
					{ -2.862328228837701, -4.503735202231092, 1.4518723847005124 },
					{ -2.85865093531958, -4.500736135789328, 1.4495941467985316 },
				}, 
			{ { -2.899505629169042, -4.548364161161539, 1.4521018423960261 },
				{ -2.8989065416283166, -4.547433110787763, 1.4524436040771178 },
				{ -2.8981378971273584, -4.546480868221906, 1.4528993083099462 },
				{ -2.8973540806959295, -4.545359518757637, 1.4533466761878429 },
				{ -2.8965751254083596, -4.5441933308508515, 1.453810811815597 },
				{ -2.895774141192328, -4.5429462392671995, 1.454173550007031 },
				{ -2.8950661625562057, -4.541661949279563, 1.45473072831907 },
				{ -2.8941681496432965, -4.540411880693198, 1.454938204375513 },
				{ -2.893395803770542, -4.539146228361884, 1.45546866723065 },
				{ -2.8924916574540607, -4.5378974228932165, 1.4557710281115468 },
				{ -2.8916302743283784, -4.53664250046218, 1.4561732033449641 },
				{ -2.890777434255375, -4.535326060616272, 1.4564903501872049 },
				{ -2.889870622261962, -4.53406202202226, 1.4568210073331527 },
				{ -2.8889791611265205, -4.532768310999187, 1.4570817321855989 },
				{ -2.8880669201170694, -4.531488520084011, 1.4573954020098057 },
				{ -2.887088746377709, -4.530262894000491, 1.4573772528510984 },
				{ -2.8862339138676028, -4.528931293276453, 1.457842538134701 },
				{ -2.8852620289117237, -4.527694993381986, 1.457929952676035 },
				{ -2.884245073333445, -4.526527824317204, 1.4578716425130083 },
				{ -2.8832755504242065, -4.525315916500885, 1.4578991222472588 },
				{ -2.8822868233171004, -4.5241005526221185, 1.457895755287018 },
				{ -2.881299322582848, -4.522963164118027, 1.4578348628424287 },
				{ -2.8803132789691257, -4.52182618757332, 1.4577152772196111 },
				{ -2.879321261443676, -4.52070254295122, 1.4575725140940203 },
				{ -2.8783148988105522, -4.5196332122633, 1.4573569361498417 },
				{ -2.877329622398499, -4.518532511532248, 1.4571890935075387 },
				{ -2.876391288646012, -4.517460260768697, 1.457106916807775 },
				{ -2.8754156880255137, -4.516436501619167, 1.4568944778747235 },
				{ -2.8744524798666378, -4.515432776953435, 1.456594531982067 },
				{ -2.873520708407449, -4.514410896714585, 1.456422196669865 },
				{ -2.8725877657865535, -4.51342215978876, 1.4562000312005428 },
				{ -2.8716544268170248, -4.512474935425308, 1.4559099977330248 },
				{ -2.8707525647804095, -4.511503188652549, 1.4557153437431443 },
				{ -2.869824933344303, -4.510643455936545, 1.4552866207939088 },
				{ -2.8689342149857806, -4.509721717174035, 1.455052779772189 },
				{ -2.8680364436749803, -4.508905414882614, 1.454645286306437 },
				{ -2.8671620705451843, -4.508094685686738, 1.4542477066069914 },
				{ -2.866320730424869, -4.5072313579999115, 1.453980775269092 },
				{ -2.8654649763726336, -4.506478545795096, 1.4535382289854073 },
				{ -2.8646331700763374, -4.505701716212127, 1.4531965808207443 },
				{ -2.8637952250440115, -4.505051722481375, 1.452608415950242 },
				{ -2.8630009834110584, -4.504306700799682, 1.4522669731522493 },
				{ -2.8622206209879235, -4.503627104486004, 1.4517919829234964 },
				{ -2.861436095220635, -4.502985726857995, 1.4513301048565674 },
				{ -2.8606460392102373, -4.502390367667475, 1.4508417517896817 },
				{ -2.859924277148609, -4.501778482708632, 1.4503580070707154 },
				{ -2.859171212456856, -4.501224436891216, 1.4498301154472193 },
				{ -2.858464026767858, -4.500672058772368, 1.4493435508435264 },
				{ -2.857761071804442, -4.4999946409269835, 1.449223136193853 },
				{ -2.857098832436903, -4.499364900423389, 1.4489864308286329 },
			}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ 0.2720515646146176, -0.26366079476785764, -0.925457147302413 },
				{ 0.25054404509557265, -0.2592108238780924, -0.9327579698140286 },
				{ 0.24874098161438385, -0.28321669671833016, -0.9262376729357699 },
				{ 0.24248559251586416, -0.2953417871034073, -0.924107118364963 },
				{ 0.2260119919438158, -0.3010181000237147, -0.9264484243365636 },
				{ 0.207866790614158, -0.333709986393481, -0.9194721541955648 },
				{ 0.18159438174564302, -0.35396993608406685, -0.9174577727977781 },
				{ 0.17674811656557116, -0.3624766969583617, -0.9150796399509058 },
				{ 0.16307478488668842, -0.3686990152812047, -0.9151325863856178 },
				{ 0.16065898731061637, -0.377868801116629, -0.9118135000859598 },
				}, { 
					{ 0.1693644781590702, -0.24300981926880758, -0.9551240240287371 },
					{ 0.20931279662661315, -0.23865154519615744, -0.9482792801405251 },
					{ 0.25593025749647796, -0.25259435049619644, -0.9331108173176385 },
					{ 0.2661939053778628, -0.25741738752084914, -0.9289117790950993 },
					{ 0.2790320906549641, -0.2657548451417667, -0.9227759504172156 },
					{ 0.2697353233316616, -0.27090493709745345, -0.9240418661529296 },
					{ 0.2636546263637959, -0.2599555957329768, -0.9289291287520702 },
					{ 0.2490896464756618, -0.2707619557143203, -0.9298614473976142 },
					{ 0.2546727448052333, -0.2651200054375319, -0.929974825342155 },
					{ 0.2541776210512604, -0.2808842062839308, -0.9254716633246899 },
					{ 0.26093696905300123, -0.2841093041153446, -0.92260164832745 },
					{ 0.247667154921132, -0.2807682368242576, -0.9272702829077775 },
					{ 0.2533024063396523, -0.28393921977688236, -0.9247791143916676 },
					{ 0.24505747744439493, -0.2812272955825141, -0.9278243588998595 },
					{ 0.24916729975296206, -0.286228995165417, -0.925196529965614 },
					{ 0.23152066201701882, -0.288639186236564, -0.9290240057328419 },
					{ 0.2412958127594196, -0.27928620821959577, -0.9293952574890253 },
					{ 0.23996383875771335, -0.2841125584387317, -0.9282765806730559 },
					{ 0.23822361862491317, -0.2975013263664655, -0.9245228327842671 },
					{ 0.23681913758613515, -0.28961768660049286, -0.9273824948106029 },
					{ 0.23156090588722636, -0.28699992198946506, -0.9295217004689713 },
					{ 0.23721851920158765, -0.2960487561067507, -0.9252472686560342 },
					{ 0.22634188353948204, -0.29800363714012196, -0.9273419994840405 },
					{ 0.22488892612711117, -0.2981225753706577, -0.9276572109134716 },
					{ 0.22414192404259775, -0.30398753386973915, -0.9259330305903751 },
					{ 0.22347073299746875, -0.30176290145290335, -0.9268225195798272 },
					{ 0.20805434958264848, -0.3226585147521385, -0.9233660544321981 },
					{ 0.20493970300004888, -0.3386099118943565, -0.9183371089644304 },
					{ 0.1989149358123515, -0.33673423833186183, -0.9203493364184215 },
					{ 0.18903486602520214, -0.34409489190744946, -0.9197089348212479 },
					{ 0.18647190843483286, -0.34261828056058463, -0.9207827871927123 },
					{ 0.1807702004721817, -0.34583834132653263, -0.9207160128344462 },
					{ 0.1830609679459029, -0.3464316710531749, -0.9200400965751514 },
					{ 0.17679503543621444, -0.3503213092182847, -0.9197926373659959 },
					{ 0.18090853470923496, -0.34818805247495804, -0.9198027952681236 },
					{ 0.1847402582801587, -0.355503353460562, -0.9162359972457287 },
					{ 0.17452809345300202, -0.3636997914027209, -0.9150204403887803 },
					{ 0.1723147015973187, -0.3604830608790977, -0.9167112993918334 },
					{ 0.1733790314308407, -0.35748733673044936, -0.9176832326775257 },
					{ 0.16790858019082794, -0.36886339945391594, -0.9141917201777762 },
					{ 0.17547627709384087, -0.3620554132237494, -0.9154910998653485 },
					{ 0.16813880190317185, -0.365715437568543, -0.9154133285126533 },
					{ 0.1617069190792355, -0.3638579914904382, -0.9173103260895115 },
					{ 0.16522342434401122, -0.36391971244754345, -0.9166589676319847 },
					{ 0.17246997262987956, -0.37194162102641554, -0.9120929443260105 },
					{ 0.16571947397365855, -0.36316207130745587, -0.9168698740331543 },
					{ 0.1670917121354272, -0.3703294900959995, -0.9137485586860798 },
					{ 0.1675702270507398, -0.36509587517238684, -0.9157648284020699 },
					{ 0.1700111674283274, -0.3904685676374159, -0.9047820182987956 },
					{ 0.16933905266701565, -0.3974135905247344, -0.9018795503325687 },
					}	};

		//*/

	
	public double[][] Z;
	
	//public double fibreEndPos[][];
	//public double fibreNA = 0.28; // [ written on the fibre bundle packing reel ]
	
	//public double fibreEndDiameter = 0.001; // roughly 1mm diameter [ looking at the fibres, and it agrees with 540x total / 10 = 54x per bundle. 54x * jacket size = ~1mm area ]
	
	public double fibreNA = 0.22; // [ written on the fibre bundle packing reel ]
	public double fibreEndDiameter = 0.0004; // roughly 1mm diameter [ looking at the fibres, and it agrees with 540x total / 10 = 54x per bundle. 54x * jacket size = ~1mm area ]
			
	public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5); 
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, portNormal, fibresYVec, 0.020, 0.070, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(portNormal, 0.050)), 
											portNormal, fibresYVec, 0.200, 0.200, Absorber.ideal());

	/***** Observation target ****/
	public int targetBeamIdx = 0; 
	public double targetBeamR = 5.6;
	public double targetObsPos[] = W7XRudix.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double beamAxis[] = W7XRudix.def().uVec(0);
	
	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lens1CentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.200, NullInterface.ideal());

	public Element tracingTarget = lens1;
		
	public BeamEmissSpecAEM41() {
		super("beamSpec-aem41");
		
		addElement(portLiner);
		addElement(frontPlateAperture);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lens1Iris);
		addElement(lens1);
		addElement(lens2Iris);
		addElement(lens2);
		//addElement(lens3Iris);
		//addElement(lens3);
		addElement(fibrePlane);
		addElement(beamPlane);
		
		/*fibreEndPos = new double[nFibres][];
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
		}*/
		
		int nBeams = channelR.length;
		fibrePlanes = new Square[nBeams][];
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			
			fibrePlanes[iB] = new Square[nFibres];
			for(int iF=0; iF < nFibres; iF++){
						//double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
				double norm[] = fibreEndNorm[iB][iF];
				double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
				double y[] = Util.reNorm(Util.cross(x, norm));
				
				//shift away from Y=0, to get multiple rows in
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibresYVec, ((iB == 0) ? 1 : -1) * 0.003));
				
				fibrePlanes[iB][iF] = new Square("fibrePlane_" + iB + "_" + iF, fibreEndPos[iB][iF], norm, y, 0.007, 0.007, NullInterface.ideal());
				addElement(fibrePlanes[iB][iF]);
			}
		}
			
		addElement(catchPlane);
		
		Z = new double[nBeams][];
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;			
			Z[iB] = new double[nFibres];
			for(int iF=0; iF < nFibres; iF++){
				Z[iB][iF] = W7XRudix.def().getPosOfBoxAxisAtR(0, channelR[iB][iF])[2];
			}
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());		
	}

	public String getDesignName() { return "aem41";	}
	
	

}
