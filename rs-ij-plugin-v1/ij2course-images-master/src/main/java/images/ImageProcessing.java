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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdTree;
import org.opencv.core.Mat;

import loci.formats.ImageReader;
import Services.service;
import ij.gui.GenericDialog;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;


public class ImageProcessing implements PlugInFilter {
    private static int central_wavelength = 700;
    private static int left_wavelength = 660;
    private static int right_wavelength = 740;

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

    //return Roi with respect to first order ROI(coordinate is the coordinate in first slice)
    private static Roi find_first_roi(Roi zeroth_psf_roi, Roi first_roi, Roi zeroth_roi, double[] x_parameters, double[] y_parameters) {
        double min_x_distance = x_parameters[2]*left_wavelength*left_wavelength + x_parameters[1]*left_wavelength + x_parameters[0];
        double min_y_distance = y_parameters[2]*central_wavelength*central_wavelength + y_parameters[1]*central_wavelength + y_parameters[0];
        double max_x_distance = x_parameters[2]*right_wavelength*right_wavelength + x_parameters[1]*right_wavelength + x_parameters[0];

        Roi first_psf_roi;
        //double max_y_distance = y_parameters[2]*710*710 + y_parameters[1]*710 + y_parameters[0];
        int first_psf_roi_y = Math.max((int)(zeroth_psf_roi.getBounds().y + min_y_distance-2-first_roi.getBounds().y+zeroth_roi.getBounds().y),0);
        if(first_psf_roi_y + zeroth_psf_roi.getBounds().height + 4 < first_roi.getBounds().height){
            int h = zeroth_psf_roi.getBounds().height+4;
            first_psf_roi = new Roi((int)(zeroth_psf_roi.getBounds().x + min_x_distance-first_roi.getBounds().x+zeroth_roi.getBounds().x), first_psf_roi_y, (int)(max_x_distance-min_x_distance)+zeroth_psf_roi.getBounds().width, h);
        }else{
            int h = first_roi.getBounds().height-first_psf_roi_y;
            first_psf_roi = new Roi((int)(zeroth_psf_roi.getBounds().x + min_x_distance-first_roi.getBounds().x+zeroth_roi.getBounds().x), first_psf_roi_y, (int)(max_x_distance-min_x_distance)+zeroth_psf_roi.getBounds().width, h);
        }
        
        return first_psf_roi;
    }

    private static void update_localization(ImageProcessor localization_ImageProcessor, int[] centroid){
        //if (localization_ImageProcessor.get(centroid[0],centroid[1])+4 <= 256){
        localization_ImageProcessor.putPixel(centroid[0],centroid[1],localization_ImageProcessor.get(centroid[0],centroid[1])+64);
        //}
        return;
    }

    //return Roi with respect to zeroth order ROI
    private static Roi find_psf(Point[][] pointArray, int x, int y) throws Exception{
        List<int[]> cur_pointset = new ArrayList<>();
        cur_pointset.add(new int[] { x, y });
        service.findSet(x,y,pointArray,cur_pointset);
        int[] boundary = service.findBoundary(cur_pointset);

        
        Roi roi = new Roi(Math.max(boundary[0]-1,0),Math.max(boundary[1]-1,0),boundary[2]-boundary[0]+3,boundary[3]-boundary[1]+3);
                                
        /**/
    
        return roi;                   
    }

