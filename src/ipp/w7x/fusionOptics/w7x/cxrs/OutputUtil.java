package ipp.w7x.fusionOptics.w7x.cxrs;

import java.io.PrintStream;

import algorithmrepository.Algorithms;
import fusionDefs.neutralBeams.SimpleBeamGeometry;
import fusionOptics.Util;
import fusionOptics.drawing.STLDrawer;
import fusionOptics.interfaces.NullInterface;
import fusionOptics.surfaces.Cylinder;
import fusionOptics.types.Optic;
import ipp.w7x.neutralBeams.W7xNBI;
import uk.co.oliford.jolu.OneLiners;

public class OutputUtil {
	
	public ObservationSystem sys;
	public SimpleBeamGeometry beams;
	public String outPath;
	
	public double losCyldRadius = 1.0;
	
	public OutputUtil(ObservationSystem sys, SimpleBeamGeometry beams, String outPath) {
		this.sys = sys;
		this.beams = beams;
		this.outPath = outPath;
	}

	public static enum Thing { 
		FreeCADWallHit, //FreeCAD python to make wall hit position (BackgroundTargetting)
		FreeCADApproach, //FreeCAD python to make sphere at closest approach to beam
		FreeCADBeamPlane, //FreeCAD python to make sphere at contact with beam plane
		FreeCADLOS,  //FreeCAD python to make cylinder of LOS
		JSON_LOS,   //JSON LOS definition (without end)
		TXT_LOS_MM  //simple text LOS
	};
	
	public void outputInfo(PrintStream stream, double startPoints[][][], double hitPoints[][][], double beamPlanePos[][][], int iB, int iP, Thing thing){
		boolean isLast = (iB == sys.channelR().length-1) && (iP == sys.channelR()[iB].length-1);

		double extendLOSCylds = 1.000; // extend 200mm in each direction

		//double rad = hitPoints[iB][iP][3];
		double rad = losCyldRadius;
		double uVec[];
		double losLen;
		
		//use beam plane pos for LOS direction if not null, other hit positions
		if(beamPlanePos != null) {
			uVec = Util.reNorm(Util.minus(beamPlanePos[iB][iP], startPoints[iB][iP]));
			losLen = Util.length(Util.minus(beamPlanePos[iB][iP], startPoints[iB][iP]));
		}else {
			uVec = Util.reNorm(Util.minus(hitPoints[iB][iP], startPoints[iB][iP]));
			losLen = Util.length(Util.minus(hitPoints[iB][iP], startPoints[iB][iP]));
			
		}
		
		int approaches[];
		if(beams instanceof W7xNBI) {
			approaches = new int[] { 2, 3, 6, 7 };			
		}else {
			approaches = new int[] { 0 };
		}
		
		//point on ray closest to beam axes
		double approach[][] = new double[8][];
		for(int jB : approaches){			
			
			//double beamStart[] = beams.start(sys.beamIdx[iB]);
			//double beamVec[] =  beams.uVec(sys.beamIdx[iB]);
			double beamStart[] = beams.start(jB);
			double beamVec[] =  beams.uVec(jB);
			
			double aL = Algorithms.pointOnLineNearestAnotherLine(startPoints[iB][iP], uVec, beamStart, beamVec);
			if(aL > 0 && aL < 10) {
				approach[jB] = OneLiners.plus(startPoints[iB][iP], OneLiners.mul(uVec, aL));
			}else {
				approach[jB] = null;
			}
		}
		
		//double start[] = sys.lens1.getBackSurface().getCentre();
		
		String chanName = sys.getChanName(iB, iP);
		
		double p[] = Util.minus(startPoints[iB][iP], Util.mul(uVec, extendLOSCylds/2));
		switch(thing){
			case FreeCADWallHit:
				stream.println(freecadMakeSphere("bgHit_Q"+sys.beamIdx()[iB]+"_"+sys.getDesignName()+"_"+chanName, hitPoints[iB][iP], rad));				
				break;
				
			case FreeCADApproach:		
				stream.println(freecadMakeSphere("beamApproach_Q"+sys.beamIdx()[iB]+"_"+sys.getDesignName()+"_"+chanName, hitPoints[iB][iP], rad));
				
				break;
		
			case FreeCADBeamPlane:
				if(beamPlanePos != null)					
					stream.println(freecadMakeSphere("beamPlane_"+sys.beamIdx()[iB]+"_"+sys.getDesignName()+"_"+chanName, beamPlanePos[iB][iP], rad));				
				break;
		
			case FreeCADLOS:
				stream.println(freecadMakeCylinder("los_"+sys.getDesignName()+"_"+chanName, p, uVec, rad, (losLen + extendLOSCylds)));		
				break;
				
			case JSON_LOS:
				stream.print("{ \"id\" : \"" + chanName
						+ "\", \"start\":[ " + String.format("%7.5g", startPoints[iB][iP][0]) + ", " + String.format("%7.5g", startPoints[iB][iP][1]) + ", " + String.format("%7.5g", startPoints[iB][iP][2]) + "]"
						+ ", \"uVec\":[ " + String.format("%7.5g", uVec[0]) + ", " + String.format("%7.5g", uVec[1]) + ", " + String.format("%7.5g", uVec[2]) + "]");
				for(int jB=0; jB < approach.length; jB++){
					if(approach[jB] != null)
						stream.print(", \"approachQ"+(jB+1)+"\":[ " + String.format("%7.5g", approach[jB][0]) + ", " + String.format("%7.5g", approach[jB][1]) + ", " + String.format("%7.5g", approach[jB][2]) + "]");
				}
				
				if(beamPlanePos != null) {
					stream.println(", \"beamPlaneHit\":[ "+ String.format("%7.5g", beamPlanePos[iB][iP][0]) 
										+ ", " + String.format("%7.5g", beamPlanePos[iB][iP][1]) 
										+ ", " + String.format("%7.5g", beamPlanePos[iB][iP][2]) + "]");
				}
				
				if(hitPoints != null) {
					stream.println(", \"bgHit\":[ "+ String.format("%7.5g", hitPoints[iB][iP][0]) 
										+ ", " + String.format("%7.5g", hitPoints[iB][iP][1]) 
										+ ", " + String.format("%7.5g", hitPoints[iB][iP][2]) + "]");
				}
				
				stream.println("}" + (isLast ? "" : ", ")
						);
				break;
				
			case TXT_LOS_MM:
				stream.println(String.format("%7.3f", startPoints[iB][iP][0]*1e3) + " " + String.format("%7.3f", startPoints[iB][iP][1]*1e3) + " " + String.format("%7.3f", startPoints[iB][iP][2]*1e3) + " "
							+ String.format("%7.3f", hitPoints[iB][iP][0]*1e3) + " " + String.format("%7.3f", hitPoints[iB][iP][1]*1e3) + " " + String.format("%7.3f", hitPoints[iB][iP][2]*1e3));
						
				break;
		}
	}
	
