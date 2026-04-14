package ipp.w7x.fusionOptics.w7x.cxrs.aek41;

import java.util.ArrayList;
import java.util.List;

import ipp.w7x.fusionOptics.w7x.cxrs.ObservationSystem;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import uk.co.oliford.jolu.OneLiners;
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
public abstract class BeamEmissSpecAEK41_base extends ObservationSystem  {	
	public double globalUp[] = {0,0,1};
		
	//public double flangeCentre[] = { -5.1112919921875, -6.455993896484375, 0.05283078002929688 };
	public double flangeCentre[] = { -5.1113217773437, -6.455943603515625, 0.05281460571289063 };
	public double portNormal[] = { -0.62109906,  -0.74825539,  -0.23313049 };
	
	public double virtualObsPos[] = { -5.123078546180935,	-6.470663860518723,	0.048767601107390174 }; //closest approach of all LOSs, from lightAssesment
	
	public double windowCentre[] = Util.plus(flangeCentre, Util.mul(portNormal, +0.008));
	
	public Disc entryWindowFront;
	public Disc entryWindowBack;
	public Iris entryWindowIris;

	//Thor Labs LA4795-ML Fused Silica lens, 
	// (Maybe modified in constructor)
	double lens1DistBehindWindow = 0.028;
	double lens1Diameter = 0.075;
	double lens1CentreThickness = 0.011;
	//double lens1FocalLength = 0.200;
	double lens1CurvatureRadius = 0.092;
	double lens1ClearAperture = 0.0675;
	
	public enum AlignmentState {
		OP12_design,
		OP21_design,
		OP22_design,
		OP22_measured,
	}
		
	public Surface losStartSurface;

	/*** Fibres ****/
	public int beamIdx[] = null;
	public double[][] channelR = null;
	public double[][][] fibreEndPos = null;
	public double[][][] fibreEndNorm = null;
		
	public double[][] fibreFocus = null;
	
	public double[][] channelZ;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.22; // standard
	public double fibreDiameter = 0.000400; // standard
	
	public double fibrePlaneBehindLens;
	
	double[] lensCentrePos;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public double[] fibrePlanePos;
	public double[] fibresXVec;
	public double[] fibresYVec;
	public Square fibrePlane;
	public Square fibrePlanes[][];

	public Element tracingTarget;
	public Surface checkSurface;
	

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
	
	public double overrideObsPositions[][][] = null;
	
	public double getFibreNA(int iB, int iP) { return fibreNA;	}
	public double getFibreDiameter(int iB, int iP) { return fibreDiameter; }
		
	protected abstract void setupFibrePositions();
	
	public Surface strayPlane = null;

	public AlignmentState alignmentState;	

	public String[] backgroundSTLFiles() {
		return new String[] { 
			"/work/ipp/w7x/cad/aek41/bg-targetting/pumpslot-m4.off-aek41-cut.stl",
			"/work/ipp/w7x/cad/aek41/bg-targetting/target-m4.off-aek41-cut.stl"
		};
	};
		
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
	
