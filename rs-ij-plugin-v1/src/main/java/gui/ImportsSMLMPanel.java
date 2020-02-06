/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;


import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import ij.IJ;
import ij.plugin.BrowserLauncher;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import unmixing.Blinking;

/**
 *
 * @author Janel
 */
public class ImportsSMLMPanel extends JPanel {
    private Analysis controller;
     private static final String url = "sSMLM_Import.html";
    private static final String ver = "2020_01";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private JFrame imsFrame;
    private JButton buttonHelp;
    private JButton buttonLoadSpectra;
     private JButton buttonLoadSpectraData;
    public static JTextField textfieldLoadSpectraDataFilename;
    private boolean csvLoaded;
    private JButton buttonLoadVisualization;
    private SpatialImageParameterPanel sip;
    private ArrayList<Blinking> spData;
 
    public ImportsSMLMPanel(Analysis sSMLM, JFrame ims){
        this.controller = sSMLM;
        this.imsFrame =ims;
        setupView();
        setupActionListerners();
        
    }
       
       
    private void setupView(){
      Dimension d = new Dimension (150,20);   
     buttonLoadSpectra = new JButton("Load Spectra");
     buttonLoadSpectra.setEnabled(true);
    
     buttonLoadSpectraData = new JButton("Load Spectroscopic Data");
    
     buttonLoadSpectraData.setEnabled(true);
      csvLoaded=false;
     buttonLoadVisualization = new JButton("Visualize Data"); 
     buttonLoadVisualization.setPreferredSize(d);
     buttonLoadVisualization.setEnabled(true);  
     
     setBorder(BorderFactory.createTitledBorder("Load Saved sSMLM Data"));
        GridBagLayout gbl = new GridBagLayout();
      
        setLayout(gbl);
        
        GridBagConstraints bc = new GridBagConstraints();
        bc.weightx=1;
        bc.weighty=1;
        bc.gridx = 0;
        bc.gridy = 0;
        bc.anchor = GridBagConstraints.CENTER;
        
         JPanel spLoadDataPanel = new JPanel();
        spLoadDataPanel.setLayout(new FlowLayout());
        spLoadDataPanel.add(new JLabel("sSMLM Data File Path: "));
        
        textfieldLoadSpectraDataFilename = new JTextField(20);
        spLoadDataPanel.add(textfieldLoadSpectraDataFilename);
        spLoadDataPanel.add(buttonLoadSpectraData);
        add(spLoadDataPanel,bc);
        
       
        bc.gridy++;
      
        
        sip = new SpatialImageParameterPanel(controller,false);
        add(sip,bc);
       
        bc.gridy++;
      
        add(buttonLoadVisualization,bc);
        
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
        bc.anchor=GridBagConstraints.LAST_LINE_END;
        
       add(helpPanel, bc);
        } catch (Exception ex) {
            Logger.getLogger(ImportsSMLMPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
     
 }
                      
    private void setupActionListerners(){
        
        buttonLoadSpectraData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    spData=new ArrayList<Blinking>();
                    IJ.log(textfieldLoadSpectraDataFilename.getText());
                    spData =controller.loadBlinkingSpectraData(textfieldLoadSpectraDataFilename.getText());
                   if(spData!=null){
                    IJ.log("Size: "+spData.size());
                }
                    
                } catch (IOException ex) {
                    Logger.getLogger(ImportsSMLMPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
   
        
        buttonLoadVisualization.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                sip.setParams();
                controller.setDefaults();
                controller.displayLoadedData(spData);
                imsFrame.setVisible(false);
              
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
        
    
    }
    
}
