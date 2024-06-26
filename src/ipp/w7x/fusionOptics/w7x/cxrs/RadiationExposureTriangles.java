package ipp.w7x.fusionOptics.w7x.cxrs;

import net.jafama.FastMath;
import uk.co.oliford.jolu.ColorMaps;
import uk.co.oliford.jolu.OneLiners;
import uk.co.oliford.jolu.RandomManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import algorithmrepository.Algorithms;
import binaryMatrixFile.AsciiMatrixFile;
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
import fusionOptics.surfaces.Triangle;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;

/** Simple radiation exposure calculation by emitting rays in 4.pi from a plane representing the plasma edge
 * and seeing how many hit a given surface.
 *  
 * @author oliford
 *
 */
public abstract class RadiationExposureTriangles {
	
	public static String inPath = "/work/cad/radExposure";	
	public static Element[] testElements;	
	public static Element[] thingsInWay;

	public static int nRays = 1000000;
	public static int nRaysToDraw = 1000;
	public static int nThreads = 1;
	
	public static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/radExposure/";
	
	/** Power emitted from radiating surface, Watts per square meter per Steradian */
	public static double powerAngularDensity = 100e3 / 2 / Math.PI;  //100/2.pi kW m^-2 SR^-1
	
	public abstract Optic sys();
	public abstract Square radSurf();
	public abstract String designName();
	public Element tracingTarget() { return testElementsAll; }
	
	/* Things used by all threads */
	private Optic all;
	private Optic testElementsAll;
	private static VRMLDrawer vrmlOut;
	private HashMap<Element, double[][]> triangleHits;
	private double[] totalPower;
	private int[] nHitTotal;
	public int nRaysDone, nRaysDrawn;
	private double dA;
	private double[][] col;
	
