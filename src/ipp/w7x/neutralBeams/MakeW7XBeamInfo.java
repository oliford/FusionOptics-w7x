package ipp.w7x.neutralBeams;


import algorithmrepository.Algorithms;
import oneLiners.OneLiners;
import fusionOptics.MinervaOpticsSettings;
import fusionOptics.Util;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.types.Optic;
import fusionOptics.types.Surface;
import jafama.FastMath;
import otherSupport.ColorMaps;

public class MakeW7XBeamInfo {
	
	
	public static void main(String[] args) {
		String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/w7xBeams";
		
		W7xNBI w7xNBI = W7xNBI.def();
		W7XRudix w7xRudix = new W7XRudix();
		
		
		System.out.print("NBI K20 p_box = ");	OneLiners.dumpArray(W7xNBI.def().startBox(0));
		System.out.print("NBI K20 v_box = ");	OneLiners.dumpArray(W7xNBI.def().uVecBox(0));
		System.out.print("NBI K21 p_box = ");	OneLiners.dumpArray(W7xNBI.def().startBox(1));
		System.out.print("NBI K21 v_box = ");	OneLiners.dumpArray(W7xNBI.def().uVecBox(1));
		
		for(int i=0; i < 8; i++){
			System.out.println("NBI source "+(i+1)+":  ");
			System.out.print("\tp_Entry = "); OneLiners.dumpArray(w7xNBI.def().getPosOfBeamAxisAtR(i, 6.3));
			System.out.print("\tp_Exit = "); OneLiners.dumpArray(w7xNBI.def().getPosOfBeamAxisAtR(i, 5.0));
			System.out.print("\tv = "); OneLiners.dumpArray(w7xNBI.def().uVec(i));
			
			// Impact radius = closest distance to machine central axis
			double a = Algorithms.pointOnLineNearestAnotherLine(W7xNBI.def().start(i), W7xNBI.def().uVec(i), new double[]{0, 0,0}, new double[]{0,0,1});
			double pos[] = Util.plus(W7xNBI.def().start(i), Util.mul(W7xNBI.def().uVec(i), a));
			System.out.println("\tImpact radius = " + FastMath.sqrt(pos[0]*pos[0] + pos[1]*pos[1]));			
		}

		System.out.print("RuDIX p = ");		OneLiners.dumpArray(W7XRudix.def().startBox(0));
		System.out.print("RuDIX v = ");		OneLiners.dumpArray(W7XRudix.def().uVec(0));
		
		System.out.print("\tp_Entry = "); OneLiners.dumpArray(W7XRudix.def().getPosOfBeamAxisAtR(0, 6.3));
		System.out.print("\tp_Exit = "); OneLiners.dumpArray(W7XRudix.def().getPosOfBeamAxisAtR(0, 4.6));
		
		double a = Algorithms.pointOnLineNearestAnotherLine(W7XRudix.def().start(0), W7XRudix.def().uVec(0), new double[]{0, 0,0}, new double[]{0,0,1});
		double pos[] = Util.plus(W7XRudix.def().start(0), Util.mul(W7XRudix.def().uVec(0), a));
		System.out.println("\tImpact radius = " + FastMath.sqrt(pos[0]*pos[0] + pos[1]*pos[1]));
		
	}	
	
}
