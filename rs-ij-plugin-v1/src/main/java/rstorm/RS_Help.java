/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rstorm;

import ij.IJ;
import ij.plugin.PlugIn;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;

import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;

import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 *
 * @author Janel Davis
 */
public class RS_Help implements PlugIn {
    private static final String url1 = "AboutRainbowSTORM.html";
    private static final String ver = "2020_01";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;

    
     @Override
    public void run(String arg){
        launchHelp(url1);
    }
    
    public void launchHelp(String url){
   
        
      try {
           
            JDialog dialog = new JDialog(IJ.getInstance(), "RainbowSTORM sSMLM Help Screens(" + ver + ")");
            if(IJ.isJava17()) {
                dialog.setType(Window.Type.UTILITY);
            }
           
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
        } catch(Exception e) {
            IJ.handleException(e);
        }
    
            
     
    
    }
   
}
