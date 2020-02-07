/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ij.IJ;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import rstorm.Analysis;
import java.awt.Dimension;
import java.text.ParseException;



/**
 *
 * @author Janel Davis
 */
public class SpatialImageParameterPanel extends JPanel {
    
    private Analysis controller;
    private JButton buttonSaveCropPositions;
    
    public static JTextField textfieldCropPositions;
    private JButton buttonLoadCropPositions;
    private JButton buttonCropImage;
    private boolean flg_crp;
    //Camera Parameters
    public static JFormattedTextField ftfPixSize;
    public static JFormattedTextField ftfBsLv;
    public static JFormattedTextField ftfADU;
    public static JFormattedTextField ftfEmGain;
    
    //Cropping Parameters
    public static JFormattedTextField ftfXPosition;
    public static JFormattedTextField ftfYPosition;
    public static JFormattedTextField ftfWidth;
    public static JFormattedTextField ftfHeight;
    
    //Visualization Parameters
    public static JFormattedTextField ftfMag;
 
    
    
      public SpatialImageParameterPanel(Analysis controller,boolean flg) {
        flg_crp=flg;
        this.controller = controller;
        setupView();
        setupActionListeners();
      
    }
    
    private void setupView(){
        
        setBorder(BorderFactory.createTitledBorder("Camera Setup and Cropping Parameters"));
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[]{50, 200, 16};
        
        setLayout(gbl);
        
        GridBagConstraints c = new GridBagConstraints();
           c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, 0, 10);
        c.anchor = GridBagConstraints.LINE_END;
        
        add(new JLabel("Camera Setup: "), c);
        c.gridy ++;
        add(new JLabel("Pixel Size [nm]: "), c);
        c.gridy ++;
        add(new JLabel("Photoelectrons per A/D count : "), c);
        c.gridy ++;        
        add(new JLabel("Base level [A/D count]: "), c);
        c.gridy ++;
        add(new JLabel("EM Gain: "), c);
        
         
        c.gridx ++; c.gridy = 1;
        c.weightx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        
        int iDpxsize = 100;
        ftfPixSize = new JFormattedTextField();
        ftfPixSize.setValue(new Integer(iDpxsize));
        ftfPixSize.setColumns(4);
     
        int iDbslv= 200;
        ftfBsLv = new JFormattedTextField();
        ftfBsLv.setValue(new Integer(iDbslv));
        ftfBsLv.setColumns(4);
      
        double dDADU= 4.6;
        ftfADU = new JFormattedTextField();
        ftfADU.setValue(new Double (dDADU));
        ftfADU.setColumns(4);
       
        int iDEmGain = 100;
        ftfEmGain= new JFormattedTextField();
        ftfEmGain.setValue(new Integer(iDEmGain ));
        ftfEmGain.setColumns(4);
       
        add(ftfPixSize, c);
        c.gridy ++;
        add(ftfADU, c);
        c.gridy ++;
        add(ftfBsLv, c);
        c.gridy ++;
        add(ftfEmGain, c);
      
        c.gridy=0;
        c.gridx ++;
        
        add(new JLabel("Cropping Parameters: "), c);
        c.gridy ++;
        add(new JLabel("X Position: "), c);
        c.gridy ++;
        add(new JLabel("Y Position: "), c);

        c.gridy ++;
        add(new JLabel("Width: "), c);
        c.gridy ++;
        add(new JLabel("Height: "), c);
        
        
        c.gridx ++; c.gridy = 1;
        c.weightx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        
        int iDxpos = 0;         
        ftfXPosition = new JFormattedTextField();
        ftfXPosition.setValue(new Integer(iDxpos));
        ftfXPosition.setColumns(4);
       
        int iDypos = 0;  
        ftfYPosition = new JFormattedTextField();
        ftfYPosition.setValue(new Integer(iDypos));
        ftfYPosition.setColumns(4);
      
        int iDwidth = 0;
        ftfWidth = new JFormattedTextField();
        ftfWidth.setValue(new Integer(iDwidth));
        ftfWidth.setColumns(4);
       
        int iDheight = 0;
        ftfHeight= new JFormattedTextField();
        ftfHeight.setValue(new Integer(iDheight));
        ftfHeight.setColumns(4);
        
        add(ftfXPosition, c);
        c.gridy ++;
        add(ftfYPosition, c);
        c.gridy ++;
        add(ftfWidth, c);
        c.gridy ++;
        add(ftfHeight, c);
    
