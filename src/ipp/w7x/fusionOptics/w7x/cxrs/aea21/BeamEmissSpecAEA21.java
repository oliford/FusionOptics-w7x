package ipp.w7x.fusionOptics.w7x.cxrs.aea21;

import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.materials.BK7;
import fusionOptics.materials.Sapphire;
import fusionOptics.optics.STLMesh;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAEA21 extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ e_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public double portNormal[] = { 0.7725425415039062, 2.3776411743164063, 1.52587890625e-08 };	// roughly , from CAD
	public double portEntryPos[] = new double[] { 2.0940919189453124, 6.0920563964843755, 0.3777649383544922 };  //point roughly in middle of end of immersion tube
	
	public double virtualObsPos[] = { -0.9556435146926046,	6.200182024233047,	-0.16812844867308413 }; //closest approach of all LOSs, from lightAssesment (not yet known)
	
	/***** Observation target ****/
	//public int targetBeamIdx = 6; // 6 = Q7 = K21 lower radial   
	public double targetBeamR = 5.75;
	//public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double targetObsPos[] = W7xNBI.def().getPosOfBoxAxisAtR(1, targetBeamR);
	public double sourceNormal[] =  Util.reNorm(Util.minus(targetObsPos, portEntryPos));
	
	//directions perp to port, sidesways and toward/away from source 
	public double portSourcePerp[] = Util.reNorm(Util.cross(sourceNormal, portNormal));
	public double portSourcePlane[] = Util.reNorm(Util.cross(portNormal, portSourcePerp));
	
	public double opticsTiltInPortSideways = 0 * Math.PI / 180;
	public double opticsTiltInPortToSource = 0 * Math.PI / 180; //+ve is away from source
	public double opticAxisA[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePerp, opticsTiltInPortToSource), portNormal));
	public double opticAxis[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePlane, opticsTiltInPortSideways), opticAxisA));
	
	/**** Mirror *****/
	public double mirrorDistIntoPort = 0.020;
	public double mirrorDistAwayFromSource = 0.000; //~toroidally in vessel
	public double mirrorDistSidewaysInPort = 0.000; //up/down in vessel
	public double mirrorPos0[] = Util.plus(portEntryPos, Util.mul(opticAxis, mirrorDistIntoPort)); 
	public double mirrorPos[] = Util.plus(Util.plus(mirrorPos0, 
													Util.mul(portSourcePlane, -mirrorDistAwayFromSource)),
													Util.mul(portSourcePerp, mirrorDistSidewaysInPort));
	
	public double mirrorRotationInPlane = 0 * Math.PI / 180;
	public double mirrorWidth = 0.160; // [Made up ]
	public double mirrorHeight = 0.110;
					
	public double mirrorNormal[] = Util.reNorm(Util.plus(sourceNormal, opticAxis));

	public double mirrorA[] = Util.reNorm(Util.cross(sourceNormal, opticAxis));
	public double mirrorB[] =  Util.reNorm(Util.cross(mirrorA, mirrorNormal));
	public double mirrorX[] = Util.reNorm(Util.plus(Util.mul(mirrorA, FastMath.cos(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.sin(mirrorRotationInPlane))));
	public double mirrorY[] = Util.reNorm(Util.plus(Util.mul(mirrorA, -FastMath.sin(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.cos(mirrorRotationInPlane))));
	
	public Square mirror = new Square("mirror", mirrorPos, mirrorNormal, mirrorX, mirrorHeight, mirrorWidth, Reflector.ideal());
	
	//public NodesAndElementsMesh shieldTiles = new2 NodesAndElementsMesh("shield", "/work/ipp/w7x/cad/shield-m2", mirrorPos, 0.150);
	
	/***** Entry Window *****/
	public double windowDistBehindMirror = 0.080;
	public double entryWindowDiameter = 0.098; // DN100CF=98mm, DN63CF=68mm
	public double entryWindowThickness = 0.010; // [Made up]
	
	public double entryWindowFrontPos[] = Util.plus(mirrorPos, Util.mul(opticAxis, windowDistBehindMirror));
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	/**** Main Lens *****/
	public double lensDistBehindWindow = 0.040;
	public double lensDiameter = 0.120 + 0.001;
	
	public double lensCentrePos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, lensDistBehindWindow));
	
	
	public double focalLength = 0.200; // Would be better, NA~0.33, much better focus
	
	public Medium lensMedium = new Medium(new BK7());  // [J.Balzhuhn's eBANF for the lens]
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lensCentrePos,
											opticAxis,
											lensDiameter/2, // radius
											focalLength, // focal length
											0.020, // centreThickness [ fromDesigner CAD for glass - although it's curvature doesn't match Jurgen's eBANF's focal length] 
											lensMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	
	public double lensIrisPos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, -0.002));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lensDiameter, lensDiameter*0.48, null, null, Absorber.ideal());
	
	
	/**** Lens2 *****/
	public double lens2DistBehindLens1 = 0.100;
	public double lens2Diameter = 0.120 + 0.001;
	
	public double lens2CentrePos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, lens2DistBehindLens1));
	
	public double focalLength2 = 0.200; // Would be better, NA~0.33, much better focus
	
	public Medium lens2Medium = new Medium(new BK7());  // [J.Balzhuhn's eBANF for the lens]
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens2",
											lens2CentrePos,
											opticAxis,
											lens2Diameter/2, // radius
											focalLength2, // focal length
											0.020, // centreThickness [ fromDesigner CAD for glass - although it's curvature doesn't match Jurgen's eBANF's focal length] 
											lens2Medium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lens2IrisPos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, -0.002));
	public Iris lens2Iris = new Iris("lens2Iris", lens2IrisPos, opticAxis, lens2Diameter, lens2Diameter*0.48, null, null, Absorber.ideal());
	

	/*** Fibres ****/
	public int beamIdx[] = { W7xNBI.BEAM_Q8 };
	
	public double fibre1EndPos[] = { -2.89649, -4.54998, 1.44268 }; // core channel, [fromDesigner-20151106] 
	public double fibre10EndPos[] = { -2.85912, -4.50896, 1.43616 }; // edge channel,  [fromDesigner-20151106]
	
	public double[][] channelR = { 
			{ 5.2, 5.294, 5.389, 5.483, 5.578, 5.672, 5.767, 5.861, 5.956, 6.05, }, 
		}; 
		public double[][][] fibreEndPos = { { 
					{ 2.212450505867738, 6.360123375539141, 0.3791953857130034 },
					{ 2.209172032119978, 6.363621127936311, 0.3789338304501974 },
					{ 2.205546709501264, 6.36703398041381, 0.3787691762517194 },
					{ 2.201314513860204, 6.369652799926165, 0.37855543132609754 },
					{ 2.1971487716213063, 6.373172962425379, 0.37828072005694663 },
					{ 2.1922700619865645, 6.375652617277898, 0.3780266999493186 },
					{ 2.186786331927945, 6.376982917246934, 0.3777314073576372 },
					{ 2.181156448565602, 6.378378541994496, 0.3774733527295074 },
					{ 2.1754702567383712, 6.379920944802096, 0.37719177200884213 },
					{ 2.169371725198904, 6.380500779046577, 0.376888084720784 },
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				}, 	};


	public double[] channelZ;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.28; // [ written on the fibre bundle packing reel ]
	
	public double fibreEndDiameter = 0.001; // roughly 1mm diameter [ looking at the fibres, and it agrees with 540x total / 10 = 54x per bundle. 54x * jacket size = ~1mm area ]
	public double fibrePlaneBehindLens2 = 0.240;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public double beamAxis[] = W7xNBI.def().uVec(0);
	
	public double fibrePlanePos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, fibrePlaneBehindLens2)); 
	public double fibresXVec[] = Util.reNorm(Util.cross(Util.cross(beamAxis, opticAxis),opticAxis));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, opticAxis));	
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, opticAxis, fibresYVec, 0.300, 0.300, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(opticAxis, 0.050)), 
										opticAxis, fibresYVec, 0.300, 0.300, Absorber.ideal());


	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.600, NullInterface.ideal());

	public Element tracingTarget = mirror;
		
	public BeamEmissSpecAEA21() {
		super("beamSpec-aea21");
		
		//addElement(new STLMesh("panel", "/work/ipp/w7x/cad/aea21/panel-cutting-aea21-edge-channels-cut-front.stl", portEntryPos, 0.500));
		
		addElement(mirror);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lensIris);
		addElement(lens1);
		addElement(lens2Iris);
		addElement(lens2);
		addElement(fibrePlane);
		addElement(beamPlane);
		//addElement(shieldTiles);
		
		setupFibrePlanes();
		
		/*
		fibreEndPos = new double[nFibres][];
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
		}
		*/
		/*fibrePlanes = new Square[nFibres];
		for(int i=0; i < nFibres; i++){
			//double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
			double norm[] = fibreEndNorm[i];
			double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
			double y[] = Util.reNorm(Util.cross(x, norm));
			fibrePlanes[i] = new Square("fibrePlane_" + i, fibreEndPos[i], norm, y, 0.007, 0.007, NullInterface.ideal());
			addElement(fibrePlanes[i]);
		}*/
			
		addElement(catchPlane);
		
		channelZ = new double[ channelR.length];
		for(int i=0; i < channelR.length; i++){
			channelZ[i] = W7xNBI.def().getPosOfBoxAxisAtR(0, channelR[0][i])[2];
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());		
	}

	public String getDesignName() { return "aea21";	}

	public Element[] makeSimpleModel() {
		return new Element[0];
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
		

}
