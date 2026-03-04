package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionOptics.types.Optic;

/** Generic fiber based observation system at W7X. Used by the various design apps. */
public abstract class ObservationSystem extends Optic {
	
	public ObservationSystem(String name) {		super(name);  }

	public String getChanName(int iB, int iP) {
		return lightPathsSystemName() + lightPathRowName(iB) + ":" + String.format("%02d", iP+1);
	}

	public abstract String lightPathsSystemName();

	public String lightPathRowName(int iB) {
		String n[] = lightPathRowNames();
		return n != null ? n[iB] : ""; 
	}

	public abstract String[] lightPathRowNames();
	
	public abstract double getFibreNA(int iB, int iP);
	public abstract double getFibreDiameter(int iB, int iP);

	protected abstract String getDesignName();

	protected abstract double[][] channelR();

	protected abstract int[] beamIdx();

	protected abstract double[][][] fibreEndPos();
	protected abstract double[][][] fibreEndNorm();
	
	/** Return list of files containing STL meshes of the wall that the lines of sight might hit. Used primarily by BackgroundTargetting... */
	public abstract String[] backgroundSTLFiles();
}
