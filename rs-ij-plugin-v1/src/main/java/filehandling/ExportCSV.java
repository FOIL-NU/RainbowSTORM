/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filehandling;
import au.com.bytecode.opencsv.CSVWriter;
import ij.IJ;
import java.io.*;

import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;


/**
 *
 * @author Janel L Davis
 */
public class ExportCSV {
    
    public ExportCSV(){
        
    }
    
    private JFileChooser fc2;
    private String lastFilename;
    
    
    public void exportCSV(String dir,String[] header,ArrayList<String[]> data ) throws IOException{
        JFrame parentFrame = new JFrame();
         String saveDirectory = null;
        
         if (dir != null) {
            fc2 = new JFileChooser(dir);
        } else {
            fc2 = new JFileChooser(ij.io.OpenDialog.getLastDirectory());
        }
       
        fc2.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int userSelection = fc2.showSaveDialog(parentFrame);
        if (userSelection== JFileChooser.APPROVE_OPTION) {
           
                saveDirectory = fc2.getSelectedFile().toString();
                lastFilename=saveDirectory;
                String saveFilename = saveDirectory +".csv";
              
                File file = new File(saveFilename);
        
        
         try { 
        // create FileWriter object with file as parameter 
        FileWriter outputfile = new FileWriter(file); 
  
        // create CSVWriter object filewriter object as parameter 
        CSVWriter writer = new CSVWriter(outputfile); 
  
        // adding header to csv 
        writer.writeNext(header); 
       
        // add data to csv 
            for(int i =0; i<data.size();i++){
               
                String[] out = data.get(i);
                
                writer.writeNext(out);
         
       }
        
       
        // closing writer connection 
        writer.close(); 
    } 
    catch (IOException e) { 
        // TODO Auto-generated catch block 
       System.out.println("Error writing file.");
    } 
        
            } else {
            IJ.error("A folder was not selected.");
        }

        
    }
    
    
    public void exportSpectraCSV(String dir,String[] header,ArrayList<String[]> data ) throws IOException{
             
       
                String saveFilename = dir+"_spec.csv";
               
                File file = new File(saveFilename);
        
        
         try { 
        // create FileWriter object with file as parameter 
        FileWriter outputfile = new FileWriter(file); 
  
        // create CSVWriter object filewriter object as parameter 
        CSVWriter writer = new CSVWriter(outputfile); 
  
        // adding header to csv 
        writer.writeNext(header); 
       
        // add data to csv 
            for(int i =0; i<data.size();i++){
               
                String[] out = data.get(i);
                
                writer.writeNext(out);
         
       }
        
       
        // closing writer connection 
        writer.close(); 
    } 
    catch (IOException e) { 
        // TODO Auto-generated catch block 
       System.out.println("Error writing file.");
    } 
        
           
        
    }
    
    public String getLastFilename(){
        return lastFilename;
    }
            
    
}
