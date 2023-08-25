package images;
import ij.IJ;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.Roi;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import ij.gui.TextRoi;
import ij.gui.WaitForUserDialog;
import ij.ImagePlus;
import ij.ImageStack;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdTree;

import loci.formats.ImageReader;
import Services.service;
import ij.gui.GenericDialog;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
//import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.Params;
import org.opencv.features2d.SimpleBlobDetector;


public class ImageProcessing implements PlugInFilter {

    @Override
    public void run(ImageProcessor arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    @Override
    public int setup(String arg0, ImagePlus arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setup'");
    }

    public static KdTree build_zeroth_order_kdtree(List<float[]> centroids) {
        KdTree zeroth_kdtree = new KdTree();
        for (int i = 0;i < centroids.size();i++){
            Coordinate node = new Coordinate(centroids.get(i)[0],centroids.get(i)[1]);
            zeroth_kdtree.insert(node);
        }
        return zeroth_kdtree;
    }

    public static List<float[]> localization(String image_filePath, double[] x_parameters, double[] y_parameters) throws Exception{
        //OpenCV workflow. To be implemented
        /*System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List<Mat> frames = null;
        try {
            boolean succeed = Imgcodecs.imreadmulti(filePath, frames);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            ImageReader reader = new ImageReader();
            reader.setOriginalMetadataPopulated(true);
            reader.setId(image_filePath);

            // Create an ImagePlus from the ImageProcessor
            ImagePlus original_imageplus = service.read_first_slice_ND2(reader,10000);
            
            //ImageProcessor ip = first_order_imagePlus.getStack().getProcessor(0);
            //ImagePlus display_ip = new ImagePlus("2",ip);
            //first_order_imagePlus.show();

            original_imageplus.show();
            
            /*float[] kernel = {
                    0.0625f, 0.125f, 0.0625f ,
                    0.125f , 0.25f , 0.125f ,
                    0.0625f, 0.125f, 0.0625f 
                };*/
            float[] kernel = {
                    0.1111f, 0.1111f, 0.1111f ,
                    0.1111f , 0.1111f , 0.1111f ,
                    0.1111f, 0.1111f, 0.1111f 
                };
            int kernelWidth = 3;
            int kernelHeight = 3;
            List<float[]> matched_centroids = new ArrayList<>();
            

            WaitForUserDialog dialog = new WaitForUserDialog("Draw Region", "Draw a region on the image and click OK.");
            dialog.show();
            
            // Get the drawn region
            Roi zeroth_roi = original_imageplus.getRoi();
            Rectangle rect;
            if (zeroth_roi != null) {
                rect = zeroth_roi.getBounds();
                
                System.out.println("Point: X = " + rect.x + ", Y = " + rect.y+", width = "+rect.getWidth()+", height = "+rect.getHeight());

            } else {
                System.out.println("No region drawn.");
                return matched_centroids;
            }

            int central_wavelength = 690;

            //calculate X_distance and Y_distance based on spectral calibration

            double x_distance = x_parameters[2]*central_wavelength*central_wavelength + x_parameters[1]*central_wavelength + x_parameters[0];
            double y_distance = y_parameters[2]*central_wavelength*central_wavelength + y_parameters[1]*central_wavelength + y_parameters[0]+6; //why +6? need to confirm with Ben
            System.out.println("x_distance = " + x_distance + ", y_distance = " + y_distance);

            Roi first_roi = new Roi(rect.x+x_distance,rect.y+y_distance,rect.getWidth(),rect.getHeight());

            Overlay overlay = new Overlay();
            overlay.add(first_roi);
            overlay.add(zeroth_roi);
            ImageProcessor zeroth_first_imageprocessor = original_imageplus.getProcessor();
            ImagePlus zeroth_first_imageplus = new ImagePlus("display", zeroth_first_imageprocessor);
            zeroth_first_imageplus.setOverlay(overlay);
            zeroth_first_imageplus.show();

            // Display the image
            
            //return matched_centroids;
            
            for (int i = 10001; i < 10003; i++) {

                ImageProcessor uncropped_img = service.read_first_slice_ND2(reader,i).getProcessor();

                uncropped_img.setRoi(zeroth_roi);
                ImageProcessor zeroth_slice = uncropped_img.crop();
                uncropped_img.setRoi(first_roi);
                ImageProcessor first_slice = uncropped_img.crop();

                //get the image processor
                Overlay zeroth_overlay = new Overlay();
                Overlay first_overlay = new Overlay();
                    

                FloatProcessor zeroth_fp = zeroth_slice.convertToFloatProcessor();
                FloatProcessor first_fp = first_slice.convertToFloatProcessor();
                Convolver convolver = new Convolver();
                convolver.setNormalize(true);
                convolver.convolve(zeroth_fp, kernel, kernelWidth, kernelHeight);
                convolver.convolve(first_fp, kernel, kernelWidth, kernelHeight);
                ImageProcessor zeroth_resultIp = zeroth_fp.convertToByteProcessor(true);
                ImageProcessor first_resultIp = first_fp.convertToByteProcessor(true);
                //apply gaussian blur filter

                int zeroth_width = zeroth_slice.getWidth();
                int zeroth_height = zeroth_slice.getHeight();
                    
                int first_width = first_slice.getWidth();
                int first_height = first_slice.getHeight();

                ImageProcessor zeroth_seg_ip = zeroth_resultIp.duplicate();
                ImageProcessor first_seg_ip = first_resultIp.duplicate();
                int threshold_value = (int) (zeroth_seg_ip.getStatistics().mean + 2.25 *zeroth_seg_ip.getStatistics().stdDev);
                zeroth_seg_ip.threshold(threshold_value);
                threshold_value = (int) (first_seg_ip.getStatistics().mean + 2.25 *first_seg_ip.getStatistics().stdDev);
                first_seg_ip.threshold(threshold_value);

                Point[][] pointArray = new Point[first_width][first_height];


                for (int y = 0; y < first_height; y++) {
                    for (int x = 0; x < first_width; x++) {
                        int pixelValue = first_seg_ip.get(x, y);
                        pointArray[x][y] = new Point(pixelValue);
                    }
                }

                List<float[]> zeroth_centroids = new ArrayList<>();
                List<float[]> first_centroids = new ArrayList<>();
                for (int y = 0; y < first_height; y++) {
                    for (int x = 0; x < first_width; x++) {
                        if (pointArray[x][y].visited == false) {
                            pointArray[x][y].visited = true;
                            if (pointArray[x][y].color == 255){
                                List<int[]> cur_pointset = new ArrayList<>();
                                cur_pointset.add(new int[] { x, y });
                                service.findSet(x,y,pointArray,cur_pointset);
                                int[] boundary = service.findBoundary(cur_pointset);

                                int edge = Math.max(boundary[2]-boundary[0]+1,boundary[3]-boundary[1]+1);
                                float centerX = ((float)boundary[2] + (float)boundary[0])/2;
                                float centerY = ((float)boundary[3]+ (float)boundary[1])/2;

                                double[] params;

                                Roi roi = new Roi(boundary[0]-1,boundary[1]-1,boundary[2]-boundary[0]+3,boundary[3]-boundary[1]+3);
                                
                                first_slice.setRoi(roi);
                                ImageProcessor cropped = first_slice.crop();
                                float[] cur_zeroth_centroid = new float[2];

                                if (edge == 1){
                                    System.out.println("edge = 1");
                                    params = service.fit2DGaussian(cropped);
                                    cur_zeroth_centroid[0] = (float) (boundary[0] + params[0]);
                                    cur_zeroth_centroid[1] = (float) (boundary[1] + params[1]);
                                }
                                else {
                                    GaussianCurveFitter fitter = GaussianCurveFitter.create();
                                    WeightedObservedPoints row_value = new WeightedObservedPoints();
                                    for (int row_num = 0; row_num < cropped.getHeight(); row_num++) {
                                        int row_sum = 0;
                                        for (int column_num = 0; column_num < cropped.getWidth(); column_num++) {
                                            row_sum += cropped.get(column_num,row_num);
                                        }
                                        row_value.add(row_num,row_sum);
                                    }

                                    WeightedObservedPoints column_value = new WeightedObservedPoints();
                                    for (int column_num = 0; column_num < cropped.getWidth(); column_num++) {
                                        int column_sum = 0;
                                        for (int row_num = 0; row_num < cropped.getHeight(); row_num++) {
                                            column_sum += cropped.get(column_num,row_num);
                                        }
                                        column_value.add(column_num,column_sum);
                                    }
                                    
                                    double[] bestFit_Y = fitter.fit(row_value.toList());
                                    double[] bestFit_X = fitter.fit(column_value.toList());
                                    cur_zeroth_centroid[0] = (float) (boundary[0] + bestFit_X[1]);
                                    cur_zeroth_centroid[1] = (float) (boundary[1] + bestFit_Y[1]);
                                    
                                }
                                    
                                

                                    //double centroid_x = (cropped.getRoi().getMinX() + params[0]);
                                    //double centroid_y = (cropped.getRoi().getMinY() + params[1]);

                                
                                
                                first_centroids.add(cur_zeroth_centroid);
                                    
                                int armLength = 3;
                                Roi horizontalLine = new Roi((int) cur_zeroth_centroid[0] - armLength, (int) cur_zeroth_centroid[1], 2 * armLength, 1);
                                first_overlay.add(horizontalLine);

                                    // Create the vertical arm
                                Roi verticalLine = new Roi((int) cur_zeroth_centroid[0], (int) cur_zeroth_centroid[1] - armLength, 1, 2 * armLength);
                                first_overlay.add(verticalLine);
                                    
                                first_overlay.setStrokeColor(Color.YELLOW);
                            }
                        }
                    }
                }


                ImagePlus overlayImagePlus = new ImagePlus("Overlay Image", first_seg_ip);
                overlayImagePlus.setOverlay(first_overlay);
                overlayImagePlus.show();
            }
            reader.close();
            return matched_centroids;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}