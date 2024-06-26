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
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21.Subsystem;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_AsMeasured;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposure_AEA21 {
	
	//AEA21
	public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21(Subsystem.CXRS);
	public static String inPath = "/work/ipp/w7x/cad/aea21/radExposure3";
	
	public static Element[] testElements = {
			new STLMesh(inPath + "/mirrorBlock.stl"),
			new STLMesh(inPath + "/cap1-middle.stl"),
			new STLMesh(inPath + "/cap1-top.stl"),
			new STLMesh(inPath + "/cap1-bottom.stl"),
			new STLMesh(inPath + "/cap1-left.stl"),
			new STLMesh(inPath + "/cap1-right.stl"),
			new STLMesh(inPath + "/cap2.stl"),
			new STLMesh(inPath + "/strap1.stl"),
			new STLMesh(inPath + "/strap2.stl"),
			
			new STLMesh(inPath + "/cover3-front.stl"),
			new STLMesh(inPath + "/cover3-top.stl"),
			new STLMesh(inPath + "/cover3-bottom.stl"),  

	};
	
	public static Element[] thingsInWay = {	

			new STLMesh(inPath + "/frontPlateWithRim.stl"),
			new STLMesh(inPath + "/panelTL-simplified.stl"),
			new STLMesh(inPath + "/panelTR-simplified.stl"),
	};

	//private static int nX = 70, nY= 70;
	private static int nRays = 1000000;
	private static int nRaysToDraw = 1000;
	//*/
		
	//AEM21
	//public static double a[] = Util.plus(radSurfaceCentre, Util.mul(radSurfaceNormal, -0.300)); 
	//public static Surface testSurface = new Disc("testdisc", a, radSurfaceNormal, 0.050, Absorber.ideal()); 
	
	
	//public static int nRaysPerPoint = 100;
	
	
	
	public final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure2-cover3-smallerLaunch/";
	
	/** Power emitted from radiating surface, Watts per square meter per Steradian */
	public static double powerAngularDensity = 100e3 / 2 / Math.PI;  //100/2.pi kW m^-2 SR^-1
	
	public static void main(String[] args) { 
		System.out.println(outPath);
		
		sys.removeElement(sys.mirror);
		sys.removeElement(sys.beamPlane);
		sys.removeElement(sys.catchPlane);
		sys.removeElement(sys.strayPlane);
		
		
		//sys.addElement(new Sphere("targSphere", testElement.getBoundarySphereCentre(), testElement.getBoundarySphereRadius(), NullInterface.ideal()));
		
		
		
		//double dx = sys.radSurfWidth / (nX - 1);
		//double dy = sys.radSurfHeight / (nY - 1);
		double dA = sys.radSurfHeight * sys.radSurfWidth / nRays;
			
		double col[][] = ColorMaps.jet(testElements.length);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/radExposure-"+sys.getDesignName() + ".vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		int nRaysDrawn = 0;
		
		//vrmlOut.drawOptic(sys);
		
		for(Element testElement : testElements) {
			if(testElement instanceof Surface)
				((Surface)testElement).setInterface(Absorber.ideal());
			if(testElement instanceof Optic)
				for(Surface s : ((Optic)testElement).getSurfacesAll())
					s.setInterface(Absorber.ideal());
		}
		Optic testElementsAll = new Optic("testElements", testElements);
		sys.addElement(testElementsAll);
		
		sys.addElement(sys.radSurface);
		sys.addElement(new Optic("thingsInWay", thingsInWay));
		
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
			
			ray.dir = Tracer.generateRandomRayTowardSurface(startPos, testElementsAll, true);
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
			
			
			if((iS % 100) == 0) {
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
