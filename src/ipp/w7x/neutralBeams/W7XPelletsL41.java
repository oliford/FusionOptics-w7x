package ipp.w7x.neutralBeams;


import algorithmrepository.Algorithms;
import fusionDefs.neutralBeams.SimpleBeamGeometry;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.types.Optic;

/** OP1.2 Pellets, (pretending to be a beam)
 * */
public class W7XPelletsL41 extends SimpleBeamGeometry {
	
	private static W7XPelletsL41 instance = new W7XPelletsL41();
	
	/** The default as-advertised geometry */
	public static W7XPelletsL41 def(){ return instance;	}
	
	private double Px[][];
	private double u[][];
	
	public W7XPelletsL41() {
	
		Px = new double[][]{
				{ -2.2802724609375002, -2.1077960205078123, -0.0727509880065918 },  // End of guide tube from CATIA 
			};
		u = new double[][]{
				{ -0.44775938, -0.88435189,  0.13203512 }, //Axis of end of guide tube  
			};
		
		beamWidth = 0.03; // [J.Baldzuhn]
		sourceR = 2.80; // At entry flange
		plasmaR0 = 5.1; //~10cm outside plasma 
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
