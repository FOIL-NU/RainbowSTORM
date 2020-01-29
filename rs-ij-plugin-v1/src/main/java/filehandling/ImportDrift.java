/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filehandling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import rendering.sSMLMDriftResults;

/**
 *
 * Code Adapted from ThunderSTORM
 */
public class ImportDrift {
    
    static final String JSON_ROOT = "root";
    static final String ROOT = "results";
    private  boolean im_success;
        private JFileChooser fc;
         private String filename;
  
        public sSMLMDriftResults loadResultsFromFile(String fname,String dir,JFrame frame) throws IOException,NullPointerException, IndexOutOfBoundsException {
         //im_success=false;
        BufferedReader reader = null;
          if (dir != null) {
             
            fc = new JFileChooser(dir);
            if (!fname.isEmpty()){
                
                fc.setSelectedFile(new File(fname));
            }
        } else {
            fc = new JFileChooser(ij.io.OpenDialog.getLastDirectory());
        }
  
        
        int returnVal = fc.showOpenDialog(frame);
      
        try {
            File file = fc.getSelectedFile();
             filename = file.getAbsolutePath();
             reader = new BufferedReader(new FileReader(file));

            Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(
                    UnivariateFunction.class,
                    new InstanceCreator<PolynomialSplineFunction>() {
                        @Override
                        public PolynomialSplineFunction createInstance(Type type) {
                            return new PolynomialSplineFunction(new double[]{1, 2}, new PolynomialFunction[]{new PolynomialFunction(new double[1])});
                        }
                    }).create();
            
           
            im_success =true;
      
            return gson.fromJson(reader, sSMLMDriftResults.class);
        }
         finally {
            if(reader != null) {
                reader.close();
            }
        }
        }
     
      
    public boolean getDriftflag(){
        return im_success;
    }
            
         
     public String getFileName(){
     return filename;
}
      
    
    public String getName() {
        return "JSON";
    }

    public String getSuffix() {
        return "json";
    }
    
}
