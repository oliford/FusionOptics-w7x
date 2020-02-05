package ipp.w7x.fusionOptics.w7x.cxrs;

import net.jafama.FastMath;

import java.io.FileNotFoundException;
import java.io.PrintStream;
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
public class RadiationExposureAEM21 {
	
	//AEA21
	public static BeamEmissSpecAEM21_postDesign_LC3 sys = new BeamEmissSpecAEM21_postDesign_LC3(false);
	public static String inPath = "/work/cad/aem21/radExposure20/";
	
	public static Element[] testElements = {

			new STLMesh(inPath + "/port-AEM21-endOnly.stl"),
			new STLMesh(inPath + "/zwickle2.stl"), 
	};

	public static Element[] thingsInWay = {	
			//AEM21 port protection - front plate
			new Optic("FrontPlate", new Element[] {
				new STLMesh(inPath + "/FrontPlateSlice2.0.stl"), //bottom
				new STLMesh(inPath + "/FrontPlateSlice2.1.stl"), //top right
				new STLMesh(inPath + "/FrontPlateSlice2.2.stl"), //left
				new STLMesh(inPath + "/FrontPlateSlice2.3.stl"), //top
				new STLMesh(inPath + "/FrontPlateSlice2.4.stl"), //middle top
				new STLMesh(inPath + "/FrontPlateCap.stl"), //cap
				new STLMesh(inPath + "/frontPlatePipe.stl"), //pipe welded into front plate
			}),
			
			//AEM21 port protection - port shield
			new Optic("PortShield", new Element[] {
					new STLMesh(inPath + "/portShield-Slice002.0.stl"), //left middle
				new STLMesh(inPath + "/portShield-Slice002.1.stl"), //centre middle
				new STLMesh(inPath + "/portShield-Slice002.2.stl"), //right middle
				new STLMesh(inPath + "/portShield-Slice002.3.stl"), //left top
				new STLMesh(inPath + "/portShield-Slice002.4.stl"), //centre top
				new STLMesh(inPath + "/portShield-Slice002.5.stl"), //right top
				new STLMesh(inPath + "/portShield-Slice002.6.stl"), //left bottom
				new STLMesh(inPath + "/portShield-Slice002.7.stl"), //centre/right bottom
			}),
						
			//Port and vessel walls
			//new STLMesh(inPath + "/port-AEM21-endOnly.stl"),
			//new STLMesh(inPath + "/zwickle2.stl"), 
			//new STLMesh(inPath + "/vessel-around-AEM21-AEN21.stl"), 
			new STLMesh(inPath + "/portLiner-AEN21-spaltSchutz.stl"),
			new STLMesh(inPath + "/portLiner-AEN21-spaltSchutz2.stl"),
			
			// Tube, shutter and components
			new STLMesh(inPath + "/tubeSimple.stl"),
			new Optic("Shutter", new Element[] {
				new STLMesh(inPath + "/mirrorBlockClosed-simplified.stl"),
				new STLMesh(inPath + "/protectionCollar1.stl"),
				new STLMesh(inPath + "/protectionCollar2.stl"),
				new STLMesh(inPath + "/shutterDriveMech1.stl"),
				new STLMesh(inPath + "/shutterDriveMech2.stl"),
				new STLMesh(inPath + "/shutterDriveMech3.stl"),
				new STLMesh(inPath + "/strap1.stl"),
				new STLMesh(inPath + "/strap2.stl"),
				new STLMesh(inPath + "/strapCover1.stl"),
				new STLMesh(inPath + "/strapCover2.stl"),
			}),
	//};
	
	//public static Element[] thingsInWay = {	
			new STLMesh(inPath + "/panel1.stl"),
			new STLMesh(inPath + "/panel2.stl"),
			new STLMesh(inPath + "/panel3.stl"),
			new STLMesh(inPath + "/vessel-around-AEM21-AEN21.stl"), 
			new STLMesh(inPath + "/portLiner-AEN21-simple.stl"), 
			
			
	};

	private static int nX = 30, nY= 30;
	//*/
		
	//AEM21
	//public static double a[] = Util.plus(radSurfaceCentre, Util.mul(radSurfaceNormal, -0.300)); 
	//public static Surface testSurface = new Disc("testdisc", a, radSurfaceNormal, 0.050, Absorber.ideal()); 
	
	
	public static int nRaysPerPoint = 30;
	public static int nSkip = 0;
	
	
	public final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName() + "/radExposure-portWalls-30x30x30/";
	
	/** Power emitted from radiating surface, Watts per square meter per Steradian */
	public static double powerAngularDensity = 100e3 / 2 / Math.PI;  //100/2.pi kW m^-2 SR^-1
	
	public static void main(String[] args) { 
		System.out.println(outPath);
		
		sys.removeElement(sys.mirror);
		sys.removeElement(sys.beamPlane);
		sys.removeElement(sys.catchPlane);
		sys.removeElement(sys.strayPlane);
		
		sys.removeElement(sys.panelEdge);
		sys.removeElement(sys.mirrorBlock);
		sys.removeElement(sys.mirrorClampRing);
		
		//sys.addElement(new Sphere("targSphere", testElement.getBoundarySphereCentre(), testElement.getBoundarySphereRadius(), NullInterface.ideal()));
		
		
		
		double dx = sys.radSurfWidth / (nX - 1);
		double dy = sys.radSurfHeight / (nY - 1);
			
		double col[][] = ColorMaps.jet(testElements.length);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/radExposure-"+sys.getDesignName() + ".vrml", 5.005);
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
		sys.addElement(new Optic("thingsInWay", thingsInWay));
		
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
		
		try {
			PrintStream textOut = new PrintStream(outPath + "/powers.txt");
			
			dumpPowers(textOut, totalPower, nHitTotal, dx, dy);
			dumpPowers(System.out, totalPower, nHitTotal, dx, dy);
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		vrmlOut.destroy();
	}

	private static void dumpPowers(PrintStream textOut, double[] totalPower, int[] nHitTotal, double dx, double dy) {

		double totalPowerAll = 0;
		for(int iTE=0; iTE < testElements.length; iTE++) {
			textOut.print(sys.getDesignName() + " " + testElements[iTE].getName() + ": nHit = " + nHitTotal[iTE] + ", dx*dy = " + (dx*dy) + ", Total = " + totalPower[iTE] + " W");
			totalPowerAll += totalPower[iTE];
			if(testElements[iTE] instanceof Disc) {
				double targetArea = Math.PI * FastMath.pow2(((Disc)testElements[iTE]).getRadius());
				textOut.print(", Power/area = " + (totalPower[iTE] / targetArea) + " W/m2");
			}else if(testElements[iTE] instanceof Square) {
				double targetArea = ((Square)testElements[iTE]).getWidth() *  ((Square)testElements[iTE]).getHeight();
				textOut.print(". Power/area = " + (totalPower[iTE] / targetArea) + " W/m2");				
			}
			textOut.println();
		}
		textOut.println("Total power captured = " + totalPowerAll + " W");
		
	}
}
