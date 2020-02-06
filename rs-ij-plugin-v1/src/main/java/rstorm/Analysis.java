package rstorm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import ij.plugin.*;
import ij.process.ImageProcessor;

import ij.*;
import ij.gui.*;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.measure.CurveFitter;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.io.File;
import javax.swing.JFileChooser;

import au.com.bytecode.opencsv.*;
import gui.SpatialImageParameterPanel;
import gui.LoadLocalizationsPanel;
import gui.SpectraExtractionPanel;
import filehandling.ExportCSV;
import filehandling.ImportCSV;
import filehandling.ImportDrift;
import gui.BackgroundSubtractionPanel;
import gui.PreviewPanel;
import static gui.SpatialImageParameterPanel.textfieldCropPositions;
import gui.VisSettingsPanel;

import gui.VisualizationPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeListener;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.io.FileWriter;

import static java.lang.Math.round;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collections;

import javax.swing.UIManager;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import rendering.ScatterRendering;
import unmixing.BackgroundSubtraction;

import unmixing.Blinking;


import javax.swing.BorderFactory;
import javax.swing.WindowConstants;
import javax.swing.event.SwingPropertyChangeSupport;

import org.apache.commons.lang3.ArrayUtils;

import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import org.jfree.chart.ChartFactory;

import org.jfree.chart.ChartPanel;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.util.ShapeUtils;
import rendering.GaussianRendering;

import rendering.sSMLMDriftResults;
import rendering.sSMLMLambdaColoredRendering;
import rendering.sSMLM_3D;



public class Analysis implements PlugIn {
    public final static String CSV_LOADED = "CSV File Was Loaded";
    public final static String SPECTRA_READY = "Spectra were extracted.";
    public final static String UPDATE_STATS ="Update Statistics";
    public final static String BACKGROUND_READY = "Background was calculated";
    public final static String IMAGES_READY = "Zero and First Order Images Loaded";
    public final static String CALI_LOADED="Calibration File Loaded";
    public final static String PRE_CLOSED = "Preview Window Closed";
    
    PlugIn Test;
    JFrame mainFrame;
    JFrame newCSVFrame;
    JFrame newsSMLMCSVFrame;
    JFrame visFrame;
    JFrame previewFrame;
    JFrame lineFrame;
    JFileChooser fc2;
    JFileChooser fc;
    JPanel mainPanel;
     JFrame  compPlot;
    ArrayList<Blinking> blinkings;
    ArrayList<Blinking> curBlinkings;
    ArrayList<Blinking> currentBEs;
    ArrayList<Blinking> oldBEs;
   // ArrayList<Integer> oldIDs;
    //ArrayList<Integer> curIDs;
    ImagePlus zeroOrderImage;
    ImagePlus firstOrderImage;
    ImagePlus firstOrderImage_bk;
    ImagePlus blinkingImage;
    String currentImageDirectory;
    ImagePlus backgroundImage;
    ImagePlus spectralImage;
    ImagePlus bkImage;
    VisualizationPanel vPanel;
    
    private int cID;

    private int visMethod;
    private int magnification;
    
    private boolean isROI;
    private boolean csvFileLoaded;
    private boolean imgsReady;
    private boolean driftLoaded;
    private boolean is3D;
    private boolean smlmImgLoaded;
    private double zeroth_Pos;
    private String csvFilePath;
    
    private double mx_x;
    private double mx_y;
    private double mx_fr;
    
    private boolean closeFlg;
    private double px_shift;
    private double sp_disp;
    private boolean caliLoaded;
   
   private double[] xD;
   private double[] yD;
   private int fitOrder;
    private int org_X;
    private int org_Y;
    private int org_Wid;
    private int r_Hei;
    private int r_Wid;
    private sSMLMDriftResults driftRes;
    private int[] fy;
    private String postProcessString;
    
