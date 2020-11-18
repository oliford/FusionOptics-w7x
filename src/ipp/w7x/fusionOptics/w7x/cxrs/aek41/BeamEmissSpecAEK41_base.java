package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import java.util.ArrayList;
import java.util.List;

import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import seed.matrix.DenseMatrix;
import algorithmrepository.Algorithms;
import algorithmrepository.exceptions.NotImplementedException;
import net.jafama.FastMath;
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
import fusionOptics.materials.FusedSilica;
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
public abstract class BeamEmissSpecAEK41_base extends Optic {
	public double globalUp[] = {0,0,1};
		
	public double flangeCentre[] = { -5.1112919921875, -6.455993896484375, 0.05283078002929688 };
	public double portNormal[] = { -0.62109906,  -0.74825539,  -0.23313049 };
	
	public double virtualObsPos[] = { -5.123078546180935,	-6.470663860518723,	0.048767601107390174 }; //closest approach of all LOSs, from lightAssesment
	
	public double windowCentre[] = Util.plus(flangeCentre, Util.mul(portNormal, +0.008));
	
	
	/** Optic axis tilt to match spatial calibration images (18.08.2017)*/
	public double tiltVertical = 0.20 * Math.PI / 180;
	public double tiltHorizontal = -0.22 * Math.PI / 180;
	
	public double rotVert[] = Util.reNorm(Util.cross(portNormal, globalUp));
	public double opticAxis0[] = OneLiners.rotateVectorAroundAxis(tiltVertical, rotVert, portNormal);
	public double opticAxis[] = OneLiners.rotateVectorAroundAxis(tiltHorizontal, globalUp, opticAxis0);	
	
	
	/***** Entry Window *****/
	
	public double entryWindowDiameter = 0.060; // DN63 CF Window
	public double entryWindowThickness = 0.005; // Guessed
	public double entryWindowShift = 0.000; 
	
	public double entryWindowFrontPos[] = Util.plus(windowCentre, Util.mul(opticAxis, entryWindowShift));
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	/**** Main Lens *****/
	//Thor Labs LA4795-ML Fused Silica lens, 
	public double lens1DistBehindWindow = 0.028;
	public double lens1Diameter = 0.075;
	public double lens1CentreThickness = 0.011;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.092;
	public double lens1ClearAperture = 0.0675;

	public double lensCentrePos[] = Util.plus(windowCentre, Util.mul(opticAxis, lens1DistBehindWindow + lens1CentreThickness));
	