         Dimension d = new Dimension (150,20);
        
        c.gridx = 1; c.gridy ++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        buttonCropImage = new JButton("Crop sSMLM Image");
        buttonCropImage.setPreferredSize(d);
        buttonCropImage.setEnabled(true);
         if(flg_crp){
        add(buttonCropImage, c);
         }
            c.gridx = 0; c.gridy ++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
   
        c.anchor = GridBagConstraints.CENTER;
     
        
       add(new JLabel("Parameter File Path:"), c);
        
       c.gridx ++; 
     
        textfieldCropPositions = new JTextField();
        textfieldCropPositions.setColumns(20);
        add(textfieldCropPositions, c);
        
        c.gridx ++; 
    
        buttonLoadCropPositions = new JButton("Load Parameters");
        buttonLoadCropPositions.setPreferredSize(d);
        buttonLoadCropPositions.setEnabled(true);
       
       add(buttonLoadCropPositions, c);
        
        c.gridx = 1; c.gridy ++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        buttonSaveCropPositions = new JButton("Save Parameters");
        buttonSaveCropPositions.setPreferredSize(d);
        buttonSaveCropPositions.setEnabled(false);
        if(flg_crp)
        add(buttonSaveCropPositions, c);
    }
    
    private void setupActionListeners() {
         buttonSaveCropPositions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Double> crp_prms = new ArrayList();
                ArrayList<String> prmNames = new ArrayList();
                
                double xpos=Double.parseDouble(ftfXPosition.getText());
                double ypos=Double.parseDouble(ftfYPosition.getText());
                double wid=Double.parseDouble(ftfWidth.getText());
                double hgh=Double.parseDouble(ftfHeight.getText());
                   
                double pxSz=Double.parseDouble(ftfPixSize.getText());
                double bsLv=Double.parseDouble(ftfBsLv.getText());
                double adu=Double.parseDouble(ftfADU.getText());
                double emG=Double.parseDouble(ftfEmGain.getText());
                
                boolean flg = isValidParams(xpos,ypos,wid,hgh,pxSz,bsLv,adu,emG);
                if(flg){
                crp_prms.add(xpos);
                crp_prms.add(ypos);
                crp_prms.add(wid);
                crp_prms.add(hgh);
                
                crp_prms.add(pxSz);
                crp_prms.add(bsLv);
                crp_prms.add(adu);
                crp_prms.add(emG);
             
                prmNames.add("X Postion");
                prmNames.add("Y Postion");
                prmNames.add("Width");
                prmNames.add("Height");
                
                prmNames.add("Pixel Size[nm]");
                prmNames.add("Base Level");
                prmNames.add("ADU");
                prmNames.add("EM Gain");
             
                
                try {
                    controller.saveCropPos(prmNames,crp_prms);
                } catch (IOException ex) {
                    Logger.getLogger(SpatialImageParameterPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                }else{
                    IJ.error("Inputs must be real and non-ngative");
                }
               
            }
                
        });
         
             controller.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(Analysis.IMAGES_READY)) {
                    buttonSaveCropPositions.setEnabled(true);
                }
            }
        });
    
          
             buttonCropImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
             
                int[] cropPositions = controller.getROICropPositions();
                 
              if (cropPositions[0]==0&&cropPositions[1]==0&&cropPositions[2]==0&&cropPositions[3]==0){
                int xpos =(int)Double.parseDouble(ftfXPosition.getText());
                int ypos =(int)Double.parseDouble(ftfYPosition.getText());
                int wid= (int)Double.parseDouble(ftfWidth.getText());
                int height=(int)Double.parseDouble(ftfHeight.getText());
                               
                boolean flg = isValidCrpParams(xpos,ypos,wid,height);
                if(flg){
               cropPositions[0]=xpos;
               cropPositions[1]=ypos;
               cropPositions[2]=wid;
               cropPositions[3]=height;
               controller.updateCropping(cropPositions);
               
               setParams();
               
               
                       }else{
                    IJ.error("Please select region to be cropped or specificy cropping parameters");
                 }
                 }else  
                          
                   ftfXPosition.setValue(cropPositions[0]);
                   ftfYPosition.setValue(cropPositions[1]);
                   ftfWidth.setValue(cropPositions[2]);
                   ftfHeight.setValue(cropPositions[3]);

                   controller.updateCropping(cropPositions);
                   setParams();
                  
            }
        }); 
             
             
             
          buttonLoadCropPositions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              
               ArrayList<Double> crp_params;
                try {
                  
                 crp_params= controller.loadCropPos(textfieldCropPositions.getText());
                
                  if (crp_params==null||crp_params.isEmpty()){
                     IJ.error("Incorrect File Loaded");
                      
                  }else{
               
                           
               int xpos= crp_params.get(0).intValue();
               int ypos=crp_params.get(1).intValue();
               int wid = crp_params.get(2).intValue();
               int hght = crp_params.get(3).intValue();
               
               int pxsiz= crp_params.get(4).intValue();
               int bslv =crp_params.get(5).intValue();
               double adu = crp_params.get(6);
               int emg =crp_params.get(7).intValue();
               
                 boolean flg = isValidParams(xpos,ypos,wid,hght,pxsiz,bslv,(int) adu,emg);
                if(flg){
               ftfXPosition.setValue(xpos);
               ftfYPosition.setValue(ypos);
               ftfWidth.setValue(wid);
               ftfHeight.setValue(hght);
                              
               ftfPixSize.setValue(pxsiz);
               ftfBsLv.setValue(bslv);
               ftfADU.setValue(adu);
               ftfEmGain.setValue(emg);
             
               
               int[] cropPositions={xpos,ypos,wid,hght};
               
               if(flg_crp){
                    controller.updateCropping(cropPositions);
                    controller.setADU(adu);
                    controller.setPxSize(pxsiz);
                controller.setbsLv(bslv);
                controller.setGain(emg);
                controller.setCrppos(cropPositions);
               }
               
                }else{ 
                   
                    IJ.error("Inputs must be real and non-negative");
                      }
                  }
                } catch (IOException ex) {
                    Logger.getLogger(SpatialImageParameterPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                
               
            }
        });
    }
         
          
      public boolean isValidCrpParams(int xpos,int ypos,int wid,int hgh){
                  
          
          boolean flg=wid>0&&hgh>0&&xpos>=0&&ypos>=0;
          
          return flg;
          
      }
      
      public boolean isValidParams(double xpos,double ypos,double wid,double hgh,double pxSz,double bsLv,double adu,double emG){
          
          boolean flg1=wid>0&&hgh>0&&xpos>=0&&ypos>=0;
          boolean flg2=pxSz>=0&&bsLv>=0&&adu>=0&&emG>=0;
          boolean flg =flg1&&flg2;
          
          
          return flg;
      }
      
          public boolean isValidParams(int xpos,int ypos,int wid,int hgh,int pxSz,int bsLv,int  adu,int emG){
          
          boolean flg1=wid>0&&hgh>0&&xpos>=0&&ypos>=0;
          boolean flg2=pxSz>=0&&bsLv>=0&&adu>=0&&emG>=0;
          boolean flg =flg1&&flg2;
          
          
          return flg;
      }
    
     
      public void setParams(){
                 
          
                try{
                               
                 
                ftfPixSize.commitEdit();
                ftfEmGain.commitEdit();
                ftfADU.commitEdit();
                ftfBsLv.commitEdit();
                 
                 int pxSz=((Number)ftfPixSize.getValue()).intValue();
                 int emG=((Number)ftfEmGain.getValue()).intValue();
                 double adu= ((Number)ftfADU.getValue()).doubleValue();
                 int bsLv=((Number)ftfBsLv.getValue()).intValue();
          
                int xpos=Integer.parseInt(ftfXPosition.getText());
                int ypos=Integer.parseInt(ftfYPosition.getText());
                int wid=Integer.parseInt(ftfWidth.getText());
                int hgh=Integer.parseInt(ftfHeight.getText());
               
                int[] crpPos={xpos,ypos,wid,hgh};
                
                boolean flg = isValidParams(xpos,ypos,wid,hgh,pxSz,bsLv,adu,emG);
               //if(flg){
                controller.setADU(adu);
                controller.setPxSize((double)pxSz);
                
                controller.setbsLv(bsLv);
                controller.setGain(emG);
                controller.setCrppos(crpPos);
              // }
                
              
          }catch(ParseException e1){
                     IJ.error("Invalid Input");
                 }
          
      }    
    
        
    
   
    
   
    
}
