/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.LUT;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import static java.lang.Math.round;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import rstorm.Analysis;
import ij.plugin.BrowserLauncher;

import ij.plugin.ContrastEnhancer;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Window;
import java.net.URL;

import java.util.Collections;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;


import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import rendering.FRC_Analysis;

import unmixing.Blinking;


/**
 *
 * @author Janel
 */
public class VisualizationPanel extends JPanel {
    
    private Analysis controller;
    private static final String url = "sSMLM_Visualization.html";
    private static final String ver = "2020_01";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private JButton buttonHelp;
     private JPanel visThresP;
   
    private JPanel spectraPanel;
   
    private  JScrollPane jspSpectra;
    private ChartPanel cenPanel;
    private ChartPanel avgSpPanel;
    private JButton buttonReset ;
    private JButton buttonShowHists;
    private JButton buttonApplyFilter;
  
    private JButton buttonClassify;
   
    private JFormattedTextField ftfThres1;
    private JFormattedTextField ftfThres2;
    private JComboBox histCombo;
    private JComboBox filterCombo;
    private JButton buttonSetROI;
    private JButton buttonRunFRC;
    private JButton buttonShow3D;
    
    
    private ArrayList<Blinking> cBEs;
    private int[] imVals;      
    
    
    
    
    private ImageIcon sp_imc;
   
