package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21;
import ipp.w7x.fusionOptics.w7x.cxrs.aea21.BeamEmissSpecAEA21U_CISDual;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_edgeUV;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsK41;
import ipp.w7x.fusionOptics.w7x.cxrs.aek41.BeamEmissSpecAEK21_pelletsL41;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_LC3_tilt3;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_obsolete;
import ipp.w7x.fusionOptics.w7x.cxrs.aem21.BeamEmissSpecAEM21_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET20_postDesign_LC3;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.BeamEmissSpecAET21_postDesign;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_HST_TwoFlatAndLenses_75mm_UVFS_3cmAperture;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_OneSmallFlatMirror;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_Parabolic;
import ipp.w7x.fusionOptics.w7x.cxrs.aet21.op2.BeamEmissSpecAET21_OP2_TwoFlatAndLenses;
import ipp.w7x.fusionOptics.w7x.cxrs.other.BeamEmissSpecAEM41;
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
	public final static int nAttempts = 100;
	//*/
	
	public static double wavelength = sys.designWavelenth;
	
	public static int nRaysToDraw = 10;
		
	public static String[] meshesToImage = {
			"/work/cad/wendel/stl/panel-m11.stl",
			"/work/cad/wendel/stl/target-m2.stl",
			"/work/cad/wendel/stl/baffel-m1.stl",
			"/work/cad/wendel/stl/baffel-m2.stl",
			"/work/cad/wendel/stl/panel-m20.stl",
			"/work/cad/wendel/stl/shield-m1.stl",
			"/work/cad/wendel/stl/shield-m2.stl",

	};
	public static int nPointsPerMesh = 20000;			
	
	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/" + sys.getDesignName();
			
	public static void main(String[] args) {
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/image-"+sys.getDesignName()+ ((writeWRLForDesigner != null) ? ("-" + writeWRLForDesigner + ".wrl") : ".vrml"), 1.005);
		if((writeWRLForDesigner == null)){
			vrmlOut.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});			
		}
		vrmlOut.setSkipRays((meshesToImage.length*nPointsPerMesh) / nRaysToDraw);
		double col[][] = ColorMaps.jet(nPointsPerMesh);
		
		/*Element[] e = new Element[meshesToImage.length];
		for(int i=0; i < meshesToImage.length; i++) {
			e[i] = new STLMesh(meshesToImage[i]);
		}
		sys.addElement(new Optic("meshes", e));
		*/
		
		//double imagePoints[][] = new double[meshesToImage.length*nPointsPerMesh*nAttempts][4];
		int iI=0;
		BinaryMatrixWriter binOut = new BinaryMatrixWriter(outPath + "/imageHits.bin", 5);
				
		for(int iB=0; iB < meshesToImage.length; iB++){
						
			Triangles triangles = BinarySTLFile.mustRead(meshesToImage[iB]);
						
			for(int iP=0; iP < nPointsPerMesh; iP++){
								
				int iT = (int)((long)iP * (long)triangles.count / nPointsPerMesh);
				
				int iV1,iV2;
				do {
					iV1 = (int)RandomManager.instance().nextUniform(0, 3);
					iV2 = (int)RandomManager.instance().nextUniform(0, 3);
				}while(iV1 != iV2);				
				double v[][] = { triangles.vertex1[iT], triangles.vertex2[iT], triangles.vertex3[iT] };
				double p0[] = Util.mul(v[iV1], 1e-3);
				double p1[] = Util.mul(v[iV2], 1e-3);
				
				double dp[] = Util.minus(p1, p0);
				double l = Util.length(dp);
				double d = RandomManager.instance().nextUniform(0, 1) * l;
				double startPos[] = Util.plus(p0, Util.mul(dp, d));
							
								
				int nHit = 0;
				
				
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
							binOut.writeRow(xy, j, iB, ray.length);
							iI++;
							vrmlOut.drawRay(ray, col[iP]);
							
							nHit++;
						}
					}
					
					Pol.recoverAll();
					
					//if(nHit > 0)
					//	break;
					
					//hits = ray.getIntersections(mustHitToDraw);
					//if(hits.size() > 0){												
					//	vrmlOut.drawRay(ray, col[iP]);
					//}
				}

				
				System.out.println(meshesToImage[iB] + " " + iB + " " + iP + " (" + iT + "): " + nHit);
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
