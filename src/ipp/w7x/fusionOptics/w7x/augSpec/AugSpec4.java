package ipp.w7x.fusionOptics.w7x.augSpec;

import algorithmrepository.Algorithms;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.DiffractionGrating;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.Nikon135mmF28;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.lenses.ThorLabs100mmAspheric;
import fusionOptics.materials.BK7;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;
import jafama.FastMath;

public class AugSpec4 extends Optic {
	public double designWavelenth = 656e-9;
	
	public double inputAngle = 15 * Math.PI / 180; 
	public double outputAngle = 15 * Math.PI / 180;
	
	public double axisHeight = 0.200;
	
	public double gratingAxisX = 0;
	public double gratingAxisY = -0.3007383422851563;
	
	public double gratingHeight = 0.110;
	public double gratingWidth = 0.110;
	public double gratingDensity = 2400e3;
	
	public int nFibres = 45;
	public double fibreDiameter = 400e-6;
	public double fibreSpacing = 440e-6;
	public double fibreEffectiveNA = 0.22;
	
	public double ccdWidth = 1024 * 13e-6;
	public double ccdHeight = 1024 * 13e-6;
		
	public double gratingFibreDistance = 0.466;
	
	public double inputLensFocalLength = 0.300;
	public double inputLensFNumber = 2.8;
	
	public double outputLensFocalLength = 0.200;
	public double outputLensFNumber = 2.0;
	
	public double gratingCCDDistance = 0.4703;
	
	public double ccdBlockingStripDistance = 0.0125;
	public double blockingStripWidth = 0.003;
	

	//setup of spectrometer, assuming inputAngle = outputAngle
	public int diffractionOrder = -1;	
	public double gratingAngle = FastMath.asin(diffractionOrder * designWavelenth * gratingDensity / 2 / FastMath.cos(inputAngle));
			
	
	public double gratingPos[] = { gratingAxisX, gratingAxisY, axisHeight };
	public double globalUp[] = { 0, 0, 1 };
			 
	
	public double inputAxis[] = { -FastMath.sin(inputAngle), FastMath.cos(inputAngle), 0}; //from grating towards fibres
	public double outputAxis[] = { FastMath.sin(outputAngle), FastMath.cos(outputAngle), 0}; //from grating towards CCD
	
	public double fibreCentrePos[] = Util.plus(gratingPos, Util.mul(inputAxis, gratingFibreDistance));
	
	public double ccdCentrePos[] = Util.plus(gratingPos, Util.mul(outputAxis, gratingCCDDistance));
	
	public Square ccd = new Square("ccd", ccdCentrePos, outputAxis, globalUp, ccdHeight, ccdWidth, Absorber.ideal());
	
	public double fibrePos[][];
	public Cylinder fibres[];
	
	public DiffractionGrating gratingInterface = new DiffractionGrating(diffractionOrder, gratingDensity, globalUp);	
	public Square grating = new Square("grating", gratingPos.clone(), new double[]{ 0, 1, 0 }, globalUp, gratingHeight, gratingWidth, gratingInterface);
	
	public double lensCentreThickness = 0.020;
	public double inputLensPos[] = Util.plus(gratingPos, Util.mul(inputAxis, gratingFibreDistance - inputLensFocalLength));
	public double outputLensPos[] = Util.plus(gratingPos, Util.mul(outputAxis, gratingCCDDistance - outputLensFocalLength));
	
	public double inputLensDiameter = inputLensFocalLength / inputLensFNumber;
	public double outputLensDiameter = outputLensFocalLength / outputLensFNumber;
	

	/*public Medium inputLensMedium = new Medium(new BK7()); 
	public SimplePlanarConvexLens inputLens = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
			"inputLens",
			inputLensPos,
			inputAxis,
			inputLensDiameter/2, // radius
			inputLensFocalLength, // focal length
			lensCentreThickness, // centreThickness  
			inputLensMedium, 
			IsoIsoInterface.ideal(),
			designWavelenth);//*/
	
	public Nikon135mmF28 inputLens = new Nikon135mmF28(inputLensPos, inputLensFocalLength / 0.135);	
	//public Iris inputLensIris = new Iris("inputLensIris", inputLensPos, inputAxis, 0.6 * inputLensDiameter, 0.499*inputLensDiameter, Absorber.ideal());
	
	public Iris inputLensIris = new Iris("inputLensIris", 
			Util.plus(gratingPos, Util.mul(inputAxis, gratingFibreDistance - 0.132)), 
			inputAxis, 
			0.6 * inputLensDiameter, 
			0.057/2, Absorber.ideal());
	
	/*public Medium outputLensMedium = new Medium(new BK7());  
	public SimplePlanarConvexLens outputLens = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
			"outputLens",
			outputLensPos,
			outputAxis,
			outputLensDiameter/2, // radius
			outputLensFocalLength, // focal length
			lensCentreThickness, // centreThickness  
			outputLensMedium, 
			IsoIsoInterface.ideal(),
			designWavelenth);//*/
	
	//public Nikon50mmF11 outputLens = new Nikon50mmF11(outputLensPos, outputLensFocalLength / 0.050);		
	//public Nikon135mmF28 outputLens = new Nikon135mmF28(outputLensPos, outputLensFocalLength / 0.135);
	public ThorLabs100mmAspheric outputLens = new ThorLabs100mmAspheric(outputLensPos.clone(), new double[]{ 1,0,0 }, outputLensFocalLength / 0.0917);
	public Iris outputLensIris = new Iris("outputLensIris", outputLensPos, outputAxis, 0.7 * outputLensDiameter, 0.070/2, Absorber.ideal());
	
	public double[] blockingStripPos = Util.plus(ccdCentrePos, Util.mul(outputAxis, -ccdBlockingStripDistance));
	public Square blockingStrip = new Square("blockingStrip", blockingStripPos, outputAxis, globalUp, 0.050, blockingStripWidth, Absorber.ideal());
	//public Square blockingStrip = new Square("blockingStrip", blockingStripPos, outputAxis, globalUp, 0.050, 0.006, Absorber.ideal());
			
	public AugSpec4() {
		super("AugSpec4");
		//gratingAngle=0;
		System.out.println("Grating angle = " + gratingAngle * 180 / Math.PI + " deg");
		
		inputLens.rotate(inputLensPos, Algorithms.rotationMatrix(globalUp, Math.PI/2 + inputAngle));
		outputLens.rotate(outputLensPos, Algorithms.rotationMatrix(globalUp, Math.PI/2 - outputAngle));
		
		grating.rotate(gratingPos, Algorithms.rotationMatrix(globalUp, -gratingAngle));
		
		addElement(inputLensIris);
		addElement(inputLens);
		addElement(grating);
		addElement(outputLensIris);
		addElement(outputLens);
		addElement(blockingStrip);
		addElement(ccd);
		
		double fibreLength = 0.030;
		
		fibres = new Cylinder[nFibres];
		fibrePos = new double[nFibres][];		
		for(int i=0; i < nFibres; i++){
			fibrePos[i] = Util.plus(fibreCentrePos, Util.mul(globalUp, (i - (0.5*nFibres))*fibreSpacing));
			double fibreCyldPos[] = Util.plus(fibrePos[i], Util.mul(inputAxis, fibreLength/2)); 
			fibres[i] = new Cylinder("fibre"+i, fibreCyldPos, inputAxis, fibreDiameter/2, fibreLength, NullInterface.ideal());
			addElement(fibres[i]);
		}
	}

}