     public static JTextField textfieldDriftFilePath;
     private JButton buttonLoadDrift;
     private JButton buttonDriftCorrect;
     private JButton buttonSaveSpectra;
     private JButton buttonUpdateImage;
    private JTextField textfieldSaveSpectraFilename;
    private boolean dataImported;
     private boolean is3DData;
    
    
       public VisualizationPanel(Analysis controller,boolean isImportScreen,boolean is3D,double[] cens,double[] phts, float[] avg_sp,int[] fy,ArrayList<float[]> specs){
      
        this.controller = controller;
        dataImported=isImportScreen;
        is3DData=is3D;
        setupView(cens,phts, avg_sp,fy,specs);
        setupActionListerners();
        
    }
  
       
    private void setupView(double[] cens,double[] phts, float[] avg_sp,int[] fy,ArrayList<float[]> spectra){
       
        Color[] col=new Color[1];
        col[0]=Color.black;
         XYSeriesCollection dataset =new XYSeriesCollection();
         dataset.addSeries(controller.drawCenScatterPlot(cens,phts,"Centroids"));
       
        XYDataset cen_dataset =dataset;
        JFreeChart cenChart=controller.createCenPhChart(cen_dataset,col);
       
       XYDataset avg_dataset=controller.drawAvgSpecPlot(avg_sp,fy,"Averaged Spectrum");
       JFreeChart  avgSpecChart= controller.createAvgSpChart(avg_dataset,"Averaged Spectrum");
     
         int sp_sz=avg_sp.length;
         int sz= cens.length;
         
         IJ.log("Wavelength "+sp_sz);
         IJ.log("Num Cens "+sz);
       
         float [][] out_spec=controller.arrayListTo2DFloatArray(spectra, sp_sz,sz);
         FloatProcessor sp_out=new FloatProcessor(out_spec);
             
         ImagePlus sp_Img = new ImagePlus("spectra",sp_out);
         sp_Img.setLut(LUT.createLutFromColor(Color.RED));
         
          ContrastEnhancer ce = new ContrastEnhancer();
          ce.stretchHistogram(sp_Img, 0.35);
        
                String wv1=""+fy[0];
                String wv3=""+(fy[fy.length-1]+1);
                String wv2=""+((int)Math.round(((fy[fy.length-1]+1)+fy[0])/2));
           
              spectraPanel= new JPanel();  
            
              Image sp_output= sp_Img.getImage();
               sp_imc= new ImageIcon(sp_output);
        
        
                
               jspSpectra = new JScrollPane(new JLabel(sp_imc));
               //jspSpectra.add(this)
                jspSpectra.setPreferredSize(new Dimension(sp_sz+50,300));
                jspSpectra.createVerticalScrollBar();
                
               
                  cenPanel = new ChartPanel(cenChart);
                cenPanel.setPreferredSize(new Dimension(300, 300)); 
                
                 avgSpPanel = new ChartPanel(avgSpecChart);
                 avgSpPanel.setPreferredSize(new Dimension(300,300));
                
                              
                setBorder(BorderFactory.createTitledBorder("sSMLM Visualization"));
        
                setLayout(new GridBagLayout());
                
                GridBagConstraints mc = new GridBagConstraints();  
                
                mc.gridx=0;
                mc.gridy=0;
                
                JPanel imgPanel = new JPanel();
                imgPanel.setLayout(new FlowLayout());
              
               spectraPanel.setLayout(new BorderLayout());
                spectraPanel.add(new JLabel("Spectral Images"),BorderLayout.NORTH);
               spectraPanel.add(jspSpectra, BorderLayout.CENTER);
               
                           
                 JLabel fy1= new JLabel("   "+wv1+"         "+wv2+"         "+wv3);
              fy1.setHorizontalAlignment(JLabel.LEFT);
              spectraPanel.add(fy1,BorderLayout.SOUTH);
              
                 JPanel lblPanel = new JPanel();
                lblPanel.setLayout(new BorderLayout());
                lblPanel.add(spectraPanel,BorderLayout.CENTER);
                
                
               
              JLabel yaxis =new JLabel("Events");
              lblPanel.add(yaxis,BorderLayout.WEST);
          
               JLabel xaxis =new JLabel("Wavelength [nm]");
              xaxis.setHorizontalAlignment(JLabel.CENTER);
              lblPanel.add(xaxis,BorderLayout.SOUTH);
              imgPanel.add(lblPanel);
          
                      
                avgSpPanel.setLayout(new BorderLayout());
                imgPanel.add(avgSpPanel);
              
                
                cenPanel.setLayout(new BorderLayout());
               
                imgPanel.add(cenPanel);
                
                add(imgPanel,mc);
            
               
                mc.gridx=0;
                mc.gridy=spectraPanel.getHeight()+20;
                
                
                JPanel visFields =new JPanel();
                visFields.setLayout(new GridBagLayout());
                 GridBagConstraints sc = new GridBagConstraints();  
                sc.anchor=GridBagConstraints.CENTER;
                
                sc.gridx=0;
                sc.gridy=0;
              
                JPanel histPanel = new JPanel();
                histPanel.setLayout(new FlowLayout());
                JLabel lblHists = new JLabel("Histograms: ");
                histPanel.add(lblHists);
                //histPanel.add(new JLabel("Select Histogram:"));
                ArrayList<String> col_Names= new ArrayList<String>();
                
                if(is3DData){
                   
            String[] h_arr    ={"id","frame", "x [nm]","y [nm]","z [nm]","spatial photons","spatial sigma1 [nm]","spatial sigma2 [nm]","localization uncertainty [nm]",
           "spectral centroid [nm]","spectral photons","spectral bk photons","spectral bk photons/pixel","spectral sigma [nm]","spectral uncertainty [nm]"};
            Collections.addAll(col_Names, h_arr);
            
                }else{
               String[] h_arr    ={"id","frame", "x [nm]","y [nm]","spatial photons","spatial sigma [nm]","localization uncertainty [nm]",
            "spectral centroid [nm]","spectral photons","spectral bk photons","spectral bk photons/pixel","spectral sigma [nm]","spectral uncertainty [nm]"};
            Collections.addAll(col_Names, h_arr);
                }
                 histCombo = new JComboBox(col_Names.toArray());
                
                histPanel.add(histCombo);
                buttonShowHists = new JButton("Show Histogram");
                histPanel.add(buttonShowHists);
                
                visFields.add(histPanel,sc);
                sc.gridy++;
                 Dimension d = new Dimension (150,20);
                 JPanel driftPanel = new JPanel();
                driftPanel.setLayout(new FlowLayout());
                driftPanel.add(new JLabel("Drift Correction File: "));
                textfieldDriftFilePath = new JTextField(20);
                //textfieldDriftFilePath .setText(def_file);
                driftPanel.add(textfieldDriftFilePath);
                buttonLoadDrift = new JButton("<<Load");
                buttonLoadDrift.setEnabled(true);
                driftPanel.add(buttonLoadDrift);
                buttonDriftCorrect = new JButton("Correct Drift");
                buttonDriftCorrect.setEnabled(false);
                driftPanel.add(buttonDriftCorrect);
                
                
                visFields.add(driftPanel,sc);
                sc.gridy++;
                
                  sc.fill=GridBagConstraints.HORIZONTAL;
                visFields.add(new VisSettingsPanel(controller,true),sc);
               
                buttonUpdateImage= new JButton("Update Rendering");
                buttonUpdateImage.setPreferredSize(d);
                
                sc.gridy++;
                
                
                JPanel filterPanel = new JPanel();
                
                filterPanel .setLayout(new FlowLayout());
                
               filterPanel.add(new JLabel("Select Filter"));
               
                 filterCombo = new JComboBox(col_Names.toArray());
                
                filterPanel.add(filterCombo);
                Format format = NumberFormat.getNumberInstance();
                double dIt1 =0;
                ftfThres1 = new JFormattedTextField(format);
                ftfThres1.setValue(dIt1);
                ftfThres1.setColumns(4);
                filterPanel.add(ftfThres1);
                double dIt2 =0;
               
                ftfThres2 = new JFormattedTextField(format);
                ftfThres2.setValue(dIt2);
                ftfThres2.setColumns(4);
                filterPanel.add(ftfThres2);
                buttonApplyFilter = new JButton("Set Range");
                filterPanel.add(buttonApplyFilter);
                
                 visFields.add(filterPanel,sc);     
                sc.gridy++;
                sc.gridx=0;
           
              sc.fill=GridBagConstraints.NONE;
              sc.anchor = GridBagConstraints.CENTER;
              JPanel ctrlPanel = new JPanel();
              ctrlPanel.setLayout(new FlowLayout());
                
                buttonSetROI =new JButton("Restrict to ROI");
                buttonSetROI.setPreferredSize(d);
                ctrlPanel.add(buttonSetROI);
                
                buttonShow3D = new JButton("Display 3D sSMLM");
                buttonShow3D.setPreferredSize(d);
                if(is3DData){
                    ctrlPanel.add(buttonShow3D);
                }
                
                buttonClassify =new JButton("Classify by Centroid");
                buttonClassify.setPreferredSize(d);
                ctrlPanel.add(buttonClassify);
                
                buttonRunFRC =new JButton("Calculate FRC");
                buttonRunFRC.setPreferredSize(d);
                ctrlPanel.add(buttonRunFRC);
              
                buttonReset =new JButton("Reset sSMLM Data");
                buttonReset.setPreferredSize(d);
               
                ctrlPanel.add(buttonReset);
                visFields.add(ctrlPanel,sc);
                sc.gridy++;
                sc.gridx=0;
              
                visThresP= new JPanel();
                
                int w1=750;
                int h1= 100;
                Dimension d2 =new Dimension(w1,h1);
                visThresP.setPreferredSize(d2);
                JPanel tmp=new VisThresPanel(controller, dataImported);
                visThresP.add(tmp);
                  visFields.add(visThresP,sc);
                  sc.gridy++;
                  
                 JPanel spSavePanel=new JPanel();
                spSavePanel.setLayout(new FlowLayout());
        
     
                spSavePanel.add(new JLabel("File Path: "));
        
                textfieldSaveSpectraFilename = new JTextField(20);
                spSavePanel.add(textfieldSaveSpectraFilename);
                buttonSaveSpectra = new JButton("Save Spectroscopic Data");
                buttonSaveSpectra.setEnabled(true);
                spSavePanel.add(buttonSaveSpectra);
                visFields.add(spSavePanel,sc);
            
                add(visFields,mc);
                  mc.anchor=GridBagConstraints.LAST_LINE_END;
        
         JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new FlowLayout());
        URL loc;
        try {
        loc = getClass().getClassLoader().getResource("helpbutton.png");
       
       ImageIcon img_icon =new ImageIcon(loc);
       Image img = img_icon.getImage();
       
       Image newimg = img.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
       img_icon = new ImageIcon(newimg); 
       
       
        buttonHelp =new JButton(img_icon);
        buttonHelp.setEnabled(true);
        buttonHelp.setPreferredSize(new Dimension (20,20));
        helpPanel.add(buttonHelp);
        //bc.gridy=bc.gridy++;
        mc.anchor=GridBagConstraints.LAST_LINE_END;
        
       add(helpPanel, mc);
        } catch (Exception ex) {
            Logger.getLogger(VisualizationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        
    }
    
    private void setupActionListerners(){
            
           buttonRunFRC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Blinking> BEs = controller.getCurrentData();
                int mag = controller.getMag();
                double pxSz= controller.getPixelSize();
                
                   FRC_Analysis FRC = new FRC_Analysis(BEs,mag, pxSz);
               
            }
        });
 
         
         
            buttonSetROI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 
                    int[] cropPositions = new int[]{0,0,0,0};
                    controller.cropROI(cropPositions);
                
                    ArrayList<Blinking> n_BEs=controller.getCurrentData();
                    int[] fy= controller.getRange();
                    controller.setROIflg(true);
                  
                    updatePlots(n_BEs,fy);
                 
             
            }
        });
          buttonLoadDrift.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    
                try {
                    controller.loadDriftResults(textfieldDriftFilePath.getText());
                    buttonDriftCorrect.setEnabled(true);
                    
                } catch (IOException ex) {
                    Logger.getLogger(LoadLocalizationsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
               
            
                  
            }
        });
          
           buttonDriftCorrect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 
                controller.driftCorrect();
                ArrayList<Blinking> n_BEs=controller.getCurrentData();
                    int[] fy= controller.getRange();
                    
                    updatePlots(n_BEs,fy);
              
            }
        });
         
           buttonReset.addActionListener(new ActionListener() {
           
            public void actionPerformed(ActionEvent e) {
               
                    controller.resetVisData();
                    ArrayList<Blinking> n_BEs=controller.getCurrentData();
                    int[] fy= controller.getRange();
                    controller.setROIflg(false);
                    updatePlots(n_BEs,fy);
            
             
            }
        });
           
           
            buttonClassify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                     
                     
                   
                     JFrame cbcFrame = new JFrame();
                     cbcFrame.setIconImage(IJ.getInstance().getIconImage());
                     JPanel cbcPanel =new JPanel();
                     cbcPanel.add(new VisbyClassPanel(controller,cbcFrame));
                     
                     cbcFrame.getContentPane().add(cbcPanel);
                     cbcFrame.pack();
                     cbcFrame.setVisible(true);
                     
                    
                   
             
            }
        }); 
               
              buttonShow3D.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Blinking> cBEs = controller.getCurrentData();
                controller.draw3DTCImage(cBEs);
              
            }
        }); 
           buttonShowHists.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    
                    int idx= histCombo.getSelectedIndex();
                    if(is3DData){
                    controller.shwHists3D(idx);
                    }else{
                          controller.shwHists2D(idx);
                    }
                    
                   
             
            }
        });   
           
         buttonApplyFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                        // Thread processThread =new Thread(){
                  //  public void run(){
                    int idx= filterCombo.getSelectedIndex();
                     String th1_Val= ftfThres1.getText();
                    double th1=Double.parseDouble(th1_Val.replace(",",""));
                    String th2_Val= ftfThres2.getText();
                    double th2=Double.parseDouble(th2_Val.replace(",",""));
                    if(th1<th2){
                    if(is3DData){
                        controller.applyFilter3D(idx,th1,th2);
                    }else{    
                    controller.applyFilter(idx,th1,th2);
                    }
                    ArrayList<Blinking> n_BEs=controller.getCurrentData();
                    int[] fy= controller.getRange();
                  
                    updatePlots(n_BEs,fy);
                    }else{
                        IJ.error("Invalid threshold set");
                    }
              
            }
        });
           
            buttonSaveSpectra.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if(is3DData){
                        controller.save3DBlinkingSpectra(textfieldSaveSpectraFilename.getText());
                    }else{
                    controller.saveBlinkingSpectra(textfieldSaveSpectraFilename.getText());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Analysis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
            
                   buttonHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                  try {
           
            JDialog dialog = new JDialog(IJ.getInstance(), "RainbowSTORM Help(" + ver + ")");
            if(IJ.isJava17()) {
                dialog.setType(Window.Type.UTILITY);
            }
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE); 
            final JEditorPane aboutPanel = new JEditorPane();
            aboutPanel.setBorder(BorderFactory.createEmptyBorder());
            aboutPanel.setEditable(false);
            aboutPanel.addHyperlinkListener(new HyperlinkListener(){
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event){
            if(event.getEventType() ==HyperlinkEvent.EventType.ACTIVATED){
                try{
                    if(event.getURL().toString().contains("https://")){
                     Desktop.getDesktop().browse(event.getURL().toURI());
                    }else{

                   aboutPanel.setPage(event.getURL());
                    }

                }catch(Exception ioe){
                    System.err.println("Error loading url from link:"+ioe);

                }
            }
            }
            });            
            URL resource = getClass().getClassLoader().getResource(url);
           
            JScrollPane scrollPane = new JScrollPane(aboutPanel);
            scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            dialog.getContentPane().add(scrollPane);
            aboutPanel.setPage(resource);
            
          
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            } catch(Exception e2) {
            IJ.handleException(e2);
        }
            }
    
    });
            
            buttonUpdateImage.addActionListener(new ActionListener(){
        
      @Override
            public void actionPerformed(ActionEvent e) {
                    
                            
                    ArrayList<Blinking> n_BEs=controller.getCurrentData();
                    int[] fy= controller.getRange();
                     updatePlots(n_BEs,fy);
           
            }
        });
        
    }
    
     
    
       
     public void updatePlots(ArrayList<Blinking> BEs,int[] fy){
         
        ArrayList<Double> centroids= new ArrayList<Double>();
        ArrayList<Double> photons= new ArrayList<Double>();
        
        ArrayList<Double> xps= new ArrayList<Double>();
        ArrayList<Double> yps= new ArrayList<Double>();
        ArrayList<Double> unc= new ArrayList<Double>();
        
        ArrayList<float[]> spectra= new ArrayList<float[]>();
       
        int sp_sz=0;
         Blinking t_bE=BEs.get(0);
         float [] tmp_spec=t_bE.getSpectrum();
         sp_sz=tmp_spec.length;
          IJ.log("Spectral Centroids: "+sp_sz);
         int sz=BEs.size();
         IJ.log("Current Centroids: "+sz);
        
        float [] sum_spec=new float[sp_sz];
        Arrays.fill(sum_spec,0);
        for( int i=0;i<sz;i++){
            Blinking bE=BEs.get(i);
            double tmp_c=bE.getCentroid();
            double tmp_ph=bE.getPhotons();
            
            double tmp_x=bE.getXPosition();
            double tmp_y=bE.getYPosition();
            double tmp_unc=bE.getUnc();
            
           
            float [] t_spec=bE.getSpectrum();
            sum_spec=controller.getSumSpectra(sum_spec,t_spec);
            
      
           
            centroids.add(tmp_c);
            photons.add(tmp_ph);
            xps.add(tmp_x);
            yps.add(tmp_y);
            unc.add(tmp_unc);
            
           
            spectra.add(t_spec);
            
         
            }  
           
         double[] cens= ArrayUtils.toPrimitive(centroids.toArray(new Double[sz]));
         double[]phts= ArrayUtils.toPrimitive(photons.toArray(new Double[sz]));
         double [] xpos= ArrayUtils.toPrimitive(xps.toArray(new Double[sz]));
         double [] ypos= ArrayUtils.toPrimitive(yps.toArray(new Double[sz]));
         double [] uncs= ArrayUtils.toPrimitive(unc.toArray(new Double[sz]));
      
        
          Max mx_Val = new Max();
           int mxunc= (int) round(mx_Val.evaluate(uncs,0,sz));
          int mx1=(int) round(mx_Val.evaluate(cens,0,sz));
          int tmp_val=mxunc;
         // if(mxunc<=0||tmp_val>1000){
          if(mxunc<=0){
              tmp_val=1;
          }
          IJ.log("Tmp Val:"+tmp_val);
          int xmax=(int) round(mx_Val.evaluate(xpos,0,sz))+tmp_val;
              
          int ymax=(int) round(mx_Val.evaluate(ypos,0,sz))+tmp_val;
          Min mn_Val = new Min();
          int mn1=(int) round(mn_Val.evaluate(cens,0,sz));
          int xmin=(int) round(mn_Val.evaluate(xpos,0,sz))-tmp_val;
          int ymin=(int) round(mn_Val.evaluate(ypos,0,sz))-tmp_val;
          
           if(ymin<0){
              ymin=0;
          }
             if(xmin<0){
              xmin=0;
          }
          double pixelSize = controller.getPixelSize()+0.5;
          //double conv=pixelSize+0.5;
          double conv=pixelSize+0.5;
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
        
          
         float[] avg_sp= controller.getAvgSpectra(sum_spec,sz);
    
         IJ.log("Data Updated");
        Color[] col=new Color[1];
        col[0]=Color.black;
         XYSeriesCollection dataset =new XYSeriesCollection();
         dataset.addSeries(controller.drawCenScatterPlot(cens,phts,"Centroids"));
         XYDataset up_cendset=dataset;
         JFreeChart up_cenChart = controller.createCenPhChart(up_cendset,col);
         
         cenPanel.setChart(up_cenChart);
 
         XYDataset up_avgspdset=controller.drawAvgSpecPlot(avg_sp, fy,"Average Spectrum");
         JFreeChart up_avgSpecChart= controller.createAvgSpChart(up_avgspdset,"Average Spectrum");
         avgSpPanel.setChart(up_avgSpecChart);
   
        
         float [][] out_spec=controller.arrayListTo2DFloatArray(spectra, sp_sz,sz);
         FloatProcessor sp_out=new FloatProcessor(out_spec);
             
         ImagePlus sp_Img = new ImagePlus("spectra",sp_out);
         sp_Img.setLut(LUT.createLutFromColor(Color.RED));
          ContrastEnhancer ce = new ContrastEnhancer();
          ce.stretchHistogram(sp_Img, 0.35);
         Image sp_output= sp_Img.getImage();
         sp_imc= new ImageIcon(sp_output);
         
                           
                String wv1=""+fy[0];
                String wv3=""+(fy[fy.length-1]+1);
                String wv2=""+((int)Math.round(((fy[fy.length-1]+1)+fy[0])/2));
              
      
                JScrollPane jspSpectra2= new JScrollPane(new JLabel(sp_imc));
              
                spectraPanel.removeAll();
                  spectraPanel.add(new JLabel("Spectral Images"),BorderLayout.NORTH);
                spectraPanel.add(jspSpectra2);
                
                  JLabel fy1= new JLabel("   "+wv1+"         "+wv2+"         "+wv3);
              fy1.setHorizontalAlignment(JLabel.LEFT);
              spectraPanel.add(fy1,BorderLayout.SOUTH);
                spectraPanel.validate();
                spectraPanel.repaint();
                
                visThresP.removeAll();
                VisThresPanel tmp= new VisThresPanel(controller,dataImported);
                tmp.updateStats();
                visThresP.add(tmp);
                visThresP.validate();
                visThresP.repaint();
         
             repaint();
              
               this.cBEs =BEs;
               //imVals=null;
               
               int[] tVals={mn1,mx1,wid,hei,xmin,ymin,mxunc};
                  imVals=tVals;
                  IJ.log("Crop vals: "+mn1+" "+mx1+" "+wid+" "+hei+" "+xmin+" "+ymin+" "+mxunc);
                   IJ.log("Crop vals: "+imVals[0]+" "+imVals[1]+" "+imVals[2]+" "+imVals[3]+" "+imVals[4]+" "+imVals[5]+ " "+imVals[6]);
           
                updateImgs();
                IJ.log("Charts Updated");  
      
      
      
     } 
     
 public void updateImgs(){
      Thread processThread =new Thread(){
                    public void run(){
                          IJ.log("Crop vals: "+imVals[0]+" "+imVals[1]+" "+imVals[2]+" "+imVals[3]+" "+imVals[4]+" "+imVals[5]+ " "+imVals[6]);
      controller.drawTCGaussianROI(cBEs,imVals[0],imVals[1],imVals[2],imVals[3],imVals[4],imVals[5],imVals[6]);
              
       }
                };
                processThread.start();
 }

    
    
    
}
