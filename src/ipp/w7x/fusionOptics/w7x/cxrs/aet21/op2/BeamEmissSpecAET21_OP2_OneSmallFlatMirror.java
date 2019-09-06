package ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2;

import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;

import org.apache.commons.math3.util.FastMath;

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
public class BeamEmissSpecAET21_OP2_OneSmallFlatMirror extends Optic {
	public String lightPathsSystemName = "AET21";
	
	
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9;	
	
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
	public double targetBeamR = 5.725;
	//public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double targetObsPos[] = W7xNBI.def().getPosOfBoxAxisAtR(targetBoxIdx, targetBeamR);

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
	
	public double mirror1FromFront = 0.110;
	public double mirror1PortRightShift = 0.080;	
	public double mirror1PortUpShift = 0.040;
	public double mirror1Width = 0.100;
	public double mirror1Height = 0.030;
	public double mirror1InPlaneRotate = 15 * Math.PI / 180;
	public double mirror1InPlaneShiftRight = -0.015;
	public double mirror1CentrePos0[] = Util.plus(frontDiscCentre, Util.plus(Util.mul(portAxis, -mirror1FromFront),
																		Util.plus(Util.mul(portRight, mirror1PortRightShift),
																				  Util.mul(portUp, mirror1PortUpShift))));
	
	/**** Lenses ****/
	//https://www.edmundoptics.com/p/100mm-dia-x-300mm-focal-length-pcx-condenser-lens/1011/
	//public double lens1FocalLength = 0.300; //291mm
	public double lens1CurvatureRadius = 0.15500;
	public double lens1ClearAperture = 0.09700;	
	public double lens1Thickness = 0.0125;
	public double lens1Diameter = 0.100;
	public double lens1FromMirror = 0.080;
	public double lens1PortRightShift = 0.000;	
	public double lens1PortUpShift = 0.000;	
	
	/*public double lens1FocalLength = 0.300;
	public double lens1Thickness = 0.010;
	public double lens1Diameter = 0.100;
	public double lens1FromMirror = 0.080;
	public double lens1PortRightShift = 0.000;	
	public double lens1PortUpShift = 0.000;*/	

	//public double lens2FocalLength = 0.300;
	public double lens2CurvatureRadius = 0.15500;
	public double lens2ClearAperture = 0.09700;	
	public double lens2Thickness = 0.0125;
	public double lens2Diameter = 0.100;
	public double lens2FromLens1 = 0.060;
	public double lens2PortRightShift = 0.000;	
	public double lens2PortUpShift = 0.000;	
	
	/*public double lens2Thickness = 0.010;
	public double lens2Diameter = 0.100;
	public double lens2FromLens1 = 0.060;
	public double lens2PortRightShift = 0.000;	
	public double lens2PortUpShift = 0.000;*/	

	//https://www.edmundoptics.com/p/100mm-dia-x-200mm-focal-length-pcx-condenser-lens/1010/	
	//public double lens1FocalLength = 0.200; //189mm
	public double lens3CurvatureRadius = 0.103500;
	public double lens3ClearAperture = 0.09700;	
	public double lens3Thickness = 0.017;
	public double lens3Diameter = 0.100;
	public double lens3FromLens2 = 0.160;
	public double lens3PortRightShift = 0.000;	
	public double lens3PortUpShift = 0.000;	
	

	/*public double lens3FocalLength = 0.200;
	public double lens3Thickness = 0.015;
	public double lens3Diameter = 0.100;
	public double lens3FromLens2 = 0.160;
	public double lens3PortRightShift = 0.000;	
	public double lens3PortUpShift = 0.000;	
	 */
	
