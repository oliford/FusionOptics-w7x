package ipp.w7x.fusionOptics.w7x.cxrs.aea21;

import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import net.jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.EdmundOptics50mmAspheric;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.lenses.ThorLabs100mmAspheric;
import fusionOptics.materials.BK7;
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
import fusionOptics.types.Surface;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAEA21U_CISDual extends Optic {
	
	public String lightPathsSystemName = "AEA21";
	
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ He_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public double portNormal[] = Util.reNorm(new double[]{ 0.7725425415039062, 2.3776411743164063, 1.52587890625e-08 });	// roughly , from CAD
	public double portEntryPos[] = new double[] { 2.0940919189453124, 6.0920563964843755, 0.3777649383544922 };  //point roughly in middle of end of immersion tube
	
	public double virtualObsPos[] = { 2.1224104306465854,	6.106432410473533,	0.3768450856770087 }; //closest approach of all LOSs, from lightAssesment (not yet known)
	
	/***** Observation target ****/
	//public int targetBeamIdx = 6; // 6 = Q7 = K21 lower radial   
	public double targetBeamR = 5.55;
	public double targetAboveBeam = -0.200; //reversed to K20, because we setup on K21 and flip
	//public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double targetObsPos0[] = W7xNBI.def().getPosOfBoxAxisAtR(1, targetBeamR);
	public double targetObsPos[] = Util.plus(targetObsPos0, Util.mul(globalUp, targetAboveBeam));	
	public double sourceNormal[] =  Util.reNorm(Util.minus(targetObsPos, portEntryPos));
	
	public double overrideObsPositions[][][] = {
			{
				{ 4.923, 3.890,  0.103}, //left
				{ 2.690, 4.430, -0.215}, //right
				{ 3.278, 4.578,  0.323}, //top
				{ 4.066, 3.249, -0.700}, //bottom
				{ 3.777, 4.341, 0.450 }, //behind top bar - shield
				{ 4.059, 4.544, 0.550 }, //behind top bar - divertor
			}
		};
	
	//directions perp to port, sidesways and toward/away from source 
	public double portSourcePerp[] = Util.reNorm(Util.cross(sourceNormal, portNormal));
	public double portSourcePlane[] = Util.reNorm(Util.cross(portNormal, portSourcePerp));
	
	public double opticsTiltInPortSideways = 0 * Math.PI / 180;
	public double opticsTiltInPortToSource = 0 * Math.PI / 180; //+ve is away from source
	public double opticAxisA[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePerp, opticsTiltInPortToSource), portNormal));
	public double opticAxis[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePlane, opticsTiltInPortSideways), opticAxisA));
	
	/**** Mirror *****/
	public double mirrorDistIntoPort = 0.010;
	public double mirrorDistAwayFromSource = 0.000; //~toroidally in vessel
	public double mirrorDistSidewaysInPort = 0.000; //up/down in vessel
	public double mirrorCentrePos0[] = Util.plus(portEntryPos, Util.mul(opticAxis, mirrorDistIntoPort)); 
	public double mirrorCentrePos[] = Util.plus(Util.plus(mirrorCentrePos0, 
													Util.mul(portSourcePlane, -mirrorDistAwayFromSource)),
													Util.mul(portSourcePerp, mirrorDistSidewaysInPort));
	
	public double mirrorRotationInPlane = 0 * Math.PI / 180;
	public double mirrorWidth = 0.120; // [Made up ]
	public double mirrorHeight = 0.060;
					
	public double mirrorNormal[] = Util.reNorm(Util.plus(sourceNormal, opticAxis));

	public double mirrorA[] = Util.reNorm(Util.cross(sourceNormal, opticAxis));
	public double mirrorB[] =  Util.reNorm(Util.cross(mirrorA, mirrorNormal));
	public double mirrorX[] = Util.reNorm(Util.plus(Util.mul(mirrorA, FastMath.cos(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.sin(mirrorRotationInPlane))));
	public double mirrorY[] = Util.reNorm(Util.plus(Util.mul(mirrorA, -FastMath.sin(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.cos(mirrorRotationInPlane))));
	
	//shift sideways to fit into shutter block
	public double mirrorCentrePos1[] = Util.plus(mirrorCentrePos, Util.mul(mirrorY, -0.005));

	public Square mirror = new Square("mirror", mirrorCentrePos1, mirrorNormal, mirrorX, mirrorHeight, mirrorWidth, Reflector.ideal());
	
	public final String backgroundSTLFiles[] = {
			"/home/oliford/rzg/w7x/cad/aea21/bg-targetting/baffle-m3.off-aea21-cut.stl",
			"/home/oliford/rzg/w7x/cad/aea21/bg-targetting/panel-m21.off-aea21-cut.stl",
			"/home/oliford/rzg/w7x/cad/aea21/bg-targetting/panel-m30.off-aea21-cut.stl",
			"/home/oliford/rzg/w7x/cad/aea21/bg-targetting/shield-m3.off-aea21-cut.stl",
	};
		
	//public NodesAndElementsMesh shieldTiles = new2 NodesAndElementsMesh("shield", "/work/ipp/w7x/cad/shield-m2", mirrorPos, 0.150);
	
	/***** Entry Window *****/
	public double windowDistBehindMirror = 0.068;
	public double entryWindowDiameter = 0.068; // DN100CF=98mm, DN63CF=68mm
	public double entryWindowThickness = 0.003; // [Made up]
	
	public double entryWindowFrontPos[] = Util.plus(mirrorCentrePos, Util.mul(opticAxis, windowDistBehindMirror));
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	double portRight[] = Util.reNorm(Util.cross(portNormal, globalUp));
	double portUp[] = Util.reNorm(Util.cross(portRight, portNormal));
	
	double lens1FocalLength = 0.016;
	double lens1BehindWindow = 0.025;
	double lens1Right = 0.000;
	double lens1Up = 0.016;
	
	double lens1CentrePos[] = Util.plus(Util.plus(Util.plus(
										entryWindowFrontPos, 
										Util.mul(portNormal, lens1BehindWindow)),
										Util.mul(portUp, lens1Up)),
										Util.mul(portRight, lens1Right));
	double lens1Normal[] = portNormal;
	// Some lens
	
	public Nikon50mmF11 lens1 = new Nikon50mmF11(lens1CentrePos, lens1FocalLength / 0.050, lens1Normal);

	
	double lens2FocalLength = lens1FocalLength;
	double lens2BehindWindow = lens1BehindWindow + 0.00001;
	double lens2Right = 0.000;
	double lens2Up = -0.016;

	double lens2CentrePos[] = Util.plus(Util.plus(Util.plus(
										entryWindowFrontPos, 
										Util.mul(portNormal, lens2BehindWindow)),
										Util.mul(portUp, lens2Up)),
										Util.mul(portRight, lens2Right));
	double lens2Normal[] = portNormal;
	// Some lens
	
	public Nikon50mmF11 lens2 = new Nikon50mmF11(lens2CentrePos, lens2FocalLength / 0.050, lens2Normal);

	/*** Fibres ****/
	public int beamIdx[] = null;
	//public double[][] channelR = null;
	//public double[][][] fibreEndPos = null;
	//public double[][][] fibreEndNorm = null;
	public String lightPathRowName[] = null;
	

	public double[] channelZ;
	
	public double[][] channelR = { 
			{ 6.05, 5.993, 5.918, 5.836, 5.748, 5.655, 5.558, 5.458, 5.355, 5.25, }, 
		}; 
		public double[][][] fibreEndPos = { { 
					{ 1.9273085953931133, 6.271372078363657, -0.39277190173415544 },
					{ 1.9197759826273333, 6.274334091455761, -0.3941205010000625 },
					{ 1.9248054643441062, 6.272229602817867, -0.39671195489784744 },
					{ Double.NaN, Double.NaN, Double.NaN },
					{ 1.9259890420237753, 6.271824984624961, -0.39616445692243085 },
					{ 1.9279623116357727, 6.271196346066052, -0.39616486181085797 },
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ -0.4670049493194494, -0.8831826235894602, -0.04352965312033171 },
				{ -0.38765055496194895, -0.9188393612666179, -0.07390044265663502 },
				{ -0.3575467782563375, -0.9276293361750503, 0.10800053715583505 },
				{ Double.NaN, Double.NaN, Double.NaN },
				{ -0.3993079176219959, -0.9123096041011269, 0.09079853076581391 },
				{ -0.5021555414781882, -0.8580089552447342, 0.10798353986875683 },
				}, 	};

	
	//public double fibreEndPos[][];
	//public double fibreNA = 0.53; // F/0.95
	public double fibreNA = 0.35; // F/1.4
	
	public double fibreEndDiameter = 0.0004; // 400um AUG-like fibres
	public double fibrePlaneBehindLens1 = lens1FocalLength;
	public double fibrePlaneBehindLens2 = lens2FocalLength;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public double beamAxis[] = W7xNBI.def().uVec(0);
	
	double fibrePlaneRotate = -40 * Math.PI / 180;
	
	public double fibrePlane1Pos[] = Util.plus(lens1CentrePos, Util.mul(opticAxis, fibrePlaneBehindLens1)); 
	public double fibresXVec0[] = Util.reNorm(Util.cross(Util.cross(beamAxis, opticAxis),opticAxis));
	public double fibresYVec0[] = Util.reNorm(Util.cross(fibresXVec0, opticAxis));
	
	public double fibresXVec[] = Util.plus(Util.mul(fibresXVec0, FastMath.cos(fibrePlaneRotate)), Util.mul(fibresYVec0, FastMath.sin(fibrePlaneRotate)));
	public double fibresYVec[] = Util.plus(Util.mul(fibresXVec0, -FastMath.sin(fibrePlaneRotate)), Util.mul(fibresYVec0, FastMath.cos(fibrePlaneRotate)));
	
	public Square fibrePlane1 = new Square("fibrePlane1", fibrePlane1Pos, opticAxis, fibresYVec, 0.020, 0.020, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public double fibrePlane2Pos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, fibrePlaneBehindLens2)); 
	public Square fibrePlane2 = new Square("fibrePlane2", fibrePlane2Pos, opticAxis, fibresYVec, 0.020, 0.020, NullInterface.ideal());
	
	public Square fibrePlane = fibrePlane1;
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlane1Pos, Util.mul(opticAxis, 0.050)), 
										opticAxis, fibresYVec, 0.300, 0.300, Absorber.ideal());

	public Disc strayPlane = new Disc("strayPlane", Util.plus(entryWindowFrontPos, Util.mul(portNormal, -0.300)),
									portNormal, 0.200, Absorber.ideal());
	
	//plane to cover up ports AEE30 and AEK30 for background targetting
	public double port30PlanePos[] = { -4.886710693359375, 4.122841796875, -0.09633171272277832 };
	public double port30PlaneNormal[] = { -0.73000834,  0.67882323, -0.07928962 };
	public double port30PlaneUp[] = Util.reNorm(Util.cross(Util.cross(port30PlaneNormal, globalUp), port30PlaneNormal));
	
	public Square port30Plane = new Square("port30Plane", port30PlanePos, port30PlaneNormal, port30PlaneUp, 1.00, 2.00, Absorber.ideal());


	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lens1CentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.600, NullInterface.ideal());

	/** Plasma radiating surface for heat-load analysis */
	public double[] radSurfaceCentre = { 1.68544196,  5.88435327,  0.43004889 };
	public double[] radSurfaceNormal = { -0.2822023 , -0.85883142, -0.42751662 };
	public double[] radUp = Util.createPerp(radSurfaceNormal);
	//public double radSurfWidth = 1.100; //for testing inner parts (window etc)
	//public double radSurfHeight = 0.900;

	public double radSurfWidth = 2.500; //for testing closed shutter
	public double radSurfHeight = 1.500;

	public Square radSurface = new Square("radSurface", radSurfaceCentre, radSurfaceNormal, radUp, radSurfHeight, radSurfWidth, NullInterface.ideal()); 
	
	
	public Element tracingTarget = mirror;
	public Surface checkSurface = mirror;
			
	public BeamEmissSpecAEA21U_CISDual() {
		super("beamSpec-aea21u");
			
		lightPathsSystemName = "AEA21u";
			
			

		//addElement(new STLMesh("panel", "/work/ipp/w7x/cad/aea21/panel-cutting-aea21-edge-channels-cut-front.stl", portEntryPos, 0.500));
		
		addElement(mirror);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lens1);
		addElement(lens2);
		addElement(fibrePlane1);
		addElement(fibrePlane2);
		addElement(beamPlane);
		//addElement(shieldTiles);
		
		addElement(catchPlane);
		addElement(strayPlane);
		addElement(port30Plane);
		
		for(Surface s : lens1.getSurfacesAll()) {
			if(s instanceof Iris)
				((Iris) s).setDiscRadius(0.018);
				//s.setInterface(NullInterface.ideal());
		}
		for(Surface s : lens2.getSurfacesAll()) {
			if(s instanceof Iris)
				((Iris) s).setDiscRadius(0.018);
				//s.setInterface(NullInterface.ideal());
		}
		
		//now rotate everything around centre of A port
		double module2centreAng = 2 * Math.PI / 5;
		
		double rotAxis[] = { FastMath.cos(module2centreAng), FastMath.sin(module2centreAng), 0 };
		double rotCentre[] = Util.mul(rotAxis, 6.0); //actually doesn't matter
		double rotMat[][] = Algorithms.rotationMatrix(rotAxis, Math.PI);
		
		this.rotate(rotCentre, rotMat);
		
		

		setupFibrePositions();
		setupFibrePlanes();
		
		channelZ = new double[ channelR.length];
		for(int i=0; i < channelR.length; i++){
			channelZ[i] = 0;
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
		double cyldLen = 3.000;
		double cyldRadius = 0.0025;
		
		double u[] = Util.reNorm(Util.mul(opticAxis, -1));
		double p[] = Util.plus(mirrorCentrePos, Util.mul(u, -cyldLen));
		 
		System.out.println("Part.show(Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
				",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 +
				"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360)); #TubeAxisCylinder");
		
		u = Util.reNorm(Util.mul(sourceNormal, -1));
		p = Util.plus(mirrorCentrePos, Util.mul(u, -cyldLen));
		
		System.out.println("Part.show(Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
				",FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3 +
				"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360)); #SourceAxisCylinder");
		
		double adjustedTargetPos[] = Util.plus(targetObsPos, Util.mul(opticAxis, mirrorDistIntoPort));
		System.out.println("Part.show(Part.makeSphere("+(cyldRadius*1e3)+", FreeCAD.Vector("+adjustedTargetPos[0]*1e3+","+adjustedTargetPos[1]*1e3+","+adjustedTargetPos[2]*1e3 + "))); # adjustedTargetPos");
		
		System.out.println("Part.show(Part.makeSphere("+(cyldRadius*1e3)+", FreeCAD.Vector("+mirrorCentrePos[0]*1e3+","+mirrorCentrePos[1]*1e3+","+mirrorCentrePos[2]*1e3 + "))); # mirrorCentrePos");
		
	}

	public String getDesignName() { return "aea21u-cisDual-" + String.format("%.0f", lens1FocalLength*1e3) + "mm";	}

	public Element[] makeSimpleModel() {
		return new Element[0];
	}
	
	private void setupFibrePositions() {
		int nLenses = 2;
		int nBeams =  nLenses;
		int nRows = 4;
		int nCols = 5;
		int nFibres = nRows * nCols;
		
		channelR = new double[nBeams][];
		beamIdx = new int[nBeams];
		lightPathRowName = new String[nBeams];
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
				
		
		double fibreBundleWidth = 0.010; // 1000 fibres of 10um
		double fibreBundleHeight = 0.008; // 800 fibres of 10um
		
		double xVec[] = Util.reNorm(Util.cross(portNormal, globalUp));
		double yVec[] = Util.reNorm(Util.cross(xVec, portNormal));
		
		for(int iB=0; iB < nBeams; iB++){
			double origin[] = (iB == 1) ? fibrePlane2Pos : fibrePlane1Pos;
			//Util.plus(Util.plus(fibrePlanePos, 
		//									Util.mul(portRight, (iB == 1) ? lens2Right : lens1Right)),
		//									Util.mul(portUp, (iB == 1) ? lens2Up : lens1Up));

			beamIdx[iB] = -iB;
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			channelR[iB] = new double[nFibres];
			lightPathRowName[iB] = "row" + iB;
		
			for(int iFR=0; iFR < nRows; iFR++){
				for(int iFC=0; iFC < nCols; iFC++){
					int iF = iFR * nCols + iFC;
					
					channelR[iB][iF] = iF;
					
					double x = -fibreBundleWidth/2 + iFC * (fibreBundleWidth / (nCols - 1));
					double y = -fibreBundleHeight/2 + iFR * (fibreBundleHeight / (nRows - 1));
					
					fibreEndPos[iB][iF] = Util.plus(Util.plus(origin, Util.mul(xVec, x)), Util.mul(yVec, y));
					
					fibreEndNorm[iB][iF] = Util.mul(fibrePlane1.getNormal(), -1.0);
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

	public double getFibreNA(int iB, int iP) { return 0.3; }

	public double getFibreDiameter(int i, int j) { return 0.000065; }		

}
