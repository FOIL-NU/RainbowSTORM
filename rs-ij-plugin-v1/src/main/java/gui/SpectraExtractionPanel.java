/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ij.IJ;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import rstorm.Analysis;

import java.awt.Dimension;
import java.awt.Image;

import java.net.URL;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

import rstorm.RS_Help;



/**
 *
 * @author Janel L Davis
 */
public class SpectraExtractionPanel extends JPanel {
    
    Analysis controller;
    private static final String url = "sSMLM_Analysis.html";
   
    private JButton buttonFindSpectra;
    private JButton buttonPreview;
    private JButton buttonHelp;
    private JButton buttonDefaults;
    
    private JCheckBox checkboxAdParams;

    public static JFormattedTextField ftfBlinkingWidth;
    public static JFormattedTextField ftfSpectraWindowRng1;
    public static JFormattedTextField ftfSpectraWindowRng2; 
    public static JFormattedTextField ftfStpSize;
    
    public static JFormattedTextField ftfSpecWidth;
    public static  JCheckBox checkboxFitSpecWidth;
    private  JCheckBox checkboxRmvOLSpec;
    public static JFormattedTextField ftfVisStpSize;
    public static JFormattedTextField ftfYShift;
        
    private boolean caliLoaded;
    private boolean csvLoaded;
    private boolean imgReady;
    
    
    final int iSpR1 =650;
    final int iSpStp=1;
    final int iSpR2=750;
    final boolean bFtSw=true;
    final int iAvSw=40;
    final int iLPS=1;
    final int iYCmp=0;
    final boolean bRmOvSp=false;
    final boolean bAdSt=false;
    
    
    
    public SpectraExtractionPanel(Analysis controller) {
        this.controller = controller;
        setupView();
        setupListeners();
    }
    