    private double px_Size;
    private double adu_cnt;
    private int bs_Lv;
    private int em_GAIN;
    private int[] crp_Pos;
    private int[] org_crp_Pos;
    
    
    private SwingPropertyChangeSupport pcSupport = new SwingPropertyChangeSupport(this);
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.removePropertyChangeListener(listener);
    }
    
    @Override
    public void run(String arg) {
        //IJ.setDebugMode(true);
         IJ.log("Analysis started");
        ImagePlus stack = IJ.getImage();
        FileInfo fi = stack.getOriginalFileInfo(); // need to use original file info or the info is not correct
        
        this.currentImageDirectory = fi.directory;
      
        setupGUI();
    }   
    
    
    /*
        Opens a dialog box asking the user to locate a CSV file
        Reads the CSV file line by line and adds it to a JTable
        Creates a new window for the CSV file.
    */
    
    public void fireUpdateStats(){
         pcSupport.firePropertyChange(Analysis.UPDATE_STATS, false, true);
    }
    
    public String getCurrentImageDirectory(){
        return this.currentImageDirectory;
    }
    
    public double[] loadCali(String fileName,boolean importScreen) throws IOException,FileNotFoundException{
        caliLoaded =false;
        double[] d_prms= null;
        
        ImportCSV im= new ImportCSV();
       
         try{   
        ArrayList<String[]> data=im.importCSV(fileName,this.currentImageDirectory, mainFrame);
        String fName= im.getFileName();
       
        gui.LoadLocalizationsPanel.textfieldCaliFilePath.setText(fName);
       
       if(data!=null&&!data.isEmpty()){
        String[] columnNames = data.get(0);
        boolean flg1=columnNames[0].equals("Pixels");
        boolean flg2 = columnNames[1].equals("Wavelength [nm]");
       
        if(flg1&&flg2){
        String[] init= data.get(1);
       
         ArrayList<Double> xData = new ArrayList<Double>();
         ArrayList<Double> yData = new ArrayList<Double>();
         
          int cfid=-1;
           for(int i =2; i<data.size();i++){
                String[] row=data.get(i);
                if(row[0].equals("Description")){
                    cfid=i+1;
                    break;
                    
                }
                
                yData.add(Double.parseDouble(row[0]));
                System.out.println(row[0]);
                xData.add(Double.parseDouble(row[1]));
                System.out.println(row[1]);
                
            }
           
             String[] cfRow=data.get(cfid);
              fitOrder=(int) Double.parseDouble(cfRow[1]);
             
                xD= ArrayUtils.toPrimitive(xData.toArray(new Double[xData.size()]));
                yD= ArrayUtils.toPrimitive(yData.toArray(new Double[yData.size()]));
                double ival= Double.parseDouble(init[0]);
                int p_sz=2*yData.size();
                d_prms= new double[p_sz];
                
                for(int j=0; j<p_sz;j++){
                    if(j<yData.size()){
            d_prms[j]= xD[j];
                    }else{
                        d_prms[j]=yD[j-yData.size()]-ival;
                    }
        }
        
                 Max mx_Val = new Max();
                double mxy= mx_Val.evaluate(yD,0,yD.length);
                int i_mxy=yData.indexOf(mxy);
                
                 double mxx= xD[i_mxy];
                
                
                Min mn_Val = new Min();
                double mny=mn_Val.evaluate(yD,0,yD.length);
                int i_mny=yData.indexOf(mny);
                
                 double mnx= xD[i_mny];
             
                px_shift=ival;
                sp_disp=((mxx-mnx)/(mxy-mny));
                IJ.log("Disperion: "+sp_disp);
                
                caliLoaded=true;
                pcSupport.firePropertyChange(Analysis.CALI_LOADED, false, true);
        } else{IJ.error("Incorrect File Loaded");}
         }
       
         }catch(NullPointerException e1){
             caliLoaded=false;
             
         }catch(IndexOutOfBoundsException e2){
            caliLoaded=false;
                
            }
         return d_prms;
      
    
    }
    
    
    public void launchPxWvPlot(double[] pxWvs){
        if(lineFrame!=null){
            lineFrame.dispose();
        }
        
        XYSeriesCollection lineData; 
        int v=round(pxWvs.length/2);
        
        ArrayList<Double> xData=new ArrayList<Double>();
        ArrayList<Double> yData = new ArrayList<Double>();
                
        XYSeries cvseries = new XYSeries("Calibration");
       
        for(int i=0;i<v;i++){
            xData.add(pxWvs[i]);
            yData.add(pxWvs[i+v]);     
              cvseries.add(yData.get(i),xData.get(i));
        }
        
        
       int lbnd=400;
       int ubnd=850;
       //set range for prism
        if((fitOrder==1||fitOrder==2)){  
       int mn_v=xData.indexOf(Collections.min(xData)); 
       int mx_v=xData.indexOf(Collections.max(xData));
       lbnd=(int)Math.round(xData.get(mn_v));
       ubnd=(int)Math.round(xData.get(mx_v));
       if(lbnd-20>400){
           lbnd=lbnd-20;
       }
       if(ubnd+20<850){
           ubnd=ubnd+20;
       }
       
        }
        
            double upSam=1;
        //set range of wavelengths
        int d=(ubnd-lbnd)*(int)round(upSam);
             
        double[] waveRng = new double[d];
         for (int i=0; i<(d);i++){
            double init= (double) lbnd;
            double val =(double) i;
           waveRng[i]=init+(val/upSam);           
        }
        
         double[] xDs= ArrayUtils.toPrimitive(xData.toArray(new Double[v]));
        double[] yDs= ArrayUtils.toPrimitive(yData.toArray(new Double[v]));
        
         CurveFitter cali= new CurveFitter(xDs,yDs);
        int fitp=0;
            switch (fitOrder) {
                case 0:
                    cali.doFit(CurveFitter.STRAIGHT_LINE); //For Gratings
                    fitp=1;
                    break;
                case 1:
                    cali.doFit(CurveFitter.POLY2); //For Prisms
                    fitp=2;
                    break;
                case 2:
                    cali.doFit(CurveFitter.POLY3); //For Prisms
                    fitp=3;
                    break;
                default:
                    cali.doFit(CurveFitter.STRAIGHT_LINE); //For Gratings
                    fitp=1;
                    break;
            }
       
        double r2= cali.getRSquared();
      
        double rmse= cali.getSD();
      
        double[] coeffs= cali.getParams();
        
        
         //Evaluate Polynomial at Coefficients
        int[] xi =new int[waveRng.length] ;
        double[] x_new = new double[waveRng.length];
        
         XYSeries cvlinseries = new XYSeries("Fitted");
           
        
        //Wavelength Range
        for(int n=0;n<waveRng.length;n++){
            double pt=waveRng[n];
            x_new[n]=cali.f(coeffs, pt);
           
            
            cvlinseries.add(x_new[n],pt);
                  
            
        }
        
         lineData = new XYSeriesCollection(); 
        lineData.addSeries(cvseries);
        lineData.addSeries(cvlinseries);
        
        
        JFreeChart linePlot = this.createLineChart(lineData);
       
         XYPlot lineCaliPlot = linePlot.getXYPlot();
        lineCaliPlot.setBackgroundPaint(Color.lightGray);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        renderer2.setSeriesLinesVisible(0, false);
        renderer2.setSeriesShapesVisible(0,true);
        Shape cross = ShapeUtils.createDiagonalCross(3, 1);
        
        renderer2.setSeriesShape(0, cross);
        
         renderer2.setSeriesPaint(0,Color.red);
        renderer2.setSeriesPaint(1,Color.black);
 
        
         renderer2.setSeriesLinesVisible(1, true);
        renderer2.setSeriesShapesVisible(1,false);
       
        lineCaliPlot.setRenderer(renderer2);
        
          
        BigDecimal bd = new BigDecimal(r2).setScale(2, RoundingMode.HALF_UP); 
        double nr2 = bd.doubleValue();
        
        BigDecimal bd2 = new BigDecimal(rmse).setScale(2, RoundingMode.HALF_UP); 
        double nrmse = bd2.doubleValue();
        
       String sch1="R-squared: "+Double.toString(nr2);
       String sch2=", RMSE:"+Double.toString(nrmse);
        
        TextTitle subText = new TextTitle(sch1+sch2);
    
        JPanel linePanel=new ChartPanel(linePlot);
        linePanel.setPreferredSize(new Dimension(500,250));
        lineFrame= new JFrame();
        lineFrame.setTitle("Calibration Information");
        lineFrame.getContentPane().add(linePanel);
        lineFrame.setIconImage(IJ.getInstance().getIconImage());
        lineFrame.pack();
        lineFrame.setVisible(true);
        
       
    }
   
     private JFreeChart createLineChart(XYSeriesCollection lineData){
               
      JFreeChart linePlot = ChartFactory.createXYLineChart("Pixels vs Wavelength ",
                "Pixels",
                "Wavelength (nm)",
             
                lineData,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        
        linePlot.setBackgroundPaint(Color.white);
        
        XYPlot caliLinePlot = linePlot.getXYPlot();
        caliLinePlot.setBackgroundPaint(Color.lightGray);   
         NumberAxis rangeAxis = (NumberAxis) caliLinePlot.getRangeAxis();
         NumberAxis domainAxis = (NumberAxis) caliLinePlot.getDomainAxis();
     
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        domainAxis.setAutoRange(true);
        domainAxis.setAutoRangeIncludesZero(false);
      
        return linePlot;

}
      public double[] getCalibrationParams(){
         
            double[] caliVals=new double[2];
            if(this.caliLoaded){
                
            caliVals[0]=sp_disp;
            caliVals[1]=px_shift;
            }else{
               caliVals[0]=0;
               caliVals[1]=0; 
            }
            return caliVals;
        }
    

   public void loadDriftResults(String fname)throws IOException,FileNotFoundException{
       
       ImportDrift drift= new ImportDrift();
       try{
       driftRes=drift.loadResultsFromFile(fname,this.currentImageDirectory, mainFrame);
        String fName= drift.getFileName();
        gui.VisualizationPanel.textfieldDriftFilePath.setText(fName);
       
       driftLoaded=true;
       }catch(NullPointerException e1){
           driftLoaded =false;
       }catch(IndexOutOfBoundsException e2){
            driftLoaded =false;
                
            }
     
            
     }   
      
      
    
    public ArrayList<Blinking> loadCSV(String fileName)throws IOException,FileNotFoundException{
        int fr_col=-1;
        int x_col=-1;
        int y_col=-1;
        int z_col =-1;
        int sig_col=-1;
        int sig1_col=-1;
        int sig2_col=-1;
        int int_col=-1;
        int unc_col=-1;
        
        csvFileLoaded =false;
   
        try{
        ImportCSV im= new ImportCSV();
       
        ArrayList<String[]> data=im.importCSV(fileName,this.currentImageDirectory, mainFrame);
        String fName= im.getFileName();
        gui.LoadLocalizationsPanel.textfieldFilePath.setText(fName);
        if(data!=null&&!data.isEmpty()){
        String[] columnNames = data.get(0);
        IJ.log("Number of Columns "+ columnNames.length);
        for(int k=0;k<columnNames.length;k++){
            String cName= columnNames[k];
            
            if(cName.contains("frame")){
                fr_col=k;
                }
            
            if(cName.contains("x")&&x_col==-1){
                x_col=k;
                }
            
            if(cName.contains("y")&&y_col==-1){
               y_col=k;
                        }
              
            if(cName.contains("z")&&z_col==-1){
                is3D =true;             
                z_col=k;
                       
            }
             
            if(cName.contains("sigma")){
                if(is3D){            
                    if(cName.contains("sigma1")&&sig1_col==-1){
                        sig1_col=k;
                                        }
                    if(cName.contains("sigma2")&&sig2_col==-1){
                        sig2_col=k;
                                       }
            }else{
                    
                if(sig_col==-1){    
                sig_col=k;
                
                  IJ.log("cName: "+cName+" ColNum:"+k+"  "+sig_col);
                }
            }
           }
            
             if(cName.contains("intensity")&&int_col==-1){
                int_col=k;
                        }
             
             if(cName.contains("uncertainty")&&unc_col==-1){
                unc_col=k;
                 } 
            
        }
        
        if(columnNames.length>5){
             
                blinkings = new ArrayList<Blinking>();
                
                csvFileLoaded = true;
         
                ArrayList<Double> frm= new ArrayList<Double>();
                ArrayList<Double> xpos= new ArrayList<Double>();
                ArrayList<Double> ypos= new ArrayList<Double>();
                 ArrayList<Double> zpos= new ArrayList<Double>();
              
                double xp=0;
                double yp=0;
                double zp=0;
                double fr=0;
                double ph_spt=0;
                double sg_spt=0;
                double sg1_spt=0;
                double sg2_spt=0;
                double lcunc=0;
                int counter=0;
                if(is3D){
                   for(int i=1;i<data.size ();i++){ 
                    String[] row=data.get(i); 
                    fr=Double.parseDouble(row[fr_col]);
                    xp=Double.parseDouble(row[x_col]);
                    yp=Double.parseDouble(row[y_col]);
                    zp=Double.parseDouble(row[z_col]);
                    ph_spt=Double.parseDouble(row[int_col]);
                    sg1_spt=Double.parseDouble(row[sig1_col]);
                    sg2_spt=Double.parseDouble(row[sig2_col]);
                    lcunc=Double.parseDouble(row[unc_col]);
                   
    
                    blinkings.add(new Blinking((int) fr, xp, yp,zp, ph_spt,sg1_spt,sg2_spt,lcunc));
                    frm.add(fr);
                    xpos.add(xp);
                    ypos.add(yp);
                    zpos.add(zp);
                   
                  
                   }
                }else{
                for(int i2=1;i2<data.size();i2++){
                 
                    String[] row2=data.get(i2); 
                    fr=Double.parseDouble(row2[fr_col]);
                    xp=Double.parseDouble(row2[x_col]);
                    yp=Double.parseDouble(row2[y_col]);
                    ph_spt=Double.parseDouble(row2[int_col]);
                    sg_spt=Double.parseDouble(row2[sig_col]);
                    lcunc=Double.parseDouble(row2[unc_col]);
                    
                    blinkings.add(new Blinking((int) fr, xp, yp,ph_spt,sg_spt,lcunc));
                    frm.add(fr);
                    xpos.add(xp);
                    ypos.add(yp);
                   
                 
                    }
                    
                }
                       
                //System.out.println(Integer.toString(counter));
                mx_x= Collections.max(xpos);
                //System.out.println(Double.toString(mx_x));
                mx_y=Collections.max(ypos);
                //System.out.println(Double.toString(mx_y));
                mx_fr=Collections.max(frm);
                //System.out.println(Double.toString(mx_fr));
               
               
             pcSupport.firePropertyChange(Analysis.CSV_LOADED, false, true);
           
                }else{
                    IJ.error("Incorrect Column Format");
                }
      
        }
        }
           catch (IOException e){
            System.out.println("Unable to load File");
        }catch(NullPointerException e1){
             csvFileLoaded =false;
        }
        catch(IndexOutOfBoundsException e2){
              csvFileLoaded =false;
        }
        return blinkings;
    }
    
    
    private void launchVisualization(ArrayList<Blinking> BEs, int[] fy, boolean isImport){
                  
         int sz=BEs.size();
        
        ArrayList<Double> centroids= new ArrayList<Double>();
        ArrayList<Double> photons= new ArrayList<Double>();
        ArrayList<float[]> spectra= new ArrayList<float[]>();
       
         Blinking t_bE=BEs.get(0);
         float [] tmp_spec=t_bE.getSpectrum();
         int sp_sz=tmp_spec.length;
        
        float [] sum_spec=new float[sp_sz];
        Arrays.fill(sum_spec,0);
        for( int i=0;i<sz;i++){
            Blinking bE=BEs.get(i);
            double tmp_c=bE.getCentroid();
            double tmp_ph=bE.getPhotons();
        
            float [] t_spec=bE.getSpectrum();
            sum_spec=getSumSpectra(sum_spec,t_spec);
           
            centroids.add(tmp_c);
            photons.add(tmp_ph);
            spectra.add(t_spec);
            
            }  
           
  
        double[] cens= ArrayUtils.toPrimitive(centroids.toArray(new Double[sz]));
        double[]phts= ArrayUtils.toPrimitive(photons.toArray(new Double[sz]));

       
      float[] avg_sp= getAvgSpectra(sum_spec,sz);
      
     
                visFrame = new JFrame("RainbowSTORM Visualization");
                visFrame.setIconImage(IJ.getInstance().getIconImage());
                visFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                              
                JPanel mainVP = new JPanel();
                GridBagLayout mgb = new GridBagLayout();
                mainVP.setLayout(mgb);
                GridBagConstraints mc = new GridBagConstraints();  
                
                mc.anchor=GridBagConstraints.CENTER;
                mc.fill=GridBagConstraints.NONE;
                mc.gridx=1;
                mc.gridy=0;
                mc.weightx=1;
                mc.weighty=1;
                vPanel= new VisualizationPanel(this,isImport,is3D,cens,phts,avg_sp,fy,spectra);
               // mainVP.add(new VisualizationPanel(this,isImport,is3D,cens,phts,avg_sp,fy,spectra),mc);
               mainVP.add(vPanel,mc);
                pcSupport.firePropertyChange(Analysis.UPDATE_STATS, false, true);
                mainVP.validate();
                visFrame.setResizable(false);
                visFrame.getContentPane().add(mainVP);
                visFrame.pack();
                visFrame.setVisible(true);
                visFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                
               
                visFrame.addWindowListener(new WindowAdapter() { 
                    @Override public void windowClosing(WindowEvent e)
                    {
                        closeVisScreen();
                    }
                });
                
              
          
    }
    
    public void closeVisScreen(){
        closeFlg= vPanel.getCloseAll();
        if(closeFlg){
          
            String[] titles= WindowManager.getImageTitles();
            for(int vt=0;vt<titles.length;vt++){
               
                        if(titles[vt].contains("sSMLM")){  
                        IJ.log(titles[vt]);
                        
                       
                        IJ.selectWindow(titles[vt]);
                        IJ.run("Close");
                        
                        }
       
            }
            
          
            //IJ.sel
             String[] titles2= WindowManager.getNonImageTitles();
            for(int yt=0;yt<titles2.length;yt++){
              
                        if(titles2[yt].contains("sSMLM")){    
                           IJ.log(titles2[yt]);  
                            if(titles2[yt].contains("Multi-Channel Plots")){
                            compPlot.dispose();
                        }else{
                           
                        IJ.selectWindow(titles2[yt]);
                       
                        IJ.run("Close");
                            }
                        }
       
            }
            
            
        }else{
        
        visFrame.dispose();
    }
        
    }
    
    
    public float[] getSumSpectra(float[] s_spectra,float[] t_spec){
           
        int ar_sz= s_spectra.length;
      
          for(int i=0;i<ar_sz;i++){
              float cur_val=s_spectra[i]+t_spec[i]; 
              s_spectra[i]=cur_val;
              Double cval=(double) cur_val;
              if(cval.isNaN()||cval.isInfinite()){
                  IJ.log("Error with sum Spec");
              }
              
              
           }
       
        
        return s_spectra;
    }
    
    public float[] getAvgSpectra(float[] spectra,int sz){
        int ar_sz= spectra.length;
        float[] avg_spec = new float[ar_sz];
        
          for(int k=0;k<ar_sz;k++){
               
               float cur_px=spectra[k]/sz;
               avg_spec[k]=cur_px;
               
           }
    
        return avg_spec;
    }
    
    private Double[] mapToDouble(String[] sVals){
        int m_cols=sVals.length;
        Double[] d_nextLine= new Double[m_cols];
        for(int i=0;i<m_cols;i++){
            d_nextLine[i]=Double.parseDouble(sVals[i]);
        }
        
        return d_nextLine;
    }
    
    private float[] mapToFloat(String[] sVals){
        int m_cols=sVals.length;
        float[] f_nextLine= new float[m_cols];
        for(int i=0;i<m_cols;i++){
            f_nextLine[i]=Float.parseFloat(sVals[i]);
        }
        
        return f_nextLine;
    }
    
    
    public boolean getCSVFileLoaded() {
        return csvFileLoaded;
    }
    
    public boolean getCaliFileLoaded() {
        return caliLoaded;
    }
    
    public boolean getImgsReady() {
        return imgsReady;
    }
    
    public String getCSVFilePath() {
        return csvFilePath;
    }
    
    
    public void setVisMethod(int idx){
        visMethod =idx;
    }
    public  int getVisMethod(){
        return visMethod;
    }
    
    public void setMag(int mag){
        IJ.log("Magnification: "+mag);
        magnification = mag;
        
    }
    
    public void setPxSize(double pxSz){
      px_Size=pxSz;
     }
    
    public double getPxSize(){
     return  px_Size;
     }
    
    public void setADU(double ADU){
        adu_cnt =     ADU;
   
    }
    
    public void setbsLv(int bsLv){
      bs_Lv=bsLv;
    }
    
    public void setGain(int EMG){
         em_GAIN=EMG;
    }
    
    public void setCrppos(int[] cropPos){
        
        IJ.log("x:"+cropPos[0]);
        IJ.log("y:"+cropPos[1]);
        IJ.log("width:"+cropPos[2]);
        IJ.log("height:"+cropPos[3]);
        if(cropPos[2]==0||cropPos[3]==0){
            this.setROIflg(true);
            IJ.log("ROI?: "+isROI);
        }
        crp_Pos =cropPos;
    }
    
    
      public int getMag(){
          
           int mag =0;
            if(magnification<=0){  //int mag=1;
                 try{
                  gui.VisSettingsPanel.ftfMag.commitEdit();
                   mag=((Number)gui.VisSettingsPanel.ftfMag.getValue()).intValue();
                 }catch(ParseException e1){
                     IJ.error("Invalid Input");
                 }
            }else{           
            
            mag= magnification;
            }
              IJ.log("Magnification:"+mag);
              if(mag<=0){
                  mag=5;
                  gui.VisSettingsPanel.ftfMag.setValue(mag);
                  IJ.error("Magnification must be positive and non-zero. Default value used.");
              }
              
              
              return mag;
          }
    
    
    
  
    public short[] averageStack() {
        short[] average = null;
        
        if (this.firstOrderImage_bk != null) {
            ImageStack stack = firstOrderImage_bk.getStack();
            int[] sum;
            // takes pixels of one slice
            short[] pixels;
            int dimension = stack.getProcessor(1).getWidth()*stack.getProcessor(1).getHeight();
            
            sum = new int[dimension];
            // get the pixels of each slice in the stack
            for (int i=1; i<=stack.getSize(); i++) {
                pixels = (short[]) stack.getPixels(i);
                // add the value of each pixel an the corresponding position of the sum array
                for (int j=0; j<dimension; j++) {
                    sum[j]+=  pixels[j];
                }
            }
            
            average = new short[dimension];
            
            // divide each entry by the number of slices
            for (int j=0;j<dimension;j++) {
                average[j] = (short) ((sum[j]/stack.getSize()) );
            }
            
            // add the resulting image as new slice
            ImageProcessor ip1 = new ShortProcessor (stack.getProcessor(1).getWidth(),stack.getProcessor(1).getHeight(), average, null); //ShortProcessor ip1 = new ShortProcessor(w,h);
            ImagePlus img1 = new ImagePlus("Average",ip1);
            img1.show();
        }
        
        return average;
    }
    
    /**
     * Calculates the background by averaging through the stack. 
     * Ignores suspected blinkings by thresholding the mean of a row.
     * Probably not the best way. Probably want to calculate an average for each frame.
     * @return a single background image of background averaged through the stack
     */
    public int[][] calculateBackgroundThroughStack() {
        int[][] average = null;
        
        if (this.firstOrderImage_bk != null) {
            ImageStack stack = firstOrderImage_bk.getStack();
            
            // takes pixels of one slice
            int width = stack.getProcessor(1).getWidth();
            int height = stack.getProcessor(1).getHeight();
           
            int[] rowSums = new int[height];
            
            int[][] pixelsSum = new int[width][height];
            int[][] numSlices = new int[width][height];
            int[][] pixels = new int[width][height];
            
            rowSums = new int[height];
            
           
            int threshold = 0;
            
            // loop through each element of the stack
            for (int i=1; i<=stack.getSize(); i++) {
                pixels = stack.getProcessor(i).getIntArray();
                
                // Calculate the sum of each row
                for (int row=0; row<height; row++) {
                    for (int col=0; col<width; col++) {
                        rowSums[row] += pixels[col][row];
                    }
                }
                
                for (int row=0; row<height; row++) {
                    if (rowSums[row]/width < threshold) { // Threshold 
                        for (int col=0; col<width; col++) {
                            pixelsSum[col][row] += pixels[col][row];
                            numSlices[col][row] ++;
                        }
                    }
                }
            }
            
            average = new int[width][height];
            
            // divide each entry by the number of slices
            for (int row=0; row<height; row++) {
                for (int col=0; col<width; col++) {
                    if (numSlices[col][row] > 0) {
                        average[col][row] = pixelsSum[col][row] / numSlices[col][row];
                    }
                }
            }
            
            // add the resulting image as new slice
            ImageProcessor ip1 = new ShortProcessor (width, height); //ShortProcessor ip1 = new ShortProcessor(w,h);
            ip1.setIntArray(average);
            ImagePlus img1 = new ImagePlus("Average",ip1);
            img1.show();
        }
        
        return average;
    }
    
    public int getCurrentEventID(){
       
        return cID;
    }
    
  public void setEvent(int idx){
      cID=idx;
      
  } 
   
    
     public void previewSpectra(int bw, int rng1, int rng2, int stp,int pixelSize, int EMG, double adu,int bsLv, int fitFlg, double specWidth, int yComp, int rmvOL){
        
        cID=0; 
        double[] params = new double[16];
        params[0]=(double)bw;
        params[1]=(double)rng1;
        params[2]=(double) rng2;
        params[3]=(double)stp;
        params[4]=(double)pixelSize;
        //params[5]=sp_disp;
        params[5]=(double) org_X;
        params[6]=(double) org_Y;
        params[7]=(double) org_Wid;
        params[8]=px_shift;
        params[9]=sp_disp;
        params[10]=(double) EMG;
        params[11]=adu;
        params[12]=(double)bsLv;
        params[13]=(double)fitFlg;
        params[14]=specWidth;
        params[15]=yComp;
        ArrayList<Blinking> bEvents = new ArrayList<Blinking>();
        ArrayList<Blinking> preEvents = new ArrayList<Blinking>();
        int lim=10000;
        if(blinkings.size()>=lim){
            preEvents.addAll(blinkings.subList(0,(lim-1)));
        }else{
            preEvents=blinkings;
        }
          if(rmvOL==1){
             
                  bEvents= excludeOverlappingSpe(params,preEvents); 
              }else{
              bEvents= preEvents;
          }
        
        
        int tot =bEvents.size();
        
        
      
        
        if(cID>=0&&cID<tot){
        Blinking nB =bEvents.get(cID);
       // fy=null;
        int[] wvs= nB.setWavelengths(rng1, rng2, stp);
       
        
        ImageProcessor psfIm= nB.getPSF_Image(zeroOrderImage,nB,params);
       ImageProcessor specIm = nB.getSpec_Image(firstOrderImage,nB,params,xD,yD,fitOrder);
       Blinking cBE= nB.getBlinkingInfo(firstOrderImage,backgroundImage,nB, params,xD,yD,fitOrder);
        
        
        
        float[] sm_spec =cBE.getSpectrum();
        double psf_phs =cBE.getPhPsf();
        double psf_unc =cBE.getUnc();
        double spec_phs =cBE.getPhotons();
        double spec_unc =cBE.getUncSPE();
        int cur=cID;
               previewFrame = new JFrame("sSMLM Preview");
                previewFrame.setIconImage(IJ.getInstance().getIconImage());
                              
                JPanel mainVP = new JPanel();
                GridBagLayout mgb = new GridBagLayout();
                mainVP.setLayout(mgb);
                GridBagConstraints mc = new GridBagConstraints();  
                
                mc.anchor=GridBagConstraints.CENTER;
                mc.fill=GridBagConstraints.NONE;
                mc.gridx=1;
                mc.gridy=0;
                mc.weightx=1;
                mc.weighty=1;
            
               
                mainVP.add(new PreviewPanel(this,psfIm,specIm,sm_spec,wvs,psf_phs,psf_unc,spec_phs,spec_unc,cur,tot,bEvents),mc);
                mainVP.validate();
                previewFrame.setResizable(false);
                
                previewFrame.addWindowListener(new WindowAdapter() { 
                    @Override public void windowClosing(WindowEvent e)
                    {
                        pcSupport.firePropertyChange(Analysis.PRE_CLOSED, false, true);
                        e.getWindow().dispose();
                    }
                });
                
                 
                previewFrame.getContentPane().add(mainVP);
                previewFrame.pack();
                previewFrame.setVisible(true);
        }else{
            IJ.error("No Localization found");
        }
               
        
     }
     
     public double[] getParams(){
         
      double[] params = new double[16];
      
      
        try{
                 gui.SpectraExtractionPanel.ftfYShift.commitEdit();
                  gui.SpectraExtractionPanel.ftfBlinkingWidth.commitEdit();
                  gui.SpectraExtractionPanel.ftfSpectraWindowRng1.commitEdit();
                   gui.SpectraExtractionPanel.ftfSpectraWindowRng2.commitEdit();
                   gui.SpectraExtractionPanel.ftfStpSize.commitEdit(); 
                   int ycomp =((Number)gui.SpectraExtractionPanel.ftfYShift.getValue()).intValue();
                 int bw=((Number)gui.SpectraExtractionPanel.ftfBlinkingWidth.getValue()).intValue();
                 
                   int rng1=((Number)gui.SpectraExtractionPanel.ftfSpectraWindowRng1.getValue()).intValue();
                 int rng2=((Number)gui.SpectraExtractionPanel.ftfSpectraWindowRng2.getValue()).intValue();
               
               
                 int stp=((Number) gui.SpectraExtractionPanel.ftfStpSize.getValue()).intValue();
                 
                 gui.SpatialImageParameterPanel.ftfPixSize.commitEdit();
                 gui.SpatialImageParameterPanel.ftfEmGain.commitEdit();
                 gui.SpatialImageParameterPanel.ftfADU.commitEdit();
                 gui.SpatialImageParameterPanel.ftfBsLv.commitEdit();
                 
                 int px_Size=((Number)gui.SpatialImageParameterPanel.ftfPixSize.getValue()).intValue();
                 int EMG=((Number)gui.SpatialImageParameterPanel.ftfEmGain.getValue()).intValue();
                 double adu= ((Number)gui.SpatialImageParameterPanel.ftfADU.getValue()).doubleValue();
                 int bsLv=((Number)gui.SpatialImageParameterPanel.ftfBsLv.getValue()).intValue();
                 int fitFlg=1;
                 double specWidth=0;
                 if(gui.SpectraExtractionPanel.checkboxFitSpecWidth.isSelected()==true&& gui.SpectraExtractionPanel.ftfSpecWidth.isEnabled()==false)
                 {
                     specWidth=1;
                     fitFlg=1;
                 }
                 else
                 {   
                    gui.SpectraExtractionPanel.ftfSpecWidth.commitEdit();
                     specWidth=((Number)gui.SpectraExtractionPanel.ftfSpecWidth.getValue()).doubleValue();
                     fitFlg=0;
                 }
                  boolean flg1 =gui.SpectraExtractionPanel.validRange(rng1,rng2,stp);
                 boolean flg2 = gui.SpectraExtractionPanel.validParams(bw,px_Size,EMG,adu,bsLv,specWidth);
                 
                 if(flg1&&flg2){
 
        int[] wvs= blinkings.get(0).setWavelengths(rng1, rng2, stp);
         
        params[0]=(double)bw;
        params[1]=(double)rng1;
        params[2]=(double) rng2;
        params[3]=(double)stp;
        params[4]=(double)px_Size;
        //params[5]=sp_disp;
        params[5]=(double) org_X;
        params[6]=(double) org_Y;
        params[7]=(double) org_Wid;
        params[8]=px_shift;
        params[9]=sp_disp;
        params[10]=(double) EMG;
        params[11]=adu;
        params[12]=(double)bsLv;
        params[13]=(double)fitFlg;
        params[14]=specWidth;
        params[15]=ycomp;
     }else{
                     if(!flg1)
                     IJ.error("Invalid Spectral Range");
                     if(!flg2)
                     IJ.error("Invalid Paramters");
                 }
                 
                 
              
                  }catch(ParseException e1){
                     IJ.error("Invalid Input");
                 }
                 
                 return params;
         
     }
     
     public ImageProcessor getPSFImg(Blinking nB,double[] params){
         
          
        ImageProcessor psfIm= nB.getPSF_Image(zeroOrderImage,nB,params);
        
        return psfIm;
         
     }
     
      public ImageProcessor getSpecImg(Blinking nB,double[] params){
        
        ImageProcessor specIm = nB.getSpec_Image(firstOrderImage,nB,params,xD,yD,fitOrder);
       
        return specIm;
         
     }
      
    
              
      public Blinking getbEInfo(Blinking nB,double[] params){
          
        Blinking cBE= nB.getBlinkingInfo(firstOrderImage,backgroundImage,nB, params,xD,yD,fitOrder);
        return cBE;
         
     }
     
      public ArrayList<Blinking> getBEs(){
          return blinkings;
      }
             
         public ArrayList<Blinking> getPreList(){
          return blinkings;
      }
     
    public void extractSpectra(int bw, int rng1, int rng2, int stp,int pixelSize, int EMG, double adu,int bsLv, int fitFlg, double specWidth,int yComp,int rmvOL){
        
        mainFrame.setVisible(false);
       
        double[] params = new double[16];
        params[0]=(double)bw;
        params[1]=(double)rng1;
        params[2]=(double) rng2;
        params[3]=(double)stp;
        params[4]=getPixelSize();
        params[5]=(double) org_X;
        params[6]=(double) org_Y;
        params[7]=(double) org_Wid;
        params[8]=px_shift;
        params[9]=sp_disp;
        params[10]=(double) EMG;
        params[11]=adu;
        params[12]=(double)bsLv;
        params[13]=(double)fitFlg;
        params[14]=specWidth;
        params[15]=yComp;
        ArrayList<Blinking> bEvents= new ArrayList<Blinking>();
        bEvents=blinkings;
      
        if(bEvents.size()>0){
        Blinking nB =bEvents.get(0);
        fy=null;
        fy= nB.setWavelengths(rng1, rng2, stp);
        
        ArrayList<Blinking> n_blinkings = new ArrayList<Blinking>();
        
        n_blinkings=nB.addSpectroscopicData(firstOrderImage,backgroundImage, bEvents, params,xD,yD,fitOrder,rmvOL,is3D);
     
        int sz=n_blinkings.size();
       
        ArrayList<Integer> frms= new ArrayList<Integer>();
        ArrayList<Double> xpos= new ArrayList<Double>();
        ArrayList<Double> ypos= new ArrayList<Double>();
        ArrayList<Double> zpos= new ArrayList<Double>();
        ArrayList<Double> photon_spt = new ArrayList<Double>();
        ArrayList<Double> sigma_spt = new ArrayList<Double>();
        ArrayList<Double> sigma_spt2 = new ArrayList<Double>();
        ArrayList<Double> loc_unc =new ArrayList<Double>();
        ArrayList<Double> centroids= new ArrayList<Double>();
        ArrayList<Double> photons= new ArrayList<Double>();
        ArrayList<Double>bk_pnum= new ArrayList<Double>();
        ArrayList<Double> bk_px= new ArrayList<Double>();
        ArrayList<Double> sigma_spe= new ArrayList<Double>();
        ArrayList<Double> unc_spe= new ArrayList<Double>();
        
        ArrayList<float[]> spectra= new ArrayList<float[]>();
      
        double w=1+(2*bw);
        double xth1=w+1;
        double xth2=r_Wid-w-1;
        
        double yth1=w+1;
        double yth2=r_Hei-w-1;
       
        double conv=getPixelSize()+0.5;
        
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<Blinking> n_BEs= new ArrayList<Blinking>();
        int cnt=1;
        for( int i=0;i<sz;i++){
            Blinking bE=n_blinkings.get(i);
            
            Integer tmp_f=bE.getFrame();
            double tmp_x=bE.getXPosition();
            double tmp_y=bE.getYPosition();
            double tmp_z=-1;
            double tmp_stpsig=-1;
            double tmp_stpsig2=-1;
            if(is3D){
                tmp_z=bE.getZPosition();
                tmp_stpsig=bE.getSigma1Psf();
                tmp_stpsig2=bE.getSigma2Psf();
            }else{
                 tmp_stpsig=bE.getSigmaPsf();
            }
            double tmp_stph=bE.getPhPsf();
            
            double tmp_stpunc=bE.getUnc();
            double tmp_c=bE.getCentroid();
            double tmp_ph=bE.getPhotons();
            double tmp_bkph=bE.getBkPhotons();
            double tmp_bkpx=bE.getBkPx();
            double tmp_spesig=bE.getSigmaSPE();
            double tmp_speunc=bE.getUncSPE();
            
            float [] t_spec=bE.getSpectrum();
         
            double txp=((tmp_x)/conv);
                                 
            double typ=((tmp_y)/conv);
            double t_unc=(tmp_stpunc/conv);
          
            boolean flg= isInvalidSpe(txp,typ,t_unc, xth1,xth2, yth1, yth2, tmp_ph, tmp_c,rng1, rng2, tmp_spesig, tmp_speunc);
            
            if (!flg){
        
            Blinking n_be= null;
            if (is3D){
                 n_be=  new Blinking(tmp_f,tmp_x,tmp_y,tmp_z,tmp_stph,tmp_stpsig,tmp_stpsig2,
                    tmp_stpunc,tmp_c,tmp_ph,tmp_bkph,tmp_bkpx,tmp_spesig,tmp_speunc,t_spec);
                 
                 
            }else{
              n_be=  new Blinking(tmp_f,tmp_x,tmp_y,tmp_stph,tmp_stpsig,
                    tmp_stpunc,tmp_c,tmp_ph,tmp_bkph,tmp_bkpx,tmp_spesig,tmp_speunc,t_spec);
            }
            ids.add(cnt);
            n_BEs.add(n_be); 
            frms.add(tmp_f);
       
            
            xpos.add(tmp_x);
            ypos.add(tmp_y);
            
            photon_spt.add(tmp_stph);
            sigma_spt.add(tmp_stpsig);
            loc_unc.add(tmp_stpunc);
            
            centroids.add(tmp_c);
            photons.add(tmp_ph);
            bk_pnum.add(tmp_bkph);
            bk_px.add(tmp_bkpx);
            sigma_spe.add(tmp_spesig);
            unc_spe.add(tmp_speunc);
            
            spectra.add(t_spec);
            cnt++;
          
            }
         
        }
       
         int cn_sz=centroids.size();
         
        double[] xps= ArrayUtils.toPrimitive(xpos.toArray(new Double[cn_sz]));
        double[] yps= ArrayUtils.toPrimitive(ypos.toArray(new Double[cn_sz]));
        double[] cens= ArrayUtils.toPrimitive(centroids.toArray(new Double[cn_sz]));
        double [] uncs= ArrayUtils.toPrimitive(loc_unc.toArray(new Double[cn_sz]));  
       
       
         Max mx_Val = new Max();
         int mx1=(int) round(mx_Val.evaluate(cens,0,cn_sz));
         int mxunc=(int) round(mx_Val.evaluate(uncs,0,cn_sz))+1;
         int xmax=(int) round(mx_Val.evaluate(xps,0,cn_sz))+mxunc;
         int ymax=(int) round(mx_Val.evaluate(yps,0,cn_sz))+mxunc;
        
          Min mn_Val = new Min();
          int mn1=(int) round(mn_Val.evaluate(cens,0,cn_sz));
          int xmin=(int) round(mn_Val.evaluate(xps,0,cn_sz))-mxunc;
          int ymin=(int) round(mn_Val.evaluate(yps,0,cn_sz))-mxunc;
                 
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
          this.drawTCGaussianROI(n_BEs,mn1,mx1,wid,hei,xmin,ymin,mxunc);
        
                  currentBEs=n_BEs;
                  oldBEs =n_BEs;
                  
       
       this.launchVisualization(n_BEs,fy,false);
       
         
       pcSupport.firePropertyChange(Analysis.SPECTRA_READY, false, true);
                    
            String[] titles= WindowManager.getImageTitles();
            for(int vt=0;vt<titles.length;vt++){
                IJ.log(titles[vt]);
                  if(titles[vt]=="Cropped Region 1"){
                        IJ.selectWindow("Cropped Region 1");
                        IJ.run("Close");
       
                     }
                  
                  if(titles[vt]=="Cropped Region 2"){
                        IJ.selectWindow("Cropped Region 2");
                        IJ.run("Close");
                  
                     }
                  
                    if(titles[vt]=="Spectral Domain-Background Image"){
                        IJ.selectWindow("Spectral Domain-Background Image");
                        IJ.run("Close");
                   
                     }
                    
                     if(titles[vt]=="Background-Subtracted Spectral Images"){
                        IJ.log("test");
                       IJ.selectWindow("Background-Subtracted Spectral Images");
                        IJ.run("Close");
                   
                     }
                                
                    if(titles[vt]=="Spatial Image"){
                        IJ.selectWindow("Spatial Image");
                        IJ.run("Close");
                   
                     }
                
                
            }
       
       if(lineFrame!=null){
       lineFrame.dispose();
       }
   
        }else{
            IJ.error("No localizations found");
        }
        
    }
    
    
    
    public boolean isInvalidSpe(double xpos, double ypos,double lc_unc, double xth1, double xth2,  double yth1, double yth2,double photons,double centroid, double rng1, double rng2, double speSig, double speUnc){
        
            boolean flg=true;
        
            Double db= Math.abs(photons);
            boolean pflg1=db.isNaN();
            boolean pflg2= db.isInfinite();
            boolean pflg3= photons<=0;
            boolean pflg=(pflg1||pflg2||pflg3);
            
            Double specPre =Math.abs(speUnc);
            boolean uflg1= specPre.isNaN();
            boolean uflg2=  specPre.isInfinite();
            boolean uflg3= speUnc<=0;
            boolean uflg=(uflg1||uflg2||uflg3);
            
            Double dtc=Math.abs(centroid);
            boolean cflg1 =centroid<rng1;
            boolean cflg2=centroid>rng2;
            boolean cflg3 =dtc.isNaN();
            boolean cflg4 = dtc.isInfinite();
            boolean cflg=(cflg1||cflg2||cflg3||cflg4);
           
            Double sg =Math.abs(speSig);
            boolean sflg1 =speSig<=0;
            boolean sflg2=speSig>=((rng2-rng1)/2);
            boolean sflg3=sg.isNaN();
            boolean sflg4=sg.isInfinite();
            
            boolean sflg=(sflg1||sflg2||sflg3||sflg4);
            
            boolean xp1=(xpos-lc_unc)<xth1;
            
            boolean xp2=(xpos+lc_unc)>xth2;
            
            boolean yp1=(ypos-lc_unc)<yth1;
            boolean yp2=(ypos+lc_unc)>yth2;
            
            boolean unc1= lc_unc<0;
            boolean unc2= lc_unc>xth2||lc_unc>yth2;
            
            boolean lflg= xp1||xp2||yp1||yp2||unc1||unc2;
            
            if (pflg||uflg||cflg||sflg||lflg){
                flg=true;
            }else{
                flg=false;
            }
        
        
        return flg;
    }
    
    public void driftCorrect(){
        
        ArrayList<Blinking> curBEs=currentBEs;
        ArrayList<Double> centroids= new ArrayList<Double>();
        ArrayList<Double> xpos= new ArrayList<Double>();
        ArrayList<Double> ypos= new ArrayList<Double>();
        ArrayList<Double> unc = new ArrayList<Double>();
        
        
         int sz=curBEs.size();
        IJ.log("Current Size:"+sz);
           for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_c=bE.getCentroid();
            double tmp_x=bE.getXPosition();
            double tmp_y=bE.getYPosition();
            double tmp_unc=bE.getUnc();
            
            
            centroids.add(tmp_c);
            xpos.add(tmp_x);
            ypos.add(tmp_y);
            unc.add(tmp_unc);
            
            

            }
         
        double[] cens= ArrayUtils.toPrimitive(centroids.toArray(new Double[sz]));
        double[] xps= ArrayUtils.toPrimitive(xpos.toArray(new Double[sz]));
        double[] yps= ArrayUtils.toPrimitive(ypos.toArray(new Double[sz]));   
        double[] uncs= ArrayUtils.toPrimitive(unc.toArray(new Double[sz]));  
       
       
         Max mx_Val = new Max();
          int mxunc= (int) round(mx_Val.evaluate(uncs,0,sz));
          int mx1=(int) round(mx_Val.evaluate(cens,0,sz))+1;
          int xmax=(int) round(mx_Val.evaluate(xps,0,sz))+mxunc;
          int ymax=(int) round(mx_Val.evaluate(yps,0,sz))+mxunc;
         
         
          Min mn_Val = new Min();
          int mn1=(int) round(mn_Val.evaluate(cens,0,sz))+1;
          int xmin=(int) round(mn_Val.evaluate(xps,0,sz))-mxunc;
          int ymin=(int) round(mn_Val.evaluate(yps,0,sz))-mxunc;
         
       
         double conv=getPixelSize()+0.5;
          //double conv=getPixelSize();//+0.5;
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
        
        
         if (driftLoaded){
         
         double pxs= Double.parseDouble(gui.SpatialImageParameterPanel.ftfPixSize.getText());
         ArrayList<Blinking> n_points=driftRes.applyToResults(driftRes,curBEs,pxs);
          currentBEs=n_points;
          
            if(postProcessString.equals("")){
         postProcessString = postProcessString+" Drift Corrected";
         }else{
             postProcessString = postProcessString+"+ Drift Corrected";
         }
         } else{
                
                  currentBEs=curBEs;
                 
                 }
        
    }
    
   
       
    
      public void applyROI(int[] cropPositions){
         setCrppos(cropPositions);
        ArrayList<Blinking> n_BEs = filterbyROI(cropPositions,currentBEs); 
        
       updateVisData(n_BEs);
    
    }  
       
       public ArrayList<Blinking> filterbyROI(int[] cropPositions,ArrayList<Blinking> curBEs){
        int mag=getMag();
        double pixSz=this.getPxSize()+0.5;
        double res= pixSz/mag; 
        ArrayList<Blinking> n_BEs = new ArrayList<Blinking>();
        ArrayList<Integer> n_ids = new ArrayList<Integer>();
        
        int xthres1=cropPositions[0];
        int ythres1=cropPositions[1];
        
        int xthres2=xthres1+cropPositions[2];
        int ythres2=ythres1+cropPositions[3];
        
        int cnt=0;
        int sz= curBEs.size();
        IJ.log("Old Size:"+sz);
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
           // int id=curIDs.get(i);
            double tmp_x=bE.getXPosition()/res;
            double tmp_y=bE.getYPosition()/res;
            
                        
            boolean flg1 =tmp_x>=xthres1;
            boolean flg2=tmp_x<=xthres2;
            
            boolean flgx= (flg1&&flg2);
            
            boolean flg3 =tmp_y>=ythres1;
            boolean flg4=tmp_y<=ythres2;
            boolean flgy= (flg3&&flg4);
            
            
            boolean flg=(flgx&&flgy);
            if (flg){
           
            n_BEs.add(bE); 
           // n_ids.add(id);
           
            cnt++;
            }
         
            
        }
        int rsz= n_BEs.size();
       
       // curIDs.clear();
        //curIDs=n_ids;
        return n_BEs;
           
           
       }
      
      
       public ArrayList<Blinking> filterbyCW(int cw_Rng1,int cw_Rng2,ArrayList<Blinking> curBEs){
           
        ArrayList<Blinking> n_BEs = new ArrayList<Blinking>();
        
        int sz= curBEs.size();
        IJ.log("Old Size:"+sz);
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getCentroid();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=cw_Rng1;
            boolean flg2=cw<=cw_Rng2;
           if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            }else{
            
            }
       
        }
        return n_BEs;
     
       }
    
       public void createCompImg(ArrayList<int[]> channels,int nActive, boolean showPlots){
        
        ArrayList<Blinking> curBEs=currentBEs;
        ArrayList<Double> xpos= new ArrayList<Double>();
        ArrayList<Double> ypos= new ArrayList<Double>();
        ArrayList<Double> cens1 =new ArrayList<Double>();
        ArrayList<Double> phs1 = new ArrayList<Double>();
        
         int sz=curBEs.size();
             for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            
            double tmp_x=bE.getXPosition();
            double tmp_y=bE.getYPosition();
            double tmp_cen = bE.getCentroid();
            double tmp_ph =bE.getPhotons();
           
            cens1.add(tmp_cen);
            phs1.add(tmp_ph);
            
            
            xpos.add(tmp_x);
            ypos.add(tmp_y);
           
            }
         
            
        double[] xps= ArrayUtils.toPrimitive(xpos.toArray(new Double[sz]));
        double[] yps= ArrayUtils.toPrimitive(ypos.toArray(new Double[sz])); 
        double[] cen1= ArrayUtils.toPrimitive(cens1.toArray(new Double[sz]));
        double[] phts1 =ArrayUtils.toPrimitive(phs1.toArray(new Double[sz]));
      
         Max mx_Val = new Max();
         
          int xmax=(int) round(mx_Val.evaluate(xps,0,sz))+1;
          int ymax=(int) round(mx_Val.evaluate(yps,0,sz))+1;
          int cMax= (int) round(mx_Val.evaluate(cen1,0,sz))+1;
         
         
          Min mn_Val = new Min();
     
          int xmin=(int) round(mn_Val.evaluate(xps,0,sz))-1;
          int ymin=(int) round(mn_Val.evaluate(yps,0,sz))-1;
          int cMin= (int) round(mn_Val.evaluate(cen1,0,sz))+1;
         
          double conv=getPixelSize()+0.5;
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
        
         int mag=getMag();
         int width=0;
         int height=0;
        
                
        if(!isROI){
             
        width=r_Wid*mag;
        height=r_Hei*mag;
         
         if(width<=0){
              width = wid*mag;
         }
         if(height<=0){
          height =hei*mag;
         }
             
         xmin=0;
         ymin=0;
         }else{
          width = wid*mag;
          height =hei*mag;
         }
         
         
         double resolution=conv/mag;
         
        int d_width = (int) ((width ));
        int d_height = (int) ((height));
         
        ImageStack stack = new ImageStack(d_width,d_height); 
         
        int nChs= channels.size();
       
        int cnt=0;
        ImagePlus[] imgs = new ImagePlus[nActive]; 
        Color[] cols = new Color[nActive];
         XYSeriesCollection dataset =new XYSeriesCollection();
         String sName="Centroids";
         dataset.addSeries(drawCenScatterPlot(cen1,phts1,sName));
         HistogramDataset dataset2 =new HistogramDataset();
          dataset2.setType(HistogramType.FREQUENCY);
     
          int bin1 = (int) Math.round((cMax-cMin));
        
         dataset2.addSeries(sName, cen1, bin1);
         
         Color[] sc_col2= new Color[nActive+1];
         Color[] ht_col2= new Color[nActive+1];
         int cnt2=0;
         int cnt1=0;
       
         Color c = Color.gray;
         int r= c.getRed();
         int g= c.getGreen();
         int b = c.getBlue();
         int a= 50;
         Color c2 =new Color(r,g,b,a);
         ht_col2[cnt2]=c2;
          c = Color.lightGray;
         r= c.getRed();
         g= c.getGreen();
         b = c.getBlue();
         a= 20;
         Color c3 =new Color(r,g,b,a);
         
         sc_col2[cnt2]=c3;
         
       
         cnt2=cnt2+1;
         
           
        for(int k=0;k<nChs;k++){
            int [] curCh= channels.get(k);
            int activeCh=curCh[2];
            int showCh= curCh[3];
            ArrayList<Double> cens =new ArrayList<Double>();
            ArrayList<Double> phs = new ArrayList<Double>();
            
            if(activeCh==1){
           
            
            ArrayList<Blinking> chBEs =filterbyCW(curCh[0],curCh[1],curBEs);
            
              int sz2=chBEs.size();
     
           for( int i=0;i<sz2;i++){
            Blinking bE=chBEs.get(i);
            
            
            double tmp_cen = bE.getCentroid();
            double tmp_ph =bE.getPhotons();
           
            cens.add(tmp_cen);
            phs.add(tmp_ph);
           }
           
           
        double[] cen= ArrayUtils.toPrimitive(cens.toArray(new Double[sz2]));
        double[] phts =ArrayUtils.toPrimitive(phs.toArray(new Double[sz2]));
        String sName2="Centroids: "+curCh[0]+":"+curCh[1];
        
       int bin = (int) Math.round((curCh[1]-curCh[0]));
        
        dataset.addSeries(drawCenScatterPlot(cen,phts,sName2));
         dataset2.addSeries(sName2, cen, bin);//,cMin,cMax);
        
        int v =this.getVisMethod();
        ImageProcessor ch;
               
        switch(v){
            case 0:
         ScatterRendering sr = new ScatterRendering(width, height, resolution);
         ch= sr.renderImage(chBEs, xmin, ymin);
         
          break;
            case 1:
         
         GaussianRendering gsr = new GaussianRendering(width,height,resolution);
          ch= gsr.renderImage(chBEs, xmin, ymin);
    
          break;
        
          default:
         ScatterRendering dsr = new ScatterRendering(width, height, resolution);
          ch=dsr.renderImage(chBEs, xmin, ymin);
   
          break;
        }
       
        String sch="sSMLM C"+Integer.toString(cnt+1);
        String t_c;
        Color tmp_c ;
        switch(k){
            case 0: 
                tmp_c=Color.RED;
                t_c =("(red)");
             
                break;
            case 1:
                tmp_c=Color.GREEN;
                t_c =("(green)");
             
             break;
             case 2:
                 tmp_c=Color.BLUE;
                t_c =("(blue)");
               
             break;
               case 3:
                tmp_c=Color.YELLOW;   
                t_c =("(yellow)");
              
             break;
               case 4:
                tmp_c=Color.CYAN;   
                t_c =("(cyan)");
              
             break;
               case 5:
                   tmp_c=Color.MAGENTA;
                t_c =("(magenta)");
              
             break;
            default:
                tmp_c=Color.GRAY;
                t_c =("(gray)");
              
                break;
        }
        
       
        
        sch=sch+t_c;
          if (showCh==1){
           ImagePlus t_im=new ImagePlus(sch,ch);
           LUT lut =LUT.createLutFromColor(tmp_c);
           lut.min=ch.getMin();
           lut.max=ch.getMax();  
           t_im.setLut(lut);
           
           t_im.show();
        }
               
         cols[cnt]=tmp_c;  
      
         
         ht_col2[cnt2]=tmp_c;
         
         sc_col2[cnt2]=tmp_c;
         cnt2=cnt2+1;
         cnt1=cnt1+1;
       
        stack.addSlice(ch);
       
        cnt++;
     
            }
          
            
            
        }
        
      
      ImagePlus fin = new ImagePlus("Multi-Color sSMLM ",stack);
       CompositeImage imgs2= new CompositeImage(fin,IJ.COMPOSITE);
        
       for(int j=0;j<nActive;j++){
       ImageProcessor ip= stack.getProcessor(j+1);
       LUT lut =LUT.createLutFromColor(cols[j]);
       lut.min=ip.getMin();
       lut.max=ip.getMax();  
       
       imgs2.setChannelLut(lut, j+1);
        }
    
     imgs2.show();
     
     if(showPlots){
     
     XYDataset up_cendset=dataset;
     JFreeChart up_cenChart = createCenPhChart(up_cendset,sc_col2);
     up_cenChart.getPlot().setBackgroundPaint(Color.WHITE);
     JFreeChart histChart = createHistChart(dataset2,ht_col2);
     histChart.getPlot().setBackgroundPaint(Color.WHITE);
     ChartPanel cenPanel=new ChartPanel(up_cenChart);
     cenPanel.setPreferredSize(new Dimension(400, 400)); 
     ChartPanel histPanel = new ChartPanel(histChart);
     histPanel.setPreferredSize(new Dimension(400, 400)); 
     JPanel outPanel = new JPanel();
     
       outPanel.setBorder(BorderFactory.createTitledBorder("Composite Image Stats"));
       outPanel.setLayout(new FlowLayout());
       outPanel.add(cenPanel);
       outPanel.add(histPanel);
      
       compPlot = new JFrame("sSMLM Multi-Channel Plots");
       compPlot.setIconImage(IJ.getInstance().getIconImage());
       compPlot.getContentPane().add(outPanel);
       compPlot.setResizable(false);
       compPlot.pack();
       compPlot.setVisible(true);
       WindowManager.addWindow(compPlot);
      
     }  
   
        
        
       }
     
       
       private JFreeChart createHistChart(IntervalXYDataset dataset,Color[] col) { 
           int snum =col.length;
           JFreeChart chart = ChartFactory.createHistogram( "Histogram: Spectral Centroids",
                   "Spectral Centroid [nm]",
                   "Count",
                    dataset,
                   PlotOrientation.VERTICAL, 
                   true, 
                   false, 
                   false );
           XYPlot plot = (XYPlot) chart.getPlot();
           plot.setForegroundAlpha(0.75f); 
          
           
            XYBarRenderer rnd = (XYBarRenderer) plot.getRenderer();
      for(int i=0;i<snum;i++){  
      rnd.setBarPainter(new StandardXYBarPainter());
      rnd.setSeriesPaint(i,col[i]);
     }
       return chart;
       }
       
       
     public XYSeries drawCenScatterPlot(double[] centroids, double[] photons,String sName){
       XYSeries x_series =new XYSeries(sName);
      for(int k=0;k<centroids.length;k++){
           double tmp1=centroids[k];
           double tmp2=photons[k];
        x_series.add(tmp1,tmp2);
       
    }
     
            
     return x_series;
     
        
    }
        
        public JFreeChart createCenPhChart(XYDataset dset,Color[] col){
        int snum=col.length;          
       JFreeChart chart = ChartFactory.createScatterPlot(
                "Centroids Vs Photons",
                "Spectral Centroid [nm]",
                "Spectral Photons",
                dset,PlotOrientation.VERTICAL,true,true,false);
     
      Shape shape  = new Ellipse2D.Double(0,0,1,1);
      XYPlot xyPlot = (XYPlot)    chart.getPlot();
      XYItemRenderer rnd = xyPlot.getRenderer();
      for(int i=0;i<snum;i++){  
      rnd.setSeriesShape(i, shape);
      rnd.setSeriesPaint(i,col[i]);
     }
       
       
       return chart;
        }
         
    
      public XYDataset drawAvgSpecPlot(float[] avgSp, int[] wvs,String slb){
    
       int sz= avgSp.length;
       IJ.log("Array Size:"+sz);
       
       int sz2= wvs.length;
       IJ.log("Wave Array Size:"+sz2);
       
       XYSeriesCollection dataset =new XYSeriesCollection();
       
       XYSeries x_series =new XYSeries(slb);
      
       for(int k=0;k<avgSp.length;k++){
           
           double tmp1=(double)wvs[k];
           double tmp2=(double)avgSp[k];
        
        if(tmp2>0){   
        x_series.add(tmp1,tmp2);
        }
     
    }
       dataset.addSeries(x_series);
       
       XYDataset dset= dataset;
       return dset;
     
        
    }


     public JFreeChart createAvgSpChart(XYDataset dset,String slb){
       
       JFreeChart chart = ChartFactory.createXYLineChart(
               slb,
                "Wavelength (nm)",
                "Intensity a.u.",
                dset,PlotOrientation.VERTICAL,true,true,false);
       return chart;
        }      
      
      
    public void updateVisData(ArrayList<Blinking> curBEs){
          currentBEs=curBEs;  
      
    }
    
    public void resetVisData(){
        int c_sz=currentBEs.size();
        int o_sz1=oldBEs.size();
        currentBEs=oldBEs;
        int o_sz2=currentBEs.size();
        
        // curIDs.clear();
        // curIDs=oldIDs;
        
        IJ.log("Updated Data:"+o_sz2);
        postProcessString="";
        crp_Pos=null;
         crp_Pos=org_crp_Pos;
    }
    
     public ArrayList<Blinking> getCurrentData(){
         int c_sz=currentBEs.size();
         return this.currentBEs;
     
     }
     
       public ArrayList<Blinking> getOriginalData(){
         int o_sz1=oldBEs.size();
         return this.oldBEs;
         }
       
     public ArrayList<Blinking> getSMLMData(){
       return this.blinkings;
           }  
     public int[] getRange(){
       return fy;
        }
  
    public void setDefaults(){
    postProcessString ="";    
    magnification =5;
    visMethod=0;
  
}

    public void saveBlinkingSpectra(String filename) throws FileNotFoundException, IOException {
        // Either load from the last image directory or the current image directory
         ArrayList<Blinking> BEs=currentBEs;
         ExportCSV ex= new ExportCSV();
         int spc_sz=fy.length;
         String[] s_header= new String[spc_sz];
         for(int k=0;k<spc_sz;k++){
             s_header[k]=Integer.toString(fy[k]);
         }
       
        String[] header = {"id","frame", "x [nm]","y [nm]","spatial photons","spatial sigma [nm]","localization uncertainty [nm]",
            "spectral centroid [nm]","spectral photons","spectral bk photons","spectral bk photons/pixel","spectral sigma [nm]","spectral uncertainty [nm]"};
        String [] f_header =ArrayUtils.addAll(header,s_header);
        ArrayList<String[]> data= new ArrayList<String[]>();
        
        ArrayList<String[]> s_data = new ArrayList<String[]>();
        ArrayList<String[]> f_data = new ArrayList<String[]>();
                
        for(int i =0; i<BEs.size();i++){
                Blinking bEvent =BEs.get(i);
                String id_name= Integer.toString(i+1);
                String fr_prm = Double.toString(bEvent.getFrame());
                String x_prm = Double.toString(bEvent.getXPosition());
                String y_prm = Double.toString(bEvent.getYPosition());
                String spt_ph= Double.toString(bEvent.getPhPsf());
                String spt_sig =Double.toString(bEvent.getSigmaPsf());
                String spt_unc =Double.toString(bEvent.getUnc());
                String cen_prm = Double.toString(bEvent.getCentroid());
                String ph_prm = Double.toString(bEvent.getPhotons());
                String bkph_prm = Double.toString(bEvent.getBkPhotons());
                String bkpx_prm = Double.toString(bEvent.getBkPx());
                String spec_sig =Double.toString(bEvent.getSigmaSPE());
                String spec_unc =Double.toString(bEvent.getUncSPE());
                
                String[] prms = {id_name,fr_prm, x_prm, y_prm,spt_ph,spt_sig,spt_unc,cen_prm,ph_prm,bkph_prm,bkpx_prm,spec_sig,spec_unc};
                data.add(prms);
                
                float[] spec=bEvent.getSpectrum();
                String[] s_prms= new String[spc_sz];
                for(int j=0;j<spc_sz;j++){
                    s_prms[j]=Float.toString(spec[j]);
                    }
                s_data.add(s_prms);
                String[] f_prms=ArrayUtils.addAll(prms,s_prms);
                f_data.add(f_prms);
         
       }
        ex.exportCSV(this.currentImageDirectory, header, data);
       
        String fname=ex.getLastFilename();
    
         ex.exportSpectraCSV(fname,f_header,f_data);
    }
    
    
    public void save3DBlinkingSpectra(String filename) throws FileNotFoundException, IOException {
        // Either load from the last image directory or the current image directory
         ArrayList<Blinking> BEs=currentBEs;
         ExportCSV ex= new ExportCSV();
         int spc_sz=fy.length;
         String[] s_header= new String[spc_sz];
         for(int k=0;k<spc_sz;k++){
             s_header[k]=Integer.toString(fy[k]);
         }
        //ArrayList<String> crp_prms= crp.params.toString();
        String[] header = {"id","frame", "x [nm]","y [nm]","z [nm]","spatial photons","spatial sigma1 [nm]","spatial sigma2 [nm]","localization uncertainty [nm]",
           "spectral centroid [nm]","spectral photons","spectral bk photons","spectral bk photons/pixel","spectral sigma [nm]","spectral uncertainty [nm]"};
        String [] f_header =ArrayUtils.addAll(header,s_header);
        ArrayList<String[]> data= new ArrayList<String[]>();
        ArrayList<String[]> s_data = new ArrayList<String[]>();
            ArrayList<String[]> f_data = new ArrayList<String[]>();
                
        for(int i =0; i<BEs.size();i++){
                Blinking bEvent =BEs.get(i);
                String id_name= Integer.toString(i+1);
                String fr_prm = Double.toString(bEvent.getFrame());
                String x_prm = Double.toString(bEvent.getXPosition());
                String y_prm = Double.toString(bEvent.getYPosition());
                String z_prm = Double.toString(bEvent.getZPosition());
                String spt_ph= Double.toString(bEvent.getPhPsf());
                String spt_sig1 =Double.toString(bEvent.getSigma1Psf());
                String spt_sig2 =Double.toString(bEvent.getSigma2Psf());
                String spt_unc =Double.toString(bEvent.getUnc());
                String cen_prm = Double.toString(bEvent.getCentroid());
                String ph_prm = Double.toString(bEvent.getPhotons());
                String bkph_prm = Double.toString(bEvent.getBkPhotons());
                String bkpx_prm = Double.toString(bEvent.getBkPx());
                String spec_sig =Double.toString(bEvent.getSigmaSPE());
                String spec_unc =Double.toString(bEvent.getUncSPE());
                
                String[] prms = {id_name,fr_prm, x_prm, y_prm,z_prm,spt_ph,spt_sig1,spt_sig2,spt_unc,cen_prm,ph_prm,bkph_prm,bkpx_prm,spec_sig,spec_unc};
                data.add(prms);
                
                float[] spec=bEvent.getSpectrum();
                String[] s_prms= new String[spc_sz];
                for(int j=0;j<spc_sz;j++){
                    s_prms[j]=Float.toString(spec[j]);
                    }
                s_data.add(s_prms);
                String[] f_prms=ArrayUtils.addAll(prms,s_prms);
                f_data.add(f_prms);
         
       }
        ex.exportCSV(this.currentImageDirectory, header, data);
       
        String fname=ex.getLastFilename();
        ex.exportSpectraCSV(fname,f_header,f_data);
    }
    
   public void shwHists3D( int selH){
      
       ArrayList<Blinking> curBEs=currentBEs;
        String t_c;
        
            int sz= curBEs.size(); 
      
            switch(selH){
            
            case 0:
                t_c =("sSMLM id");
            
                double[] id = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 
                 id[i]=i+1;//curIDs.get(i);
                 
                }
          FloatProcessor id_histogram =new FloatProcessor(1,sz,id);
          ImagePlus idHist = new ImagePlus(t_c,id_histogram);
     
          HistogramWindow idHistWin =new  HistogramWindow(idHist);
          idHistWin.show();
          
          IJ.log(t_c);
             break;
             case 1:
                t_c =("sSMLM frame");
                double[] fr = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_fr=bE.getFrame();
                 fr[i]= tmp_fr;
                 
                }
          FloatProcessor fr_histogram =new FloatProcessor(1,sz,fr);
          ImagePlus frHist = new ImagePlus(t_c,fr_histogram);
     
          HistogramWindow frHistWin =new  HistogramWindow(frHist);
          frHistWin.show();
          
          IJ.log(t_c);
               
             break;
               case 2:
              
                t_c =("sSMM x");
                   double[] x= new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_x=bE.getXPosition();
                 x[i]= tmp_x;
                 
                }
          FloatProcessor x_histogram =new FloatProcessor(1,sz,x);
          ImagePlus xHist = new ImagePlus(t_c,x_histogram);
     
          HistogramWindow xHistWin =new  HistogramWindow(xHist);
          xHistWin.show();
         
             break;
               case 3:
                 t_c =("sSMLM y");
                    double[] y = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_y=bE.getYPosition();
                 y[i]= tmp_y;
                 
                }
          FloatProcessor y_histogram =new FloatProcessor(1,sz,y);
          ImagePlus yHist = new ImagePlus(t_c,y_histogram);
     
          HistogramWindow yHistWin =new  HistogramWindow(yHist);
          yHistWin.show();
           
             break;
             case 4:
                 t_c =("sSMLM z");
                    double[] z = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_z=bE.getZPosition();
                 z[i]= tmp_z;
                 
                }
          FloatProcessor z_histogram =new FloatProcessor(1,sz,z);
          ImagePlus zHist = new ImagePlus(t_c,z_histogram);
     
          HistogramWindow zHistWin =new  HistogramWindow(zHist);
          zHistWin.show();
   
             break;
             
               case 5:
                
                t_c =("sSMLM spatial_photons");
                
                double[] psfPh = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_psfPh=bE.getPhPsf();
                 psfPh[i]= tmp_psfPh;
                 
                }
          FloatProcessor psfPh_histogram =new FloatProcessor(1,sz,psfPh);
          ImagePlus psfPhHist = new ImagePlus(t_c,psfPh_histogram);
     
          HistogramWindow psfPhHistWin =new  HistogramWindow(psfPhHist);
          psfPhHistWin.show();
         
             break;
              case 6:
                
                t_c =("sSMLM spatial_sig1");
                     double[] psfSig = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_psfSig=bE.getSigma1Psf();
                 psfSig[i]= tmp_psfSig;
                 
                }
          FloatProcessor psfSig_histogram =new FloatProcessor(1,sz,psfSig);
          ImagePlus psfSigHist = new ImagePlus(t_c,psfSig_histogram);
     
          HistogramWindow psfSigHistWin =new  HistogramWindow(psfSigHist);
          psfSigHistWin.show();
        
             break;
              case 7:
                
                t_c =("sSMLM spatial_sig2");
                     double[] psfSig2 = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_psfSig2=bE.getSigma2Psf();
                 psfSig2[i]= tmp_psfSig2;
                 
                }
          FloatProcessor psfSig2_histogram =new FloatProcessor(1,sz,psfSig2);
          ImagePlus psfSigHist2 = new ImagePlus(t_c,psfSig2_histogram);
     
          HistogramWindow psfSigHistWin2 =new  HistogramWindow(psfSigHist2);
          psfSigHistWin2.show();
         
             break;
             
             case 8:
                
                t_c =("sSMLM loc_unc");
                double[] unc = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_unc=bE.getUnc();
                 unc[i]= tmp_unc;
                 
                }
          FloatProcessor unc_histogram =new FloatProcessor(1,sz,unc);
          ImagePlus uncHist = new ImagePlus(t_c,unc_histogram);
     
          HistogramWindow uncHistWin =new  HistogramWindow(uncHist);
          uncHistWin.show();
        
             break;
              case 9:
                
                t_c =("sSMLM spec_centroid");
                double[] cen = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_cen=bE.getCentroid();
                 cen[i]= tmp_cen;
                 
                }
          FloatProcessor cen_histogram =new FloatProcessor(1,sz,cen);
          ImagePlus cenHist = new ImagePlus(t_c,cen_histogram);
     
          HistogramWindow cenHistWin =new  HistogramWindow(cenHist);
          cenHistWin.show();
        
             break;
             
              case 10:
                
                t_c =("sSMLM spec_photons");
                double[] ph= new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_ph=bE.getPhotons();
                 ph[i]= tmp_ph;
                 
                }
          FloatProcessor ph_histogram =new FloatProcessor(1,sz,ph);
          ImagePlus phHist = new ImagePlus(t_c,ph_histogram);
     
          HistogramWindow phHistWin =new  HistogramWindow(phHist);
          phHistWin.show();
        
             break;
              case 11:
                
                t_c =("(sSMLM spec_bkphotons");
                double[] bkPh = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_bkPh=bE.getBkPhotons();
                 bkPh[i]= tmp_bkPh;
                 
                }
          FloatProcessor bkPh_histogram =new FloatProcessor(1,sz,bkPh);
          ImagePlus bkPhHist = new ImagePlus(t_c,bkPh_histogram);
     
          HistogramWindow bkPhHistWin =new  HistogramWindow(bkPhHist);
          bkPhHistWin.show();
         
           
             break;
              case 12:
                
                t_c =("sSMLM spec_bk photons/px");
             
                double[] bkPx = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_bkPx=bE.getBkPx();
                 bkPx[i]= tmp_bkPx;
                 
                }
          FloatProcessor bkPx_histogram =new FloatProcessor(1,sz,bkPx);
          ImagePlus bkPxHist = new ImagePlus(t_c,bkPx_histogram);
     
          HistogramWindow bkPxHistWin =new  HistogramWindow(bkPxHist);
          bkPxHistWin.show();
       
             break;
              case 13:
                
                t_c =("sSMLM spec_sig");
               double[] speSig = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_speSig =bE.getSigmaSPE();
                 speSig [i]= tmp_speSig ;
                 
                }
          FloatProcessor speSig_histogram =new FloatProcessor(1,sz,speSig);
          ImagePlus speSigHist = new ImagePlus(t_c,speSig_histogram);
     
          HistogramWindow speSigHistWin =new  HistogramWindow(speSigHist);
          speSigHistWin.show();
        
             break;
              case 14:
                
                t_c =("sSMLM spec_unc");
                double[] speUnc = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_speUnc=bE.getUncSPE();
                 speUnc[i]= tmp_speUnc;
                 
                }
          FloatProcessor speUnc_histogram =new FloatProcessor(1,sz,speUnc);
          ImagePlus speUncHist = new ImagePlus(t_c,speUnc_histogram);
     
          HistogramWindow speUncHistWin =new  HistogramWindow(speUncHist);
          speUncHistWin.show();
      
             break;
           
            default:
             
                t_c =("");
               IJ.log("Nothing Selected");
                break;
        }
        
    }
        
        
        
        
    public void shwHists2D( int selH){
      
       ArrayList<Blinking> curBEs=currentBEs;
        String t_c;
        
            int sz= curBEs.size(); 
     
           switch(selH){
            
            case 0:
                t_c =("sSMLM id");
            
                double[] id = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 
                 id[i]= i+1;
                 
                }
          FloatProcessor id_histogram =new FloatProcessor(1,sz,id);
          ImagePlus idHist = new ImagePlus(t_c,id_histogram);
     
          HistogramWindow idHistWin =new  HistogramWindow(idHist);
          idHistWin.show();
         
             break;
             case 1:
                t_c =("sSMLM frame");
                double[] fr = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_fr=bE.getFrame();
                 fr[i]= tmp_fr;
                 
                }
          FloatProcessor fr_histogram =new FloatProcessor(1,sz,fr);
          ImagePlus frHist = new ImagePlus(t_c,fr_histogram);
     
          HistogramWindow frHistWin =new  HistogramWindow(frHist);
          frHistWin.show();
       
             break;
               case 2:
              
                t_c =("sSMLM x");
                   double[] x= new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_x=bE.getXPosition();
                 x[i]= tmp_x;
                 
                }
          FloatProcessor x_histogram =new FloatProcessor(1,sz,x);
          ImagePlus xHist = new ImagePlus(t_c,x_histogram);
     
          HistogramWindow xHistWin =new  HistogramWindow(xHist);
          xHistWin.show();
        
             break;
               case 3:
                 t_c =("sSMLM y");
                    double[] y = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_y=bE.getYPosition();
                 y[i]= tmp_y;
                 
                }
          FloatProcessor y_histogram =new FloatProcessor(1,sz,y);
          ImagePlus yHist = new ImagePlus(t_c,y_histogram);
     
          HistogramWindow yHistWin =new  HistogramWindow(yHist);
          yHistWin.show();
       
             break;
               case 4:
                
                t_c =("sSMLM spatial_photons");
                
                double[] psfPh = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_psfPh=bE.getPhPsf();
                 psfPh[i]= tmp_psfPh;
                 
                }
          FloatProcessor psfPh_histogram =new FloatProcessor(1,sz,psfPh);
          ImagePlus psfPhHist = new ImagePlus(t_c,psfPh_histogram);
     
          HistogramWindow psfPhHistWin =new  HistogramWindow(psfPhHist);
          psfPhHistWin.show();
                       
              
             break;
              case 5:
                
                t_c =("sSMLM spatial_sig");
                double[] psfSig = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_psfSig=bE.getSigmaPsf();
                 psfSig[i]= tmp_psfSig;
                 
                }
          FloatProcessor psfSig_histogram =new FloatProcessor(1,sz,psfSig);
          ImagePlus psfSigHist = new ImagePlus(t_c,psfSig_histogram);
     
          HistogramWindow psfSigHistWin =new  HistogramWindow(psfSigHist);
          psfSigHistWin.show();
          
             break;
             case 6:
                
                t_c =("sSMLM loc_unc");
                double[] unc = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_unc=bE.getUnc();
                 unc[i]= tmp_unc;
                 
                }
          FloatProcessor unc_histogram =new FloatProcessor(1,sz,unc);
          ImagePlus uncHist = new ImagePlus(t_c,unc_histogram);
     
          HistogramWindow uncHistWin =new  HistogramWindow(uncHist);
          uncHistWin.show();
        
             break;
              case 7:
                
                t_c =("sSMLM spec_centroid");
                double[] cen = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_cen=bE.getCentroid();
                 cen[i]= tmp_cen;
                 
                }
          FloatProcessor cen_histogram =new FloatProcessor(1,sz,cen);
          ImagePlus cenHist = new ImagePlus(t_c,cen_histogram);
     
          HistogramWindow cenHistWin =new  HistogramWindow(cenHist);
          cenHistWin.show();
       
             break;
             
              case 8:
                
                t_c =("sSMLM spec_photons");
                double[] ph= new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_ph=bE.getPhotons();
                 ph[i]= tmp_ph;
                 
                }
          FloatProcessor ph_histogram =new FloatProcessor(1,sz,ph);
          ImagePlus phHist = new ImagePlus(t_c,ph_histogram);
     
          HistogramWindow phHistWin =new  HistogramWindow(phHist);
          phHistWin.show();
          
             break;
              case 9:
                
                t_c =("sSMLM spec_bkphotons");
                double[] bkPh = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_bkPh=bE.getBkPhotons();
                 bkPh[i]= tmp_bkPh;
                 
                }
          FloatProcessor bkPh_histogram =new FloatProcessor(1,sz,bkPh);
          ImagePlus bkPhHist = new ImagePlus(t_c,bkPh_histogram);
     
          HistogramWindow bkPhHistWin =new  HistogramWindow(bkPhHist);
          bkPhHistWin.show();
                    
             break;
              case 10:
                
                t_c =("sSMLM spec_bk photons/px");
             
                double[] bkPx = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_bkPx=bE.getBkPx();
                 bkPx[i]= tmp_bkPx;
                 
                }
          FloatProcessor bkPx_histogram =new FloatProcessor(1,sz,bkPx);
          ImagePlus bkPxHist = new ImagePlus(t_c,bkPx_histogram);
     
          HistogramWindow bkPxHistWin =new  HistogramWindow(bkPxHist);
          bkPxHistWin.show();
        
             break;
              case 11:
                
                t_c =("sSMLM spec_sig");
               double[] speSig = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_speSig =bE.getSigmaSPE();
                 speSig [i]= tmp_speSig ;
                 
                }
          FloatProcessor speSig_histogram =new FloatProcessor(1,sz,speSig);
          ImagePlus speSigHist = new ImagePlus(t_c,speSig_histogram);
     
          HistogramWindow speSigHistWin =new  HistogramWindow(speSigHist);
          speSigHistWin.show();
          
          //IJ.log(t_c);
             break;
              case 12:
                
                t_c =("sSMLM spec_unc");
                double[] speUnc = new double[sz];
                for( int i=0;i<sz;i++){
                 Blinking bE=curBEs.get(i);
                 double tmp_speUnc=bE.getUncSPE();
                 speUnc[i]= tmp_speUnc;
                 
                }
          FloatProcessor speUnc_histogram =new FloatProcessor(1,sz,speUnc);
          ImagePlus speUncHist = new ImagePlus(t_c,speUnc_histogram);
     
          HistogramWindow speUncHistWin =new  HistogramWindow(speUncHist);
          speUncHistWin.show();
      
             break;
                          
            default:
               
                t_c =("");
               IJ.log("Nothing Selected");
                break;
        }
        
    }
    
    
     public void applyFilter(int selH, double t1, double t2){
        boolean ifrm_flg=false; 
        ArrayList<Blinking> curBEs=currentBEs;
        ArrayList<Blinking> n_BEs = new ArrayList<Blinking>();
        
        //ArrayList<Integer> cIDs = this.curIDs;
        //ArrayList<Integer> n_IDs = new ArrayList<Integer>();
        
        int sz= curBEs.size();
        int cnt=0;
        String t_c;
        String str_units;
   
           switch(selH){
            
            case 0:
                t_c =("id");
                ifrm_flg=true;
                str_units="";
                     cnt=0;
   
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            //int id=cIDs.get(i);
                      
            Double cw= (double) Math.abs(i+1);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           if (flg1&&flg2){
           
            n_BEs.add(bE);  
           // n_IDs.add(id);
           
            cnt++;
            }    
        }
         updateVisData(n_BEs);
         //Update cIds
         
        
                     
          IJ.log(t_c);
             break;
             case 1:
             t_c =("frame");
             str_units="";
             ifrm_flg=true;          
             cnt=0;
   
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getFrame();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
        
           
            }
        
        }
         updateVisData(n_BEs);
              break;
               case 2:
              
                t_c =("x");
                str_units=" nm";
         cnt=0;
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getXPosition();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
        
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
           
            }
         
        }
         updateVisData(n_BEs);
             break;
               case 3:
                 t_c =("y");
                 str_units=" nm";
                 cnt=0;
        
           for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getYPosition();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
        
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
            
            }
      
        }
         updateVisData(n_BEs);
                     
             break;
               case 4:
                
                t_c =("PSF photons");
                str_units="";
                 cnt=0;
                 ifrm_flg=true;  
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getPhPsf();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
          
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
            
            }
         
            
        }
         updateVisData(n_BEs);
            break;
              case 5:
              t_c =("PSF sigma");
              str_units=" nm";
                  cnt=0;
          for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getSigmaPsf();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
            //flg1&&flg2
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
            
           
            }
         
            
        }
         updateVisData(n_BEs);
          IJ.log(t_c);
             break;
             case 6:
                
                t_c =("localization uncertainty");
                str_units=" nm";
                  cnt=0;
        
        IJ.log("Old Size:"+sz);
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getUnc();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
         
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
            
           
            }
         
            
        }
         updateVisData(n_BEs);
          IJ.log(t_c);
             break;
              case 7:
                
                t_c =("spectral centroid");
                str_units=" nm";
                cnt=0;
                ifrm_flg=true; 
        IJ.log("Old Size:"+sz);
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getCentroid();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
             
            }
         
            
        }
         updateVisData(n_BEs);
          break;
             
              case 8:
                
                t_c =("spectral photons");
                str_units="";
                 cnt=0;
                 ifrm_flg=true; 
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getPhotons();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
             
           
            }
         
            
        }
         updateVisData(n_BEs);
         
             break;
              case 9:
                
                t_c =("(spectral background photons");
                str_units="";
                 cnt=0;
                 ifrm_flg=true; 
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getBkPhotons();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
           
            }
         
            
        }
         updateVisData(n_BEs);
        
             break;
           
              case 10:
                
                t_c =("spectral background photons/pixel");
                str_units="";
                ifrm_flg=true; 
                  cnt=0;
        
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getBkPx();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
             
            }
         
            
        }
         updateVisData(n_BEs);
          break;
              case 11:
                
                t_c =("spectral sigma");
                str_units=" nm";
                 cnt=0;
    
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getSigmaSPE();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
             
            }
                    
        }
         updateVisData(n_BEs);
       
             break;
              case 12:
                
                t_c =("spectral uncertainty");
                str_units=" nm";
                cnt=0;
        
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getUncSPE();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
            
           
            }
         
            
        }
         updateVisData(n_BEs);
         
             break;
             
            default:
                t_c =("");
                str_units="";
             
                break;
        }
         String str1 = ""; 
        String str2 = "";   
        if (ifrm_flg){   
         str1 = String.format("%.0f", t1); 
        str2 = String.format("%.0f", t2); 
        }else{
            str1 = String.format("%.2f", t1); 
        str2 = String.format("%.2f", t2); 
        }
   
     String st = t_c+"="+str1+str_units+":"+str2+str_units;
           if(postProcessString.equals("")){
         postProcessString = postProcessString+st;
         }else{
             postProcessString = postProcessString+"+ "+st;
         }
        
    }
    
     public void applyFilter3D(int selH, double t1, double t2){
        
        ArrayList<Blinking> curBEs=currentBEs;        
        ArrayList<Blinking> n_BEs = new ArrayList<Blinking>();
       // ArrayList<Integer> cIDs = this.curIDs;
        //ArrayList<Integer> n_IDs = new ArrayList<Integer>();
        int sz= curBEs.size();
        int cnt=0;
        String t_c;
        String str_units;
   
           switch(selH){
            
            case 0:
            t_c =("id");
            str_units="";
            cnt=0;
   
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            //int id=cIDs.get(i);
            //double tmp_cw=bE.getFrame();
           
            Double cw= (double) Math.abs(i+1);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           if (flg1&&flg2){
           
            n_BEs.add(bE);  
           // n_IDs.add(id);
           
            cnt++;
            }else{
                      
            }
                   
        }
         updateVisData(n_BEs);
         
        
             break;
             case 1:
             t_c =("frame");
             str_units="";      
             cnt=0;
   
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getFrame();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
                      
            }
                   
        }
         updateVisData(n_BEs);
         
             break;
               case 2:
              str_units=" nm";
                t_c =("x");
         cnt=0;
      
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getXPosition();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
          
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
            
           
            }
         
            
        }
         updateVisData(n_BEs);
          
             break;
               case 3:
                 t_c =("y");
                 str_units=" nm";
                    cnt=0;
        
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getYPosition();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
               n_BEs.add(bE);    
           
            cnt++;
            }else{
          
            }
        
        }
         updateVisData(n_BEs);
         
             break;
             case 4:
                 t_c =("z");
                 str_units=" nm";
                    cnt=0;
        
        IJ.log("Old Size:"+sz);
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getZPosition();
           
            Double cw= tmp_cw;
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
          
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
           
            }
         
        }
         updateVisData(n_BEs);
         
             break;
               case 5:
                 t_c =("PSF photons");
                 str_units="";
                 cnt=0;
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            double tmp_cw=bE.getPhPsf();
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
          
            }
         
            
        }
         updateVisData(n_BEs);
         
             break;
              case 6:
                
                t_c =("PSF sigma1");
                str_units=" nm";
                  cnt=0;
        
        IJ.log("Old Size:"+sz);
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getSigma1Psf();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }else{
            
            }
         
            
        }
         updateVisData(n_BEs);
          
             break;
               case 7:
                
                t_c =("PSF sigma2");
                str_units=" nm";
                  cnt=0;
        
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getSigma2Psf();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
            
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }
            
        }
         updateVisData(n_BEs);
       
             break;
             case 8:
                
                t_c =("localization uncertainty");
                str_units=" nm";
                  cnt=0;
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            double tmp_cw=bE.getUnc();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }
        }
         updateVisData(n_BEs);
         
             break;
              case 9:
                
                t_c =("spectral centroid");
                str_units=" nm";
                  cnt=0;
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getCentroid();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }
            
        }
         updateVisData(n_BEs);
        
             break;
             
              case 10:
                
                t_c =("spectral photons");
                str_units="";
                 cnt=0;
                
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getPhotons();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }
            
        }
         updateVisData(n_BEs);
         
             break;
              case 11:
                
                t_c =("(spectral background photons");
                str_units="";
                 cnt=0;
       
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getBkPhotons();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }
        }
         updateVisData(n_BEs);
        
             break;
           
              case 12:
                
                t_c =("spectral background photons/pixel");
                str_units="";
             
                  cnt=0;
        
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getBkPx();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
          
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }
        }
         updateVisData(n_BEs);
         
             break;
              case 13:
                
                t_c =("spectral sigma");
                str_units=" nm";
                cnt=0;
     
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getSigmaSPE();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
          
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }        
        }
         updateVisData(n_BEs);
         
             break;
              case 14:
                t_c =("spectral uncertainty");
                str_units=" nm";
                cnt=0;
             for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_cw=bE.getUncSPE();
           
            Double cw= Math.abs(tmp_cw);
            boolean flg1 =cw>=t1;
            boolean flg2=cw<=t2;
           
            if (flg1&&flg2){
           
            n_BEs.add(bE);    
           
            cnt++;
            }         
            
        }
         updateVisData(n_BEs);
            break;
        
            default:
              
                t_c =("");
                str_units="";
               IJ.log("Nothing Selected");
                break;
        }
           
            
      String st = t_c+"="+Double.toString(t1)+str_units+":"+Double.toString(t2)+str_units;
           if(postProcessString.equals("")){
         postProcessString = postProcessString+st;
         }else{
             postProcessString = postProcessString+"+ "+st;
         }
        
    }
    
    
    
    public ArrayList<float[]> loadBlinkingSpectra(String filename) throws FileNotFoundException, IOException {
       csvFileLoaded =false;
        boolean oldflg=csvFileLoaded;
        ArrayList<float[]> spc_data= new ArrayList<float[]>();
        try{
        ImportCSV im= new ImportCSV();
         csvFileLoaded = true;
      
        int counter=0;
        ArrayList<String[]> data=im.importCSV(filename,this.currentImageDirectory, mainFrame);
        String[] columnNames = data.get(0);
        
        int spc_sz= columnNames.length;
        int[] wvs=new int[spc_sz];
         for(int k=0;k<spc_sz;k++){
             
             wvs[k]=(int) round(Double.parseDouble(columnNames[k]));
         }
         fy=null;
         fy=wvs;
       
                for(int i=1;i<data.size();i++){
                    
                    String[] row=data.get(i);  
                  
                    float[] f_nextLine= new float[row.length];
                    f_nextLine=mapToFloat(row);
                    
                    spc_data.add(f_nextLine);
                    
                    counter++;
                    
                }
          
              pcSupport.firePropertyChange(Analysis.CSV_LOADED, false, true);
            
        }catch (IOException e){
            System.out.println("Unable to load File");
        }catch(NullPointerException e1){
             csvFileLoaded =false;
        }
     
        return spc_data;
    }
 
    public void displayLoadedData(ArrayList<Blinking> n_BEs){
        
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ArrayList<Integer> frms= new ArrayList<Integer>();
        ArrayList<Double> xpos= new ArrayList<Double>();
        ArrayList<Double> ypos= new ArrayList<Double>();
        ArrayList<Double> zpos= new ArrayList<Double>();
        ArrayList<Double> photon_spt = new ArrayList<Double>();
         ArrayList<Double> sigma_spt = new ArrayList<Double>();
        ArrayList<Double> sigma_spt2 = new ArrayList<Double>();
        ArrayList<Double> loc_unc =new ArrayList<Double>();
        ArrayList<Double> centroids= new ArrayList<Double>();
        ArrayList<Double> photons= new ArrayList<Double>();
        ArrayList<Double>bk_pnum= new ArrayList<Double>();
        ArrayList<Double> bk_px= new ArrayList<Double>();
        ArrayList<Double> sigma_spe= new ArrayList<Double>();
        ArrayList<Double> unc_spe= new ArrayList<Double>();
        
        ArrayList<float[]> spectra= new ArrayList<float[]>();
       
        int sz=n_BEs.size();
        int sp_sz=0;
        int cnt=0;
      
        for( int i=0;i<sz;i++){
            Blinking bE=n_BEs.get(i);
            int id =i+1;
            float [] t_spec=bE.getSpectrum();
             Integer tmp_f=bE.getFrame();
            double tmp_x=bE.getXPosition();
            double tmp_y=bE.getYPosition();
            double tmp_stph=bE.getPhPsf();
             double tmp_z=-1;
            double tmp_stpsig=-1;
            double tmp_stpsig2=-1;
             if(is3D){
                tmp_z=bE.getZPosition();
                tmp_stpsig=bE.getSigma1Psf();
                tmp_stpsig2=bE.getSigma2Psf();
            }else{
                 tmp_stpsig=bE.getSigmaPsf();
            }
       
            double tmp_stpunc=bE.getUnc();
            double tmp_c=bE.getCentroid();
            double tmp_ph=bE.getPhotons();
            double tmp_bkph=bE.getBkPhotons();
            double tmp_bkpx=bE.getBkPx();
            double tmp_spesig=bE.getSigmaSPE();
            double tmp_speunc=bE.getUncSPE();
          
            sp_sz=t_spec.length;
            ids.add(id);
            frms.add(tmp_f);
            xpos.add(tmp_x);
            ypos.add(tmp_y);
            photon_spt.add(tmp_stph);
            sigma_spt.add(tmp_stpsig);
            loc_unc.add(tmp_stpunc);
            centroids.add(tmp_c);
            photons.add(tmp_ph);
            bk_px.add(tmp_bkpx);
            bk_pnum.add(tmp_bkph);
            sigma_spe.add(tmp_spesig);
            unc_spe.add(tmp_speunc);
            spectra.add(t_spec);
            
            cnt++;
         
        }
      
         int cn_sz=centroids.size();
        
        double[] xps= ArrayUtils.toPrimitive(xpos.toArray(new Double[cn_sz]));
        double[] yps= ArrayUtils.toPrimitive(ypos.toArray(new Double[cn_sz]));   
        double[] cens= ArrayUtils.toPrimitive(centroids.toArray(new Double[cn_sz]));
      
        double[] unc_spec =ArrayUtils.toPrimitive(unc_spe.toArray(new Double[cn_sz]));
     
      Max mx_Val = new Max();
      int mx1=(int) round(mx_Val.evaluate(cens,0,cn_sz));
      int mxunc=(int) round(mx_Val.evaluate(unc_spec,0,cn_sz))+1;
         
      int xmax=(int) round(mx_Val.evaluate(xps,0,cn_sz))+mxunc;
      int ymax=(int) round(mx_Val.evaluate(yps,0,cn_sz))+mxunc;
       
          Min mn_Val = new Min();
          int mn1=(int) round(mn_Val.evaluate(cens,0,cn_sz));
         
          int xmin=(int) round(mn_Val.evaluate(xps,0,cn_sz))-mxunc;
          int ymin=(int) round(mn_Val.evaluate(yps,0,cn_sz))-mxunc;
           double conv=this.getPixelSize()+0.5;
        
         
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
       
         
                 IJ.showStatus("Rendering Image...");
              
                 this.drawTCGaussianROI(n_BEs,mn1,mx1,wid,hei,xmin,ymin,mxunc);
                
                  currentBEs=n_BEs;
                  oldBEs =n_BEs;
                  r_Wid=wid;
                  r_Hei=hei;
              
                
                
       IJ.showStatus("Launching Visualization Screen...");
     
       this.launchVisualization(n_BEs,fy,true);
    
    }
    
  
    
    
    public ArrayList<Blinking> loadBlinkingSpectraData(String filename) throws FileNotFoundException, IOException {
         is3D =false; 
        int id_col=-1;  
        int fr_col=-1;
        int x_col=-1;
        int y_col=-1;
        int z_col =-1;
        int sig_col=-1;
        int sig1_col=-1;
        int sig2_col=-1;
        int int_col=-1;
        int unc_col=-1;
        int cen_col=-1;
        int phs_col=-1;
        int bkphs_col=-1;
        int bkpxs_col=-1;
        int specsig_col=-1;
        int speunc_col=-1;
  
       csvFileLoaded =false;
        boolean oldflg=csvFileLoaded;
          int counter=0;
        try{
        ImportCSV im= new ImportCSV();
         csvFileLoaded = true;
        
        ArrayList<String[]> data=im.importCSV(filename,this.currentImageDirectory, mainFrame);
              String fName= im.getFileName();
        
          gui.ImportsSMLMPanel.textfieldLoadSpectraDataFilename.setText(fName); 
         currentImageDirectory =  im.getDirName();
        
          if(data!=null&&!data.isEmpty()){
        String[] columnNames = data.get(0);
       
        for(int k=0;k<columnNames.length;k++){
            String cName= columnNames[k];
            
              
            if(cName.contains("id")){
                id_col=k;
             
            }
            
            
            if(cName.contains("frame")){
                fr_col=k;
             
            }
            
            if(cName.contains("x [")&&x_col==-1){
                x_col=k;
               
            }
            
            if(cName.contains("y [")&&y_col==-1){
                
                y_col=k;
               
            }
              
            if(cName.contains("z [")&&z_col==-1){
                is3D =true;             
                z_col=k;
              
            }
               
            if(cName.contains("sigma")){
                if(is3D){            
                    if(cName.contains("sigma1")&&sig1_col==-1){
                        sig1_col=k;
                       
                }
                    if(cName.contains("sigma2")&&sig2_col==-1){
                        sig2_col=k;
                         
                }
                    
                 if(cName.contains("spectral")&&specsig_col==-1){
                    specsig_col=k;
                }   
            }else{
                    
                if(cName.contains("spectral")&&specsig_col==-1){
                    specsig_col=k;
                  
                }else{
                sig_col=k;
                
                }
            
            }
           }
            if(cName.contains("photons")){
             
                 if(cName.contains("spatial")&&int_col==-1){
                int_col=k;
              
            }
                  if(cName.contains("spectral photons")&&phs_col==-1){
                phs_col=k;
               
            }
             if(cName.contains("bk photons")&&bkphs_col==-1){
                bkphs_col=k;
            
            }       
            
            if(cName.contains("pixel")&&bkpxs_col==-1){
                bkpxs_col=k;
                
               }  
             }
             
            if(cName.contains("centroid")&&cen_col==-1){
                cen_col=k;
                           }
           
             if(cName.contains("uncertainty")){ 
                 
             if(cName.contains("spectral")&&speunc_col==-1){
              
                speunc_col=k;
               
            }
             if(cName.contains("localization")&&unc_col==-1){
                   unc_col=k;
                  }
             }
        
        }
        
        int nFields=-1;
        if(is3D){
            nFields=15;
        }else{
            nFields=13;
        }
     
                blinkings = new ArrayList<Blinking>();
             
                 int nCols= columnNames.length;
                 int spc_sz=nCols-nFields; 
               
        int[] wvs=new int[spc_sz];
        
        int cnt=0;
         for(int k=nFields;k<nCols;k++){
             
             wvs[cnt]=(int) round(Double.parseDouble(columnNames[k]));
          
              cnt++;
         }
      
         fy=wvs;
         
         if(columnNames.length>nFields){
             
                blinkings = new ArrayList<Blinking>();
          
                csvFileLoaded = true;
                ArrayList<Integer> ids = new ArrayList<Integer>();
                ArrayList<Double> frm= new ArrayList<Double>();
                ArrayList<Double> xpos= new ArrayList<Double>();
                ArrayList<Double> ypos= new ArrayList<Double>();
                ArrayList<Double> zpos= new ArrayList<Double>();
                ArrayList<Double> photon_spt = new ArrayList<Double>();
                ArrayList<Double> sigma_spt = new ArrayList<Double>();
                ArrayList<Double> sigma1_spt = new ArrayList<Double>();
                ArrayList<Double> sigma2_spt = new ArrayList<Double>();
                ArrayList<Double> loc_unc =new ArrayList<Double>();
                
                ArrayList<Double> cens= new ArrayList<Double>();
                ArrayList<Double> phs= new ArrayList<Double>();
                ArrayList<Double> bkphs= new ArrayList<Double>();
                ArrayList<Double> bkpxs= new ArrayList<Double>();
                ArrayList<Double> specsig= new ArrayList<Double>();
                ArrayList<Double> specunc= new ArrayList<Double>();
                
                ArrayList<Double[]> d_data= new ArrayList<Double[]>();
                double id=0;
                double xp=0;
                double yp=0;
                double zp=0;
                double fr=0;
                double ph_spt=0;
                double sg_spt=0;
                double sg1_spt=0;
                double sg2_spt=0;
                double lcunc=0;
                 double cen=0;
                double ph=0;
                double bkph=0;
                double bkpx=0;
                double spec_sig=0;
                double spec_unc=0;
                 int sz =data.size();
              
                if(is3D){
             
                   for(int i1=1;i1<sz;i1++){ 
                     
                    IJ.log("i: "+i1);  
                      
                    String[] row=data.get(i1); 
                    
                    id =Double.parseDouble(row[id_col]);
                    fr=Double.parseDouble(row[fr_col]);
                   
                    xp=Double.parseDouble(row[x_col]);
                    
                    yp=Double.parseDouble(row[y_col]);
                   
                    zp=Double.parseDouble(row[z_col]);
                   
                    ph_spt=Double.parseDouble(row[int_col]);
                  
                    sg1_spt=Double.parseDouble(row[sig1_col]);
                    
                    sg2_spt=Double.parseDouble(row[sig2_col]);
                 
                    lcunc=Double.parseDouble(row[unc_col]); 
                    
                    cen=Double.parseDouble(row[cen_col]);
                 
                    ph=Double.parseDouble(row[phs_col]);
                   
                    bkph=Double.parseDouble(row[bkphs_col]);
                  
                    bkpx=Double.parseDouble(row[bkpxs_col]);
                   
                    spec_sig=Double.parseDouble(row[specsig_col]);
                  
                    spec_unc=Double.parseDouble(row[speunc_col]);
                   
                     int cnt1=0;
                     float[] tspec = new float[spc_sz];
                     for(int j=nFields;j<nCols;j++){
                   
                      float tmp =(float)Double.parseDouble(row[j]);
                      tspec[cnt1]= tmp;
                      
                       cnt1++;
                        }
                   
                    blinkings.add(new Blinking((int) fr, xp, yp,zp, ph_spt,sg1_spt,sg2_spt,lcunc,cen,ph,bkph,bkpx,spec_sig,spec_unc,tspec));
                    // curIDs.add((int)id);
                    //oldIDs.add((int)id);
                    frm.add(fr);
                    xpos.add(xp);
                    ypos.add(yp);
                    zpos.add(zp);
                    photon_spt.add(ph_spt);
                    sigma1_spt.add(sg1_spt);
                    sigma2_spt.add(sg2_spt);
                    loc_unc.add(lcunc);
                 
                    counter++;
                  
                   }
                   
                }else{
                for(int i2=1;i2<sz;i2++){
                    
                    String[] row=data.get(i2); 
                    id =Double.parseDouble(row[id_col]);
                    fr=Double.parseDouble(row[fr_col]);
                    xp=Double.parseDouble(row[x_col]);
                    yp=Double.parseDouble(row[y_col]);
                    ph_spt=Double.parseDouble(row[int_col]);
                    sg_spt=Double.parseDouble(row[sig_col]);
                    lcunc=Double.parseDouble(row[unc_col]);
                    cen=Double.parseDouble(row[cen_col]);
                    ph=Double.parseDouble(row[phs_col]);
                    bkph=Double.parseDouble(row[bkphs_col]);
                    bkpx=Double.parseDouble(row[bkpxs_col]);
                    spec_sig=Double.parseDouble(row[specsig_col]);
                    spec_unc=Double.parseDouble(row[speunc_col]);
                    float[] tspec = new float[spc_sz];
                     int cnt1=0;
                     for(int j=nFields;j<nCols;j++){
                      float tmp =(float)Double.parseDouble(row[j]);
                      tspec[cnt1]= tmp;
                       cnt1++;
                        }
                    
                    Double[] d_nextLine= new Double[row.length];
                    d_nextLine=mapToDouble(row);
                    
                    d_data.add(d_nextLine);
                    blinkings.add(new Blinking((int) fr, xp, yp,ph_spt,sg_spt,lcunc,cen,ph,bkph,bkpx,spec_sig,spec_unc,tspec));
                    //curIDs.add((int)id);
                    //oldIDs.add((int)id);
                    frm.add(fr);
                    xpos.add(xp);
                    ypos.add(yp);
                    photon_spt.add(ph_spt);
                    sigma_spt.add(sg_spt);
                    loc_unc.add(lcunc);
                  
                    counter++;
                    }
                    
                }
         }
                
               
          }
       
        }catch (IOException e){
            System.out.println("Unable to load File");
        }catch(IndexOutOfBoundsException e1){
            System.out.println("Unable to load row"+counter);
        }
      
        return blinkings;
    }
    
       public void applyGlobalBkSub(double avg_bk) {
        
        if(imgsReady){
           int wns=0;
           
            String[] titles= WindowManager.getImageTitles();
            for(int vt=0;vt<titles.length;vt++){
              
                
                    if(titles[vt]=="Spectral Domain-Background Image"){
                    IJ.selectWindow("Spectral Domain-Background Image");
                    IJ.run("Close");
                    wns++;
                     }
                    
                    if(titles[vt]=="Background-Subtracted Spectral Images"){
                    IJ.selectWindow("Background-Subtracted Spectral Images");
                    IJ.run("Close");
                    wns++;
                     }
                    if(wns==2){
                        imgsReady=false;
                        break;
                    }
                
            }
          }   
           
          
        /*if(avg_bk<0){
            IJ.log("Auto bk Sub");
        }else
        {
            IJ.log("Global Bk");
        }*/
       
        this.backgroundImage = null;
        this.firstOrderImage=null;
             boolean wnOpen=false;
     String[] imtitles= WindowManager.getImageTitles();
            for(int ut=0;ut<imtitles.length;ut++){
                if(imtitles[ut].contains("Cropped Region 2")){
                   wnOpen=true;
                   break;
                     }
            }
                
      if(wnOpen){    
       
        int sz1= firstOrderImage_bk.getWidth();
        int mid1=Math.round(sz1/2);
        int sz0=zeroOrderImage.getWidth();
        int mid0=Math.round(sz0/2);
        int d_shf=3;
        int rng1=0;
        int rng2=0;
        if(sz1>sz0)
        {
            d_shf=mid0-1;
            rng1=mid1-d_shf;
            rng2=mid1+d_shf;
        }else if(sz1<sz0 && sz1>d_shf)
        { 
            rng1=mid1-d_shf;
            rng2=mid1+d_shf;
            
        }
        else{
            IJ.error("Image doesn't meet the requirements for Automatic Subtraction");
        }
       
        int crp_wid= rng2-rng1;
     
       int crp_h=firstOrderImage_bk.getHeight();
       
        ImageStack tmp1=firstOrderImage_bk.getStack();
        int sz=tmp1.getSize();
      
         ArrayList<float[]> bkPixel=new ArrayList<float[]>();
        
        for(int n=0; n<crp_h;n++){
          
            bkPixel.add(getRwThres(tmp1,n,rng1,crp_wid,avg_bk));
          
        }
      
           
         float[][] bkPixelArray= new float[sz1][crp_h]; 
         bkPixelArray=arrayListTo2DFloatArray(bkPixel,sz1,crp_h);
         FloatProcessor fp = new FloatProcessor(bkPixelArray);
        
        ImagePlus result = new ImagePlus("Spectral Domain-Background Image", fp);
        result.show();
        this.backgroundImage=result;
        BackgroundSubtraction bk = new BackgroundSubtraction();
        ImagePlus bk_sub =bk.process(firstOrderImage_bk, backgroundImage);
        bk_sub.setTitle("Background-Subtracted Spectral Images");
        bk_sub.show();
        this.firstOrderImage = bk_sub;
        imgsReady=true;
          pcSupport.firePropertyChange(Analysis.BACKGROUND_READY, false, true);
      }else{
          imgsReady=false;
          IJ.error("Cropped images need to be open for background subtraction.");
          
      }       
        
    }
    
    public float[][] arrayListTo2DFloatArray(ArrayList<float[]> lst,int wid,int hgh)
    {   
        
        float[][] fp = new float[wid][hgh];
        for(int i=0;i<hgh;i++){
           
            float[] tmp=lst.get(i);
            for(int j=0;j<wid;j++){
               
                float tmp1=tmp[j];
                fp[j][i]=tmp1;
              
            }
        }
        return fp;
    }
    
    
    
     public String getpostProcessString(){
     return postProcessString;
     }
     
 public double[] getStats(){
   
      ArrayList<Blinking> curBEs=currentBEs;
      ArrayList<Blinking> oBEs=oldBEs;
      ArrayList<Blinking> BEs=blinkings;
      
        double specPre =0;
        double specPhns=0;
        double sSMLM_sz=oBEs.size();
        double SMLM_sz=BEs.size();
        
        double sz=curBEs.size();
       
           for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
            
            double tmp_c=bE.getUncSPE();
            double tmp_ph=bE.getPhotons();
      
            specPre=specPre+tmp_c;
            specPhns=specPhns+tmp_ph;
       
            }
         
           
        double avg_spePh= 0;
        double avg_spePre= 0;
        
        if (sz>0){
         if (specPhns>0 ){  
        avg_spePh= specPhns/sz;
         }
        if (specPre>0){ 
        avg_spePre= specPre/sz;
        }
        }
       
        double stats[] = new double[5];
        stats[0]=avg_spePh;
        stats[1]=avg_spePre;
        stats[2]=sz;
        stats[3]=(sz/sSMLM_sz)*100;
        stats[4]=(sz/SMLM_sz)*100;
        
        return stats;
     
 }
 
    public float[] getRwThres(ImageStack img,int y, int x,int n_cols,double threshold){
        int sz=img.getSize();
        
        double res=0;
        double[] res2=new double[sz];
        int cnt=0;
        int cnt2=0;
        int m_rng=(x+n_cols);
     
                  for(int n=0;n<sz;n++){
                      
                      for(int k=x;k<m_rng;k++){
                     
                        double tmp= img.getVoxel(k, y, n);
                     
                        res=res+tmp;
                        cnt=k;
                    
            }
               double avg= res/m_rng;
           
               res2[cnt2]=avg;
               cnt2++;
               cnt=0;
               res=0;
          
            
        }
      
         double thres=0;
         int rs_sz=res2.length-1;
       
         int rs_ind=0;
          Min mn_Val= new Min();
         
         double mn=mn_Val.evaluate(res2,rs_ind,rs_sz);
       
         if(threshold<0){
         
         double avg= Arrays.stream(res2).average().getAsDouble();
       
         StandardDeviation std = new StandardDeviation();
         double sd=4*std.evaluate(res2);
        
         thres=mn+sd;
        
         }
         else{
           
            thres=threshold;
         }
         int wid= img.getWidth();
        
         float[][] bkPixelArray=new float[wid][1];
         
         ImageStack tmp_stk =new ImageStack(wid,1);
       
         for(int i=0;i<sz;i++){
     
             if(res2[i]<thres){
                 
                 for(int j=0;j<wid;j++){
                     int nf=i;
                     int ind=j;
                  
                      double tmp1= img.getVoxel(j, y, nf);
                  
                      bkPixelArray[ind][0]=(float)tmp1;
                 }
                 FloatProcessor out= new FloatProcessor(bkPixelArray);
                
                 tmp_stk.addSlice(out);
                 
             }
             
             
         }
         float[] rt_vs=new float[wid];
         
        if(tmp_stk.getSize()<=0){
           
                 for(int j=0;j<wid;j++){
                     
                     int ind=j;
                   
                      bkPixelArray[ind][0]=(float)mn;
                 }
                 FloatProcessor out= new FloatProcessor(bkPixelArray);
                
                 tmp_stk.addSlice(out);
             
        }
      
        ImagePlus rt_im=new ImagePlus("",tmp_stk);
       
        ZProjector zproj = new ZProjector(rt_im);
        
        zproj.setMethod(ZProjector.AVG_METHOD);
        zproj.doProjection();
        
       
        ImagePlus avg_rw= zproj.getProjection();
        ImageProcessor ip= avg_rw.getProcessor();
        FloatProcessor f=ip.convertToFloatProcessor();
        
        int ind=0;
        for(int v=0;v<wid;v++){
            float tmp =f.getPixelValue(v,0);
            //IJ.log("Val: "+tmp);
            rt_vs[ind]=tmp;
            ind++;
        }
      
         return rt_vs;
            
   
    }
   
    public void applyBackgroundSubtraction() {
        BackgroundSubtraction bs = new BackgroundSubtraction();
        ImagePlus backgroundSubtractedImage = bs.process(this.firstOrderImage_bk, this.backgroundImage);
        backgroundSubtractedImage.show();
     
        this.firstOrderImage=null;
        this.firstOrderImage = backgroundSubtractedImage;
    }
   
    public void displayCSVFile() {
       
        if(csvFileLoaded)
                      
        this.setPixelSize();
        this.drawBlinkingImage();
    }
    
    public void saveCropPos(ArrayList<String> paramNames,ArrayList<Double> crp_params)throws IOException{
       
        ExportCSV ex= new ExportCSV();
       
        String[] header = { "Parameters", "Values" }; 
        ArrayList<String[]> data= new ArrayList<String[]>();
        for(int i =0; i<crp_params.size();i++){
                String pname= paramNames.get(i);
                String prm = Double.toString(crp_params.get(i));
                String[] prms = {pname, prm};
                data.add(prms);
         
       }
        ex.exportCSV(this.currentImageDirectory, header, data);
       
    }
    
    private void saveCrpParams(String filename,ArrayList<String> prmNames,ArrayList<Double> crp_prms){
        
        File file = new File(filename);
    
         try { 
        // create FileWriter object with file as parameter 
        FileWriter outputfile = new FileWriter(file); 
  
        // create CSVWriter object filewriter object as parameter 
        CSVWriter writer = new CSVWriter(outputfile); 
  
        // adding header to csv 
        String[] header = { "Parameters", "Values" }; 
        writer.writeNext(header); 
       
        // add data to csv 
            for(int i =0; i<crp_prms.size();i++){
                String pname= prmNames.get(i);
                String prm = Double.toString(crp_prms.get(i));
                String[] data = {pname, prm};
                writer.writeNext(data);
         
       }
        
        // closing writer connection 
        writer.close(); 
    } 
    catch (IOException e) { 
        // TODO Auto-generated catch block 
       System.out.println("Error writing file.");
    } 
        
        
    }
    
     public ArrayList<Double> loadCropPos(String fileName) throws IOException,FileNotFoundException{
         
        ImportCSV im= new ImportCSV();
         ArrayList<Double> d_dt=new ArrayList<Double>();
        try{
        ArrayList<String[]> data=im.importCSV(fileName,this.currentImageDirectory, mainFrame);
        String fName= im.getFileName();
        textfieldCropPositions.setText(fName);
        if(data!=null&&!data.isEmpty()){
        String[] columnNames = data.get(0);
        boolean flg1 =columnNames[0].equals("Parameters");
        boolean flg2= columnNames[1].equals("Values");
        
        IJ.log(columnNames[0]);
        IJ.log(columnNames[1]);
        IJ.log(Boolean.toString(flg1));
        IJ.log(Boolean.toString(flg2));
        
       
        if(flg1&&flg2){
       
           for(int i =1; i<data.size();i++){
                String[] row=data.get(i);
                
                d_dt.add(Double.parseDouble(row[1]));
                
         
            }
     
        }else{
            d_dt=null;
             IJ.error("Incorrect File Loaded");
                      
        }
        
        }
        }catch(NullPointerException e1){
             csvFileLoaded =false;
        }
         
            return d_dt;
           
         }
    
     public double getPixelSize(){
        
         return px_Size; 
     }
     
      public void setPixelSize(){
         double px=Double.parseDouble(gui.SpatialImageParameterPanel.ftfPixSize.getText());
         px_Size = px;
        // return  Double.parseDouble(gui.SpatialImageParameterPanel.ftfPixSize.getText());
     }
     
     
     public int[] getROICropPositions(){
             ImagePlus img = IJ.getImage();
             ImageProcessor ip = img.getProcessor();
             Rectangle roi = ip.getRoi();
             
             IJ.log("Roi no selection:"+roi.width);
           
         if (roi.width == ip.getWidth() && roi.height == ip.getHeight()) {
         
          int[] cropPositions ={0,0,0,0};
          return cropPositions;
        }else{
        
          int[] cropPositions ={(int)round(roi.getX()),round((int)roi.getY()),round((int)roi.width),round((int)roi.height)};
           return cropPositions;
         }
        
     }
     
     
    public void updateCropping(int[] cropPositions) {
        
       // if((this.zeroOrderImage!=null)||(this.firstOrderImage_bk!=null)){
              int wns=0;
           
            String[] titles= WindowManager.getImageTitles();
            for(int vt=0;vt<titles.length;vt++){
                if(titles[vt]=="Cropped Region 1"){
                    IJ.selectWindow("Cropped Region 1");
                    IJ.run("Close");
                    wns++;
                     }
                
                    if(titles[vt]=="Cropped Region 2"){
                    IJ.selectWindow("Cropped Region 2");
                    IJ.run("Close");
                    wns++;
                     }
                    if(wns==2){
                        break;
                    }
                
            }
            
    
       // }
          
       
         ImagePlus img = IJ.getImage();
         int ip_wid= img.getWidth();
         int ip_height=img.getHeight();
      
         Rectangle roi = new Rectangle( cropPositions[0], cropPositions[1],cropPositions[2],cropPositions[3]);
         int roiYPosition = (int)round(roi.getY());
         int roiXPosition = (int) round(roi.getX());
         int roiWidth = (int)round(roi.getWidth());
        
        int roiHeight = (int)round(roi.getHeight());
               
         boolean bflg1 =roiHeight<=ip_height&&roiWidth<=ip_wid;
         boolean bflg2= roiWidth>0&&roiHeight>0&&roiXPosition>=0&&roiYPosition>=0;       
        if(bflg1&&bflg2){
        r_Wid= roiWidth;
        r_Hei=roiHeight;
        
                   
        int r2_x=roiXPosition+roiWidth;
        int r2_y=roiYPosition;
        int r2_Width=img.getWidth()-r2_x;
        int r2_Height =roiHeight;
        
        Rectangle roi2 = new Rectangle( r2_x, r2_y, r2_Width, r2_Height);
       
        ImageStack stack1 = new ImageStack(roi.width, roi.height);
        ImageStack stack2 = new ImageStack(roi2.width, roi2.height);
        
        ImageStack original = img.getStack();
        org_X=roiXPosition;//roiXPosition;//original.getWidth()-roi.width;//DEBUG this
        org_Y=roiYPosition;
        org_Wid=original.getWidth()-r2_Width;
        
       int offv = Integer.parseInt(gui.SpatialImageParameterPanel.ftfBsLv.getText());
       float offset=(float) offv;
       IJ.log("Offset"+offset);
       FloatProcessor off_img = new FloatProcessor(roi2.width, roi2.height);
       for(int k=1;k<roi2.width;k++){
           for(int j=1;j<roi2.height;j++){
               off_img.setf(k,j,offset);
               
           }
       }
       
       ImagePlus offset_img = new ImagePlus("",off_img);
        
         BackgroundSubtraction bk = new BackgroundSubtraction();
       
        for (int i = 1; i <= original.getSize(); i++) {
           
            stack1.addSlice("", crop(original.getProcessor(i), roi));
            ImageProcessor tmp=crop(original.getProcessor(i), roi2);
            ImagePlus tmp_img = new ImagePlus("",tmp);
            ImagePlus bk_sub =bk.process(tmp_img, offset_img);
            ImageProcessor tmp_im = bk_sub.getProcessor();
            stack2.addSlice("", tmp_im);
        }
        
        this.zeroOrderImage = new ImagePlus("Cropped Region 1", stack1);
        this.firstOrderImage_bk = new ImagePlus("Cropped Region 2", stack2);
        
        this.zeroOrderImage.show();
        this.firstOrderImage_bk.show();
        zeroth_Pos= roiYPosition;
        
        pcSupport.firePropertyChange(Analysis.IMAGES_READY, false, true);
        }else{
            if (!bflg1){
            IJ.error("Invalid Cropping Parameters: Cropping Parameters must be within the selected image");
            }
            if(!bflg2){
                IJ.error("Invalid Cropping Parameters: Cropped Area can't be 0");
            }
        }
            
   
    }
    
    
    public void cropROI(int[] cropPositions){
         ImagePlus img = IJ.getImage();
         ImageProcessor ip = img.getProcessor();
         Rectangle roi = ip.getRoi();
        
         
         if (roi.width == ip.getWidth() && roi.height == ip.getHeight()) {
           
            IJ.error("Please select region to be cropped");
        }else{
     
        
        int roiYPosition = (int)round(roi.getY());
        int roiXPosition = (int) round(roi.getX());
        int roiWidth = (int)round(roi.getWidth());
       
        int roiHeight = (int)round(roi.getHeight());
        
        cropPositions[0]=roiXPosition;
        cropPositions[1]=roiYPosition;
        cropPositions[2]=roiWidth;
        cropPositions[3]=roiHeight;
       
        ImageStack stack1 = new ImageStack(roi.width, roi.height);
      
        ImageStack original = img.getStack();
        
        for (int i = 1; i <= original.getSize(); i++) {
            stack1.addSlice("", crop(original.getProcessor(i), roi));
            
        }
        
        ImagePlus nImg=new ImagePlus("Cropped Region 1", stack1);
      
        }
         applyROI(cropPositions);
         
             String x= "X= "+Integer.toString(cropPositions[0]);
      String y ="Y= "+Integer.toString(cropPositions[1]);
      String width ="Width= "+Integer.toString(cropPositions[2]);
      String height ="Height= "+Integer.toString(cropPositions[3]);
      String st =" "+ x+" & "+y+" & "+width+" & "+height;
           if(postProcessString.equals("")){
         postProcessString = postProcessString+st;
         }else{
             postProcessString = postProcessString+" + "+st;
         }
        
    }
    
    public static ImageProcessor crop(ImageProcessor ip, Rectangle rect) {
        ip.setRoi(rect);
        return ip.crop();
    } 
   
      public void drawBlinkingImage() {
          
        
           
            String[] titles= WindowManager.getImageTitles();
            for(int vt=0;vt<titles.length;vt++){
                                
                    if(titles[vt]=="Spatial Image"){
                        IJ.selectWindow("Spatial Image");
                        IJ.run("Close");
                    break;
                     }
                  
                
            }
              
      
       
        ArrayList<Blinking> bEs=this.blinkings;
        int mag=getMag();
        ArrayList<Double> xpos= new ArrayList<Double>();
        ArrayList<Double> ypos= new ArrayList<Double>();
        ArrayList<Double> unc= new ArrayList<Double>();
        
         int sz=bEs.size();
     
           for( int i=0;i<sz;i++){
            Blinking bE=bEs.get(i);
            double tmp_x=bE.getXPosition();
            double tmp_y=bE.getYPosition();
            double tmp_unc=bE.getUnc();
           
            xpos.add(tmp_x);
            ypos.add(tmp_y);
            unc.add(tmp_unc);
          
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
        
         double conv=getPixelSize()+0.5;
        
         int stp_sz=(int)(mxunc/conv);
         
         int width=0;
         int height=0;
         
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
      
         if(!isROI){
             
         width=crp_Pos[2]*mag;
         height=crp_Pos[3]*mag;
         xmin=0;
         ymin=0;
         if(width==0){
              width = wid*mag;
         }
         if(height==0){
          height =hei*mag;
         }
             
         xmin=0;
         ymin=0;
         }else{
          width = wid*mag;
          height =hei*mag;
         }
     
         double resolution= conv/mag;
     
        int v =this.getVisMethod();
        IJ.log("Mag="+mag);
        IJ.log("Vismethod="+v);
        IJ.log("Num NB="+bEs.size());
        IJ.log("PxSz="+getPixelSize());
        IJ.log("Conv="+conv);
        switch(v){
            case 0:
         ScatterRendering sr = new ScatterRendering(width, height, resolution);
          sr.renderImage(bEs, xmin, ymin);
           
        ImagePlus img = sr.getImage();
        img.setTitle("Spatial Image");
        img.show();
        smlmImgLoaded=true;
          break;
          case 1:
         
         GaussianRendering gsr = new GaussianRendering(width,height,resolution);
         gsr.renderImage(bEs, xmin, ymin);
        ImagePlus gImg = gsr.getImage();
        gImg.setTitle("Spatial Image");
        gImg.show();
        smlmImgLoaded=true;
          break;
        
          default:
         ScatterRendering dsr = new ScatterRendering(width, height, resolution);
          dsr.renderImage(bEs, xmin, ymin);
           
        ImagePlus dImg = dsr.getImage();
        dImg.setTitle("Spatial Image");
        dImg.show();
        smlmImgLoaded=true;
          break;
        }
        
    }
    
    public void drawTCBlinkingImage(ArrayList<Blinking> BEs, int mn_c,int mx_c, int wid, int hei) {
         int mag=getMag();
         
         int width = wid*mag;
         int height =hei*mag;
        
         double resolution= Double.parseDouble(gui.SpatialImageParameterPanel.ftfPixSize.getText())/mag;
    
          int stp=1;
        System.out.println("Blinking Image Width: " + width);
        System.out.println("Blinking Image Height: " + height);
        
    
        sSMLMLambdaColoredRendering stcr = new sSMLMLambdaColoredRendering(width, height, resolution);
        int[] nRng= stcr.setRng(mn_c,mx_c,stp);
        
        ArrayList<Color> cols =stcr.getColRng(mn_c,mx_c,nRng);
       
        stcr.renderTCImage(BEs,nRng,cols);
        stcr.drawLegend(nRng,cols);
       
        ImagePlus img = stcr.getImage();
        img.show();
    
    }
    
    
     public void drawTCBlinkingROI(ArrayList<Blinking> BEs, int mn_c,int mx_c, int wid, int hei,int xmin, int ymin) {
       
         int mag=getMag();
         
         int width = wid*mag;
         int height =hei*mag;
      
       double resolution= (Double.parseDouble(gui.SpatialImageParameterPanel.ftfPixSize.getText())+0.5)/mag;
    
        int stp=1;
    
        sSMLMLambdaColoredRendering stcr = new sSMLMLambdaColoredRendering(width, height, resolution);
        int[] nRng= stcr.setRng(mn_c,mx_c,stp);
      
        ArrayList<Color> cols =stcr.getColRng(mn_c,mx_c,nRng);
        
        stcr.renderTCROI(BEs,nRng,cols,xmin,ymin);
        stcr.drawLegend(nRng,cols);
       
        ImagePlus img = stcr.getImage();
        img.show();
               
    }
     
     public void draw3DTCImage(ArrayList<Blinking> BEs){
         
        ArrayList<Double> centroids= new ArrayList<Double>();
        ArrayList<Double> photons= new ArrayList<Double>();
        
        ArrayList<Double> xps= new ArrayList<Double>();
        ArrayList<Double> yps= new ArrayList<Double>();
        ArrayList<Double> zps= new ArrayList<Double>();
        ArrayList<Double> psfSig2= new ArrayList<Double>();
        
        ArrayList<Double> unc= new ArrayList<Double>();
        double smPSFSig2=0;
     
        int sp_sz=0;
         Blinking t_bE=BEs.get(0);
        
         int sz=BEs.size();
        
        float [] sum_spec=new float[sp_sz];
        Arrays.fill(sum_spec,0);
        for( int i=0;i<sz;i++){
            Blinking bE=BEs.get(i);
            double tmp_c=bE.getCentroid();
            double tmp_ph=bE.getPhotons();
            
            double tmp_x=bE.getXPosition();
            double tmp_y=bE.getYPosition();
            double tmp_z=bE.getZPosition();
            double tmp_sigpsf2=bE.getSigma2Psf();
            double tmp_unc=bE.getUnc();
            
            smPSFSig2=smPSFSig2+tmp_sigpsf2;
           
            centroids.add(tmp_c);
            photons.add(tmp_ph);
            xps.add(tmp_x);
            yps.add(tmp_y);
            zps.add(tmp_z);
            psfSig2.add(tmp_sigpsf2);
            unc.add(tmp_unc);
          
            }  
        
        double avgsig2 =smPSFSig2/sz;
        
        int zStep=(int) (10*(Math.floor(Math.abs((avgsig2/2.355)/10))));
           
         double[] cens= ArrayUtils.toPrimitive(centroids.toArray(new Double[sz]));
         double[]phts= ArrayUtils.toPrimitive(photons.toArray(new Double[sz]));
         double [] xpos= ArrayUtils.toPrimitive(xps.toArray(new Double[sz]));
         double [] ypos= ArrayUtils.toPrimitive(yps.toArray(new Double[sz]));
         double [] zpos= ArrayUtils.toPrimitive(zps.toArray(new Double[sz]));
         double [] psfSg2= ArrayUtils.toPrimitive(psfSig2.toArray(new Double[sz]));
         double [] uncs= ArrayUtils.toPrimitive(unc.toArray(new Double[sz]));
         
        
          Max mx_Val = new Max();
          int mxunc= (int) round(mx_Val.evaluate(uncs,0,sz));
          int mx1=(int) round(mx_Val.evaluate(cens,0,sz));
          int tmp_val=mxunc;
          if(mxunc<=0||tmp_val>1000){
              tmp_val=1;
          }
          IJ.log("Tmp Val:"+tmp_val);
          int xmax=(int) round(mx_Val.evaluate(xpos,0,sz))+tmp_val;
          int ymax=(int) round(mx_Val.evaluate(ypos,0,sz))+tmp_val;
          int zmax=(int) round(mx_Val.evaluate(zpos,0,sz));
          
          Min mn_Val = new Min();
          int mn1=(int) round(mn_Val.evaluate(cens,0,sz));
          int xmin=(int) round(mn_Val.evaluate(xpos,0,sz))-tmp_val;
          int ymin=(int) round(mn_Val.evaluate(ypos,0,sz))-tmp_val;
          int zmin=(int) round(mn_Val.evaluate(zpos,0,sz));
          
          int zRng=Math.abs(zmax-zmin);
          IJ.log("zstep:"+zStep);
          IJ.log("Min: "+zmin+" Max: "+zmax);
          IJ.log("ZRNG: "+zRng);
          int sz_z= (int) Math.round(zRng/zStep);
          IJ.log("num frames:" +sz_z);
          
           if(ymin<0){
              ymin=0;
          }
             if(xmin<0){
              xmin=0;
          }
          double pixelSize = getPixelSize()+0.5;
          
          double conv=pixelSize+0.5;
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
          
           int mag=getMag();
         
               
        int width=0;
        int height=0;
     
         int stp_sz=(int)(mxunc/conv);
         IJ.log("Roi?"+isROI);
         
         if(!isROI){
             
        width=r_Wid*mag;//crp_Pos[2]*mag;//cropPos[2];//(Integer.parseInt(gui.SpatialImageParameterPanel.ftfWidth.getText())+stp_sz)*mag;
        height=r_Hei*mag;//crp_Pos[3]*mag;//(Integer.parseInt(gui.SpatialImageParameterPanel.ftfHeight.getText())+stp_sz)*mag;
         
         if(width<=0){
              width = wid*mag;
         }
         if(height<=0){
          height =hei*mag;
         }
             
         xmin=0;
         ymin=0;
         }else{
          width = wid*mag;
          height =hei*mag;
         }
      
      
        double resolution= conv/mag;
     
        int v =this.getVisMethod();
     
        int stp=1;
          
        ImageStack imgs = new ImageStack(width, height);
        int curZ=zmin;
        int nz=zmin+zStep;
        ArrayList<Blinking> zBEs= new ArrayList<Blinking>();
        
           
        sSMLM_3D stcr3D = new sSMLM_3D(width, height, resolution);
        int[] nRng= stcr3D.setRng(mn1,mx1,stp);
      
        ArrayList<Color> cols =stcr3D.getColRng(mn1,mx1,nRng);
        
        for(int k=0; k<sz_z;k++){
          
            for(int j=0;j<sz;j++){
                int crZ=(int) Math.round(zps.get(j));
                //IJ.log("Cur Z: "+crZ);
                if(crZ>curZ&&crZ<nz){
                    zBEs.add(BEs.get(j));
                }
                
            }
          
           curZ=nz;
           nz=nz+zStep;
           
     
        switch(v){
            case 0:
          IJ.showStatus("Rendering sSMLM Scatter Image");      
         imgs.addSlice(stcr3D.renderTCROI(zBEs, nRng, cols, xmin, ymin));
          IJ.showStatus("sSMLM Scatter Image Rendered");
          break;
            case 1:
         IJ.showStatus("Rendering sSMLM Averaged Gaussian Image"); 
         imgs.addSlice(stcr3D.renderTCGaussianROI(zBEs, nRng, cols, xmin, ymin,mxunc));
         IJ.showStatus("sSMLM Averaged Gaussian Image Rendered"); 
       
          break;
        
          default:
          IJ.showStatus("Rendering sSMLM Scatter Image");     
         imgs.addSlice(stcr3D.renderTCROI(zBEs, nRng, cols, xmin, ymin));
         IJ.showStatus("sSMLM Scatter Image Rendered");
          break;
        }
        zBEs.clear();
      
        }
      
        ImagePlus out = new ImagePlus("3D sSMLM",imgs);
        out.show();
        
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(out, 0.35);
        
        stcr3D.drawLegend(nRng, cols);
      
     }
  
       public void drawTCGaussianROI(ArrayList<Blinking> BEs, int mn_c,int mx_c, int wid, int hei,int xmin, int ymin, int mxunc) {
        // make new imageprocessor
         int mag=getMag();
        
        int width=0;
        int height=0;
    
         double conv=getPixelSize()+0.5;
        
         int stp_sz=(int)(mxunc/conv);
         IJ.log("Roi?"+isROI);
         
         if(!isROI){
             
        width=r_Wid*mag;
        height=r_Hei*mag;
         
         if(width<=0){
              width = wid*mag;
         }
         if(height<=0){
          height =hei*mag;
         }
             
         xmin=0;
         ymin=0;
         }else{
          width = wid*mag;
          height =hei*mag;
         }
      
         IJ.log("Width: "+width);
         IJ.log("Height: "+height);
    
        double resolution= conv/mag;
     
        int v =this.getVisMethod();
       if(width>0&height>0){
        int stp=1;
        sSMLMLambdaColoredRendering stcr = new sSMLMLambdaColoredRendering(width, height, resolution);
        int[] nRng= stcr.setRng(mn_c,mx_c,stp);
      
        ArrayList<Color> cols =stcr.getColRng(mn_c,mx_c,nRng);
        
        switch(v){
            case 0:
          IJ.showStatus("Rendering sSMLM Scatter Image");      
         stcr.renderTCROI(BEs, nRng, cols, xmin, ymin);
          IJ.showStatus("sSMLM Scatter Image Rendered");
          break;
            case 1:
         IJ.showStatus("Rendering sSMLM Averaged Gaussian Image"); 
         stcr.renderTCGaussianROI(BEs, nRng, cols, xmin, ymin,mxunc);
         IJ.showStatus("sSMLM Averaged Gaussian Image Rendered"); 
       
          break;
        
          default:
          IJ.showStatus("Rendering sSMLM Scatter Image");     
         stcr.renderTCROI(BEs, nRng, cols, xmin, ymin);
         IJ.showStatus("sSMLM Scatter Image Rendered");
          break;
        }
     
       }else{
           IJ.error("Unable to render sSMLM images");
       }
    }
    
       
   public ArrayList<Blinking> excludeOverlappingSpe(double[] params,ArrayList<Blinking> curBEs){
        
         long sTime= System.currentTimeMillis();
         long eTime =0;
        
        
        ArrayList<Blinking> n_BEs = new ArrayList<Blinking>();
      
         ArrayList<Integer> skipList = new ArrayList<Integer>();
        
        int sz= curBEs.size();
        ArrayList<Integer> frms = new ArrayList<Integer>();
        for( int k=0;k<sz;k++){
            frms.add(curBEs.get(k).getFrame());
        }
        
  
        for( int i=0;i<sz;i++){
            Blinking bE=curBEs.get(i);
           
            int tmp_f=bE.getFrame();
            int frm1=frms.indexOf(tmp_f);
            int frm2=frms.indexOf(tmp_f+1);
           
            boolean flg =false;
          
            for(int j=frm1; j<frm2;j++){
                if(i!=j){
                     Blinking bE2=curBEs.get(j);
                     int tmp_f2 = bE2.getFrame();
                   
                         flg=bE.isOverlapping(bE, bE2, params, xD, yD,fitOrder);
                  
                    
                }
                if(flg==true){
                
                 break;
                }
             
            }
               if(flg==false)
                     n_BEs.add(bE);
           
           }
          
    
      eTime = System.currentTimeMillis();
      double dTime=(eTime-sTime)/1000;
      
      double pRem= ((double)n_BEs.size()/sz)*100;
     
       return n_BEs;
    }
   
   
   public boolean excludeCurSpec(int idx, Blinking bEvent){
       boolean flg=false;
       
       return flg;
   }
 
    
    public boolean getROIflg(){
        return isROI;
    }
    
    public void setROIflg(boolean flg){
        isROI=flg;
    }
    
    
    private void setupGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
         
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
             // stack trace as a string
            
            IJ.log(sw.toString());
        }
        isROI=false;
        mainFrame = new JFrame("RainbowSTORM: sSMLM Analysis");
        mainFrame.setResizable(false);
        mainFrame.setIconImage(IJ.getInstance().getIconImage());
        mainPanel = new JPanel();
       
        mainPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
       
        mainPanel.add(new SpatialImageParameterPanel(this,true),gbc);
        mainPanel.add(new VisSettingsPanel(this,false),gbc);
        mainPanel.add(new LoadLocalizationsPanel(this),gbc);
        caliLoaded=false;
       
        mainPanel.add(new BackgroundSubtractionPanel(this),gbc);
        imgsReady =false; 
        mainPanel.add(new SpectraExtractionPanel(this),gbc);
        smlmImgLoaded=false;
        
      
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        mainFrame.pack();
        mainFrame.setVisible(true);
        setDefaults();
        
    }

 
}
