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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import rstorm.Analysis;
import static java.lang.Math.round;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import unmixing.Blinking;

/**
 *
 * @author Janel L Davis
 */
public class VisSettingsPanel extends JPanel {
    
        private Analysis controller;
        private JComboBox visCombo;
        private JButton buttonUpdateImage;
        public static JFormattedTextField ftfMag;
        
    
        public VisSettingsPanel(Analysis controller,boolean showUpdate) {
        this.controller = controller;
        setupView(showUpdate);
        setupListeners();
    }
         private void setupView(boolean showUpdate){
             
                setBorder(BorderFactory.createTitledBorder("Visualization Settings"));
                
                GridBagLayout gbl = new GridBagLayout();
                  

                setLayout(gbl);

                GridBagConstraints c = new GridBagConstraints();

                c.gridx = 0;
                c.gridy = 0;
               
                c.anchor = GridBagConstraints.LINE_START;
                JPanel visPanel = new JPanel();
                visPanel.setLayout(new FlowLayout());
               visPanel.add(new JLabel("Select Reconstruction Method:"));
                String[] vis_options ={"Scatter Plot","Averaged Gaussian Plot"};
                 visCombo = new JComboBox(vis_options);
                
                visPanel.add(visCombo);
               
                visPanel.add(new JLabel("Magnification:"));
                int iDmag=5;
                ftfMag= new JFormattedTextField();
                ftfMag.setValue(new Integer(iDmag));
                ftfMag.setColumns(4);
                
                visPanel.add(ftfMag);
              
                 buttonUpdateImage= new JButton("Update Rendering");
                  if(showUpdate){
                 visPanel.add(buttonUpdateImage);
                  }
                  add(visPanel,c);
                  
                  int idx= visCombo.getSelectedIndex();
                  controller.setVisMethod(idx);
                
         
         }
          private void setupListeners() {
              
            visCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    
                    int idx= visCombo.getSelectedIndex();
                    controller.setVisMethod(idx);
                    
                   
             
            }
        });
            
              ftfMag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    
                   try{
                  ftfMag.commitEdit();
                  int mag=((Number)ftfMag.getValue()).intValue();
                      controller.setMag(mag);
                 }catch(ParseException e1){
                     IJ.error("Invalid Input");
                 }
         
            }
        });
            
                 buttonUpdateImage.addActionListener(new ActionListener(){
        
      @Override
            public void actionPerformed(ActionEvent e) {
                 try{
                  ftfMag.commitEdit();
                  int mag=((Number)ftfMag.getValue()).intValue();
                      controller.setMag(mag);
                 }catch(ParseException e1){
                     IJ.error("Invalid Input");
                 }
                     Thread processThread =new Thread(){
                    public void run(){ 
                    ArrayList<Blinking> BEs=controller.getCurrentData();
                    int[] fy= controller.getRange();
                    
                    
                       ArrayList<Double> centroids= new ArrayList<Double>();
      
        
                       ArrayList<Double> xps= new ArrayList<Double>();
                       ArrayList<Double> yps= new ArrayList<Double>();
                       ArrayList<Double> unc= new ArrayList<Double>();


                       int sz=BEs.size();


        
            for( int i=0;i<sz;i++){
                Blinking bE=BEs.get(i);
                double tmp_c=bE.getCentroid();


                double tmp_x=bE.getXPosition();
                double tmp_y=bE.getYPosition();
                double tmp_unc=bE.getUnc();


                float [] t_spec=bE.getSpectrum();


                centroids.add(tmp_c);

                xps.add(tmp_x);
                yps.add(tmp_y);
                unc.add(tmp_unc);



                }  
           
         double[] cens= ArrayUtils.toPrimitive(centroids.toArray(new Double[sz]));
       
         double [] xpos= ArrayUtils.toPrimitive(xps.toArray(new Double[sz]));
         double [] ypos= ArrayUtils.toPrimitive(yps.toArray(new Double[sz]));
         double [] uncs= ArrayUtils.toPrimitive(unc.toArray(new Double[sz]));
        
          Max mx_Val = new Max();
           int mxunc= (int) round(mx_Val.evaluate(uncs,0,sz));
          int mx1=(int) round(mx_Val.evaluate(cens,0,sz))+1;
          int xmax=(int) round(mx_Val.evaluate(xpos,0,sz))+mxunc;
          int ymax=(int) round(mx_Val.evaluate(ypos,0,sz))+mxunc;
          Min mn_Val = new Min();
          int mn1=(int) round(mn_Val.evaluate(cens,0,sz))-1;
          int xmin=(int) round(mn_Val.evaluate(xpos,0,sz))-mxunc;
          int ymin=(int) round(mn_Val.evaluate(ypos,0,sz))-mxunc;
          
          double pixelSize = controller.getPixelSize();
          double conv=pixelSize+0.5;
          
          int wid=(int) ((xmax-xmin)/conv);
          int hei=(int) ((ymax-ymin)/conv);
        
                    
                    controller.drawTCGaussianROI(BEs,mn1,mx1,wid,hei,xmin,ymin,mxunc);
                    }
              };
                processThread.start();
            
            }
        });
            
            
              
          }
          
        
    
}
