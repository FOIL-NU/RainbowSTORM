/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;
import ij.IJ;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import rstorm.Analysis;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
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
public class VisbyClassPanel extends JPanel {
      private Analysis controller;
   private static final String url = "sSMLM_Classification.html";
    private static final String ver = "2020_01";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private JButton buttonHelp;
    
     private JFormattedTextField ftfCh1;
     private JFormattedTextField ftfCh2;
     private JFormattedTextField ftfCh3;
     private JFormattedTextField ftfCh4;
     private JFormattedTextField ftfCh5;
     private JFormattedTextField ftfCh6;
     
     
      private JFormattedTextField ftfCh1_2;
     private JFormattedTextField ftfCh2_2;
     private JFormattedTextField ftfCh3_2;
     private JFormattedTextField ftfCh4_2;
     private JFormattedTextField ftfCh5_2;
     private JFormattedTextField ftfCh6_2;
     
     private JButton buttonClassify;
     
      private JCheckBox checkboxCh1;
      private JCheckBox checkboxCh2;
      private JCheckBox checkboxCh3;
      private JCheckBox checkboxCh4;
      private JCheckBox checkboxCh5;
      private JCheckBox checkboxCh6;
      
 
      
      private JCheckBox checkboxShowPlots;
      private JFrame contentFrame;
      
     
      
