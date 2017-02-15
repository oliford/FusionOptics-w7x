package ipp.w7x.fusionOptics.w7x.bremstrahlung;

import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.materials.Air;
import fusionOptics.materials.FusedSilica;
import fusionOptics.materials.BK7;
import fusionOptics.materials.LAK9;
import fusionOptics.materials.SF6;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Dish;
import fusionOptics.surfaces.Iris;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Collection optics for W7-X Thomson Scattering System AEM 31 port as designed
 *  using the java project "FusionOptics" by oliford
 *  
 * 	<Andrea, E3, IPP>*/

public class AEM31Design extends Optic {
	// ****************** materials initialization *******************
	public static Medium Suprasil = new Medium(new FusedSilica());
	public static Medium BK7 = new Medium(new BK7());
	public static Medium mediumLAK9 = new Medium(new LAK9());
	public static Medium mediumSF6 = new Medium(new SF6());
	public static Medium mediumAir = new Medium(new Air());
	
	/**  Fibre positions for AEM31 TS optics from Andrea's code */
	public final static double fibrePosAEM31[][] = { 
	  		{-5.112729064303374,	1.3606436669254245,	1.617604250356293},
		   {-5.118457534101027,	1.3636939675087878,	1.6197040146394877},
		   {-5.123952608519543,	1.3665962438161467,	1.6215897582716328},
		   {-5.129452052940696,	1.3694222081452336,	1.6233108460048247},
		   {-5.134931848494507,	1.3721189729010366,	1.6247867120496007},
		   {-5.145582358976941,	1.377053994549046,	1.6269911429514583},
		   {-5.1566969737873665,	1.3818273886885624,	1.628330483131044},
		   {-5.168369967395094,	1.386655848436531,	1.628992716584907},
		   {-5.174189451558558,	1.3889465716771796,	1.6291683817672165},
		   {-5.178021664471207,	1.3904980375352614,	1.6281526501742962}};
	
	// ******************* surfaces initialization *******************
	public static double w1Dist = 0.0;
	
	public static double w1Pos[] =  new double[]{w1Dist, 0.0, 0.0};
	public static double w1Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double w1Thick = 20e-3;
	public static double w1Radius = 110.0e-3;
	public static Medium w1Medium1 = null;
	public static Medium w1Medium2 = Suprasil;
	
	public static double w2Dist = w1Dist + w1Thick;
	public static double w2Pos[] =  new double[]{w2Dist, 0.0, 0.0};
	public static double w2Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double w2Thick = 34e-3;
	public static double w2Radius = 110.0e-3;
	public static Medium w2Medium1 = Suprasil;
	public static Medium w2Medium2 = mediumAir;
	
	public static double s1Dist = w2Dist + w2Thick ;
	public static double s1Pos[] =  new double[]{s1Dist, 0.0, 0.0};
	public static double s1Curv = 210.048e-3;
	public static double s1Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double s1Thick = 65e-3;
	public static double s1Radius = 80.0e-3;
	public static Medium s1Medium1 = mediumAir;
	public static Medium s1Medium2 = BK7;
	
	public static double s2Dist = s1Dist + s1Thick;
	public static double s2Pos[] =  new double[]{s2Dist, 0.0, 0.0};
	public static double s2Curv = 230.215e-3;
	public static double s2Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s2Thick = 3.584717e-3;
	public static double s2Radius = 80.0e-3;
	public static Medium s2Medium1 = BK7;
	public static Medium s2Medium2 = mediumAir;
	
	public static double s3Dist = s2Dist + s2Thick;
	public static double s3Pos[] =  new double[]{s3Dist, 0.0, 0.0};
	public static double s3Curv = 187.4196e-3;
	public static double s3Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s3Thick = 20e-3;
	public static double s3Radius = 80.0e-3;
	public static Medium s3Medium1 = mediumAir;
	public static Medium s3Medium2 = mediumLAK9;
	
	public static double s4Dist = s3Dist + s3Thick;
	public static double s4Pos[] =  new double[]{s4Dist, 0.0, 0.0};
	public static double s4Curv = 295.7254e-3;
	public static double s4Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s4Thick = 20e-3;
	public static double s4Radius = 80.0e-3;
	public static Medium s4Medium1 = mediumLAK9;
	public static Medium s4Medium2 = mediumAir;
	
