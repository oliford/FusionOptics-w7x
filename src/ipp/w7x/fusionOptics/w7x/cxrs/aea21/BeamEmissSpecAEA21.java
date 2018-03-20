package ipp.w7x.fusionOptics.w7x.cxrs.aea21;

import ipp.w7x.neutralBeams.W7xNBI;
import oneLiners.OneLiners;
import algorithmrepository.Algorithms;
import jafama.FastMath;
import fusionOptics.Util;
import fusionOptics.interfaces.Absorber;
import fusionOptics.interfaces.IsoIsoInterface;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.interfaces.Reflector;
import fusionOptics.lenses.EdmundOptics50mmAspheric;
import fusionOptics.lenses.ThorLabs100mmAspheric;
import fusionOptics.materials.BK7;
import fusionOptics.materials.Sapphire;
import fusionOptics.optics.STLMesh;
import fusionOptics.optics.SimplePlanarConvexLens;
import fusionOptics.surfaces.Disc;
import fusionOptics.surfaces.Iris;
import fusionOptics.surfaces.Square;
import fusionOptics.types.Element;
import fusionOptics.types.Medium;
import fusionOptics.types.Optic;

/** Beam Emission Spectroscopy / CXRS on AET21 looking at AEK21 beams */
public class BeamEmissSpecAEA21 extends Optic {
	
	public String lightPathsSystemName = "AEA21_???";
	
	public double globalUp[] = {0,0,1};
	public double designWavelenth = 500e-9; // [ He_II @468.58 and/or C_VI @529.06, average is pretty much 500nm ]
	
	public double portNormal[] = Util.reNorm(new double[]{ 0.7725425415039062, 2.3776411743164063, 1.52587890625e-08 });	// roughly , from CAD
	public double portEntryPos[] = new double[] { 2.0940919189453124, 6.0920563964843755, 0.3777649383544922 };  //point roughly in middle of end of immersion tube
	
	public double virtualObsPos[] = { -0.9556435146926046,	6.200182024233047,	-0.16812844867308413 }; //closest approach of all LOSs, from lightAssesment (not yet known)
	
	/***** Observation target ****/
	//public int targetBeamIdx = 6; // 6 = Q7 = K21 lower radial   
	public double targetBeamR = 5.65;
	//public double targetObsPos[] = W7xNBI.def().getPosOfBeamAxisAtR(targetBeamIdx, targetBeamR);
	public double targetObsPos[] = W7xNBI.def().getPosOfBoxAxisAtR(1, targetBeamR);
	public double sourceNormal[] =  Util.reNorm(Util.minus(targetObsPos, portEntryPos));
	
	//directions perp to port, sidesways and toward/away from source 
	public double portSourcePerp[] = Util.reNorm(Util.cross(sourceNormal, portNormal));
	public double portSourcePlane[] = Util.reNorm(Util.cross(portNormal, portSourcePerp));
	
