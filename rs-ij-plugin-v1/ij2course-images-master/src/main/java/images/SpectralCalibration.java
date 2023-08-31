package images;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.ImagePlus;
import com.opencsv.CSVWriter;

import Services.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import java.io.FileWriter;
import net.imglib2.algorithm.localization.Gaussian;
import net.imglib2.algorithm.localization.LevenbergMarquardtSolver;
import net.imglib2.algorithm.localization.LocalizationUtils;
import net.imglib2.algorithm.localization.MLGaussianEstimator;
import net.imglib2.algorithm.localization.Observation;
import ij.measure.Measurements;
import ij.plugin.ContrastEnhancer;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.min;

import loci.plugins.BF;
import loci.poi.hssf.util.HSSFColor.RED;
import net.imglib2.img.ImagePlusAdapter;

import java.io.IOException;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: May 2017
 * <p>
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
 */

public class SpectralCalibration implements PlugInFilter {

    private static service findService;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G + DOES_16 + DOES_32;
    }

    public SpectralCalibration(service findService){
        SpectralCalibration.findService = findService;
    }

    @Override
    public void run(ImageProcessor ip) {
        // Replace "path/to/your/nd2file.nd2" with the actual path to your ND2 file
        String inputFilePath = "path/to/your/nd2file.nd2";

        // Read the ND2 file using Bio-Formats
        ImagePlus imagePlus = service.readND2(inputFilePath);

        // Check if the ND2 file was successfully read
        if (imagePlus != null) {
            // Get the number of frames (images) in the ND2 file
            int numFrames = imagePlus.getStackSize();

            // Average all the frames
            ImageProcessor averagedIp = imagePlus.getStack().getProcessor(1).duplicate(); // Initialize with the first frame

            for (int i = 2; i <= numFrames; i++) {
                ImageProcessor currentIp = imagePlus.getStack().getProcessor(i);
                averagedIp.copyBits(currentIp, 0, 0, 3);//Add
            }

            // Divide by the number of frames to compute the average
            averagedIp.multiply(1.0 / numFrames);

            // Create a new ImagePlus with the averaged image
            ImagePlus averagedImage = new ImagePlus("Averaged", averagedIp);

            // Replace "path/to/output/averaged.tif" with the desired path for the output averaged file
            String outputFilePath = "C:\\Users\\FOIL_member\\Downloads\\averaged.tif";

            // Save the averaged image as a new file
            IJ.saveAsTiff(averagedImage, outputFilePath);
        }
    }


    private static List<float[]> findCentroid(ImageProcessor averagedIp,Roi calibration_roi) throws Exception {
        List<float[]> centroids = new ArrayList<>();
        
        float[] kernel = {
             0.0625f, 0.125f, 0.0625f ,
             0.125f , 0.25f , 0.125f ,
             0.0625f, 0.125f, 0.0625f 
        };
        int kernelWidth = 3;
        int kernelHeight = 3;
        Overlay overlay = new Overlay();

        // Display the adjusted image

        ImageProcessor processIP = averagedIp.duplicate();
        processIP.convolve(kernel, kernelWidth, kernelHeight);
        /*int[] histogram = processIP.getHistogram();

        for (int i = 0; i < 1000; i++) {
            IJ.log(i + ": " + histogram[i]);
        }*/
        
        processIP.setRoi(calibration_roi);
        averagedIp.setRoi(calibration_roi);
        ImageProcessor seg_ip = averagedIp.crop();
        ImageProcessor threashold_seg_ip = processIP.crop();
        int width = seg_ip.getWidth();
        int height = seg_ip.getHeight();

        int threshold_value = (int) (threashold_seg_ip.getStatistics().mean + 3.6 *threashold_seg_ip.getStatistics().stdDev);
        System.out.println("Mean is:"+threashold_seg_ip.getStatistics().mean);
        System.out.println("threshold_value is:"+threshold_value);
        threashold_seg_ip.threshold(threshold_value);

        Point[][] pointArray = new Point[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = threashold_seg_ip.get(x, y);
                pointArray[x][y] = new Point(pixelValue);
            }
        }

        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pointArray[x][y].visited == false) {
                    pointArray[x][y].visited = true;
                    if (pointArray[x][y].color == 255){
                        List<int[]> cur_pointset = new ArrayList<>();
                        //cur_pointset.add(new int[] { x, y });
                        service.findSet(x,y,pointArray,cur_pointset);
                        
                        int[] boundary = service.findBoundary(cur_pointset);
                        
                        Roi roi = new Roi(boundary[0]-2,boundary[1]-2,boundary[2]-boundary[0]+5,boundary[3]-boundary[1]+5);
                        
                        
                        seg_ip.setRoi(roi);
                        ImageProcessor cropped = seg_ip.crop();
                        
                        double[] params = service.fit2DGaussian(cropped);

                        //double centroid_x = (cropped.getRoi().getMinX() + params[0]);
			            //double centroid_y = (cropped.getRoi().getMinY() + params[1]);


                        float[] cur_centroid = new float[2];
                        cur_centroid[0] = (float) (boundary[0] - 2 + params[0]);
                        cur_centroid[1] = (float) (boundary[1] - 2 + params[1]);
                        centroids.add(cur_centroid);
                        
                        int armLength = 3;
                        int centerX = (int) cur_centroid[0];
                        int centerY = (int) cur_centroid[1];
                        Roi horizontalLine = new Roi(centerX - armLength, centerY, 2 * armLength, 1);
                        overlay.add(horizontalLine);

                        // Create the vertical arm
                        Roi verticalLine = new Roi(centerX, centerY - armLength, 1, 2 * armLength);
                        overlay.add(verticalLine);
                        
                        overlay.setStrokeColor(Color.RED);
                        
                    }
                }
            }
        }

        ImagePlus overlayImagePlus = new ImagePlus("Overlay Image", seg_ip);
        overlayImagePlus.setOverlay(overlay);
        overlayImagePlus.show();
        return centroids;
    }

    public static void writeFloatArrayListToCSV(List<float[]> floatArrayList, String csvFileName) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFileName))) {
            // Writing each float array as a separate line in the CSV file
            for (float[] floatArray : floatArrayList) {
                String[] stringArray = new String[floatArray.length];
                for (int i = 0; i < floatArray.length; i++) {
                    stringArray[i] = String.valueOf(floatArray[i]);
                }
                writer.writeNext(stringArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static float[] calibrate(String filePath,Roi calibration_roi) throws Exception {
        // Replace "path/to/your/nd2file.nd2" with the actual path to your ND2 file
       
        ImagePlus imagePlus = service.readND2(filePath);
        float[] auto_calibrated = new float[2];

        System.out.println("Calibrate x: "+calibration_roi);

        // Check if the ND2 file was successfully read
        if (imagePlus != null) {
            // Get the number of frames (images) in the ND2 file
            int numFrames = imagePlus.getStackSize();

            System.out.println("NumFrames: "+numFrames);
            // Average all the frames
            ImageProcessor firstIp = imagePlus.getStack().getProcessor(1).duplicate(); // Initialize with the first frame

            float[] accPixels = new float[firstIp.getPixelCount()];

            // Loop through each slice in the stack
            for (int i = 1; i <= numFrames; i++) {
                // Get the ImageProcessor of the current slice
                ImageProcessor sliceIp = imagePlus.getStack().getProcessor(i);

                // Accumulate pixel values
                for (int p = 0; p < accPixels.length; p++) {
                    accPixels[p] += sliceIp.getf(p);
                }
            }

            // Calculate the average by dividing accumulated values
            for (int p = 0; p < accPixels.length; p++) {
                accPixels[p] /= numFrames;
            }

            // Create a new ImageProcessor for the average image
            ImageProcessor avgIp = firstIp.createProcessor(firstIp.getWidth(), firstIp.getHeight());

            // Set the average pixel values to the new ImageProcessor
            for (int p = 0; p < accPixels.length; p++) {
                avgIp.setf(p, accPixels[p]);
            }

            for (int y = 0; y < avgIp.getHeight(); y++) {
                for (int x = 0; x < avgIp.getWidth(); x++) {
                    //int pixelValue = threashold_seg_ip.get(x, y);
                    //pointArray[x][y] = new Point(pixelValue);
                    if (avgIp.get(x,y) >= 2048){
                        System.out.println("Pixel val:"+avgIp.get(x,y));
                    }
                }
            }

            //ImageProcessor filtered = GaussianFilter(averagedIp);

            List<float[]> centroids = findCentroid(avgIp,calibration_roi);

            String csvFname = "C:\\Users\\xufen\\Downloads\\centroids.csv";

            writeFloatArrayListToCSV(centroids, csvFname);

            float left_min_x = avgIp.getWidth(), right_min_x = avgIp.getWidth();
            float left_max_x = 0, right_max_x = 0;
            float left_min_y = avgIp.getHeight(), right_min_y = avgIp.getHeight();
            float left_max_y = 0, right_max_y = 0;
            float[][] left_centroids = new float[4][2];
            float[][] right_centroids = new float[4][2];

            for (float[] centroid : centroids) {
                if (centroid[0] < avgIp.getWidth()/2){
                    if (centroid[0] < left_min_x) {
                        left_min_x = centroid[0];
                        left_centroids[0] = centroid;
                    }
                    if (centroid[0] > left_max_x) {
                        left_max_x = centroid[0];
                        left_centroids[1] = centroid;
                    }
                    if (centroid[1] < left_min_y) {
                        left_min_y = centroid[1];
                        left_centroids[2] = centroid;
                    }
                    if (centroid[1] > left_max_y) {
                        left_max_y = centroid[1];
                        left_centroids[3] = centroid;
                    }
                }
                else{
                    if (centroid[0] < right_min_x) {
                        right_min_x = centroid[0];
                        right_centroids[0] = centroid;
                    }
                    if (centroid[0] > right_max_x) {
                        right_max_x = centroid[0];
                        right_centroids[1] = centroid;
                    }
                    if (centroid[1] < right_min_y) {
                        right_min_y = centroid[1];
                        right_centroids[2] = centroid;
                    }
                    if (centroid[1] > right_max_y) {
                        right_max_y = centroid[1];
                        right_centroids[3] = centroid;
                    }
                }
            }

            float x_sum = 0;
            float y_sum = 0;

            for (int i =0;i<4;i++) {
                x_sum += right_centroids[i][0] - left_centroids[i][0];
                y_sum += right_centroids[i][1] - left_centroids[i][1];
                System.out.println("left: "+left_centroids[i][0]+", "+left_centroids[i][1]);
                System.out.println("right: "+right_centroids[i][0]+", "+right_centroids[i][1]);
            }
            
            auto_calibrated[0] = x_sum/4;
            auto_calibrated[1] = y_sum/4;

            System.out.println("Calibrate x: "+auto_calibrated[0]+", y: "+auto_calibrated[1]);

            // Create a new ImagePlus with the averaged image
            ImagePlus averagedImage = new ImagePlus("Averaged", avgIp);

            // Replace "path/to/output/averaged.tif" with the desired path for the output averaged file
            String outputFilePath = "C:\\Users\\xufen\\Downloads\\averaged.tif";

            // Save the averaged image as a new file
            IJ.saveAsTiff(averagedImage, outputFilePath);
            return auto_calibrated;
        }
        return auto_calibrated;
    }
}
