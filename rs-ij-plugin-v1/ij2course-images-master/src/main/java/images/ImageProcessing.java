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

    //return Roi with respect to first_roi
    private static Roi find_first_roi(Roi zeroth_blob_roi, Roi first_roi, double[] x_parameters, double[] y_parameters) {
        double min_x_distance = x_parameters[2]*650*650 + x_parameters[1]*650 + x_parameters[0];
        double min_y_distance = y_parameters[2]*650*650 + y_parameters[1]*650 + y_parameters[0];
        double max_x_distance = x_parameters[2]*750*750 + x_parameters[1]*750 + x_parameters[0];
        System.out.println("min x distance:"+min_x_distance);
        System.out.println("max x distance:"+max_x_distance);
        //double max_y_distance = y_parameters[2]*710*710 + y_parameters[1]*710 + y_parameters[0];
        Roi first_blob_roi = new Roi(zeroth_blob_roi.getBounds().x + min_x_distance-first_roi.getBounds().x, zeroth_blob_roi.getBounds().y + min_y_distance-2-first_roi.getBounds().y, (int)(max_x_distance-min_x_distance)+zeroth_blob_roi.getBounds().width, zeroth_blob_roi.getBounds().height+4);
        return first_blob_roi;
    }

    private static Roi find_bright_blob(Point[][] pointArray, int x, int y, Roi zeroth_roi) throws Exception{
        List<int[]> cur_pointset = new ArrayList<>();
        cur_pointset.add(new int[] { x, y });
        service.findSet(x,y,pointArray,cur_pointset);
        int[] boundary = service.findBoundary(cur_pointset);

        int edge = Math.max(boundary[2]-boundary[0]+1,boundary[3]-boundary[1]+1);
        float centerX = ((float)boundary[2] + (float)boundary[0])/2;
        float centerY = ((float)boundary[3]+ (float)boundary[1])/2;

        double[] params;

        Roi roi = new Roi(boundary[0]-1+zeroth_roi.getBounds().x,boundary[1]-1+zeroth_roi.getBounds().y,boundary[2]-boundary[0]+3,boundary[3]-boundary[1]+3);
                                
        /*slice.setRoi(roi);
        ImageProcessor cropped = slice.crop();
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
                                    
        }*/
    
        return roi;                   
    }

    public static List<float[]> localization(String image_filePath, double[] x_parameters, double[] y_parameters) throws Exception{

        try {
            ImageReader reader = new ImageReader();
            reader.setOriginalMetadataPopulated(true);
            reader.setId(image_filePath);

            // Create an ImagePlus from the ImageProcessor
            ImagePlus original_imageplus = service.read_first_slice_ND2(reader,10000);

            original_imageplus.show();
            
            float[] kernel = {
                    0.0625f, 0.125f, 0.0625f ,
                    0.125f , 0.25f , 0.125f ,
                    0.0625f, 0.125f, 0.0625f 
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
            int left_wavelength = 670;
            int right_wavelength = 720;

            //calculate X_distance and Y_distance based on spectral calibration

            double x_distance = x_parameters[2]*left_wavelength*left_wavelength + x_parameters[1]*left_wavelength + x_parameters[0];
            double width_inc = x_parameters[2]*right_wavelength*right_wavelength + x_parameters[1]*right_wavelength + x_parameters[0] - x_distance;
            double y_distance = y_parameters[2]*central_wavelength*central_wavelength + y_parameters[1]*central_wavelength + y_parameters[0]; // if calibration is good, this should work
            System.out.println("x_distance = " + x_distance + ", y_distance = " + y_distance +",width_inc ="+width_inc);

            Roi first_roi = new Roi(rect.x+x_distance,rect.y+y_distance,rect.getWidth()+2*width_inc,rect.getHeight());

            Overlay overlay = new Overlay();
            overlay.add(first_roi);
            overlay.add(zeroth_roi);
            ImageProcessor zeroth_first_imageprocessor = original_imageplus.getProcessor();
            ImagePlus zeroth_first_imageplus = new ImagePlus("display", zeroth_first_imageprocessor);
            zeroth_first_imageplus.setOverlay(overlay);
            zeroth_first_imageplus.show();

            // Display the image
            
            //return matched_centroids;
            
            for (int i = 10001; i < 10002; i++) {

                ImageProcessor uncropped_img = service.read_first_slice_ND2(reader,i).getProcessor();

                uncropped_img.setRoi(zeroth_roi);
                ImageProcessor zeroth_slice = uncropped_img.crop();
                uncropped_img.setRoi(first_roi);
                ImageProcessor first_slice = uncropped_img.crop();
                //get the image processor
                    

                ImageProcessor zeroth_resultIp = zeroth_slice.duplicate();
                ImageProcessor first_resultIp = first_slice.duplicate();
                zeroth_resultIp.convolve(kernel,kernelWidth,kernelHeight);
                first_resultIp.convolve(kernel,kernelWidth,kernelHeight);
                //apply gaussian blur filter

                int roi_width = zeroth_slice.getWidth();
                int roi_height = zeroth_slice.getHeight();
                    
                int first_width = first_slice.getWidth();
                int first_height = first_slice.getHeight();

                ImageProcessor zeroth_seg_ip = zeroth_resultIp.duplicate();
                ImageProcessor first_seg_ip = first_resultIp.duplicate();
                int threshold_value = (int) (zeroth_seg_ip.getStatistics().mean + 2.25 *zeroth_seg_ip.getStatistics().stdDev);
                zeroth_seg_ip.threshold(threshold_value);
                threshold_value = (int) (first_seg_ip.getStatistics().mean + 2.25 *first_seg_ip.getStatistics().stdDev);
                first_seg_ip.threshold(threshold_value);

                Point[][] zeroth_pointArray = new Point[roi_width][roi_height];
                Point[][] first_pointArray = new Point[first_width][first_height];


                for (int y = 0; y < roi_height; y++) {
                    for (int x = 0; x < roi_width; x++) {
                        int pixelValue = zeroth_seg_ip.get(x, y);
                        zeroth_pointArray[x][y] = new Point(pixelValue);
                        pixelValue = first_seg_ip.get(x, y);
                        first_pointArray[x][y] = new Point(pixelValue);
                    }
                }

                List<Roi> zeroth_blob_roi_list = new ArrayList<>();
                List<Roi> first_blob_roi_list = new ArrayList<>();
                first_seg_ip.setColor(Color.RED);
                for (int y = 0; y < roi_height; y++) {
                    for (int x = 0; x < roi_width; x++) {
                        if (zeroth_pointArray[x][y].visited == false) {
                            zeroth_pointArray[x][y].visited = true;
                            if (zeroth_pointArray[x][y].color == 255){
                                Roi zeroth_blob_roi = find_bright_blob(zeroth_pointArray,x,y,zeroth_roi);
                                zeroth_blob_roi_list.add(zeroth_blob_roi);
                                
                                Roi first_blob_roi = find_first_roi(zeroth_blob_roi,first_roi,x_parameters,y_parameters);
                                
                                for (int y_first = 0; y_first < first_blob_roi.getBounds().height; y_first++){
                                    for (int x_first = 0; x_first < first_blob_roi.getBounds().width; x_first++){
                                        if (first_pointArray[x_first][y_first].visited == false) {
                                            first_pointArray[x_first][y_first].visited = true;
                                            if (first_pointArray[x_first][y_first].color == 255){
                                                find_bright_blob(first_pointArray,x_first,y_first,first_roi);
                                            }
                                        }
                                    }
                                }

                                if (zeroth_blob_roi.getBounds().width == zeroth_blob_roi.getBounds().height && zeroth_blob_roi.getBounds().width>=7){
                                    Overlay blob_overlay = new Overlay();
                                    blob_overlay.add(zeroth_blob_roi);
                                    blob_overlay.add(first_blob_roi);
                                    blob_overlay.setStrokeColor(Color.RED);
                                    ImagePlus overlayImagePlus = new ImagePlus("Overlay Image", uncropped_img);
                                    overlayImagePlus.setOverlay(blob_overlay);
                                    overlayImagePlus.show();
                                    /*uncropped_img.setRoi(zeroth_blob_roi);
                                    ImageProcessor zero_blob = uncropped_img.crop();
                                    ImagePlus zeroth_blob_imageplus = new ImagePlus("zeroth blob", zero_blob);
                                    zeroth_blob_imageplus.show();
                                    uncropped_img.setRoi(first_blob_roi);
                                    ImageProcessor first_blob = uncropped_img.crop();
                                    ImagePlus first_blob_imageplus = new ImagePlus("first blob", first_blob);
                                    first_blob_imageplus.show();*/
                                }
                            }
                        }
                    }
                }

                //ImagePlus overlayImagePlus = new ImagePlus("Overlay Image", first_seg_ip);
                //overlayImagePlus.show();
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