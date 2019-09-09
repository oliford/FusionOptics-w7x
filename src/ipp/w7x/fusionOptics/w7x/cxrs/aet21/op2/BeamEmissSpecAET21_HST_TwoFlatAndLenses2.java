package ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2;

import ipp.w7x.neutralBeams.W7xNBI;
import net.jafama.FastMath;
import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.materials.BK7;
import fusionOptics.materials.IsotropicFixedIndexGlass;
import fusionOptics.materials.Sapphire;
import fusionOptics.materials.SchottSFL6;
import fusionOptics.optics.STLMesh;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Dish;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Paraboloid;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAET21_HST_TwoFlatAndLenses2 extends Optic {
	public String lightPathsSystemName = "AET21-HST";
	
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 1000e-9;
	
	/** DIrection of port axis */
	public double portAxis[] = { 0.07272971344397348, -0.9468079681488022, 0.3134725829036636 };
	
	public double portRight[] = Util.reNorm(Util.cross(portAxis, globalUp));
	public double portUp[] = Util.reNorm(Util.cross(portRight, portAxis));
	
	/** Point on port axis, somewhere in middle of port */
	public double portMidCentre[] = { -1.0052220437467529, 7.2323006773619745, -0.46831944419294246 };
	
	/** Point on Baffel on far side of port from beam */
	public double baffelPoint[] = { -1.0722723388671875, 6.15255517578125, -0.030531103134155273  };
		
	public double frontDiscCentre[] = Util.plus(portMidCentre, Util.mul(portAxis, Algorithms.pointOnLineNearestPoint(portMidCentre, portAxis, baffelPoint)));
	
	public double frontDiscRadius = Util.length(Util.minus(baffelPoint, frontDiscCentre));
	
	public Disc frontDisc = new Disc("frontDisc", frontDiscCentre, portAxis, frontDiscRadius, NullInterface.ideal());
	
	/***** Observation target ****/
	public int targetBoxIdx = 1; //NI21
	
	public double beamDumps[][] = {
		{ -0.21141751098632813, 4.57866845703125, 0.4623721008300781 },
		{ 0.1455321044921875, 4.9178349609375, 0.41085964965820315 },
		{ 0.15248867797851562, 5.02684375, 0.1864234619140625 },
		{ -0.17423611450195312, 4.73173291015625, 0.174153076171875 }};
	
	public double targetObsPos[] = Util.mul(Util.plus(Util.plus(beamDumps[0], beamDumps[1]), Util.plus(beamDumps[2], beamDumps[3])), 0.25);
	public Sphere targetSphere = new Sphere("targetSphere", targetObsPos, 0.025, NullInterface.ideal());

	/**** Port Tube****/	
	public double portTubeDiameter = 0.300;
	public double portTubeLength = 1.000;
	public double portTubeCentre[] = Util.minus(frontDiscCentre, Util.mul(portAxis, 0.010 + portTubeLength / 2));
	public Cylinder portTubeCyld = new Cylinder("portTubeCyld", portTubeCentre, portAxis, portTubeDiameter/2, portTubeLength, Absorber.ideal());


	/*** Entry iris ***/
	public double entryApertureMirrorDist = 0.080;
	public double entryApertureDiameter = 0.025;
	
	/**** Mirror ****/	
	
	public double mirror1FromFront = 0.130;
	public double mirror1PortRightShift = 0.020;	
	public double mirror1PortUpShift = 0.060;
	public double mirror1Width = 0.050;
	public double mirror1Height = 0.040;
	public double mirror1CentrePos[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror1FromFront),
																		Util.plus(Util.mul(portRight, mirror1PortRightShift),
																				  Util.mul(portUp, mirror1PortUpShift))));	
	public double mirror2FromFront = 0.140;
	public double mirror2PortRightShift = -0.050;	
	public double mirror2PortUpShift = 0.030;	
	public double mirror2Width = 0.100;
	public double mirror2Height = 0.050;
	public double mirror2InPlaneRotate = -10 * Math.PI / 180;
	public double mirror2InPlaneShiftRight = 0.010;
	public double mirror2CentrePos0[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror2FromFront),
																		Util.plus(Util.mul(portRight, mirror2PortRightShift),
																				  Util.mul(portUp, mirror2PortUpShift))));
	
	/**** Lens ****/
	//https://www.edmundoptics.com/p/100mm-dia-x-300mm-focal-length-pcx-condenser-lens/1011/
	//public double lens1FocalLength = 0.300;
	public double lens1CurvatureRadius = 0.15500;
	public double lens1ClearAperture = 0.09700;	
	public double lens1Thickness = 0.0125;
	public double lens1Diameter = 0.100;
	public double lens1FromMirror = 0.070;
	public double lens1PortRightShift = 0.000;	
	public double lens1PortUpShift = 0.000;	

	//public double lens2FocalLength = 0.300;
	public double lens2CurvatureRadius = 0.15500;
	public double lens2ClearAperture = 0.09700;	
	public double lens2Thickness = 0.0125;
	public double lens2Diameter = 0.100;
	public double lens2FromLens1 = 0.060;
	public double lens2PortRightShift = 0.000;	
	public double lens2PortUpShift = 0.000;	

	//public double lens3FocalLength = 0.300;
	public double lens3CurvatureRadius = 0.15500;
	public double lens3ClearAperture = 0.09700;	
	public double lens3Thickness = 0.0125;
	public double lens3Diameter = 0.100;
	public double lens3FromLens2 = 0.160;
	public double lens3PortRightShift = 0.000;	
	public double lens3PortUpShift = 0.000;	

	public double lens4FocalLength = 0.025;
	public double lens4FromLens3 = 0.150;
	
	
	/*** Positions/vectors ****/
	
	//public double mirror1Angle = (90 + 45) * Math.PI / 180;
	//public double mirror1Normal[] = Util.reNorm(Algorithms.rotateVector(Algorithms.rotationMatrix(portUp, mirror1Angle), portAxis));	
		
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, mirror1CentrePos));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVecBox(targetBoxIdx), observationVec));
	
	public double mirror12Vec[] = Util.reNorm(Util.minus(mirror1CentrePos, mirror2CentrePos0));
	
	
	public double lens1CentrePos[] = Util.plus(mirror2CentrePos0, Util.plus(Util.mul(portAxis, -lens1FromMirror),
			Util.plus(Util.mul(portRight, lens1PortRightShift),
					  Util.mul(portUp, lens1PortUpShift))));

	public double lensNormal[] = Util.reNorm(Util.minus(lens1CentrePos, mirror2CentrePos0));
	//public double lensNormalN[] = Util.mul(lensNormal, -1);
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(lensNormal, lens2FromLens1));

	public double lens3CentrePos[] = Util.plus(lens2CentrePos, Util.mul(lensNormal, lens3FromLens2));

	public double lens4CentrePos[] = Util.plus(lens3CentrePos, Util.mul(lensNormal, lens4FromLens3));

	public double mirror1Normal[] = Util.reNorm(Util.mul(Util.plus(Util.mul(mirror12Vec, -1), observationVec), 0.5));
	public double mirror1Right[] = Util.reNorm(Util.cross(mirror1Normal, globalUp));
	public double mirror1Up[] = Util.reNorm(Util.cross(mirror1Right, mirror1Normal));
	
	public double mirror2Normal[] = Util.reNorm(Util.mul(Util.plus(lensNormal, mirror12Vec), 0.5));
	
	public double mirror2Right0[] = Util.reNorm(Util.cross(mirror2Normal, globalUp));
	public double mirror2Up0[] = Util.reNorm(Util.cross(mirror2Right0, mirror2Normal));
	
	public double mirror2Right[] = Util.plus(Util.mul(mirror2Right0, FastMath.cos(mirror2InPlaneRotate)),
											Util.mul(mirror2Up0, FastMath.sin(mirror2InPlaneRotate)));
	public double mirror2Up[] = Util.reNorm(Util.cross(mirror2Right, mirror1Normal));

	
	public double mirror2CentrePosPhys[] = Util.plus(mirror2CentrePos0, Util.mul(mirror2Right, mirror2InPlaneShiftRight));
	
	
	public double entryAperturePos[] = Util.plus(mirror1CentrePos, Util.mul(observationVec, entryApertureMirrorDist));
	public Iris entryAperture = new Iris("entryAperture", entryAperturePos, observationVec, 3*entryApertureDiameter/2, entryApertureDiameter*0.495, Absorber.ideal());
	public Disc entryTarget = new Disc("entryTarget", entryAperturePos, observationVec, 0.505*entryApertureDiameter, NullInterface.ideal());
	
	public Medium lens1Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens1CentrePos, lensNormal, lens1Diameter/2, lens1CurvatureRadius, lens1Thickness, lens1Medium, IsoIsoInterface.ideal());
	public Iris lens1Iris = new Iris("lens1Iris", lens1CentrePos, lensNormal, lens1Diameter*0.7, lens1ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens2Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens2CentrePos, lensNormal, lens2Diameter/2, lens2CurvatureRadius, lens2Thickness, lens2Medium, IsoIsoInterface.ideal());
	public Iris lens2Iris = new Iris("lens2Iris", lens2CentrePos, lensNormal, lens2Diameter*0.7, lens2ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens3Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens3 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens3CentrePos, lensNormal, lens3Diameter/2, lens3CurvatureRadius, lens3Thickness, lens3Medium, IsoIsoInterface.ideal());
	public Iris lens3Iris = new Iris("lens3Iris", lens3CentrePos, lensNormal, lens3Diameter*0.7, lens3ClearAperture/2, null, null, Absorber.ideal());
	
	
	public Nikon50mmF11 lens4 = new Nikon50mmF11(lens4CentrePos, lens4FocalLength / 0.050, lensNormal);
		
	public Square mirror1 = new Square("mirror1", mirror1CentrePos, mirror1Normal, mirror1Up, mirror1Height, mirror1Width, Reflector.ideal());
	//public Disc mirror1 = new Disc("mirror1", mirror1CentrePos, mirror1Normal, mirror1Width/2, Reflector.ideal());
	//public Dish mirror1 = new Dish("mirror1", mirror1CentrePos, mirror1Normal, 0.380, mirror1Width/2, Reflector.ideal());

	public Square mirror2 = new Square("mirror2", mirror2CentrePosPhys, mirror2Normal, mirror2Up, mirror2Height, mirror2Width, Reflector.ideal());
	//public Dish mirror2 = new Dish("mirror2", mirror2CentrePos, mirror2Normal, 0.100, mirror2Width/2, Reflector.ideal());

	/*** Fibre plane ****/
	public double fibrePlaneFromLens4 = lens4FocalLength + 0.0043;
	public double fibrePlanePos[] = Util.plus(lens4CentrePos, Util.mul(lensNormal, fibrePlaneFromLens4));
	public double fibrePlaneSize = 0.050;
	
	public double fibrePlaneNormal[] = Util.mul(lensNormal, -1);
	
	public double fibresXVec0[] = Util.reNorm(Util.cross(fibrePlaneNormal, globalUp));
	public double fibresYVec0[] = Util.reNorm(Util.cross(fibresXVec0, fibrePlaneNormal));
	public double fibreRotation = -5 * Math.PI / 180;
	public double fibresXVec[] = Algorithms.rotateVector(Algorithms.rotationMatrix(fibrePlaneNormal, fibreRotation), fibresXVec0);
	public double fibresYVec[] = Algorithms.rotateVector(Algorithms.rotationMatrix(fibrePlaneNormal, fibreRotation), fibresYVec0);
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, fibrePlaneNormal, fibresYVec, fibrePlaneSize, fibrePlaneSize, Absorber.ideal());	
		
	public STLMesh panelEdge = new STLMesh("panel", "/work/cad/aet21/conflicting-panel-aet21.stl");
	
	public Element tracingTarget = entryTarget;
	
	double beamDumpPlaneCentre[] = Util.mul(Util.plus(Util.plus(beamDumps[0], beamDumps[1]), Util.plus(beamDumps[2], beamDumps[3])), 0.25); 
	double beamDumpPlaneNormal[] = Util.reNorm(Util.cross(Util.minus(beamDumps[1], beamDumps[0]), Util.minus(beamDumps[2], beamDumps[0])));
	
	public double beamDumpPlaneRight[] = Util.reNorm(Util.cross(beamDumpPlaneNormal, globalUp));
	public double beamDumpPlaneUp[] = Util.reNorm(Util.cross(beamDumpPlaneRight, beamDumpPlaneNormal));
	
	public Square beamPlane = new Square("beamDumpPlane", beamDumpPlaneCentre, beamDumpPlaneNormal, beamDumpPlaneUp, 2.0, 2.0, Absorber.ideal());
	public Square strayPlane = null;
	public Cylinder rod = null;
	
	public int beamIdx[] = { W7xNBI.BEAM_Q7  };
	//public double[] channelR = OneLiners.linSpace(5.38, 5.88, nFibres);
	public String[] lightPathRowName = null;
	
	public double fibreNA = 0.22; // As AUG	
	public double fibreEndDiameter = 0.000200; // Paul's 200um fibres, guessing the jacket diameter

	public Square fibrePlanes[][] = {{
	}};
	
	public double[][] channelR = { 
			{ 5.06, 5.07, 5.08, 5.09,
			 5.06, 5.07, 5.08, 5.09, }, 
		}; 
		public double[][][] fibreEndPos = { { 
			
			{ -0.9210183291964545, 6.729867004747737, -0.26715987262019025 },
			{ -0.9131350741258694, 6.730586674023365, -0.267407539036859 },
			{ -0.9116947146363172, 6.729458907431256, -0.27068633263687536 },
			{ -0.9194838842455488, 6.728823464865586, -0.2713123308138487 },
			
			{ -0.9210183291964545, 6.729867004747737, -0.26715987262019025 },
			{ -0.9131350741258694, 6.730586674023365, -0.267407539036859 },
			{ -0.9116947146363172, 6.729458907431256, -0.27068633263687536 },
			{ -0.9194838842455488, 6.728823464865586, -0.2713123308138487 },
				}, 	}; 
		
		public double[][][] fibreEndNorm = { { 
			{ 0.19454430160486041, -0.9465105930124409, 0.2574300138451508 },
			{ -0.01417406420422795, -0.9639323373283242, 0.26576972167026136 },
			{ -0.05653836455201652, -0.9325555771639292, 0.3565718845257349 },
			{ 0.1486389215107028, -0.9176021699667827, 0.3686634355131882 },

			{ 0.19454430160486041, -0.9465105930124409, 0.2574300138451508 },
			{ -0.01417406420422795, -0.9639323373283242, 0.26576972167026136 },
			{ -0.05653836455201652, -0.9325555771639292, 0.3565718845257349 },
			{ 0.1486389215107028, -0.9176021699667827, 0.3686634355131882 },
				}, 	};
	
	public BeamEmissSpecAET21_HST_TwoFlatAndLenses2() {
		super("beamSpec-aet21-op2");
		
		//addElement(frontDisc);
		addElement(panelEdge);
		addElement(targetSphere);
		addElement(entryTarget);
		addElement(entryAperture);
		//addElement(portTubeCyld);
		addElement(mirror1);
		addElement(mirror2);
		addElement(lens1);
		addElement(lens1Iris);
		addElement(lens2);
		addElement(lens2Iris);
		addElement(lens3);
		addElement(lens3Iris);
		addElement(lens4);
		addElement(fibrePlane);
		//addElement(new Sphere("bSphere", mirror1.getBoundarySphereCentre(), mirror1.getBoundarySphereRadius(), NullInterface.ideal()));
	
		for(int i=0; i < 4; i++) {
			fibreEndPos[0][i] = Util.plus(fibreEndPos[0][i], Util.mul(fibrePlaneNormal, 0.005));
			
			fibreEndPos[0][i+4] = Util.plus(fibreEndPos[0][i], Util.mul(fibresXVec, 0.000500));			
			
		}
		
		/*// Shift fibres +/- final lens
		 double shiftX = 0.003;
		for(int i=0; i < fibreEndPos[0].length; i++) {
			fibreEndPos[0][i] = Util.plus(fibreEndPos[0][i], Util.mul(fibresXVec, shiftX));
		}
		lens4.shift(Util.mul(fibresXVec, shiftX));
		//*/
		
		//rotate fibres and final lens around final lens centre
		/*double rot = 1.0 * Math.PI / 180;
		double rotCentre[] = lens4.getCentre();
		lens4.rotate(rotCentre, Algorithms.rotationMatrix(portUp, rot));
		for(int i=0; i < fibreEndPos[0].length; i++) {
			fibreEndNorm[0][i] = Algorithms.rotateVector(Algorithms.rotationMatrix(portUp, rot),fibreEndNorm[0][i]);
			fibreEndPos[0][i] = Util.plus(rotCentre, 
											Algorithms.rotateVector(Algorithms.rotationMatrix(portUp, rot), 
																	Util.minus(fibreEndPos[0][i], rotCentre))
									);
		}
		
		
		/*double shift[] = Util.mul(fibresXVec, 0.003);
		//shift 2 mid lenses and field
		lens1.shift(shift);
		lens2.shift(shift);
		lens3.shift(shift);
		//*/
		
		/* rotate mirrors
		double rotMirror = 1.0 * Math.PI / 180;
		//mirror1.rotate(mirror1.getCentre(), Algorithms.rotationMatrix(mirror1.getUp(), rotMirror));
		//mirror2.rotate(mirror2.getCentre(), Algorithms.rotationMatrix(mirror2.getUp(), rotMirror));
		//*/
		
		//shift mirrors
		//double shift[] = Util.mul(fibresXVec, 0.003);
		//shift 2 mid lenses and field
		//mirror1.shift(shift);
		
		
		setupFibrePlanes();
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
	
	public String getDesignName() { return "aet21-hst-twoFlat";	}
		

}