	public Medium lensMedium = new Medium(new FusedSilica());  
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	//public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lensCentrePos,
											opticAxis,
											lens1Diameter/2, // radius
											lens1CurvatureRadius, // rad curv
//											lens1FocalLength, // rad curv
											lens1CentreThickness,  
											lensMedium, 
											IsoIsoInterface.ideal()
											);//,designWavelenth);
	
	public double lensIrisPos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, -0.005));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lens1Diameter, lens1ClearAperture/2, null, null, Absorber.ideal());
	

	/*** Fibres ****/
	public int beamIdx[] = null;
	public double[][] channelR = null;
	public double[][][] fibreEndPos = null;
	public double[][][] fibreEndNorm = null;
		
	public double[][] fibreFocus = null;
	
	public double[][] channelZ;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.22; // As AUG
	
	
	public double fibrePlaneBehindLens = 0.200;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	
	
	public double fibrePlanePos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, fibrePlaneBehindLens)); 
	public double fibresXVec[] = Util.reNorm(new double[]{ 0.0208466796875, 0.02511328125, -0.13614230346679687 });
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, opticAxis));	
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, opticAxis, fibresYVec, 0.300, 0.300, NullInterface.ideal());
	public Square fibrePlanes[][];
	


	public Element tracingTarget = entryWindowFront;
	
	public Surface checkSurface = lens1.getFrontSurface();
	

	/** Plasma radiating surface for heat-load analysis */
	public double[] portCentreAtPanel = { -3.850013427734375, -4.8233515625, 0.368464647769928 };
	//public double[] radSurfaceNormal = {  -6.38700537e-01, -7.69455340e-01,  3.21739182e-04 }; //port axis
	public double[] radSurfaceNormal = { -0.46565936, -0.81304686,  0.34945123 }; //panel normal -ish
	public double[] radSurfaceCentre = Util.plus(portCentreAtPanel, Util.mul(radSurfaceNormal, -0.100));
	public double[] radRight = Util.reNorm(Util.cross(radSurfaceNormal, new double[] { 0,0,1 }));
	public double[] radUp = Util.reNorm(Util.cross(radRight, radSurfaceNormal));
	
	public double radSurfWidth = 0.700; //for testing closed shutter
	public double radSurfHeight = 1.100;

	public Square radSurface = new Square("radSurface", radSurfaceCentre, radSurfaceNormal, radUp, radSurfHeight, radSurfWidth, NullInterface.ideal()); 
	
			
	protected abstract void setupFibrePositions();
			
		
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
	
	private void adjustFibres(){
		double tiltAng = 0 * Math.PI / 180;
		double tiltAng2 = 0 * Math.PI / 180;
		//double shift = 0.015; //for lens2DistBehindLens1 = 60mm
		double shift = 0.000; //for lens2DistBehindLens1 = 40mm
		//double shift = -0.014; //for lens2DistBehindLens1 = 20mm
		double shiftR = 0.000, shiftU = 0.000; //for tolerance testing
		
		double r[] = Util.reNorm(Util.minus(fibreEndPos[0][fibreEndPos[0].length-1], fibreEndPos[0][0]));
		double u[] = Util.reNorm(Util.cross(r, portNormal));
		r = Util.reNorm(Util.cross(portNormal, u));
		/*
		for(int iB=0; iB < channelR.length; iB++){
			for(int iF=0; iF < channelR[iB].length; iF++){
				double a[] = OneLiners.rotateVectorAroundAxis(tiltAng, mirrorPivotVector, fibreEndNorm[iB][iF]);
				double bVec[] = Util.reNorm(Util.cross(a, mirrorPivotVector));				
				a = OneLiners.rotateVectorAroundAxis(tiltAng2, bVec, a);
				fibreEndNorm[iB][iF] = a;
				
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(portNormal, shift)); 
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(r, shiftR)); 
				fibreEndPos[iB][iF] = Util.plus(fibreEndPos[iB][iF], Util.mul(u, shiftU)); 
				
			}
		}
		*/
	}
	
	public BeamEmissSpecAEK41_base() {
		super("beamSpec-aek21");
				
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lensIris);
		addElement(lens1);
		
		addElement(fibrePlane);
		
		setupFibrePositions();
		//adjustFibres();
		setupFibrePlanes();
		
		channelZ = new double[channelR.length][];
		for(int iB=0; iB < channelR.length; iB++){
			channelZ[iB] = new double[channelR[iB].length];
			for(int iF=0; iF < channelR[iB].length; iF++){
				channelZ[iB][iF] = W7xNBI.def().getPosOfBoxAxisAtR(0, channelR[iB][iF])[2];
			}
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		/*rotate(new double[3], rotateLC3);
		
		shift(offsetLC3);
		
		for(int iB=0;iB<fibreEndPos.length;iB++){
			for(int iF=0; iF < fibreEndPos[iB].length; iF++){
			
				double newVec1[] = new double[3], newVec2[] = new double[3];
				for(int j=0; j < 3; j++){
					for(int k=0; k < 3; k++){
						newVec1[j] += rotateLC3[j][k] * fibreEndNorm[iB][iF][k];
						newVec2[j] += rotateLC3[j][k] * fibreEndPos[iB][iF][k];
					}
					
					newVec2[j] += offsetLC3[j];
				}
				fibreEndNorm[iB][iF] = newVec1;
				//fibreEndPos[iB][iF] = newVec2;
			}			
		}
		*/
	}

	public abstract String getDesignName();

	public List<Element> makeSimpleModel() {
		return new ArrayList<Element>();
	}
}
