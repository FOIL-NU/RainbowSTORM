package images;

import java.io.IOException;

import javax.swing.JFrame;

import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Services.service;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;



import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;
import loci.plugins.LociImporter;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import ij.measure.ResultsTable;
import loci.plugins.BF;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;


/**
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, 
 *         rhaase@mpi-cbg.de
 * Date: May 2017
 * 
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics, 
 *                Dresden, Germany
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in the 
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its 
 *      contributors may be used to endorse or promote products derived from 
 *      this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */ 
public class ImagesMain {
    

    

    public static <T extends RealType<T>> void main(String... args) throws Exception {

        GenericDialog spectral_dialog = new GenericDialog("Load Spectral Calibration");
        spectral_dialog.addMessage("Run spectral calibration or load calibration result");;
        spectral_dialog.enableYesNoCancel("Run Calibration","Load Result");
        spectral_dialog.showDialog();

        GenericDialog axial_dialog = new GenericDialog("Load axial Calibration");
        axial_dialog.addMessage("Run axial calibration or load calibration result");;
        axial_dialog.enableYesNoCancel("Run Calibration","Load Result");
        axial_dialog.showDialog();
        
        OpenDialog od = new OpenDialog("Open ND2 File", null);
        String directory = od.getDirectory();
        String fileName = od.getFileName();

        // Check if the user selected a file or canceled the dialog
        if (fileName == null) {
            IJ.log("File selection canceled.");
            return;
        }
        String[] splitted = fileName.split("_");
        String zeroth_order_filePath = directory + splitted[0] +'_'+ splitted[1]+"_zeroth.tif";
        String first_order_filePath = directory + splitted[0] +'_'+ splitted[1]+"_first.tif";
        String whole_img_filepath = directory+"1141_com002.nd2";
        

        
        //AxialCalibration.calibrate(filePath);
        
        // Combine the directory and filename to get the full path

        // Use Bio-Formats to open the ND2 file
        //Opener opener = new Opener();
        //ImagePlus image = openND2Image(filePath);

        // Read the ND2 file using Bio-Formats
        //ImagePlus imagePlus = readND2(filePath);
        
        String[] nd2files = new String[5];
        nd2files[0] = "532.nd2";
        nd2files[1] = "605.nd2";
        nd2files[2] = "635.nd2";
        nd2files[3] = "685.nd2";
        nd2files[4] = "750.nd2";
        float[][] Result = new float[5][2];
        double[]xData = {532.0,605.0,635.0,685.0,750.0};

        ImagePlus imagePlus = service.readND2(directory+nd2files[0]);
        // Get the number of frames (images) in the ND2 file
        int numFrames = imagePlus.getStackSize();

        System.out.println("NumFrames: "+numFrames);
        // Average all the frames
        ImageProcessor averagedIp = imagePlus.getStack().getProcessor(1).duplicate();
        ImagePlus avImagePlus = new ImagePlus("Select Calibration ROI", averagedIp);
        avImagePlus.show();
        WaitForUserDialog dialog = new WaitForUserDialog("Select Calibration ROI", "Draw a region on the image and click OK.");
        dialog.show();
            
            // Get the drawn region
        Roi calibration_roi = avImagePlus.getRoi();


        for (int i=0;i<5;i++){
            Result[i] = SpectralCalibration.calibrate(directory+nd2files[i],calibration_roi);
        }
        WeightedObservedPoints obs_x = new WeightedObservedPoints();
        WeightedObservedPoints obs_y = new WeightedObservedPoints();
        for (int i = 0; i < xData.length; i++) {
            System.out.println("X Distance:"+Result[i][0]);
            obs_x.add(xData[i],Result[i][0]);
            obs_y.add(xData[i],Result[i][1]);
        }

        PolynomialCurveFitter fitter_x = PolynomialCurveFitter.create(2);
        PolynomialCurveFitter fitter_y = PolynomialCurveFitter.create(2);
        double[] x_parameters = fitter_x.fit(obs_x.toList());
        double[] y_parameters = fitter_y.fit(obs_y.toList());

        // Parameters: parameters[0] = a, parameters[1] = b, parameters[2] = c,
        // parameters[3] = d, parameters[4] = e
        double c = x_parameters[0];
        double b = x_parameters[1];
        double a = x_parameters[2];

        XYSeries fitted = new XYSeries("Fitted curve");
        for (int i = 300; i<= 800; i++){
            //double y = coefficients[4]* i* i* i* i + coefficients[3]* i* i* i + coefficients[2] * i * i + coefficients[1] * i + coefficients[0];
            double y = a* i * i+ b * i +c;
            fitted.add(i, y);
        }

        // Print the best-fit parameters (a and b)
        System.out.println("Best-fit parameter (a): " + a);
        System.out.println("Best-fit parameter (b): " + b);
        System.out.println("Best-fit parameter (c): " + c);
        XYSeries dist_wavelength = new XYSeries("Distance");
        for (int i = 0; i < xData.length; i++) {
            System.out.println("Distance:"+Result[i][0]);
            dist_wavelength.add(xData[i],Result[i][0]);
        }
        
        XYSeriesCollection parabolaset_zeroth = new XYSeriesCollection();
        parabolaset_zeroth.addSeries(fitted);
        XYSeriesCollection dataset_first = new XYSeriesCollection();
        dataset_first.addSeries(dist_wavelength);
        JFreeChart parabola_zeroth = ChartFactory.createXYLineChart(
            "Spectral Distance VS Wavelength", // Chart title
            "Wavelength(nm)", // X-axis label
            "Distance(pixels)", // Y-axis label
            parabolaset_zeroth // Dataset
        );
        XYPlot plot = parabola_zeroth.getXYPlot();
        plot.getRangeAxis().setRange(500,1000);
        plot.setDataset(1, dataset_first);
        plot.setRenderer(1, new XYLineAndShapeRenderer());
        
        JFrame frame = new JFrame("Distance VS Wavelength");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new ChartPanel(parabola_zeroth));

