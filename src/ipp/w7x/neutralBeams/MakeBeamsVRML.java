package ipp.w7x.neutralBeams;

import otherSupport.ColorMaps;

import java.util.LinkedList;

import fusionDefs.neutralBeams.SimpleBeamGeometry;
import fusionOptics.Util;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.types.Element;
import fusionOptics.types.Optic;
import fusionOptics.types.Surface;
import net.jafama.FastMath;

/** Erm... Don't expect this to work
 * It used to make the VRML of the optics model of the neutral beams
 * It was kind-of spat out after lots of refactoring and Mavenisation
 * 
 * *** This exists both in FusionOptics-imse and FusionOptics-w7x ***
 * I can't find anywhere to put it without copying :(
 *  
 * @author oliford
 *
 */
public class MakeBeamsVRML {
	private static final double dL = 0.05;

	private static final double[][] cols = ColorMaps.jet(20);
	
	public static double vrmlTransformMatrix[][] = new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}};
	
	public static void makeBeamsAllGreen(String fileName, SimpleBeamGeometry beamGeom){
			
		VRMLDrawer vrmlOut = new VRMLDrawer(fileName, 0.005);
		vrmlOut.setTransformationMatrix(vrmlTransformMatrix);
		vrmlOut.setDrawPolarisationFrames(false);
		vrmlOut.drawOptic(makeAllBeamCylds(beamGeom));
		vrmlOut.destroy();
	}
	
	public static void makeBeamsPINIColoured(String fileName, SimpleBeamGeometry beamGeom){
		
		VRMLDrawer vrmlOut = new VRMLDrawer(fileName, 0.005);
		vrmlOut.setTransformationMatrix(vrmlTransformMatrix);
		vrmlOut.setDrawPolarisationFrames(false);
		
		double cols[][] = new double[][]{ 
				{1,1,0}, {0,1,0}, {1,0,1}, {1,0,0},
				{1,1,0}, {0,1,0}, {1,0,1}, {1,0,0},
			};
		
		for(int i=0; i < 8; i++){
			vrmlOut.addMat("beam_Q"+(i+1), cols[i][0] + " " + cols[i][1] + " " + cols[i][2], 0.7);
		}
				
		for(int i=0; i < beamGeom.nBeams(); i++){
			double l0 = beamGeom.getLOfBeamAxisAtR(i, beamGeom.plasmaR1());
			double l1 = beamGeom.getLOfBeamAxisAtR(i, beamGeom.plasmaR0());
			if(Double.isNaN(l1)){ l1 = 5; } //some beams dont hit the inner wall
			vrmlOut.startGroup("beamQ" +(i+1));
			vrmlOut.drawOptic(makeBeamCylds(beamGeom, i, dL, beamGeom.beamWidth(), l0, l1), "beam_Q"+(i+1), cols[i]);
			vrmlOut.endGroup();
		}
		
		vrmlOut.destroy();
	}
	
	public static void makeBeamsPINIColouredSeparate(String fileNamePrefix, SimpleBeamGeometry beamGeom){
		
		
		double cols[][] = new double[][]{ 
				{1,1,0}, {0,1,0}, {1,0,1}, {1,0,0},
				{1,1,0}, {0,1,0}, {1,0,1}, {1,0,0},
			};
		
				
		for(int i=0; i < beamGeom.nBeams(); i++){
			VRMLDrawer vrmlOut = new VRMLDrawer(fileNamePrefix + "-Q" + (i+1) + ".vrml", 0.005);
			vrmlOut.setTransformationMatrix(vrmlTransformMatrix);
			vrmlOut.setDrawPolarisationFrames(false);
			
			vrmlOut.addMat("beam_Q"+(i+1), cols[i][0] + " " + cols[i][1] + " " + cols[i][2], 0.7);
						
			double l0 = beamGeom.getLOfBeamAxisAtR(i, beamGeom.plasmaR1());
			double l1 = beamGeom.getLOfBeamAxisAtR(i, beamGeom.plasmaR0());
			if(Double.isNaN(l1)){ l1 = 5; } //some beams dont hit the inner wall
			vrmlOut.startGroup("beamQ" +(i+1));
			vrmlOut.drawOptic(makeBeamCylds(beamGeom, i, dL, beamGeom.beamWidth(), l0, l1), "beam_Q"+(i+1), cols[i]);
			vrmlOut.endGroup();
			
			vrmlOut.destroy();
		}
		
	}	
	
	public static void makeBeamsRadialColoured(String fileName, SimpleBeamGeometry beamGeom){
		VRMLDrawer vrmlOut = new VRMLDrawer(fileName, 0.005);
		for(int i=0; i < cols.length; i++)
			vrmlOut.addMat("beamR" + i, 
					cols[i][0] + " " + cols[i][1] + " " + cols[i][2],
					0.3);
		
		vrmlOut.addMat("beamOutOfRange", "0 0 0" , 0.8);
		vrmlOut.setTransformationMatrix(vrmlTransformMatrix);
		vrmlOut.setDrawPolarisationFrames(false);
		drawRadialColoured(vrmlOut, makeAllBeamCylds(beamGeom), beamGeom);
		vrmlOut.destroy();
	}
	
	public static void drawRadialColoured(VRMLDrawer vrmlOut, Optic optic, SimpleBeamGeometry beamGeom){
		
		for(Optic subOptic : optic.getSubOptics()){
			drawRadialColoured(vrmlOut, subOptic, beamGeom);
		}
		
		for(Surface surf : optic.getSurfaces()){
			double pos[] = surf.getBoundarySphereCentre();
			double R = FastMath.sqrt(pos[0]*pos[0] + pos[1]*pos[1]);
			
			int colIdx = (int)(cols.length * (R - beamGeom.plasmaR0()) / (beamGeom.plasmaR1() - beamGeom.plasmaR0()));
			
			if(colIdx >= 0 && colIdx < cols.length)
				vrmlOut.drawSurface(surf, "beamR" + colIdx, cols[colIdx]);
			//else
				//vrmlOut.drawSurface(surf, "beamOutOfRange");
			
		}
	}
	
	public static Optic makeAllBeamCylds(SimpleBeamGeometry beamGeom){
		//return makeAllBeamCylds(0.05, 0.16);
		return makeAllBeamCylds(beamGeom, 0.05, beamGeom.beamWidth());		
	}
	
	public static Optic makeAllBeamCylds(SimpleBeamGeometry beamGeom, double dL, double fwhm){
		double l0 = beamGeom.getLOfBeamAxisAtR(0, beamGeom.plasmaR0());
		double l1 = beamGeom.getLOfBeamAxisAtR(0, beamGeom.plasmaR1());		
		if(l1 < l0){
			double ll = l0;
			l0 = l1;
			l1 = ll;
		}
		return makeAllBeamCylds(beamGeom, dL, fwhm, l0, l1);
	}
		
	public static Optic makeAllBeamCylds(SimpleBeamGeometry beamGeom, double dL, double fwhm, double l0, double l1){
		LinkedList<Element> beams = new LinkedList<Element>();
		
		for(int i=0; i < beamGeom.nBeams(); i++){
			Optic beam = makeBeamCylds(beamGeom, i, dL, fwhm, l0, l1);
			beams.add(beam);
		}
		
		return new Optic("beamCylds", beams);
	}
	
	public static Optic makeBeamCylds(SimpleBeamGeometry beamGeom) {
		return makeBeamCylds(beamGeom, 1, 0.10, 0.16);
	}
	
	public static Optic makeBeamCylds(SimpleBeamGeometry beamGeom, int iB, double dL, double fwhm) {
		double l0 = 0.2;
		double l1 = 2.9;		
		return makeBeamCylds(beamGeom, iB, dL, fwhm, l0, l1);
	}
		
	public static Optic makeBeamCylds(SimpleBeamGeometry beamGeom, int iB, double dL, double fwhm, double l0, double l1) {
		LinkedList<Element> cylds = new LinkedList<Element>();
		
		
		for(int i=0; i < (l1 - l0) / dL; i++){
			double l = l0 + i * dL;			
			Cylinder clyd = new Cylinder(
					"cyld"+i,
					new double[]{ 
							beamGeom.start(iB)[0] + l * beamGeom.uVec(iB)[0],
							beamGeom.start(iB)[1] + l * beamGeom.uVec(iB)[1],
							beamGeom.start(iB)[2] + l * beamGeom.uVec(iB)[2],							
					},
					beamGeom.uVec(iB),
					fwhm/2, //radius, HWHM of beam
					dL, //length 
					NullInterface.ideal());
			clyd.setDrawingDetails(8, 10);
			
			cylds.add(clyd);
		}
		
		return new Optic("beamCylds" + iB, cylds);
	}
	
}
