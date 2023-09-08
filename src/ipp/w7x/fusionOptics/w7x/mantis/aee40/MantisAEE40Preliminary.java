package ipp.w7x.fusionOptics.w7x.mantis.aee40;

import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Interface;
import fusionOptics.types.Surface;
import ipp.w7x.fusionOptics.w7x.cxrs.ObservationSystem;

public class MantisAEE40Preliminary extends ObservationSystem {
	
	public String getDesignName() { return "mantis1"; }
	
	@Override
	protected String lightPathsSystemName() { return "MANTIS";	}
	@Override
	protected String[] lightPathRowNames() { return new String[] { "row1" };	}
	
	// 667.8, 728.1, 706.5
	public double designWavelenth = 700e-9;
	public double collectionNA = 0.1;
	public double pixelDiameter = 6e-6; 
	
	//----  global definitinos and specific points of interest -----
	public double[] globalUp = { 0, 0, 1 };
	
	public double[] aei31Port = { -4.531, 2.095, 0.850  };
	public double[] aej31Port = { -5.245, 2.177, 0.922  };
	
	public double[] portAxis = Util.reNorm(new double[]{ 0.877919934, 0.478807466, 0});
	public double[] portFlangeCentre = { -7.213338623046875, -3.93402978515625, -0.0999970703125 };
	
	// ------ helium beam and target of view -----
	public double[] beamAxis = Util.reNorm(Util.minus(aej31Port, aei31Port));
	public double[] beamCentre = Util.mul(Util.plus(aej31Port, aei31Port), 0.5);
	public double beamRadius = 0.01;
	public double beamLength = Util.length(Util.minus(aej31Port, aei31Port));
	
	public double[] targetObsPos = beamCentre;
	
	public double[][][] overrideObsPositions = null;//{{ targetObsPos }}; 

	public Cylinder heliumBeamCyld = new Cylinder("heliumBeamCyld", beamCentre, beamAxis, beamRadius, beamLength, NullInterface.ideal());
	
	
	/// ----- mirror ------
	
	public double mirrorDepthFromFlange = 1.700;
	public double mirrorWidth = 0.100;
	public double mirrorHeight = 0.100;
	
	public double[] mirrorCentre = Util.plus(portFlangeCentre, Util.mul(portAxis, mirrorDepthFromFlange));
	public double[] observationVector = Util.reNorm(Util.minus(targetObsPos, mirrorCentre));
	
	
	public double[] mirrorNormal = Util.reNorm(Util.plus(Util.mul(portAxis, -1.0), observationVector));
	
	public Square mirror = new Square("mirror", mirrorCentre, mirrorNormal, globalUp, mirrorHeight, mirrorWidth, Reflector.ideal());
	
	
	/// ----- aperture -----
	
	public double apertureFromMirror = 0.05;
	public double apertureDiameter = 0.03;
	
	
	public double[] aperturePos = Util.plus(mirrorCentre, Util.mul(observationVector, apertureFromMirror));
	public Iris aperture = new Iris("aperture", aperturePos, observationVector, 10*apertureDiameter/2, apertureDiameter/2, null, null, Absorber.ideal());
	
	/// ----- imagePlane ------

	public double imagePlaneDepthFromFlange = 1.000;
	public double imagePlaneWidth = 0.300;
	public double imagePlaneHeight = 0.300;
	public double[] imagePlaneCentre = Util.plus(portFlangeCentre, Util.mul(portAxis, imagePlaneDepthFromFlange));
	
	public Square imagePlane = new Square("imagePlane", imagePlaneCentre, portAxis, globalUp, imagePlaneHeight, imagePlaneWidth, Absorber.ideal());
	
	
	
	// things that LightAssesment etc scripts want
	public Square fibrePlane = imagePlane;
	public Surface entryWindowFront = mirror;
	public Surface tracingTarget = mirror;
	public int beamIdx[] = { 0 };
	public double getFibreNA(int iB, int iP) { return collectionNA; }

	public double getFibreDiameter(int i, int j) { return pixelDiameter; }

	
	public MantisAEE40Preliminary() { 
		super("MANTIS");
		
		addElement(heliumBeamCyld);
		addElement(aperture);
		addElement(mirror);
		addElement(imagePlane);
		
	}
}
