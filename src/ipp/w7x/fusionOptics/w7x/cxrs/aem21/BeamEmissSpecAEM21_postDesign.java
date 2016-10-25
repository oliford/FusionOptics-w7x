package ipp.w7x.fusionOptics.w7x.cxrs.aem21;

import java.util.ArrayList;
import java.util.List;

import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import seed.matrix.DenseMatrix;
import algorithmrepository.Algorithms;
import algorithmrepository.exceptions.NotImplementedException;
import jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.EdmundOptics50mmAspheric;
import fusionOptics.lenses.Nikon135mmF28;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.lenses.ThorLabs100mmAspheric;
import fusionOptics.materials.BK7;
import fusionOptics.materials.IsotropicFixedIndexGlass;
import fusionOptics.materials.Sapphire;
import fusionOptics.optics.NodesAndElementsMesh;
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
public class BeamEmissSpecAEM21_postDesign extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ e_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	// CAD from designer
	
	
	public double portNormal[] = { 0.35536503, -0.14530594,  0.92336444 };
	
	public double virtualObsPos[] = { -0.5830873174901741,	5.362082222103293,	1.1250303063301719 }; //closest approach of all LOSs, from lightAssesment
									  
	public double windowCentre[] = { -0.47792361,  5.38480054,  1.17044202 };
	
	/***** Mirror/Shutter *****/
	
	public double mirrorDiameter = 0.120;
	
	public double mirrorAngleAdjust = 0 * Math.PI / 180; // Adjust of shutter open angle. 0 is default open, -60 is closed
	
	public double mirrorRingRotate = 0 * Math.PI / 180; //Adjustment of mirror mount ring
	
	public double mirrorCentrePos0[] = { -0.52209747,  5.40077637,  1.05967297 }; // shutter/mirror centre in default open position
	public double mirrorNormal0[] = { 0.95671975,  0.18248719,  0.22668426 }; // shutter/mirror normal in default open position
	
	public double mirrorPivotCentre[] = { -0.53505125,  5.39330481,  1.07630972 }; //pivot of shutter/mirror to open/close
	public double mirrorPivotVector[] = { -0.23614408,  0.94177839,  0.23935211 }; //pivot of shutter/mirror to open/close
	
	//rotate around shutter pivot
	public double mirrorCentrePos1[] = Util.plus(mirrorPivotCentre, 
										Algorithms.rotateVector(Algorithms.rotationMatrix(mirrorPivotVector, mirrorAngleAdjust), 
												Util.minus(mirrorCentrePos0, mirrorPivotCentre)));	
	public double mirrorNormal1[] = Algorithms.rotateVector(Algorithms.rotationMatrix(mirrorPivotVector, mirrorAngleAdjust), mirrorNormal0);
	
	//rotate around window (mouting ring)
	public double mirrorCentrePos[] = Util.plus(windowCentre, 
			Algorithms.rotateVector(Algorithms.rotationMatrix(portNormal, mirrorRingRotate), 
					Util.minus(mirrorCentrePos1, windowCentre)));

	public double mirrorNormal[] = Algorithms.rotateVector(Algorithms.rotationMatrix(portNormal, mirrorRingRotate), mirrorNormal1);

	
	public Disc mirror = new Disc("mirror", mirrorCentrePos, mirrorNormal, mirrorDiameter/2, Reflector.ideal());
	
	public STLMesh panelEdge = new STLMesh("panel", "/work/ipp/w7x/cad/aem21/panel-cutting-edge-channels-cut.stl");
	public STLMesh mirrorBlock = new STLMesh("mirrorBlock", "/work/ipp/w7x/cad/aem21/mirrorBlockSimpleOpen.stl");
	
	public STLMesh blockPlate = new STLMesh("blockPlate", "/work/ipp/w7x/cad/aem21/blockPlate-grooved.stl");
	

	
	public double opticAxis[] = portNormal;
	
	/***** Entry Window *****/
	
	
	public double windowDistBehindMirror = 0.170;
	public double entryWindowDiameter = 0.095; // 
	public double entryWindowThickness = 0.010; //
	public double entryWindowShift = 0.000;
	
	public double entryWindowFrontPos[] = Util.plus(windowCentre, Util.mul(opticAxis, entryWindowShift));
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	/**** Main Lens *****/

	public double lens1DistBehindWindow = 0.060;
	public double lens1Diameter = 0.090; //75
	public double lens1FocalLength = 0.180;
	public double lens1CentreThickness = 0.015; //0.00874;
	//public double lens1CurvatureRadius = 0.10336;
	public double lens1ClearAperture = 0.089; //0.0735;

	public double lensCentrePos[] = Util.plus(windowCentre, Util.mul(opticAxis, lens1DistBehindWindow + lens1CentreThickness));
	
	//public Nikon50mmF11 objLens = new Nikon50mmF11(lensCentrePos, 0.100 / 0.050, opticAxis);
	//public Iris objLensIris = new Iris("objLensIris", lensCentrePos, opticAxis, 0.100, objLens.getCaseRadius()*0.99, null, null, Absorber.ideal());
		
	//public Nikon135mmF28 objLens = new Nikon135mmF28(lensCentrePos, 0.050 / 0.050, opticAxis);	
	//public Iris objLensIris = new Iris("objLensIris", lensCentrePos, opticAxis, 0.100, 0.050*0.48, null, null, Absorber.ideal());

	//public ThorLabs100mmAspheric objLens = new ThorLabs100mmAspheric(lensCentrePos, opticAxis);	
	//public Iris objLensIris = new Iris("objLensIris", lensCentrePos, opticAxis, 0.100, 0.050*0.48, null, null, Absorber.ideal());
		
	public Medium lensMedium = new Medium(new BK7());  
	//public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lensCentrePos,
											opticAxis,
											lens1Diameter/2, // radius
											//lens1CurvatureRadius, // rad curv
											lens1FocalLength, // rad curv
											lens1CentreThickness,  
											lensMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lensIrisPos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, -0.005));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lens1Diameter, lens1ClearAperture/2, null, null, Absorber.ideal());
	
	/**** Lens2 *****/
	public double lens2DistBehindLens1 = 0.040;
	
	//public double lens2Diameter = 0.100;
	//public double lens2FocalLength = 0.200; // Would be better, NA~0.33, much better focus
	//public double lens2CentreThickness = 0.017;
	//public double lens2ClearAperture = 0.095;
	//public double lens2CurvatureRadius = 0.10350;

	public double lens2Diameter = 0.090; //75
	public double lens2FocalLength = 0.300;
	public double lens2CentreThickness = 0.015; //0.00874;
	//public double lens2CurvatureRadius = 0.10336;
	public double lens2ClearAperture = 0.089; //0.0735;
	
	public double lens2CentrePos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, lens2DistBehindLens1 + lens2CentreThickness));
	
	
	public Medium lens2Medium = new Medium(new BK7());  
	//public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens2",
											lens2CentrePos,
											opticAxis,
											lens2Diameter/2, // radius
