/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ij.IJ;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import rstorm.Analysis;
import java.awt.Dimension;

/**
 *
 * @author Janel
 */
public class VisThresPanel extends JPanel {
    
    private Analysis controller;
    private JLabel avgPhotons;    
    private JLabel spPrecision;
    private JLabel numLocs;    
    private JLabel pSPELocs;
    private JLabel pSMLMLocs;
    private JLabel postProcess;
    private boolean dataImported;
    
    
 public VisThresPanel(Analysis controller,boolean importedData){
        this.controller = controller;
        dataImported=importedData;
        setupView();
        setupActionListerners();
        
    }
       
       
    private void setupView(){
             
                 Dimension preferredSize = new Dimension(750,150);
                setPreferredSize(preferredSize);
                setBorder(BorderFactory.createTitledBorder("sSMLM Summary")); 
                GridBagLayout mgb = new GridBagLayout();
                setLayout(mgb);
                GridBagConstraints mc = new GridBagConstraints();  
             
                mc.gridx = 0;
                mc.gridy = 0;
               
                mc.anchor=GridBagConstraints.CENTER;
                            
               
               
                JPanel statsPanel =new JPanel();
                
                GridBagLayout sgb = new GridBagLayout();
                
                statsPanel.setLayout(sgb);
                GridBagConstraints sc = new GridBagConstraints();  
              
                sc.gridx = 0;
                sc.gridy = 0;
                postProcess  = new JLabel("Post-Processing: "); 
              
                add(postProcess,mc);
                mc.gridy++;
                avgPhotons= new JLabel("Average Photons:");
              
                 
                 add(avgPhotons,mc);
                 mc.gridy++;
                 
                spPrecision= new JLabel("Average Spectral Precision:");
                
                 
                 add(spPrecision,mc);
                 mc.gridy++;
                 
                 numLocs= new JLabel("Number of Localizations:");
             
                 add(numLocs,mc);
                 mc.gridy++;
                 
                
                pSPELocs= new JLabel("Percent Spectroscopic Localizations:");
              
                
                add(pSPELocs,mc);
                 mc.gridy++;
                 
                
                 
                String st= " ";
               if(!dataImported){
                st="Percent SMLM Localizations:";
               }
                pSMLMLocs= new JLabel(st);
        
                  add(pSMLMLocs,mc);
              
                
                
        
        
    }
    
    private void setupActionListerners(){
        controller.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(Analysis.UPDATE_STATS)) {
                    
                   updateStats();
         
                }
            }
        });
    }
    
    
    public void updateStats(){
         double[] stats=controller.getStats();
                       double avg_ph=stats[0];
                       double avg_spePre=stats[1];
                       double numPts=stats[2];
                       double utilSpec=stats[3];
                       double utilPts=stats[4];
                       IJ.log("UtilPts"+utilPts);
                       
                       String st = "Average Spectral Photons: ";
                       String str = String.format("%.2f", avg_ph);
                       st= st+(str);
                       avgPhotons.setText(st);
                       
                       
                       String st1 = "Average Spectral Precision:";
                       String str1 = String.format("%.2f", avg_spePre);
                       st1= st1+(str1)+" [nm]";
                       spPrecision.setText(st1);
                       
                       String st2 = "Number of Localizations:";
                       String str2 = String.format("%.0f", numPts);
                       st2= st2.concat(str2);
                       numLocs.setText(st2);
                       
                       String st3 = "Percent Spectroscopic Localizations:";
                       String str3 = String.format("%.2f", utilSpec);
                       st3= st3+(str3)+" %";
                       pSPELocs.setText(st3);
                       
                       
                       String st4 = "Percent SMLM Localizations:";
                       //if(!dataImported){
                       String str4 = String.format("%.2f", utilPts);
                       if(!dataImported){
                       st4= st4+(str4)+" %";
                       }else{
                           st4=" ";
                       }
                       pSMLMLocs.setText(st4);
                       
                       
                       String st5="Post-processing: "+controller.getpostProcessString();
                       postProcess.setText(st5);
    }
    
}

