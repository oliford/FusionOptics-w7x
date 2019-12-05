package ipp.w7x.fusionOptics.w7x.cxrs;

import net.jafama.FastMath;

import java.util.List;

import otherSupport.ColorMaps;
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
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_AsMeasured;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public class RadiationExposure {
	
	//AEA21
	public static BeamEmissSpecAEA21 sys = new BeamEmissSpecAEA21();
	
	//public static Element testElement = sys.entryWindowFront;
	//public static Element testElement = new STLMesh("mirrorBlockClosed", "/work/ipp/w7x/cad/aea21/radExposure/mirrorBlockClosed.stl");

	//public static Element testElement = new STLMesh("simpleCapFront", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Front.stl");
	//public static Element testElement = new STLMesh("simpleCapBottom", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Bottom.stl");
	//public static Element testElement = new STLMesh("simpleCapLeftBottom", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-LeftBottom.stl");
	//public static Element testElement = new STLMesh("simpleCapLeftTop", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-LeftTop.stl");
	//public static Element testElement = new STLMesh("simpleCapTop", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Top.stl");
	
	//public static Element testElement = new STLMesh("mirrorBlock", "/work/ipp/w7x/cad/aea21/radExposure2/mirrorBlock.stl");
	//public static Element testElement = new STLMesh("cap1", "/work/ipp/w7x/cad/aea21/radExposure2/cap1.stl");
	//public static Element testElement = new STLMesh("cap2", "/work/ipp/w7x/cad/aea21/radExposure2/cap2.stl");
	//public static Element testElement = new STLMesh("coverFront", "/work/ipp/w7x/cad/aea21/radExposure2/coverFront.stl");
	//public static Element testElement = new STLMesh("coverTop", "/work/ipp/w7x/cad/aea21/radExposure2/coverTop.stl");
	//public static Element testElement = new STLMesh("coverBottom", "/work/ipp/w7x/cad/aea21/radExposure2/coverBottom.stl");
	//public static Element testElement = new STLMesh("strap1", "/work/ipp/w7x/cad/aea21/radExposure2/strap1.stl");
	public static Element[] testElements = {
			//new STLMesh("frontPlateWithRim", "/work/ipp/w7x/cad/aea21/radExposure2/frontPlateWithRim.stl"),
			new STLMesh("mirrorBlock", "/work/ipp/w7x/cad/aea21/radExposure2/mirrorBlock.stl"),
			new STLMesh("cap1-middle", "/work/ipp/w7x/cad/aea21/radExposure2/cap1-middle.stl"),
			new STLMesh("cap1-top", "/work/ipp/w7x/cad/aea21/radExposure2/cap1-top.stl"),
			new STLMesh("cap1-bottom", "/work/ipp/w7x/cad/aea21/radExposure2/cap1-bottom.stl"),
			new STLMesh("cap1-left", "/work/ipp/w7x/cad/aea21/radExposure2/cap1-left.stl"),
			new STLMesh("cap1-right", "/work/ipp/w7x/cad/aea21/radExposure2/cap1-right.stl"),
			new STLMesh("cap2", "/work/ipp/w7x/cad/aea21/radExposure2/cap2.stl"),
			new STLMesh("strap1", "/work/ipp/w7x/cad/aea21/radExposure2/strap1.stl"),
			new STLMesh("strap2", "/work/ipp/w7x/cad/aea21/radExposure2/strap2.stl"),
			
			new STLMesh("coverFront", "/work/ipp/w7x/cad/aea21/radExposure2/cover3-front.stl"),
			new STLMesh("coverTop", "/work/ipp/w7x/cad/aea21/radExposure2/cover3-top.stl"),
			new STLMesh("coverBottom", "/work/ipp/w7x/cad/aea21/radExposure2/cover3-bottom.stl"),
			
			//new STLMesh("coverFront", "/work/ipp/w7x/cad/aea21/radExposure2/coverFront.stl"),
			//new STLMesh("coverTop", "/work/ipp/w7x/cad/aea21/radExposure2/coverTop.stl"),
			//new STLMesh("coverBottom", "/work/ipp/w7x/cad/aea21/radExposure2/coverBottom.stl"),
			
			//new STLMesh("coverFront", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Front.stl"),
			//new STLMesh("coverTop", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Top.stl"),
			//new STLMesh("coverBottom", "/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Bottom.stl"),
			
	};

	private static int nX = 30, nY= 30;
	public static String[] thingsInWay = { 
		//aea21 window = 8W
		//"/work/ipp/w7x/cad/aea21/radExposure/arms.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/frontPlate.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/mirrorBlock.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/reducePlate.stl",
					
		// aea21 closed mirror = 700W
		//"/work/ipp/w7x/cad/aea21/radExposure/frontPlate.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/panelTL-simplified.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/panelTR-simplified.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/capSimple.stl",
		
		//"/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Bottom.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Front.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/simpleCap-LeftBottom.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/simpleCap-LeftTop.stl",
		//"/work/ipp/w7x/cad/aea21/radExposure/simpleCap-Top.stl",
			
		/*	
		"/work/ipp/w7x/cad/aea21/radExposure2/mirrorBlock.stl", // < 70W
		"/work/ipp/w7x/cad/aea21/radExposure2/cap1.stl", // 400W 8500mm2 47Kwm2
		"/work/ipp/w7x/cad/aea21/radExposure2/cap2.stl", // 90W
		"/work/ipp/w7x/cad/aea21/radExposure2/coverFront.stl", // 520W 7200mm2 72Kwm2
		"/work/ipp/w7x/cad/aea21/radExposure2/coverTop.stl",  // 120W 5200mm2 23Kwm2
		"/work/ipp/w7x/cad/aea21/radExposure2/coverBottom.stl", // 300W 5200mm2 57Kwm2
		"/work/ipp/w7x/cad/aea21/radExposure2/strap1.stl", // 5W 
		//"/work/ipp/w7x/cad/aea21/radExposure2/strap2.stl", // 5W		  
		 */
		"/work/ipp/w7x/cad/aea21/radExposure2/frontPlateWithRim.stl"
	};
	//*/
		
	//AEM21
	/*public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(false);
	
	//public static double a[] = Util.plus(radSurfaceCentre, Util.mul(radSurfaceNormal, -0.300)); 
	//public static Surface testSurface = new Disc("testdisc", a, radSurfaceNormal, 0.050, Absorber.ideal()); 
	
	private static int nX = 50, nY= 50;
	//public static Element testElement = sys.mirror;
	//public static Element testElement = new STLMesh("mirrorBlockClosed", "/work/cad/aem21/radExposure/mirrorBlockClosed-simplified.stl");
	//public static Element testElement = new STLMesh("zwickle", "/work/cad/aem21/radExposure/zwickle.stl");
	//public static Element testElement = new STLMesh("port-AEM21-endOnly", "/work/cad/aem21/radExposure/port-AEM21-endOnly.stl");
	public static Element testElement = new STLMesh("portLinerTopfSteffen1", "/work/cad/aem21/radExposure/portLinerTopfSteffen1.stl");
			
	public static String[] thingsInWay = { 				
		// aem21 closed mirror		
		//"/work/cad/aem21/radExposure/portLinerTopfSimple.stl",
		//"/work/cad/aem21/radExposure/portLinerTopfSteffen1.stl",
		//"/work/cad/aem21/radExposure/portLinerTopfSteffen1.stl",	
			
		"/work/cad/aem21/radExposure/mirrorBlockClosed-simplified.stl",
		"/work/cad/aem21/radExposure/panel1.stl",
		"/work/cad/aem21/radExposure/panel2.stl",
		"/work/cad/aem21/radExposure/panel3.stl",
		"/work/cad/aem21/radExposure/port-AEM21-endOnly.stl",
		"/work/cad/aem21/radExposure/portLiner-AEN21-placeHolder.stl",
		"/work/cad/aem21/radExposure/zwickle.stl",
		
	};
	//*/
	
	public static int nRaysPerPoint = 50;
	public static int nSkip = 0;
	
	
	public final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure/";
	
	/** Power emitted from radiating surface, Watts per square meter per Steradian */
	public static double powerAngularDensity = 100e3 / 2 / Math.PI;  //100/2.pi kW m^-2 SR^-1
	
	public static void main(String[] args) { 
		System.out.println(outPath);
		
		sys.removeElement(sys.mirror);
		sys.removeElement(sys.beamPlane);
		sys.removeElement(sys.catchPlane);
		sys.removeElement(sys.strayPlane);
		
		//sys.removeElement(sys.panelEdge);
		//sys.removeElement(sys.mirrorBlock);
		//sys.removeElement(sys.mirrorClampRing);
		
		//sys.addElement(new Sphere("targSphere", testElement.getBoundarySphereCentre(), testElement.getBoundarySphereRadius(), NullInterface.ideal()));
		
		
		
		double dx = sys.radSurfWidth / (nX - 1);
		double dy = sys.radSurfHeight / (nY - 1);
			
		double col[][] = ColorMaps.jet(testElements.length);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/radExposure-"+sys.getDesignName() + "-cover1.vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		vrmlOut.setSkipRays(nSkip);
		
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
		
		for(String fileName : thingsInWay){
			String parts[] = fileName.split("/");			
			sys.addElement(new STLMesh("bg_"+parts[parts.length-1], fileName));			
		}
		
		double totalPower[] = new double[testElements.length];
		int nHitTotal[] = new int[testElements.length];
		
		for(int iY = 0; iY < nY; iY++){
			double y = -sys.radSurfHeight/2 + iY * dy;
			
			for(int iX = 0; iX < nX; iX++){
				double x = -sys.radSurfWidth/2 + iX * dx;
				
				double startPos[] = Util.plus(sys.radSurfaceCentre, 
										Util.plus(
												Util.mul(sys.radSurface.getRight(), x),
												Util.mul(sys.radSurface.getUp(), y) ));
				
				
				double solidAngle = Double.NaN;				
				int nHit[] = new int[testElements.length];
				
				for(int iR=0; iR < nRaysPerPoint; iR++){

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
							nHit[iTE]++;
							vrmlOut.drawRay(ray, col[iTE]);
							break; //don't count twice. Shouldn't if they are absorbers
						}
					}
					
					Pol.recoverAll();
				}
				
				System.out.print(iY + ", "+ iX+": ");
				for(int iTE=0; iTE < testElements.length; iTE++) {
					double power = powerAngularDensity * dx * dy * solidAngle //power emitted into ray generation cone 
								* ((double)nHit[iTE] / nRaysPerPoint);  //fraction of that power captured by target
					totalPower[iTE] += power;
					nHitTotal[iTE] += nHit[iTE];
					
					System.out.print(nHit[iTE] + ", ");
				}
				System.out.println();
			}
			
			
			
		}

		double totalPowerAll = 0;
		for(int iTE=0; iTE < testElements.length; iTE++) {
			System.out.print(sys.getDesignName() + " " + testElements[iTE].getName() + ": nHit = " + nHitTotal[iTE] + ", dx*dy = " + (dx*dy) + ", Total = " + totalPower[iTE] + " W");
			totalPowerAll += totalPower[iTE];
			if(testElements[iTE] instanceof Disc) {
				double targetArea = Math.PI * FastMath.pow2(((Disc)testElements[iTE]).getRadius());
				System.out.print(", Power/area = " + (totalPower[iTE] / targetArea) + " W/m2");
			}else if(testElements[iTE] instanceof Square) {
				double targetArea = ((Square)testElements[iTE]).getWidth() *  ((Square)testElements[iTE]).getHeight();
				System.out.print(". Power/area = " + (totalPower[iTE] / targetArea) + " W/m2");				
			}
			System.out.println();
		}
		System.out.println("Total power captured = " + totalPowerAll + " W");
		
		vrmlOut.destroy();
	}
}
