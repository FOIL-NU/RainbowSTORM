/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.*;
import ij.gui.ImageCanvas;
import ij.process.*;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.plugin.ContrastEnhancer;
import java.awt.*;
import ij.plugin.ZProjector;
import static java.lang.Math.ceil;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import unmixing.Blinking;


/**
 *
 * @author Janel Davis
 */
public class sSMLMLambdaColoredRendering {
    

    
    double resolution;
    int width;
    int height;
    
    ImagePlus image;
    ColorProcessor cp; 
    ImageProcessor imgProc; 
    ImageProcessor img_tmp;
    ImageStack n_stack;
    ImageStack imgs;
    
    
    
      public sSMLMLambdaColoredRendering(int width, int height, double resolution) {
        this.resolution = resolution;
     
        
        this.width = (int) ((width ));//+(2*resolution));
        this.height = (int) ((height));//+(2*resolution));
        
        System.out.println("Blinking Image Width: " + this.width);
        System.out.println("Blinking Image Height: " + this.height);
        //this.imgProc = new 
        this.cp = new ColorProcessor(this.width, this.height);
        this.imgProc = new FloatProcessor(this.width, this.height);
        //this.imgProc.setValue(16777215);
    }
      
      
      
        public void renderTCImage(ArrayList<Blinking> BEs,int[] nRng, ArrayList<Color> cols) {
           int sz=BEs.size();
           if(sz>0){
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
  
    }
        
        
     public void renderTCROI(ArrayList<Blinking> BEs,int[] nRng, ArrayList<Color> cols,int xmin, int ymin) {
           int sz=BEs.size();
           
         if(sz>0){  
        
         for (int i=0;i<sz;i++){
              updateProgress(i,sz);
              Blinking bEvent= BEs.get(i);
              double centroid= bEvent.getCentroid();
              
              double xpos=(bEvent.getXPosition()-xmin)+1;
              double ypos =(bEvent.getYPosition()-ymin)+1;

              Color col = getColor(centroid,nRng,cols);
              this.drawPoint(xpos,ypos,col);
           
          }
         
          
     
      ImagePlus out = new ImagePlus("sSMLM Pseudo-Colored Scatter Rendering", cp);     
      ContrastEnhancer ce = new ContrastEnhancer();
      ce.stretchHistogram(out, 0.35);
      out.setTitle("sSMLM Pseudo-Colored ScatterRendering");
      out.show();
      this.drawLegend(nRng,cols);
         }
   
    }
     

    
     
     
      public void renderTCGaussianROI(ArrayList<Blinking> BEs,int[] nRng, ArrayList<Color> cols,int xmin, int ymin, int mxunc) {
        
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
       ImagePlus fin = new ImagePlus("sSMLM Pseudo-Colored Averaged Gaussian Rendering",n_stack);
       
       ZProjector imgs2 = new ZProjector(fin);
       imgs2.setMethod(ZProjector.SUM_METHOD);
       imgs2.doRGBProjection();
       
      ImagePlus out= imgs2.getProjection();
      
      ContrastEnhancer ce = new ContrastEnhancer();
      ce.stretchHistogram(out, 0.35);
      out.setTitle("sSMLM Pseudo-Colored Averaged Gaussian Rendering");
      out.show();
      this.drawLegend(nRng,cols);
     
          
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
                            double xerfdif = (erf((difx) / sqrt2dx) - erf((difx + 1) / sqrt2dx));
                               
                            for(int idy = v - actualRadius; idy <= v + actualRadius; idy++) {
                               
                                    double dify = idy - y_org;
                                   
                                    double val = (1*xerfdif
                                            * (erf((dify) / sqrt2dx) - erf((dify + 1) / sqrt2dx)));
                           
                                    
                                 tmp.setf(idx, idy,(float)val+tmp.getf(idx,idy));
                      
                              
                            }
                        }
                  
        }
        return tmp;
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
        //imgProc.setf(u,v, color);
    }
      
  
         
         
          public ImagePlus getImage() {
     
         image = new ImagePlus("sSMLM Pseudo-Colored Super-resolution Rendering", cp);
        
   
        
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
         Color mn_col=cols.get(0);
         Color mx_col=cols.get(nbins-1);
          int i_mncol=mn_col.getRGB();
         
        
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
      
      
      
        public float getFColor(double val,int[] new_rng, ArrayList<Float> cols){
          
        float c = -1;
         int nbins =cols.size();
         //IJ.log("Bins:"+nbins);
         int stp=-1;
        for(int i=0;i<nbins;i++){
            int cbin=new_rng[i];
            if (val<cbin){
            stp=i;
            break;
            
         
            }
        }
          if(stp!=-1)
          {
              c = cols.get(stp);
          }
          
       
         return c;
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
       
       
     public ArrayList<Float> getFColRng( int mn1, int mx1,int[] n_rng) {
        ArrayList<Float> cRng =new ArrayList<Float>();  
        SpectrumPaintScale spc = new SpectrumPaintScale(mn1,mx1);
       
        int nbins=n_rng.length;
       
        for(int i=0;i<nbins;i++){
             
            float c=spc.getFPaint(n_rng[i]);
            
             cRng.add(c);
            
            }
        
        
        return cRng;
    }  
     
        public ArrayList<Color> convertFtoCol(ArrayList<Float> fvals) {
        ArrayList<Color> cRng =new ArrayList<Color>();  
        SpectrumPaintScale spc = new SpectrumPaintScale(0,1);
       
        int nbins=fvals.size();
        
        for(int i=0;i<nbins;i++){
            float curval =fvals.get(i);
            Color c=spc.getColor(curval);
            
             cRng.add(c);
            
            }
        
        
        return cRng;
    }  
        
        
   
       
   public void drawLegend(int[] new_rng, ArrayList<Color> cols){
                 int sz =new_rng.length;
                int min= new_rng[0];
                int max =new_rng[sz-1];
                int ipwid=60;
                int iph=200;
		ColorProcessor iplegend = new ColorProcessor(ipwid , iph );
		
                for(int u = 0; u<ipwid; u++){
			for (int v = 0; v < iph ; v++){
                            
                               float val =(float)( max - (max-min) * (double) v / iph) ;
                                
                                
                                Color col=getColor(val,new_rng,cols);
                                float vu=(float)col.getRGB();
                                iplegend.setColor(col);
                                 
                                if(u<=ipwid/3)
				iplegend.setf(u,v,vu);
                                else{
                                Color col2=Color.white;
                                float vu2=(float)col2.getRGB();
                                iplegend.setf(u,v, vu2);
                                }
                             
			}
		}
             
		ImagePlus plegend = new ImagePlus("sSMLM Legend", iplegend); 
		plegend.show();
                ImageCanvas ic = new ImageCanvas(plegend);
              
                ic.setSize(ipwid, iph+20);
               // plegend.set
		//IJ.run("Canvas Size...", "width="+ 80 +" height="+ 220 +" position=Center-Left");
		
                     
		Font font = new Font("SansSerif", Font.PLAIN, 8);
		String stup = "" + max + " nm";
		String stmin = "" + min + " nm";
		String stmid = "" + (max + min)/2 + " nm";
		TextRoi roiup = new TextRoi(ipwid/2, 2, stup, font); 
		TextRoi roimid = new TextRoi(ipwid/2,(iph/2)+5, stmid, font);
		TextRoi roilow = new TextRoi(ipwid/2, iph, stmin, font);
               
		//roi.drawPixels(fond);
		roiup.setStrokeColor(Color.black);
		roimid.setStrokeColor(Color.black);
		roilow.setStrokeColor(Color.black);
		Overlay ov = new Overlay(); 
		ov.add(roiup);
		ov.add(roimid);
		ov.add(roilow);
                plegend.setOverlay(ov);
	}	    
  
   
   
       
       
       
}
