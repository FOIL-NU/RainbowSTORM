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
         
        //File f = new File(fname);
        //String d =f.
         
        ArrayList<String[]> data= new ArrayList<String[]>();
        
        if (dir != null) {
             
            fc = new JFileChooser(dir);
            if (!fname.isEmpty()){
                
                fc.setSelectedFile(new File(fname));
            }
        } else {
            fc = new JFileChooser(ij.io.OpenDialog.getLastDirectory());
        }
  
        
        int returnVal = fc.showOpenDialog(frame);
        
    
        
        
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            dirName =file.getParent();
           // String fpath= file.getAbsolutePath()
            filename = file.getAbsolutePath();
            try {
               
                 CSVReader reader = new CSVReader(new FileReader(file));
                 String[] nextLine;

                
                // Read the first row, which has the column names.
                //String[] columnNames = reader.readNext();
                // Create a model with 0 rows. Note: table will only show if columnNames are defined
                //DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
                
                int cnt= 0;
                // Add the remaining data, reading the CSV line by line.
                while ((nextLine = reader.readNext()) != null) {
                data.add(nextLine);
                    
                   
                //System.out.println(nextLine[1]);
               
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
            return data;          
     }
     
     public String getFileName(){
     return filename;
}
     
      public String getDirName(){
     return dirName;
}
    
}
