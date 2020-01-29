/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ij.IJ;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JFormattedTextField;

import rstorm.Calibration;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Window;
import java.net.URL;
import java.text.ParseException;


import java.util.ArrayList;
import javax.swing.ImageIcon;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
/**
 *
 * @author Janel
 */
public class sSMLMCalibrationGUI extends JPanel{
    
   
    private Calibration controller;
    
    private static final String url = "sSMLM_Calibration.html";
    private static final String ver = "2020_01";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private JButton buttonCalibrate;
    private JButton buttonSet;
    private JButton buttonSave;
    private JButton buttonNext;
    private JButton buttonBack;
    private JLabel caliLabel;
    
    private JFormattedTextField ftfSP;
    private JFormattedTextField ftfWv1;
    private JFormattedTextField ftfWv2;
    private JFormattedTextField ftfWv3;
    private JFormattedTextField ftfWv4;
    private JFormattedTextField ftfWv5;
    
    private JFormattedTextField ftfPk0;
    private JFormattedTextField ftfPk1;
    private JFormattedTextField ftfPk2;
    private JFormattedTextField ftfPk3;
    private JFormattedTextField ftfPk4;
    private JFormattedTextField ftfPk5;
   
   private JComboBox cmbDE;
   private JComboBox cmbSource;
   private JButton buttonHelp;
   private JButton buttonReset;
 
 
    
    
    public sSMLMCalibrationGUI(Calibration controller){
        this.controller =controller;
        setupView();
        setupListeners();
        
    }
    
