package ipp.w7x.neutralBeams;

import algorithmrepository.Algorithms;
import fusionDefs.neutralBeams.SimpleBeamGeometry;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.types.Optic;

/** OP1.2 Pellets, (pretending to be a beam)
 * */
public class EdgePenetrationAEK41 extends SimpleBeamGeometry {
	
	private static EdgePenetrationAEK41 instance = new EdgePenetrationAEK41();
	
	/** The default as-advertised geometry */
	public static EdgePenetrationAEK41 def(){ return instance;	}
	
	private double Px[][];
	private double u[][];
	
	public EdgePenetrationAEK41() {
	
		Px = new double[][]{
				{ -3.354854248046875, -4.325751953125, 0.8589786987304687 },  // VMEC LCFS point 
			};
		u = new double[][]{
				Util.reNorm(new double[]{ 0.031499267578125004, 0.01667236328125, -0.24191143798828124 }), //roughly perp to surfaces there 
			};
		
		Px[0] = Util.minus(Px[0], Util.mul(u[0], 0.200));
		
		beamWidth = 0.30; // nonsense
		sourceR = 10.00; // nonsense
		plasmaR0 = 6.2; //~10cm outside plasma 
		plasmaR1 = 5.4; //plasma axis, pellet only expected to go in ~10cm 
		
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
