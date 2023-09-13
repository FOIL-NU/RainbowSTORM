package Services;

import java.util.List;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.process.ByteProcessor;
import images.Point;
import ij.gui.Roi;
import loci.plugins.BF;
import loci.formats.ImageReader;
import net.imglib2.algorithm.localization.Gaussian;
import net.imglib2.algorithm.localization.LevenbergMarquardtSolver;
import net.imglib2.algorithm.localization.LocalizationUtils;
import net.imglib2.algorithm.localization.MLGaussianEstimator;
import net.imglib2.algorithm.localization.Observation;
import net.imglib2.img.ImagePlusAdapter;

public class service {

    public static double[] fit2DGaussian(ImageProcessor ip) throws Exception {
		long[] center = new long[] { (long)ip.getWidth() / 2, (long)ip.getHeight() / 2 };
		net.imglib2.Point cpoint = new net.imglib2.Point(center);
		Observation data = LocalizationUtils.gatherObservationData(ImagePlusAdapter.wrapShort(new ImagePlus("", ip)), cpoint, center);

		double[] params = new MLGaussianEstimator(2.0, 2).initializeFit(cpoint, data);
        //double[] params = {(double) center[0], (double) center[1]};
		LevenbergMarquardtSolver.solve(data.X, params, data.I, new Gaussian(), 1e-3, 1e-2, 300);
		return params;
	}

    public static ImagePlus readND2(String filePath) {
        try {
            // Use Bio-Formats to read the ND2 file
            return BF.openImagePlus(filePath)[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean check_within_roi(float[] centroid, Roi roi){
        if (centroid[0] >= roi.getBounds().x && centroid[0] < roi.getBounds().x+roi.getBounds().width && centroid[1] >= roi.getBounds().y && centroid[1] < roi.getBounds().y+roi.getBounds().height){
            return true;
        }
        else{
            return false;
        }
    }


    public static ImagePlus read_first_slice_ND2(ImageReader reader,int frame_number) {
        try {
            // Use Bio-Formats to read the ND2 file
            // Specify the series index for your ND2 dataset (usually 0 for the default series)
            int seriesIndex = 0;

            // Specify the channel index, slice index, and time point for the frame you want to read
            int channelIndex = 0;
            int sliceIndex = 0;
            int timePointIndex = 0;

            // Set the series, channel, slice, and time point
            //reader.setSeries(seriesIndex);
            

            // Read the pixel data for the specified frame
            byte[] pixels = reader.openBytes(frame_number);

            // Create an ImageProcessor from the pixel data
            int width = (int) reader.getSizeX();
            int height = (int) reader.getSizeY();
            //System.out.println("width:"+width);
            //System.out.println("height:"+height);
            //System.out.println("pixels length:"+pixels.length);
            short[] pixelValues = new short[width * height];

            // Populate the pixelValues array from the byteArray (assuming each pixel is represented by 2 bytes)
            for (int i = 0; i < width * height; i++) {
                int index = i * 2;
                short pixelValue = (short) ((pixels[index] & 0xFF) | ((pixels[index + 1] & 0xFF) << 8));
                pixelValues[i] = pixelValue;
            }
            ShortProcessor ip = new ShortProcessor(width, height, pixelValues,null);

            // Create an ImagePlus from the ImageProcessor
            ImagePlus imagePlus = new ImagePlus("Select ROI", ip);
            return imagePlus;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void make_pointArray(Point[][] pointArray, ImageProcessor seg_ip){
        for (int y = 0; y < seg_ip.getHeight(); y++) {
            for (int x = 0; x < seg_ip.getWidth(); x++) {
                int pixelValue = seg_ip.get(x, y);
                pointArray[x][y] = new Point(pixelValue);
            }
        }
    }

    //return: minX, minY, maxX, maxY
    public static int[] findBoundary(List<int[]> cur_pointset){
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
        int[] boundary = {minX,minY,maxX,maxY};
        return boundary;
    }

    public static void findSet(int x, int y, Point[][] points, List<int[]> pointSet) {
        // Get the point at the current coordinates
        if (x>=1){
            Point currentPoint_left = points[x-1][y];
            if (currentPoint_left.color == 255 && !currentPoint_left.visited) {
                // Mark the current point as visited
                currentPoint_left.visited = true;
    
                // Add the current point to the pointSet
                pointSet.add(new int[] { x-1, y });
                // Recursively check neighboring points
                findSet(x - 1, y, points, pointSet); // left
            }
            if (y>=1) {
                Point currentPoint_left_down = points[x-1][y-1];
                if (currentPoint_left_down.color == 255 && !currentPoint_left_down.visited) {
                    // Mark the current point as visited
                    currentPoint_left_down.visited = true;
        
                    // Add the current point to the pointSet
                    pointSet.add(new int[] { x-1, y-1 });
                    // Recursively check neighboring points
                    findSet(x-1, y-1, points, pointSet); // left
                }
            }
        }
        if (y>=1){
            Point currentPoint_down = points[x][y-1];
            if (currentPoint_down.color == 255 && !currentPoint_down.visited) {
                // Mark the current point as visited
                currentPoint_down.visited = true;
    
                // Add the current point to the pointSet
                pointSet.add(new int[] { x, y-1 });
                // Recursively check neighboring points
                findSet(x, y-1, points, pointSet); // up
            }
            if (x<points.length-1) {
                Point currentPoint_right_down = points[x+1][y-1];
                if (currentPoint_right_down.color == 255 && !currentPoint_right_down.visited) {
                    // Mark the current point as visited
                    currentPoint_right_down.visited = true;
        
                    // Add the current point to the pointSet
                    pointSet.add(new int[] { x+1, y-1 });
                    // Recursively check neighboring points
                    findSet(x+1, y-1, points, pointSet); // left
                }
            }
        }
        if (x<points.length-1){
            Point currentPoint_right = points[x+1][y];
            if (currentPoint_right.color == 255 && !currentPoint_right.visited) {
                // Mark the current point as visited
                currentPoint_right.visited = true;
    
                // Add the current point to the pointSet
                pointSet.add(new int[] { x+1, y });
                // Recursively check neighboring points
                findSet(x + 1, y, points, pointSet); // right
            }
            if (y<points[0].length-1) {
                Point currentPoint_right_up = points[x+1][y+1];
                if (currentPoint_right_up.color == 255 && !currentPoint_right_up.visited) {
                    // Mark the current point as visited
                    currentPoint_right_up.visited = true;
        
                    // Add the current point to the pointSet
                    pointSet.add(new int[] { x+1, y+1 });
                    // Recursively check neighboring points
                    findSet(x+1, y+1, points, pointSet); // left
                }
            }
        }
        if (y<points[0].length-1){
            Point currentPoint_up = points[x][y+1];
            if (currentPoint_up.color == 255 && !currentPoint_up.visited) {
                // Mark the current point as visited
                currentPoint_up.visited = true;
    
                // Add the current point to the pointSet
                pointSet.add(new int[] { x, y+1 });
                // Recursively check neighboring points
                findSet(x , y+1, points, pointSet); // bottom
            }
            if (x>=1) {
                Point currentPoint_left_up = points[x-1][y+1];
                if (currentPoint_left_up.color == 255 && !currentPoint_left_up.visited) {
                    // Mark the current point as visited
                    currentPoint_left_up.visited = true;
        
                    // Add the current point to the pointSet
                    pointSet.add(new int[] { x-1, y+1 });
                    // Recursively check neighboring points
                    findSet(x-1, y+1, points, pointSet); // left
                }
            }
        }
  
        return;
    }
    
}