	public void start() { 
		System.out.println(outPath);
		
		dA = radSurf().getHeight() * radSurf().getWidth() / nRays;
			
		col = ColorMaps.jet(testElements.length);
		
		vrmlOut = new VRMLDrawer(outPath + "/radExposure-"+designName() + ".vrml", 5.005);
		vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		

		Path inPathP = Paths.get(inPath);
		Path inPathLink = Paths.get(outPath, "inputFiles");
							
	    try {
	    	Files.deleteIfExists(inPathLink);
			Files.createSymbolicLink(inPathLink, inPathP);
		} catch (IOException e) { e.printStackTrace();}
	    
		
		for(Element testElement : testElements) {
			if(testElement instanceof Surface)
				((Surface)testElement).setInterface(Absorber.ideal());
			if(testElement instanceof Optic)
				for(Surface s : ((Optic)testElement).getSurfacesAll())
					s.setInterface(Absorber.ideal());
		}
			
		
		testElementsAll = new Optic("testElements", testElements);
		
		all = new Optic("TestSetup", new Element[] {
				radSurf(),
				testElementsAll,
				new Optic("thingsInWay", thingsInWay),
		});
		//vrmlOut.drawOptic(all);
		vrmlOut.drawOptic(new Optic("RadiationSurface", new Element[] { radSurf() }));
		
		
		totalPower = new double[testElements.length];
		nHitTotal = new int[testElements.length];
		
		triangleHits = new HashMap<Element, double[][]>();
		for(Element testElement : testElements) {
			if(testElement instanceof STLMesh) {
				triangleHits.put(testElement, new double[((STLMesh)testElement).getSurfaces().size()][2]);
			}
		}
		
		nRaysDone = 0;
		nRaysDrawn = 0;
		
		startThreads();
		
		while(nRaysDone < nRays){
			
			System.out.println("Rays: " + nRaysDone + " / " + nRays + ", nHit = " + sum(nHitTotal));
			dumpPowers(totalPower, nHitTotal, dA, ((double)nRaysDone)/nRays);
			dumpTrianglePowers(triangleHits, dA, ((double)nRaysDone)/nRays);
			System.out.println("VRML: "+ nRaysDrawn + " / " + nRaysToDraw);
			
			if(vrmlOut != null && nRaysDrawn >= nRaysToDraw) {
				synchronized (vrmlOut) {
					System.out.println("VRML Done");
					vrmlOut.destroy();
					
					Path vrmlPath = Paths.get(vrmlOut.getFileName());
					Path linkPath = Paths.get(inPath, vrmlPath.getFileName().toString());					
				    try {
				    	Files.deleteIfExists(linkPath);
						Files.createSymbolicLink(linkPath, vrmlPath);
					} catch (IOException e) { e.printStackTrace();}
				    
				    vrmlOut = null;
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }
			
		}
		
		dumpPowers(totalPower, nHitTotal, dA, 1.0);
		
		if(vrmlOut != null)
			vrmlOut.destroy();
	}
	
	private long sum(int a[]) {
		long sum=0;
		for(int i=0; i < a.length; i++)
			sum += a[i];
		return sum;
	}
	
	private void startThreads() {
		for(int i=0; i < nThreads; i++) {
			final int is = i;
			new Thread(new Runnable() {				
				@Override
				public void run() {
					inThread(is);					
				}
			}).start();			
		}
	}	
	
	private void inThread(int is) {
		RandomManager rnd = new RandomManager();
		while(nRaysDone < nRays) {
			double y = radSurf().getHeight() * (rnd.nextUniform(0,1) - 0.5); 
			double x = radSurf().getWidth() * (rnd.nextUniform(0,1) - 0.5);
			//x=0;y=0;
			
			double startPos[] = Util.plus(radSurf().getCentre(), 
									Util.plus(
											Util.mul(radSurf().getRight(), x),
											Util.mul(radSurf().getUp(), y) ));
			

			RaySegment ray = new RaySegment();
			ray.startPos = startPos;
			
			ray.dir = Tracer.generateRandomRayTowardSurface(rnd, startPos, tracingTarget(), true);
			double solidAngle = Util.length(ray.dir);
			
			/*ray.dir = Util.reNorm(new double[] { 
					RandomManager.instance().nextNormal(0, 1),
					RandomManager.instance().nextNormal(0, 1),
					RandomManager.instance().nextNormal(0, 1),
			});
			solidAngle = 4 * Math.PI;*/
			//System.out.println(solidAngle);
			
			ray.dir = Util.reNorm(ray.dir);
					
			//ray.dir = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget);
			ray.wavelength = 500e-9; //irrelevant
			ray.E0 = new double[][]{{1,0,0,0}};
			ray.up = Util.createPerp(ray.dir);
					
			Tracer.trace(all, ray, 100, 0, false);
					
			for(int iTE=0; iTE < testElements.length; iTE++) {						
				List<Intersection> hits = ray.getIntersections(testElements[iTE]);
				if(hits.size() > 0){
					double power = powerAngularDensity * dA * solidAngle; //power emitted into ray generation cone
					synchronized (totalPower) {
						totalPower[iTE] += power;
						nHitTotal[iTE] += 1;
					}
					
					synchronized (triangleHits) {
						Surface s = hits.get(0).surface;
						Optic parentOptic = s.getOptic();
						if(s.getName().startsWith("triangle_")) {
							int iTri = Integer.parseInt(s.getName().substring(9));
							
							double triHits[][] = triangleHits.get(parentOptic);
							triHits[iTri][0]++;							
							triHits[iTri][1] += power;
						}
					}
				
					if(vrmlOut != null) {
						synchronized (vrmlOut) {
							if(vrmlOut != null) {
								vrmlOut.drawRay(ray, col[iTE]);
								nRaysDrawn++;
							}
						}							
					}
					break; //don't count twice. Shouldn't if they are absorbers
				}
			}
			
			Pol.recoverAll();
			
			nRaysDone++;
		}
	}
	
	private void dumpTrianglePowers(HashMap<Element, double[][]> triangleHits, double dA, double fracCollected) {
		
		for(Element testElement : triangleHits.keySet()) {
			double triHits[][] = triangleHits.get(testElement);
			int nTri = triHits.length;
			double d[][] = new double[nTri][4];
			
			for(int i=0; i < nTri; i++) {
				Triangle t = ((Triangle)((STLMesh)testElement).getSurfaces().get(i));
				
				d[i][0] = triHits[i][0]; //hits
				d[i][1] = triHits[i][1] / fracCollected; //power
				d[i][2] = t.getArea();
				d[i][3] = d[i][1] / d[i][2];
			}
		
			AsciiMatrixFile.mustWrite(outPath + "/triangleHits/"+testElement.getName()+".txt", d, false);
		}
	}

	private void dumpPowers(double[] totalPower, int[] nHitTotal, double dA, double fracCollected) {
		try {
			PrintStream textOut = new PrintStream(outPath + "/powers.txt");
			
			dumpPowers(textOut, totalPower, nHitTotal, dA, fracCollected);
			dumpPowers(System.out, totalPower, nHitTotal, dA, fracCollected);
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void dumpPowers(PrintStream textOut, double[] totalPower, int[] nHitTotal, double dA, double fracCollected) {

		double totalPowerAll = 0;
		for(int iTE=0; iTE < testElements.length; iTE++) {
			double finalTotalPower = (totalPower[iTE] / fracCollected);
			textOut.print(designName() + " " + testElements[iTE].getName() + ": nHit = " + nHitTotal[iTE] + ", dA = " + (dA) + ", Total = " + finalTotalPower + " W");
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
