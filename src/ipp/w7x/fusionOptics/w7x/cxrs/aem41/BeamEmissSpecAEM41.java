package ipp.w7x.fusionOptics.w7x.cxrs.aem41;

import ipp.w7x.fusionOptics.w7x.cxrs.ObservationSystem;
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
public class BeamEmissSpecAEM41 extends ObservationSystem {
	
	public String lightPathsSystemName() { return "AEM41"; };	

	@Override
	protected String[] lightPathRowNames() { return new String[]{ "A", "B" };	}
	
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
			
	public double frontPlateTotalThickness = 0.076;
	public double frontPlateAperture2Diameter = frontPlateApertureDiameter;
	private double frontPlateAperture2Pos[] = Util.plus(frontPlateAperturePos, Util.mul(portNormal, frontPlateTotalThickness));
	
	public Iris frontPlateAperture2 = new Iris("frontPlateAperture2", frontPlateAperture2Pos, portNormal, frontPlateAperture2Diameter*2, frontPlateAperture2Diameter*0.49, null, null, Absorber.ideal());
	
	/**** Lens *****/
	public double lens1BehindWindow = 0.060;
	public double lens1CentrePos[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, lens1BehindWindow));
	//public double lens1CentrePos[] = { -2.84897, -4.55875, 1.339 }; // [CAD fromDesigner-20151106]
	
	/*public double lens1CentreThickness = 0.00874;
	//public double lens1CurvatureRadius = 0.10336;
	public double lens1Diameter = 0.075;
	public double lens1ClearAperture = 0.0735;
	//*/

	/* ** OptoSigma SLB-80-200PM ** */	
	/*public double lens1Diameter = 0.080 + 0.001;	
	public double lens1CentreThickness = 0.01100;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1038;
	public double lens1ClearAperture = 0.079;
	*/

	/* ** OptoSigma SLB-100-200PM ** */	
	//https://www.optosigma.com/us_en/plano-convex-lens-100mm-diameter-200mm-focal-length-400-700nm-SLB-100-200PM.html
	public double lens1Diameter = 0.100 + 0.001;	
	public double lens1CentreThickness = 0.01580;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1038;
	public double lens1ClearAperture = 0.085;
	
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
	

	public double lens3DistBehindLens2 = 0.050;
	
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
	
	// ----------- Oct2021 for OP2 passive -------------
	//50x 400um fibres in straight array
	public double[][] channelR = { 
			{ 6.1, 6.094, 6.086, 6.076, 6.065, 6.054, 6.041, 6.028, 6.015, 6.001, 5.986, 5.971, 5.955, 5.94, 5.923, 5.907, 5.89, 5.873, 5.855, 5.837, 5.819, 5.801, 5.782, 5.763, 5.744, 5.725, 5.705, 5.685, 5.665, 5.645, 5.624, 5.604, 5.583, 5.562, 5.54, 5.519, 5.497, 5.475, 5.453, 5.431, 5.409, 5.386, 5.363, 5.341, 5.318, 5.294, 5.271, 5.247, 5.224, 5.2, },
			//{ 6.05, 5.993, 5.918, 5.836, 5.748, 5.655, 5.558, 5.458, 5.355, 5.25, }, 
		}; 
		public double[][][] fibreEndPos = { { 
				/*	{ -2.8493004725039275, -4.506494259433065, 1.4225712263181869 },
					{ -2.8493233985045032, -4.5066799117349, 1.4224433354211765 },
					{ -2.849412570606781, -4.506913703867188, 1.4221560271271891 },
					{ -2.8496436068973368, -4.507093190451572, 1.4224293128874832 },
					{ -2.8499811949689677, -4.5072885079378535, 1.422534264580225 },
					{ -2.8501936289715113, -4.5075774564493285, 1.4226609074418002 },
					{ -2.8506636429513286, -4.507730123530328, 1.4231674512378598 },
					{ -2.851099455455346, -4.5079091873548105, 1.4236843029917887 },
					{ -2.8515795286395447, -4.508161722516428, 1.4241399456401171 },
					{ -2.852115331807848, -4.508353594760318, 1.424908524069706 },
					{ -2.8527337901761993, -4.508587622294353, 1.425744892340987 },
					{ -2.8534029126361404, -4.508813635083379, 1.4266586588374925 },
					{ -2.8540839024971167, -4.509185733862964, 1.4273905475043234 },
					{ -2.8547152552303108, -4.509670589498523, 1.4277988408785731 },
					{ -2.855458331538676, -4.510240199468382, 1.4283096780066704 },
					{ -2.8561494091104582, -4.5108107953060905, 1.428789806509652 },
					{ -2.856922608123572, -4.511410693461548, 1.4292919483725302 },
					{ -2.857686120735338, -4.512082726997088, 1.429704109757678 },
					{ -2.85854702039228, -4.512748144544922, 1.4302649684440552 },
					{ -2.8593818899079126, -4.513506722590522, 1.4307172954724423 },
					{ -2.860260950102707, -4.514242780957592, 1.4312107090955661 },
					{ -2.861138631832838, -4.515046687148942, 1.4316282061352281 },
					{ -2.862094545475408, -4.515896980547715, 1.4320961356528192 },
					{ -2.8630498506158526, -4.516795043500131, 1.4325257728667573 },
					{ -2.864027410391285, -4.517708545630244, 1.432967385015006 },
					{ -2.8650107236176074, -4.518680540299186, 1.4333172241043515 },
					{ -2.8660829008261755, -4.519711878461927, 1.433711244139425 },
					{ -2.867137257191559, -4.520802007810937, 1.4339993445294827 },
					{ -2.8682187234466117, -4.521911538357986, 1.4343363058935927 },
					{ -2.8693177461605353, -4.523045545031182, 1.4346347993997004 },
					{ -2.8704600161741816, -4.524284097924912, 1.4348732210758195 },
					{ -2.8715812591152696, -4.525464995286082, 1.435146126114015 },
					{ -2.8727354604847237, -4.526790099897638, 1.4352784792177427 },
					{ -2.873924210630218, -4.528098397800642, 1.4354914885159078 },
					{ -2.8751582862542917, -4.52953004257828, 1.4356048254135145 },
					{ -2.8763494608795512, -4.530905315239979, 1.4356989570785044 },
					{ -2.877563568031021, -4.532424056487563, 1.4356589499663186 },
					{ -2.878793497096093, -4.533938929888674, 1.4356042122165682 },
					{ -2.8800148643796883, -4.535480084540104, 1.4355162682956089 },
					{ -2.881199411806548, -4.537071202236006, 1.4352525744560594 },
					{ -2.882355988494338, -4.538681484668827, 1.4349481713144134 },
					{ -2.8835684759292928, -4.540364972075716, 1.434595038680339 },
					{ -2.884707699821798, -4.542091380877236, 1.4340597795007177 },
					{ -2.885800725308988, -4.5437339212405075, 1.4335679526099814 },
					{ -2.8868899513441337, -4.545458331169597, 1.4329440266945588 },
					{ -2.88802032939455, -4.547208995366803, 1.4322930735987198 },
					{ -2.889148711008057, -4.548852052298095, 1.431941677669455 },
					{ -2.8903544848637948, -4.550532711047177, 1.4315710594571651 },
					{ -2.891569930772904, -4.552045286415267, 1.4315026819262198 },
					{ -2.8927033714389707, -4.553690065603098, 1.4311753449427025 },
					*/
			{-2.8956,-4.5509,1.4335},
			{-2.8940,-4.5491,1.4332},
			{-2.8924,-4.5474,1.4330},
			{-2.8908,-4.5456,1.4327},
			{-2.8892,-4.5438,1.4324},
			{-2.8876,-4.5421,1.4321},
			{-2.8860,-4.5403,1.4319},
			{-2.8844,-4.5385,1.4316},
			{-2.8828,-4.5368,1.4313},
			{-2.8812,-4.5350,1.4310},
			{-2.8816,-4.5329,1.4353},
			{-2.8806,-4.5318,1.4351},
			{-2.8796,-4.5307,1.4349},
			{-2.8786,-4.5296,1.4348},
			{-2.8776,-4.5285,1.4346},
			{-2.8766,-4.5274,1.4344},
			{-2.8756,-4.5263,1.4343},
			{-2.8746,-4.5251,1.4341},
			{-2.8736,-4.5240,1.4339},
			{-2.8726,-4.5229,1.4337},
			{-2.8719,-4.5225,1.4331},
			{-2.8709,-4.5214,1.4329},
			{-2.8699,-4.5203,1.4328},
			{-2.8689,-4.5192,1.4326},
			{-2.8679,-4.5181,1.4324},
			{-2.8669,-4.5170,1.4323},
			{-2.8659,-4.5159,1.4321},
			{-2.8649,-4.5148,1.4319},
			{-2.8639,-4.5137,1.4317},
			{-2.8629,-4.5126,1.4316},
			{-2.8619,-4.5129,1.4290},
			{-2.8614,-4.5124,1.4290},
			{-2.8609,-4.5118,1.4289},
			{-2.8605,-4.5113,1.4288},
			{-2.8600,-4.5108,1.4287},
			{-2.8595,-4.5103,1.4286},
			{-2.8591,-4.5098,1.4286},
			{-2.8586,-4.5093,1.4285},
			{-2.8581,-4.5088,1.4284},
			{-2.8577,-4.5082,1.4283},
			{-2.8563,-4.5090,1.4244},
			{-2.8558,-4.5085,1.4243},
			{-2.8554,-4.5080,1.4242},
			{-2.8549,-4.5075,1.4241},
			{-2.8544,-4.5070,1.4240},
			{-2.8540,-4.5064,1.4240},
			{-2.8535,-4.5059,1.4239},
			{-2.8530,-4.5054,1.4238},
			{-2.8526,-4.5049,1.4237},
			{-2.8521,-4.5044,1.4236}
				}, { 
					{ -2.8500899031955997, -4.507965928802268, 1.4217131033936747 },
					{ -2.85203299844313, -4.5091645129217035, 1.4234732671427024 },
					{ -2.855165059073376, -4.511235979762206, 1.4261694993034213 },
					{ -2.8589233801573104, -4.514387945332718, 1.4283539967288525 },
					{ -2.863303950722654, -4.518364798715294, 1.4304699834777364 },
					{ -2.8682365849986233, -4.523320550043452, 1.4320908687652993 },
					{ -2.8736269078717833, -4.5291918364023696, 1.4331409135755924 },
					{ -2.8792078530975136, -4.53598497738618, 1.4331007227733257 },
					{ -2.8846642620208476, -4.543480685178386, 1.4317277563527815 },
					{ -2.8896763752271255, -4.551206512577184, 1.4291393615764727 },
				} }; 
		
		
		
		
		public double[][][] fibreEndNorm = { { 
				{ 0.33199416967021966, -0.15541247060593388, -0.9303906895950438 },
				{ 0.35965241578424556, -0.18257186716392187, -0.9150506287309109 },
				{ 0.3462266140492317, -0.1744038381907173, -0.9217973925697287 },
				{ 0.34284183450622535, -0.1864478805035183, -0.9207044392030224 },
				{ 0.32093365842761623, -0.18051224591374757, -0.9297402411229928 },
				{ 0.32571450863265555, -0.19461238970739111, -0.9252221769059399 },
				{ 0.30463018916826234, -0.20228584722621187, -0.9307421145834556 },
				{ 0.28875769077605845, -0.21784774350219974, -0.9322882369035222 },
				{ 0.2743374423317252, -0.23315783027552398, -0.9329396518082493 },
				{ 0.25472902149660315, -0.2598279367146329, -0.9314518607582465 },
				{ 0.23045299532870933, -0.2941833228472517, -0.9275492383170694 },
				{ 0.19886399038654365, -0.3281620831990128, -0.9234515474447076 },
				{ 0.1735752569497936, -0.35906773273299536, -0.9170288945746471 },
				{ 0.16171089408805686, -0.3683571344214569, -0.9155122108710806 },
				{ 0.16366969106933701, -0.36610019344779593, -0.9160692553419497 },
				{ 0.16723396441051122, -0.36949825779112927, -0.9140589907860784 },
				{ 0.1665787889018965, -0.36695992363828484, -0.9152004816057308 },
				{ 0.17317171201782808, -0.3617291286868639, -0.9160587293488647 },
				{ 0.16695743277594638, -0.3637693320979702, -0.9164044350972238 },
				{ 0.175076548947273, -0.3646588244168307, -0.9145338396055381 },
				{ 0.17581683428116437, -0.3576470404486417, -0.917157039574836 },
				{ 0.17936047719374332, -0.36207617786414126, -0.9147298293179946 },
				{ 0.1771709321786358, -0.35411278438483573, -0.9182671706677594 },
				{ 0.17972422443399513, -0.3538312688407413, -0.9178794236402459 },
				{ 0.18693812086019132, -0.35426176648139207, -0.916271106048174 },
				{ 0.18889816258945885, -0.35400322975256565, -0.9159689937410974 },
				{ 0.18226128494398902, -0.3504068951522241, -0.9186924577029814 },
				{ 0.19036673006670837, -0.3419526538030404, -0.9202330632186431 },
				{ 0.19501027538297866, -0.34266344465257154, -0.9189955147844242 },
				{ 0.1970989473038641, -0.33236800201133204, -0.9223250599494214 },
				{ 0.20742059366003388, -0.32618247399664985, -0.9222698580042308 },
				{ 0.2117075419776229, -0.3162396333270802, -0.9247553249281385 },
				{ 0.2182556735906219, -0.31696129445237825, -0.9229842895545821 },
				{ 0.22318440070909498, -0.310189739199638, -0.9241055399549245 },
				{ 0.22775597847122034, -0.3065630552607003, -0.9242003610796967 },
				{ 0.23197847446411385, -0.30053002740506696, -0.9251311744899813 },
				{ 0.23628863441054734, -0.3024015273779962, -0.9234289347252735 },
				{ 0.23509648337613098, -0.2955795882933964, -0.9259386321395618 },
				{ 0.23974860078777063, -0.29180098096738927, -0.9259442725816579 },
				{ 0.23928057723705476, -0.28836490583678465, -0.9271410283440401 },
				{ 0.242357284657923, -0.2869223772469646, -0.9267893482384165 },
				{ 0.24659319672383967, -0.28036051475156915, -0.9276797815505914 },
				{ 0.2463404261978303, -0.2798932177457615, -0.9278880218434739 },
				{ 0.24918024016128815, -0.27762936411586775, -0.9278098641929724 },
				{ 0.24782631764360274, -0.2803900692542855, -0.9273421835260106 },
				{ 0.24338951837692535, -0.2797309210895735, -0.9287153245912481 },
				{ 0.2512279181015828, -0.2780747255083759, -0.9271240371168173 },
				{ 0.2533986606529275, -0.2704063600982982, -0.9288000426344155 },
				{ 0.25347199091209377, -0.2657875862808177, -0.9301123097788118 },
				{ 0.2612918959185039, -0.2697044395295274, -0.9268150087398117 },
				},{ 
					{ 0.2979844694816753, -0.2191558044077775, -0.9290726501959383 },
					{ 0.24221873118713183, -0.2897161700336521, -0.9259560610974626 },
					{ 0.15647354023296825, -0.3783143820101308, -0.9123564323066148 },
					{ 0.1655919043135133, -0.3680767016373465, -0.9149310700471354 },
					{ 0.18219796355930412, -0.3556435661725476, -0.9166905453395466 },
					{ 0.19435612001378325, -0.33795513016281464, -0.9208756857523294 },
					{ 0.22482533651554262, -0.3072180281524845, -0.9247003034706824 },
					{ 0.24331814025670961, -0.2886069267393152, -0.9260142139622393 },
					{ 0.25554266930889663, -0.26462526743333376, -0.9298770951036059 },
					{ 0.281945454129101, -0.24044739096323242, -0.9288120439975515 },
			},
		};

		//*/

	
	public double[][] Z;
	
	//public double fibreEndPos[][];
	//public double fibreNA = 0.28; // RUDIX-AEM41 [ written on the fibre bundle packing reel ]	
	//public double fibreEndDiameter = 0.001; // RUDIX-AEM41 roughly 1mm diameter [ looking at the fibres, and it agrees with 540x total / 10 = 54x per bundle. 54x * jacket size = ~1mm area ]
	
	private double fibreNA[] = { 0.22, 0.28 }; // [ written on the fibre bundle packing reel ]
	private double fibreEndDiameter[] = { 0.000400, 0.001000 }; // 400um fibres, patched via XFER-AEM21
			
	public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5); 
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, portNormal, fibresYVec, 0.020, 0.070, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(portNormal, 0.050)), 
											portNormal, fibresYVec, 0.200, 0.200, Absorber.ideal());
	
	public final String backgroundSTLFiles[] = {			
			"/work/cad/aem41/bg-targetting/baffle-m4-cut.stl",
			"/work/cad/aem41/bg-targetting/shield-m4-cut.stl",
			"/work/cad/aem41/bg-targetting/target-m4-cut.stl",			
	};

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
		addElement(frontPlateAperture2);
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
			
			//shift fibres of 10-channel system and 50-channel systems in opposite directions
			double outOfPlaneShift = (nFibres <= 10 ? 1 : -1) * 0.003;
			
			fibrePlanes[iB] = new Square[nFibres];
			for(int iF=0; iF < nFibres; iF++){
						//double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
				
				fibreEndNorm[iB][iF] = Util.mul(portNormal, -1);
				double norm[] = fibreEndNorm[iB][iF];
				double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
				double y[] = Util.reNorm(Util.cross(x, norm));
				
				//shift away from Y=0, to get multiple rows in
				
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibresYVec, outOfPlaneShift));
				
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

	public double getFibreNA(int iB, int iP) { return fibreNA[iB];	}
	public double getFibreDiameter(int iB, int iP) { return fibreEndDiameter[iB]; }

}
