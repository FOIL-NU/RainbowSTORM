/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.plugin.ZProjector;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import java.awt.Color;
import java.awt.Font;
import static java.lang.Math.ceil;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import unmixing.Blinking;
import rendering.GaussianRendering;
/**
 *
 * @author Janel
 */
public class sSMLM_3D {
    
    double resolution;
    int width;
    int height;
    
    ImagePlus image;
    ColorProcessor cp; 
    ImageProcessor imgProc; 
    ImageProcessor img_tmp;
    ImageStack n_stack;
    ImageStack imgs;
    GaussianRendering grn;
      
      public sSMLM_3D(int width, int height, double resolution) {
        this.resolution = resolution;
     
        this.width = (int) ((width ));
        this.height = (int) ((height));
      
   
        this.imgProc = new FloatProcessor(this.width, this.height);
        this.cp = new ColorProcessor(this.width, this.height);
        this.n_stack = new ImageStack(this.width, this.height);
        grn = new GaussianRendering(width,height,resolution);
        
    }
      
      
        public void renderTCImage(ArrayList<Blinking> BEs,int[] nRng, ArrayList<Color> cols) {
           int sz=BEs.size();
         
          for (int i=0;i<sz;i++){
              
              updateProgress(i,sz);
              Blinking bEvent= BEs.get(i);
              double centroid= bEvent.getCentroid();
            
              double xpos=bEvent.getXPosition();
              double ypos =bEvent.getYPosition();
             
              
              Color col = getColor(centroid,nRng,cols);
           
              this.drawPoint(xpos,ypos,col);
      
          }
  
    }
        
        
     public ImageProcessor renderTCROI(ArrayList<Blinking> BEs,int[] nRng, ArrayList<Color> cols,int xmin, int ymin) {
           int sz=BEs.size();
           cp = new ColorProcessor(imgProc.getWidth(),imgProc.getHeight());
       
         for (int i=0;i<sz;i++){
              updateProgress(i,sz);
              Blinking bEvent= BEs.get(i);
              double centroid= bEvent.getCentroid();
              double xpos=(bEvent.getXPosition()-xmin)+1;
              double ypos =(bEvent.getYPosition()-ymin)+1;

              Color col = getColor(centroid,nRng,cols);
              this.drawPoint(xpos,ypos,col);
       
                
          }
         
       
       return cp.convertToRGB();
            
      
    }
     
     
      public ImageProcessor renderTCGaussianROI(ArrayList<Blinking> BEs,int[] nRng, ArrayList<Color> cols,int xmin, int ymin, int mxunc) {
       
          int sz=BEs.size();
         if(sz>0){
            int im_sz2=nRng.length;
         
           imgs = new ImageStack(imgProc.getWidth(),imgProc.getHeight()); 
          
           for(int k=0;k<im_sz2;k++){
           
               imgs.addSlice(new FloatProcessor(imgProc.getWidth(),imgProc.getHeight()));
            
           }
           
          for (int i=0;i<sz;i++){
              updateProgress(i,sz);
           
              Blinking bEvent= BEs.get(i);
              double centroid= bEvent.getCentroid();
              double dx=bEvent.getUnc();
              double xpos=(bEvent.getXPosition()-xmin)+1;
              double ypos =(bEvent.getYPosition()-ymin)+1;
              
            
              int col = getRGBColor(centroid,nRng,cols);
              
              img_tmp=null;
         
              img_tmp=drawTCGaussian(xpos,ypos,dx,imgs.getProcessor(col+1));
              imgs.setProcessor(img_tmp,col+1);
           
          }
          
           n_stack = new ImageStack(imgProc.getWidth(),imgProc.getHeight());
            ImageProcessor ip= null;
          
           for(int uv=0;uv<im_sz2;uv++){
               
              Color c1= cols.get(uv);
               
                            
              int cnt=uv+1; 
             
             ip=imgs.getProcessor(cnt);
            
             double mn_v=ip.getMin();
             double mx_v=ip.getMax();
             
             if(mn_v>=0&&mx_v>=0&&mx_v>mn_v){
            
               LUT lut =LUT.createLutFromColor(c1);
                lut.min=mn_v;
                lut.max=mx_v;
               
               ip.setLut(lut);
               cp=ip.convertToColorProcessor();
             
               n_stack.addSlice(cp);
   
             }
             ip=null;
      
          }
       ImagePlus fin = new ImagePlus("sSMLM Averaged Gaussian Rendering",n_stack);
       
       ZProjector imgs2 = new ZProjector(fin);
       imgs2.setMethod(ZProjector.SUM_METHOD);
       imgs2.doRGBProjection();
      
      ImagePlus out= imgs2.getProjection();
      ImageProcessor c_img = out.getProcessor();
     
      
     return c_img; 
         }else{
             
          return cp.convertToRGB();
         }
  
    }
     
