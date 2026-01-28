package ipp.w7x.fusionOptics.w7x.cxrs.aem50;

import java.util.HashMap;

import algorithmrepository.Algorithms;
import ipp.w7x.fusionOptics.w7x.cxrs.aem41.BeamEmissSpecAEM41;

/** The AEM41 QSC Immersion tube and optics were moved from AEM41 to AEM50 before OP2.4. This class just does the appropriate rotation */
public class BeamEmissSpecAEM50 extends BeamEmissSpecAEM41 {
	
	/** To get M41 to M50 we want to rotate 180 around the radial through the triangular cross-section between M41 and M50 */ 
	public static double rotationAxisToroidalAngle = 7.0 / 10 * 2*Math.PI; 
	
	public static double[] rotationAxis = { Math.cos(rotationAxisToroidalAngle), Math.sin(rotationAxisToroidalAngle), 0 };
	
	public static double rotationAngle = Math.PI;
	
	public String getDesignName() { return "aem50";	}
	
	public static HashMap<String, double[]> measured = new HashMap<>();	
	static {
		// Pre OP2.4 in-vessel alignment measurement
		measured.put("AEM50_A:12", new double[]{ -1120.97802734375, -6132.107421875, 437.01336669921875 }); //is cut by portliner. This might be 10. Not sure.
		measured.put("AEM50_A:14", new double[]{  -1122.32373046875, -5998.87158203125, 434.38946533203125 }); //
		measured.put("AEM50_A:13", new double[]{  -1115.2227783203125, -6019.15771484375, 438.15399169921875 }); //clear of portliner
		measured.put("AEM50_A:15", new double[]{ -1121.644775390625, -5985.375, 434.0535888671875 });
		measured.put("AEM50_A:10", new double[]{ -1119.3797607421875, -6126.126953125, 437.40155029296875 }); //very cut by portliner
		measured.put("AEM50_A:11", new double[]{ -1115.7437744140625, -6047.37060546875, 436.88372802734375 }); //cut by portliner
		measured.put("AEM50_A:20", new double[]{  -1110.6708984375, -5899.41015625, 414.9024963378906 });
		measured.put("AEM50_A:21", new double[]{ -1123.3070068359375, -5855.2587890625, 402.8792724609375 });
		measured.put("AEM50_A:25", new double[]{ -1107.808837890625, -5760.31396484375, 382.54718017578125 });
		measured.put("AEM50_A:30", new double[]{ -1097.3612060546875, -5656.1669921875, 380.6864929199219 });
		measured.put("AEM50_A:31", new double[]{ -1102.033935546875, -5622.568359375, 378.0204162597656 });
		measured.put("AEM50_A:35", new double[]{ -1092.76806640625, -5539.97607421875, 378.4144592285156});
		measured.put("AEM50_A:40", new double[]{ -1111.67138671875, -5430.9736328125, 392.806365966796 });
		measured.put("AEM50_A:41", new double[]{ -1120.1251220703125, -5395.74951171875, 396.56396484375 });
		measured.put("AEM50_A:45", new double[]{ -1134.2513427734375, -5247.24560546875, 424.9038391113281 });
		measured.put("AEM50_A:50", new double[]{ -1161.415283203125, -5062.45751953125, 468.832763671875 });
		
		
	}

	public BeamEmissSpecAEM50() {
		super();
		this.name = "beamSpec-aem41";
		this.backgroundSTLFiles = new String[]{
				"/work/cad/aem50/bg-targetting/shield-m50-cut2.stl",
				"/work/cad/aem50/bg-targetting/baffle-m50-cut2.stl",
				"/work/cad/aem50/bg-targetting/target-m50-cut2.stl",
		};

		this.rotate(new double[3], Algorithms.rotationMatrix(rotationAxis, rotationAngle));
		
		//the fibrePlanes will have been rotated by that, so we can copy back the fibre end positions and normals to our arrays
		for(int iB=0; iB < fibrePlanes.length; iB++){
			for(int iF=0; iF < fibrePlanes[iB].length; iF++){
				fibreEndPos[iB][iF] = fibrePlanes[iB][iF].getCentre();
				fibreEndNorm[iB][iF] = fibrePlanes[iB][iF].getNormal();
			}
		}
	}
}
