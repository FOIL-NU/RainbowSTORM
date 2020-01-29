/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;

//Implements ThunderSTORM DriftCorrection

import ij.IJ;
import ij.gui.Plot;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import unmixing.Blinking;

/**
 *
 * @author Janel
 */
public class sSMLMDriftResults {
    
    
    //interpolated drift
    private PolynomialSplineFunction xFunction;
    private PolynomialSplineFunction yFunction;
    //actual estimated drift data
    private double[] driftDataFrame;
    private double[] driftDataX;
    private double[] driftDataY;
    //
    private int minFrame;
    private int maxFrame;
 
    

    public int getBinCount() {
        return driftDataFrame.length;
    }

    public double[] getDriftDataFrame() {
        return driftDataFrame;
    }

    public double[] getDriftDataX() {
        return driftDataX;
    }

    public double[] getDriftDataY() {
        return driftDataY;
    }

    public int getMinFrame() {
        return minFrame;
    }

    public int getMaxFrame() {
        return maxFrame;
    }

    public Point2D.Double getInterpolatedDrift(double frameNumber) {
        return new Point2D.Double(xFunction.value(frameNumber), yFunction.value(frameNumber));
    }

       //Adapted from ThunderSTORM
    public ArrayList<Blinking> applyToResults(sSMLMDriftResults driftCorrection,ArrayList<Blinking> BEs,double pixelSize) {
        IJ.showStatus("Applying drift...");
        int sz=BEs.size();
        double[] vals= new double[2];
      
        ArrayList<Blinking> n_locs= new ArrayList<Blinking>();
        for(int i = 0; i < sz; i++) {
            Blinking loc= BEs.get(i);
            int frameNumber = loc.getFrame();
            double xVal = loc.getXPosition();
            double yVal = loc.getYPosition();
            Point2D.Double drift = driftCorrection.getInterpolatedDrift(frameNumber);
            double conv=pixelSize+0.5;
            double d_x= drift.x*conv;
            double d_y=drift.y*conv;
           
            double x_new=xVal-(d_x);
            double y_new=yVal-(d_y);
            
       
            Blinking d_loc=new Blinking(frameNumber,x_new,y_new,loc.getPhPsf(),loc.getSigmaPsf(),loc.getUnc(),
                    loc.getCentroid(),loc.getPhotons(),loc.getBkPhotons(),loc.getBkPx(),loc.getSigmaSPE(),loc.getUncSPE(),loc.getSpectrum());
            n_locs.add(d_loc);
          
        }
        
        return n_locs;
    }

    
}
