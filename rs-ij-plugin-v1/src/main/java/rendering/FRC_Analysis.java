/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import static java.lang.Math.round;
import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import unmixing.Blinking;



/**
 *
 * @author Janel
 */
public class FRC_Analysis {
 
    ImagePlus odd_im;
    ImagePlus even_im;
    
    
    public FRC_Analysis(ArrayList<Blinking> BEs, int mag,double pixelSize){
     
   generateFFTs(BEs,mag,pixelSize);
     
    }
    
    
    private void generateFFTs(ArrayList<Blinking> BEs, int mag, double pixelSize){
        int sz= BEs.size();
        ArrayList<Blinking> evenBEs= new ArrayList<Blinking>();
        ArrayList<Blinking> oddBEs=new ArrayList<Blinking>();
        ArrayList<Double> xpos= new ArrayList<Double>();
        ArrayList<Double> ypos= new ArrayList<Double>();
        ArrayList<Double> unc= new ArrayList<Double>();
        
      
        
        for(int i=0; i<sz;i++){
            Blinking curBE=BEs.get(i);
            int fr =curBE.getFrame();
            double t_xpos =curBE.getXPosition();
            double t_ypos =curBE.getYPosition();
            double t_unc =curBE.getUnc();
            
             
            int rem =fr%2;
            if(rem==0){
                evenBEs.add(curBE);
            
        }else{
                oddBEs.add(curBE);
            }
            
             xpos.add(t_xpos);
            ypos.add(t_ypos);
            unc.add(t_unc);
            
            
            
        }
        
        double[] xps= ArrayUtils.toPrimitive(xpos.toArray(new Double[sz]));
        double[] yps= ArrayUtils.toPrimitive(ypos.toArray(new Double[sz]));   
        double [] uncs= ArrayUtils.toPrimitive(unc.toArray(new Double[sz]));  
       
         Max mx_Val = new Max();
        
            int mxunc=(int) round(mx_Val.evaluate(uncs,0,sz))+1;
          int xmax=(int) round(mx_Val.evaluate(xps,0,sz))+mxunc;
          int ymax=(int) round(mx_Val.evaluate(yps,0,sz))+mxunc;
          
           Min mn_Val = new Min();
                 
          int xmin=(int) round(mn_Val.evaluate(xps,0,sz))-mxunc;
          int ymin=(int) round(mn_Val.evaluate(yps,0,sz))-mxunc;
          
          double conv=pixelSize+0.5;
            IJ.log("pixelsize: "+conv); 
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
          
          int width = wid*mag;
          int height =hei*mag;
          double resolution= conv/mag;
          
          GaussianRendering o_gsr = new GaussianRendering(width,height,resolution);
          o_gsr.renderImage(oddBEs, xmin,ymin);
          odd_im = o_gsr.getImage();
          
          odd_im.show();
          
          GaussianRendering e_gsr = new GaussianRendering(width,height,resolution);
          e_gsr.renderImage(evenBEs,xmin,ymin);
          even_im = e_gsr.getImage();
      
          even_im.show();
        
     
          
         FloatProcessor ipROI1 =(FloatProcessor) odd_im.getProcessor();
         FloatProcessor ipROI2 =(FloatProcessor) even_im.getProcessor();
          
        FRC myFRC = new FRC();
        double res= myFRC.calculateFireNumber(ipROI1, ipROI2, FRC.ThresholdMethod.FIXED_1_OVER_7);
	
    }
 
    

    
    
}
