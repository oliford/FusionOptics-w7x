package ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2;

import ipp.w7x.neutralBeams.W7xNBI;
import net.jafama.FastMath;
import uk.co.oliford.jolu.OneLiners;

import java.util.Arrays;

import algorithmrepository.Algorithms;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.materials.FusedSilica;
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
import fusionOptics.types.Surface;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAET21_HST_TwoFlatAndLenses_75mm_UVFS_3cmAperture extends Optic {
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
		{ 0.1492540283203125, 4.95760693359375, 0.27083691406250002},
		{ -0.17423611450195312, 4.73173291015625, 0.174153076171875 }
		
		
	};
	
	public double hhfTiles[][] = {
			 { 0.2124384078979492, 4.93090771484375, 0.4795155334472656},
			 //{ 0.16073893737792969, 4.905971923828125, 0.46937031555175784},
			 { 0.11097871398925782, 4.883496337890625, 0.45982559204101564},
			 { 0.062120355606079106, 4.858057861328125, 0.45018853759765626},
			 //{ 0.014070590496063233, 4.830812255859375, 0.43990501403808596},
			 { -0.033051654338836674, 4.804234375, 0.4300990142822266 }	
		};
	
	public double overrideObsPositions[][][] = { beamDumps.clone(), beamDumps.clone(), hhfTiles.clone() };
	
	public double targetObsPos[] = Util.mul(Util.plus(Util.plus(beamDumps[0], beamDumps[1]), Util.plus(beamDumps[2], beamDumps[3])), 0.25);
	public Sphere targetSphere = new Sphere("targetSphere", targetObsPos, 0.025, NullInterface.ideal());

	/**** Port Tube****/	
	public double portTubeDiameter = 0.300;
	public double portTubeLength = 1.000;
	public double portTubeCentre[] = Util.minus(frontDiscCentre, Util.mul(portAxis, 0.010 + portTubeLength / 2));
	public Cylinder portTubeCyld = new Cylinder("portTubeCyld", portTubeCentre, portAxis, portTubeDiameter/2, portTubeLength, Absorber.ideal());


	/*** Entry iris ***/
	public double entryApertureMirrorDist = 0.080;
	public double entryApertureDiameter = 0.030;
	
	/**** Mirror ****/	
	
	public double mirror1FromFront = 0.130;
	public double mirror1PortRightShift = 0.000;	
	public double mirror1PortUpShift = 0.060;
	public double mirror1Width = 0.050;
	public double mirror1Height = 0.040;
	public double mirror1CentrePos[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror1FromFront),
																		Util.plus(Util.mul(portRight, mirror1PortRightShift),
																				  Util.mul(portUp, mirror1PortUpShift))));	
	public double mirror2FromFront = 0.140;
	public double mirror2PortRightShift = -0.070;	
	public double mirror2PortUpShift = 0.030;	
	public double mirror2Width = 0.100;
	public double mirror2Height = 0.050;
	public double mirror2InPlaneRotate = -10 * Math.PI / 180;
	public double mirror2InPlaneShiftRight = 0.010;
	public double mirror2CentrePos0[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror2FromFront),
																		Util.plus(Util.mul(portRight, mirror2PortRightShift),
																				  Util.mul(portUp, mirror2PortUpShift))));
	
	/**** Lens ****/
	//Eksma Optics 110-1717E, http://eksmaoptics.com/optical-components/lenses/uv-fs-plano-convex-lenses/ 	280€
	//public double lens1FocalLength = 0.250;
	public double lens1CurvatureRadius = 0.11900;
	public double lens1ClearAperture = 0.07500;	
	public double lens1Thickness = 0.0090;
	public double lens1Diameter = 0.0762;
	public double lens1FromMirror = 0.060;
	public double lens1PortRightShift = 0.000;	
	public double lens1PortUpShift = 0.000;	

	//public double lens2FocalLength = 0.250;
	public double lens2CurvatureRadius = 0.11900;
	public double lens2ClearAperture = 0.07500;	
	public double lens2Thickness = 0.0090;
	public double lens2Diameter = 0.0762;
	public double lens2FromLens1 = 0.060;
	public double lens2PortRightShift = 0.000;	
	public double lens2PortUpShift = 0.000;	

	//public double lens3FocalLength = 250;
	public double lens3CurvatureRadius = 0.11900;
	public double lens3ClearAperture = 0.07500;	
	public double lens3Thickness = 0.0090;
	public double lens3Diameter = 0.0762;
	public double lens3FromLens2 = 0.150;
	public double lens3PortRightShift = 0.000;	
	public double lens3PortUpShift = 0.000;	

	public double lens4FocalLength = 0.035;
	public double lens4FromLens3 = 0.170;
	
	
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
	
	public Medium lens1Medium = new Medium(new FusedSilica());
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens1CentrePos, lensNormal, lens1Diameter/2, lens1CurvatureRadius, lens1Thickness, lens1Medium, IsoIsoInterface.ideal());
	public Iris lens1Iris = new Iris("lens1Iris", lens1CentrePos, lensNormal, lens1Diameter*0.7, lens1ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens2Medium = new Medium(new FusedSilica());
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens2CentrePos, lensNormal, lens2Diameter/2, lens2CurvatureRadius, lens2Thickness, lens2Medium, IsoIsoInterface.ideal());
	public Iris lens2Iris = new Iris("lens2Iris", lens2CentrePos, lensNormal, lens2Diameter*0.7, lens2ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens3Medium = new Medium(new FusedSilica());
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
	//public double fibrePlaneFromLens4 = lens4FocalLength + 0.0043; //25mm obj
	public double fibrePlaneFromLens4 = lens4FocalLength + 0.010; //35mm obj
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
	
	public int beamIdx[] = { -1, -2, -3  };
	//public double[] channelR = OneLiners.linSpace(5.38, 5.88, nFibres);
	public String[] lightPathRowName = null;
	
	public double fibreNA = 0.22; // As AUG	
	public double fibreEndDiameter = 0.000200; // Paul's 200um fibres, guessing the jacket diameter

	public Square fibrePlanes[][] = {{
	}};
	
	public double[][] channelR = { 
			{ 5.06, 5.07, 5.08, 5.09, }, 
			{ 5.06, 5.07, 5.08, 5.09, }, 
			{ 5.06, 5.07, 5.08, 5.09, }, 
		}; 
		public double[][][] fibreEndPos = { { 
					{ -0.9024098797183137, 6.7439881969491156, -0.2714119413905194 },
					{ -0.8935709738268723, 6.744704304486915, -0.27170325188969613 },
					{ -0.8929003328529744, 6.743966479446878, -0.274079937103133 },
					{ -0.900655409580841, 6.742739838794106, -0.27603603671794125 },
				}, { 
					{ -0.9024108111925647, 6.743997594885148, -0.2714172725396348 },
					{ -0.8935722628462642, 6.744707739301485, -0.2717023271266992 },
					{ -0.8928983862254896, 6.743957458370052, -0.27407529990987645 },
					{ -0.9006536959858601, 6.742724889533057, -0.2760323072826409 },
				}, { 
					{ -0.8926387866591708, 6.744987351588245, -0.27067312613253974 },
					{ -0.8945279070341462, 6.744910129797169, -0.27090323329047616 },
					{ -0.8955489397075542, 6.744860159647423, -0.2710513186730527 },
					{ -0.8976695950029432, 6.744678819299955, -0.271411142824974 },
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ 0.10879008642589102, -0.9441039424355785, 0.3111791493224523 },
				{ 0.03379682529694248, -0.955455343172194, 0.2931942390354098 },
				{ 0.03436999413793976, -0.9492304256594941, 0.31269842101496687 },
				{ 0.09942213277476598, -0.9328094945344684, 0.34638372713057175 },
				}, { 
				{ 0.10040296896055408, -0.9475065504735578, 0.3035631410985287 },
				{ 0.025960795369988902, -0.9524401837065297, 0.3036177425066415 },
				{ 0.032374174418744295, -0.9482216027497331, 0.31595522611508114 },
				{ 0.10979604805716048, -0.9299926015712643, 0.3507970764612234 },
				}, { 
				{ 0.05405943874693843, -0.9507666806016195, 0.30515618319172894 },
				{ 0.033942382564394476, -0.9601673289003954, 0.2773564767192799 },
				{ 0.03795608323588509, -0.956814331186911, 0.28821115762706834 },
				{ 0.07505989868420697, -0.9538920952069483, 0.29061294243583635 },
				}, 	};
	public BeamEmissSpecAET21_HST_TwoFlatAndLenses_75mm_UVFS_3cmAperture() {
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
		
		dumpInfoForDesigner();
		
		//defocus HST fibres

		/*
		for(int j=0; j < channelR[0].length; j++) {
			fibreEndPos[0][j] = Util.plus(fibreEndPos[0][j], Util.mul(fibrePlaneNormal, -0.003)); //refocus
		}
		//*/
		
		// all fibres parallel to port axis 
		for(int i=0; i < channelR.length; i++) {
			for(int j=0; j < channelR[i].length; j++) {
				fibreEndNorm[i][j] = fibrePlaneNormal.clone();
			}
		}	
		
		//make neightbour fibres for scientific measurements
		for(int j=0; j < channelR[0].length; j++) {
			fibreEndPos[1][j] = Util.plus(fibreEndPos[0][j], Util.mul(fibresXVec, 0.000500));
			fibreEndNorm[1][j] = fibreEndNorm[0][j].clone();
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
	
		

	public void dumpInfoForDesigner() {
		
		for(Surface s : new Surface[] {
					mirror1,
					mirror2,
					lens1.getPlanarSurface(),
					lens2.getPlanarSurface(),
					lens3.getPlanarSurface(),
				}) {
			double c[] = s.getCentre();
			System.out.println(String.format("%s: (%5.3f, %5.3f, %5.3f) mm", 
					s.getName(), 
					c[0]*1e3, c[1]*1e3, c[2]*1e3));
		}
	
		double c[] = lens4CentrePos;
		System.out.println(String.format("lens4 (centre of case): (%5.3f, %5.3f, %5.3f) mm", c[0]*1e3, c[1]*1e3, c[2]*1e3));

		c = mirror1.getNormal();			
		System.out.println(String.format("mirror1 normal: (%5.3f, %5.3f, %5.3f)", c[0], c[1], c[2]));
		
		c = mirror2.getNormal();			
		System.out.println(String.format("mirror2 normal: (%5.3f, %5.3f, %5.3f)", c[0], c[1], c[2]));
		
		c = lens1.getBackSurface().getDishNormal();			
		System.out.println(String.format("lens1,2,3 normal: (%5.3f, %5.3f, %5.3f)", c[0], c[1], c[2]));
	}
	
	public String getDesignName() { return "aet21-hst-twoFlat";	}
	
}