//											lens2CurvatureRadius, // focal length
											lens2FocalLength, // focal length
											lens2CentreThickness, // centreThickness  
											lens2Medium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lens2IrisPos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, -0.005));
	public Iris lens2Iris = new Iris("lens2Iris", lens2IrisPos, opticAxis, lens2Diameter*2, lens2ClearAperture/2, null, null, Absorber.ideal());

	/**** Petzval *****/
	public double lensPVDistBehindLens1 = 0.110;
	public double lensPVDiameter = 0.100;
	
	public double lensPVCentrePos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, lensPVDistBehindLens1));
	
	public double focalLengthPV = -0.200; 
	
	public Medium lensPVMedium = new Medium(new BK7());  
	public SimplePlanarConvexLens lensPV = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lensPV",
											lensPVCentrePos,
											opticAxis,
											lensPVDiameter/2, // radius
											focalLengthPV, // focal length
											0.005, // centreThickness [ fromDesigner CAD for glass - although it's curvature doesn't match Jurgen's eBANF's focal length] 
											lensPVMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lensPVIrisPos[] = Util.plus(lensPVCentrePos, Util.mul(opticAxis, -0.005));
	public Iris lensPVIris = new Iris("lensPVIris", lensPVIrisPos, opticAxis, lensPVDiameter*2, lensPVDiameter*0.48, null, null, Absorber.ideal());
 	

	/*** Fibres ****/
	public double[][] channelR = { 
			{ 5.45, 5.51, 5.57, 5.63, 5.69, 5.75, 5.81, 5.87, 5.93, 5.99, 6.05, }, 
			{ 5.45, 5.51, 5.57, 5.63, 5.69, 5.75, 5.81, 5.87, 5.93, 5.99, 6.05, }, 
		}; 
		public double[][][] fibreEndPos = { { 
					{ -0.4151448562148828, 5.383456200196058, 1.371716341232199 },
					{ -0.4118090921337036, 5.378320359480315, 1.373420267927041 },
					{ -0.4087815271518238, 5.373102908799051, 1.374032565386316 },
					{ -0.4059844513040573, 5.367810818300312, 1.3740263296548019 },
					{ -0.40349982683165714, 5.362601194028755, 1.3730900873410141 },
					{ -0.4012883516778664, 5.3574992885644415, 1.3717434290780988 },
					{ -0.3991294349414123, 5.352504857205603, 1.3703992080386493 },
					{ -0.3975173470692666, 5.347981948898822, 1.367863244566594 },
					{ -0.39561417592570974, 5.3433730751744255, 1.366414812451498 },
					{ -0.3943147632485092, 5.339228326899611, 1.3637328108551927 },
					{ -0.39397640261119166, 5.335875269591658, 1.3592562503156131 },
				}, { 
					{ -0.42976462745291855, 5.37637014281222, 1.3716090639395835 },
					{ -0.42541451412368253, 5.371459106170163, 1.373837995183539 },
					{ -0.4210296371627697, 5.36629750970641, 1.3757544636267303 },
					{ -0.4171604846637566, 5.361049186954655, 1.3760056641226879 },
					{ -0.4135448809825214, 5.355818830864836, 1.375477340041584 },
					{ -0.41033300223487656, 5.35088633380428, 1.3739822755866795 },
					{ -0.407225209803147, 5.346172408173898, 1.3725328768829201 },
					{ -0.40486454005648403, 5.34192081315087, 1.3695685621042875 },
					{ -0.40243905108132294, 5.33782218171506, 1.3671058044949758 },
					{ -0.40025049222141684, 5.334022546493178, 1.3645795694947012 },
					{ -0.3992255804837451, 5.331256598470901, 1.3592162878263216 },
				}, 	}; 
		public double[][][] fibreEndNorm = { { 
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				}, { 
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				{ -0.35536503, 0.14530594, -0.92336444 },
				}, 	};
	
	public double[][] fibreFocus = null;
	
	public double[][] channelZ;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.22; // As AUG
	
	public double fibreEndDiameter = 0.000480; // as AUG
	public double fibrePlaneBehindLens2 = 0.090;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public int targetBeamIdx = 7; //Q8
	public double targetBeamR = 5.7;
	public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double beamAxis[] = W7xNBI.def().uVec(targetBeamIdx);
	
	
	public double fibrePlanePos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, fibrePlaneBehindLens2)); 
	public double fibresXVec[] = Util.reNorm(Util.cross(Util.cross(beamAxis, opticAxis),opticAxis));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, opticAxis));	
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, opticAxis, fibresYVec, 0.300, 0.300, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(opticAxis, 0.050)), 
										opticAxis, fibresYVec, 0.300, 0.300, Absorber.ideal());

	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 1.500, 2.000, NullInterface.ideal());
	
	double strayPos[] = Util.plus(mirrorCentrePos, Util.mul(portNormal, -0.250)); 
	
	public Disc strayPlane = new Disc("strayPlane", strayPos, portNormal, 0.200, Absorber.ideal());

	public Element tracingTarget = mirror;
		
	/** Set fibre positions equal spacing in holder */
	private void setupFibrePositions() {
		int nBeams = channelR.length;
		fibreEndPos = new double[nBeams][][];
		fibreEndNorm = new double[nBeams][][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			fibreEndPos[iB] = new double[nFibres][];
			fibreEndNorm[iB] = new double[nFibres][];
			
			double dX = -fibreEndDiameter;
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
	
	private void tiltFibres(){
		double tiltAng = 0 * Math.PI / 180;
		double tiltAng2 = 0 * Math.PI / 180;
		for(int iB=0; iB < channelR.length; iB++){
			for(int iF=0; iF < channelR[iB].length; iF++){
				double a[] = OneLiners.rotateVectorAroundAxis(tiltAng, mirrorPivotVector, fibreEndNorm[iB][iF]);
				double bVec[] = Util.reNorm(Util.cross(a, mirrorPivotVector));				
				a = OneLiners.rotateVectorAroundAxis(tiltAng2, bVec, a);
				fibreEndNorm[iB][iF] = a;
				
			}
		}
	}
	
	public BeamEmissSpecAEM21_postDesign() {
		super("beamSpec-aem21");
		
		for(Surface s : blockPlate.getSurfaces())
			s.setInterface(Reflector.ideal());

		mirrorBlock.rotate(mirrorCentrePos, Algorithms.rotationMatrix(portNormal, mirrorRingRotate));
		
		addElement(panelEdge);
		addElement(mirrorBlock);
				
		addElement(mirror);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lensIris);
		addElement(lens1);
		addElement(lens2Iris);
		addElement(lens2);
		//addElement(objLens);
		//addElement(objLensIris);
		//addElement(lensPVIris);
		//addElement(lensPV);
		addElement(fibrePlane);
		//addElement(shieldTiles);
		addElement(catchPlane);
		
		addElement(strayPlane);
		//addElement(blockPlate);
		
		//setupFibrePositions();
		tiltFibres();
		setupFibrePlanes();
		
		channelZ = new double[channelR.length][];
		for(int iB=0; iB < channelR.length; iB++){
			channelZ[iB] = new double[channelR[iB].length];
			for(int iF=0; iF < channelR[iB].length; iF++){
				channelZ[iB][iF] = W7xNBI.def().getPosOfBoxAxisAtR(0, channelR[iB][iF])[2];
			}
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());		
	}

	public String getDesignName() { return "aem21";	}

	public List<Element> makeSimpleModel() {
		return new ArrayList<Element>();
	}
	
	

}
