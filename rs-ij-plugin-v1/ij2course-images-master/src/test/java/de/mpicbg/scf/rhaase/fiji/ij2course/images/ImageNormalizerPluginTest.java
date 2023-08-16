package de.mpicbg.scf.rhaase.fiji.ij2course.images;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageStatistics;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: June 2017
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
public class ImageNormalizerPluginTest {

    private double tolerance = 0.0001;

    @Test
    public void testNormalisation() {
        // create random image
        ImagePlus testImage = NewImage.createByteImage("test", 1000,1000,1, NewImage.FILL_RANDOM);

        // normalize it
        long timeStamp = System.currentTimeMillis();
        ImagePlus resultImage = ImageNormalizerPlugin.normalize(testImage);
        System.out.println("Normalisation took " + (System.currentTimeMillis() - timeStamp) + " msec");

        // check if normalisation has reasonable result statistics
        ImageStatistics statsBeforeTest = testImage.getStatistics();
        ImageStatistics statsAfterTest = resultImage.getStatistics();

        assertEquals(0.0, statsAfterTest.min, tolerance);
        assertEquals(1.0, statsAfterTest.max, tolerance);
        assertEquals((statsBeforeTest.mean - statsBeforeTest.min) / (statsBeforeTest.max - statsBeforeTest.min),
                statsAfterTest.mean, tolerance);
    }

    @Test
    public void testIfDimensionsMatch() {
        ImagePlus testImage = IJ.openImage("src/main/resources/mitosis.tif");
        // normalize it
        long timeStamp = System.currentTimeMillis();
        ImagePlus resultImage = ImageNormalizerPlugin.normalize(testImage);
        System.out.println("Normalisation took " + (System.currentTimeMillis() - timeStamp) + " msec");

        // check result image dimensions
        assertEquals(testImage.getNSlices(), resultImage.getNSlices());
        assertEquals(testImage.getNChannels(), resultImage.getNChannels());
        assertEquals(testImage.getNFrames(), resultImage.getNFrames());
    }

}