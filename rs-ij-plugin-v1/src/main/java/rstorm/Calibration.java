/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rstorm;

import filehandling.ExportCSV;
import gui.sSMLMCalibrationGUI;
import ij.*;//IJ;
import ij.ImagePlus;
import ij.measure.CurveFitter;
import ij.plugin.*;
import ij.plugin.filter.MaximumFinder;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import static java.lang.Math.round;
import org.jfree.chart.ChartFactory;
import javax.swing.JLabel;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartMouseEvent;
import java.io.*;
import static java.lang.Math.floor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.util.ShapeUtils;
import unmixing.BackgroundSubtraction;





/**
 *
 * @author Janel L Davis
 */
public class Calibration implements PlugIn{
    
    JFileChooser fc2;
    JFrame mainFrame;
    JPanel mainPanel;
    JLabel resOutput;
    JLabel curOutput;
    JPanel resPanel;
    JFormattedTextField ftfCurOutput;
    JLabel resOutput1;
    JPanel resPanel1;
    JLabel resOutput2;
    JPanel resPanel2;
     JPanel mancaliPanel;
    ChartPanel chartPanel;
    ChartPanel caliPanel;
    ChartPanel linePanel;
    JFreeChart orgDataPlot;
    XYSeriesCollection caliData; 
    XYSeriesCollection lineData; 
    GridBagConstraints gbc; 
    XYPlot peakPlot;
    ChartPanel chartPanel1;
    ChartPanel caliPanel1;
    JFreeChart orgDataPlot1;
    JFreeChart caliPlot1;
    XYSeriesCollection caliData1; 
    XYPlot peakPlot1;
    
        
    private double[] colSum;
    private double[] colSum1;
    private double[] colSum2;
    private double[] waveRng;
    private double d_res;
    private int i_upVal;
    private ArrayList<Double> cVals;
    private ArrayList<Integer> maxima;
    private ArrayList<Double>  coeffVals;
    private int fitpower;
    private int[] maxima1;
    private int[] maxima2;
    String currentImageDirectory;
    
