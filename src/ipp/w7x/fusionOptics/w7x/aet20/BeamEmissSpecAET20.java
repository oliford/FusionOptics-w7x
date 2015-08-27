package ipp.w7x.fusionOptics.w7x.aet20;

import jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET20 looking at AEK20 beams */
public class BeamEmissSpecAET20 extends Optic {
	
	public double globalUp[] = {0,0,1};
	//roughly
	
	public double shutterPivotCentre[] = { -0.913686, 6.20968, -0.131649 };
	
	public double shutterPoints[][] = {
		{-0.995693, 6.16063, -0.1641200},
		{-0.820756, 6.22351, -0.1766120},
		{-0.940116, 6.20814, -0.0385021},
	};
	public double shutterDiameter = 0.200;
	
	public double shutterNormal[] = Util.reNorm(Util.cross(Util.minus(shutterPoints[0], shutterPoints[1]), Util.minus(shutterPoints[2], shutterPoints[1])));
	
	public Disc shutter = new Disc("shutterDisc", shutterPivotCentre, shutterNormal, shutterDiameter/2, NullInterface.ideal());
	
	public double basicObsPoint[] = new double[]{ -0.860258, 6.22327, -0.161877 };
	
	
	public double shutterRight[] = Util.reNorm(Util.cross(shutterNormal, globalUp));
	public double shutterUp[] = Util.reNorm(Util.cross(shutterRight, shutterNormal));
	
	public double entryWindowRadiusOnShutter = 0.060;
	public double entryWindowDiameter = 0.060;
	public double entryWindowAngularPosition = 120 * Math.PI / 180;
	
	public double entryWindowPos[] = Util.plus(shutterPivotCentre, 
										Util.plus( Util.mul(shutterUp, entryWindowRadiusOnShutter * FastMath.cos(entryWindowAngularPosition)),
												   Util.mul(shutterRight, entryWindowRadiusOnShutter * FastMath.sin(entryWindowAngularPosition)) ));
	
	public Disc entryWindow = new Disc("entryWindow", entryWindowPos, shutterNormal, entryWindowDiameter/2, null, null, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowPos, shutterNormal, 0.080, entryWindowDiameter/2, null, null, NullInterface.ideal());
		
	public BeamEmissSpecAET20() {
		super("beamSpec-aet20");
		addElement(shutter);
		addElement(entryWindow);
		addElement(entryWindowIris);
		
	}
	
	

}
