/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rstorm;



import ij.IJ;
import ij.plugin.PlugIn;
import gui.ImportsSMLMPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 *
 * @author Janel
 */
public class ImportsSMLM implements PlugIn {
     JFrame mainFrame;
     JPanel mainPanel;
     Analysis savedData;
     
    @Override
    public void run(String arg){
     
             savedData= new Analysis();
             setupGUI();
      
    }
    
     private void setupGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            IJ.error("There was a problem setting the loook of the UI");
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
           
        }
        
        mainFrame = new JFrame("RainbowSTORM sSMLM Import");
        mainFrame.setIconImage(IJ.getInstance().getIconImage()); 
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainPanel = new JPanel();
      
        mainPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
    
        mainPanel.add(new ImportsSMLMPanel(savedData,mainFrame),gbc);
        
        mainFrame.getContentPane().add(mainPanel);

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    
    
}