        frame.pack();
        frame.setVisible(true);
        
        //
        ImageProcessing.localization(whole_img_filepath,x_parameters,y_parameters);



        /*
        testImage.getProcessor().threshold(170);

        IJ.setAutoThreshold(testImage, "Default");
        IJ.run(testImage, "Threshold", "");
        IJ.run(testImage, "Convert to Mask", "");

        int options = ParticleAnalyzer.SHOW_OUTLINES | ParticleAnalyzer.CLEAR_WORKSHEET | ParticleAnalyzer.ADD_TO_MANAGER;
        int measurements = ParticleAnalyzer.CENTROID;
        ParticleAnalyzer.setRoiManager(null);
        ResultsTable resultsTable = new ResultsTable();
        ParticleAnalyzer analyzer = new ParticleAnalyzer(options, measurements, resultsTable, 0.0, Double.POSITIVE_INFINITY);
        analyzer.analyze(testImage);

        // Step 3: Calculate the centroid for each connected component (dot)
        int numDots = resultsTable.getCounter();
        float[] centroidX = resultsTable.getColumn(ResultsTable.X_CENTROID);
        float[] centroidY = resultsTable.getColumn(ResultsTable.Y_CENTROID);

        // Display the centroid of each dot
        for (int i = 0; i < numDots; i++) {
            float x = centroidX[i];
            float y = centroidY[i];
            System.out.println("Dot " + (i + 1) + " Centroid: (" + x + ", " + y + ")");
        }


        //// inside ImageJ we could run
        // IJ.run(testImage, "Normalisation", "");
        // ImagePlus resultImage = IJ.getImage();
        //// but from within the IDE we need to do that:

        //ImagePlus resultImage = ImageNormalizerPlugin.normalize(testImage);
        //resultImage.setTitle("Result using IJ1");
        //resultImage.show(); */
    }
}
