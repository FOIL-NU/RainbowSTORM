package images;
import ij.IJ;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.ImagePlus;
import ij.gui.Overlay;
import java.awt.Color;
import ij.gui.GenericDialog;
import com.opencsv.CSVWriter;

import Services.service;

import java.io.FileWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.List;
import loci.plugins.BF;
import net.imglib2.algorithm.localization.LevenbergMarquardtSolver;
import net.imglib2.algorithm.localization.LocalizationUtils;
import net.imglib2.algorithm.localization.MLGaussianEstimator;
import net.imglib2.algorithm.localization.Observation;
import net.imglib2.img.ImagePlusAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AxialCalibration implements PlugInFilter {

    @Override
    public void run(ImageProcessor arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    public static void calibrate(String filePath) throws Exception{
        ImagePlus imagePlus = readND2(filePath);

        int numFrames = imagePlus.getStackSize();

        System.out.println("NumFrames: "+numFrames);
            // Average all the frames
        ImageProcessor tempImageProcessor = imagePlus.getStack().getProcessor(100).duplicate(); // Initialize with the first frame
        float[] kernel = {
            0.0625f, 0.125f, 0.0625f ,
            0.125f , 0.25f , 0.125f ,
            0.0625f, 0.125f, 0.0625f 
        };
        int kernelWidth = 3;
        int kernelHeight = 3;

        FloatProcessor fp = tempImageProcessor.convertToFloatProcessor();

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
                if (pixelValue < 25) {
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

        List<int[]> calibration_area = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width/3; x++) {
                if (pointArray[x][y].visited == false) {
                    pointArray[x][y].visited = true;
                    if (pointArray[x][y].color == 255){
                        List<int[]> cur_pointset = new ArrayList<>();
                        //cur_pointset.add(new int[] { x, y });
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
                        
                        
                        if (minX > 2 && minY > height/5 && maxX < width/3-3 && maxY < 4*height/5 ){
                            int[] a = {minX-6,minY-6,maxX+6,maxY+6};
                            calibration_area.add(a);
                            
                        }
                    }
                }
            }
        }
        
        int numRois = calibration_area.size();
        Overlay overlay = new Overlay();
        int roiNumber = 1;
        String[] ROI_selectlist = new String[numRois];
        List<Roi> roiList = new ArrayList<>();
        for (int i = 0; i < numRois; i++) {
            int[] cur_area = calibration_area.get(i);
            Roi roi = new Roi(cur_area[0],cur_area[1],cur_area[2]-cur_area[0],cur_area[3]-cur_area[1]);
            roiList.add(roi);
            ROI_selectlist[i] = Integer.toString(i+1);
            roi.setStrokeColor(Color.YELLOW); // Yellow color for the ROI
            overlay.add(roi);
            String text = Integer.toString(roiNumber);
            TextRoi textRoi = new TextRoi(cur_area[0]-10, cur_area[1]-5, text);
            textRoi.setStrokeColor(Color.YELLOW); // Set the color of the number
            overlay.add(textRoi);
            roiNumber += 1;
        }
        
        ImageProcessor improcessor = imagePlus.getStack().getProcessor(100).duplicate();
        ImagePlus out = new ImagePlus("Select Zeroth Order ROI", improcessor);
        out.setOverlay(overlay);
        out.show();
        IJ.run(out, "Enhance Contrast", "saturated=1.5");

        // Update the display
        out.updateAndDraw();

        GenericDialog gd = new GenericDialog("Select ROI");
        gd.addChoice("Select one ROI:", ROI_selectlist, "1");
        gd.showDialog();

        if (gd.wasCanceled()) {
            System.out.println("User canceled ROI selection.");
            return;
        }

        String selectedROI = gd.getNextChoice();

        // Perform further processing with the selected ROI
        Roi roi_Zeroth = roiList.get(Integer.parseInt(selectedROI)-1);
        

        int roi_x_center_Zeroth = (int) (roi_Zeroth.getBounds().x+roi_Zeroth.getBounds().width/2.0);
        int roi_y_center_Zeroth = (int) (roi_Zeroth.getBounds().y+roi_Zeroth.getBounds().height/2.0);
        
        int spectral_dist_x = 950;
        int spectral_dist_y = 19;

        int roi_x_center_First = roi_x_center_Zeroth+spectral_dist_x;
        int roi_y_center_First = roi_y_center_Zeroth+spectral_dist_y;
        if (roi_x_center_First+12 >= width || roi_y_center_First-5 <0 || roi_y_center_First+5>height){
            GenericDialog gd_out = new GenericDialog("Warning");
            gd_out.addMessage("First order ROI is out of scope. Please start over.");
            gd_out.showDialog();
            return;
        }

        List<int[]> cur_pointset = new ArrayList<>();
        service.findSet(roi_x_center_First,roi_y_center_First,pointArray,cur_pointset);
        int minX_First = Integer.MAX_VALUE;
        int maxX_First = Integer.MIN_VALUE;
        int minY_First = Integer.MAX_VALUE;
        int maxY_First = Integer.MIN_VALUE;

        for (int[] point : cur_pointset) {
            if (point[0] < minX_First) {minX_First = point[0];}
            if (point[1] < minY_First) {minY_First = point[1];}
            if (point[0] > maxX_First) {maxX_First = point[0];}
            if (point[1] > maxY_First) {maxY_First = point[1];}
        }
        
        Roi roi_First = new Roi(minX_First-8,(int)((maxY_First+minY_First-roi_Zeroth.getBounds().height)/2),maxX_First-minX_First+17,roi_Zeroth.getBounds().height);
        
        Overlay overlay_zero_first = new Overlay();
        roi_First.setStrokeColor(Color.YELLOW);
        roi_Zeroth.setStrokeColor(Color.YELLOW);
        overlay_zero_first.add(roi_Zeroth);
        overlay_zero_first.add(roi_First);

        //ImageProcessor improcessor_zero_first = imagePlus.getStack().getProcessor(100).duplicate();
        ImagePlus out_zero_first = new ImagePlus("Zeroth and First order ROI", improcessor);
        out_zero_first.setOverlay(overlay_zero_first);
        out_zero_first.show();

        double[] Zeroth_FWHM = getFWHM(roi_Zeroth, imagePlus, numFrames);
        double Zeroth_min_FWHM = findMinValue(Zeroth_FWHM);
        for (int i = 0; i<Zeroth_FWHM.length;i++){
            if (Zeroth_FWHM[i] > 2.4*Zeroth_min_FWHM){
                Zeroth_FWHM[i] = 0;
            }
        }

        double[] First_FWHM = getFWHM(roi_First, imagePlus, numFrames);
        double First_min_FWHM = findMinValue(First_FWHM);
        for (int i = 0; i<First_FWHM.length;i++){
            if (First_FWHM[i] > 2.4*First_min_FWHM){
                First_FWHM[i] = 0;
            }
        }
        
        plotFWHM(numFrames,Zeroth_FWHM,First_FWHM);
        
    }

    private static double[] getFWHM(Roi roi, ImagePlus imagePlus, int numFrames){
        int selected_x = roi.getBounds().x;
        int selected_y = roi.getBounds().y;
        int selected_width = roi.getBounds().width;
        int selected_height = roi.getBounds().height;
        double[] FWHM = new double[numFrames];
        //int start_frame = (int) (numFrames*0.15);
        //int end_frame = (int) (numFrames*0.7);
        for (int i=1; i<=numFrames; i++) {
            WeightedObservedPoints column_value = new WeightedObservedPoints();
            for (int row_num=selected_y; row_num<selected_y+selected_height; row_num++) {
                int row_sum = 0;
                for (int pixel_x=selected_x; pixel_x<selected_x+selected_width; pixel_x++) {
                    
                    row_sum += imagePlus.getStack().getProcessor(i).get(pixel_x,row_num);
                }
                column_value.add(row_num-selected_y,row_sum);
            }
            GaussianCurveFitter fitter = GaussianCurveFitter.create();
            double[] bestFit = fitter.fit(column_value.toList());
            FWHM[i-1] = bestFit[2];
        }
        return FWHM;
    }

    public static int[] find_start_end(int numFrames, double[] FWHM){
        int[] start_end = new int[2];
        for (int i = 0; i < 100; i++) {
            if (FWHM[i] == 0 && FWHM[i+1] > 0){
                start_end[0] = i+1;
            }
            if (FWHM[numFrames-1-i] == 0 && FWHM[numFrames-2-i] > 0 ){
                start_end[1] = numFrames-2-i;
            }
        }
        return start_end;
    }

    public static double findMinValue(double[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty or null.");
        }

        double minValue = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }

        return minValue;
    }

    private static void plotFWHM(int numFrames, double[] Zeroth_FWHM, double[] First_FWHM){
        XYSeries series_zeroth = new XYSeries("Zeroth Order FWHM");
        XYSeries series_first = new XYSeries("First Order FWHM");
        XYSeries parabolaSeries_zeroth = new XYSeries("Zeroth Order Parabola");
        XYSeries parabolaSeries_first = new XYSeries("First Order Parabola");
        XYSeries ratio_zero_first = new XYSeries("Ratio");
        WeightedObservedPoints zeroth_obs = new WeightedObservedPoints();
        WeightedObservedPoints first_obs = new WeightedObservedPoints();
        //int start_frame = (int) (numFrames*0.15);
        //int end_frame = (int) (numFrames*0.7);
        int[] start_end_zeroth = find_start_end(numFrames, Zeroth_FWHM);
        int[] start_end_first = find_start_end(numFrames, First_FWHM);
        for (int i = 0; i < numFrames; i++) {
            series_zeroth.add(i, 2.335*Zeroth_FWHM[i]);
            series_first.add(i, 2.335*First_FWHM[i]);

            if (i >= start_end_zeroth[0] && i<= start_end_zeroth[1]){
                zeroth_obs.add(i,2.335*Zeroth_FWHM[i]);
            }

            if (i >= start_end_first[0] && i<= start_end_first[1]){
                first_obs.add(i,2.335*First_FWHM[i]);
            }
        }
        
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] zeroth_coefficients = fitter.fit(zeroth_obs.toList());
        double[] first_coefficients = fitter.fit(first_obs.toList());
        

        for (int i = start_end_zeroth[0]; i<= start_end_zeroth[1]; i++){
            //double y = coefficients[4]* i* i* i* i + coefficients[3]* i* i* i + coefficients[2] * i * i + coefficients[1] * i + coefficients[0];
            double y = zeroth_coefficients[2] * i * i + zeroth_coefficients[1] * i + zeroth_coefficients[0];
            parabolaSeries_zeroth.add(i, y);
        }
        double zeroth_min = -zeroth_coefficients[1] / (2 * zeroth_coefficients[2]);

        for (int i = start_end_first[0]; i<= start_end_first[1]; i++){
            //double y = coefficients[4]* i* i* i* i + coefficients[3]* i* i* i + coefficients[2] * i * i + coefficients[1] * i + coefficients[0];
            double y = first_coefficients[2] * i * i + first_coefficients[1] * i + first_coefficients[0];
            parabolaSeries_first.add(i, y);
        }
        double first_min = -first_coefficients[1] / (2 * first_coefficients[2]);

        for (int i = start_end_zeroth[0]; i <= start_end_first[1];i++) {
            double ratio = (first_coefficients[2] * i * i + first_coefficients[1] * i + first_coefficients[0])/(zeroth_coefficients[2] * i * i + zeroth_coefficients[1] * i + zeroth_coefficients[0]);
            ratio_zero_first.add(i,ratio);
        }

        System.out.println("zeroth_min:"+zeroth_min+"; first_min:"+first_min);
        XYSeriesCollection dataset_zeroth = new XYSeriesCollection();
        dataset_zeroth.addSeries(series_zeroth);
        XYSeriesCollection dataset_first = new XYSeriesCollection();
        dataset_first.addSeries(series_first);
        XYSeriesCollection parabolaset_zeroth = new XYSeriesCollection();
        parabolaset_zeroth.addSeries(parabolaSeries_zeroth);
        XYSeriesCollection parabolaset_first = new XYSeriesCollection();
        parabolaset_first.addSeries(parabolaSeries_first);


        XYSeriesCollection raCollection_first_zero = new XYSeriesCollection();
        raCollection_first_zero.addSeries(ratio_zero_first);


        JFreeChart parabola_zeroth = ChartFactory.createXYLineChart(
            "FWHM VS Z", // Chart title
            "Z", // X-axis label
            "FWHM", // Y-axis label
            parabolaset_zeroth // Dataset
        );

        JFreeChart ratio_Chart = ChartFactory.createXYLineChart(
            "Ratio VS Z", // Chart title
            "Z", // X-axis label
            "First Order/Zeroth Order", // Y-axis label
            raCollection_first_zero // Dataset
        );

        XYPlot plot = parabola_zeroth.getXYPlot();
        plot.setDataset(1, dataset_zeroth);
        plot.setRenderer(1, new XYLineAndShapeRenderer());
        plot.setDataset(2, parabolaset_first);
        plot.setDataset(3, dataset_first);
        plot.setRenderer(3, new XYLineAndShapeRenderer());

        JFrame frame = new JFrame("FWHM VS Z");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new ChartPanel(parabola_zeroth));

        frame.pack();
        frame.setVisible(true);
        
        JFrame frame_ratio = new JFrame("Ratio VS Z");
        frame_ratio.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame_ratio.add(new ChartPanel(ratio_Chart));

        frame_ratio.pack();
        frame_ratio.setVisible(true);
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

    @Override
    public int setup(String arg0, ImagePlus arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setup'");
    }
    
}