	public static double s5Dist = s4Dist + s4Thick;
	public static double s5Pos[] =  new double[]{s5Dist, 0.0, 0.0};
	public static double s5Curv = 203.3874e-3;
	public static double s5Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double s5Thick = 50e-3;
	public static double s5Radius = 70.0e-3;
	public static Medium s5Medium1 = mediumAir;
	public static Medium s5Medium2 = mediumLAK9;
	
	public static double s6Dist = s5Dist + s5Thick;
	public static double s6Pos[] =  new double[]{s6Dist, 0.0, 0.0};
	public static double s6Curv = 148.9331e-3;
	public static double s6Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s6Thick = 3.342302e-3;
	public static double s6Radius = 70.0e-3;
	public static Medium s6Medium1 = mediumLAK9;
	public static Medium s6Medium2 = mediumAir;
	
	public static double s7Dist = s6Dist + s6Thick;
	public static double s7Pos[] =  new double[]{s7Dist, 0.0, 0.0};
	public static double s7Curv = 128.096e-3;
	public static double s7Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s7Thick = 17e-3;
	public static double s7Radius = 70.0e-3;
	public static Medium s7Medium1 = mediumAir;
	public static Medium s7Medium2 = mediumSF6;
	
	public static double s8Dist = s7Dist + s7Thick;
	public static double s8Pos[] =  new double[]{s8Dist, 0.0, 0.0};
	public static double s8Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s8Thick = 25e-3;
	public static double s8Radius = 70.0e-3;
	public static Medium s8Medium1 = mediumLAK9;
	public static Medium s8Medium2 = mediumAir;
	
	public static double s9Dist = s8Dist + s8Thick;
	public static double s9Pos[] =  new double[]{s9Dist, 0.0, 0.0};
	public static double s9Curv = 191.4201e-3;
	public static double s9Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s9Thick = 17e-3;
	public static double s9Radius = 70.0e-3;
	public static Medium s9Medium1 = mediumAir;
	public static Medium s9Medium2 = mediumSF6;
	
	public static double s10Dist = s9Dist + s9Thick;
	public static double s10Pos[] =  new double[]{s10Dist, 0.0, 0.0};
	public static double s10Curv = 132.6221e-3;
	public static double s10Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double s10Thick = 2.663274e-3;
	public static double s10Radius = 70.0e-3;
	public static Medium s10Medium1 = mediumSF6;
	public static Medium s10Medium2 = mediumAir;
	
	public static double s11Dist = s10Dist + s10Thick;
	public static double s11Pos[] =  new double[]{s11Dist, 0.0, 0.0};
	public static double s11Curv = 150.153e-3;
	public static double s11Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double s11Thick = 50e-3;
	public static double s11Radius = 70.0e-3;
	public static Medium s11Medium1 = mediumAir;
	public static Medium s11Medium2 = mediumLAK9;
	
	public static double s12Dist = s11Dist + s11Thick;
	public static double s12Pos[] =  new double[]{s12Dist, 0.0, 0.0};
	public static double s12Curv = 116.0759e-3;
	public static double s12Norm[] = new double[]{-1.0, 0.0, 0.0};
	public static double s12Thick = 10e-3;
	public static double s12Radius = 70.0e-3;
	public static Medium s12Medium1 = mediumLAK9;
	public static Medium s12Medium2 = mediumAir;
	
	public static double s13Dist = s12Dist + s12Thick;
	public static double s13Pos[] =  new double[]{s13Dist, 0.0, 0.0};
	public static double s13Curv = 108.0164e-3;
	public static double s13Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double s13Thick = 40e-3;
	public static double s13Radius = 65.0e-3;
	public static Medium s13Medium1 = mediumAir;
	public static Medium s13Medium2 = mediumLAK9;
	
	public static double s14Dist = s13Dist + s13Thick;
	public static double s14Pos[] =  new double[]{s14Dist, 0.0, 0.0};
	public static double s14Curv = 349.9835e-3;
	public static double s14Norm[] = new double[]{1.0, 0.0, 0.0};
	public static double s14Thick = 37.22965e-3;
	public static double s14Radius = 65.0e-3;
	public static Medium s14Medium1 = mediumLAK9;
	public static Medium s14Medium2 = mediumAir;
	
	
	// ******************** lenses initialization ********************
	public Disc windowFront = new Disc("W-front", w1Pos, w1Norm, w1Radius, w1Medium2, w1Medium1, IsoIsoInterface.ideal());
	public Disc windowBack  = new Disc("W-back" , w2Pos, w2Norm, w2Radius, w2Medium2, w2Medium1, IsoIsoInterface.ideal());
	
