package ipp.w7x.neutralBeams;


import oneLiners.OneLiners;
import ipp.neutralBeams.MakeBeamsVRML;
import fusionOptics.MinervaOpticsSettings;
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
		
		W7XRudix w7xRudix = new W7XRudix();
		MakeBeamsVRML.makeBeamsRadialColoured(outPath + "/w7xRuDIX-colourByR.vrml", w7xRudix);
		MakeBeamsVRML.makeBeamsAllGreen(outPath + "/w7xRuDIX-allGreen.vrml", w7xRudix);
		MakeBeamsVRML.makeBeamsPINIColoured(outPath + "/w7xRuDIX-colourByPini.vrml", w7xRudix);
		

		System.out.print("RuDIX p = ");		OneLiners.dumpArray(W7XRudix.def().startBox(0));
		System.out.print("RuDIX v = ");		OneLiners.dumpArray(W7XRudix.def().uVec(0));
		System.out.print("NBI K20 p = ");		OneLiners.dumpArray(W7xNBI.def().startBox(0));
		System.out.print("NBI K20 v = ");		OneLiners.dumpArray(W7xNBI.def().uVec(0));
		System.out.print("NBI K21 p = ");		OneLiners.dumpArray(W7xNBI.def().startBox(1));
		System.out.print("NBI K21 v = ");		OneLiners.dumpArray(W7xNBI.def().uVec(1));
	}	
	
}
