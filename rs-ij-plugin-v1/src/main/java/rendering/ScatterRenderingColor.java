/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;


import ij.ImagePlus;
import ij.process.ColorProcessor;
import java.awt.Color;
import java.util.ArrayList;
import unmixing.Blinking;

/**
 *
 * @author Janel Davis
 */
public class ScatterRenderingColor {
    
    double resolution;
    int width;
    int height;
    
    ImagePlus image;
    ColorProcessor imgProc; 
    
    public ScatterRenderingColor(int width, int height, double resolution) {
        this.resolution = resolution;
        this.width = (int) ((width )+(2*resolution));
        this.height = (int) ((height)+(2*resolution));
       
        this.imgProc = new ColorProcessor(this.width, this.height);
        
    }
    
    public void drawPoint(double x, double y,int color) {
        int u = (int) (x / this.resolution);
        int v = (int) (y / this.resolution);
        
        imgProc.setf(u,v,color);
    }
    
       public ColorProcessor  renderColorImage(ArrayList<Blinking> BEs,int col,int xmin, int ymin) {
           int sz=BEs.size();
           for (int i=0;i<sz;i++){
              
              Blinking bEvent= BEs.get(i);
             
              double xpos=bEvent.getXPosition()-xmin;
              double ypos =bEvent.getYPosition()-ymin;
              
              this.drawPoint(xpos,ypos,col);
             }
          
            return imgProc;
      
    }
       
       
       
    
    public ImagePlus getImage() {
        image = new ImagePlus("Scatter Rendering", imgProc);
        return image;
    }
    
  
    
}