	public Dish lens1Front = new Dish("L1-front", s1Pos, s1Norm, s1Curv, s1Radius, s1Medium2, s1Medium1, IsoIsoInterface.ideal());
	public Dish lens1Back  = new Dish("L1-back" , s2Pos, s2Norm, s2Curv, s2Radius, s2Medium1, s2Medium2, IsoIsoInterface.ideal());

	public Dish lens2Front = new Dish("L2-front", s3Pos, s3Norm, s3Curv, s3Radius, s3Medium1, s3Medium2, IsoIsoInterface.ideal());
	public Dish lens2Back  = new Dish("L2-back" , s4Pos, s4Norm, s4Curv, s4Radius, s4Medium1, s4Medium2, IsoIsoInterface.ideal());
	
	public Dish lens3Front = new Dish("L3-front", s5Pos, s5Norm, s5Curv, s5Radius, s5Medium2, s5Medium1, IsoIsoInterface.ideal());
	public Dish lens3Back  = new Dish("L3-back" , s6Pos, s6Norm, s6Curv, s6Radius, s6Medium1, s6Medium2, IsoIsoInterface.ideal());

	public Dish lens4Front = new Dish("L4-front", s7Pos, s7Norm, s7Curv, s7Radius, s7Medium1, s7Medium2, IsoIsoInterface.ideal());
	public Disc lens4Back  = new Disc("L4-back" , s8Pos, s8Norm, s8Radius, s8Medium1, s8Medium2, IsoIsoInterface.ideal());

	public Dish lens5Front = new Dish("L5-front", s9Pos, s9Norm, s9Curv, s9Radius, s9Medium1, s9Medium2, IsoIsoInterface.ideal());
	public Dish lens5Back  = new Dish("L5-back" , s10Pos, s10Norm, s10Curv, s10Radius, s10Medium2, s10Medium1, IsoIsoInterface.ideal());

	public Dish lens6Front = new Dish("L6-front", s11Pos, s11Norm, s11Curv, s11Radius, s11Medium2, s11Medium1, IsoIsoInterface.ideal());
	public Dish lens6Back  = new Dish("L6-back" , s12Pos, s12Norm, s12Curv, s12Radius, s12Medium1, s12Medium2, IsoIsoInterface.ideal());

	public Dish lens7Front = new Dish("L7-front", s13Pos, s13Norm, s13Curv, s13Radius, s13Medium2, s13Medium1, IsoIsoInterface.ideal());
	public Dish lens7Back  = new Dish("L7-back" , s14Pos, s14Norm, s14Curv, s14Radius, s14Medium2, s14Medium1, IsoIsoInterface.ideal());

	// ****************** optic system constructor *******************
	public AEM31Design() {
		super("AEM31Design");

		addElement(windowFront);
		addElement(windowBack);
		addElement(lens1Front);
		addElement(lens1Back);
		addElement(lens2Front);
		addElement(lens2Back);
		addElement(lens3Front);
		addElement(lens3Back);
		addElement(lens4Front);
		addElement(lens4Back);
		addElement(lens5Front);
		addElement(lens5Back);
		addElement(lens6Front);
		addElement(lens6Back);
		addElement(lens7Front);
		addElement(lens7Back);
		addElement(new Iris("Port", new double[]{0.001*(0), 0,0}, new double[]{1,0,0}, 0.001*200.0, 0.001*0.5*220, Absorber.ideal()));		
		addElement(new Iris("Front", new double[]{0.001*(20+34), 0,0}, new double[]{1,0,0}, 0.001*200.0, 0.001*0.5*158, Absorber.ideal()));
		addElement(new Iris("Stop", new double[]{0.001*(20+34+65+3.584717+30), 0,0}, new double[]{1,0,0}, 0.001*200.0, 0.001*0.5*105.5592, Absorber.ideal()));
	}
}