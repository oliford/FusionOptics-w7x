package ipp.w7x.fusionOptics.w7x.cxrs.aem41;

import algorithmrepository.Algorithms;

/** The AEM41 QSC Immersion tube and optics were moved from AEM41 to AEM50 before OP2.4. This class just does the appropriate rotation */
public class BeamEmissSpecAEM50 extends BeamEmissSpecAEM41 {
	
	/** To get M41 to M50 we want to rotate 180 around the radial through the triangular cross-section between M41 and M50 */ 
	public static double rotationAxisToroidalAngle = 7.0 / 10 * 2*Math.PI; 
	
	public static double[] rotationAxis = { Math.cos(rotationAxisToroidalAngle), Math.sin(rotationAxisToroidalAngle), 0 };
	
	public static double rotationAngle = Math.PI;
	
	public String getDesignName() { return "aem50";	}

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
