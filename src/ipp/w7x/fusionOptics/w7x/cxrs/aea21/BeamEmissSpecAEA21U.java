package ipp.w7x.fusionOptics.w7x.cxrs.aea21;

import org.apache.commons.math3.util.FastMath;

import algorithmrepository.Algorithms;
import fusionOptics.Util;
import uk.co.oliford.jolu.OneLiners;

public class BeamEmissSpecAEA21U extends BeamEmissSpecAEA21 {

	public BeamEmissSpecAEA21U() {
		super(Subsystem.CXRS);
		
				
		//now rotate everything around centre of A port
		double module2centreAng = 2 * Math.PI / 5;
		
		double rotAxis[] = { FastMath.cos(module2centreAng), FastMath.sin(module2centreAng), 0 };
		double rotCentre[] = Util.mul(rotAxis, 6.0); //actually doesn't matter
		double rotMat[][] = Algorithms.rotationMatrix(rotAxis, Math.PI);
		
		this.rotate(rotCentre, rotMat);
		
		for(int i=0; i < fibreEndPos.length; i++) {
			for(int j=0; j < fibreEndPos[i].length; j++) {
				fibreEndPos[i][j] = OneLiners.rotateVectorAroundAxis(Math.PI, rotAxis, fibreEndPos[i][j]);
			}
		}
		
	}
	
	@Override
	public String lightPathsSystemName() { return "AEA21u";	}

	public String getDesignName() { return "aea21u";	}
}
