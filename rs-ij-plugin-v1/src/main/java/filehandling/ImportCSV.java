/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filehandling;

import au.com.bytecode.opencsv.CSVReader;
import ij.IJ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 *
 * @author Janel
 */
public class ImportCSV {
    
     public ImportCSV(){
        
    }
    
    private JFileChooser fc;
    private String filename;
    private String dirName;
    
     public ArrayList<String[]> importCSV(String fname,String dir, JFrame frame) throws IOException,FileNotFoundException,NullPointerException, IndexOutOfBoundsException{
         
    
        ArrayList<String[]> data= new ArrayList<String[]>();
        
        if (dir != null) {
             
            fc = new JFileChooser(dir);
            if (!fname.isEmpty()){
                
                fc.setSelectedFile(new File(fname));
            }
        } else {
            fc = new JFileChooser(ij.io.OpenDialog.getLastDirectory());
            if (!fname.isEmpty()){
                
                fc.setSelectedFile(new File(fname));
            }
        }
  
        int returnVal = fc.showOpenDialog(frame);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            dirName =file.getParent();
            filename = file.getAbsolutePath();
            try {
               
                 CSVReader reader = new CSVReader(new FileReader(file));
                 String[] nextLine;
            
                int cnt= 0;
                // Add the remaining data, reading the CSV line by line.
                while ((nextLine = reader.readNext()) != null) {
                data.add(nextLine);
                  
                cnt++;
                }
                reader.close();
               
                
                  } catch (FileNotFoundException e1) {
               IJ.error("File Not Found");
            } catch (IOException e2) {
                IJ.error("There was an IO Exception");
            }catch(NullPointerException e3){
                
            }catch(IndexOutOfBoundsException e4){
                
            }
            
        }
        
       /* if(returnVal == JFileChooser.CANCEL_OPTION||returnVal ==JFileChooser.ABORT){
          fc.cancelSelection();
            
        }*/
        
            return data;          
     }
     
     public String getFileName(){
     return filename;
}
     
      public String getDirName(){
     return dirName;
}
    
}
