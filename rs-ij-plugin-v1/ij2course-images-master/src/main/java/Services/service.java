package Services;

import java.util.List;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import images.Point;
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

		LevenbergMarquardtSolver.solve(data.X, params, data.I, new Gaussian(), 1e-3, 1e-1, 300);

		return params;
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
        }
  
        return;
    }
    
}