	public double opticsTiltInPortSideways = 0 * Math.PI / 180;
	public double opticsTiltInPortToSource = 0 * Math.PI / 180; //+ve is away from source
	public double opticAxisA[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePerp, opticsTiltInPortToSource), portNormal));
	public double opticAxis[] = Util.reNorm(Algorithms.matrixMul(Algorithms.rotationMatrix(portSourcePlane, opticsTiltInPortSideways), opticAxisA));
	
	/**** Mirror *****/
	public double mirrorDistIntoPort = 0.010;
	public double mirrorDistAwayFromSource = 0.000; //~toroidally in vessel
	public double mirrorDistSidewaysInPort = 0.000; //up/down in vessel
	public double mirrorPos0[] = Util.plus(portEntryPos, Util.mul(opticAxis, mirrorDistIntoPort)); 
	public double mirrorPos[] = Util.plus(Util.plus(mirrorPos0, 
													Util.mul(portSourcePlane, -mirrorDistAwayFromSource)),
													Util.mul(portSourcePerp, mirrorDistSidewaysInPort));
	
	public double mirrorRotationInPlane = 0 * Math.PI / 180;
	public double mirrorWidth = 0.130; // [Made up ]
	public double mirrorHeight = 0.070;
					
	public double mirrorNormal[] = Util.reNorm(Util.plus(sourceNormal, opticAxis));

	public double mirrorA[] = Util.reNorm(Util.cross(sourceNormal, opticAxis));
	public double mirrorB[] =  Util.reNorm(Util.cross(mirrorA, mirrorNormal));
	public double mirrorX[] = Util.reNorm(Util.plus(Util.mul(mirrorA, FastMath.cos(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.sin(mirrorRotationInPlane))));
	public double mirrorY[] = Util.reNorm(Util.plus(Util.mul(mirrorA, -FastMath.sin(mirrorRotationInPlane)), Util.mul(mirrorB, FastMath.cos(mirrorRotationInPlane))));
	
	public Square mirror = new Square("mirror", mirrorPos, mirrorNormal, mirrorX, mirrorHeight, mirrorWidth, Reflector.ideal());
	
	public final String backgroundSTLFiles[] = {
			"/work/ipp/w7x/cad/aea21/bg-targetting/baffle-m3.off-aea21-cut.stl",
			"/work/ipp/w7x/cad/aea21/bg-targetting/panel-m21.off-aea21-cut.stl",
			"/work/ipp/w7x/cad/aea21/bg-targetting/panel-m30.off-aea21-cut.stl",
			"/work/ipp/w7x/cad/aea21/bg-targetting/shield-m3.off-aea21-cut.stl",
	};
		
	//public NodesAndElementsMesh shieldTiles = new2 NodesAndElementsMesh("shield", "/work/ipp/w7x/cad/shield-m2", mirrorPos, 0.150);
	
	/***** Entry Window *****/
	public double windowDistBehindMirror = 0.068;
	public double entryWindowDiameter = 0.068; // DN100CF=98mm, DN63CF=68mm
	public double entryWindowThickness = 0.003; // [Made up]
	
	public double entryWindowFrontPos[] = Util.plus(mirrorPos, Util.mul(opticAxis, windowDistBehindMirror));
	public double entryWindowIrisPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness / 2));
	private double entryWindowBackPos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, entryWindowThickness));
	
	Medium windowMedium = new Medium(new Sapphire());
	public Disc entryWindowFront = new Disc("entryWindowFront", entryWindowFrontPos, opticAxis, entryWindowDiameter/2, windowMedium, null, NullInterface.ideal());
	public Disc entryWindowBack = new Disc("entryWindowBack", entryWindowBackPos, opticAxis, entryWindowDiameter/2, null, windowMedium, NullInterface.ideal());
	public Iris entryWindowIris = new Iris("entryWindowIris", entryWindowIrisPos, opticAxis, entryWindowDiameter*2, entryWindowDiameter*0.49, null, null, Absorber.ideal());
	
	/**** Main Lens *****/
	public double lens1DistBehindWindow = 0.062;
	
	/* ** Thor Labs, same as AEM21 ** */
	/*public double lens1Diameter = 0.095 + 0.001;	
	public double lens1CentreThickness = 0.00874;
	public double lens1FocalLength = 0.200;
	//public double lens1CurvatureRadius = 0.10336;
	public double lens1ClearAperture = 0.0735;
	*/
	
	/* ** OptoSigma SLB-80-200PM ** */
	/*public double lens1Diameter = 0.080 + 0.001;	
	public double lens1CentreThickness = 0.01100;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1038;
	public double lens1ClearAperture = 0.079;
	*/
	
	/* Edmund Optics modified 27-501 */
	/*public double lens1Diameter = 0.090 + 0.001;	
	public double lens1CentreThickness = 0.01700;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1035;
	public double lens1ClearAperture = 0.089;
	*/
	
	/* Edmund Optics modified 27-501 */
	public double lens1Diameter = 0.080 + 0.001;	
	public double lens1CentreThickness = 0.01100;
	//public double lens1FocalLength = 0.200;
	public double lens1CurvatureRadius = 0.1038;
	public double lens1ClearAperture = 0.079;
	
	public double lens1CentrePos[] = Util.plus(entryWindowFrontPos, Util.mul(opticAxis, lens1DistBehindWindow));
	
	
	//public double focalLength = 0.200; // Would be better, NA~0.33, much better focus

	public Medium lensMedium = new Medium(new BK7());  
	public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	//public SimplePlanarConvexLens lens1 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens1",
											lens1CentrePos,
											opticAxis,
											lens1Diameter/2, // radius
											lens1CurvatureRadius, // rad curv
											//lens1FocalLength, // rad curv
											lens1CentreThickness,  
											lensMedium, 
											IsoIsoInterface.ideal());//,
											//designWavelenth);
	
	//public ThorLabs100mmAspheric lens1 = new ThorLabs100mmAspheric(lensCentrePos, opticAxis, 2.0);
	
	
	
	public double lensIrisPos[] = Util.plus(lens1CentrePos, Util.mul(opticAxis, -0.002));
	public Iris lensIris = new Iris("lensIris", lensIrisPos, opticAxis, lens1Diameter, lens1ClearAperture*0.48, null, null, Absorber.ideal());
	
	
	/**** Lens2 *****/
	public double lens2DistBehindLens1 = 0.060;
	
	/*public double lens2Diameter = 0.095 + 0.001;
	public double lens2CentreThickness = 0.00874;
	public double lens2FocalLength = 0.200;
	//public double lens2CurvatureRadius = 0.10336;
	public double lens2ClearAperture = 0.0735;
	*/
	

	/** As lens1 */
	public double lens2Diameter = lens1Diameter;	
	public double lens2CentreThickness = lens1CentreThickness;
	//public double lens2FocalLength = lens1FocalLength;
	public double lens2CurvatureRadius = lens1CurvatureRadius;
	public double lens2ClearAperture = lens1ClearAperture;
	
	
	
	public double lens2CentrePos[] = Util.plus(lens1CentrePos, Util.mul(opticAxis, lens2DistBehindLens1));
	
	public double focalLength2 = 0.200; // Would be better, NA~0.33, much better focus
	
	public Medium lens2Medium = new Medium(new BK7());
	public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromRadiusOfCurvAndCentreThickness(
	//public SimplePlanarConvexLens lens2 = SimplePlanarConvexLens.fromFocalLengthAndCentreThickness(
											"lens2",
											lens2CentrePos,
											opticAxis,
											lens2Diameter/2, // radius
											lens2CurvatureRadius, // rad curv
											//lens2FocalLength, // rad curv
											lens2CentreThickness,  
											lens2Medium, 
											IsoIsoInterface.ideal());//,
											//designWavelenth);
	
	//public ThorLabs100mmAspheric lens2 = new ThorLabs100mmAspheric(lens2CentrePos, opticAxis, 2.0);
	
	public double lens2IrisPos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, -0.002));
	public Iris lens2Iris = new Iris("lens2Iris", lens2IrisPos, opticAxis, lens2Diameter, lens2Diameter*0.48, null, null, Absorber.ideal());
	

	/*** Fibres ****/
	public int beamIdx[] = {  W7xNBI.BEAM_Q8 ,  W7xNBI.BEAM_Q8 ,  W7xNBI.BEAM_Q8  };
	
	public double fibre1EndPos[] = { -2.89649, -4.54998, 1.44268 }; // core channel, [fromDesigner-20151106] 
	public double fibre10EndPos[] = { -2.85912, -4.50896, 1.43616 }; // edge channel,  [fromDesigner-20151106]
	

	public double[][] channelR = { 
		//	{ 5.25, 5.258, 5.265, 5.273, 5.28, 5.288, 5.296, 5.303, 5.311, 5.319, 5.326, 5.334, 5.341, 5.349, 5.357, 5.364, 5.372, 5.38, 5.387, 5.395, 5.402, 5.41, 5.418, 5.425, 5.433, 5.44, 5.448, 5.456, 5.463, 5.471, 5.479, 5.486, 5.494, 5.501, 5.509, 5.517, 5.524, 5.532, 5.54, 5.547, 5.555, 5.562, 5.57, 5.578, 5.585, 5.593, 5.6, 5.608, 5.616, 5.623, 5.631, 5.639, 5.646, 5.654, 5.661, 5.669, 5.677, 5.684, 5.692, 5.7, 5.707, 5.715, 5.722, 5.73, 5.738, 5.745, 5.753, 5.76, 5.768, 5.776, 5.783, 5.791, 5.799, 5.806, 5.814, 5.821, 5.829, 5.837, 5.844, 5.852, 5.86, 5.867, 5.875, 5.882, 5.89, 5.898, 5.905, 5.913, 5.92, 5.928, 5.936, 5.943, 5.951, 5.959, 5.966, 5.974, 5.981, 5.989, 5.997, 6.004, 6.012, 6.02, 6.027, 6.035, 6.042, 6.05, },
			{ 5.240, 5.250, 5.261, 5.271, 5.281, 5.291, 5.302, 5.312, 5.322, 5.332, 5.343, 5.353, 5.363, 5.373, 5.384, 5.394, 5.404, 5.414, 5.425, 5.435, 5.445, 5.455, 5.466, 5.476, 5.486, 5.496, 5.507, 5.517, 5.527, 5.537, 5.548, 5.558, 5.568, 5.578, 5.589, 5.599, 5.609, 5.619, 5.630, 5.640, 5.650, 5.660, 5.671, 5.681, 5.691, 5.701, 5.712, 5.722, 5.732, 5.742, 5.753, 5.763, 5.773, 5.783, 5.794, 5.804, 5.814, 5.824, 5.835, 5.845, 5.855, 5.865, 5.876, 5.886, 5.896, 5.906, 5.917, 5.927, 5.937, 5.947, 5.958, 5.968, 5.978, 5.988, 5.999, 6.009, 6.019, 6.029, 6.040, 6.050 },
			{ -3, -2, -1, 1, 2, 3 },
			{ -3, -2, -1, 1, 2, 3 },
		}; 
		public double[][][] fibreEndPos = { { 
			{ 2.1587452, 6.3614656, 0.3778486 },
			{ 2.1593456, 6.3614237, 0.3770866 },
			{ 2.1597394, 6.3614451, 0.3779734 },
			{ 2.1603375, 6.3613963, 0.3772114 },
			{ 2.1607290, 6.3614108, 0.3780981 },
			{ 2.1613249, 6.3613551, 0.3773362 },
			{ 2.1617142, 6.3613627, 0.3782229 },
			{ 2.1623079, 6.3613000, 0.3774609 },
			{ 2.1626949, 6.3613007, 0.3783477 },
			{ 2.1632863, 6.3612312, 0.3775857 },
			{ 2.1636711, 6.3612250, 0.3784724 },
			{ 2.1642603, 6.3611485, 0.3777105 },
			{ 2.1646428, 6.3611354, 0.3785972 },
			{ 2.1652298, 6.3610520, 0.3778352 },
			{ 2.1656101, 6.3610320, 0.3787219 },
			{ 2.1661948, 6.3609418, 0.3779600 },
			{ 2.1665728, 6.3609148, 0.3788467 },
			{ 2.1671553, 6.3608177, 0.3780848 },
			{ 2.1675311, 6.3607838, 0.3789715 },
			{ 2.1681113, 6.3606798, 0.3782095 },
			{ 2.1684849, 6.3606390, 0.3790962 },
			{ 2.1690628, 6.3605281, 0.3783343 },
			{ 2.1694341, 6.3604804, 0.3792210 },
			{ 2.1700099, 6.3603625, 0.3784591 },
			{ 2.1703789, 6.3603080, 0.3793458 },
			{ 2.1709524, 6.3601832, 0.3785838 },
			{ 2.1713192, 6.3601218, 0.3794705 },
			{ 2.1718905, 6.3599901, 0.3787086 },
			{ 2.1722551, 6.3599217, 0.3795953 },
			{ 2.1728241, 6.3597831, 0.3788334 },
			{ 2.1731864, 6.3597079, 0.3797201 },
			{ 2.1737532, 6.3595624, 0.3789581 },
			{ 2.1741133, 6.3594802, 0.3798448 },
			{ 2.1746778, 6.3593278, 0.3790829 },
			{ 2.1750356, 6.3592387, 0.3799696 },
			{ 2.1755979, 6.3590794, 0.3792076 },
			{ 2.1759535, 6.3589834, 0.3800944 },
			{ 2.1765135, 6.3588172, 0.3793324 },
			{ 2.1768669, 6.3587144, 0.3802191 },
			{ 2.1774247, 6.3585412, 0.3794572 },
			{ 2.1777758, 6.3584315, 0.3803439 },
			{ 2.1783313, 6.3582514, 0.3795819 },
			{ 2.1786802, 6.3581347, 0.3804687 },
			{ 2.1792335, 6.3579478, 0.3797067 },
			{ 2.1795801, 6.3578242, 0.3805934 },
			{ 2.1801312, 6.3576304, 0.3798315 },
			{ 2.1804756, 6.3574999, 0.3807182 },
			{ 2.1810244, 6.3572992, 0.3799562 },
			{ 2.1813665, 6.3571617, 0.3808430 },
			{ 2.1819131, 6.3569541, 0.3800810 },
			{ 2.1822530, 6.3568098, 0.3809677 },
			{ 2.1827973, 6.3565952, 0.3802058 },
			{ 2.1831350, 6.3564440, 0.3810925 },
			{ 2.1836771, 6.3562226, 0.3803305 },
			{ 2.1840125, 6.3560645, 0.3812172 },
			{ 2.1845523, 6.3558361, 0.3804553 },
			{ 2.1848855, 6.3556711, 0.3813420 },
			{ 2.1854231, 6.3554358, 0.3805801 },
			{ 2.1857540, 6.3552639, 0.3814668 },
			{ 2.1862893, 6.3550217, 0.3807048 },
			{ 2.1866180, 6.3548429, 0.3815915 },
			{ 2.1871511, 6.3545938, 0.3808296 },
			{ 2.1874776, 6.3544081, 0.3817163 },
			{ 2.1880084, 6.3541521, 0.3809544 },
			{ 2.1883326, 6.3539595, 0.3818411 },
			{ 2.1888612, 6.3536966, 0.3810791 },
			{ 2.1891832, 6.3534970, 0.3819658 },
			{ 2.1897096, 6.3532272, 0.3812039 },
			{ 2.1900293, 6.3530208, 0.3820906 },
			{ 2.1905534, 6.3527441, 0.3813287 },
			{ 2.1908709, 6.3525307, 0.3822154 },
			{ 2.1913928, 6.3522471, 0.3814534 },
			{ 2.1917080, 6.3520269, 0.3823401 },
			{ 2.1922276, 6.3517364, 0.3815782 },
			{ 2.1925406, 6.3515092, 0.3824649 },
			{ 2.1930580, 6.3512118, 0.3817029 },
			{ 2.1933687, 6.3509777, 0.3825897 },
			{ 2.1938839, 6.3506734, 0.3818277 },
			{ 2.1941924, 6.3504324, 0.3827144 },
			{ 2.1947053, 6.3501212, 0.3819525 },
				
				},{	
					{ 2.1657104, 6.3615405, 0.3843393 },
					{ 2.1666352, 6.3612400, 0.3825916 },
					{ 2.1675600, 6.3609395, 0.3808439 },
					{ 2.1694097, 6.3603385, 0.3773486 },
					{ 2.1703345, 6.3600381, 0.3756009 },
					{ 2.1712593, 6.3597376, 0.3738532 },
					
				},{		
					{ 2.1785921, 6.3580632, 0.3860860 },
					{ 2.1795169, 6.3577627, 0.3843383 },
					{ 2.1804417, 6.3574622, 0.3825906 },
					{ 2.1822914, 6.3568613, 0.3790953 },
					{ 2.1832162, 6.3565608, 0.3773476 },
					{ 2.1841410, 6.3562603, 0.3755999 },
				}	}; 
		
		public double[][][] fibreEndNorm = { { 
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
				{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
		},{
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
		},{
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
			{ -0.3090170281652762, -0.9510565053160096, -6.10351585339942E-9 },
		}};

	public double[] channelZ;
	
	//public double fibreEndPos[][];
	public double fibreNA = 0.22; // [ written on the fibre bundle packing reel ]
	
	public double fibreEndDiameter = 0.0004; // 400um AUG-like fibres
	public double fibrePlaneBehindLens2 = 0.100;
	
	//public double fibresXVec[] = Util.reNorm(Util.minus(fibre10EndPos, fibre1EndPos));
	//public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, portNormal));
	//public double fibrePlanePos[] = Util.mul(Util.plus(fibre1EndPos, fibre10EndPos), 0.5);
	
	public double beamAxis[] = W7xNBI.def().uVec(0);
	
	public double fibrePlanePos[] = Util.plus(lens2CentrePos, Util.mul(opticAxis, fibrePlaneBehindLens2)); 
	public double fibresXVec[] = Util.reNorm(Util.cross(Util.cross(beamAxis, opticAxis),opticAxis));
	public double fibresYVec[] = Util.reNorm(Util.cross(fibresXVec, opticAxis));	
	
	public Square fibrePlane = new Square("fibrePlane", fibrePlanePos, opticAxis, fibresYVec, 0.100, 0.100, NullInterface.ideal());
	public Square fibrePlanes[][];
	
	public Square catchPlane = new Square("catchPlane", Util.plus(fibrePlanePos, Util.mul(opticAxis, 0.050)), 
										opticAxis, fibresYVec, 0.300, 0.300, Absorber.ideal());

	public Disc strayPlane = new Disc("strayPlane", Util.plus(entryWindowFrontPos, Util.mul(portNormal, -0.300)),
									portNormal, 0.200, Absorber.ideal());


	public double beamObsPerp[] = Util.reNorm(Util.cross(Util.minus(lens1CentrePos, targetObsPos), beamAxis));
	public double beamObsPlaneNormal[] = Util.reNorm(Util.cross(beamAxis, beamObsPerp));
	
	public Square beamPlane = new Square("beamPlane", targetObsPos, beamObsPlaneNormal, beamObsPerp, 0.500, 1.600, NullInterface.ideal());

	public Element tracingTarget = mirror;
		
	public BeamEmissSpecAEA21() {
		super("beamSpec-aea21");
		
		//addElement(new STLMesh("panel", "/work/ipp/w7x/cad/aea21/panel-cutting-aea21-edge-channels-cut-front.stl", portEntryPos, 0.500));
		
		addElement(mirror);
		addElement(entryWindowIris);
		addElement(entryWindowFront);
		addElement(entryWindowBack);
		addElement(lensIris);
		addElement(lens1);
		addElement(lens2Iris);
		addElement(lens2);
		addElement(fibrePlane);
		addElement(beamPlane);
		//addElement(shieldTiles);
		
		setupFibrePlanes();
		
		/*
		fibreEndPos = new double[nFibres][];
		fibrePlanes = new Square[nFibres];
		double dp[] = Util.mul(Util.minus(fibre10EndPos, fibre1EndPos), 1.0 / (nFibres - 1));
		for(int i=0; i < nFibres; i++){
			
			fibreEndPos[i] = Util.plus(fibre1EndPos, Util.mul(dp, i));
			
			double losVec[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));			
			fibreEndPos[i] = Util.plus(fibreEndPos[i], 
									Util.mul(losVec, shift[i]*1e-3));
			
			double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
			double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
			double y[] = Util.reNorm(Util.cross(x, norm));
			fibrePlanes[i] = new Square("fibrePlane_" + i, fibreEndPos[i], norm, y, 0.007, 0.007, NullInterface.ideal());
			addElement(fibrePlanes[i]);
		}
		*/
		/*fibrePlanes = new Square[nFibres];
		for(int i=0; i < nFibres; i++){
			//double norm[] = Util.reNorm(Util.minus(lensCentrePos, fibreEndPos[i]));
			double norm[] = fibreEndNorm[i];
			double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
			double y[] = Util.reNorm(Util.cross(x, norm));
			fibrePlanes[i] = new Square("fibrePlane_" + i, fibreEndPos[i], norm, y, 0.007, 0.007, NullInterface.ideal());
			addElement(fibrePlanes[i]);
		}*/
			
		addElement(catchPlane);
		addElement(strayPlane);
		
		channelZ = new double[ channelR.length];
		for(int i=0; i < channelR.length; i++){
			channelZ[i] = W7xNBI.def().getPosOfBoxAxisAtR(0, channelR[0][i])[2];
		}
		
		System.out.print("Window centre posXYZ = "); OneLiners.dumpArray(entryWindowFront.getCentre());		
	}

	public String getDesignName() { return "aea21";	}

	public Element[] makeSimpleModel() {
		return new Element[0];
	}
	
	private void setupFibrePlanes() {
		int nBeams = channelR.length;
		fibrePlanes = new Square[nBeams][];
		
		for(int iB=0; iB < nBeams; iB++){
			int nFibres = channelR[iB].length;
			fibrePlanes[iB] = new Square[nFibres];
		
			for(int iF=0; iF < nFibres; iF++){
	
				double norm[] = fibreEndNorm[iB][iF];
				double x[] = Util.reNorm(Util.cross(norm, fibresYVec));
				double y[] = Util.reNorm(Util.cross(x, norm));
				fibrePlanes[iB][iF] = new Square("fibrePlane_Q" + (iB+1) + "_" + iF, fibreEndPos[iB][iF].clone(), norm, y, 0.007, 0.007, NullInterface.ideal());
				//addElement(fibrePlanes[i]);
			}
		}
	}
		

}
