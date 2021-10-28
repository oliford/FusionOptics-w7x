package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21U_CISDual;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK41_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_LC3_tilt3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_obsolete;
import ipp.w7x.fusionOptics.w7x.cxrs.aem41.BeamEmissSpecAEM41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET20_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses_75mm_UVFS_3cmAperture;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_OneSmallFlatMirror;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_Parabolic;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_TwoFlatAndLenses;
import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEW21;
import ipp.w7x.neutralBeams.W7XRudix;
import ipp.w7x.neutralBeams.W7xNBI;
import net.jafama.FastMath;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import binaryMatrixFile.AsciiMatrixFile;
import binaryMatrixFile.BinaryMatrixFile;
import binaryMatrixFile.BinaryMatrixWriter;
import otherSupport.BinarySTLFile;
import otherSupport.ColorMaps;
import otherSupport.RandomManager;
import otherSupport.ScientificNumberFormat;
import otherSupport.BinarySTLFile.Triangles;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.collection.HitPositionAverage;
import fusionOptics.collection.IntensityInfo;
import fusionOptics.collection.LightConeInfo;
import fusionOptics.collection.PlaneAngleInfo;
import fusionOptics.drawing.STLDrawer;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.optics.STLMesh;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Plane;
import fusionOptics.surfaces.Sphere;
import fusionOptics.surfaces.Triangle;
import fusionOptics.tracer.Tracer;
import fusionOptics.types.Element;
import fusionOptics.types.Intersection;
import fusionOptics.types.Optic;
import fusionOptics.types.Pol;
import fusionOptics.types.RaySegment;
import fusionOptics.types.Surface;

/** Make image as CIS/Video will see it through AEA21 */
public class CISImage {
	
	public static BeamEmissSpecAEA21U_CISDual sys = new BeamEmissSpecAEA21U_CISDual();
	
	public static Surface mustHitToDraw = sys.entryWindowFront;	

			
	public static Plane[] imagePlanes = {
			sys.fibrePlane1,
			sys.fibrePlane2
	};
	
	public static boolean writeSolidAngeInfo = true;
	public static String writeWRLForDesigner = null;//"-20160826";
	public final static int nAttempts = 1000;
	//*/
	
	public static double wavelength = sys.designWavelenth;
	
	public static int nRaysToDraw = 10000;
		
	public static String[] meshesToImage = {
			"/work/cad/wendel/stl/panel-m11.stl",
			"/work/cad/wendel/stl/baffel-m1.stl",
			"/work/cad/w7xBeams/Q4.stl",
			"/work/cad/wendel/stl/target-m2.stl",
			"/work/cad/wendel/stl/baffel-m2.stl",
			"/work/cad/wendel/stl/panel-m20.stl",
			"/work/cad/wendel/stl/shield-m1.stl",
			"/work/cad/wendel/stl/shield-m2.stl",
			"/work/cad/wendel/stl/panel-m10.stl",

	};
	public static int nPointsPerM = 100;	//lower number will favor drawing long edges of triangles
	public static int nPointsPerMesh = 500;
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName();
			
