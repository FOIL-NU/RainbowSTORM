/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unmixing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.CurveFitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import static java.lang.Math.round;
import java.util.ArrayList;
import java.util.Arrays;

import ij.plugin.ContrastEnhancer;

import java.util.OptionalInt;
import javax.swing.SwingUtilities;


/**
 *
 * @author Brian T. Soetikno
 */
public class Blinking {
    private int frame;
    private double x;
    private double y;
    private double z;
    private double ph_psf;
    private double sigma_psf;
    private double sigma1_psf;
    private double sigma2_psf;
    private double unc;
    private double centroid;
    private double ph_num;
    private double bk_pnum;
    private double bk_px;
    private double sigma_spe;
    private double unc_spe;
    private float[] spectrum;
    
    private double[] t_spec;
   
        
    
    public Blinking(int frame, double x, double y,double ph_psf,double sigma_psf,double unc) {
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.ph_psf=ph_psf;
        this.sigma_psf=sigma_psf;
        this.unc=unc;
    }
    
     public Blinking(int frame, double x, double y,double z, double ph_psf,double sigma1_psf,double sigma2_psf,double unc) {
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ph_psf=ph_psf;
        this.sigma1_psf=sigma1_psf;
        this.sigma2_psf=sigma2_psf;
        this.unc=unc;
    }
    
    public Blinking(int frame, double x, double y,double ph_psf,double sigma_psf, double unc,
            double centroid,double ph_num,double bk_pnum, double bk_px,double sigma_spe,double unc_spe,float[] spectrum) {
        // may have problems later because not directly copying
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.ph_psf=ph_psf;
        this.sigma_psf=sigma_psf;
        this.unc =unc;
        this.centroid= centroid;
        this.ph_num =ph_num;
        this.bk_pnum=bk_pnum;
        this.bk_px=bk_px;
        this.sigma_spe =sigma_spe;
        this.unc_spe =unc_spe;
        this.spectrum = spectrum;
      
    }
    
     public Blinking(int frame, double x, double y,double z,double ph_psf,double sigma1_psf,double sigma2_psf, double unc,
            double centroid,double ph_num,double bk_pnum, double bk_px,double sigma_spe,double unc_spe,float[] spectrum) {
        // may have problems later because not directly copying
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ph_psf=ph_psf;
        this.sigma1_psf=sigma1_psf;
        this.sigma2_psf=sigma2_psf;
        this.unc =unc;
        this.centroid= centroid;
        this.ph_num =ph_num;
        this.bk_pnum=bk_pnum;
        this.bk_px=bk_px;
        this.sigma_spe =sigma_spe;
        this.unc_spe =unc_spe;
        this.spectrum = spectrum;
      
    }
    
       public Blinking(int frame, double x, double y,double z,double ph_psf,double sigma1_psf,double sigma2_psf, double unc,
            double centroid,double ph_num,double bk_pnum, double bk_px,double sigma_spe,double unc_spe) {
        // may have problems later because not directly copying
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ph_psf=ph_psf;
        this.sigma1_psf=sigma1_psf;
        this.sigma2_psf=sigma2_psf;
        this.unc =unc;
        this.centroid= centroid;
        this.ph_num =ph_num;
        this.bk_pnum=bk_pnum;
        this.bk_px=bk_px;
        this.sigma_spe = sigma_spe;
        this.unc_spe = unc_spe;      
    }
    