    @Override
    public void run(String arg) {
  
        setLookandFeel();
       
        //get calibration image
        ImageProcessor ip = IJ.getProcessor();
        ImagePlus img = IJ.getImage();
       
        ZProjector zproj = new ZProjector(img);
        zproj.setMethod(ZProjector.AVG_METHOD);
        zproj.doProjection();
                
        ImagePlus avg_img= zproj.getProjection();
        ImageProcessor a_ip=avg_img.getProcessor();
            
        int w = ip.getWidth();
        int h = ip.getHeight();
        int mid = round(h/2);
        int[] bk1= new int[w]; 
        int[] bk2= new int[w]; 
        a_ip.getRow(0, mid,bk1, w);
       
        
        OptionalDouble temp=Arrays.stream(bk1).average();
        double thres =temp.getAsDouble();
        if(thres<0)  thres=0;
        
        int shf=30;
        
        int mid_shf=(int) floor(shf/2);
        int w_shf=w-mid_shf;
              
        double pc=5/100;
        double  thr2=(thres*pc);
        
        for(int y=0;y<w;y++){
            
            double t=thres;
            
            if (y>mid_shf&&y<w_shf){
                int init=y-mid_shf;
                int fin =y+mid_shf;
                int[] sub_bk =Arrays.copyOfRange(bk1,init,fin+1);
                int sm=Arrays.stream(sub_bk).sum();
                t=round(sm/shf);
                
              }
           if(t>thr2){
          
             bk2[y]=(int)round(t);
           }else{
            
              bk2[y]=(int)round(thr2);
          }
       
        }
        
         ImageProcessor bkp = new FloatProcessor(w,h);
     
         for (int u =0; u < w; u++ ) {
         
            for (int v = 0; v< h; v++) {
              
                 int bx=bk2[u];
                  
                bkp.putPixelValue(u, v, bx);
            }
      
        }
    
        
       ImagePlus mn_img =new ImagePlus("Background",bkp);
        
       BackgroundSubtraction bk = new BackgroundSubtraction();
       ImagePlus bk_sub =bk.process(avg_img, mn_img);

       ContrastEnhancer ce = new ContrastEnhancer();
       ce.stretchHistogram(bk_sub, 0.35);
       bk_sub.show("Background Removed");
               
        ImageProcessor bk_rm = bk_sub.getProcessor();
       
      
        ByteProcessor bp = bk_rm.convertToByteProcessor();
      
        colSum = new double[w];
        colSum1 = new double[w];
        colSum2 = new double[w];
        
        int h_top=0;
        int h_bt=Math.round(h)-1;
                
        
        for (int i =0; i < w; i++ ) {
     
            
            for (int j = 0; j< h; j++) {
                int p = bp.getPixel(i,j);
                          
                colSum[i] += (double) p/h;
               
             if(h>5) {  
                 if(j>h_top&& j<h_top+3){
                        colSum1[i]+=(double) p/h;
                           }
                
                if(j>(h_bt-3)&&j<h_bt){
                    colSum2[i]+=(double) p/h;
                            }
            }
                  
            }
     
        }
        
       
        updatePlot();
        setupListeners();
 
    }
    
       
      private void updatePlot() {
        
        mainFrame = new JFrame("RainbowSTORM: sSMLM Calibration");
        mainFrame.setIconImage(IJ.getInstance().getIconImage());
         mainFrame.setResizable(false);
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
                    
        XYDataset orgDt = this.plotData(colSum);
        orgDataPlot = ChartFactory.createXYLineChart("Uncalibrated Spectrum",
                "Pixels",
                "Intensity a.u.",
                orgDt,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);
        
        orgDataPlot.setBackgroundPaint(Color.white);
        
        peakPlot = orgDataPlot.getXYPlot();
        peakPlot.setBackgroundPaint(Color.lightGray);
        peakPlot.setDomainCrosshairVisible(true);
            
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(0, false);
        peakPlot.setRenderer(renderer);
        NumberAxis rangeAxis = (NumberAxis) peakPlot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        chartPanel=new ChartPanel(orgDataPlot);
        chartPanel.setDomainZoomable(true);
        chartPanel.setPreferredSize(new Dimension(500,270));
        gbc= new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        
        mainPanel.add(chartPanel,gbc);
        mainPanel.validate();
      
        caliData = new XYSeriesCollection();
        JFreeChart caliPlot=this.createCaliChart();  
    
        caliPanel=new ChartPanel(caliPlot);
        caliPanel.setPreferredSize(new Dimension(500,270));
        
        gbc.gridx = gbc.gridx+chartPanel.getHeight()+20;
        gbc.fill = GridBagConstraints.BOTH;
              
        mainPanel.add(caliPanel,gbc);
        lineData = new XYSeriesCollection();
        JFreeChart linePlot=this.createLineChart();
   
        linePanel=new ChartPanel(linePlot);
        linePanel.setPreferredSize(new Dimension(500,270));
               
        gbc.gridx = gbc.gridx+chartPanel.getWidth()+caliPanel.getWidth()+20;
      
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth= chartPanel.getWidth()+caliPanel.getWidth()+linePanel.getWidth()+20;
        
        mainPanel.add(linePanel,gbc);
        mainPanel.validate();
        
        GridBagConstraints rbc =new GridBagConstraints();
        resPanel= new JPanel();
      
        resPanel.setBorder(BorderFactory.createTitledBorder(" "));
        resPanel.setLayout(new GridBagLayout());
        
        rbc.gridx=0;
        rbc.gridy=0;
        rbc.anchor = GridBagConstraints.LINE_START;
        curOutput = new JLabel("Current Peak: ");
        resPanel.add(curOutput,rbc);
        resPanel.validate();
        rbc.gridx ++;
        rbc.gridy=0;
        
        ftfCurOutput= new JFormattedTextField();
        ftfCurOutput.setEditable(true);
        ftfCurOutput.setColumns(4);
        ftfCurOutput.setValue(new Integer(0));
        resPanel.add(ftfCurOutput,rbc);
        resPanel.validate();
        
        
        rbc.gridx=0;
        rbc.gridy ++;
        resOutput = new JLabel("    ");
        resPanel.add(resOutput,rbc);
        resPanel.validate();
        
        gbc.gridx = 0;
        gbc.gridy=gbc.gridy+chartPanel.getHeight()+caliPanel.getHeight()+20;
        gbc.gridwidth=480;
        mainPanel.add(resPanel,gbc);
        mainPanel.validate();
       
        gbc.gridx = 0;
        gbc.gridy=gbc.gridy+chartPanel.getHeight()+caliPanel.getHeight()+resPanel.getHeight()+20;
      
        gbc.gridwidth=480;
       
        mainPanel.add(new sSMLMCalibrationGUI(this),gbc);  
        mainPanel.validate();
     
                
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
  
    }
      
          
    private XYDataset  plotData(double[] values) {
        double[] x = new double[values.length];
          
        XYSeries series1 = new XYSeries("Lamp Spectra");
        for (int i = 0; i < x.length; i++){
            x[i] = i;           
            series1.add(x[i],values[i]);
        }
        XYSeriesCollection orgData = new XYSeriesCollection();
        orgData.addSeries(series1);
        
             
        return orgData;
    }
      
    
    
