/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ij.IJ;
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

/**
 *
 * @author Janel L Davis
 */
public class BackgroundSubtractionPanel extends JPanel {
    
    private Analysis controller;
    private JButton buttonBkSub;
    private JCheckBox checkboxAutoBkSub;
    private JFormattedTextField ftfAvgBk;
    
    public BackgroundSubtractionPanel(Analysis controller){
        this.controller = controller;
        setupView();
        setupActionListerners();
        
    }
    
    private void setupView(){
        setBorder(BorderFactory.createTitledBorder("Global Background Subtraction"));
        GridBagLayout bc = new GridBagLayout();
        setLayout(bc);
         Dimension d = new Dimension (150,20);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=0;
        c.weightx=1;
        c.weighty=1;
        c.anchor=GridBagConstraints.CENTER;
        
        add(new JLabel("Automatic Threshold Selection:"),c);
        c.gridx++;
        checkboxAutoBkSub =new JCheckBox();
        checkboxAutoBkSub.setEnabled(true);
        checkboxAutoBkSub.setSelected(true);
        add(checkboxAutoBkSub,c);
       // c.gridx=0;
        c.gridx++;
        add(new JLabel("Background Threshold:"),c);
        c.gridx++;
        
        ftfAvgBk= new JFormattedTextField();
        ftfAvgBk.setColumns(5);
        ftfAvgBk.setEditable(false);
        int iD_avgbk=0;
        ftfAvgBk.setValue(new Integer(iD_avgbk));
        add(ftfAvgBk,c);
        
          
       c.gridx ++;
     
        buttonBkSub= new JButton("Subtract Background");
        buttonBkSub.setPreferredSize(d);
        buttonBkSub.setEnabled(false);
        add(buttonBkSub,c);
     
    }

    private void setupActionListerners() {
       checkboxAutoBkSub.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               
               if (checkboxAutoBkSub.isSelected()==false){
                   ftfAvgBk.setEditable(true);
               }else{
                   ftfAvgBk.setEditable(false);
               }
                   
                   
           }
        });
       
       
    controller.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(Analysis.IMAGES_READY)) {
                    buttonBkSub.setEnabled(true);
                }
            }
        });
    
    buttonBkSub.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            
            if(checkboxAutoBkSub.isSelected()==false)
            {
                if(ftfAvgBk.isEditable()){
            double avg_bk=((Number)ftfAvgBk.getValue()).doubleValue();
            if(isValid(avg_bk)){
            controller.applyGlobalBkSub(avg_bk);
            }
                }else{
                    IJ.error("Background Subtraction Error");
                }
            }else{
                double thres=-1;
                controller.applyGlobalBkSub(thres);
            }
                
            
            
        }
    });
    }
    
    public boolean isValid(double bkThres){
        boolean flg=false;
        
        if(bkThres<0){
            IJ.error("Background Threshold must be positive");
        }else{
            flg=true;
        }
        
        return flg;
        
    }
    
}
