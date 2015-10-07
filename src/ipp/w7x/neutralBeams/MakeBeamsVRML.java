package ipp.w7x.neutralBeams;


import fusionOptics.MinervaOpticsSettings;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.types.Optic;
import fusionOptics.types.Surface;
import jafama.FastMath;
import otherSupport.ColorMaps;

public class MakeBeamsVRML {
	private static final double R0 = 1.04; //inner heat shield
	//private static final double R0 = 1.15; // HFS plasma edge
	//private static final double R0 = 1.50; //just past core
	//private static final double R1 = 2.10; //LFS plama edge
	private static final double R1 = 8.00; // past pini pivot at ~7.6m
	private static final double fwhm = 0.001; //0.02;//0.16;
	private static final double dL = 0.05;
	private static final double[][] cols = ColorMaps.jet(20);
	private static final double l0 = -8;
	private static final double l1 = 3;
	
	
	public static void main(String[] args) {
		String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/w7xBeams/";
		makeBeamsRadialColoured(outPath + "/w7xBeams-colourByR.vrml");
		makeBeamsAllGreen(outPath + "/w7xBeams-allGreen.vrml");
		makeBeamsPINIColoured(outPath + "/w7xBeams-colourByPini.vrml");
	}	
	
	private static void makeBeamsAllGreen(String fileName){
			
		VRMLDrawer vrmlOut = new VRMLDrawer(fileName, 0.005);
		vrmlOut.setRotationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		vrmlOut.setDrawPolarisationFrames(false);
		vrmlOut.drawOptic(W7xNBI.makeAllBeamCylds());
		vrmlOut.drawOptic(W7XRudix.makeAllBeamCylds());
		vrmlOut.destroy();
	}
	
	private static void makeBeamsPINIColoured(String fileName){
		
		VRMLDrawer vrmlOut = new VRMLDrawer(fileName, 0.005);
		vrmlOut.setRotationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		vrmlOut.setDrawPolarisationFrames(false);
		
		vrmlOut.addMat("beam_Q1", "1 1 0", 0.7);  
		vrmlOut.addMat("beam_Q2", "0 1 0", 0.7);  
		vrmlOut.addMat("beam_Q3", "0 0 1", 0.7);  
		vrmlOut.addMat("beam_Q4", "1 0 0", 0.7);  
				
		vrmlOut.drawOptic(W7xNBI.makeBeamCylds(W7xNBI.BEAM_Q1, dL, fwhm, l0, l1), "beam_Q1");
		vrmlOut.drawOptic(W7xNBI.makeBeamCylds(W7xNBI.BEAM_Q2, dL, fwhm, l0, l1), "beam_Q2");
		vrmlOut.drawOptic(W7xNBI.makeBeamCylds(W7xNBI.BEAM_Q3, dL, fwhm, l0, l1), "beam_Q3");
		vrmlOut.drawOptic(W7xNBI.makeBeamCylds(W7xNBI.BEAM_Q4, dL, fwhm, l0, l1), "beam_Q4");
		
		vrmlOut.destroy();
	}	
	
	private static void makeBeamsRadialColoured(String fileName){
		VRMLDrawer vrmlOut = new VRMLDrawer(fileName, 0.005);
		for(int i=0; i < cols.length; i++)
			vrmlOut.addMat("beamR" + i, 
					cols[i][0] + " " + cols[i][1] + " " + cols[i][2],
					0.3);
		
		vrmlOut.addMat("beamOutOfRange", "0 0 0" , 0.8);
	
		vrmlOut.setRotationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		vrmlOut.setDrawPolarisationFrames(false);
		drawRadialColoured(vrmlOut, W7xNBI.makeAllBeamCylds());
		drawRadialColoured(vrmlOut, W7XRudix.makeAllBeamCylds());
		vrmlOut.destroy();
	}
	
	private static void drawRadialColoured(VRMLDrawer vrmlOut, Optic optic){
		
		for(Optic subOptic : optic.getSubOptics()){
			drawRadialColoured(vrmlOut, subOptic);
		}
		
		for(Surface surf : optic.getSurfaces()){
			double pos[] = surf.getBoundarySphereCentre();
			double R = FastMath.sqrt(pos[0]*pos[0] + pos[1]*pos[1]);
			
			int colIdx = (int)(cols.length * (R - R0) / (R1 - R0));
			
			if(colIdx >= 0 && colIdx < cols.length)
				vrmlOut.drawSurface(surf, "beamR" + colIdx);
			//else
				//vrmlOut.drawSurface(surf, "beamOutOfRange");
			
		}
	}
}