    private void updateProgress(final int cur,final int total) { 
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() { 

                IJ.showStatus("Rendering Image");
                IJ.showProgress(cur, total);
            } });}
    
      private void updateStatus() { 
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() { 

                IJ.showStatus("Rendering Image");
     
            } });}
      
       public ImageProcessor drawTCGaussian(double x, double y,double dx, ImageProcessor tmp) {
       
        
        int xt1=1;
        int xt2=this.width;
     
          
            int u = (int) (x / resolution);
            int v = (int) (y  / resolution);
            double radius=  (dx / resolution);
           
             double x_org = x / resolution;
             double y_org = y / resolution;
           
            int actualRadius =(int) (ceil(radius * 3)); 
           
            double sqrt2dx = Math.sqrt(2) * radius;
         
            boolean flg1=((u-actualRadius)>xt1)&&((u+actualRadius)<xt2);
        
            int yt1 =1;
            int yt2=this.height;
            boolean flg2=((v-actualRadius)>yt1)&&((v+actualRadius)<yt2);
          
        if(flg1&&flg2){
                   
                    for(int idx = u - actualRadius; idx <= u + actualRadius; idx++) {
                       
                            double difx = idx - x_org;
                            double xerfdif = (grn.erf((difx) / sqrt2dx) - grn.erf((difx + 1) / sqrt2dx));
                               
                            for(int idy = v - actualRadius; idy <= v + actualRadius; idy++) {
                               
                                    double dify = idy - y_org;
                                   
                                    double val = (1*xerfdif
                                            * (grn.erf((dify) / sqrt2dx) - grn.erf((dify + 1) / sqrt2dx)));
                           
                                    
                                 tmp.setf(idx, idy,(float)val+tmp.getf(idx,idy));
                      
                              
                            }
                        }
                  
        }
        return tmp;
    } 
        
       
      public void drawPoint(double x, double y, Color color) {
    
        int u = (int) (x / this.resolution);
        int v = (int) (y / this.resolution);
        int xt1=1;
        int xt2=this.width;
        boolean flg1=(u>xt1)&&(u<xt2);
        int yt1 =1;
        int yt2=this.height;
        boolean flg2=(v>yt1)&&(v<yt2);
        
        if(flg1&&flg2){
        cp.setColor(color);
        cp.drawDot(u,v);
        }
       
    }
      
  
          public ImagePlus getImage() {
       
         image = new ImagePlus("sSMLM True Color Rendering", cp);
       
        return image;
    }
       
      public int [] setRng(int mn1, int mx1,int stp) {
      
        int nbins=(int) Math.round((mx1-mn1)/stp)+1;
        
        int[] new_rng=getnewRng(mn1,mx1,stp,nbins);
        
        return new_rng;
    }   
      
      public Color getColor(double val,int[] new_rng, ArrayList<Color> cols){
        
         int nbins =cols.size();
         
         int stp=-1;
        for(int i=0;i<nbins;i++){
            int cbin=new_rng[i];
            if (val<cbin){
            stp=i;
            break;
            
            }
        }
          if(stp==-1)
          {
              stp=nbins-1;
          }
          
          Color c = cols.get(stp);
         return c;
      }
      
        public int getRGBColor(double val,int[] new_rng, ArrayList<Color> cols){
        
         int nbins =cols.size();
        
         int stp=-1;
        for(int i=0;i<nbins;i++){
            int cbin=new_rng[i];
            if (val<cbin){
            stp=i;
            break;
            
            }
        }
          if(stp==-1)
          {
              stp=nbins-1;
          }
         
         return stp;
      }
      
           
        public int[] getnewRng( int mn1, int mx1,int stp, int nbins) {
         
        int[] n_rng=new int[nbins];
        int cVal=mn1;
        for(int i=0;i<nbins;i++){
            n_rng[i]=cVal;
            cVal=cVal+stp;
            }
        
        
        return n_rng;
    }   
        
        
        
       public ArrayList<Color> getColRng( int mn1, int mx1,int[] n_rng) {
        ArrayList<Color> cRng =new ArrayList<Color>();  
        SpectrumPaintScale spc = new SpectrumPaintScale(mn1,mx1);
       
        int nbins=n_rng.length;
       
        for(int i=0;i<nbins;i++){
             
            Color c=spc.getPaint(n_rng[i]);
            
             cRng.add(c);
            
            }
        
        
        return cRng;
    }  
       
   
    
  
    
}
