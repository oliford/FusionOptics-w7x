package ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2;

import ipp.w7x.neutralBeams.W7xNBI;
import net.jafama.FastMath;
import oneLiners.OneLiners;

import java.util.Arrays;

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
import fusionOptics.types.Surface;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7 extends Optic {
	public String lightPathsSystemName = "AET21-HST"; //and -PHYS for the 2nd set
	
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 1000e-9;
	
	public double virtualObsPos[] = { -0.8358309322759397,	6.072309870384837,	-0.02230182161925606 };
	
	/** Origin of coordinate system for lab alignment. Point on top of forward frame above rear most M8 but */
	public static double labAlignOrigin[] = { -0.92803, 6.53730, -0.09468 };
	
	/** X axis of coordinate system for lab alignment. Plugin and tube axis direction. Should be the same as portAxis */
	public static double labAlignX[] = Util.reNorm(new double[]{ 0.073, -0.947,  0.313 }); 
	
	/** X axis of coordinate system for lab alignment. Plugin and tube axis direction */
	public static double labAlignZ[] = Util.reNorm(new double[]{ 0.141, 0.321, 0.937  }); 
	
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
	
	public boolean rotateToAET20 = false;
	public boolean adjustToLC3 = false;
	
	public enum Focus {		
		BeamDump(0.0),
		M1(0.002), 	//Was 5mm, dropped to 2mm to avoid picking up from W tile.
		L3(-0.005);
		
		Focus(double shift) { this.shift = shift; }		
		public double shift;		
	}
	
	public Focus focus;
	
	/** Original design points OP1.2 and OP2 design. Roughly in the middle maybe adjusted according to Paul's intuition */
	/*public double beamDumps[][] = {
		{ -0.21141751098632813, 4.57866845703125, 0.4623721008300781 },
		{ 0.1455321044921875, 4.9178349609375, 0.41085964965820315 },
		{ 0.1492540283203125, 4.95760693359375, 0.27083691406250002},
		{ -0.17423611450195312, 4.73173291015625, 0.174153076171875 }
	};
	//*/
	
	/** Target points from Sam [1-QYB-Y0011.1] */
	public double beamDumps[][] = {
			{ -0.217779, 4.57337, 0.488461 },
			//{ 0.105223, 4.89688, 0.405341 },
			{ 0.09220848846435548, 4.8905068359375, 0.404767578125 },	//Adjusted by Paul to avoid W		
			//{ 0.122839, 4.94578, 0.256883 },
			{ 0.1048245620727539, 4.93611865234375, 0.2572196044921875 },	//Adjusted by Paul to avoid W
			{ -0.188638, 4.68283, 0.208365 }
			
		};
	//*/
	
	public double hhfTiles[][] = {
			 { 0.2124384078979492, 4.93090771484375, 0.4795155334472656},
			 //{ 0.16073893737792969, 4.905971923828125, 0.46937031555175784},
			 { 0.11097871398925782, 4.883496337890625, 0.45982559204101564},
			 { 0.062120355606079106, 4.858057861328125, 0.45018853759765626},
			 //{ 0.014070590496063233, 4.830812255859375, 0.43990501403808596},
			 { -0.033051654338836674, 4.804234375, 0.4300990142822266 }	
		};
	
	public final String backgroundSTLFiles[] = {
			"/work/cad/aet21/bg-targetting/target-m2-aet21-hst-cut.stl",
			"/work/cad/aet21/bg-targetting/shield-m2-aet21-hst-cut.stl",
			"/work/cad/aet21/bg-targetting/baffle-m2-aet21-hst-cut.stl"
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
	public double entryApertureDiameter = 0.025;
	
	/**** Mirror ****/		
	public double mirror1FromFront = 0.130;
	public double mirror1PortRightShift = 0.000;
	public double mirror1PortUpShift = 0.060;
	public double mirror1Width = 0.057;
	public double mirror1Height = 0.043;
	public double mirror1InPlaneRotate = 20 * Math.PI / 180;
	public double mirror1InPlaneShiftUp = 0.0015;
	public double mirror1InPlaneShiftRight = 0.000;
	public double mirror1CentrePos0[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror1FromFront),
																		Util.plus(Util.mul(portRight, mirror1PortRightShift),
																				  Util.mul(portUp, mirror1PortUpShift))));	
	public double mirror2FromFront = 0.140;
	public double mirror2PortRightShift = -0.070;
	public double mirror2PortUpShift = 0.030;
	public double mirror2Width = 0.1068;
	public double mirror2Height = 0.0568;
	public double mirror2InPlaneRotate = -20 * Math.PI / 180;
	public double mirror2InPlaneShiftRight = 0.010; //shift with changing targeting
	public double mirror2InPlaneShiftUp = 0.000;
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
	
	public double windowFromLens3 = 0.090;
	public double windowDiameter = 0.038;
	
	public double lens4FocalLength = 0.025;
	public double lens4FromLens3 = 0.150;
	
	
	/*** Positions/vectors ****/
	
	//public double mirror1Angle = (90 + 45) * Math.PI / 180;
	//public double mirror1Normal[] = Util.reNorm(Algorithms.rotateVector(Algorithms.rotationMatrix(portUp, mirror1Angle), portAxis));	
		
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, mirror1CentrePos0));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVecBox(targetBoxIdx), observationVec));
	
	public double mirror12Vec[] = Util.reNorm(Util.minus(mirror1CentrePos0, mirror2CentrePos0));
	
	
	public double lens1CentrePos[] = Util.plus(mirror2CentrePos0, Util.plus(Util.mul(portAxis, -lens1FromMirror),
			Util.plus(Util.mul(portRight, lens1PortRightShift),
					  Util.mul(portUp, lens1PortUpShift))));

	public double lensNormal[] = Util.reNorm(Util.minus(lens1CentrePos, mirror2CentrePos0));
	//public double lensNormalN[] = Util.mul(lensNormal, -1);
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(lensNormal, lens2FromLens1));

	public double lens3CentrePos[] = Util.plus(lens2CentrePos, Util.mul(lensNormal, lens3FromLens2));

	public double windowCentrePos[] = Util.plus(lens3CentrePos, Util.mul(lensNormal, windowFromLens3));

	public double lens4CentrePos[] = Util.plus(lens3CentrePos, Util.mul(lensNormal, lens4FromLens3));

	public double mirror1Normal[] = Util.reNorm(Util.mul(Util.plus(Util.mul(mirror12Vec, -1), observationVec), 0.5));
	public double mirror1Right0[] = Util.reNorm(Util.cross(mirror1Normal, globalUp));
	public double mirror1Up0[] = Util.reNorm(Util.cross(mirror1Right0, mirror1Normal));
	
	public double mirror1Right[] = Util.reNorm(Util.plus(Util.mul(mirror1Right0, FastMath.cos(mirror1InPlaneRotate)),
											Util.mul(mirror1Up0, FastMath.sin(mirror1InPlaneRotate))));
	public double mirror1Up[] = Util.reNorm(Util.cross(mirror1Right, mirror1Normal));

	public double mirror1CentrePosPhys[] = Util.plus(Util.plus(mirror1CentrePos0, Util.mul(mirror1Right, mirror1InPlaneShiftRight)),
			Util.mul(mirror1Up, mirror1InPlaneShiftUp));

	
	public double mirror2Normal[] = Util.reNorm(Util.mul(Util.plus(lensNormal, mirror12Vec), 0.5));
	
	public double mirror2Right0[] = Util.reNorm(Util.cross(mirror2Normal, globalUp));
	public double mirror2Up0[] = Util.reNorm(Util.cross(mirror2Right0, mirror2Normal));
	
	public double mirror2Right[] = Util.reNorm(Util.plus(Util.mul(mirror2Right0, FastMath.cos(mirror2InPlaneRotate)),
											Util.mul(mirror2Up0, FastMath.sin(mirror2InPlaneRotate))));
	public double mirror2Up[] = Util.reNorm(Util.cross(mirror2Right, mirror2Normal));

	
	public double mirror2CentrePosPhys[] = Util.plus(Util.plus(mirror2CentrePos0, Util.mul(mirror2Right, mirror2InPlaneShiftRight)),
												Util.mul(mirror2Up, mirror2InPlaneShiftUp));
	
	
	public double entryAperturePos[] = Util.plus(mirror1CentrePos0, Util.mul(observationVec, entryApertureMirrorDist));
	public Iris entryAperture = new Iris("entryAperture", entryAperturePos, observationVec, 3*entryApertureDiameter/2, entryApertureDiameter*0.495, Absorber.ideal());
	public Disc entryTarget = new Disc("entryTarget", entryAperturePos, observationVec, 0.505*entryApertureDiameter, NullInterface.ideal());
	
	public Medium lens1Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens1CentrePos, lensNormal, lens1Diameter/2, lens1CurvatureRadius, lens1Thickness, lens1Medium, IsoIsoInterface.ideal());
	public Iris lens1Iris = new Iris("lens1Iris", lens1CentrePos.clone(), lensNormal, lens1Diameter*0.7, lens1ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens2Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens2CentrePos, lensNormal, lens2Diameter/2, lens2CurvatureRadius, lens2Thickness, lens2Medium, IsoIsoInterface.ideal());
	public Iris lens2Iris = new Iris("lens2Iris", lens2CentrePos.clone(), lensNormal, lens2Diameter*0.7, lens2ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens3Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens3 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens", 
			lens3CentrePos, lensNormal, lens3Diameter/2, lens3CurvatureRadius, lens3Thickness, lens3Medium, IsoIsoInterface.ideal());
	public Iris lens3Iris = new Iris("lens3Iris", lens3CentrePos.clone(), lensNormal, lens3Diameter*0.7, lens3ClearAperture/2, null, null, Absorber.ideal());
	
	public Disc window = new Disc("window", windowCentrePos, lensNormal, windowDiameter/2, null, null, NullInterface.ideal());
	
	public Nikon50mmF11 lens4 = new Nikon50mmF11(lens4CentrePos, lens4FocalLength / 0.050, lensNormal);
		
	public Square mirror1 = new Square("mirror1", mirror1CentrePosPhys, mirror1Normal, mirror1Up, mirror1Height, mirror1Width, Reflector.ideal());
	//public Disc mirror1 = new Disc("mirror1", mirror1CentrePos, mirror1Normal, mirror1Width/2, Reflector.ideal());
	//public Dish mirror1 = new Dish("mirror1", mirror1CentrePos, mirror1Normal, 0.380, mirror1Width/2, Reflector.ideal());

	public Square mirror2 = new Square("mirror2", mirror2CentrePosPhys, mirror2Normal, mirror2Up, mirror2Height, mirror2Width, Reflector.ideal());
	//public Dish mirror2 = new Dish("mirror2", mirror2CentrePos, mirror2Normal, 0.100, mirror2Width/2, Reflector.ideal());

	/*** Fibre plane ****/
	public double fibrePlaneFromLens4 = lens4FocalLength + 0.00495; //adjusted 
	//public double fibrePlaneFromLens4 = lens4FocalLength + 0.0043; //25mm obj
	//public double fibrePlaneFromLens4 = lens4FocalLength + 0.000; //25mm obj
	//public double fibrePlaneFromLens4 = lens4FocalLength + 0.010; //20mm obj
	//public double fibrePlaneFromLens4 = lens4FocalLength + 0.002; //16mm obj
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
	
	
	// Plasma radiating surface for heat-load analysis 	
	//public double[] radSurfaceCentre = { -0.6759374 ,  5.97114095, -0.09727827 }; //shifted, for aperture targetting
	public double[] radSurfaceCentre = {  -0.9586693115234375, 5.927921875, -0.07366898345947266 }; //central, for graphite
	public double[] radSurfaceNormal = { -0.12153662,  0.95077941,  0.28503923  };
	
	public double[] radUp = Util.createPerp(radSurfaceNormal);
	//public double radSurfWidth = 1.100; //for testing inner parts (window etc)
	//public double radSurfHeight = 0.900;

	//public double radSurfWidth = 1.000; //for testing closed shutter
	//public double radSurfHeight = 0.900;
	
	public double radSurfWidth = 1.600; //for testing graphite
	public double radSurfHeight = 1.600;


	public Square radSurface = new Square("radSurface", radSurfaceCentre, radSurfaceNormal, radUp, radSurfHeight, radSurfWidth, NullInterface.ideal()); 
	//*/
	
	//heating element for HST test
	/*public double shutterHeaterCentre[] = { -0.8896515502929687, 6.21238232421875, -0.0716107177734375 }; 
	public double shutterHeaterNormal[] = Util.reNorm(new double[] { -0.51348896,  0.8281041 , -0.22488371 }); 
	public double shutterHeaterUp[] =  Util.reNorm(new double[] { 0.10244399, 0.31902389, 0.94219371 }); 
	public double shutterHeaterWidth = 0.035;
	public double shutterHeaterHeight = 0.025; 

	public double shutterHeaterPower = 300;
	public double shutterHeaterPowerFlux = shutterHeaterPower / (shutterHeaterWidth * shutterHeaterHeight);
	
	public Square shutterHeater = new Square("shutterHeater", shutterHeaterCentre, shutterHeaterNormal, shutterHeaterUp, shutterHeaterHeight, shutterHeaterWidth, NullInterface.ideal()); 
	
	public Square radSurface = shutterHeater;	
	public double radSurfaceCentre[] = shutterHeaterCentre;
	public double radSurfWidth = shutterHeaterWidth; //for testing closed shutter
	public double radSurfHeight = shutterHeaterHeight;
	
	public double shutterHeaterTargetDist = 0.030;
	public double shutterHeaterTargetDiameter = 0.100;
	public double shutterHeaterTargetCentre[] = Util.plus(shutterHeaterCentre, Util.mul(shutterHeaterNormal, shutterHeaterTargetDist));
	public Disc shutterHeaterTarget = new Disc("shutterHeaterTarget", shutterHeaterTargetCentre, shutterHeaterNormal, shutterHeaterTargetDiameter/2, NullInterface.ideal()); 
	*/
	
	public int beamIdx[] = { -1, -2, -3  };
	//public double[] channelR = OneLiners.linSpace(5.38, 5.88, nFibres);
	public String[] lightPathRowName = null;
	
	public double fibreNA = 0.22; // As AUG	
	public double fibreEndDiameter = 0.000200; // Paul's 200um fibres, guessing the jacket diameter

	public Square fibrePlanes[][] = {{
	}};
	/*// fibre positions used to feed OP2 design 	
	public double[][] channelR = { 
			{ 5.06, 5.07, 5.08, 5.09, }, 
			{ 5.06, 5.07, 5.08, 5.09, }, 
		}; 

public double[][][] fibreEndPos = { { 
			{ -0.9009351251434649, 6.731370867963247, -0.2674288880838504 },
			{ -0.8929946755691235, 6.732041116065124, -0.267684756644496 },
			{ -0.8923895560005203, 6.731350304556539, -0.2698199519231584 },
			{ -0.8993837992575295, 6.730263703386894, -0.27159624521103887 },
		}, { 
			{ -0.9009345798229672, 6.731364779156342, -0.2674272229022717 },
			{ -0.8929947215534777, 6.732041682163185, -0.26768523250811777 },
			{ -0.892389766677509, 6.731357089733645, -0.26982151091319373 },
			{ -0.8993842772381687, 6.730263295230016, -0.27159527035948794 },
		}, { 
			{ -0.8921800804939098, 6.732223067504862, -0.26675289196209606 },
			{ -0.8938556070493875, 6.732252157036456, -0.2669697561973881 },
			{ -0.8947698822512581, 6.732215572803202, -0.26709863353104785 },
			{ -0.8966930883223921, 6.732031040892463, -0.26741112201053463 },
		}, 	}; 
public double[][][] fibreEndNorm = { { 
		{ 0.18453312732050003, -0.9449114755873694, 0.2703513052023535 },
		{ -0.017980975459953117, -0.9587884882436327, 0.28355126402997727 },
		{ -0.03819006199148642, -0.9443105212554307, 0.3268320036523093 },
		{ 0.14676647865444564, -0.9150140379384877, 0.3757777416490735 },
		}, { 
		{ 0.18749694374711717, -0.9455332979615226, 0.26610463831263254 },
		{ -0.019879373252429066, -0.9593625838864632, 0.28147511996227925 },
		{ -0.031694687982131785, -0.9426030201136992, 0.33240787178743025 },
		{ 0.14483106236872506, -0.9134852550877577, 0.3802218459141987 },
		}, { 
		{ -0.04580736754110138, -0.9665796606215864, 0.252240846715224 },
		{ 0.007972034853946992, -0.9649757360943693, 0.26221799215427133 },
		{ 0.03425598856896587, -0.9615031848473968, 0.27265023890595713 },
		{ 0.06853166902397345, -0.9568959122891254, 0.28222973511866356 },
		}, 	};
		//*/
	
	
	// fibre positions to match Sam's targetting positions, then hand adjusted by Paul due to the W
	public double[][] channelR = { 
			{ 5.06, 5.07, 5.08, 5.09, }, 
			{ 5.06, 5.07, 5.08, 5.09, }, 
			//{ -1.234  }, //tube axis laser
		}; 
	public double[][][] fibreEndPos = { { 
		{ -0.9004460969964927, 6.7315331364030015, -0.267208888580648 },
		{ -0.8933134910223678, 6.731993915963584, -0.26788634913155 },
		{ -0.8925457382661305, 6.7312498244952135, -0.270162110510244 },
		{ -0.8993174351416117, 6.7303999785124615, -0.2712871836437615 },
	}, { 
		{ -0.9004461645001314, 6.731530300168183, -0.2672077593922265 },
		{ -0.893313853869483, 6.731999877834865, -0.2678879367223601 },
		{ -0.8925451886356279, 6.731243568400969, -0.2701599874983965 },
		{ -0.8993171612544141, 6.730393483830447, -0.27128469327080773 },
	}, /*{ 
		fibrePlanePos,
	}, 	*/}; 
