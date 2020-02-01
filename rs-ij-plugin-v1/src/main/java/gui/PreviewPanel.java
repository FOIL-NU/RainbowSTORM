/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import rstorm.Analysis;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.BrowserLauncher;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import unmixing.Blinking;

/**
 *
 * @author Janel
 */
public class PreviewPanel extends JPanel {
    
  private Analysis controller;
   private static final String url = "sSMLM_Preview.html";
    private static final String ver = "2020_01";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
  private JButton buttonHelp;  
  private JButton buttonNext ;
  private JButton buttonPrevious ;
  private JButton buttonUpdate ;
 
  private JPanel psfPanel;
  private JPanel specPanel;
  private JPanel statsPanel;
 
  private ChartPanel specPlotPanel;
  private ImageIcon psf_imc;
  private ImageIcon spec_imc;
  private ArrayList<Blinking> bEs;

  public PreviewPanel(Analysis controller,ImageProcessor psf_im, ImageProcessor spec_im, float[] sm_spec,int[] wvs,double psf_phs, double psf_unc, double spec_phs, double spec_unc,int cur, int tot, ArrayList<Blinking> bEvents){
        
        this.controller = controller;
        bEs = bEvents; 
        setupView(psf_im,spec_im,sm_spec,wvs,psf_phs,psf_unc,spec_phs,spec_unc,cur,tot);
        setupActionListerners();
        
    }

private void setupView(ImageProcessor psf_im, ImageProcessor spec_im, float[] sm_spec,int[] wvs,double psf_phs, double psf_unc, double spec_phs, double spec_unc,int cur, int tot){
        
         JPanel imPanel = new JPanel();
         imPanel.setLayout(new FlowLayout());
         ImagePlus psf_Img = new ImagePlus("PSF",psf_im);
      
         ImagePlus spec_Img = new ImagePlus("spectra",spec_im);
     
        XYDataset spec_dataset=controller.drawAvgSpecPlot(sm_spec,wvs,"Single Molecule Spectrum");
       JFreeChart  specChart= controller.createAvgSpChart(spec_dataset,"Single Molecule Spectrum");
       
       
       psfPanel= new JPanel();  
       psfPanel.setLayout(new GridBagLayout());
       GridBagConstraints pc = new GridBagConstraints();  
       pc.anchor = GridBagConstraints.CENTER;
       pc.gridx=0;
       pc.gridy=0;
       Image psf_output= psf_Img.getImage();
       int w=psf_Img.getWidth()*10;
       int h=psf_Img.getHeight()*10;
       psf_output = psf_output.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
       
       psf_imc= new ImageIcon(psf_output);
       psfPanel.add(new JLabel(psf_imc),pc);
       pc.gridy++;
       psfPanel.add(new JLabel("PSF Image"),pc);
       imPanel.add(psfPanel);
     
       specPanel= new JPanel();  
     
       specPanel.setLayout(new GridBagLayout());
       GridBagConstraints spc = new GridBagConstraints();  
       spc.anchor = GridBagConstraints.CENTER;
       spc.gridx=0;
       spc.gridy=0;
       Image spec_output= spec_Img.getImage();
       int w2=spec_Img.getWidth()*10;
       int h2=spec_Img.getHeight()*10;
       spec_output = spec_output.getScaledInstance(w2, h2, java.awt.Image.SCALE_SMOOTH);
       spec_imc= new ImageIcon(spec_output);
       specPanel.add(new JLabel(spec_imc),spc);
       spc.gridy++;
       specPanel.add(new JLabel("Spectrum Image"),spc);
      
       imPanel.add(specPanel);
                
       specPlotPanel = new ChartPanel(specChart);
      
       int wid =500;
       specPlotPanel.setPreferredSize(new Dimension(wid,300));
                
                              
                setBorder(BorderFactory.createTitledBorder("sSMLM Preview"));
        
                setLayout(new GridBagLayout());
                
                GridBagConstraints mc = new GridBagConstraints();  
             
                 mc.anchor=GridBagConstraints.CENTER;
                 
                mc.gridx=1;
                mc.gridy=0;
                add(imPanel,mc); 
              
                mc.gridy++;
               
                mc.gridx=1;
                
                 mc.anchor=GridBagConstraints.CENTER;
                add(specPlotPanel,mc);
                mc.anchor=GridBagConstraints.CENTER;
                
                mc.gridy++;
                
                
                statsPanel =new JPanel();
                statsPanel.setLayout(new GridBagLayout());
                setStats(cur+1, tot, psf_unc, psf_phs, spec_unc,spec_phs);
                
             
                mc.gridy++;
                add(statsPanel,mc);
                mc.gridy++;
                JPanel ctrlPanel = new JPanel();
                ctrlPanel.setLayout(new FlowLayout());
                 buttonUpdate = new JButton("Update event");
                buttonUpdate.setPreferredSize(new Dimension(120,20));
               
                buttonPrevious = new JButton("Previous event");
                buttonPrevious.setPreferredSize(new Dimension(120,20));
                buttonPrevious.setEnabled(false);
               
               buttonNext = new JButton("Next event");
               buttonNext.setPreferredSize(new Dimension(120,20));
                //add(buttonNext,mc);
               ctrlPanel.add(buttonUpdate);
               ctrlPanel.add(buttonPrevious);
               ctrlPanel.add(buttonNext);
               add(ctrlPanel,mc);  
               
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
            Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
               
}
private void setupActionListerners(){
        
         buttonNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                    
                    int sz= bEs.size();//controller.getBEs().size();
                     
                    if(controller.getCurrentEventID()<sz-1){
                  
                    int idx=controller.getCurrentEventID()+1;
                    controller.setEvent(idx);
                    updatePlots();
                    buttonPrevious.setEnabled(true);
            }else{
             buttonNext.setEnabled(false);
         }
                    
                   
             
            }
        });
         buttonPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 
                    if(controller.getCurrentEventID()>0){
                   
                    int idx=controller.getCurrentEventID()-1;
                    controller.setEvent(idx);
                    updatePlots();
                     buttonNext.setEnabled(true);
            }else{
             buttonPrevious.setEnabled(false);
         }
                    
                   
             
            }
        });
               buttonUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 updatePlots();
                    
                   
             
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
 public void updatePlots(){
     
    
     double[] ps =controller.getParams();
     int rng1=(int) ps[1];
     int rng2= (int) ps[2];
     int stp= (int)  ps[3];
    
     int tot= bEs.size();
     int cID = controller.getCurrentEventID();
        
     if(cID>=0&&cID<tot){
     Blinking nB =bEs.get(cID);
     ImageProcessor psfIm = controller.getPSFImg(nB,ps);
     ImageProcessor specIm = controller.getSpecImg(nB,ps);
     Blinking cBE =controller.getbEInfo(nB,ps);
      int[] wvs= cBE.setWavelengths(rng1, rng2, stp);
  
     float[] sm_spec =cBE.getSpectrum();
        double psf_phs =cBE.getPhPsf();
        double psf_unc =cBE.getUnc();
        double spec_phs =cBE.getPhotons();
        double spec_unc =cBE.getUncSPE();
        int cur=cID+1;
        
        
        XYDataset up_spdset=controller.drawAvgSpecPlot(sm_spec, wvs,"Single Molecule Spectrum");
        JFreeChart up_SpecChart= controller.createAvgSpChart(up_spdset,"Single Molecule Spectrum");
        specPlotPanel.setChart(up_SpecChart);
        
          ImagePlus psf_Img = new ImagePlus("PSF",psfIm);
          ImagePlus spec_Img = new ImagePlus("Spectrum",specIm);
         
       psfPanel.removeAll();
        GridBagConstraints pc = new GridBagConstraints();  
       pc.anchor = GridBagConstraints.CENTER;
       pc.gridx=0;
       pc.gridy=0;
       
       Image psf_output= psf_Img.getImage();
       int w=psf_Img.getWidth()*10;
       int h=psf_Img.getHeight()*10;
       psf_output = psf_output.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
       
       psf_imc= new ImageIcon(psf_output);
       psfPanel.add(new JLabel(psf_imc),pc);
       pc.gridy++;
       psfPanel.add(new JLabel("PSF Image"),pc);
       psfPanel.validate();
       psfPanel.repaint();
   
          
       specPanel.removeAll();
        GridBagConstraints spc = new GridBagConstraints();  
       spc.anchor = GridBagConstraints.CENTER;
       spc.gridx=0;
       spc.gridy=0;
       
       Image spec_output= spec_Img.getImage();
       int w2=spec_Img.getWidth()*10;
     
       int h2=spec_Img.getHeight()*10;
       spec_output = spec_output.getScaledInstance(w2, h2, java.awt.Image.SCALE_SMOOTH);
       
       spec_imc= new ImageIcon(spec_output);
       specPanel.add(new JLabel(spec_imc),spc);
       spc.gridy++;
       specPanel.add(new JLabel("Spectrum Image"),spc);
       
       specPanel.repaint();
       specPanel.validate();
       
        
                statsPanel.removeAll();
                setStats(cur, tot, psf_unc, psf_phs, spec_unc,spec_phs);
                statsPanel.validate();
                statsPanel.repaint();
                
                  repaint();
                
     }else{
         IJ.error("No Localizations found");
     }
     
     
 }
 
 private void setStats(int cur, int tot, double psf_unc, double psf_phs,double spec_unc,double spec_phs){
      GridBagConstraints sc = new GridBagConstraints();  
               int SMLM_tot=controller.getSMLMData().size();
               double pSMLM=((double)tot/(double)SMLM_tot)*100;
                sc.gridx=1;
                sc.gridy=0;
                statsPanel.add(new JLabel("Uncertainty(Spatial): "+String.format("%.1f", psf_unc)+" [nm]"),sc);
                sc.gridy++;
                statsPanel.add(new JLabel("Photons(Spatial): "+String.format("%.0f", psf_phs)),sc);
                sc.gridy++;
                statsPanel.add(new JLabel("Uncertainty(Spectral): "+String.format("%.1f", spec_unc)+" [nm]"),sc);
                sc.gridy++;
                statsPanel.add(new JLabel("Photons(Spectral): "+String.format("%.0f", spec_phs)),sc);
                sc.gridy++;
                statsPanel.add(new JLabel(cur+" of "+tot),sc);
                 sc.gridy++;
                 statsPanel.add(new JLabel("Percent SMLM Data: "+String.format("%.2f",pSMLM)+" %"),sc);
               
               
 }
 
}
