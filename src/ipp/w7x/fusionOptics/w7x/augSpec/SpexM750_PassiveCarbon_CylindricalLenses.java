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
import fusionOptics.optics.CylindricalPlanarConvexLens;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.CyldDish;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Dish;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.surfaces.TruncatedCylinder;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;
import net.jafama.FastMath;

public class SpexM750_PassiveCarbon_CylindricalLenses extends Optic {
	public double designWavelenth = 529e-9;
	
	
	public double axisHeight = 0.200;

	public double inputMirrorFocalLength = 0.750;
	public double inputMirrorFNumber = 6.0;
	
	public double outputMirrorFocalLength = 0.750;
	public double outputMirrorFNumber = 6.0;
	
	public double separation = 0.150;
	
	public double inputAngle = FastMath.asin(separation / inputMirrorFocalLength);
	
	public double gratingAxisX = 0;
	public double gratingAxisY = -(inputMirrorFocalLength - Math.sqrt(inputMirrorFocalLength*inputMirrorFocalLength - separation*separation));
	
	public double gratingHeight = 0.110;
	public double gratingWidth = 0.110;
	public double gratingDensity = 2400e3;
	
	public int nFibres = 40;
	public double fibreDiameter = 400e-6;
	public double fibreSpacing = 450e-6;
	public double fibreEffectiveNA = 0.22;
	
	public double ccdWidth = 512 * 16e-6;
	public double ccdHeight = 512 * 16e-6;
		
	
	public double ccdBlockingStripDistance = 0.0125;
	public double blockingStripWidth = 0.003;
	

	//setup of spectrometer, assuming inputAngle = outputAngle
	public int diffractionOrder = 1;	
	public double gratingAngle = -FastMath.asin(diffractionOrder * designWavelenth * gratingDensity / 2 / FastMath.cos(inputAngle));
	//public double gratingAngle = 0 * Math.PI/ 180;
			
	
	public double gratingPos[] = { gratingAxisX, gratingAxisY, axisHeight };
	public double globalUp[] = { 0, 0, 1 };
	
	public double fibreCentrePos[] = { -separation, 0, axisHeight };	
	public double ccdCentrePos[] = { +separation, 0.007, axisHeight };
	
	public double inputMirrorPos[] = { -separation, -inputMirrorFocalLength, axisHeight };
	public double outputMirrorPos[] = { separation, -inputMirrorFocalLength, axisHeight };
	
	public double inputAxis[] = Util.reNorm(Util.minus(fibreCentrePos, inputMirrorPos));
	public double outputAxis[] = Util.reNorm(Util.minus(ccdCentrePos, outputMirrorPos));
	
	public Square ccd = new Square("ccd", ccdCentrePos, outputAxis, globalUp, ccdHeight, ccdWidth, Absorber.ideal());
	
	public double fibrePos[][];
	public Cylinder fibres[];
	
	public DiffractionGrating gratingInterface = new DiffractionGrating(diffractionOrder, gratingDensity, globalUp);
	//public Reflector gratingInterface = Reflector.ideal();
	public Square grating = new Square("grating", gratingPos.clone(), new double[]{ 0, 1, 0 }, globalUp, gratingHeight, gratingWidth, gratingInterface);
	
	
	public double lensCentreThickness = 0.020;
	
	public double inputMirrorDiameter = inputMirrorFocalLength / inputMirrorFNumber;
	public double outputMirrorDiameter = outputMirrorFocalLength / outputMirrorFNumber;
	
	public double mirrorRadCurv = inputMirrorFocalLength*2;
	
	
	
	public double inputMirrorNormal[] = Util.reNorm(Util.plus(Util.reNorm(Util.minus(gratingPos, inputMirrorPos)), inputAxis));
	public double outputMirrorNormal[] = Util.reNorm(Util.plus(Util.reNorm(Util.minus(gratingPos, outputMirrorPos)), outputAxis));
	
	public Dish inputMirror = new Dish("inputMirror", inputMirrorPos, inputMirrorNormal, mirrorRadCurv, inputMirrorDiameter/2, Reflector.ideal());
	public Dish outputMirror = new Dish("outputMirror", outputMirrorPos, outputMirrorNormal, mirrorRadCurv, outputMirrorDiameter/2, Reflector.ideal());
	
	//Thor Labs LK1069L1-A [https://www.thorlabs.de/thorproduct.cfm?partnumber=LK1069L1-A]
	public double lens1DistFromCCD = -0.100;
	public double lens1Height = 0.030; 
	//public double lens1FocalLength = -0.200;
	public double lens1RadCurv = -0.1034;
	public double lens1CentreThickness = 0.003;
	public double lens1Length = 0.032;
	
	public double lens1Centre[] = Util.plus(ccdCentrePos, Util.mul(outputAxis, lens1DistFromCCD));
	