public double[][][] fibreEndNorm = { { 
	{ 0.16814104935076746, -0.9490074852957616, 0.26667092150408456 },
	{ -0.010778118221499511, -0.9601928554998671, 0.27912992031420586 },
	{ -0.023159206297983535, -0.9407659051696137, 0.33826492994995067 },
	{ 0.14151427320208507, -0.9221533699765136, 0.3600095453193495 },
	}, { 
	{ 0.1740088835252065, -0.9488614983080023, 0.26340608474939253 },
	{ -0.004987047218466593, -0.9592191910196138, 0.28261930744327857 },
	{ -0.030914261919640577, -0.9403987917688414, 0.33866564757835577 },
	{ 0.1466494232417221, -0.9189905407507704, 0.3659922576687616 },
	}, /*{
		fibrePlaneNormal,
	},*/ 	};

	//*/

	public BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7() {
		this(false, false, Focus.BeamDump);
	}

	public BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7(boolean rotateToAET20, boolean adjustToLC3, Focus focus) {
		super("beamSpec-aet21-op2");
		this.rotateToAET20 = rotateToAET20;
		this.adjustToLC3 = adjustToLC3;
		this.focus = focus;
			
		
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
		addElement(window);
		addElement(lens4);
		addElement(fibrePlane);
		//addElement(new Sphere("bSphere", mirror1.getBoundarySphereCentre(), mirror1.getBoundarySphereRadius(), NullInterface.ideal()));
		
		dumpInfoForDesigner();
		
		//defocus HST fibres

		//+0 is focus on beam dumps,
		// +3-5mm is focus on M2,
		//-5mm focuses near L4 and misses lots of mirrors
		
		for(int j=0; j < channelR[0].length; j++) {
			fibreEndPos[0][j] = Util.plus(fibreEndPos[0][j], Util.mul(fibrePlaneNormal, focus.shift)); //refocus
		}
		
		// all fibres parallel to port axis 
		for(int i=0; i < channelR.length; i++) {
			for(int j=0; j < channelR[i].length; j++) {
				fibreEndNorm[i][j] = fibrePlaneNormal.clone();
			}
		}	
	
		//make neightbour fibres for scientific measurements
		/*double neighbourChannelShift = 0.000240;
		for(int j=0; j < channelR[0].length; j++) {
			int k = (j + 1) % channelR[0].length; //offset colours
			fibreEndPos[1][k] = Util.plus(fibreEndPos[0][j], Util.mul(fibresXVec, neighbourChannelShift));
			fibreEndNorm[1][k] = fibreEndNorm[0][j].clone();
		}#8
		
		
		//spit out cylinder mkaing code for ferrules
		double ferruleLen = 0.040;
		double ferruleRadius = 0.001500 / 2;		
		for(int i=0; i < channelR[0].length; i++){
			double u[] = fibreEndNorm[0][i];
			double p[] = Util.plus(fibreEndPos[0][i], Util.mul(u, -ferruleLen));
			p = Util.plus(p, Util.mul(fibresXVec, neighbourChannelShift / 2));
				 						
			System.out.println("Part.show(Part.makeCylinder("+ferruleRadius*1e3+","+ferruleLen*1e3 +","										
					+"FreeCAD.Vector("+p[0]*1e3+","+p[1]*1e3+","+p[2]*1e3+"), "
					+"FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+ "))); FreeCAD.ActiveDocument.ActiveObject.Label=\"Ferrule"+getDesignName()+"_"+i+"\";");
				
		}
		*/
		
		/*// Shift fibres +/- final lens
		 double shiftX = 0.003;
		for(int i=0; i < fibreEndPos[0].length; i++) {
			fibreEndPos[0][i] = Util.plus(fibreEndPos[0][i], Util.mul(fibresXVec, shiftX));
		}
		lens4.shift(Util.mul(fibresXVec, shiftX));
		//*/

		// Shift fibres +/- final lens
		/* double shiftPA = -0.010;
		 for(int iB=0; iB < fibreEndPos.length; iB++) {
			 for(int iF=0; iF < fibreEndPos[0].length; iF++) {
					fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(portAxis, shiftPA));
			 }
		}
		lens4.shift(Util.mul(portAxis, shiftPA));
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
		
		// rotate mirrors
		double rotMirror = -2.0 * Math.PI / 180;
		//mirror1.rotate(mirror1.getCentre(), Algorithms.rotationMatrix(mirror1.getUp(), rotMirror));
		//mirror2.rotate(mirror2.getCentre(), Algorithms.rotationMatrix(mirror2.getRight(), rotMirror));
		//*/
		
		//shift mirrors
		//double shift[] = Util.mul(fibresXVec, 0.003);
		//shift 2 mid lenses and field
		//mirror1.shift(shift);
		
		
		
		if(rotateToAET20) {
			lightPathsSystemName = lightPathsSystemName.replaceAll("AET21", "AET20");
			double[] rotAxis = { FastMath.cos(2*FastMath.PI / 5), FastMath.sin(2*FastMath.PI / 5), 0 }; 
			double rotMat[][] = Algorithms.rotationMatrix(rotAxis, FastMath.PI);
			/*for(int i=0; i < 3; i++)
				rotMat[i] = OneLiners.mul(rotMat[i], 1000);
			vrmlOut.setTransformationMatrix(rotMat);*/
			rotate(new double[] {0, 0,0,}, rotMat);
			beamPlane.rotate(new double[] {0, 0,0,}, rotMat);
			
			for(int i=0; i < fibreEndPos.length; i++) {
				for(int j=0; j < fibreEndPos[i].length; j++) {
					fibreEndPos[i][j] = Algorithms.rotateVector(rotMat, fibreEndPos[i][j]);
					fibreEndNorm[i][j] = Algorithms.rotateVector(rotMat, fibreEndNorm[i][j]);
				}				
			}
		}
		
		if(adjustToLC3) {
			//these are adjustments for AET20 (not 21)
			/*
			// Adjust only according to flange movement 	
			double[] b0 = { 4.44481591796875, 4.5028818359375, 0.09980704498291016 };
			double[] a3 = { 4.52733349609375, 4.82219189453125, 0.22024343872070312 };
			double[] b3 = { 4.44028173828125, 4.50640869140625, 0.09968163299560547 };
			
			double shift[] = Util.minus(a3, a0);
			
			double ab0[] = Util.minus(b0, a0);
			double ab3[] = Util.minus(b3, a3);
			double rotVec[] = Util.cross(ab3, ab0);
			double rotAng = FastMath.asin(Util.length(rotVec));
			rotVec = Util.reNorm(rotVec);
			double[][] rotMat = Algorithms.rotationMatrix(rotVec, rotAng);
			double rotCentre[] = a3.clone();
			*/
			
			// Adjust to Gunter's file, from preOct2020 design
			double[] shift = { -0.00455078125, 0.00353173828125, -0.000154632568359375 };
			double[] rotCentre = null;
			double[][] rotMat = null;
			
			
			shift(shift);
			if(rotMat != null)
				rotate(rotCentre, rotMat);
						
			for(int i=0; i < fibreEndPos.length; i++) {
				for(int j=0; j < fibreEndPos[i].length; j++) {
					fibreEndPos[i][j] = Util.plus(fibreEndPos[i][j], shift);

					if(rotMat != null) {
						fibreEndPos[i][j] = Util.plus(rotCentre, Algorithms.rotateVector(rotMat, Util.minus(fibreEndPos[i][j], rotCentre)));					
						fibreEndNorm[i][j] = Algorithms.rotateVector(rotMat, fibreEndNorm[i][j]);
					}
				}				
			}
		}
		
		setupFibrePlanes();
		
	}
	
	/** Remove tube components for alignment of carriage */
	public void carriageOnly() {
		//removing tube stuff for in-lab alignment 
		removeElement(mirror1);		
		removeElement(mirror2);
		removeElement(lens1);
		removeElement(lens2);
		removeElement(lens3);
		//beamPlane.setCentre(Util.plus(fibrePlanePos, Util.mul(portAxis, 0.200)));
		beamPlane.setCentre(Util.plus(lens3.getBackSurface().getCentre(), Util.mul(portAxis, -0.0001)));
		beamPlane.setNormal(portAxis.clone());
		
		//outPath += "/carriageOnly/";		
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
	
	public void dumpPositionsForLab() {
		//positions relative to objective, using port normal and M1-M2 as axes
		
		double x[] = portAxis;
		double yIsh[] = Util.reNorm(Util.minus(mirror2CentrePosPhys, mirror1CentrePosPhys));
		double z[] = Util.reNorm(Util.cross(x, yIsh));
		double y[] = Util.reNorm(Util.cross(z, x));
		
		
		double p0[] = lens3CentrePos;
		
		
		dumpPos("FibrePlane", fibrePlanePos, p0, x,y,z);		
		dumpPos("Obj4", lens4CentrePos, p0, x,y,z);	
		
		dumpPos("Window", windowCentrePos, p0, x,y,z);	
		
		dumpPos("Lens1", lens1CentrePos, p0, x,y,z);		
		dumpPos("Lens2", lens2CentrePos, p0, x,y,z);		
		dumpPos("Lens3", lens3CentrePos, p0, x,y,z);		
		
		dumpPos("Mirror2", mirror2CentrePosPhys, p0, x,y,z);
		dumpPos("Mirror1", mirror1CentrePosPhys, p0, x,y,z);
		
		dumpPos("Aperture", entryAperturePos, p0, x,y,z);
		
		
	}
		

	private void dumpPos(String name, double[] p, double[] p0, double[] x, double[] y, double[] z) {
		double dp[] = Util.minus(p, p0);
		
		System.out.println(name + ": x=" + (int)(Util.dot(dp, x)*1e3)
								+ "mm, y=" + (int)(Util.dot(dp, y)*1e3)
								+ "mm, z=" + (int)(Util.dot(dp, z)*1e3)
								+ "mm");
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
			System.out.println(String.format("%s position: (%5.3f, %5.3f, %5.3f) mm", 
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
	
	public String getDesignName() { 
		return (rotateToAET20 ? "aet20" : "aet21")
				+ "-hst-twoFlat-25mm" 
				+ "-focus" + focus.toString()
				+ (adjustToLC3 ? "-lc3" : "");	
	}
	
}
