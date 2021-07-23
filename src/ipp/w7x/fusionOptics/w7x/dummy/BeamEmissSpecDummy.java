package ipp.w7x.fusionOptics.w7x.dummy;

import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Optic;

// Dummy CXRS optics system to satisfy dependencies for radiation exposure program
public class BeamEmissSpecDummy extends Optic {

	public BeamEmissSpecDummy() { super("dummy");	}
	public String getDesignName() { return "dummy";	}
	
	public double[] radSurfaceCentre = { -3.416094970703125, -4.43269921875, 0.31065835571289063 };
	public double[] radSurfaceNormal = { 0.042628784179687504, 0.18094873046875, 0.3349514923095703 };
	
	public double[] radSurfaceUpish = { -0.0196651611328125, 0.006761962890625, -1.00250244140625e-05 };
	
	public double[] radSurfaceRight = Util.reNorm(Util.cross(radSurfaceNormal, radSurfaceUpish));
	public double[] radSurfaceUp = Util.reNorm(Util.cross(radSurfaceRight, radSurfaceNormal));
	
	public double radSurfWidth = 1.000;
	public double radSurfHeight = 0.500;

	public Square radSurface = new Square("radSurface", radSurfaceCentre, radSurfaceNormal, radSurfaceUp, radSurfHeight, radSurfWidth, NullInterface.ideal()); 
	
	

}
