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
	
	public boolean rotateToAET20 = false;
	public boolean adjustToLC3 = false;
	
	public enum Focus {		
		BeamDump(0.0),
		M1(0.005),
		L3(-0.005);
		
		Focus(double shift) { this.shift = shift; }		
		public double shift;		
	}
	
	public Focus focus;
	
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
	public double entryApertureDiameter = 0.025;
	
	/**** Mirror ****/		
	public double mirror1FromFront = 0.130;
	public double mirror1PortRightShift = 0.000;	// 0.000 in preOct2020
	public double mirror1PortUpShift = 0.060;// + 0.004;		// 0.060 in preOct2020
	public double mirror1Width = 0.057;
	public double mirror1Height = 0.043;
	public double mirror1InPlaneRotate = 20 * Math.PI / 180;
	public double mirror1InPlaneShiftUp = -0.003;
	public double mirror1CentrePos[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror1FromFront),
																		Util.plus(Util.mul(portRight, mirror1PortRightShift),
																				  Util.mul(portUp, mirror1PortUpShift))));	
	public double mirror2FromFront = 0.140;
	public double mirror2PortRightShift = -0.070;// - 0.00576;	// -0.070 in preOct2020
	public double mirror2PortUpShift = 0.030;	// 0.030 in preOct2020
	public double mirror2Width = 0.1068;
	public double mirror2Height = 0.0568;
	public double mirror2InPlaneRotate = -20 * Math.PI / 180;
	public double mirror2InPlaneShiftRight = 0.010; //shift with changing targeting
	public double mirror2InPlaneShiftUp = -0.004;
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

	public double windowCentrePos[] = Util.plus(lens3CentrePos, Util.mul(lensNormal, windowFromLens3));

	public double lens4CentrePos[] = Util.plus(lens3CentrePos, Util.mul(lensNormal, lens4FromLens3));

	public double mirror1Normal[] = Util.reNorm(Util.mul(Util.plus(Util.mul(mirror12Vec, -1), observationVec), 0.5));
	public double mirror1Right0[] = Util.reNorm(Util.cross(mirror1Normal, globalUp));
	public double mirror1Up0[] = Util.reNorm(Util.cross(mirror1Right0, mirror1Normal));
	
	public double mirror1Right[] = Util.plus(Util.mul(mirror1Right0, FastMath.cos(mirror1InPlaneRotate)),
											Util.mul(mirror1Up0, FastMath.sin(mirror1InPlaneRotate)));
	public double mirror1Up[] = Util.reNorm(Util.cross(mirror1Right, mirror1Normal));

	
	public double mirror2Normal[] = Util.reNorm(Util.mul(Util.plus(lensNormal, mirror12Vec), 0.5));
	
	public double mirror2Right0[] = Util.reNorm(Util.cross(mirror2Normal, globalUp));
	public double mirror2Up0[] = Util.reNorm(Util.cross(mirror2Right0, mirror2Normal));
	
	public double mirror2Right[] = Util.plus(Util.mul(mirror2Right0, FastMath.cos(mirror2InPlaneRotate)),
											Util.mul(mirror2Up0, FastMath.sin(mirror2InPlaneRotate)));
	public double mirror2Up[] = Util.reNorm(Util.cross(mirror2Right, mirror1Normal));

	
	public double mirror2CentrePosPhys[] = Util.plus(Util.plus(mirror2CentrePos0, Util.mul(mirror2Right, mirror2InPlaneShiftRight)),
												Util.mul(mirror2Up, mirror2InPlaneShiftUp));
	
	
	public double entryAperturePos[] = Util.plus(mirror1CentrePos, Util.mul(observationVec, entryApertureMirrorDist));
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
		
	public Square mirror1 = new Square("mirror1", mirror1CentrePos, mirror1Normal, mirror1Up, mirror1Height, mirror1Width, Reflector.ideal());
	//public Disc mirror1 = new Disc("mirror1", mirror1CentrePos, mirror1Normal, mirror1Width/2, Reflector.ideal());
	//public Dish mirror1 = new Dish("mirror1", mirror1CentrePos, mirror1Normal, 0.380, mirror1Width/2, Reflector.ideal());

	public Square mirror2 = new Square("mirror2", mirror2CentrePosPhys, mirror2Normal, mirror2Up, mirror2Height, mirror2Width, Reflector.ideal());
	//public Dish mirror2 = new Dish("mirror2", mirror2CentrePos, mirror2Normal, 0.100, mirror2Width/2, Reflector.ideal());

	/*** Fibre plane ****/
	//public double fibrePlaneFromLens4 = lens4FocalLength + 0.0043; //25mm obj
	public double fibrePlaneFromLens4 = lens4FocalLength + 0.000; //25mm obj
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
	public double[] radSurfaceCentre = { -0.6759374 ,  5.97114095, -0.09727827 };	
	public double[] radSurfaceNormal = { -0.12153662,  0.95077941,  0.28503923  };
	
	public double[] radUp = Util.createPerp(radSurfaceNormal);
	//public double radSurfWidth = 1.100; //for testing inner parts (window etc)
	//public double radSurfHeight = 0.900;

	public double radSurfWidth = 1.000; //for testing closed shutter
	public double radSurfHeight = 0.900;

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
	public double[][] channelR = { 
			{ 5.06, 5.07, 5.08, 5.09, }, 
			{ 5.06, 5.07, 5.08, 5.09, }, 
		}; 
	/*//16mm preOct2020
		public double[][][] fibreEndPos = { { 
					{ -0.8983385867229151, 6.720031117153394, -0.2643311419045835 },
					{ -0.8935109068506887, 6.72040283940562, -0.26447805086864545 },
					{ -0.8931427911105558, 6.719993543781674, -0.2657761902656337 },
					{ -0.8973877316837462, 6.7193212510363525, -0.2668494936381046 },
				}, { 
					{ -0.8983388186474494, 6.7200332749908425, -0.2643315623422543 },
					{ -0.8935112144434686, 6.7204047553749575, -0.2644787563640594 },
					{ -0.8931430575364232, 6.719995344180738, -0.26577720287481466 },
					{ -0.8973878289708143, 6.719324012792593, -0.2668505115301608 },
				}	}; 
		public double[][][] fibreEndNorm = { { 
				{ 0.1757394639965727, -0.945548062438551, 0.2739607680177933 },
				{ -0.003079346099739016, -0.9593754550551475, 0.28211567461827886 },
				{ -0.019523076108710062, -0.9441559753571445, 0.32891996548803903 },
				{ 0.13583317401848546, -0.9178106537579502, 0.37305891315510464 },
				}, { 
				{ 0.17404219401625728, -0.9450006704659665, 0.2769170408640126 },
				{ 0.003073035489538602, -0.9586220454691418, 0.2846652953793956 },
				{ -0.019384078112535397, -0.9478246931330538, 0.3182021505316385 },
				{ 0.13426461051401653, -0.9207725678092548, 0.3662661501333024 },
				}	};
		//*/
	//25mm preOct2020
		public double[][][] fibreEndPos = { { 
					{ -0.9009305233119985, 6.731366336378546, -0.26742848649473516 },
					{ -0.8929907295215366, 6.7320439835283095, -0.267688855215448 },
					{ -0.8923858859390571, 6.731352225656039, -0.2698219517809947 },
					{ -0.8993791955218674, 6.730264250358019, -0.27159830331056445 },
				}, { 
					{ -0.9009307372765338, 6.731368412536851, -0.267429551536691 },
					{ -0.8929906987905285, 6.732041916264743, -0.2676879011411546 },
					{ -0.892386285029392, 6.731353300026105, -0.269823240821865 },
					{ -0.8993799679373151, 6.730270105601281, -0.271599685494157 },
				}	}; 
		public double[][][] fibreEndNorm = { { 
				{ 0.1900531683118855, -0.9431553995951937, 0.27264938186074195 },
				{ -0.013092762190143456, -0.961021905718204, 0.27616204718965937 },
				{ -0.0352033756959906, -0.943052386907028, 0.33077623537455675 },
				{ 0.1480053971143777, -0.9142891681988752, 0.37705400056122507 },
				}, { 
				{ 0.18688873353262003, -0.9436267618431343, 0.2732052993849384 },
				{ -0.01523532220794472, -0.9598150850627735, 0.2802193559395227 },
				{ -0.03326130150560207, -0.9457532133205677, 0.3231788132226094 },
				{ 0.14016007633829594, -0.9161359100327776, 0.37556643533369394 },
				} 	};
	//*/
	
	// shifted to Gunter's AET20
	/*	public double[][][] fibreEndPos = { { 
					{ -0.8951957863092375, 6.731787019474719, -0.2674840019033479 },
					{ -0.8872764626713161, 6.732487615193336, -0.2676479662666826 },
					{ -0.8866461398914198, 6.731812513479982, -0.26977186745518705 },
					{ -0.8935940516033365, 6.730698213508666, -0.2716239097588454 },
				}, { 
					{ -0.8951962464192974, 6.731789365774185, -0.26748490328306596 },
					{ -0.8872772476005344, 6.732498598219873, -0.26765218238990646 },
					{ -0.8866464163964496, 6.73180607805042, -0.2697703646741089 },
					{ -0.8935938704036769, 6.73070045086459, -0.271625624876001 },
				}, { 
					{ -0.8864748054223605, 6.732676594533867, -0.26671130855467934 },
					{ -0.8881441175866978, 6.732695084331736, -0.2669448776824963 },
					{ -0.8890541516995684, 6.732654009054289, -0.267084411345618 },
					{ -0.890966409103881, 6.732464676747568, -0.2674189957010567 },
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ 0.19068143270087662, -0.9440970238816552, 0.2689263853193705 },
				{ -0.02068484715191355, -0.9602653393304063, 0.2783210649213635 },
				{ -0.030596987590113124, -0.9425067636824553, 0.3327834502544183 },
				{ 0.1477663186316227, -0.9143646716044272, 0.3769646699622998 },
				}, { 
				{ 0.18728195534202793, -0.9451609817479306, 0.26757463965135914 },
				{ -0.009521714343746717, -0.9602812858496559, 0.27887127676203494 },
				{ -0.0387663439489722, -0.9442321993315137, 0.3269904040218959 },
				{ 0.14916318065211534, -0.9159314401822084, 0.3725854833772914 },
				}, { 
				{ -0.037308902990487385, -0.9691802474631286, 0.24351117774130013 },
				{ 0.005703160040360712, -0.9657930696372093, 0.25925126924721215 },
				{ 0.031762586775497245, -0.9633628488953431, 0.2663140241323716 },
				{ 0.07794919312453007, -0.9584358124415837, 0.2744534873538248 },
				}, 	};
*/
	
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
		for(int j=0; j < channelR[0].length; j++) {
			int k = (j + 1) % channelR[0].length; //offset colours
			fibreEndPos[1][k] = Util.plus(fibreEndPos[0][j], Util.mul(fibresXVec, 0.000500));
			fibreEndNorm[1][k] = fibreEndNorm[0][j].clone();
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
		double yIsh[] = Util.reNorm(Util.minus(mirror2CentrePosPhys, mirror1CentrePos));
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
		dumpPos("Mirror1", mirror1CentrePos, p0, x,y,z);
		
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
	
	public String getDesignName() { 
		return (rotateToAET20 ? "aet20" : "aet21")
				+ "-hst-twoFlat-25mm" 
				+ "-focus" + focus.toString()
				+ (adjustToLC3 ? "-lc3" : "");	
	}
	
}
