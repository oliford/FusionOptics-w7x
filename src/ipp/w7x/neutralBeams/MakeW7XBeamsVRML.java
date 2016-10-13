package ipp.w7x.neutralBeams;


import algorithmrepository.Algorithms;
import oneLiners.OneLiners;
import ipp.neutralBeams.MakeBeamsVRML;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.types.Optic;
import fusionOptics.types.Surface;
import jafama.FastMath;
import otherSupport.ColorMaps;

public class MakeW7XBeamsVRML {
	
	
	public static void main(String[] args) {
		String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/w7xBeams/";
		
		W7xNBI w7xNBI = W7xNBI.def();
		MakeBeamsVRML.makeBeamsRadialColoured(outPath + "/w7xNBI-colourByR.vrml", w7xNBI);
		MakeBeamsVRML.makeBeamsAllGreen(outPath + "/w7xNBI-allGreen.vrml", w7xNBI);
		MakeBeamsVRML.makeBeamsPINIColoured(outPath + "/w7xNBI-colourByPini.vrml", w7xNBI);
		MakeBeamsVRML.makeBeamsPINIColouredSeparate(outPath + "/w7xNBI-colourByPini", w7xNBI);
		
		W7XRudix w7xRudix = new W7XRudix();
		MakeBeamsVRML.makeBeamsRadialColoured(outPath + "/w7xRuDIX-colourByR.vrml", w7xRudix);
		MakeBeamsVRML.makeBeamsAllGreen(outPath + "/w7xRuDIX-allGreen.vrml", w7xRudix);
		MakeBeamsVRML.makeBeamsPINIColoured(outPath + "/w7xRuDIX-colourByPini.vrml", w7xRudix);
		
	}	
	
}
