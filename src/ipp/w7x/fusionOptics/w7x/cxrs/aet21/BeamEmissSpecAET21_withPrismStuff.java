package ipp.w7x.fusionOptics.w7x.cxrs.aet21;

import java.util.ArrayList;

import ipp.w7x.neutralBeams.W7xNBI;
import uk.co.oliford.jolu.OneLiners;
import algorithmrepository.Algorithms;
import net.jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.drawing.STLDrawer;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.Nikon135mmF28;
import fusionOptics.lenses.Nikon50mmF11;
import fusionOptics.materials.BK7;
import fusionOptics.materials.FusedSilica;
import fusionOptics.materials.IsotropicFixedIndexGlass;
import fusionOptics.materials.Sapphire;
import fusionOptics.materials.SchottSFL6;
import fusionOptics.materials.Vacuum;
import fusionOptics.optics.DoubleGaussLens;
import fusionOptics.optics.STLMesh;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Dish;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Material;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;
import fusionOptics.types.Surface;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAET21_withPrismStuff extends Optic {
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9;
	
	/**** Shutter ****/
	public double shutterPivotCentreA[] = { -0.9081088256835937, 6.23119873046875, -0.13847615051269532 };
	public double shutterPoints[][] = { //as in original cad, sticking out a bit
		{ -0.0993520263671875, 0.620496484375, -0.013595979309082032 },
		{ -0.08465389404296876, 0.624318310546875, -0.020437840270996096 },
		{ -0.08607802124023438, 0.627997900390625, -0.0068935592651367195 },
	};
	public double portNormal[] = Util.reNorm(new double[]{ -0.07340324, 0.94686172,  -0.31315308 });
	public double portRightish[] = Util.reNorm(new double[]{ -0.9312751 , -0.3297411 ,  0.15491124 });
	public double portUp[] = Util.reNorm(Util.cross(portNormal, portRightish));
	public double portRight[] = Util.reNorm(Util.cross(portUp, portNormal));
		
	public double shutterDiameter = 0.200;
	
	public double shutterNormal[] = Util.reNorm(Util.cross(Util.minus(shutterPoints[0], shutterPoints[1]), Util.minus(shutterPoints[2], shutterPoints[1])));

	public double shutterPivotCentre[] = Util.plus(shutterPivotCentreA, Util.mul(shutterNormal, 0.017));
	
	public Disc shutter = new Disc("shutterDisc", shutterPivotCentre, shutterNormal, shutterDiameter/2, NullInterface.ideal());
	
	public double shutterRight[] = Util.reNorm(Util.cross(shutterNormal, globalUp));
	public double shutterUp[] = Util.reNorm(Util.cross(shutterRight, shutterNormal));
	
	/**** Port Tube****/
	
	public double portTubeDiameter = 0.220;
	public double portTubeLength = 1.000;
	public double portTubeCentre[] = Util.plus(shutterPivotCentre, Util.mul(portNormal, portTubeLength/2));
	public Cylinder portTubeCyld = new Cylinder("portTubeCyld", portTubeCentre, portNormal, portTubeDiameter/2, portTubeLength, Absorber.ideal());
	
	
	

	/**** Entry Window ****/
	public double entryWindowRadiusOnShutter = 0.057;
	public double entryWindowIrisDiameter = 0.065;
	public double entryWindowDiameter = 0.050;
	//public double entryWindowAngularPosition = 116 * Math.PI / 180;//near window
	//public double entryWindowAngularPosition = -4 * Math.PI / 180; //top window 
	//public double entryWindowAngularPosition = -124 * Math.PI / 180; // bottom window
	public double entryWindowAngularPosition = -19 * Math.PI / 180; //top window, with 18deg rotated tube
	public double entryWindowMoveIn = 0.007;
	public double entryWindowThickness = 0.0025;
	public double entryWindowCyldLength = 0.015;
	public double entryWindowCyldPosAdjust = 0.012;
	public double entryWindowIrisPos[] = Util.plus(shutterPivotCentre, 
										Util.plus( Util.plus( Util.mul(shutterUp, entryWindowRadiusOnShutter * FastMath.cos(entryWindowAngularPosition)),
												    		  Util.mul(shutterRight, entryWindowRadiusOnShutter * FastMath.sin(entryWindowAngularPosition))),
												   Util.mul(shutterNormal, -0.000)));
		 
	public double entryWindowPos[] = Util.plus(entryWindowIrisPos, Util.mul(shutterNormal, entryWindowMoveIn));
	
	public double entryWindowFrontPos[] = Util.plus(entryWindowPos, Util.mul(shutterNormal, -entryWindowThickness/2));
	public double entryWindowBackPos[] = Util.plus(entryWindowPos, Util.mul(shutterNormal, +entryWindowThickness/2));
	
	public double entryWindowCyldPos[] = Util.plus(entryWindowIrisPos, Util.mul(shutterNormal, entryWindowCyldPosAdjust));
	
	
	//public Medium entryWindowMedium = new Medium(new IsotropicFixedIndexGlass(1.2));
	public Medium entryWindowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindow", entryWindowFrontPos, shutterNormal, entryWindowDiameter/2, entryWindowMedium, null, IsoIsoInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindow", entryWindowBackPos, shutterNormal, entryWindowDiameter/2, null, entryWindowMedium, IsoIsoInterface.ideal());
	public Iris entryWindowFrontIris = new Iris("entryWindowIris", entryWindowIrisPos, shutterNormal, 0.060, entryWindowIrisDiameter/2, null, null, Absorber.ideal());
	public Cylinder entryWindowCyld = new Cylinder("entryWindowCyld", entryWindowCyldPos, shutterNormal, entryWindowDiameter/2, entryWindowCyldLength, Absorber.ideal());
	
	//public STLMesh shutterPlate = new STLMesh("shutterPlate", "/work/ipp/w7x/cad/aet21/shutterFrontPlate-topWindow.stl");
	
	/***** Observation target ****/
	public int targetBeamIdx = 7; //Q8
	public double targetBeamR = 5.8;
	public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	
	public double observationVec[] = Util.reNorm(Util.minus(targetObsPos, entryWindowPos));
	public double observationUp[] = Util.reNorm(Util.cross(W7xNBI.def().uVec(targetBeamIdx), observationVec));
	
	public STLMesh panelEdge = new STLMesh("panel", "/work/ipp/w7x/cad/aet21/conflicting-panel-aet21.stl");
	
	
	/***** Prism on back of window ****/
	public double prismDiameter = 0.035;
	public double prismThickness = 0.018;
	public double prismRotationAngle = -15 * Math.PI / 180;
	public double prismUp[] = Algorithms.rotateVector(Algorithms.rotationMatrix(shutterNormal, prismRotationAngle), observationUp);
	
	public double prismSurfaceAngle = 15 * Math.PI / 180;
	public double prismBackNormal[] = Algorithms.rotateVector(Algorithms.rotationMatrix(prismUp, prismSurfaceAngle), shutterNormal);
	public double prismFrontPos[] = Util.plus(entryWindowBackPos, Util.mul(shutterNormal, 0.001));
	public double prismBackPos[] = Util.plus(entryWindowBackPos, Util.mul(shutterNormal, prismThickness));
	public double prismIrisPos[] = Util.plus(entryWindowBackPos, Util.mul(shutterNormal, prismThickness - 0.001));
	
	public Medium prismMedium = new Medium(new FusedSilica());// IsotropicFixedIndexGlass(1.5));
	public Disc prismFront = new Disc("prismFront", prismFrontPos, shutterNormal, prismDiameter/2, prismMedium, null, IsoIsoInterface.ideal());
	public Disc prismBack = new Disc("prismBack", prismBackPos, prismBackNormal, prismDiameter/2, null, prismMedium, IsoIsoInterface.ideal());
	
	public Iris prismFrontIris = new Iris("prismFrontIris", prismFrontPos, shutterNormal, 0.060, prismDiameter*0.499, Absorber.ideal());
	public Iris prismBackIris = new Iris("prismBackIris", prismBackPos, prismBackNormal, 0.060, prismDiameter*0.499, Absorber.ideal());
	
	
	/**** Mirror ****/
	//public double mirrorWidth = 0.126;
	public double mirrorWidth = 0.0510;
	public double mirrorHeight = 0.038;
	
	//public double mirrorCentreBackFromWindow = 0.055;	 //with prism
	//public double mirrorCentreRightFromWindow = 0.047; 
	public double mirrorCentreBackFromWindow = 0.038;
	public double mirrorCentreUpFromWindow = -0.005;	
	public double mirrorCentreRightFromWindow = 0.045;
	
	public double mirrorBuildPosA[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, mirrorCentreBackFromWindow));	
	public double mirrorBuildPosB[] = Util.plus(mirrorBuildPosA, Util.mul(portUp, mirrorCentreUpFromWindow));	
	public double mirrorCentrePos[] = Util.plus(mirrorBuildPosB, Util.mul(portRight, mirrorCentreRightFromWindow));
	
	//public double mirrorToPortAngle = 23 * Math.PI / 180;
	//public double mirrorTipAngle = -7 * Math.PI / 180; //tip to counter the effect of the prism rotation //for 0prism
		
	//public double mirrorToPortAngle = 20 * Math.PI / 180;	
	//public double mirrorTipAngle = -3.0 * Math.PI / 180; //tip to counter the effect of the prism rotation

	public double mirrorToPortAngle = 19.2 * Math.PI / 180;
	public double mirrorTipAngle = -6.5 * Math.PI / 180; //Q7

	//public double mirrorToPortAngle = 14 * Math.PI / 180;	
	//public double mirrorTipAngle = -3.0 * Math.PI / 180; //tip to counter the effect of the prism rotation, shiften lens

	public double mirrorNormal0[] =  Util.reNorm(Algorithms.rotateVector(Algorithms.rotationMatrix(portUp, mirrorToPortAngle), portRight));
	public double mirrorRight[] = Util.reNorm(Util.cross(mirrorNormal0, portUp));
	public double mirrorNormal[] =  Util.reNorm(Algorithms.rotateVector(Algorithms.rotationMatrix(mirrorRight, mirrorTipAngle), mirrorNormal0));
	
	public double mirrorUp[] = Util.reNorm(Util.cross(mirrorRight, mirrorNormal));
	
	public Square mirror = new Square("mirror", mirrorCentrePos, mirrorNormal, mirrorUp, mirrorHeight, mirrorWidth, Reflector.ideal());
	

		
	/**** Lens *****/

	//public double lensCentreBackFromWindow = 0.120;	
	//public double lensCentreUpFromWindow = -0.005;	
	//public double lensCentreRightFromWindow = 0.038;

	public double lensCentreBackFromWindow = 0.080;	
	public double lensCentreUpFromWindow = -0.005;	
	public double lensCentreRightFromWindow = 0.033;

	public double lensBuildPosA[] = Util.plus(entryWindowFrontPos, Util.mul(portNormal, lensCentreBackFromWindow));	
	public double lensBuildPosB[] = Util.plus(lensBuildPosA, Util.mul(portUp, lensCentreUpFromWindow));	
	public double lensCentrePos[] = Util.plus(lensBuildPosB, Util.mul(portRight, lensCentreRightFromWindow));
		
	public double lensFocalLength = 0.025;
	
	public double lensNormal[] = Util.reNorm(Util.minus(lensCentrePos, mirrorCentrePos));	
	public double lensDiameter = 0.045;
	
	/*public Medium lensMedium = new Medium(new SchottSFL6()); 
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lensCentrePos,
											lensNormal,
											0.51*lensDiameter, // radius
											0.050, // focal length
											0.010, // centreThickness, 
											lensMedium, 
											IsoIsoInterface.ideal(),
											designWavelenth);
	
	public double lensIrisCentrePos[] =  Util.plus(lensCentrePos, Util.mul(lensNormal, -0.005));
	public Iris lensIris = new Iris("lensIris", lensIrisCentrePos, lensNormal, 2*lensDiameter/2, 0.5*lensDiameter, Absorber.ideal());
	*/
	Nikon50mmF11 lens1 = new Nikon50mmF11(lensCentrePos, lensFocalLength / 0.050, lensNormal);
	//DoubleGaussLens lens1 = new DoubleGaussLens("lens1", lensCentrePos, lensNormal, lensFocalLength); //big fat Nope!
	//Nikon135mmF28 lens1 = new Nikon135mmF28(lensCentrePos, lensFocalLength / 0.138, lensNormal);
	
	public double lens1Diamater = 0.035;
	
	/*Material lens1Mat = new BK7(); 
	SimplePlanarConvexLens lens1a = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens1a",
			Util.plus(lensCentrePos, Util.mul(lensNormal, -0.020)), 
			lensNormal, 
			lens1Diamater/2, // d
			0.105, // f
			0.005, // L
			new Medium(lens1Mat),
			IsoIsoInterface.ideal(),
			designWavelenth);

	SimplePlanarConvexLens lens1b = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens1b",
			Util.plus(lensCentrePos, Util.mul(lensNormal, -0.010)), 
			lensNormal, 
			lens1Diamater/2, // d
			0.105, // f
			0.005, // L
			new Medium(lens1Mat),
			IsoIsoInterface.ideal(),
			designWavelenth);	
	

	SimplePlanarConvexLens lens1c = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens1b",
			Util.plus(lensCentrePos, Util.mul(lensNormal, 0.000)), 
			lensNormal, 
			lens1Diamater/2, // d
			0.105, // f
			0.005, // L
			new Medium(lens1Mat),
			IsoIsoInterface.ideal(),
			designWavelenth);	
	
	SimplePlanarConvexLens lens1d = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness("lens1b",
			Util.plus(lensCentrePos, Util.mul(lensNormal, 0.020)), 
			lensNormal, 
			lens1Diamater/2, // d
			-0.200, // f
			0.002, // L
			new Medium(lens1Mat),
			IsoIsoInterface.ideal(),
			designWavelenth);	
	Optic lens1 = new Optic("lens1", new Element[]{ lens1a, lens1b, lens1c, lens1d });
	//*/
	
	public Iris lens1Iris = new Iris("lens1Iris", lensCentrePos, lensNormal, 0.100, lens1Diamater*0.49, Absorber.ideal());
	
	/*** Fibre plane 1 ****/
	public double fibrePlaneFromLens = lensFocalLength + 0.001;
	public double fibrePlanePortRightShift = -0.000;	
	public double fibrePlanePortUpShift = 0.000;	
	public double fibrePlanePos[] = Util.plus(lensCentrePos, Util.plus(Util.mul(lensNormal, fibrePlaneFromLens),
																		Util.plus(Util.mul(portRight, fibrePlanePortRightShift),
																				  Util.mul(portUp, fibrePlanePortUpShift))));
	
	//public double fibrePlaneUp[] = Util.reNorm(Util.cross(Util.cross(lensNormal, globalUp), lensNormal));
	
	/*** Fibre plane 2 ****/
	public double fibrePlaneNormal[] = Util.mul(lensNormal, -1);
	public double fibresXVec0[] = Util.reNorm(Util.cross(fibrePlaneNormal, globalUp));
	public double fibresYVec0[] = Util.reNorm(Util.cross(fibresXVec0, fibrePlaneNormal));
	public double fibreRotation = -20 * Math.PI / 180;
	public double fibresXVec[] = Algorithms.rotateVector(Algorithms.rotationMatrix(fibrePlaneNormal, fibreRotation), fibresXVec0);
	public double fibresYVec[] = Algorithms.rotateVector(Algorithms.rotationMatrix(fibrePlaneNormal, fibreRotation), fibresYVec0);
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, fibrePlaneNormal, fibresYVec, 0.080, 0.080, Absorber.ideal());
	
	public Element tracingTarget = entryWindowFront;
	
	public double beamAxis[] = W7xNBI.def().uVec(targetBeamIdx);
	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lensCentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.200, NullInterface.ideal());

	
	/** Fibres, Observation volumes etc */
	public int nFibres = 20;
	public double fibreNA = 0.22; // As AUG	
	public double fibreEndDiameter = 0.000480; // as AUG, with polymide jacket
		
	public double[] channelR = OneLiners.linSpace(5.38, 5.88, nFibres);	
	
	/** Fibre positions from autofocus */
	//public double[] fibreFocus = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };


	public double[] fibreFocus = { 
		 3.701248168950205E-4 ,
		 4.371490478510926E-4 ,
		 5.486984252930199E-4 ,
		 5.581207275393697E-4 ,
		 6.074218750000004E-4 ,
		 6.285476684565589E-4 ,
		 6.339263916017002E-4 ,
		 6.45370483397959E-4 ,
		 6.554641723634126E-4 ,
		 6.567001342771765E-4 ,
		 6.606903076171828E-4 ,
		 6.664352416992688E-4 ,
		 6.603393554688694E-4 ,
		 6.533660888671084E-4 ,
		 6.519546508789165E-4 ,
		 6.539840698238463E-4 ,
		 6.469497680663824E-4 ,
		 6.438369750978835E-4 ,
		 6.641998291011983E-4 ,
		 5.29846191406016E-4 ,
	};


	public double[][] fibreEndPos;
	public double[][] fibreEndNorm;
	
	public Square fibrePlanes[] = {
			
	};
	
	
	/** Set fibre positions equal spacing in holder */
	private void setupFibrePositions() {
		fibreEndPos = new double[nFibres][];
		fibreEndNorm = new double[nFibres][];

		double dX = fibreEndDiameter;
		double x0 = -(nFibres-1)/2 * dX; 
		for(int i=0; i < nFibres; i++){
			fibreEndPos[i] = Util.plus(fibrePlanePos, Util.mul(fibresXVec, x0 + i * dX));
			
					
			fibreEndNorm[i] = fibrePlane.getNormal().clone();
		}
		if(fibreFocus != null){
			for(int i=0; i < nFibres; i++){
				fibreEndPos[i] = Util.plus(fibreEndPos[i], Util.mul(fibrePlane.getNormal(), fibreFocus[i]));
			}	
		}
	}
	
	private void setupFibrePlanes() {
		fibrePlanes = new Square[nFibres];
		for(int i=0; i < nFibres; i++){

			double norm[] = fibreEndNorm[i];
			double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
			double y[] = Util.reNorm(Util.cross(x, norm));
			fibrePlanes[i] = new Square("fibrePlane_" + i, fibreEndPos[i].clone(), norm, y, 0.007, 0.007, NullInterface.ideal());
			//addElement(fibrePlanes[i]);
		}
	}
	
	public BeamEmissSpecAET21_withPrismStuff() {
		super("beamSpec-aet21");
		
		//make the window a prism
		//entryWindowFront.rotate(entryWindowFront.getCentre(), Algorithms.rotationMatrix(observationUp, -5 * Math.PI / 180));
		//entryWindowBack.rotate(entryWindowBack.getCentre(), Algorithms.rotationMatrix(observationUp, 10 * Math.PI / 180));
		
		
		//addElement(shutter);
		addElement(panelEdge);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(entryWindowFrontIris);
		addElement(entryWindowCyld);
		addElement(mirror);
		addElement(lens1Iris);		
		addElement(lens1);
		addElement(fibrePlane);
		//addElement(portTubeCyld);

		//addElement(prismFrontIris);
		//addElement(prismFront);
		//addElement(prismBackIris);
		//addElement(prismBack);
		
		setupFibrePositions();
		setupFibrePlanes();
		
		double lensFibreDist = Util.length(Util.minus(lensCentrePos, fibrePlane.getCentre()));
		System.out.println("Lens - Fibres distance = " + lensFibreDist*1000 + " / mm");
		//System.out.println("Max f/" + (lensFibreDist/lens1.getRadius()/2) + " ");
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());
		
	}

	public String getDesignName() { return "aet21";	}
	
	//manual simplified model for STL
	public ArrayList<Element> makeSimpleModel(){
		ArrayList<Element> elements = new ArrayList<Element>();
		
		elements.add(mirror);
		elements.add(prismFront);
		elements.add(prismBack);
		elements.add(lens1);
		
		return elements;
	}

}