	public String freecadMakeCylinder(String name, double[] pos, double[] unitVec, double radius, double length) {
		return "Part.show(Part.makeCylinder("+radius*1e3+","+length*1e3 +","										
				+ "FreeCAD.Vector("+pos[0]*1e3+","+pos[1]*1e3+","+pos[2]*1e3+"), "
				+ "FreeCAD.Vector("+unitVec[0]*1e3+","+unitVec[1]*1e3+","+unitVec[2]*1e3+ ")));"
				+ "FreeCAD.ActiveDocument.ActiveObject.Label=\""+name+"\"; "
				+ "g.addObject(FreeCAD.ActiveDocument.ActiveObject);";
	}

	public String freecadMakeSphere(String name, double[] pos, double radius) {
		return "Part.show(Part.makeSphere("+radius*1e3+",FreeCAD.Vector("+pos[0]*1e3+","+pos[1]*1e3+","+pos[2]*1e3 + ")));"
				+ " FreeCAD.ActiveDocument.ActiveObject.Label=\""+name+"\"; g.addObject(FreeCAD.ActiveDocument.ActiveObject);";
	}
	
	public void makeFibreCyldSTL() {
		Optic fibreCylds = new Optic("fibreCylds");
		double l = 0.050;
		for(int iB = 0; iB < sys.channelR().length; iB++){
			for(int iF=0; iF < sys.channelR()[iB].length; iF++){

				double c[] = Util.plus(sys.fibreEndPos()[iB][iF], Util.mul(sys.fibreEndNorm()[iB][iF], -l/2));
				fibreCylds.addElement(new Cylinder("fibre_"+iB+"_"+iF, c, sys.fibreEndNorm()[iB][iF], sys.getFibreDiameter(iB, iF)/2, l, NullInterface.ideal()));
			}
		}
		
		STLDrawer stlDrawer = new STLDrawer(outPath + "/fibreCylds-"+sys.getDesignName()+".stl");		
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
		stlDrawer.drawOptic(fibreCylds);
		stlDrawer.destroy();

		/*stlDrawer = new STLDrawer(outPath + "/lens1-"+sys.getDesignName()+".stl");		
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
		stlDrawer.drawOptic(sys.lens1);
		stlDrawer.destroy();*/

		//stlDrawer = new STLDrawer(outPath + "/lens2-"+sys.getDesignName()+".stl");		
		//stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});	
		//stlDrawer.drawOptic(sys.lens2);
		//stlDrawer.destroy();
		
		stlDrawer = new STLDrawer(outPath + "/rodCyld-"+sys.getDesignName()+".stl");		
		stlDrawer.setTransformationMatrix(new double[][]{ {1000,0,0},{0,1000,0},{0,0,1000}});
		//if(sys.rod != null)
		//	stlDrawer.drawOptic(new Optic("rodOptic", new Element[]{ sys.rod }));
		stlDrawer.destroy();
	}
}
