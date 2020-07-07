package ipp.w7x.fusionOptics.w7x.bremstrahlung;

import algorithmrepository.Algorithms;
import fusionOptics.Util;

/** Collection optics for W7-X Thomson Scattering System AEN 31 port using as designed AEM31 optics rotated
 * and shifted to match AEN31 optics in CAD.
 *  
 *  using the java project "FusionOptics" by oliford
 *  @author oliford <codes[at]oliford.co.uk> and Andrea Dal Molin
 * 	Copyright 2011 Oliver Ford <codes[at]oliford.co.uk> */

public class AEN31AsDesignInPlace extends AEM31Design {
	
	/** Positions of extreme (core/edge) TS fibre bundles from AEN31 CAD */
	public double edgeTSFibres[][] = { 
		{ -5.960023193359375, 1.5746832885742188, 1.35295556640625 }, // TS1
		{ -5.892557373046875, 1.5779461669921875, 1.4212440795898438 }, // TS2
	};
	
	/** Bremastrahlung fibre positions from AEN31 CAD fibre holder front surface */
	public double bremstrahlungFibres[][] = {
		{-5.95970337,  1.57645483,  1.35092975},
		{-5.95836987,  1.57693799,  1.35325934},
		{-5.95638184,  1.5775957 ,  1.35659644},
		{-5.95434058,  1.57820056,  1.35986731},
		{-5.95223706,  1.5787627 ,  1.36308282},
		{-5.9500813 ,  1.57928143,  1.36623053},
		{-5.94786865,  1.5797533 ,  1.36932587},
		{-5.94560767,  1.58017279,  1.37236401},
		{-5.94329272,  1.58055334,  1.37534198},
		{-5.94092529,  1.58089111,  1.37826465},
		{-5.93850732,  1.58118567,  1.38113135},
		{-5.93603857,  1.5814422 ,  1.38393927},
		{-5.93351636,  1.58165601,  1.38669763},
		{-5.93094897,  1.5818233 ,  1.38940076},
		{-5.92833325,  1.58194843,  1.39204932},
		{-5.92566357,  1.58203375,  1.39464807},
		{-5.92294971,  1.58207751,  1.39718878},
		{-5.92018091,  1.58208154,  1.39968005},
		{-5.91736548,  1.58203912,  1.40211975},
		{-5.91451074,  1.58195483,  1.40449792},
		{-5.91159204,  1.58183521,  1.40682935},
		{-5.90863086,  1.58167194,  1.40910272},
		{-5.90561768,  1.58146698,  1.411323  },
		{-5.90255444,  1.5812196 ,  1.41348834},
		{-5.89944385,  1.58092773,  1.41559711},
		{-5.89627759,  1.58059375,  1.41765167},
		{-5.89306494,  1.58021497,  1.41964703},
	};
	
	/** Breamstrahlung fibres and the two edge TS fibres */
	public double allFibres[][];
	
	public final static double frontLensFront[] = { -5.71908984375, 1.4008873291015625, 1.161470703125 };
	
	//final static double dir[] = Util.reNorm(Util.minus(p1, p2));
	public  final static double opticAxis[] = { -0.58785042,  0.4946133 ,  0.64014807 };

	public static final String lightPathsSystemName = "FIXME_QSZ_AEN31";
	
	public double fibrePlaneNorm[], fibrePlaneRight[], fibrePlaneUp[];
	
	
	// ****************** optic system constructor *******************
	public AEN31AsDesignInPlace() {
		super();

		double centre[] = {0,0,0};
		
		// **************** rotate the optical system *****************
		double y[] = Util.createPerp(opticAxis);
		double z[] = Util.reNorm(Util.cross(opticAxis, y));
		rotate(centre, Algorithms.rotationMatrix(new double[][]{ opticAxis, y, z }));

		double shiftVec[] = Util.minus(frontLensFront, s1Pos);

		shift(shiftVec);
		
		fibrePlaneNorm = opticAxis.clone();
		fibrePlaneRight = Util.reNorm(Util.minus(edgeTSFibres[1], edgeTSFibres[0]));
		fibrePlaneUp = Util.reNorm(Util.cross(fibrePlaneNorm, fibrePlaneRight));
		fibrePlaneRight = Util.reNorm(Util.cross(fibrePlaneUp, fibrePlaneNorm));

		allFibres = new double[edgeTSFibres.length + bremstrahlungFibres.length][];
		System.arraycopy(edgeTSFibres, 0, allFibres, 0, edgeTSFibres.length);
		System.arraycopy(bremstrahlungFibres, 0, allFibres, edgeTSFibres.length, bremstrahlungFibres.length);
		
	}
	
	public String getDesignName() { return "aen31-asDesign-inPlace";	}
}