	public Medium lens1Medium = new Medium(new BK7());
	public CylindricalPlanarConvexLens lens1 = CylindricalPlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens1", 
			lens1Centre, outputAxis, globalUp, lens1Height/2, lens1RadCurv, 
			lens1CentreThickness, lens1Length, 
			lens1Medium, IsoIsoInterface.ideal());
	
	public Iris lens1Iris = new Iris("lens1Iris", lens1Centre, outputAxis, 1.5 * lens1Height/2, lens1Height*0.99/2, Absorber.ideal());
			

	//Thor Labs LJ1629L1-A [https://www.thorlabs.de/thorproduct.cfm?partnumber=LJ1629L1-A]
	public double lens2DistFromCCD = -0.055;
	public double lens2Height = 0.030; 
	//public double lens2FocalLength = 0.150;
	public double lens2RadCurv = 0.0775;
	public double lens2CentreThickness = 0.0045;
	public double lens2Length = 0.032;
	
	public double lens2Centre[] = Util.plus(ccdCentrePos, Util.mul(outputAxis, lens2DistFromCCD));
	
	public Medium lens2Medium = new Medium(new BK7());
	public CylindricalPlanarConvexLens lens2 = CylindricalPlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens2", 
			lens2Centre, outputAxis, globalUp, lens2Height/2, lens2RadCurv, 
			lens2CentreThickness, lens2Length, 
			lens2Medium, IsoIsoInterface.ideal());
			
	

	//Thor Labs LJ1629L1-A [https://www.thorlabs.de/thorproduct.cfm?partnumber=LJ1629L1-A]
	public double lens3DistFromCCD = -0.038;
	public double lens3Height = 0.030; 
	//public double lens3FocalLength = 0.150;
	public double lens3RadCurv = 0.0775;
	public double lens3CentreThickness = 0.0045;
	public double lens3Length = 0.032;
	
	public double lens3Centre[] = Util.plus(ccdCentrePos, Util.mul(outputAxis, lens3DistFromCCD));
	
	public Medium lens3Medium = new Medium(new BK7());
	public CylindricalPlanarConvexLens lens3 = CylindricalPlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens3", 
			lens3Centre, outputAxis, globalUp, lens3Height/2, lens3RadCurv, 
			lens3CentreThickness, lens3Length, 
			lens3Medium, IsoIsoInterface.ideal());
			
	

	//Thor Labs LJ1629L1-A [https://www.thorlabs.de/thorproduct.cfm?partnumber=LJ1629L1-A]
	public double lens4DistFromCCD = -0.023;
	public double lens4Height = 0.030; 
	//public double lens4FocalLength = 0.150;
	public double lens4RadCurv = 0.0775;
	public double lens4CentreThickness = 0.0045;
	public double lens4Length = 0.032;
	
	public double lens4Centre[] = Util.plus(ccdCentrePos, Util.mul(outputAxis, lens4DistFromCCD));
	
	public Medium lens4Medium = new Medium(new BK7());
	public CylindricalPlanarConvexLens lens4 = CylindricalPlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens4", 
			lens4Centre, outputAxis, globalUp, lens4Height/2, lens4RadCurv, 
			lens4CentreThickness, lens4Length, 
			lens4Medium, IsoIsoInterface.ideal());
			
	
	/*public double cyld1DistFromCCD = -0.100;
	public double cyld1Thickness = 0.010;
	public double cyld1FrontCentre[] = Util.plus(ccdCentrePos, Util.mul(outputAxis, cyld1DistFromCCD));
	public double cyld1BackCentre[] = Util.plus(cyld1FrontCentre, Util.mul(outputAxis, cyld1Thickness));
	
	public double cyld1Radius = 0.050;
	public double cyld1Height = 0.050;
	public double cyld1Length = 0.070;
	
	public Medium cyld1Medium = new Medium(new BK7());
	
	public double cyld1Axis[] = Util.reNorm(Util.cross(outputAxis, globalUp));
	public TruncatedCylinder cyld1Front = new TruncatedCylinder("cyld1Front", cyld1FrontCentre, cyld1Axis, globalUp, cyld1Radius, cyld1Length, cyld1Height, cyld1Medium, null, IsoIsoInterface.ideal());
	
	public double rimRadius = 0.050;
	
	//public CyldDish cyld1Front = new CyldDish("cyld1Front", cyld1FrontCentre, outputAxis, globalUp, cyld1Radius, cyld1Width, cyld1Height, null, cyld1Medium, IsoIsoInterface.ideal());
	public Square cyld1Back = new Square("cyld1Back", cyld1BackCentre, outputAxis, globalUp, cyld1Height, cyld1Length, cyld1Medium, null, IsoIsoInterface.ideal());
	public Optic cyld1 = new Optic("cyld1", new Element[] { cyld1Front, cyld1Back }); 
	*/
	public double[] blockingStripPos = Util.plus(ccdCentrePos, Util.mul(outputAxis, -ccdBlockingStripDistance));
	public Square blockingStrip = new Square("blockingStrip", blockingStripPos, outputAxis, globalUp, 0.050, blockingStripWidth, Absorber.ideal());
	//public Square blockingStrip = new Square("blockingStrip", blockingStripPos, outputAxis, globalUp, 0.050, 0.006, Absorber.ideal());
			
	public SpexM750_PassiveCarbon_CylindricalLenses() {
		super("SpexM750");
		//gratingAngle=0;
		System.out.println("Grating angle = " + gratingAngle * 180 / Math.PI + " deg");
		
		//inputLens.rotate(inputLensPos, Algorithms.rotationMatrix(globalUp, Math.PI/2 + inputAngle));
		//outputLens.rotate(outputLensPos, Algorithms.rotationMatrix(globalUp, Math.PI/2 - outputAngle));
		
		grating.rotate(gratingPos, Algorithms.rotationMatrix(globalUp, -gratingAngle));
		
		addElement(inputMirror);		
		addElement(grating);
		addElement(outputMirror);
		//addElement(blockingStrip);
		addElement(lens1Iris);
		addElement(lens1);
		addElement(lens2);
		addElement(lens3);
		addElement(lens4);
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