    public void runCalibration(int idx,int idx2, ArrayList<Double> caliWvs,ArrayList<Integer> caliPks){
     
     if(idx2==0&&idx==0) checkMaxima();    
     cVals= caliWvs;
     maxima= caliPks;
     this.calibrate(idx);
}
  
    public int  setFromPlot(){
 
    i_upVal =Integer.parseInt((ftfCurOutput.getText()).replace(",",""));
    
    return i_upVal;
}
    
    
    public void resetCalibration(){
        
        lineData = new XYSeriesCollection(); 
        JFreeChart linePlot = this.createLineChart();
        linePanel.setChart(linePlot);
        
        caliData = new XYSeriesCollection();
        JFreeChart caliPlot=this.createCaliChart(); 
        caliPanel.setChart(caliPlot);
        
    }
 
    public void saveCalibration() throws FileNotFoundException, IOException {
          
        ExportCSV ex= new ExportCSV();
       
          String[] header = { "Pixels", "Wavelength [nm]" }; 
        ArrayList<String[]> data= new ArrayList<String[]>();
        
        for(int i =0; i<maxima.size();i++){
                String pixels= Integer.toString(maxima.get(i));
                String wvlngths = Double.toString(cVals.get(i));
                String[] prms = {pixels, wvlngths};
                data.add(prms);
         
       }
        String[] fitRes={"Description","Parameters"};
        data.add(fitRes);
        String [] fitOrder={"Polynomial Order", Integer.toString(fitpower)};
         data.add(fitOrder);
     
        ex.exportCSV(this.currentImageDirectory, header, data);
    
    }
    
    
    private JFreeChart createLineChart(){
               
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
    
      private JFreeChart createCaliChart(){
               
      JFreeChart caliPlot = ChartFactory.createXYLineChart("Calibrated Spectrum",
                "Wavelength (nm)",
                "Intensity a.u.",
                caliData,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);
        
        caliPlot.setBackgroundPaint(Color.white);
        
        XYPlot autoPeakPlot = caliPlot.getXYPlot();
        autoPeakPlot.setBackgroundPaint(Color.lightGray);  
         NumberAxis rangeAxis2 = (NumberAxis) autoPeakPlot.getRangeAxis();
        rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return caliPlot;

}  
    
            
    private void setPkVal(double dx){
    
     ftfCurOutput.setValue(new Integer((int)dx));
      
    }
    
    private void setupListeners() {
        chartPanel.addChartMouseListener(new ChartMouseListener(){
           //Override
            public void chartMouseClicked(ChartMouseEvent e){
                double d =peakPlot.getDomainCrosshairValue();//x value
                double r=peakPlot.getRangeCrosshairValue(); //y value
                 setPkVal(d);
                
                 }

           public  void chartMouseMoved(ChartMouseEvent e){
           }
           
           });
       }

    
    private void checkMaxima() {
     
        if(colSum1.length>0&&colSum2.length>0){
        OptionalDouble temp=Arrays.stream(colSum1).average();
        double t1=temp.getAsDouble();
      
        int[] mx1 = MaximumFinder.findMaxima(colSum1, t1, true);
       
        OptionalDouble temp2=Arrays.stream(colSum2).average();
        double t2=temp2.getAsDouble();
      
         
        int[] mx2 = MaximumFinder.findMaxima(colSum2, t2, true);
        
 
        int len =5;
        
        if(len>mx1.length) len=mx1.length;
        if(len>mx2.length) len =mx2.length;
        
        
        maxima1=new int[len];
        System.arraycopy(mx1, 0, maxima1, 0,len);
        Arrays.sort(maxima1);
        
        
        maxima2=new int[len];
        System.arraycopy(mx2, 0, maxima2, 0, len);
        Arrays.sort(maxima2);
        
     int flg=0;
     int[] difs =new int[maxima1.length];
     int shf=Math.abs(maxima1[0]-maxima2[0]);
     
   
   for (int i=0;i<difs.length;i++){
        if (i==0)
                {
                    difs[i]=shf;
                }
        if (i>0){
               difs[i]=Math.abs(maxima1[i]-maxima2[i]);
        }
              if(Math.abs(difs[i]-shf)>1)
              {
                  flg=1;
              }
             
       
    }
     if(shf>2)
     {  
         
        if(flg==1)
             IJ.showMessage("Warning:Nonuniform spectral  peak positions detected");
        else
        IJ.showMessage("Warning:Nonuniform peak positions detected");
     
    }
        } 
        
    }
    
    
     
   private void calibrate(int idx){
    
        //pixels
        double[] xData = new double[maxima.size()-1];
        
        //Wavelengths for calibration
        double[] yData=new double[maxima.size()-1];
         XYSeries cvseries = new XYSeries("Calibration");
         
         int mn_v=1;
         //ArrayList<Double> tmp= new ArrayList<Double>();
        
        //set values for x and y calibration data
        for(int j=0;j<(maxima.size()-1);j++){
           yData[j] = (double)maxima.get(j+1);
         
            xData[j] = cVals.get(j+1);   
            if(xData[j]<cVals.get(mn_v)){
                mn_v=j+1;
            }
           
           cvseries.add(yData[j],xData[j]);
        }
        
        //Set range for grating
       int lbnd=400;
       int ubnd=850;
       //set range for prism
        if(idx==1){  
        
       int mx_v=cVals.indexOf(Collections.max(cVals));
       lbnd=(int)Math.round(cVals.get(mn_v));
       ubnd=(int)Math.round(cVals.get(mx_v));
       if(lbnd-20>400){
           lbnd=lbnd-20;
       }
       if(ubnd+20<850){
           ubnd=ubnd+20;
       }
       
        }
     
        
        if(lbnd>=400 &&ubnd<=850){
       
        //upsampling parameter
        double upSam=1;
        //set range of wavelengths
        int d=(ubnd-lbnd)*(int)round(upSam);
             
        waveRng = new double[d];
         for (int i=0; i<(d);i++){
            double init= (double) lbnd;
            double val =(double) i;
           waveRng[i]=init+(val/upSam);           
        }
                 
         //Caliculate coefficients         
        CurveFitter cali= new CurveFitter(xData,yData);
        int fitp=0;
            switch (idx) {
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
        fitpower=fitp;
        double r2= cali.getRSquared();
      
        double rmse= cali.getSD();
      
        double[] coeffs= cali.getParams();
            
         if(idx==0){
         d_res=1/coeffs[1];              
         String st = "Spectral Dispersion (pixels/nm): ";
         String str = String.format("%.2f", d_res);
         st= st.concat(str);
         resOutput.setText(st);
         }
         else{
           d_res=-1;
           String st = "    ";
           resOutput.setText(st);
         }
         
     coeffVals= new ArrayList<Double>();    
             
     for(int g=0;g<coeffs.length;g++){
         coeffVals.add(coeffs[g]);
     }
       
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
        
        
        JFreeChart linePlot = this.createLineChart();
       
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
     
       linePlot.addSubtitle(subText);
     
        linePanel.setChart(linePlot);
         
          //Initial PixelPostion
          int ind=(int) round(x_new[0]);
          //Final PixelPosition
          int mx=(int) round(x_new[x_new.length-1]);
        
          //Array length
          int dif= (mx-ind);
            if(ind>0&&mx>0&&dif>0){
            double[] sec_colSum= new double[dif]; 
        
         
          double[] rng=new double[dif];
          for(int r=0;r<dif;r++){
              //PixelValues and Indicies
              sec_colSum[r]=colSum[ind];
              rng[r]=ind;
              ind=ind+1;
          }
     
        double[] cal_spec=this.interpLinear(rng,sec_colSum,x_new);  
        double[] yi = new double[xi.length];
         XYSeries series2 = new XYSeries("Calibrated Spectra");
          
          for (int k = 0; k < xi.length; k++) {
                  
                    series2.add(waveRng[k],cal_spec[k]);
            }
        
         caliData = new XYSeriesCollection(); 
         caliData.addSeries(series2);
           
         
        JFreeChart caliPlot = this.createCaliChart();
        XYPlot autoPeakPlot = caliPlot.getXYPlot();
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
               
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        autoPeakPlot.setRenderer(renderer);
        caliPanel.setChart(caliPlot);
                 
        
       }else{
            IJ.error("Inputs out of Range");
        }
        }else{
                    IJ.error(" Wavelength inputs must be from 400 nm too 850 nm");
                    }
       

   }
   
   //Adapted from: http://www.mbari.org" * The Monterey Bay Aquarium Research Institute (MBARI)
    private double[] interpLinear(double[] x, double[] y, double[] xi) throws IllegalArgumentException {

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
        for (int i = 0; i < xi.length; i++) {
            if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
                 yi[i] = 0;
            }
            else {
                int loc = Arrays.binarySearch(x, xi[i]);
                if (loc < -1) {
                    loc = -loc - 2;
                    yi[i] = slope[loc] * xi[i] + intercept[loc];
                }
                else {
                    yi[i] = y[loc];
                }
            }
        }

        return yi;
    }
    
    private void setLookandFeel(){
           
        String systemLAFName = UIManager.getSystemLookAndFeelClassName();
            if("javax.swing.plaf.metal.MetalLookAndFeel".equals(systemLAFName)) {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
            }
        try {
            UIManager.setLookAndFeel(systemLAFName);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Calibration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Calibration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Calibration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Calibration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setLayout(GridBagLayout gridBagLayout) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
     