          public Blinking(int frame, double x, double y,double ph_psf,double sigma_psf,double unc,
            double centroid,double ph_num,double bk_pnum, double bk_px,double sigma_spe,double unc_spe) {
        // may have problems later because not directly copying
        this.frame = frame;
        this.x = x;
        this.y = y;
       
        this.ph_psf=ph_psf;
        this.sigma_psf=sigma_psf;
       
        this.unc =unc;
        this.centroid= centroid;
        this.ph_num =ph_num;
        this.bk_pnum=bk_pnum;
        this.bk_px=bk_px;
        this.sigma_spe = sigma_spe;
        this.unc_spe = unc_spe;      
    }
    
    
    public double[] extractSpectrum(ImageProcessor spec,int x, int y,int bw, int specWidth ){
  
        double[] specVals= new double[specWidth];
        int avgSize=3;
      
        int s_rw=y+avgSize-bw;
        int e_rw=y+avgSize+bw+1;
       
        for(int i=0;i<specWidth;i++){
            float s_val =0;
            float val =0;
            int ind_col=x+i;
            for(int ind_rw=s_rw;ind_rw<e_rw;ind_rw++){
                
                float tmp=spec.getPixelValue(ind_col,ind_rw);
                s_val=s_val+tmp;
                
            }
            val=s_val;
          
            specVals[i]=(double)val;
        }
        
        return specVals;
    }
    
   
    public ArrayList<Blinking> addSpectroscopicData(ImagePlus img,ImagePlus bk_img, ArrayList<Blinking> blinkingEvents,double[] params,double[] xData,double[] yData,int fitOrder,int rmvOL,boolean is3D){
  
        int bw=(int) params[0];
        int rng1=(int) params[1];
        int rng2=(int) params[2];
        int stp=(int) params[3];
        int pixelSize=(int) params[4];
        int org_x=(int)params[5];
        int org_y=(int)params[6];
        int org_wid=(int)params[7];
        int px_shift=(int)params[8];
        double disp =params[9];
      
        double qE=1;
        int EMG=(int)params[10];
        double adu=params[11];
        int bsLv=(int)params[12]; 
        int fitFlg =(int) params[13];
        double specWidth =params[14];
        
        int sz = blinkingEvents.size();
        Blinking nB= blinkingEvents.get(0);
     
        final int tot= sz;
        int[] fy=setWavelengths(rng1,rng2,stp);
        
         double conv=pixelSize+0.5;
    
         int x_ind=Math.abs(org_x);
         CurveFitter cali= new CurveFitter(xData,yData);
        switch (fitOrder) {
            /*case 0:
                IJ.error("Error: Invalid Fitting Parameter");
                break;*/
            case 1:
                cali.doFit(CurveFitter.STRAIGHT_LINE);
                break;
            case 2:
                cali.doFit(CurveFitter.POLY2);
                break;
            case 3:
                cali.doFit(CurveFitter.POLY3);
                break;
            default:
                IJ.error("Error: Invalid Fitting Parameter");
                break;
        }
        double[] coeffs= cali.getParams();
        double[] x_new = new double[fy.length];
          for(int n=0;n<fy.length;n++){
            double pt=fy[n];
            x_new[n]=(cali.f(coeffs, pt)-px_shift);
          }
          
          if(fitOrder==2||fitOrder==3){
              int ind1=0;
              int ind2=fy.length-1;
              disp=Math.abs((fy[ind2]-fy[ind1])/(x_new[ind2]-x_new[ind1]));
          }
        
          int ind=(int) round(x_new[0]);
          int mx=(int) round(x_new[x_new.length-1]);
          int dif= mx-ind;
          float[][] sp_im1=new float[sz][dif];
          double[] px_rng=new double[dif];
          double[] pxs=new double[dif];
        
        ImageStack stack= img.getImageStack();
        ImageStack bk_stack = bk_img.getImageStack();
        int fr=1;
        ArrayList<Blinking> spc=new ArrayList<Blinking>();
        ImageProcessor ip=stack.getProcessor(fr);
        ImageProcessor bk_ip =bk_stack.getProcessor(fr);
        
         ArrayList<Integer> frms = new ArrayList<Integer>();
        for( int k=0;k<sz;k++){
            frms.add(blinkingEvents.get(k).getFrame());
        }
       
        for(int i=0; i<sz;i++){
          
            Blinking bEvent =blinkingEvents.get(i);
            
             int nf=bEvent.getFrame();
       
            boolean flg =false;
            
           if(rmvOL==1){
           
            int frm1=frms.indexOf(nf);
            int frm2=frms.indexOf(nf+1);
           
            for(int j=frm1; j<frm2;j++){
                if(i!=j){
                     Blinking bE2=blinkingEvents.get(j);
                                        
                         flg=isOverlapping(bEvent, bE2, params, xData, yData,fitOrder);
                    
                }
                if(flg==true){
                 
                 break;
                }
             
            }
            
           }
               if(!flg){
                  
            double xpos=bEvent.getXPosition();
            double ypos=bEvent.getYPosition();
            double ph_spatial = bEvent.getPhPsf();
            double sigma_spatial =-1;
            double zpos=-1;
            double sigma_spatial2=-1;
            //IJ.log("is3d:"+is3D);
            if(is3D){
               zpos=  bEvent.getZPosition();
               sigma_spatial= bEvent.getSigma1Psf();
               sigma_spatial2= bEvent.getSigma2Psf();
            }else{
                sigma_spatial=bEvent.getSigmaPsf();
              //  IJ.log("sigma2: "+sigma_spatial);
                
            }
            double lc_un=bEvent.getUnc();
           
            double xval= xpos/conv;
            double yval=ypos/conv;
           
            int x=(int)Math.round(xval);
           
            int y=(int)Math.round(yval)-((bw*2)+1);
          
            int n_crp=(ind+x+x_ind);
            
            int x_shift=n_crp-org_wid-1;
          
            int y_shift=(y);
              double[] spec=new double[dif];  
              double[] photons=new double[3];
              double sig_spe= 0;
              double spe_unc=0;
             
          int id=1;
          for(int r=0;r<dif;r++){
              pxs[r]=id;
              id=id+1;
          }
          
            if(nf!=fr){
               ip=stack.getProcessor(nf);
                  fr=nf;
                 photons=calculatePhotons(ip,bk_ip,x_shift,y_shift,bw,dif,adu,EMG,qE,pixelSize,bsLv);
                 spec=t_spec;
                  if (fitFlg==1){
                 sig_spe=getFWHMSpec(spec, pxs,disp);
                  }
                  else{
                     sig_spe=specWidth/2.355;
                  }
            
            }else{
                 
                  photons=calculatePhotons(ip,bk_ip,x_shift,y_shift,bw,dif,adu,EMG,qE,pixelSize,bsLv);
                  spec=t_spec;
                 if (fitFlg==1){
                 sig_spe=getFWHMSpec(spec, pxs,disp);
                
                 }
                 else{
                     sig_spe=specWidth/2.355;
                 }
             }
             int idx=ind;
          for(int r=0;r<dif;r++){
              px_rng[r]=idx;
              idx=idx+1;
             
          }
         
            float[] f_spec=this.interpLinear(px_rng,spec,x_new); 
           
            double c=calculateCentroid(f_spec,fy);
            
             sp_im1[i]=this.convertDoubleToFloat(spec);
             t_spec=null;
             
             double pnum=photons[0];
             double bk_pnum =photons[1];
             double bk_pxl=photons[2];
             
             spe_unc=getSpeUnc(sig_spe,disp,qE,pnum,pixelSize,sigma_spatial,bk_pxl);
             
            Blinking spEvent=null;
            if(is3D){
                 spEvent=new Blinking(nf,xpos,ypos,zpos,ph_spatial,sigma_spatial,sigma_spatial2,lc_un,
                    c,pnum,bk_pnum,bk_pxl,sig_spe,spe_unc,f_spec);
                
            }else{
            spEvent=new Blinking(nf,xpos,ypos,ph_spatial,sigma_spatial,lc_un,
                    c,pnum,bk_pnum,bk_pxl,sig_spe,spe_unc,f_spec);
            }
            spc.add(spEvent);
          
            updateProgress(i,tot);
            
        }
           }
 
        return spc;
    }
    
    
    private void updateProgress(final int cur,final int total) { 
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() { 

             
                IJ.showStatus("Processing: " + cur+1+" of "+total);
     
                IJ.showProgress(cur, total);
            } });}
    
    
   public ImageProcessor getPSF_Image(ImagePlus zeroOrderImage,Blinking bEvent,double[] params){
       
         int rng= 3;
         int w =(2*rng)+1;
         int h=w;
         int pixelSize=(int) params[4];
         int nf=bEvent.getFrame();
            double xpos=bEvent.getXPosition();
            double ypos=bEvent.getYPosition();
           
            double conv=pixelSize+0.5;
            
            double xval= xpos/conv;
            double yval=ypos/conv;
            
            
         int x_i=(int)Math.round(xval)-rng;
         int y_i =(int)Math.round(yval)-rng;
         
         
         ImageStack c_stack = zeroOrderImage.getStack();
         ImageProcessor c_ip =c_stack.getProcessor(nf);
         ImageProcessor bkp = new FloatProcessor(w,h);
         ImageProcessor psf_im =  new FloatProcessor(w,h);
        
         for (int i =0; i < w; i++ ) {
            int u=x_i+i;
             int v_1=y_i;
             int[] bk= new int[h]; 
             c_ip.getColumn(u, v_1,bk, h);
             OptionalInt temp=Arrays.stream(bk).min();
             int thres =temp.getAsInt();
             if(thres<0)  thres=0;
            for (int j = 0; j< h; j++) {
               int v=y_i+j;
                int bx=thres;
                  
                bkp.putPixelValue(u, v, bx);
                
                float p = c_ip.getPixelValue(u,v);
                psf_im.putPixelValue(i,j,p);
            }
      
        }
      
        
         ImagePlus mn_img =new ImagePlus("Background",bkp);
          ImagePlus psf_img =new ImagePlus("PSF",psf_im);
        
         BackgroundSubtraction bk = new BackgroundSubtraction();
        ImagePlus psf_bkrm =bk.process(psf_img, mn_img);

       ContrastEnhancer ce = new ContrastEnhancer();
       ce.stretchHistogram(psf_bkrm, 0.35);
   
        ImageProcessor psfImg = psf_bkrm.getProcessor();
       
       return psfImg;
   }
    
    public ImageProcessor getSpec_Image(ImagePlus firstOrderImage,Blinking bEvent, double[] params,double[] xD,double[] yD,int fitOrder){
        int r= 3;
        int h =(2*r)+1;
       
        int rng1=(int) params[1];
        int rng2=(int) params[2];
        int stp=(int) params[3];
        int pixelSize=(int) params[4];
        int org_x=(int)params[5];
        int org_wid=(int)params[7];
        int px_shift=(int)params[8];
    
        
        int[] fy=setWavelengths(rng1,rng2,stp);
       
         int x_ind=Math.abs(org_x);
                 
        CurveFitter cali= new CurveFitter(xD,yD);
          switch (fitOrder) {
            case 1:
                cali.doFit(CurveFitter.STRAIGHT_LINE);
                break;
            case 3:
                cali.doFit(CurveFitter.POLY3);
                break;
            default:
                IJ.error("Error: Invalid Fitting Parameter");
                break;
        }
        double[] coeffs= cali.getParams();
      
        double[] x_new = new double[fy.length];
       
          for(int n=0;n<fy.length;n++){
            double pt=fy[n];
          
            x_new[n]=(cali.f(coeffs, pt)-px_shift);
     
        }
       
         //Initial PixelPostion
          int ind=(int) round(x_new[0]);
        
          int mx=(int) round(x_new[x_new.length-1]);
         
          int dif= mx-ind;
          
            int nf=bEvent.getFrame();
            double xpos=bEvent.getXPosition();
            double ypos=bEvent.getYPosition();
          
           double conv=pixelSize+0.5;
            ImageStack stk =firstOrderImage.getImageStack();
            ImageProcessor spec = stk.getProcessor(nf);
          
       int w=dif;
     
            double xval= xpos/conv;
            double yval=ypos/conv;
            
            
         int x_i=(int)Math.round(xval);
         int y_i =(int)Math.round(yval)-r;
         
          int n_crp=(ind+x_i+x_ind);
         
            int x_shift=n_crp-org_wid-1;
        
            int y_shift=(y_i);
          
             
        
        ImageProcessor specImg=  new FloatProcessor(w,h);
        
          for(int i=0;i<w;i++){
           
            int ind_col=x_shift+i;
            for(int j=0;j<h;j++){
                int ind_rw =y_shift+j;
                
                float tmp=spec.getPixelValue(ind_col,ind_rw);
               
                specImg.putPixelValue(i, j, tmp);
                
            }
           
        }
      
          ImagePlus spec_img =new ImagePlus("Spectra",specImg);
      
      ContrastEnhancer ce = new ContrastEnhancer();
       ce.stretchHistogram(spec_img, 0.35);
     
         return spec_img.getProcessor();
        
    }
    
    
       public boolean isOverlapping(Blinking blinkingEvent1,Blinking blinkingEvent2,double[] params,double[] xData,double[] yData, int fitOrder){
       
        int bw=(int) params[0];
        int rng1=(int) params[1];
        int rng2=(int) params[2];
        int stp=(int) params[3];
        int pixelSize=(int) params[4];
        int px_shift=(int)params[8];
        int r =(2*bw)+1;
      
        int[] fy=setWavelengths(rng1,rng2,stp);
       
        CurveFitter cali= new CurveFitter(xData,yData);
           switch (fitOrder) {
            case 1:
                cali.doFit(CurveFitter.STRAIGHT_LINE);
                break;
            case 3:
                cali.doFit(CurveFitter.POLY3);
                break;
            default:
                IJ.error("Error: Invalid Fitting Parameter");
                break;
        }
        double[] coeffs= cali.getParams();
      
        double[] x_new = new double[fy.length];
       
          for(int n=0;n<fy.length;n++){
            double pt=fy[n];
          
            x_new[n]=(cali.f(coeffs, pt)-px_shift);
       
            
        }
        
          int ind=(int) round(x_new[0]);
          int mx=(int) round(x_new[x_new.length-1]);
         
          int dif =mx-ind;
            Blinking bEvent1 =blinkingEvent1;
            
            
            double xpos=bEvent1.getXPosition();
            double ypos=bEvent1.getYPosition();
           
             double conv=pixelSize;
            
            double xval= xpos/conv;
            double yval=ypos/conv;
         
            int x=(int)Math.round(xval)-r;
           
            int y=(int)Math.round(yval)-r;
          
             Blinking bEvent2 =blinkingEvent2;
            
            double xpos2=bEvent2.getXPosition();
            double ypos2=bEvent2.getYPosition();
           
            double xval2= xpos2/conv;
            double yval2=ypos2/conv;
         
            int x2=(int)Math.round(xval2)-r;
           
            int y2=(int)Math.round(yval2)-r;
          
             int d1=Math.abs(x2-x);
            int d2= Math.abs(y2-y);
          
            boolean flg_OL =true;
            int dif2 =(int)Math.round((dif+1)*2);
         
         if((d1>dif2)||(d2>(2*(r+1)))){
            flg_OL =false;
         }
       
        return flg_OL;
    }
    
    
    public Blinking getBlinkingInfo(ImagePlus img,ImagePlus bk_img, Blinking blinkingEvent,double[] params,double[] xData,double[] yData,int fitOrder){
        int bw=(int) params[0];
        int rng1=(int) params[1];
        int rng2=(int) params[2];
        int stp=(int) params[3];
        int pixelSize=(int) params[4];
        int org_x=(int)params[5];
        int org_y=(int)params[6];
        int org_wid=(int)params[7];
        int px_shift=(int)params[8];
        double disp =params[9];
        
        //Photon number calculation
        double qE=1;
        int EMG=(int)params[10];
        double adu=params[11];
        int bsLv=(int)params[12]; 
        int fitFlg =(int) params[13];
        double specWidth =params[14];
       
        int rng=(rng2-rng1);
      
        int[] fy=setWavelengths(rng1,rng2,stp);
       
         int x_ind=Math.abs(org_x);
         int y_ind=Math.abs(org_y);
         
        CurveFitter cali= new CurveFitter(xData,yData);
           switch (fitOrder) {
            case 1:
                cali.doFit(CurveFitter.STRAIGHT_LINE);
                break;
            case 3:
                cali.doFit(CurveFitter.POLY3);
                break;
            default:
                IJ.error("Error: Invalid Fitting Parameter");
                break;
        }
        double[] coeffs= cali.getParams();
      
        double[] x_new = new double[fy.length];
       
          for(int n=0;n<fy.length;n++){
            double pt=fy[n];
          
            x_new[n]=(cali.f(coeffs, pt)-px_shift);
       
        }
       
          int ind=(int) round(x_new[0]);
        
          int mx=(int) round(x_new[x_new.length-1]);
         
          int dif= mx-ind;
          
          double[] sec_spec= new double[dif]; 
         
          double[] px_rng=new double[dif];
          double[] pxs=new double[dif];
        
        ImageStack stack= img.getImageStack();
        ImageStack bk_stack = bk_img.getImageStack();
        int fr=1;
        ArrayList<Blinking> spc=new ArrayList<Blinking>();
        ImageProcessor ip=stack.getProcessor(fr);
        ImageProcessor bk_ip =bk_stack.getProcessor(fr);
        
            Blinking bEvent =blinkingEvent;
            int nf=bEvent.getFrame();
            double xpos=bEvent.getXPosition();
            double ypos=bEvent.getYPosition();
            double ph_spatial = bEvent.getPhPsf();
          
            double sigma_spatial =bEvent.getSigmaPsf();
       
            double lc_un=bEvent.getUnc();
          
             double conv=pixelSize+0.5;
            double xval= xpos/conv;
            double yval=ypos/conv;
         
            int x=(int)Math.round(xval);
           
            int y=(int)Math.round(yval)-3;
          
            int n_crp=(ind+x+x_ind);
         
            int x_shift=n_crp-org_wid-1;
        
            int y_shift=(y);;
              double[] spec=new double[dif];  
              double[] photons=new double[3];
              double sig_spe= 0;
              double spe_unc=0;
              
               int id=1;
          for(int r=0;r<dif;r++){
              pxs[r]=id;
              id=id+1;
           
          }
              
            if(nf!=fr){
               ip=stack.getProcessor(nf);
               fr=nf;
               photons=calculatePhotons(ip,bk_ip,x_shift,y_shift,bw,dif,adu,EMG,qE,pixelSize,bsLv);
                 spec=t_spec;
                  if (fitFlg==1){
                 sig_spe=getFWHMSpec(spec, pxs,disp);
                  }
                  else{
                     sig_spe=specWidth/2;
                  }
             
            }else{
                 photons=calculatePhotons(ip,bk_ip,x_shift,y_shift,bw,dif,adu,EMG,qE,pixelSize,bsLv);
                 spec=t_spec;
                
                 if (fitFlg==1){
                 sig_spe=getFWHMSpec(spec, pxs,disp);
                 }
                 else{
                     sig_spe=specWidth/2.355;
                 }
            
            }
            
               int idx=ind;
          for(int r=0;r<dif;r++){
            
              px_rng[r]=idx;
              idx=idx+1;
             
          }
         
             float[] f_spec=this.interpLinear(px_rng,spec,x_new); 
           
            
            double c=calculateCentroid(f_spec,fy);
            
             t_spec=null;
             
             double pnum=photons[0];
             double bk_pnum =photons[1];
             double bk_pxl=photons[2];
             
             spe_unc=getSpeUnc(sig_spe,disp,qE,pnum,pixelSize,sigma_spatial,bk_pxl);
          
            Blinking spEvent=new Blinking(nf,xpos,ypos,ph_spatial,sigma_spatial,lc_un,
                    c,pnum,bk_pnum,bk_pxl,sig_spe,spe_unc,f_spec);
          
       
        return spEvent;
    }
    
    
    public double getSpeUnc(double sig_spe,double disp, double eta, double ph_num,int pixelSize,
            double sigma_spt,double bk_px1){
     
        int nr=1;
       
        double cnst1 =Math.sqrt(((sig_spe*sig_spe)+(disp*disp)/12));
        double cnst2 =eta*ph_num;
        double sshot = Math.sqrt(2*((sig_spe*sig_spe)+(disp*disp)/12))/(eta*ph_num);
        double sread = Math.sqrt((1024*(sig_spe*sig_spe*sig_spe)*sigma_spt*(nr*nr))/(3*disp*pixelSize*(cnst2*cnst2)));
        double sbg=Math.sqrt(2*((1024*bk_px1*(cnst1*cnst1*cnst1)*sigma_spt)/(3*disp*pixelSize*(cnst2*cnst2))));
        double sp_unc=Math.sqrt((sshot*sshot)+(sread*sread)+(sbg*sbg));
        
        return sp_unc;
    }
    
    
    public double[] calculatePhotons(ImageProcessor spec,ImageProcessor bk,int x, int y,
            int bw, int specWidth,double adu, int EMG, double qe,int pxSz, int bsLv ){
        double emg=(double)EMG;
        double[] sigs= new double[3];
        double photon_num=0;
        double bk_photon_num=0;
        double bkp_px=0;
        int avgSize=3;
        int len=specWidth;
        int wid=(2*bw)+1;
        double tA=(len*wid);
       
        double pnum = 0;
        double bknum = 0;
      
        int s_rw=y+avgSize-bw;
        int e_rw=y+avgSize+bw+1;
        int df=e_rw-s_rw;
       
        t_spec= new double[specWidth];
        for(int i=0;i<specWidth;i++){
            float val =0;
            float val_bk =0;
            int ind_col=x+i;
            for(int ind_rw=s_rw;ind_rw<e_rw;ind_rw++){
                
                float tmp=spec.getPixelValue(ind_col,ind_rw);
                val=val+tmp;
                
                float tmp_bk=bk.getPixelValue(ind_col,ind_rw);
                val_bk=val_bk+tmp_bk;
                
            }
            
            pnum=pnum+(double) val;
            bknum=bknum+(double) val_bk;
          
            t_spec[i]=(double)val;
        }
      
          double tmp_pn=(pnum/emg)*adu;
          double tmp_bk=(bknum/emg)*adu;
         
         photon_num=tmp_pn/qe;
         bk_photon_num=tmp_bk/qe;
         bkp_px=bk_photon_num/tA;
       
         sigs[0]=photon_num;
         sigs[1]=bk_photon_num;
         sigs[2]=bkp_px;
         
        return sigs;
    }
    
    private double getFWHMSpec(double[] spec,double[] px_rng, double disp){
       
        double sigma_sp=0;
    
        CurveFitter gsn= new CurveFitter(px_rng,spec);
        try{
                   
        gsn.doFit(CurveFitter.GAUSSIAN_NOOFFSET);
      
        int flg =gsn.getStatus();
       
        String dsc2 =gsn.getResultString();
        
        if (flg==0){
        double[] coeffs= gsn.getParams();
        int sz=coeffs.length;
      
        double a=coeffs[0];
        
        double b=coeffs[1];
     
        double c=coeffs[2];
      
         double c1=coeffs[3];
       
         sigma_sp=c*disp;
    }else{
        String dsc =gsn.getStatusString();
        
        }
        
           }
        catch(ArrayIndexOutOfBoundsException exception) {
            sigma_sp=-1;
        
            
        }
      
        return sigma_sp;
     
    }
            
    
    public double calculateCentroid(float[] spec, int[] wvs){
        float tmp_c=0;
        double c;
        float sum_spec=0;
        float sum_spec_wv=0;
        for(int i=0;i<spec.length;i++)
        {  
            float wv= wvs[i];
            float val=spec[i];
            float tmp =val*wv;
            // IJ.log("Wavelength "+wvs[i]);
            // IJ.log("Val "+spec[i]);
            sum_spec_wv=sum_spec_wv+tmp;
            sum_spec=sum_spec+val;
            
        }
        
        
        tmp_c=sum_spec_wv/sum_spec;
        c=(double) tmp_c;
       //  IJ.log("Centroid "+c);
        
        return c;
    }
    
    public int[] setWavelengths(int rng1,int rng2, int stp){
        int dif=Math.abs(rng2-rng1);
        int arWid = Math.round(dif/stp);
        
        int[] wvs = new int[arWid];
        int rsum=rng1;
        for(int i= 0;i<arWid;i++){
            if(i==0){
            wvs[i]=rsum;
            }else{
            rsum=rsum+stp;
            wvs[i]=rsum;
                    }
        
                        
        }
        
        return wvs;
        
    }
    
  /*  public float[] findSpectrum(ImagePlus firstOrder, int averageSize) {
        ImageStack stack = firstOrder.getStack();
        ImageProcessor processor = stack.getProcessor(this.frame);
        
        this.spectrum = new float[processor.getWidth()];
        
        float[] currentSpectrum;
        
        for (int i= -averageSize; i <=averageSize; i++) {
            // Check bounds
            if (!(this.y + i < 0 || this.y + i > processor.getHeight()) ) {
                currentSpectrum = processor.getRow(0, (int) this.y + i, spectrum, processor.getWidth());
                
                // Add it to the final spectrum
                for (int j=0; j < currentSpectrum.length; j++) {
                    this.spectrum[j] = this.spectrum[j] + currentSpectrum[j];
                }
            }
        } 
       
        return this.spectrum;
    }
    */
    
    
   
    private float[] interpLinear(double[] x, double[] y, double[] xi) throws IllegalArgumentException {

        if (x.length != y.length) {
            throw new IllegalArgumentException("X and Y must be the same length");
        }
        if (x.length == 1) {
            throw new IllegalArgumentException("X must contain more than one value");
        }
        double[] dx = new double[x.length - 1];
        double[] dy = new double[x.length - 1];
        double[] slope = new double[x.length - 1];
        double[] intercept = new double[x.length - 1];

        // Calculate the line equation (i.e. slope and intercept) between each point
        for (int i = 0; i < x.length - 1; i++) {
            dx[i] = x[i + 1] - x[i];
            if (dx[i] == 0) {
                throw new IllegalArgumentException("X must be montotonic. A duplicate " + "x-value was found");
            }
            if (dx[i] < 0) {
                throw new IllegalArgumentException("X must be sorted");
            }
            dy[i] = y[i + 1] - y[i];
            slope[i] = dy[i] / dx[i];
            intercept[i] = y[i] - x[i] * slope[i];
        }

        // Perform the interpolation here
        double[] yi = new double[xi.length];
        float[] f_yi = new float[xi.length];
        for (int i = 0; i < xi.length; i++) {
            if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
             
                yi[i] = 0;
                f_yi[i]=0;
            }
            else {
                int loc = Arrays.binarySearch(x, xi[i]);
                if (loc < -1) {
                    loc = -loc - 2;
                    yi[i] = slope[loc] * xi[i] + intercept[loc];
                    f_yi[i]=(float)yi[i];
                }
                else {
                    yi[i] = y[loc];
                    
                    f_yi[i]=(float)yi[i];
                }
            }
         
        }

        return f_yi;
    }
 

    public double[] convertFloatToDouble(float[] data) {
        double[] newData = new double[data.length];
        for (int k=0; k < data.length; k++) {
            newData[k] = data[k];
        }
        return newData;
    }
    
    
        public float[] convertDoubleToFloat(double[] data) {
        float[] newData = new float[data.length];
        for (int k=0; k < data.length; k++) {
            newData[k] =(float) data[k];
        }
        return newData;
    }
        
        
        public int[] convertDoubleToInt(double[] data) {
        int[] newData = new int[data.length];
        for (int k=0; k < data.length; k++) {
            newData[k] =(int) Math.round(data[k]);
        }
        return newData;
    }

    public int getFrame() {
        return this.frame;
    }
    
    public double getXPosition() {
        return this.x;
    }

    public double getYPosition() {
        return this.y;
    }
    
     public double getZPosition() {
        return this.z;
    }
    
    public double getPhPsf() {
        return this.ph_psf;
    }
       
    public double getSigmaPsf() {
        return this.sigma_psf;
    }
    
      public double getSigma1Psf() {
        return this.sigma1_psf;
    }
      
        public double getSigma2Psf() {
        return this.sigma2_psf;
    }
          
    public double getUnc() {
        return this.unc;
    }
    
    public double getCentroid() {
        return this.centroid;
    }
    
    
       public double getPhotons() {
        return this.ph_num;
    }
        
       
    public double getBkPhotons() {
        return this.bk_pnum;
    }
         
    
     public double getBkPx() {
        return this.bk_px;
    }
     
        public double getSigmaSPE() {
        return this.sigma_spe;
    }
        
           public double getUncSPE() {
        return this.unc_spe;
    }
      
       
    public float[] getSpectrum() {
       
        return this.spectrum;
    }
 
}
