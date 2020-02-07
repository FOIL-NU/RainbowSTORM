/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;
import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.FloatProcessor;
import static java.lang.Math.ceil;
import java.util.ArrayList;
import unmixing.Blinking;

/**
 *
 * @author Janel Davis
 */
public class GaussianRendering {
    
    double resolution;
    int width;
    int height;
    
    ImagePlus image;
    FloatProcessor imgProc; 
    
    
    public GaussianRendering(int width, int height, double resolution) {
        this.resolution = resolution;
       
        this.width = (int) ((width ));
        this.height = (int) ((height));
        
      
       
        this.imgProc = new FloatProcessor(this.width, this.height);
        
  
    }
    
     
      public FloatProcessor  renderImage(ArrayList<Blinking> BEs,int xmin, int ymin) {
           int sz=BEs.size();
          
           
           for (int i=0;i<sz;i++){
              
              Blinking bEvent= BEs.get(i);
               double dx=bEvent.getUnc();
              
               
              double xpos=(bEvent.getXPosition()-xmin)+1;
              double ypos =(bEvent.getYPosition()-ymin)+1;
              this.drawPoint(xpos,ypos,dx);
               
          }
          
            return imgProc;
      
    }
       
   
    
      protected void drawPoint(double x, double y,  double dx) {
          
    
        int xt1=1;
        int xt2=this.width;
     
            int u = (int) (x / resolution);
            int v = (int) (y  / resolution);
            double radius=  (dx / resolution);
            
             double x_org = x / resolution;
             double y_org = y / resolution;
        
            int actualRadius =(int) (ceil(radius * 3)); //
            double sqrt2dx = Math.sqrt(2) * radius;
            
            boolean flg1=((u-actualRadius)>xt1)&&((u+actualRadius)<xt2);
           
            int yt1 =1;
            int yt2=this.height;
            boolean flg2=((v-actualRadius)>yt1)&&((v+actualRadius)<yt2);
         
        if(flg1&&flg2){
                            
            
                    

                    for(int idx = u - actualRadius; idx <= u + actualRadius; idx++) {
                       
                            double difx = idx - x_org;
                            double xerfdif = (erf((difx) / sqrt2dx) - erf((difx + 1) / sqrt2dx));

                            for(int idy = v - actualRadius; idy <= v + actualRadius; idy++) {
                               
                                    double dify = idy - y_org;

                                    double val =1
                                            * xerfdif
                                            * (erf((dify) / sqrt2dx) - erf((dify + 1) / sqrt2dx));
                                 
                                    imgProc.setf(idx, idy, (float) val + imgProc.getf(idx, idy));
                                 
                              
                            }
                        }
                    
        }
    }
   
    public ImagePlus getImage() {
       
        image = new ImagePlus("Averaged Gaussian Rendering", imgProc);
          
      ContrastEnhancer ce = new ContrastEnhancer();
      ce.stretchHistogram(image, 0.35);
        return image;
    }
    
    
     // fractional error in math formula less than 1.2 * 10 ^ -7.
    // although subject to catastrophic cancellation when z in very close to 0
    // from Chebyshev fitting formula for erf(z) from Numerical Recipes, 6.2
    public double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp(-z * z - 1.26551223
                + t * (1.00002368
                + t * (0.37409196
                + t * (0.09678418
                + t * (-0.18628806
                + t * (0.27886807
                + t * (-1.13520398
                + t * (1.48851587
                + t * (-0.82215223
                + t * (0.17087277))))))))));
        if(z >= 0) {
            return ans;
        } else {
            return -ans;
        }
    }
    
}
