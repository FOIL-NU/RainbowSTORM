package de.mpicbg.scf.rhaase.fiji.ij2course.images;

import net.imglib2.Cursor;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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

@Plugin(type = Command.class, menuPath = "Plugins>Filtering>Normalisation (IJ2)")
public class ImageNormalizerIJ2Plugin implements Command {

    @Parameter
    Img input;

    @Parameter(type = ItemIO.OUTPUT)
    Img output;

    @Override
    public void run() {
        output = normalize(input);
    }

    static <T extends RealType<T>> Img<FloatType> normalize(Img<T> input) {

        // determine min/max
        T minPixel = input.firstElement().createVariable();
        T maxPixel = input.firstElement().createVariable();

        ComputeMinMax<T> computer = new ComputeMinMax<>(input, minPixel, maxPixel);
        computer.process();

        float minPixelValue = minPixel.getRealFloat();
        float maxPixelValue = maxPixel.getRealFloat();

        // get memory for output image
        long[] dimensions = new long[input.numDimensions()];
        input.dimensions(dimensions);
        Img<FloatType> output = ArrayImgs.floats(dimensions);

        // normalize all pixels
        Cursor<T> inputCursor = Views.flatIterable(input).cursor();
        Cursor<FloatType> outputCursor = output.cursor();
        while (inputCursor.hasNext() && outputCursor.hasNext()) {
            float value = inputCursor.next().getRealFloat();
            float normalisedValue = (value - minPixelValue) / (maxPixelValue - minPixelValue);

            outputCursor.next().set(normalisedValue);
        }

        return output;
    }

}
