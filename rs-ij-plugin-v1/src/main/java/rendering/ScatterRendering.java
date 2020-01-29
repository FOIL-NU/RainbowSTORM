/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;


import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.FloatProcessor;
import java.util.ArrayList;
import unmixing.Blinking;


/**
 *
 * @author Brian T. Soetikno and Janel Davis
 */
public class ScatterRendering {
    double resolution;
    int width;
    int height;
    
    ImagePlus image;
    FloatProcessor imgProc; 
    
    public ScatterRendering(int width, int height, double resolution) {
        this.resolution = resolution;
        
        this.width = (int) ((width ));//+(2*resolution));
        this.height = (int) ((height));//+(2*resolution));
        
         System.out.println("Blinking Image Width: " + this.width);
        System.out.println("Blinking Image Height: " + this.height);
      
        this.imgProc = new FloatProcessor(this.width, this.height);
        
    }
    
    
      public FloatProcessor  renderImage(ArrayList<Blinking> BEs,int xmin, int ymin) {
           int sz=BEs.size();
         
           for (int i=0;i<sz;i++){
              
              Blinking bEvent= BEs.get(i);
             
              double xpos=(bEvent.getXPosition()-xmin)+1;
              double ypos =(bEvent.getYPosition()-ymin)+1;
             
              this.drawPoint(xpos,ypos);
                 
          }
          
            return imgProc;
      
    }
       
    
    public void drawPoint(double x, double y) {
        int u = (int) (x / this.resolution);
        int v = (int) (y / this.resolution);
        
        int xt1=1;
        int xt2=this.width;
        boolean flg1=(u>xt1)&&(u<xt2);
        int yt1 =1;
        int yt2=this.height;
        boolean flg2=(v>yt1)&&(v<yt2);
        
        if(flg1&&flg2){
        imgProc.setf(u,v,1);
        }

    }
    
    public ImagePlus getImage() {
        image = new ImagePlus("Scatter Rendering", imgProc);
          
      ContrastEnhancer ce = new ContrastEnhancer();
      ce.stretchHistogram(image, 0.35);
        return image;
    }
}
