/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;


import java.awt.Dimension;
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
import javax.swing.JTextField;
import rstorm.Analysis;



/**
 *
 * @author Brian T. Soetikno and Janel L Davis
 */
public class LoadLocalizationsPanel extends JPanel {
    private Analysis controller;
    public static JTextField textfieldFilePath;
    private JButton buttonLoadCSV;
    public static JTextField textfieldCaliFilePath;
    private JButton buttonLoadCali;
 
    
    public LoadLocalizationsPanel(Analysis controller) {
        this.controller = controller;
        setupView();
        setupActionListeners();
    }
    
    private void setupView() {
         
        String def_file=" ";
        setBorder(BorderFactory.createTitledBorder("Load Localizations and Calibration Files"));
        GridBagLayout gbl = new GridBagLayout();
      
        
        setLayout(gbl);
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        
        add(new JLabel("Localization File Path: "), c);
        
        c.gridx ++; 
        c.gridy = 0;

        
        textfieldFilePath = new JTextField();
        Dimension d = new Dimension (150,20);
        textfieldFilePath.setColumns(20);
      
        add(textfieldFilePath, c);
        
        c.gridx++; c.gridy=0;
   
        buttonLoadCSV = new JButton("Load Localizations");
         buttonLoadCSV.setPreferredSize(d);
        
        add(buttonLoadCSV, c);
        
           
        c.gridx = 0;
        c.gridy ++;
        
        add(new JLabel("Calibration File Path: "), c);
        
        c.gridx ++; 
        textfieldCaliFilePath = new JTextField();
        textfieldCaliFilePath.setColumns(20);
   
        add(textfieldCaliFilePath, c);
        
        
             
        
        c.gridx ++;
       
        buttonLoadCali = new JButton("Load Calibration");
        buttonLoadCali.setPreferredSize(d);
        add(buttonLoadCali, c);
        
        c.gridx = 0; c.gridy ++;
       
    }
    
    private void setupActionListeners() {
        buttonLoadCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    
                    controller.loadCSV(textfieldFilePath.getText());
                    
                } catch (IOException ex) {
                    Logger.getLogger(LoadLocalizationsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
               
              try{  
                controller.displayCSVFile();
                  
                } catch (NullPointerException ex1) {
                   
                }  
              
            }
        });
    
         buttonLoadCali.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
             double[] d_params;
                try {
                    boolean isImportPanel=false;
                    d_params = controller.loadCali(textfieldCaliFilePath.getText(),isImportPanel);
                   
                    if(d_params!=null){
                    
                        controller.launchPxWvPlot(d_params);
           
                    }
              } catch (IOException ex) {
                    Logger.getLogger(LoadLocalizationsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
               
            }
        });
       
    }
}
