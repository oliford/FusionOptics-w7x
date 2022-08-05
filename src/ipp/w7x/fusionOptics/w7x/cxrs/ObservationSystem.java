package ipp.w7x.fusionOptics.w7x.cxrs;

import fusionOptics.types.Optic;

/* Generic fiber based observation system at W7X */
public abstract class ObservationSystem extends Optic {
	
	public ObservationSystem(String name) {		super(name);  }

	public String getChanName(int iB, int iP) {
		return lightPathsSystemName() + lightPathRowName(iB) + ":" + String.format("%02d", iP+1);
	}

	protected abstract String lightPathsSystemName();

	protected String lightPathRowName(int iB) {
		String n[] = lightPathRowNames();
		return n != null ? n[iB] : ""; 
	}

	protected abstract String[] lightPathRowNames();
	
}
