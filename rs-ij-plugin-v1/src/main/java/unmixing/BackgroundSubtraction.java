/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unmixing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 *
 * @author Brian T. Soetikno
 */
public class BackgroundSubtraction {
    public ImagePlus process(ImagePlus imp, ImagePlus background) {
        int numSlices = imp.getNSlices();
        int width = imp.getWidth();
        int height = imp.getHeight();
         IJ.showStatus("NFrames: "+numSlices);
        ImageStack inStack = imp.getStack();
        ImageStack bgInStack = background.getStack();
        int numSlices2=background.getNSlices();
         IJ.showStatus("#Frames Bk: "+numSlices2);
        
        ImageStack outStack = new ImageStack(width, height, inStack.getSize());
        
        
        
        // Holds all the pixel data relative to the current frame
       
             ImageProcessor ip2 = bgInStack.getProcessor(1);
          
            FloatProcessor bgFp = (FloatProcessor) ip2.convertToFloat();
        
        // Begin subtraction calculation
        for (int currentSlice=1; currentSlice <= numSlices; currentSlice++) {
            int index = imp.getStackIndex(1, currentSlice, 1); // channel, slice, frame
            ImageProcessor ip = inStack.getProcessor(index);
            FloatProcessor fp = (FloatProcessor)ip.convertToFloat();
           
            float[] inPix = (float[])fp.getPixels();
            float[] bgPix = (float[])bgFp.getPixels();
            
            float[] outPix = new float[inPix.length];
            for (int k = 0; k < inPix.length; k++) {
                outPix[k] = Math.max(inPix[k] - bgPix[k], 0.0f);
            }
            
            FloatProcessor outFp = new FloatProcessor(width, height, outPix);
            outStack.addSlice("" + currentSlice, (ImageProcessor)outFp, index);
            outStack.deleteSlice(index); // ??
        }
        
        ImagePlus result = new ImagePlus("Background-Subtracted: " + imp.getTitle(), outStack);
        return result;
    }
}