       public VisbyClassPanel(Analysis controller, JFrame cFrame){
        this.controller = controller;
        setupView();
        setupActionListerners();
        contentFrame=cFrame;
        
    }
       
       
    private void setupView(){
                      
                setBorder(BorderFactory.createTitledBorder("Classification by Centroid Window: ")); 
                GridBagLayout mgb = new GridBagLayout();
                setLayout(mgb);
              
               GridBagConstraints mc = new GridBagConstraints();  
               
                mc.weightx=1;
                mc.weighty=1;
                mc.gridx = 0;
                mc.gridy = 0;
                
              
                mc.anchor = GridBagConstraints.LINE_START;
             
                add(new JLabel("Set Channel        [Start:End]          Show Channel"),mc);
                mc.gridy++;
                JPanel ch1Panel= new JPanel();
                ch1Panel.setLayout(new FlowLayout());
                
                JPanel ch2Panel= new JPanel();
                ch2Panel.setLayout(new FlowLayout());
                
                JPanel ch3Panel= new JPanel();
                ch3Panel.setLayout(new FlowLayout());
                
                JPanel ch4Panel= new JPanel();
                ch4Panel.setLayout(new FlowLayout());
                
                JPanel ch5Panel= new JPanel();
                ch5Panel.setLayout(new FlowLayout());
                
                JPanel ch6Panel= new JPanel();
                ch6Panel.setLayout(new FlowLayout());
                        
                
                JLabel labelCh1 = new JLabel("Red:        ");
                ch1Panel.add(labelCh1);
                ftfCh1= new JFormattedTextField();
                ftfCh1.setColumns(4);
                 ch1Panel.add(ftfCh1);                      
                ch1Panel.add(new JLabel(" : "));
                ftfCh1_2= new JFormattedTextField();  
                ftfCh1_2.setColumns(4);
                ch1Panel.add(ftfCh1_2);
                ch1Panel.add(new JLabel("          "));
                checkboxCh1 = new JCheckBox();    
                checkboxCh1.setEnabled(true);
                checkboxCh1.setSelected(false);
                ch1Panel.add(checkboxCh1);
                               
                
                 mc.gridy++;
                 //chSelectionPanel.add(ch1Panel,mc);
                  add(ch1Panel,mc);
                 
                   
                JLabel labelCh2 = new JLabel("Green:     ");
                ch2Panel.add(labelCh2);
                ftfCh2= new JFormattedTextField();
                ftfCh2.setColumns(4);
                 ch2Panel.add(ftfCh2);                      
                ch2Panel.add(new JLabel(" : "));
                ftfCh2_2= new JFormattedTextField(); 
                ftfCh2_2.setColumns(4);
                ch2Panel.add(ftfCh2_2);
                ch2Panel.add(new JLabel("          "));
                checkboxCh2 = new JCheckBox();    
                checkboxCh2.setEnabled(true);
                checkboxCh2.setSelected(false);
                ch2Panel.add(checkboxCh2);
             
                 mc.gridy++;
                 //chSelectionPanel.add(ch2Panel,mc);
                  add(ch2Panel,mc);
                    
                JLabel labelCh3 = new JLabel("Blue:        ");
                ch3Panel.add(labelCh3);
                ftfCh3= new JFormattedTextField();
                ftfCh3.setColumns(4);
                 ch3Panel.add(ftfCh3);                      
                ch3Panel.add(new JLabel(" : "));
                ftfCh3_2= new JFormattedTextField();  
                ftfCh3_2.setColumns(4);
                ch3Panel.add(ftfCh3_2);
                ch3Panel.add(new JLabel("          "));
                checkboxCh3 = new JCheckBox();    
                checkboxCh3.setEnabled(true);
                checkboxCh3.setSelected(false);
                ch3Panel.add(checkboxCh3);
             
                 mc.gridy++;
                
                  add(ch3Panel,mc);
                 
                    
                JLabel labelCh4 = new JLabel("Yellow:     ");
                ch4Panel.add(labelCh4);
                ftfCh4= new JFormattedTextField();
                ftfCh4.setColumns(4);
                ch4Panel.add(ftfCh4);                      
                ch4Panel.add(new JLabel(" : "));
                ftfCh4_2= new JFormattedTextField();
                ftfCh4_2.setColumns(4);
                ch4Panel.add(ftfCh4_2);
                ch4Panel.add(new JLabel("          "));
                checkboxCh4 = new JCheckBox();    
                checkboxCh4.setEnabled(true);
                checkboxCh4.setSelected(false);
                ch4Panel.add(checkboxCh4);
                
             
                 mc.gridy++;
                 
                  add(ch4Panel,mc);
                 
                    
                JLabel labelCh5 = new JLabel("Cyan:       ");
                ch5Panel.add(labelCh5);
                ftfCh5= new JFormattedTextField();
                ftfCh5.setColumns(4);
                ch5Panel.add(ftfCh5);                      
                ch5Panel.add(new JLabel(" : "));
                ftfCh5_2= new JFormattedTextField();   
                ftfCh5_2.setColumns(4);
                ch5Panel.add(ftfCh5_2);
                ch5Panel.add(new JLabel("          "));
                checkboxCh5 = new JCheckBox();    
                checkboxCh5.setEnabled(true);
                checkboxCh5.setSelected(false);
                ch5Panel.add(checkboxCh5);
             
                 mc.gridy++;
                 //chSelectionPanel.add(ch5Panel,mc);
                  add(ch5Panel,mc);
                 
                    
                JLabel labelCh6 = new JLabel("Magenta: ");
                ch6Panel.add(labelCh6);
                ftfCh6= new JFormattedTextField();
                ftfCh6.setColumns(4);
                ch6Panel.add(ftfCh6);                      
                ch6Panel.add(new JLabel(" : "));
                ftfCh6_2= new JFormattedTextField(); 
                ftfCh6_2.setColumns(4);
                ch6Panel.add(ftfCh6_2);
                ch6Panel.add(new JLabel("          "));
                checkboxCh6 = new JCheckBox();    
                checkboxCh6.setEnabled(true);
                checkboxCh6.setSelected(false);
                ch6Panel.add(checkboxCh6);
             
                 mc.gridy++;
                
                 add(ch6Panel,mc);
                mc.gridy++;
                 mc.anchor = GridBagConstraints.CENTER;
                
                JPanel plotPanel = new JPanel();
                plotPanel.setLayout(new FlowLayout());
                plotPanel.add(new JLabel("Show Channel Summary"));
             
                checkboxShowPlots = new JCheckBox();
                checkboxShowPlots.setEnabled(true);
               checkboxShowPlots.setSelected(true);
                plotPanel.add(checkboxShowPlots);
                
                add(plotPanel,mc);
                mc.gridy++;
               
                buttonClassify = new JButton("Classify by Centroid");
              
                add(buttonClassify,mc);
                
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
        mc.anchor=GridBagConstraints.LAST_LINE_END;
        
       add(helpPanel, mc);
        } catch (Exception ex) {
            Logger.getLogger(VisbyClassPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
               
                
        
        
    }
    
    private void setupActionListerners(){
        
         buttonClassify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 
                    ArrayList<int[]> channels = new ArrayList<int[]>();
                    boolean validflg=true;
                   
                    int tot_active=0;
                    int ch1_active=0;
                    int ch1_showIm=0;
                    boolean ch1_valid=false;
                    int[] ch1 = new int[4];
                    
                    if (!ftfCh1.getText().isEmpty()&&!ftfCh1_2.getText().isEmpty()){
                    ch1_active=1;
                    tot_active++;
                    
                    if(checkboxCh1.isSelected()){
                        ch1_showIm=1;
                    }
                   
                    String ch1_cw1_Val= ftfCh1.getText();
                    int ch1_cw1_Thres=Integer.parseInt(ch1_cw1_Val.replace(",",""));
                    IJ.log("Centroid Window Thres: "+ch1_cw1_Thres);
                    
                    String ch1_cw2_Val= ftfCh1_2.getText();
                    int ch1_cw2_Thres=Integer.parseInt(ch1_cw2_Val.replace(",",""));
                    IJ.log("Centroid Window Thres: "+ch1_cw2_Thres);
                   
                    
                    ch1_valid =validRange(ch1_cw1_Thres,ch1_cw2_Thres);
                    ch1[0]=ch1_cw1_Thres;
                    ch1[1]=ch1_cw2_Thres;
                    ch1[2]=ch1_active;
                    ch1[3]=ch1_showIm;
                    }
                    
                     channels.add(ch1);
                    
                    
                    int ch2_active=0;
                    int ch2_showIm=0;
                    boolean ch2_valid=false;
                    int[] ch2 = new int[4];
                    if (!ftfCh2.getText().isEmpty()&&!ftfCh2_2.getText().isEmpty()){
                    ch2_active=1;
                    tot_active++;
                    if(checkboxCh2.isSelected()){
                        ch2_showIm=1;
                    }
                    
                    String ch2_cw1_Val= ftfCh2.getText();
                    int ch2_cw1_Thres=Integer.parseInt(ch2_cw1_Val.replace(",",""));
                    IJ.log("Centroid Window Thres: "+ch2_cw1_Thres);
                    
                    String ch2_cw2_Val= ftfCh2_2.getText();
                    int ch2_cw2_Thres=Integer.parseInt(ch2_cw2_Val.replace(",",""));
                    
                   ch2_valid =validRange(ch2_cw1_Thres,ch2_cw2_Thres);
                   
                    ch2[0]=ch2_cw1_Thres;
                    ch2[1]=ch2_cw2_Thres;
                    ch2[2]=ch2_active;
                    ch2[3]=ch2_showIm;
                    }
                    channels.add(ch2);
                    
                    int ch3_active=0;
                    int ch3_showIm=0;
                    boolean ch3_valid=false;
                     int[] ch3 = new int[4];
                    if (!ftfCh3.getText().isEmpty()&&!ftfCh3_2.getText().isEmpty()){
                    ch3_active=1;
                    tot_active++;
                    
                        if(checkboxCh3.isSelected()){
                        ch3_showIm=1;
                    }
                        
                    String ch3_cw1_Val= ftfCh3.getText();
                    int ch3_cw1_Thres=Integer.parseInt(ch3_cw1_Val.replace(",",""));
                    IJ.log("Centroid Window Thres: "+ch3_cw1_Thres);
                    
                    String ch3_cw2_Val= ftfCh3_2.getText();
                    int ch3_cw2_Thres=Integer.parseInt(ch3_cw2_Val.replace(",",""));
                    
                    ch3_valid =validRange(ch3_cw1_Thres,ch3_cw2_Thres);
                   
                    ch3[0]=ch3_cw1_Thres;
                    ch3[1]=ch3_cw2_Thres;
                    ch3[2]=ch3_active;
                    ch3[3]=ch3_showIm;
                    }
                    
                    
                  
                    channels.add(ch3);
                    
                    
                    int ch4_active=0;
                    int ch4_showIm=0;
                    boolean ch4_valid=false;
                     int[] ch4 = new int[4];
                    if (!ftfCh4.getText().isEmpty()&&!ftfCh4_2.getText().isEmpty()){
                    ch4_active=1;
                    tot_active++;
                    
                    if(checkboxCh4.isSelected()){
                        ch4_showIm=1;
                    }
                    String ch4_cw1_Val= ftfCh4.getText();
                    int ch4_cw1_Thres=Integer.parseInt(ch4_cw1_Val.replace(",",""));
                    IJ.log("Centroid Window Thres: "+ch4_cw1_Thres);
                    
                    String ch4_cw2_Val= ftfCh4_2.getText();
                    int ch4_cw2_Thres=Integer.parseInt(ch4_cw2_Val.replace(",",""));
                    
                    ch4_valid =validRange(ch4_cw1_Thres,ch4_cw2_Thres);
                   
                    ch4[0]=ch4_cw1_Thres;
                    ch4[1]=ch4_cw2_Thres;
                    ch4[2]=ch4_active;
                    ch4[3]=ch4_showIm;
                    }
                    
                    
                  
                    channels.add(ch4);
                    
                    
                     int ch5_active=0;
                     int ch5_showIm=0;
                     boolean ch5_valid=false;
                     int[] ch5 = new int[4];
                    if (!ftfCh5.getText().isEmpty()&&!ftfCh5_2.getText().isEmpty()){
                    ch5_active=1;
                    tot_active++;
                    
                       if(checkboxCh5.isSelected()){
                        ch5_showIm=1;
                    }
                    String ch5_cw1_Val= ftfCh5.getText();
                    int ch5_cw1_Thres=Integer.parseInt(ch5_cw1_Val.replace(",",""));
                    IJ.log("Centroid Window Thres: "+ch5_cw1_Thres);
                    
                    String ch5_cw2_Val= ftfCh5_2.getText();
                    int ch5_cw2_Thres=Integer.parseInt(ch5_cw2_Val.replace(",",""));
                    
                    ch5_valid =validRange(ch5_cw1_Thres,ch5_cw2_Thres);
                   
                    ch5[0]=ch5_cw1_Thres;
                    ch5[1]=ch5_cw2_Thres;
                    ch5[2]=ch5_active;
                    ch5[3]=ch5_showIm;
                    }
                    
                    
                  
                    channels.add(ch5);
                    
                    int ch6_active=0;
                    int ch6_showIm=0;
                     boolean ch6_valid=false;
                    int[] ch6 = new int[4];
                    if (!ftfCh6.getText().isEmpty()&&!ftfCh6_2.getText().isEmpty()){
                    ch6_active=1;
                    tot_active++;
                    
                    if(checkboxCh6.isSelected()){
                        ch6_showIm=1;
                    }
                    
                    
                    String ch6_cw1_Val= ftfCh6.getText();
                    int ch6_cw1_Thres=Integer.parseInt(ch6_cw1_Val.replace(",",""));
                    IJ.log("Centroid Window Thres: "+ch6_cw1_Thres);
                    
                    String ch6_cw2_Val= ftfCh6_2.getText();
                    int ch6_cw2_Thres=Integer.parseInt(ch6_cw2_Val.replace(",",""));
                    
                    
                    ch6_valid =validRange(ch6_cw1_Thres,ch6_cw2_Thres);
                    ch6[0]=ch6_cw1_Thres;
                    ch6[1]=ch6_cw2_Thres;
                    ch6[2]=ch6_active;//Math.abs(ch2_color);
                    ch6[3]=ch6_showIm;
                    }
                    
                    
                  
                    channels.add(ch6);
                    
                   boolean showPlots = checkboxShowPlots.isSelected();
                   
                    IJ.log("Channels: "+channels.size());
                    String outString = "Invalid Channels: "; 
                    
                    for(int v=0;v<channels.size();v++){
                    
                    
                        switch(v){
                        case 0:
                        if(!ch1_valid&&ch1_active==1){
                            if(validflg){
                                validflg=false;
                                 outString=outString+"Red";
                            }else{
                            outString=outString+" ,Red";
                                    }
                        }
                            
                        break;
                        case 1:
                             if(!ch2_valid&&ch2_active==1){
                            if(validflg){
                                validflg=false;
                                 outString=outString+"Green";
                            }else{
                            outString=outString+" ,Green";
                                    }
                        }
                        break;
                         case 2:
                                    if(!ch3_valid&&ch3_active==1){
                            if(validflg){
                                validflg=false;
                                 outString=outString+"Blue";
                            }else{
                            outString=outString+" ,Blue";
                                    }
                        }
                        break;
                         case 3:
                                    if(!ch4_valid&&ch4_active==1){
                            if(validflg){
                                validflg=false;
                                 outString=outString+"Yellow";
                            }else{
                            outString=outString+" ,Yellow";
                                    }
                        }
                        break; 
                         case 4:
                                    if(!ch5_valid&&ch5_active==1){
                            if(validflg){
                                validflg=false;
                                 outString=outString+"Cyan";
                            }else{
                            outString=outString+" ,Cyan";
                                    }
                        }
                        break; 
                        case 5:
                                   if(!ch6_valid&&ch6_active==1){
                            if(validflg){
                                validflg=false;
                                 outString=outString+"Magenta";
                            }else{
                            outString=outString+" ,Magenta";
                                    }
                        }
                        break;
                        default:
                         break;
                         
                        }
                    }
                    
                    if(validflg){
                    contentFrame.setVisible(false);
                    controller.createCompImg(channels,tot_active,showPlots);
                    }else{
                        IJ.error(outString);
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
   
        
    }
    
    
     private boolean validRange(int rng1, int rng2){

        boolean flg= false;
        boolean flg1=rng1<380||rng2>900||rng1<0||rng2<0||rng1>=rng2;
        if(flg1){
          //  IJ.error("Invalid spectrum Range selected. Please select values from 380 nm : 900 nm)");

            }
   


    if(!flg1){
        flg=true;

    }
    return flg;
    }
     
}
