package images;
import ij.IJ;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdTree;

import Services.service;
import ij.gui.GenericDialog;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
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

    public static List<float[]> localization(String zeroth_order_filePath, String first_order_filePath) throws Exception{
        //OpenCV workflow. To be implemented
        /*System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List<Mat> frames = null;
        try {
            boolean succeed = Imgcodecs.imreadmulti(filePath, frames);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        ImagePlus zeroth_order_imagePlus = IJ.openImage(zeroth_order_filePath);
        ImagePlus first_order_imagePlus = IJ.openImage(first_order_filePath);
        
        float[] kernel = {
                0.0625f, 0.125f, 0.0625f ,
                0.125f , 0.25f , 0.125f ,
                0.0625f, 0.125f, 0.0625f 
            };
        int kernelWidth = 3;
        int kernelHeight = 3;
        List<float[]> matched_centroids = new ArrayList<>();
        if (zeroth_order_imagePlus != null) {
            // Display information about the stack
            System.out.println("Stack Title: " + zeroth_order_imagePlus.getTitle());
            System.out.println("Number of Slices: " + zeroth_order_imagePlus.getStackSize());
            
            // Access the individual images in the stack
            ImageStack zeroth_order_stack = zeroth_order_imagePlus.getStack();
            ImageStack first_order_stack = first_order_imagePlus.getStack();
            for (int i = 10000; i < 10001; i++) {
                ImageProcessor zeroth_slice = zeroth_order_stack.getProcessor(i);
                ImageProcessor first_slice = first_order_stack.getProcessor(i);
                //get the image processor
                Overlay zeroth_overlay = new Overlay();
                Overlay first_overlay = new Overlay();
                //initialize overlay for visualization

                ImagePlus zeroth_slicImagePlus = new ImagePlus("0", zeroth_slice);
                zeroth_slicImagePlus.show();
                ImagePlus first_slicImagePlus = new ImagePlus("1", first_slice);
                first_slicImagePlus.show();

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

                Point[][] pointArray = new Point[zeroth_width][zeroth_height];


                for (int y = 0; y < zeroth_height; y++) {
                    for (int x = 0; x < zeroth_width; x++) {
                        int pixelValue = zeroth_seg_ip.get(x, y);
                        pointArray[x][y] = new Point(pixelValue);
                    }
                }

                List<float[]> zeroth_centroids = new ArrayList<>();
                List<float[]> first_centroids = new ArrayList<>();
                for (int y = 0; y < zeroth_height; y++) {
                    for (int x = 0; x < zeroth_width; x++) {
                        if (pointArray[x][y].visited == false) {
                            pointArray[x][y].visited = true;
                            if (pointArray[x][y].color == 255){
                                List<int[]> cur_pointset = new ArrayList<>();
                                cur_pointset.add(new int[] { x, y });
                                service.findSet(x,y,pointArray,cur_pointset);
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
                                int edge = Math.max(maxX-minX+1,maxY-minY+1);
                                float centerX = ((float)maxX+ (float)minX)/2;
                                float centerY = ((float)maxY+ (float)minY)/2;
                                
                                Roi roi = new Roi(centerX-edge/2,centerY-edge/2,edge,edge);
                                System.out.println("minX:"+minX+", minY:"+minY+", maxX:"+maxX+", maxY:"+maxY);
                                if (minX == Integer.MAX_VALUE){
                                    for (int[] point : cur_pointset) {
                                        System.out.println("point[0]:"+point[0]+", point[1]:"+point[1]);
                                    }
                                }
                                zeroth_slice.setRoi(roi);
                                ImageProcessor cropped = zeroth_slice.crop();
                                double[] params = service.fit2DGaussian(cropped);

                                //double centroid_x = (cropped.getRoi().getMinX() + params[0]);
                                //double centroid_y = (cropped.getRoi().getMinY() + params[1]);

                                float[] cur_zeroth_centroid = new float[2];
                                cur_zeroth_centroid[0] = (float) (minX + params[0]);
                                cur_zeroth_centroid[1] = (float) (minY + params[1]);
                                zeroth_centroids.add(cur_zeroth_centroid);
                                
                                int armLength = 3;
                                Roi horizontalLine = new Roi((int) cur_zeroth_centroid[0] - armLength, (int) cur_zeroth_centroid[1], 2 * armLength, 1);
                                zeroth_overlay.add(horizontalLine);

                                // Create the vertical arm
                                Roi verticalLine = new Roi((int) cur_zeroth_centroid[0], (int) cur_zeroth_centroid[1] - armLength, 1, 2 * armLength);
                                zeroth_overlay.add(verticalLine);
                                
                                zeroth_overlay.setStrokeColor(Color.YELLOW);
                            }
                        }
                    }
                }

                KdTree zeroth_kdtree = build_zeroth_order_kdtree(zeroth_centroids);
                Envelope query_Envelope = new Envelope(zeroth_height, first_width, first_height, threshold_value);
                zeroth_kdtree.query();

        
                // Load the image
                /*Mat image = frames.get(i);
                
                // Apply Gaussian blur to reduce noise
                Mat blurred = new Mat();
                Imgproc.cvtColor(image, blurred, Imgproc.COLOR_BGR2GRAY);
                //Imgproc.GaussianBlur(image, blurred, new Size(3, 3), 0);
                
                // Apply the Laplacian of Gaussian (LoG) filter
                SimpleBlobDetector detector = SimpleBlobDetector.create();
                MatOfKeyPoint keypoints = new MatOfKeyPoint();
                detector.detect(blurred, keypoints);
                
                Mat outputImage = new Mat();
                Features2d.drawKeypoints(image, keypoints, outputImage, new Scalar(0, 0, 255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);

                // Display the output image
                Imgcodecs.imwrite("output_image.jpg", outputImage);*/

                ImagePlus overlayImagePlus = new ImagePlus("Overlay Image", zeroth_seg_ip);
                overlayImagePlus.setOverlay(zeroth_overlay);
                overlayImagePlus.show();
            }
            

            
        } else {
            System.out.println("Failed to open the stack.");
        }
        return matched_centroids;
    }
    
}