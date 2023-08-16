package de.mpicbg.scf.rhaase.fiji.ij2course.images;

import ij.IJ;
import ij.gui.Roi;
import ij.ImagePlus;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import net.imglib2.algorithm.localization.Gaussian;
import net.imglib2.algorithm.localization.LevenbergMarquardtSolver;
import net.imglib2.algorithm.localization.LocalizationUtils;
import net.imglib2.algorithm.localization.MLGaussianEstimator;
import net.imglib2.algorithm.localization.Observation;
import ij.measure.Measurements;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;

import java.util.ArrayList;
import java.util.List;
import loci.plugins.BF;
import net.imglib2.img.ImagePlusAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    static class Point {
        int color;
        boolean visited;

        public Point(int color) {
            this.color = color;
            this.visited = false;
        }
    }

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G + DOES_16 + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        // Replace "path/to/your/nd2file.nd2" with the actual path to your ND2 file
        String inputFilePath = "path/to/your/nd2file.nd2";

        // Read the ND2 file using Bio-Formats
        ImagePlus imagePlus = readND2(inputFilePath);

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

    private static ImagePlus readND2(String filePath) {
        try {
            // Use Bio-Formats to read the ND2 file
            return BF.openImagePlus(filePath)[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void findSet(int x, int y, Point[][] points, List<int[]> pointSet) {
      // Get the point at the current coordinates
        Point currentPoint_left = points[x-1][y];
        Point currentPoint_down = points[x][y-1];
        Point currentPoint_right = points[x+1][y];
        Point currentPoint_up = points[x][y+1];

        // Check if the point is valid (color is 255 and not visited)
        if (currentPoint_left.color == 255 && !currentPoint_left.visited) {
            // Mark the current point as visited
            currentPoint_left.visited = true;

            // Add the current point to the pointSet
            pointSet.add(new int[] { x-1, y });
            // Recursively check neighboring points
            findSet(x - 1, y, points, pointSet); // left
        }
        if (currentPoint_down.color == 255 && !currentPoint_down.visited) {
            // Mark the current point as visited
            currentPoint_down.visited = true;

            // Add the current point to the pointSet
            pointSet.add(new int[] { x, y-1 });
            // Recursively check neighboring points
            findSet(x, y-1, points, pointSet); // left
        }
        if (currentPoint_right.color == 255 && !currentPoint_right.visited) {
            // Mark the current point as visited
            currentPoint_right.visited = true;

            // Add the current point to the pointSet
            pointSet.add(new int[] { x+1, y });
            // Recursively check neighboring points
            findSet(x + 1, y, points, pointSet); // left
        }
        if (currentPoint_up.color == 255 && !currentPoint_up.visited) {
            // Mark the current point as visited
            currentPoint_up.visited = true;

            // Add the current point to the pointSet
            pointSet.add(new int[] { x, y+1 });
            // Recursively check neighboring points
            findSet(x , y+1, points, pointSet); // left
        }
        return;
    }

    private static List<float[]> findCentroid(ImageProcessor averagedIp) throws Exception {
        List<float[]> centroids = new ArrayList<>();
        
        float[] kernel = {
             0.0625f, 0.125f, 0.0625f ,
             0.125f , 0.25f , 0.125f ,
             0.0625f, 0.125f, 0.0625f 
        };
        int kernelWidth = 3;
        int kernelHeight = 3;

        FloatProcessor fp = averagedIp.convertToFloatProcessor();

        Convolver convolver = new Convolver();
        convolver.setNormalize(true);
        convolver.convolve(fp, kernel, kernelWidth, kernelHeight);

        // Convert the result back to a byte processor
        ImageProcessor resultIp = fp.convertToByteProcessor(true);
        int width = resultIp.getWidth();
        int height = resultIp.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = resultIp.get(x, y);
                if (pixelValue < 20) {
                    resultIp.set(x, y, 0);
                } else {
                    resultIp.set(x, y, 255);
                }
            }
        }
        Point[][] pointArray = new Point[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = resultIp.get(x, y);
                pointArray[x][y] = new Point(pixelValue);
            }
        }

        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pointArray[x][y].visited == false) {
                    pointArray[x][y].visited = true;
                    if (pointArray[x][y].color == 255){
                        List<int[]> cur_pointset = new ArrayList<>();
                        findSet(x,y,pointArray,cur_pointset);
                        int minX = Integer.MAX_VALUE;
                        int maxX = Integer.MIN_VALUE;
                        int minY = Integer.MAX_VALUE;
                        int maxY = Integer.MIN_VALUE;

                        for (int[] point : cur_pointset) {
                            if (point[0] < minX) {minX = point[0];}
                            if (point[1] < minY) {minY = point[1];}
                            if (point[0] > maxX) {maxX = point[0];}
                            if (point[1] > maxY) {maxY = point[1];}
                        }
                        
                        System.out.println("minX, minY: "+minX+","+minY);
                        System.out.println("maxX, maxY: "+maxX+","+maxY);
                        Roi roi = new Roi(minX-2,minY-2,maxX-minX+4,maxY-minY+4);
                        averagedIp.setRoi(roi);
                        ImageProcessor cropped = averagedIp.crop();
                        double[] params = fit2DGaussian(cropped);

                        //double centroid_x = (cropped.getRoi().getMinX() + params[0]);
			            //double centroid_y = (cropped.getRoi().getMinY() + params[1]);
                        System.out.println("centroid_x: "+params[0]);
                        System.out.println("centroid_y: "+params[1]);


                        float[] cur_centroid = new float[2];
                        cur_centroid[0] = (float) (minX - 2 + params[0]);
                        cur_centroid[1] = (float) (minY - 2 + params[1]);
                        centroids.add(cur_centroid);
                        
                    }
                }
            }
        }
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

    private static double[] fit2DGaussian(ImageProcessor ip) throws Exception {
		long[] center = new long[] { (long)ip.getWidth() / 2, (long)ip.getHeight() / 2 };
		net.imglib2.Point cpoint = new net.imglib2.Point(center);

		Observation data = LocalizationUtils.gatherObservationData(ImagePlusAdapter.wrapShort(new ImagePlus("", ip)), cpoint, center);

		double[] params = new MLGaussianEstimator(2.0, 2).initializeFit(cpoint, data);

		LevenbergMarquardtSolver.solve(data.X, params, data.I, new Gaussian(), 1e-3, 1e-1, 300);

		return params;
	}

    public static ImagePlus calibrate(String filePath) throws Exception {
        // Replace "path/to/your/nd2file.nd2" with the actual path to your ND2 file
       
        ImagePlus imagePlus = readND2(filePath);

        // Check if the ND2 file was successfully read
        if (imagePlus != null) {
            // Get the number of frames (images) in the ND2 file
            int numFrames = imagePlus.getStackSize();

            System.out.println("NumFrames: "+numFrames);
            // Average all the frames
            ImageProcessor averagedIp = imagePlus.getStack().getProcessor(1).duplicate(); // Initialize with the first frame

            for (int i = 2; i <= numFrames; i++) {
                ImageProcessor currentIp = imagePlus.getStack().getProcessor(i);
                averagedIp.copyBits(currentIp, 0, 0, 3);//Add
            }

            // Divide by the number of frames to compute the average
            averagedIp.multiply(1.0 / numFrames);

            //ImageProcessor filtered = GaussianFilter(averagedIp);

            List<float[]> centroids = findCentroid(averagedIp);

            String csvFname = "C:\\Users\\FOIL_member\\Downloads\\centroids.csv";

            writeFloatArrayListToCSV(centroids, csvFname);

            float left_min_x = averagedIp.getWidth(), right_min_x = averagedIp.getWidth();
            float[] left_min_x_centroid = centroids.get(0);
            float left_max_x = 0, right_max_x = 0;
            float[] left_max_x_centroid = centroids.get(0);
            float left_min_y = averagedIp.getHeight(), right_min_y = averagedIp.getHeight();
            float[] left_min_y_centroid = centroids.get(0);
            float left_max_y = 0, right_max_y = 0;
            float[] left_max_y_centroid = centroids.get(0);

            for (float[] centroid : centroids) {
                if (centroid[0] < averagedIp.getWidth()/2){
                    if (centroid[0] < left_min_x) {
                        left_min_x = centroid[0];
                        left_min_x_centroid = centroid;
                    }
                    if (centroid[0] > left_max_x) {
                        left_max_x = centroid[0];
                        left_max_x_centroid = centroid;
                    }
                    if (centroid[1] < left_min_y) {
                        left_min_y = centroid[1];
                        left_min_y_centroid = centroid;
                    }
                    if (centroid[1] < left_max_x) {
                        left_max_x = centroid[1];
                        left_max_y_centroid = centroid;
                    }
                }
                else{
                    if (centroid[0] < right_min_x) {
                        right_min_x = centroid[0];
                        left_min_x_centroid = centroid;
                    }
                    if (centroid[0] > right_max_x) {
                        right_max_x = centroid[0];
                        left_max_x_centroid = centroid;
                    }
                    if (centroid[1] < right_min_y) {
                        right_min_y = centroid[1];
                        left_min_y_centroid = centroid;
                    }
                    if (centroid[1] < right_max_x) {
                        right_max_x = centroid[1];
                        left_max_y_centroid = centroid;
                    }
                }
            }

            // Create a new ImagePlus with the averaged image
            ImagePlus averagedImage = new ImagePlus("Averaged", averagedIp);

            // Replace "path/to/output/averaged.tif" with the desired path for the output averaged file
            String outputFilePath = "C:\\Users\\FOIL_member\\Downloads\\averaged.tif";

            // Save the averaged image as a new file
            IJ.saveAsTiff(averagedImage, outputFilePath);
            return averagedImage;
        }
        return imagePlus;
    }
}