     private void setupView() {
        setBorder(BorderFactory.createTitledBorder("Calibration Options"));
        
        setLayout(new GridBagLayout());
         
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx=1;
        bc.gridy=0;
        bc.anchor = GridBagConstraints.CENTER;
        bc.weightx=1;
        bc.weighty=1;
        
        JPanel pkPanel = new JPanel();
        pkPanel.setLayout(new FlowLayout());
       
        pkPanel.add(new JLabel("Spatial Peak Position:"));
       
        int iDPk0 =0;
        ftfPk0= new JFormattedTextField();
        ftfPk0.setValue(new Integer(iDPk0));
        ftfPk0.setColumns(4);
        ftfPk0.setEditable(true);
        pkPanel.add(ftfPk0);
        
        pkPanel.add(new JLabel("Spectral Peak Positions:"));
        int iDPk1 =0;
        ftfPk1= new JFormattedTextField();
        ftfPk1.setValue(new Integer(iDPk1));
        ftfPk1.setColumns(4);
        ftfPk1.setEditable(false);
        pkPanel.add(ftfPk1);
        
        int iDPk2 =0;
        ftfPk2= new JFormattedTextField();
        ftfPk2.setValue(new Integer(iDPk2));
        ftfPk2.setColumns(4);
        ftfPk2.setEditable(false);
        pkPanel.add(ftfPk2);
        
        
        int iDPk3 =0;
        ftfPk3= new JFormattedTextField();
        ftfPk3.setValue(new Integer(iDPk3));
        ftfPk3.setColumns(4);
        ftfPk3.setEditable(false);
        pkPanel.add(ftfPk3);
        
         int iDPk4 =0;
        ftfPk4= new JFormattedTextField();
        ftfPk4.setValue(new Integer(iDPk4));
        ftfPk4.setColumns(4);
        ftfPk4.setEditable(false);
        pkPanel.add(ftfPk4);
        
         int iDPk5 =0;
        ftfPk5= new JFormattedTextField();
        ftfPk5.setValue(new Integer(iDPk5));
        ftfPk5.setColumns(4);
        ftfPk5.setEditable(false);
        pkPanel.add(ftfPk5);
       
        buttonSet= new JButton("Set");
        buttonSet.setEnabled(true);
        buttonSet.setPreferredSize(new Dimension(100,30));
        pkPanel.add(buttonSet);
       
       
        buttonBack= new JButton("Previous");
        buttonBack.setEnabled(false);
        buttonBack.setPreferredSize(new Dimension(100,30));
        pkPanel.add(buttonBack);
        
        buttonNext= new JButton("Next");
        buttonNext.setEnabled(false);
        buttonNext.setPreferredSize(new Dimension(100,30));
        pkPanel.add(buttonNext);
          
        add(pkPanel,bc);
        
         bc.gridx=1;
        bc.gridy++;
        bc.anchor = GridBagConstraints.CENTER;
        bc.weightx=1;
        bc.weighty=1;
        
         JPanel wvPanel = new JPanel();
        wvPanel.setLayout(new FlowLayout());
       
        wvPanel.add(new JLabel("Slit/Emitter Postion:"));
        
        double dDSP =0.0;
        ftfSP= new JFormattedTextField();
        ftfSP.setValue(new Double(dDSP));
        ftfSP.setColumns(4);
        ftfSP.setEditable(false);
        wvPanel.add(ftfSP);
                
        wvPanel.add(new JLabel("Wavelength of Peaks:"));
        double dDWv1 =487.7;
        ftfWv1= new JFormattedTextField();
        ftfWv1.setValue(new Double(dDWv1));
        ftfWv1.setColumns(4);
        wvPanel.add(ftfWv1);
        
        double dDWv2 =546.5;
        ftfWv2= new JFormattedTextField();
        ftfWv2.setValue(new Double(dDWv2));
        ftfWv2.setColumns(4);
        wvPanel.add(ftfWv2);
       
        double dDWv3 =611.6;
        ftfWv3= new JFormattedTextField();
        ftfWv3.setValue(new Double(dDWv3));
        ftfWv3.setColumns(4);
         wvPanel.add(ftfWv3);
         add(wvPanel,bc);
         
        double dDWv4 =0;
        ftfWv4= new JFormattedTextField();
        ftfWv4.setValue(new Double(dDWv4));
        ftfWv4.setColumns(4);
        wvPanel.add(ftfWv4);
        add(wvPanel,bc);
        
         
        double dDWv5 =0;
        ftfWv5= new JFormattedTextField();
        ftfWv5.setValue(new Double(dDWv5));
        ftfWv5.setColumns(4);
        wvPanel.add(ftfWv5);
         
        add(wvPanel,bc);
                
        bc.anchor = GridBagConstraints.CENTER;
        bc.weightx=1;
        bc.weighty=1;
              
        bc.gridx=1;
        bc.gridy++;
        
        JPanel caliPanel = new JPanel();
        caliPanel.setLayout(new FlowLayout());
        caliPanel.add(new JLabel("Calibration Source:"));
         String[] src_options ={"Calibration Lamp","Fluorescent Beads"};
        cmbSource = new JComboBox(src_options);
        caliPanel.add(cmbSource);
       
        caliPanel.add(new JLabel("Disperive Element:"));
        
        String[] de_options ={"Grating","Prism (Fitting-2nd Order Polynomial)","Prism (Fitting-3rd Order Polynomial)"};
        cmbDE = new JComboBox(de_options);
        
        caliPanel.add(cmbDE);
        
        buttonCalibrate= new JButton("Calibrate");
        buttonCalibrate.setEnabled(true);
        buttonCalibrate.setPreferredSize(new Dimension(100,30));
       
        caliPanel.add(buttonCalibrate);
        
        buttonSave= new JButton("Save");
        buttonSave.setEnabled(false);
        buttonSave.setPreferredSize(new Dimension(100,30));
        caliPanel.add(buttonSave);
        
        buttonReset= new JButton("Reset");
        buttonReset.setEnabled(true);
        buttonReset.setPreferredSize(new Dimension(100,30));
        caliPanel.add(buttonReset);
        add(caliPanel, bc);
        
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
        bc.anchor=GridBagConstraints.LAST_LINE_END;
        
       add(helpPanel, bc);
        } catch (Exception ex) {
            Logger.getLogger(sSMLMCalibrationGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
               
    }
      private void setupListeners() {
        buttonCalibrate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            
                double d0=  Double.parseDouble((ftfSP.getText()).replace(",",""));
                double d1=  Double.parseDouble((ftfWv1.getText()).replace(",",""));
                double d2=  Double.parseDouble((ftfWv2.getText()).replace(",",""));
                double d3=  Double.parseDouble((ftfWv3.getText()).replace(",",""));
                double d4=  Double.parseDouble((ftfWv4.getText()).replace(",",""));
                double d5=  Double.parseDouble((ftfWv5.getText()).replace(",",""));
                
                int p0=  Integer.parseInt((ftfPk0.getText()).replace(",",""));
                int p1=  Integer.parseInt((ftfPk1.getText()).replace(",",""));
                int p2=  Integer.parseInt((ftfPk2.getText()).replace(",",""));
                int p3=  Integer.parseInt((ftfPk3.getText()).replace(",",""));
                int p4=  Integer.parseInt((ftfPk4.getText()).replace(",",""));
                int p5=  Integer.parseInt((ftfPk5.getText()).replace(",",""));
                
                int idx= cmbDE.getSelectedIndex();
                
                 ArrayList<Double> cWvs= new ArrayList<Double>();
                 ArrayList<Integer> cPks= new ArrayList<Integer>();
                               
                 if(p0>0){
                     cWvs.add(d0);
                     cPks.add(p0);
                 
                 
                  if(d1>0&&p1>0){
                     cWvs.add(d1);
                     cPks.add(p1);
                 }else if(d1>0||p1>0){
                     
                     IJ.showMessage("Input Ignored:Unbalanced peaks position and calibration values");
                      
                  }
                  
                     if(d2>0&&p2>0){
                     cWvs.add(d2);
                     cPks.add(p2);
                 }else if(d2>0||p2>0){
                     
                     IJ.showMessage("Input Ignored:Unbalanced peaks position and calibration values");
                      
                  }
                  
                    if(d3>0&&p3>0){
                     cWvs.add(d3);
                     cPks.add(p3);
                 }else if(d3>0||p3>0){
                     
                     IJ.showMessage("Input Ignored:Unbalanced peaks position and calibration values");
                      
                  }
                    
                       if(d4>0&&p4>0){
                     cWvs.add(d4);
                     cPks.add(p4);
                 }else if(d4>0||p4>0){
                     
                     IJ.showMessage("Input Ignored:Unbalanced peaks position and calibration values");
                      
                  }
                       
                   if(d5>0&&p5>0){
                     cWvs.add(d5);
                     cPks.add(p5);
                 }else if(d5>0||p5>0){
                     
                     IJ.showMessage("Input Ignored:Unbalanced peaks position and calibration values");
                      
                  }
                  
                 int min_dg=4;
                 int min_pr=5;
                 
                boolean flg=false;
                
                 if((idx==0||idx==1)&&cWvs.size()>=min_dg&&cPks.size()>=min_dg){
                     flg=true;
                 }
                 if(idx==2&&cWvs.size()>=min_pr&&cPks.size()>=min_pr){
                     flg=true;
                 }
                
               
                 if(flg){
                                    
                    
                int iVal=isValid(cWvs,cPks);
                if(iVal==0){
                buttonSave.setEnabled(true);  
                int idx2=cmbSource.getSelectedIndex();
                controller.runCalibration(idx,idx2,cWvs,cPks);
                }else{
                        if(iVal==1){
                    IJ.error("Invalid Input: Spectral peaks cannot be less than or equal to spatial image position");
                                 }
                       
                        if(iVal==2){
                        IJ.error("Invalid Input: Duplicate inputs are not allowed");
                        }
                        
                         if(iVal==3){
                        IJ.error("Invalid Input: Wavelength inputs must be from 400 nm too 850 nm");
                        }
              
            }
            }else{
                   
                     if(idx==0||idx==1){
                        
                         IJ.error("Insufficient Points: At least 3 spectral peaks are needed for calibration using a grating");
                     }else{
                             
                             
                         IJ.error("Insufficient Points: At least 4 specrtal peaks are needed for calibration using a prism");
                             
                     }
                 }
                 }else{
                      
                    IJ.error("Invalid Input: Spatial image position cannot be 0");
                                 
                     
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
           //dialog.setdef 
           dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE); //for use within modal dialog
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
    buttonSet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               
                buttonBack.setEnabled(true);
                buttonNext.setEnabled(true);
               
                int pk = controller.setFromPlot();
                if(pk>=0){
                int flg=0;
                if (ftfPk0.isEditable()==true)
                {flg=1;}
                 if (ftfPk1.isEditable()==true)
                {flg=2;}
                  if (ftfPk2.isEditable()==true)
                {flg=3;}
                   if (ftfPk3.isEditable()==true)
                {flg=4;}
                    if (ftfPk4.isEditable()==true)
                {flg=5;}
                     if (ftfPk5.isEditable()==true)
                {flg=6;}
                   
                String outx= Integer.toString(flg);
                
                
                switch(flg){
                    case 1:
                        ftfPk0.setValue(new Integer(pk));
                        ftfPk0.setEditable(false);
                        ftfPk1.setEditable(true);
                      
                        break;
                    case 2:
                        ftfPk1.setValue(new Integer(pk));
                        ftfPk1.setEditable(false);
                        ftfPk2.setEditable(true);
                        break;
                    case 3:
                        ftfPk2.setValue(new Integer(pk));
                        ftfPk2.setEditable(false);
                        ftfPk3.setEditable(true);
                         break;
                    case 4:
                        ftfPk3.setValue(new Integer(pk));
                        ftfPk3.setEditable(false);
                        ftfPk4.setEditable(true);
                         break;
                     case 5:
                        ftfPk4.setValue(new Integer(pk));
                        ftfPk4.setEditable(false);
                        ftfPk5.setEditable(true);
                         break;
                    case 6:
                        ftfPk5.setValue(new Integer(pk));
                        ftfPk5.setEditable(false);
                        buttonNext.setEnabled(false);
                        buttonBack.setEnabled(true);
                        break;
                     default:
                         String outx2= Integer.toString(flg);
                   
                }
              
            }else{
                    IJ.error("Pixel inputs must be zero or positve whole numbers.");
                }
            }
            
    });
            
         buttonBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonNext.setEnabled(true);
                int flg=0;
                if (ftfPk0.isEditable()==true)
                {flg=1;}
                 if (ftfPk1.isEditable()==true)
                {flg=2;}
                  if (ftfPk2.isEditable()==true)
                {flg=3;}
                   if (ftfPk3.isEditable()==true)
                {flg=4;}
                  if (ftfPk4.isEditable()==true)
                {flg=5;}
                    if (ftfPk5.isEditable()==true)
                {flg=6;}
                 
                String outx= Integer.toString(flg);
                
                switch(flg){
                    case 1:
                       
                        ftfPk0.setEditable(true);
                       
                        break;
                    case 2:
                       
                        ftfPk0.setEditable(true);
                        ftfPk1.setEditable(false);
                         buttonBack.setEnabled(false);
                        break;
                    case 3:
                     
                        ftfPk1.setEditable(true);
                        ftfPk2.setEditable(false);
                         break;
                    case 4:
                        ftfPk2.setEditable(true);
                        ftfPk3.setEditable(false);
                        break;
                     case 5:
                        ftfPk3.setEditable(true);
                        ftfPk4.setEditable(false);
                        break;
                      case 6:
                        ftfPk4.setEditable(true);
                        ftfPk5.setEditable(false);
                        buttonNext.setEnabled(false);
                        break;
                     default:
                         ftfPk5.setEditable(true);
                         String outx2= Integer.toString(flg);
                    
                }
                             
            }
                    
        });
             
             
        buttonNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             
                buttonBack.setEnabled(true);
                int flg=0;
                if (ftfPk0.isEditable()==true)
                {flg=1;}
                 if (ftfPk1.isEditable()==true)
                {flg=2;}
                  if (ftfPk2.isEditable()==true)
                {flg=3;}
                   if (ftfPk3.isEditable()==true)
                {flg=4;}
                     if (ftfPk4.isEditable()==true)
                {flg=5;}
                       if (ftfPk5.isEditable()==true)
                {flg=6;}
                   
                String outx= Integer.toString(flg);
               
                
                switch(flg){
                    case 1:
                        
                        ftfPk0.setEditable(false);
                        ftfPk1.setEditable(true);
                        break;
                    case 2:
                      
                        ftfPk1.setEditable(false);
                        ftfPk2.setEditable(true);
                        break;
                    case 3:
                      
                        ftfPk2.setEditable(false);
                        ftfPk3.setEditable(true);
                         break;
                           case 4:
                       
                        ftfPk3.setEditable(false);
                        ftfPk4.setEditable(true);
                         break;
                           case 5:
                       
                        ftfPk4.setEditable(false);
                        ftfPk5.setEditable(true);
                        buttonNext.setEnabled(false);
                         break;
                    case 6:
                        
                        ftfPk5.setEditable(false);
                        break;
                     default:
                         String outx2= Integer.toString(flg);
                     
                }
                          }
    });
        
         buttonSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 try {
                controller.saveCalibration();
                   } catch (IOException ex) {
                    Logger.getLogger(Calibration.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
        });
         
         buttonReset.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

               int iDPk0 =0;
               ftfPk0.setValue(new Integer(iDPk0));
               ftfPk0.setEditable(true);

               int iDPk1 =0;
               ftfPk1.setValue(new Integer(iDPk1));
               ftfPk1.setEditable(false);

               int iDPk2 =0;
               ftfPk2.setValue(new Integer(iDPk2));
               ftfPk2.setEditable(false);



               int iDPk3 =0;
               ftfPk3.setValue(new Integer(iDPk3));
               ftfPk3.setEditable(false);

                int iDPk4 =0;
               ftfPk4.setValue(new Integer(iDPk4));
               ftfPk4.setEditable(false);

                int iDPk5 =0;
               ftfPk5.setValue(new Integer(iDPk5));
               ftfPk5.setEditable(false);

               double dDSP =0.0;
               ftfSP.setColumns(4);

               double dDWv1 =487.7;
               ftfWv1.setValue(new Double(dDWv1));

               double dDWv2 =546.5;
               ftfWv2.setValue(new Double(dDWv2));

               double dDWv3 =611.6;
               ftfWv3.setValue(new Double(dDWv3));

               double dDWv4 =0;
               ftfWv4.setValue(new Double(dDWv4));

               double dDWv5 =0;
               ftfWv5.setValue(new Double(dDWv5));
                   
               controller.resetCalibration();
               buttonSave.setEnabled(false);
                
                 
              }
        });
         
    }
      
      private int isValid(ArrayList<Double> vals, ArrayList<Integer> pxs){
          int cnt=0;
         
          for(int i=1;i<vals.size();i++){
              if (vals.get(0)>=vals.get(i)||pxs.get(0)>=pxs.get(i)){
                  cnt=1;
                  break;
              }
              
              for(int j=i+1;j<vals.size();j++){
                 
                  if(vals.get(i).equals(vals.get(j))||pxs.get(i).equals(pxs.get(j))){
                      cnt=2;
                      break;
                  }
                  
                  if(vals.get(i)<400||vals.get(i)>850){
                      cnt=3;
                      break;
                  }
                 
                                  
              }
              
          }
          
          return cnt;
          
      }

}
