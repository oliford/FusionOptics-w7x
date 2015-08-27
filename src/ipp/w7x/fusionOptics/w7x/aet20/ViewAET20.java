package ipp.w7x.fusionOptics.w7x.aet20;

import fusionOptics.MinervaOpticsSettings;
import fusionOptics.drawing.VRMLDrawer;
import fusionOptics.surfaces.Cylinder;

/** Basic pictures for BeamEmissSpecAET20 model */
public class ViewAET20 {

	final static String outPath = MinervaOpticsSettings.getAppsOutputPath() + "/rayTracing/cxrs/";
	public static String vrmlScaleToAUGDDD = "Separator {\n" + //rescale to match the augddd STL models
			"Scale { scaleFactor 1000 1000 1000 }\n";
	
	public static void main(String[] args) {
		BeamEmissSpecAET20 sys = new BeamEmissSpecAET20();
		
		VRMLDrawer vrmlOut = new VRMLDrawer(outPath + "/aet20.vrml", 1.005);
		vrmlOut.setRotationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//vrmlOut.addVRML(vrmlScaleToAUGDDD);
		
		vrmlOut.drawOptic(sys);
		//vrmlOut.drawOptic(new W7XBeamDefsSimple());
		
		//vrmlOut.addVRML("}");
		vrmlOut.destroy();
	}
}