	public static void main(String[] args) {

		double s[] = sys.mirror.getCentre();
		double u[] = sys.mirror.getNormal();
		double cyldRadius = FastMath.sqrt(0.060*0.060 + 0.030*0.030);
		double cyldLen = 0.010;
		System.out.println("o=FreeCAD.ActiveDocument.addObject(\"Part::Cylinder\", \"mirrorSurface\"); "+
				"o.Shape = Part.makeCylinder("+cyldRadius*1e3+","+cyldLen*1e3 +
				",FreeCAD.Vector("+s[0]*1e3+","+s[1]*1e3+","+s[2]*1e3 +
				"), FreeCAD.Vector("+u[0]*1e3+","+u[1]*1e3+","+u[2]*1e3+"), 360);");
		
		STLDrawer stlDraw = new STLDrawer(outPath + "/mirror.stl");
		stlDraw.setTransformationMatrix(new double[][] {{ 1000,0,0 },{0,1000,0},{0,0,1000}});
		stlDraw.drawElement(sys.mirror);
		stlDraw.destroy();
		System.exit(0);
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/image-"+sys.getDesignName()+ ((writeWRLForDesigner != null) ? ("-" + writeWRLForDesigner + ".wrl") : ".vrml"), 1.005);
		if((writeWRLForDesigner == null)){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		vrmlOut.setSkipRays((int)(((long)meshesToImage.length*nPointsPerMesh*nAttempts) / nRaysToDraw));
		double col[][] = ColorMaps.jet(meshesToImage.length);
		
		/*Element[] e = new Element[meshesToImage.length];
		for(int i=0; i < meshesToImage.length; i++) {
			e[i] = new STLMesh(meshesToImage[i]);
		}
		sys.addElement(new Optic("meshes", e));
		*/
		
		
		//double imagePoints[][] = new double[meshesToImage.length*nPointsPerMesh*nAttempts][4];
		int iI=0;
		BinaryMatrixWriter binOut = new BinaryMatrixWriter(outPath + "/imageHits.bin", 6);
		
		OneLiners.TextToFile(outPath + "/stlFiles.txt", meshesToImage);
				
		for(int iB=0; iB < meshesToImage.length; iB++){
						
			Triangles triangles = BinarySTLFile.mustRead(meshesToImage[iB]);
						
			for(int iP=0; iP < nPointsPerMesh; iP++){
								
				double l,d;
				double startPos[];
				
				do {
					//pick a random triangle and a random edge
					int iT = (int)RandomManager.instance().nextUniform(0, triangles.count);				
					int iV1,iV2;
					do {
						iV1 = (int)RandomManager.instance().nextUniform(0, 3);
						iV2 = (int)RandomManager.instance().nextUniform(0, 3);
					}while(iV1 == iV2);				
					double v[][] = { triangles.vertex1[iT], triangles.vertex2[iT], triangles.vertex3[iT] };
					double p0[] = Util.mul(v[iV1], 1e-3);
					double p1[] = Util.mul(v[iV2], 1e-3);
					
					double dp[] = Util.minus(p1, p0);
					l = Util.length(dp);
					
					//pick a random point along '1m / nPointsPerM', and see if it's inside this length
					d = RandomManager.instance().nextUniform(0, 1) / nPointsPerM;
					startPos = Util.plus(p0, Util.mul(dp, d));
					
				}while(d > l);				
															
				int nHit[] = new int[2];
				
				double xyAvg[][] = new double[imagePlanes.length][2];
				double lRay = Double.NaN;;
				for(int i=0; i < nAttempts; i++){
					RaySegment ray = new RaySegment();
					ray.startPos = startPos.clone();
					ray.dir = Tracer.generateRandomRayTowardSurface(startPos, sys.tracingTarget);
					ray.wavelength = wavelength;
					ray.E0 = new double[][]{{1,0,0,0}};
					ray.up = Util.createPerp(ray.dir);
							
					Tracer.trace(sys, ray, 100, 0, false);
					
					
					for(int j=0; j < imagePlanes.length; j++) {
						List<Intersection> hits = ray.getIntersections(imagePlanes[j]);
						if(hits.size() > 0){
							Intersection fibrePlaneHit = hits.get(0);
							
							double xy[] = imagePlanes[j].posXYZToPlaneRU(fibrePlaneHit.pos);
							//binOut.writeRow(xy, j, iB, ray.length);
							xyAvg[j][0] += xy[0];
							xyAvg[j][1] += xy[1];
							lRay = ray.length;
							
							iI++;
							vrmlOut.drawRay(ray, col[iB]);
							
							nHit[j]++;
						}
					}
					
					Pol.recoverAll();
					
				}
				
				for(int j=0; j < imagePlanes.length; j++) {
					if(nHit[j] > 0) {
						binOut.writeRow(xyAvg[j][0]/nHit[j],
										xyAvg[j][1]/nHit[j],
										j, iB, l, nHit[j]);
					}
				}

				
				System.out.println(meshesToImage[iB] + " " + iB + " " + iP + ": n1=" + nHit[0] + ", n2=" + nHit[1]);
			}
		}
		System.out.println("\n------------------------------------------------------------------------------------\n");
		System.out.println("Image points = " + iI);
		
		//imagePoints = Arrays.copyOf(imagePoints, iI);
		//AsciiMatrixFile.mustWrite(outPath + "/imageHits.txt", imagePoints);
		binOut.close();
				
		if( ((Object)sys) instanceof BeamEmissSpecAEW21) 
			sys.removeElement(((BeamEmissSpecAEW21)(Object)sys).shieldTiles);
		
		//sys.addElement(fibreCylds);
		
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(W7XBeamDefsSimple.makeBeamClyds());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}


}
