package ipp.w7x.fusionOptics.w7x.multiplexer;

import org.apache.commons.math3.util.FastMath;

import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.materials.BK7;
import fusionOptics.materials.OharaS_LAH64;
import fusionOptics.optics.SimplePlanarAsphericLens;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Aspheric;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

public class Multiplexer extends Optic {
	public double designWavelength = 656e-9;

	public double[] commonFibrePos = { 0, 0, 0 };
	public double[] up = { 0, 0, 1 };

	public double mirrorHeight = 0.018;
	public double mirrorDiameter = 0.020;
	

	//public SimplePlanarConvexLens lens = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens", lensPos, up, lensDiameter/2, lensFocalLength, lensThickness, lensMedium, IsoIsoInterface.ideal(), designWavelength);
	final static double rescale = 1e-3; //everything here is in mm, we want m
	
	// Thor Labs AL2520-A (S-LAH64)
	/*
	public double lensFocalLength = 0.020;
	public double lensHeight = lensFocalLength + 0.0076;
	public double lensDiameter = 0.025;
	public double lensThickness = 0.0076;	
	public double[] lensPos = { 0, 0, lensHeight };
	public Medium lensMedium = new Medium(new OharaS_LAH64());
	
	public SimplePlanarAsphericLens lens = SimplePlanarAsphericLens.fromRadiusOfCurvAndCentreThickness(
			"lens", lensPos, up, lensDiameter/2,
			15.54 * rescale, //rad curv
			lensThickness, 
			-1.35,
			Aspheric.rescaleCoeffs(new double[] { 
					0, 2.3618134e-05, -1.1303079e-08, -1.1113906e-11, -2.3981714e-14, 3.035791e-17, 1.3660815e-19, -1.8881587e-22}, rescale), 
			lensMedium, IsoIsoInterface.ideal());
	//*/
	
	// Thor Labs AL2520-A (S-LAH64)
		public double lensFocalLength = 0.010;
		public double lensHeight = lensFocalLength + 0.004;
		public double lensDiameter = 0.0125;
		public double lensThickness = 0.0043;	
		public double[] lensPos = { 0, 0, lensHeight };
		public Medium lensMedium = new Medium(new OharaS_LAH64());
		
		public SimplePlanarAsphericLens lens = SimplePlanarAsphericLens.fromRadiusOfCurvAndCentreThickness(
				"lens", lensPos, up, lensDiameter/2,
				7.77 * rescale, //rad curv
				lensThickness, 
				-1.0,
				Aspheric.rescaleCoeffs(new double[] { 
						0, 9.8464319e-05, -6.9905851e-08, -2.3874994e-09, -1.1328583e-11, 8.7255438e-14 ,2.8967313e-16,1.7632112e-18}, rescale), 
				lensMedium, IsoIsoInterface.ideal());
		//*/


	/*
	// Thor Labs AL1225-A	
	public double lensFocalLength = 0.025;
	public double lensHeight = lensFocalLength + 0.002;
	public double lensDiameter = 0.0125;
	public double lensThickness = 0.004;
	
	public double[] lensPos = { 0, 0, lensHeight };
	public Medium lensMedium = new Medium(new BK7());
	public SimplePlanarAsphericLens lens = SimplePlanarAsphericLens.fromRadiusOfCurvAndCentreThickness(
			"lens", lensPos, up, lensDiameter/2,
			12.780 * rescale, //rad curv
			lensThickness, 
			-0.6, 
			Aspheric.rescaleCoeffs(new double[] { 
					0, 1.843e-06, -3.817e-09, -2.435e-11, 3.173e-14, -3.700e-15, 6.511e-17, -4.960e-19 }, rescale), 
			lensMedium, IsoIsoInterface.ideal());
	//*/

	public int nInputFibres = 15;		
	public double fibreDiameter = 400e-6;
	public double fibreEffectiveNA = 0.22;

	public double inputFibreRadius = 0.0018;
	public double inputFibreAngle = FastMath.asin(inputFibreRadius / lensFocalLength);
	public double[] inputFibrePosition = { inputFibreRadius, 0, lensFocalLength * (1.0 - FastMath.cos(inputFibreAngle)) };
	
	public double[] mirrorPos = { 0, 0, mirrorHeight };
	
	public double[] inputFibreNormal = Util.reNorm(Util.minus(lensPos, inputFibrePosition));
	
	public double mirrorAngle = inputFibreAngle / 2 + 0.09*Math.PI/180;
	public double[] mirrorNormal = { -FastMath.sin(mirrorAngle), 0, FastMath.cos(mirrorAngle) };
	
	public Disc mirror = new Disc("mirror", mirrorPos, mirrorNormal, mirrorDiameter/2, null, null, Reflector.ideal());
	
	
	public Disc commonFibreEnd = new Disc("commonFibreEnd", commonFibrePos, up, fibreDiameter/2, null, null, NullInterface.ideal());
	
	
	public Disc[] inputFibreEnds;
	public Cylinder[] fibreCylds;
	public double fibreCyldLength = 0.040;
	
	
	public Iris returnCatch = new Iris("returnCatch", commonFibrePos, up, 0.020, 0.55 * fibreDiameter, null, null, NullInterface.ideal());
	
	
	public Multiplexer() {
		super("Multiplexer");

		addElement(commonFibreEnd);
		addElement(lens);
		addElement(mirror);
		addElement(returnCatch);
		
		inputFibreEnds = new Disc[nInputFibres];
		fibreCylds = new Cylinder[nInputFibres];
		for(int i=0; i < nInputFibres; i++) {
			double theta = i * 2*Math.PI / nInputFibres;
			double[] pos = { inputFibreRadius * FastMath.cos(theta), 
							inputFibreRadius * FastMath.sin(theta), 
							lensFocalLength * (1.0 - FastMath.cos(inputFibreAngle)) };
			double[] normal = Util.reNorm(Util.minus(lensPos, pos));
			inputFibreEnds[i] = new Disc("inputFibreEnd_"+i, pos, normal, fibreDiameter/2, null, null, NullInterface.ideal());
			Iris fibreEndIris = new Iris("fibreEndIris_"+i, pos, normal, fibreDiameter, fibreDiameter/2, Absorber.ideal());
			addElement(fibreEndIris);
			
			double[] cyldPos = Util.plus(pos, Util.mul(normal, -fibreCyldLength/2));
			fibreCylds[i] = new Cylinder("fibreCyld_"+i, cyldPos, normal, fibreDiameter/2, fibreCyldLength, Absorber.ideal());

			addElement(inputFibreEnds[i]);
			addElement(fibreCylds[i]);
		}
		
		
	}
}