    private static float[] find_psf_centroid(Roi roi, ImageProcessor slice) throws Exception{


        int edge = Math.min(roi.getBounds().width,roi.getBounds().height);

        double[] params;

        slice.setRoi(roi);
        ImageProcessor cropped = slice.crop();
        float[] cur_zeroth_centroid = new float[2];

        if (edge <= 3){
            params = service.fit2DGaussian(cropped);
            cur_zeroth_centroid[0] = (float) (roi.getBounds().x) + (float) params[0];
            cur_zeroth_centroid[1] = (float) (roi.getBounds().y) + (float) params[1];
        }
        else {
            //double[] start_pt = {(double)roi.getBounds().width/2,(double)roi.getBounds().height/2};
            try {
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
        
                double[] bestFit_X = GaussianCurveFitter.create().withMaxIterations(100).fit(column_value.toList()); // sometimes code will stuck on these 2 lines. idk why
                //System.out.println("fit x");
                double[] bestFit_Y = GaussianCurveFitter.create().withMaxIterations(100).fit(row_value.toList());
                //System.out.println("fit y");
                cur_zeroth_centroid[0] = (float) (roi.getBounds().x) + (float) bestFit_X[1];
                cur_zeroth_centroid[1] = (float) (roi.getBounds().y) + (float) bestFit_Y[1];
            }
            catch (TooManyIterationsException e) {
                float[] error_centroid = {-1,-1};
                return error_centroid;
            }
        }
        return cur_zeroth_centroid;
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

            // create empty image to hold localization results
            //ImageProcessor localization_ImageProcessor = new ByteProcessor(zeroth_roi.getBounds().width*10, zeroth_roi.getBounds().height*10);
            ImageProcessor localization_ImageProcessor = new ByteProcessor(first_roi.getBounds().width*10, first_roi.getBounds().height*10);

            ImagePlus overlayImagePlus = new ImagePlus("localization Image", localization_ImageProcessor);
            overlayImagePlus.show();

            //this is the result for assign color
            List<Float> centroid_x_distance_list = new ArrayList<>();
            //this is the corresponding zeorth order centroid
            List<Roi> zeroth_psf_roi_list = new ArrayList<>();

            //Main loop: go through all 30,000 frame
            for (int i = 0; i < 30000; i++) {

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
                int zeroth_threshold_value = (int) (zeroth_seg_ip.getStatistics().mean + 2.7 *zeroth_seg_ip.getStatistics().stdDev);
                zeroth_seg_ip.threshold(zeroth_threshold_value);

                //ImagePlus thresholded_zeroth = new ImagePlus("thresholded", zeroth_seg_ip);
                //thresholded_zeroth.show();

                int first_threshold_value = (int) (first_seg_ip.getStatistics().mean + 2.25 *first_seg_ip.getStatistics().stdDev);
                first_seg_ip.threshold(first_threshold_value);

                Point[][] zeroth_pointArray = new Point[roi_width][roi_height];
                Point[][] first_pointArray = new Point[first_width][first_height];

                //copy thresholded image to point Array for clustering the region
                service.make_pointArray(zeroth_pointArray, zeroth_seg_ip);
                service.make_pointArray(first_pointArray, first_seg_ip);

                
                first_seg_ip.setColor(Color.RED);

                //for each frame, iterate over zeroth roi and find psf
                for (int y = 0; y < roi_height; y++) {
                    for (int x = 0; x < roi_width; x++) {

                        if (zeroth_pointArray[x][y].visited == false) {
                            zeroth_pointArray[x][y].visited = true;
                            if (zeroth_pointArray[x][y].color == 255){

                                //get zeroth psf roi
                                Roi zeroth_psf_roi = find_psf(zeroth_pointArray,x,y);
                                
                                float[] zeroth_centroid = find_psf_centroid(zeroth_psf_roi, zeroth_slice);
                                
                                if (service.check_within_roi(zeroth_centroid,zeroth_psf_roi)){
                                    

                                    //find corresponding 1st roi window by calculating spectral distance with 650/750 wavelength
                                    Roi first_psf_window = find_first_roi(zeroth_psf_roi,first_roi,zeroth_roi,x_parameters,y_parameters);
                                    int[] to_localization_centroid = {(int)(zeroth_centroid[0]*10),(int)(zeroth_centroid[1]*10)};
                                    System.out.println("x:"+zeroth_centroid[0]*10+", y:"+zeroth_centroid[1]*10);
                                    //System.out.println("zeroth_blob_roi.getBounds().y:"+zeroth_blob_roi.getBounds().y);
                                    update_localization(localization_ImageProcessor,to_localization_centroid);
                                    

                                    for (int first_y = 0; first_y < first_psf_window.getBounds().height; first_y++){
                                        for (int first_x = 0; first_x < first_psf_window.getBounds().width; first_x++){
                                            //System.out.println("first x:"+(first_x + first_psf_window.getBounds().x)+", first y:"+(first_y + first_psf_window.getBounds().y));
                                            if (first_pointArray[first_x + first_psf_window.getBounds().x][first_y + first_psf_window.getBounds().y].visited == false) {
                                                first_pointArray[first_x + first_psf_window.getBounds().x][first_y + first_psf_window.getBounds().y].visited = true;
                                                if (first_pointArray[first_x + first_psf_window.getBounds().x][first_y + first_psf_window.getBounds().y].color == 255){
                                                    Roi first_psf_roi = find_psf(first_pointArray, first_x + first_psf_window.getBounds().x, first_y + first_psf_window.getBounds().y);
                                                    
                                                    float[] first_centroid = find_psf_centroid(first_psf_roi, first_slice);
                                                    if (service.check_within_roi(first_centroid,first_psf_roi)){
                                                        /*first_slice.drawLine((int)first_centroid[0]-3, (int)first_centroid[1], (int)first_centroid[0]+3, (int)first_centroid[1]);
                                                        first_slice.drawLine((int)first_centroid[0], (int)first_centroid[1]-3, (int)first_centroid[0], (int)first_centroid[1]+3);
                                                        first_slice.setRoi(first_psf_window);
                                                        ImageProcessor first_psf_roi_ip = first_slice.crop();
                                                        ImagePlus temp_ImagePlus = new ImagePlus("first", first_psf_roi_ip);
                                                        temp_ImagePlus.show();*/
                                                        zeroth_psf_roi_list.add(zeroth_psf_roi);
                                                        float centroid_x_distance = first_centroid[0] + first_roi.getBounds().x - zeroth_centroid[0] - zeroth_roi.getBounds().x;
                                                        centroid_x_distance_list.add(centroid_x_distance);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                        
                                    
                                }
                                
                                
                                //Roi first_blob_roi = find_first_roi(zeroth_blob_roi,first_roi,zeroth_roi,x_parameters,y_parameters);
                                
                                /*for (int y_first = 0; y_first < first_blob_roi.getBounds().height; y_first++){
                                    for (int x_first = 0; x_first < first_blob_roi.getBounds().width; x_first++){
                                        if (first_pointArray[x_first][y_first].visited == false) {
                                            first_pointArray[x_first][y_first].visited = true;
                                            if (first_pointArray[x_first][y_first].color == 255){
                                                find_psf(first_pointArray,x_first,y_first);
                                            }
                                        }
                                    }
                                }*/

                                /*if (zeroth_blob_roi.getBounds().width == zeroth_blob_roi.getBounds().height && zeroth_blob_roi.getBounds().width>=7){
                                    Overlay zero_blob_overlay = new Overlay();
                                    zero_blob_overlay.add(zeroth_blob_roi);
                                    Overlay first_blob_overlay = new Overlay();
                                    first_blob_overlay.add(first_blob_roi);
                                    zero_blob_overlay.setStrokeColor(Color.RED);
                                    first_blob_overlay.setStrokeColor(Color.RED);
                                    ImagePlus zero_overlayImagePlus = new ImagePlus("Overlay Image", zeroth_slice);
                                    zero_overlayImagePlus.setOverlay(zero_blob_overlay);
                                    zero_overlayImagePlus.show();
                                    ImagePlus first_overlayImagePlus = new ImagePlus("Overlay Image", first_slice);
                                    first_overlayImagePlus.setOverlay(first_blob_overlay);
                                    first_overlayImagePlus.show();
                                }*/
                            }
                        }
                    }
                }
                overlayImagePlus.updateAndDraw();
                System.out.println("i:"+i);
            }
            localization_ImageProcessor.convolve(kernel, kernelWidth, kernelHeight);
            overlayImagePlus.updateAndDraw();

            //show histogram of centroid_x_distance result
            HistogramDataset dataset = new HistogramDataset();
            dataset.setType(HistogramType.FREQUENCY);
            double[] double_x_distance_Array = new double[centroid_x_distance_list.size()];

            for (int i = 0; i < centroid_x_distance_list.size(); i++) {
                double_x_distance_Array[i] = centroid_x_distance_list.get(i).doubleValue();
            }
            dataset.addSeries("Histogram", double_x_distance_Array, 400);

            JFreeChart chart = ChartFactory.createHistogram(
                "Float Histogram",
                "Value",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );


            JFrame frame = new JFrame("Distance Histogram");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            frame.add(new ChartPanel(chart));

            frame.pack();
            frame.setVisible(true);

            reader.close();
            return matched_centroids;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}