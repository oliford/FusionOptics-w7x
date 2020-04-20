package ipp.w7x.fusionOptics.w7x.cxrs;

import net.jafama.FastMath;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import otherSupport.ColorMaps;
import otherSupport.RandomManager;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.optics.STLMesh;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Square;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_AsMeasured;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses_75mm_UVFS_3cmAperture;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposureAET21_OP2 {
	
	//AEA21
	public static BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7 sys = new BeamEmissSpecAET21_HST_TwoFlatAndLenses2_BK7();
	public static String inPath = "/work/cad/aet21/aet21-op2/radExposure1/";
	
	public static Element[] testElements = {
			new STLMesh(inPath + "/cxrs-coverL1.stl"),
			new STLMesh(inPath + "/cxrs-L1.stl"),
			new STLMesh(inPath + "/endInner.stl"),
			new STLMesh(inPath + "/frontPlateInner.stl"),
			new STLMesh(inPath + "/hst-L1.stl"),
			new STLMesh(inPath + "/hst-M1.stl"),
			new STLMesh(inPath + "/hst-M2.stl"),
			new STLMesh(inPath + "/hst-supportM1.stl"),
			new STLMesh(inPath + "/innerEndplate.stl"),
			new STLMesh(inPath + "/tube.stl"),

			new STLMesh(inPath + "/graphiteBottom.stl"),
			new STLMesh(inPath + "/graphiteTop.stl"),	
			
			new STLMesh(inPath + "/shutterClosed.stl"),
			
	};

	public static Element[] thingsInWay = {	
			new STLMesh(inPath + "/panel-aet21-1.stl"),
			new STLMesh(inPath + "/panel-aet21-2.stl"),
			new STLMesh(inPath + "/port-aet21.stl"),
			new STLMesh(inPath + "/portLiner-aet21-simple.stl"),
			new STLMesh(inPath + "/shield-aet21.stl"),
			new STLMesh(inPath + "/baffle-aet21.stl"),
			new STLMesh(inPath + "/closure-aet21.stl"),		
			
			
	};
	
	public static Optic testElementsOptic = new Optic("testElements", testElements);
	
	public static Element tracingTarget = testElementsOptic;
	
	
	//private static int nX = 70, nY= 70;
	private static int nRays = 1000000;
	private static int nRaysToDraw = 500;
	//*/
		
	//AEM21
	//public static double a[] = Util.plus(radSurfaceCentre, Util.mul(radSurfaceNormal, -0.300)); 
	//public static Surface testSurface = new Disc("testdisc", a, radSurfaceNormal, 0.050, Absorber.ideal()); 
	
	
	//public static int nRaysPerPoint = 100;
	
	
	
	public final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/aet21/radExposure1-tubeEndClosed/";
	
	/** Power emitted from radiating surface, Watts per square meter per Steradian */
	public static double powerAngularDensity = 100e3 / 2 / Math.PI;  //100/2.pi kW m^-2 SR^-1
	
	public static void main(String[] args) { 
		System.out.println(outPath);
		
		sys.mirror1.setInterface(Absorber.ideal());
		sys.mirror2.setInterface(Absorber.ideal());
		
		sys.addElement(sys.radSurface);
		
		//sys.entryAperture.setInterface(NullInterface.ideal());
		for(Surface s : sys.getSurfacesAll())
			s.setInterface(NullInterface.ideal());
		//sys.mirror1.setInterface(Absorber.ideal());
		sys.removeElement(sys.entryTarget);
		sys.mirror1.setInterface(Reflector.ideal());
		sys.mirror2.setInterface(Reflector.ideal());
		
		//sys.removeElement(sys.mirror1);
		//sys.removeElement(sys.mirror2);
		sys.removeElement(sys.beamPlane);
		//sys.removeElement(sys.catchPlane);
		//sys.removeElement(sys.strayPlane);
		
		sys.removeElement(sys.panelEdge);
		//sys.removeElement(sys.mirrorBlock);
		//sys.removeElement(sys.mirrorClampRing);
		
		//sys.addElement(new Sphere("targSphere", testElement.getBoundarySphereCentre(), testElement.getBoundarySphereRadius(), NullInterface.ideal()));
				
		//double dx = sys.radSurfWidth / (nX - 1);
		//double dy = sys.radSurfHeight / (nY - 1);
		double dA = sys.radSurfHeight * sys.radSurfWidth / nRays;
			
		double col[][] = ColorMaps.jet(testElements.length);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/radExposure-"+sys.getDesignName() + ".vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		int nRaysDrawn = 0;
		
		vrmlOut.drawOptic(sys);
		
		sys.addElement(new Optic("thingsInWay", thingsInWay));
		
		for(Element testElement : testElements) {
			if(testElement instanceof Surface)
				((Surface)testElement).setInterface(Absorber.ideal());
			if(testElement instanceof Optic)
				for(Surface s : ((Optic)testElement).getSurfacesAll())
					s.setInterface(Absorber.ideal());
		}
		Optic testElementsAll = testElementsOptic;
		sys.addElement(testElementsAll);
		
		sys.addElement(sys.radSurface);
		
		double totalPower[] = new double[testElements.length];
		int nHitTotal[] = new int[testElements.length];
		
		for(int iS = 0; iS < nRays; iS++){
			double y = sys.radSurfHeight * (RandomManager.instance().nextUniform(0,1) - 0.5); 
			double x = sys.radSurfWidth * (RandomManager.instance().nextUniform(0,1) - 0.5); 
				
				double startPos[] = Util.plus(sys.radSurfaceCentre, 
										Util.plus(
												Util.mul(sys.radSurface.getRight(), x),
												Util.mul(sys.radSurface.getUp(), y) ));
				
				
				double solidAngle = Double.NaN;				
			

				RaySegment ray = new RaySegment();
				ray.startPos = startPos;
				
				ray.dir = Tracer.generateRandomRayTowardSurface(startPos, tracingTarget, true);
				solidAngle = Util.length(ray.dir);
				ray.dir = Util.reNorm(ray.dir);
						
				//ray.dir = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget);
				ray.wavelength = 500e-9; //irrelevant
				ray.E0 = new double[][]{{1,0,0,0}};
				ray.up = Util.createPerp(ray.dir);
						
				Tracer.trace(sys, ray, 100, 0, false);
						
				for(int iTE=0; iTE < testElements.length; iTE++) {						
					List<Intersection> hits = ray.getIntersections(testElements[iTE]);
					if(hits.size() > 0){
						double power = powerAngularDensity * dA * solidAngle; //power emitted into ray generation cone
						totalPower[iTE] += power;
						nHitTotal[iTE] += 1;
					
						if(vrmlOut != null) {
							vrmlOut.drawRay(ray, col[iTE]);
							nRaysDrawn++;
						}
						break; //don't count twice. Shouldn't if they are absorbers
					}
				}
				
				Pol.recoverAll();
				
				if((iS % 10)==0) {
					//System.out.print(iY + ", "+ iX+": ");
					System.out.print(iS + ": ");
					for(int iTE=0; iTE < testElements.length; iTE++) {					
						System.out.print(nHitTotal[iTE] + ", ");
					}
					System.out.println();
				}

			if((iS % 100) == 0) {
				dumpPowers(totalPower, nHitTotal, dA, ((double)iS)/nRays);
				System.out.println("VRML: "+ nRaysDrawn + " / " + nRaysToDraw);
			}
			
			if(vrmlOut != null && nRaysDrawn >= nRaysToDraw) {
				System.out.println("VRML Done");
				vrmlOut.destroy();
				vrmlOut = null;
			}
			
		}
		
		dumpPowers(totalPower, nHitTotal, dA, 1.0);
		
		if(vrmlOut != null)
			vrmlOut.destroy();
	}
	
	private static void dumpPowers(double[] totalPower, int[] nHitTotal, double dA, double fracCollected) {
		try {
			PrintStream textOut = new PrintStream(outPath + "/powers.txt");
			
			dumpPowers(textOut, totalPower, nHitTotal, dA, fracCollected);
			dumpPowers(System.out, totalPower, nHitTotal, dA, fracCollected);
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static void dumpPowers(PrintStream textOut, double[] totalPower, int[] nHitTotal, double dA, double fracCollected) {

		double totalPowerAll = 0;
		for(int iTE=0; iTE < testElements.length; iTE++) {
			double finalTotalPower = (totalPower[iTE] / fracCollected);
			textOut.print(sys.getDesignName() + " " + testElements[iTE].getName() + ": nHit = " + nHitTotal[iTE] + ", dA = " + (dA) + ", Total = " + finalTotalPower + " W");
			totalPowerAll += finalTotalPower;
			if(testElements[iTE] instanceof Disc) {
				double targetArea = Math.PI * FastMath.pow2(((Disc)testElements[iTE]).getRadius());
				textOut.print(", Power/area = " + (finalTotalPower / targetArea) + " W/m2");
			}else if(testElements[iTE] instanceof Square) {
				double targetArea = ((Square)testElements[iTE]).getWidth() *  ((Square)testElements[iTE]).getHeight();
				textOut.print(". Power/area = " + (finalTotalPower / targetArea) + " W/m2");				
			}
			textOut.println();
		}
		textOut.println("Total power captured = " + totalPowerAll + " W");
		
	}
}