    private void setupView(){
        Dimension d = new Dimension (100,20);
        buttonPreview = new JButton("Preview");
        buttonPreview.setPreferredSize(d);
        buttonPreview.setEnabled(false);
        buttonFindSpectra = new JButton("Run Analysis");
        buttonFindSpectra.setPreferredSize(d);
    
        buttonFindSpectra.setEnabled(false);
        csvLoaded=false;
        caliLoaded=false;
        imgReady=false;
       
        setBorder(BorderFactory.createTitledBorder("Process sSMLM Data"));
        GridBagLayout gbl = new GridBagLayout();
        
        setLayout(gbl);
        
        GridBagConstraints bc = new GridBagConstraints();
        bc.weightx=1;
        bc.weighty=1;
        bc.gridx = 0;
        bc.gridy = 0;
        bc.anchor = GridBagConstraints.CENTER;
         
       
        JPanel spWindowPanel= new JPanel();
        spWindowPanel.setLayout(new FlowLayout());
        
        spWindowPanel.add(new JLabel("Spectrum Window Range [Start : Step Size : End] [nm]: "));
        
      
        ftfSpectraWindowRng1 = new JFormattedTextField();
        ftfSpectraWindowRng1.setValue(new Integer(iSpR1));
        ftfSpectraWindowRng1.setColumns(4);
        spWindowPanel.add(ftfSpectraWindowRng1);
        
        spWindowPanel.add(new JLabel(" : "));
        
        
        ftfStpSize = new JFormattedTextField();
        ftfStpSize.setValue(new Integer(iSpStp));
         ftfStpSize.setColumns(4);
        spWindowPanel.add(ftfStpSize);
        
         spWindowPanel.add(new JLabel(" : "));
        
        ftfSpectraWindowRng2 = new JFormattedTextField();
       
        ftfSpectraWindowRng2.setValue(new Integer(iSpR2));
        ftfSpectraWindowRng2.setColumns(4);
         spWindowPanel.add(ftfSpectraWindowRng2);
         
        spWindowPanel.add(new JLabel("Remove Overlapping Spectra:"));
        checkboxRmvOLSpec = new JCheckBox();
        checkboxRmvOLSpec.setEnabled(true);
        checkboxRmvOLSpec.setSelected(bRmOvSp);
        spWindowPanel.add(checkboxRmvOLSpec);
    
        add(spWindowPanel,bc); 
      
        JPanel spSize = new JPanel();
        spSize.setLayout(new FlowLayout());
        
        spSize.add(new JLabel("Advanced Settings"));
        checkboxAdParams = new JCheckBox();
        checkboxAdParams.setEnabled(true);
        checkboxAdParams.setSelected(bAdSt);
        spSize.add(checkboxAdParams);
        
         spSize.add(new  JLabel ("Fit Spectrum Width [nm]: "));
        checkboxFitSpecWidth=new JCheckBox();
        
        checkboxFitSpecWidth.setEnabled(false);
        checkboxFitSpecWidth.setSelected(bFtSw);
       
        spSize.add(checkboxFitSpecWidth);
       
        spSize.add(new  JLabel ("Average Spectrum Width [nm]: "));
        ftfSpecWidth = new JFormattedTextField();
        ftfSpecWidth.setValue(new Double(iAvSw));
        ftfSpecWidth.setColumns(4);
        ftfSpecWidth.setEnabled(false);
        ftfSpecWidth.setEditable(false);
        spSize.add(ftfSpecWidth);
                
       
        bc.gridy++;
        add(spSize,bc);
        
        JPanel pxCompPanel = new JPanel();
        pxCompPanel.setLayout(new FlowLayout());
        
        pxCompPanel.add(new JLabel("Localization Pixel Shift: +/-"));
        
        
        ftfBlinkingWidth = new JFormattedTextField();
        ftfBlinkingWidth.setValue(new Integer(iLPS));
        ftfBlinkingWidth.setColumns(4);
        ftfBlinkingWidth.setEditable(false);
        ftfBlinkingWidth.setEnabled(false);
        pxCompPanel.add(ftfBlinkingWidth);
      
        pxCompPanel.add(new JLabel("Y-Compensation:"));
       
        ftfYShift = new JFormattedTextField();
        ftfYShift.setValue(new Integer(iYCmp));
        ftfYShift.setColumns(4);
        ftfYShift.setEditable(false);
        ftfYShift.setEnabled(false);
        pxCompPanel.add(ftfYShift);
      
        bc.gridy++;
        add(pxCompPanel,bc);
       
        JPanel exPanel = new JPanel();
        exPanel.setLayout(new FlowLayout());
    
        buttonDefaults = new JButton("Reset");
        buttonDefaults.setPreferredSize(d);
        buttonDefaults.setEnabled(true);
        exPanel.add(buttonDefaults);
      
        
        exPanel.add(buttonPreview);
        exPanel.add(buttonFindSpectra);
        
        bc.gridy++;
        bc.anchor= GridBagConstraints.CENTER;
        add(exPanel,bc);
        
         JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new FlowLayout());
        URL loc;
        try {
        loc = getClass().getClassLoader().getResource("helpbutton.png");
       
       ImageIcon img_icon =new ImageIcon(loc);
       Image img = img_icon.getImage();
       
       Image newimg = img.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
       img_icon = new ImageIcon(newimg); 
       
        buttonHelp =new JButton(img_icon);
        buttonHelp.setEnabled(true);
        buttonHelp.setPreferredSize(new Dimension (20,20));
        helpPanel.add(buttonHelp);
     
        bc.anchor=GridBagConstraints.LAST_LINE_END;
        
       add(helpPanel, bc);
        } catch (Exception ex) {
            Logger.getLogger(ImportsSMLMPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
      
     
    }
    
    private void setupListeners() {
        
        buttonDefaults.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ftfSpectraWindowRng1.setValue(iSpR1);
                ftfSpectraWindowRng2.setValue(iSpR2);
                ftfStpSize.setValue(iSpStp);
                
                checkboxFitSpecWidth.setSelected(bFtSw);
                ftfSpecWidth.setValue(iAvSw);
                
                checkboxRmvOLSpec.setSelected(bRmOvSp);
                
                ftfBlinkingWidth.setValue(iLPS);
                ftfYShift.setValue(iYCmp);
                
                checkboxAdParams.setSelected(bAdSt);
                                
                
              }
            
                 
        });
        buttonFindSpectra.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             
                  Thread processThread =new Thread(){
                    public void run(){
                
  
                try{
                  ftfYShift.commitEdit();  
                 ftfBlinkingWidth.commitEdit();
                  ftfSpectraWindowRng1.commitEdit();
                   ftfSpectraWindowRng2.commitEdit();
                   ftfStpSize.commitEdit();
                  
                   
                 int ycomp= ((Number)ftfYShift.getValue()).intValue();
                 int bw=((Number)ftfBlinkingWidth.getValue()).intValue();
              
                 int rng1=((Number)ftfSpectraWindowRng1.getValue()).intValue();
                 int rng2=((Number)ftfSpectraWindowRng2.getValue()).intValue();
                 int stp=((Number)ftfStpSize.getValue()).intValue();
                 
                gui.SpatialImageParameterPanel.ftfPixSize.commitEdit();
                 gui.SpatialImageParameterPanel.ftfEmGain.commitEdit();
                 gui.SpatialImageParameterPanel.ftfADU.commitEdit();
                 gui.SpatialImageParameterPanel.ftfBsLv.commitEdit();
                 
                 int px_Size=((Number)gui.SpatialImageParameterPanel.ftfPixSize.getValue()).intValue();
               
                 controller.setPxSize((double)px_Size);
                 int EMG=((Number)gui.SpatialImageParameterPanel.ftfEmGain.getValue()).intValue();
                 double adu= ((Number)gui.SpatialImageParameterPanel.ftfADU.getValue()).doubleValue();
                 int bsLv=((Number)gui.SpatialImageParameterPanel.ftfBsLv.getValue()).intValue();
                
                 int fitFlg=1;
                 double specWidth=0;
                 if(checkboxFitSpecWidth.isSelected()==true&& ftfSpecWidth.isEnabled()==false)
                 {
                     specWidth=1;
                     fitFlg=1;
                 }
                 else
                 {
                     ftfSpecWidth.commitEdit();
                
                     specWidth=((Number)ftfSpecWidth.getValue()).doubleValue();
                     fitFlg=0;
                 }
                 
                 boolean flg1 = validRange(rng1,rng2,stp);
                 boolean flg2 = validParams(bw,px_Size,EMG,adu,bsLv,specWidth,ycomp);
                
                 if(flg1&&flg2){
                     int rmvOL=1;
                     if(checkboxRmvOLSpec.isSelected()==false){
                      rmvOL=0;  
                     }
             
                 controller.extractSpectra(bw, rng1,  rng2,  stp, px_Size,EMG,adu,bsLv,fitFlg,specWidth,ycomp,rmvOL);
              
                  IJ.showStatus("sSMLM data Processed!");
                 
                 }
                  }catch(ParseException e1){
                     IJ.error("Invalid Input");
                 }
                  catch (OutOfMemoryError ex) {
                     IJ.error("Please increase your ImageJ Heap: Edit -> Options -> Memory & Threads -> Increase maximum memory");
                 }
            
       }
                };
                processThread.start();
            
            }
            
                 
        });
        
          buttonPreview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              
               try{
                 ftfYShift.commitEdit();  
                 ftfBlinkingWidth.commitEdit();
                  ftfSpectraWindowRng1.commitEdit();
                   ftfSpectraWindowRng2.commitEdit();
                   ftfStpSize.commitEdit();
                   
                   
                int ycomp =((Number)ftfYShift.getValue()).intValue();
                 int bw=((Number)ftfBlinkingWidth.getValue()).intValue();
                  
                int rng1=((Number)ftfSpectraWindowRng1.getValue()).intValue();
                int rng2=((Number)ftfSpectraWindowRng2.getValue()).intValue();
                 int stp=((Number)ftfStpSize.getValue()).intValue();
                 
                 
                    
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
                 if(checkboxFitSpecWidth.isSelected()==true&& ftfSpecWidth.isEnabled()==false)
                 {
                     specWidth=1;
                     fitFlg=1;
                 }
                 else
                 {  
                     ftfSpecWidth.commitEdit();
                     specWidth=((Number)ftfSpecWidth.getValue()).doubleValue();
                     fitFlg=0;
                 }
                 
                 boolean flg1 = validRange(rng1,rng2,stp);
                 boolean flg2 = validParams(bw,px_Size,EMG,adu,bsLv,specWidth,ycomp);
                
                 if(flg1&&flg2){
                  int rmvOL=1;
                     if(checkboxRmvOLSpec.isSelected()==false){
                      rmvOL=0;  
                     } 
                 controller.previewSpectra(bw, rng1,  rng2,  stp, px_Size,EMG,adu,bsLv,fitFlg,specWidth, ycomp,rmvOL);
                 buttonPreview.setEnabled(false);
                
                  IJ.showStatus("sSMLM data Processed!");
                 
                 }
                  }catch(ParseException e1){
                     IJ.error("Invalid Input");
                 }
                  catch (OutOfMemoryError ex) {
                     IJ.error("Please increase your ImageJ Heap: Edit -> Options -> Memory & Threads -> Increase maximum memory");
                 }
            
            }
              
        });
        
      
          controller.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(Analysis.PRE_CLOSED)) {
                    buttonPreview.setEnabled(true);
            }
            }
        });
        controller.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(Analysis.CSV_LOADED)) {
                     csvLoaded =controller.getCSVFileLoaded();
                    if(csvLoaded&&caliLoaded && imgReady){
                    buttonFindSpectra.setEnabled(true);
                     buttonPreview.setEnabled(true);
                    
                }else
                {
                  
                    csvLoaded =controller.getCSVFileLoaded();
                }
                }
            }
        });
        
   
          controller.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(Analysis.CALI_LOADED)) {
                    caliLoaded=controller.getCaliFileLoaded();
                    if(csvLoaded && imgReady&&caliLoaded){
                     buttonFindSpectra.setEnabled(true);
                       buttonPreview.setEnabled(true);
                }else{
                    caliLoaded=controller.getCaliFileLoaded();
                    
                }
                }
            }
        });
          
             controller.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(Analysis.BACKGROUND_READY)) {
                      imgReady=controller.getImgsReady();
                    if(csvLoaded && caliLoaded&&imgReady){
                     buttonFindSpectra.setEnabled(true);
                       buttonPreview.setEnabled(true);
                    }
                else{
                    imgReady=controller.getImgsReady();
                    
                }
                }
            }
        });
             
         checkboxAdParams.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               
               if (checkboxAdParams.isSelected()==true){
                   checkboxFitSpecWidth.setEnabled(true);
                   ftfYShift.setEnabled(true);
                   ftfYShift.setEditable(true);
                   
                   ftfBlinkingWidth.setEditable(true);
                   ftfBlinkingWidth.setEnabled(true);
               }else{
                  
                  checkboxFitSpecWidth.setEnabled(false);
                   ftfYShift.setEnabled(false);
                   ftfYShift.setEditable(false);
                   
                   ftfBlinkingWidth.setEditable(false);
                   ftfBlinkingWidth.setEnabled(false);
               }
                   
                   
           }
        });  
             
           
   
       checkboxFitSpecWidth.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               
               if (checkboxFitSpecWidth.isSelected()==false){
                   ftfSpecWidth.setEnabled(true);
                   ftfSpecWidth.setEditable(true);
               }else{
                  
                   ftfSpecWidth.setEnabled(false);
                   ftfSpecWidth.setEditable(false);
               }
                   
                   
           }
        });  
       
         buttonHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RS_Help rsHelp = new RS_Help();  
                rsHelp.launchHelp(url);
                
            }
    
    });
             
    }
    
    public static boolean validRange(int rng1, int rng2, int stp){

        boolean flg= false;
        boolean flg1=rng1<380||rng2>870||rng1<0||rng2<0||rng1>=rng2;
        if(flg1){
            IJ.error("Invalid spectrum Range selected. Please select values from 380 nm : 870 nm)");

            }
    boolean flg2 = stp>(rng2-rng1)||stp<1;
    if(flg2){
           IJ.error("Invalid Step Size selected");   
    }


    if(!flg1&&!flg2){
        flg=true;

    }
    return flg;
    }
    
   public static boolean validParams(int bw, int px_Size,int EMG,double adu,int bsLv,double specWidth,int yComp){
       boolean flg= false; 
       int inv=0;
    
    if(bw<=0){
        IJ.error("Localization Pixel Shift must be positive and non-zero");
        inv++;
        
    }
    
     if(bw<1||bw>3){
        IJ.error("Localization Pixel Shift must range from 1 to 3");
        inv++;
        
    }
    
    if(px_Size<=0){
         IJ.error("Pixel Size must be positive and non-zero");
        inv++;
    }
    
       
    if(EMG<=0){
         IJ.error("EM Gain must be positive and non-zero");
        inv++;
    } 
    
        
    if(adu<=0){
         IJ.error("Analog to digital unit (ADU) must be positive and non-zero");
        inv++;
    }
    
        
    if(bsLv<0){
         IJ.error("Camera base level must be positive");
        inv++;
    }
    
        
    if(specWidth<=0){
         IJ.error("Spectrum Width must be positive and non-zero");
        inv++;
    }
    
    
    if(yComp<-8||yComp>8){
        IJ.error("Y-Compensation must range from -8 to 8");
        inv++;
        
    }
    
     if(inv==0){
         flg=true;
     }

       return flg;    
   }
           
}