	public double lens4FocalLength = 0.025;
	public double lens4FromLens3 = 0.150;
		
	
	/*** Positions/vectors ****/
		
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, mirror1CentrePos0));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVecBox(targetBoxIdx), observationVec));
	
	
	public double lens1CentrePos[] = Util.plus(mirror1CentrePos0, Util.plus(Util.mul(portAxis, -lens1FromMirror),
			Util.plus(Util.mul(portRight, lens1PortRightShift),
					  Util.mul(portUp, lens1PortUpShift))));

	public double lensNormal[] = Util.reNorm(Util.minus(lens1CentrePos, mirror1CentrePos0));
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(lensNormal, lens2FromLens1));
	public double lens3CentrePos[] = Util.plus(lens2CentrePos, Util.mul(lensNormal, lens3FromLens2));
	public double lens4CentrePos[] = Util.plus(lens3CentrePos, Util.mul(lensNormal, lens4FromLens3));

	public double mirror1Normal[] = Util.reNorm(Util.mul(Util.plus(lensNormal, observationVec), 0.5));
	public double mirror1Right0[] = Util.reNorm(Util.cross(mirror1Normal, globalUp));
	public double mirror1Up0[] = Util.reNorm(Util.cross(mirror1Right0, mirror1Normal));
	
	public double mirror1Right[] = Util.plus(Util.mul(mirror1Right0, FastMath.cos(mirror1InPlaneRotate)),
											 Util.mul(mirror1Up0, FastMath.sin(mirror1InPlaneRotate)));
	public double mirror1Up[] = Util.reNorm(Util.cross(mirror1Right, mirror1Normal));
	
	public double mirror1CentrePosPhys[] = Util.plus(mirror1CentrePos0, Util.mul(mirror1Right, mirror1InPlaneShiftRight));
	
	public double entryAperturePos[] = Util.plus(mirror1CentrePos0, Util.mul(observationVec, entryApertureMirrorDist));
	public Iris entryAperture = new Iris("entryAperture", entryAperturePos, observationVec, 3*entryApertureDiameter/2, entryApertureDiameter*0.495, Absorber.ideal());
	public Disc entryTarget = new Disc("entryTarget", entryAperturePos, observationVec, 0.505*entryApertureDiameter, NullInterface.ideal());
	
	public Medium lens1Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens1", 
			lens1CentrePos, lensNormal, lens1Diameter/2, lens1CurvatureRadius, lens1Thickness, lens1Medium, IsoIsoInterface.ideal());
	public Iris lens1Iris = new Iris("lens1Iris", lens1CentrePos, lensNormal, lens1Diameter*0.7, lens1ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens2Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens2", 
			lens2CentrePos, lensNormal, lens2Diameter/2, lens2CurvatureRadius, lens2Thickness, lens2Medium, IsoIsoInterface.ideal());
	public Iris lens2Iris = new Iris("lens2Iris", lens2CentrePos, lensNormal, lens2Diameter*0.7, lens2ClearAperture/2, null, null, Absorber.ideal());

	public Medium lens3Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens3 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness("lens3", 
			lens3CentrePos, lensNormal, lens3Diameter/2, lens3CurvatureRadius, lens3Thickness, lens3Medium, IsoIsoInterface.ideal());
	public Iris lens3Iris = new Iris("lens3Iris", lens3CentrePos, lensNormal, lens3Diameter*0.7, lens3ClearAperture/2, null, null, Absorber.ideal());

	Nikon50mmF11 lens4 = new Nikon50mmF11(lens4CentrePos, lens4FocalLength / 0.050, lensNormal);
	
	
	public Square mirror1 = new Square("mirror1", mirror1CentrePosPhys, mirror1Normal, mirror1Up, mirror1Height, mirror1Width, Reflector.ideal());
	//public Disc mirror1 = new Disc("mirror1", mirror1CentrePos, mirror1Normal, mirror1Width/2, Reflector.ideal());
	//public Dish mirror1 = new Dish("mirror1", mirror1CentrePos, mirror1Normal, 0.380, mirror1Width/2, Reflector.ideal());

	/*** Fibre plane ****/
	public double fibrePlaneFromLens4 = lens4FocalLength + 0.0045;
	public double fibrePlanePos[] = Util.plus(lens4CentrePos, Util.mul(lensNormal, fibrePlaneFromLens4));
	public double fibrePlaneSize = 0.040;
	
	
	public double fibrePlaneNormal[] = Util.mul(lensNormal, -1);
	public double fibresXVec0[] = Util.reNorm(Util.cross(fibrePlaneNormal, globalUp));
	public double fibresYVec0[] = Util.reNorm(Util.cross(fibresXVec0, fibrePlaneNormal));
	public double fibreRotation = -5 * Math.PI / 180;
	public double fibresXVec[] = Algorithms.rotateVector(Algorithms.rotationMatrix(fibrePlaneNormal, fibreRotation), fibresXVec0);
	public double fibresYVec[] = Algorithms.rotateVector(Algorithms.rotationMatrix(fibrePlaneNormal, fibreRotation), fibresYVec0);
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, fibrePlaneNormal, fibresYVec, fibrePlaneSize, fibrePlaneSize, Absorber.ideal());
	
	public double beamAxis[] = Util.reNorm(Util.plus( 
			W7xNBI.def().uVec(W7xNBI.BEAM_Q7),
			W7xNBI.def().uVec(W7xNBI.BEAM_Q8)));
	
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, globalUp));
	public double beamObsPlaneUp[] = Util.reNorm(Util.cross(beamObsPlaneNormal, beamAxis));
			
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPlaneUp, 0.500, 1.200, NullInterface.ideal());
	
	public Square strayPlane = null;
	public Cylinder rod = null;
		
	public STLMesh panelEdge = new STLMesh("panel", "/work/cad/aet21/conflicting-panel-aet21.stl");
	
	public Element tracingTarget = entryTarget;
	
	
	
	/** Fibres */
	
	public int beamIdx[] = { W7xNBI.BEAM_Q7 };
	//public double[] channelR = OneLiners.linSpace(5.38, 5.88, nFibres);
	public String[] lightPathRowName = null;
	
	public double fibreNA = 0.22; // As AUG	
	public double fibreEndDiameter = 0.000470; // from ceramOptec offer, with polymide jacket (470µm), without Tefzel (550µm)
	public double fibreSpacing = 0.000550; // spacing is a bit worse, all 20 fibres were about 11mm in total
	
	public double[][][] fibreEndPos;
	public double[][][] fibreEndNorm;
	
	public Square fibrePlanes[][] = {{
			
	}};

	public double[][] fibreFocus = {{ 
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,		 
		}};
	
	public double[][] channelR = {{ 
		5.55 , 5.571, 5.592, 5.613, 5.634,
		5.655, 5.676, 5.697, 5.718, 5.739,
		5.761, 5.782, 5.803, 5.824, 5.845,
		5.866, 5.887, 5.908, 5.929, 5.95 }};

	
	private void setupFibrePositions() {
		int nBeams = channelR.length;
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			
			double dX = -fibreSpacing;
			double x0 = -(nFibres-1)/2 * dX; 
			for(int iF=0; iF < nFibres; iF++){
				fibreEndPos[iB][iF] = Util.plus(fibrePlanePos, Util.mul(fibresXVec, x0 + iF * dX));	
						
				fibreEndNorm[iB][iF] = fibrePlane.getNormal().clone();
			}
			if(fibreFocus != null){
				for(int iF=0; iF < nFibres; iF++){
					fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(fibrePlane.getNormal(), fibreFocus[iB][iF]));
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
	public BeamEmissSpecAET21_OP2_OneSmallFlatMirror() {
		super("beamSpec-aet21-op2");
		
		//addElement(frontDisc);
		addElement(panelEdge);
		addElement(targetSphere);
		addElement(entryTarget);
		addElement(entryAperture);
		//addElement(portTubeCyld);
		addElement(mirror1);		
		addElement(lens1Iris);
		addElement(lens1);
		addElement(lens2Iris);
		addElement(lens2);
		addElement(lens3Iris);
		addElement(lens3);
		addElement(lens4);
		addElement(fibrePlane);
		//addElement(new Sphere("bSphere", mirror1.getBoundarySphereCentre(), mirror1.getBoundarySphereRadius(), NullInterface.ideal()));

		setupFibrePositions();
		setupFibrePlanes();
		
	}

	public String getDesignName() { return "aet21-op2-oneFlat";	}
		

}