	public BeamEmissSpecAEK41_base(AlignmentState alignment) {
		super("beamSpec-aek21");
		this.alignmentState = alignment;
		
		double tiltVertical, tiltHorizontal, tiltInPlane;
		
		switch(alignment) {
		case OP12_design:
			/** OP1.2 Optic axis tilt to match spatial calibration images (18.08.2017)*/
			fibrePlaneBehindLens = 0.200;
			tiltVertical = 0.20 * Math.PI / 180;
			tiltHorizontal = -0.22 * Math.PI / 180;
			tiltInPlane = 0.0 * Math.PI / 180;
			break;
		
		case OP21_design:
			/** OP2.1 Tilted up a bit to compensate for swtiching sides of edgeVIS on backplate and to miss the portliner */
			fibrePlaneBehindLens = 0.200;
			tiltVertical = -0.2 * Math.PI / 180;
			tiltHorizontal = 0 * Math.PI / 180; //put this back at 0, otherwise we clip the protliner
			tiltInPlane = 0.0 * Math.PI / 180;
			break;
			
		case OP22_design:
			// Design OP2.2, tilted to get out of the way of future QHW, but only just clip port liner
			fibrePlaneBehindLens = 0.200;
			tiltVertical = 0.5 * Math.PI / 180;
			tiltHorizontal = 1.6 * Math.PI / 180; //moved from 2.0 to 1.8 to stop pelK hitting QHW mirror box
			tiltInPlane = 23.0 * Math.PI / 180;
			break;
			
		case OP22_measured:
			double adjustFocalLength = 1.15;
			
			// Measured post OP2.3
			fibrePlaneBehindLens = 0.200; //adjusting very weakly changes the magnification but totally kills the focus
			
			lens1CurvatureRadius *= adjustFocalLength; //This is a specific lens, so we shouldn't adjust this, but we need to somehow match the focal length
			fibrePlaneBehindLens *= adjustFocalLength;
			
			tiltVertical = 0.7 * Math.PI / 180;
			tiltHorizontal = 1.5 * Math.PI / 180; //moved from 2.0 to 1.8 to stop pelK hitting QHW mirror box
			tiltInPlane = 25.0 * Math.PI / 180;
			break;
			
		default:
			throw new RuntimeException("Unknown alignment state");
		}
		
		double[] rotVert, opticAxis0, opticAxis;
		
		rotVert = Util.reNorm(Util.cross(portNormal, globalUp));
		opticAxis0 = OneLiners.rotateVectorAroundAxis(tiltVertical, rotVert, portNormal);
		opticAxis = OneLiners.rotateVectorAroundAxis(tiltHorizontal, globalUp, opticAxis0);	
		
		/***** Entry Window *****/
		
		double entryWindowDiameter = 0.060; // DN63 CF Window
		double entryWindowThickness = 0.005; // Guessed
		double entryWindowShift = 0.000; 
		
		double entryWindowFrontPos[] = Util.plus(windowCentre, Util.mul(opticAxis, entryWindowShift));
		double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
		double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
		
		Medium windowMedium = new Medium(new Sapphire());
		entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
		entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
		entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
		
		/**** Main Lens *****/
		
		lensCentrePos = Util.plus(windowCentre, Util.mul(opticAxis, lens1DistBehindWindow + lens1CentreThickness));
		
		Medium lensMedium = new Medium(new FusedSilica());  
		SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
		//public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
												"lens1",
												lensCentrePos,
												opticAxis,
												lens1Diameter/2, // radius
												lens1CurvatureRadius, // rad curv
//												lens1FocalLength, // rad curv
												lens1CentreThickness,  
												lensMedium, 
												IsoIsoInterface.ideal()
												);//,designWavelenth);
		
		double lensIrisPos[] = Util.plus(lensCentrePos, Util.mul(opticAxis, -0.005));
		Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lens1Diameter, lens1ClearAperture/2, null, null, Absorber.ideal());
		

		fibrePlanePos = Util.plus(lensCentrePos, Util.mul(opticAxis, fibrePlaneBehindLens)); 
		double fibresXVec0[] = Util.reNorm(new double[]{ 0.0208466796875, 0.02511328125, -0.13614230346679687 });
		double fibresYVec0[] = Util.reNorm(Util.cross(fibresXVec0, opticAxis));
		
		fibresXVec = Algorithms.rotateVector(Algorithms.rotationMatrix(opticAxis, tiltInPlane), fibresXVec0);
		fibresYVec = Util.reNorm(Util.cross(fibresXVec, opticAxis));
		
		losStartSurface = entryWindowFront;
		tracingTarget = entryWindowFront;
		checkSurface = lens1.getFrontSurface();

		fibrePlane = new Square("fibrePlane", fibrePlanePos, opticAxis, fibresYVec, 0.300, 0.300, NullInterface.ideal());
		
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

	@Override
	protected double[][] channelR() { return channelR; }
	@Override
	protected int[] beamIdx() { return beamIdx; }
	@Override
	protected double[][][] fibreEndPos() { return fibreEndPos; }
	@Override
	protected double[][][] fibreEndNorm() { return fibreEndNorm; }
}
