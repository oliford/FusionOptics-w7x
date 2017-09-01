package ipp.w7x.neutralBeams;


import algorithmrepository.Algorithms;
import fusionDefs.neutralBeams.SimpleBeamGeometry;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.types.Optic;

/** OP1.2 Pellets, (pretending to be a beam)
 * */
public class W7XPelletsK41 extends SimpleBeamGeometry {
	
	private static W7XPelletsK41 instance = new W7XPelletsK41();
	
	/** The default as-advertised geometry */
	public static W7XPelletsK41 def(){ return instance;	}
	
	private double Px[][];
	private double u[][];
	
	public W7XPelletsK41() {
	
		Px = new double[][]{
				{ -5.10919580078125, -6.47696875, 0.7010863647460938 },  // Centre of flange of entry sub-port on AEK41 flange 
			};
		u = new double[][]{
				{  0.63245724,  0.76193077, -0.13949673 }, //Axis of entry sub-port on AEK41 flange 
			};
		
		beamWidth = 0.03; // [J.Baldzuhn]
		sourceR = 8.00; // At entry flange
		plasmaR0 = 6.05; //~10cm outside plasma 
		plasmaR1 = 5.6; //plasma axis, pellet only expected to go in ~10cm 
		
	}

	@Override
	public double[][] startAll() { return Px; }
	@Override
	public double[][] uVecAll() { return u; }

	@Override
	public double[] startBox(int boxIdx) { return Px[0]; }

	@Override
	public double[] uVecBox(int boxIdx) { return u[0]; }

	@Override
	public double[] getVoltageAll() { return new double[]{ Double.NaN }; } // No idea what this will be
}
