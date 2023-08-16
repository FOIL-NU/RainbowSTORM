package de.mpicbg.scf.rhaase.fiji.ij2course.images;

import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;
import loci.plugins.LociImporter;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import ij.measure.ResultsTable;
import loci.plugins.BF;

import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataStore;
import loci.formats.services.OMEXMLService;


/**
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, 
 *         rhaase@mpi-cbg.de
 * Date: May 2017
 * 
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics, 
 *                Dresden, Germany
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in the 
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its 
 *      contributors may be used to endorse or promote products derived from 
 *      this software without specific prior written permission.
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
 *
 */ 
public class ImagesMain {
    

    

    public static <T extends RealType<T>> void main(String... args) throws Exception {

        
        OpenDialog od = new OpenDialog("Open ND2 File", null);
        String directory = od.getDirectory();
        String fileName = od.getFileName();

        // Check if the user selected a file or canceled the dialog
        if (fileName == null) {
            IJ.log("File selection canceled.");
            return;
        }

        // Combine the directory and filename to get the full path
        String filePath = directory + fileName;

        // Use Bio-Formats to open the ND2 file
        //Opener opener = new Opener();
        //ImagePlus image = openND2Image(filePath);

        // Read the ND2 file using Bio-Formats
        //ImagePlus imagePlus = readND2(filePath);

        //ImagePlus Result = SpectralCalibration.calibrate(filePath);
        //Result.show();
        AxialCalibration.calibrate(filePath);
        /*
        testImage.getProcessor().threshold(170);

        IJ.setAutoThreshold(testImage, "Default");
        IJ.run(testImage, "Threshold", "");
        IJ.run(testImage, "Convert to Mask", "");

        int options = ParticleAnalyzer.SHOW_OUTLINES | ParticleAnalyzer.CLEAR_WORKSHEET | ParticleAnalyzer.ADD_TO_MANAGER;
        int measurements = ParticleAnalyzer.CENTROID;
        ParticleAnalyzer.setRoiManager(null);
        ResultsTable resultsTable = new ResultsTable();
        ParticleAnalyzer analyzer = new ParticleAnalyzer(options, measurements, resultsTable, 0.0, Double.POSITIVE_INFINITY);
        analyzer.analyze(testImage);

        // Step 3: Calculate the centroid for each connected component (dot)
        int numDots = resultsTable.getCounter();
        float[] centroidX = resultsTable.getColumn(ResultsTable.X_CENTROID);
        float[] centroidY = resultsTable.getColumn(ResultsTable.Y_CENTROID);

        // Display the centroid of each dot
        for (int i = 0; i < numDots; i++) {
            float x = centroidX[i];
            float y = centroidY[i];
            System.out.println("Dot " + (i + 1) + " Centroid: (" + x + ", " + y + ")");
        }


        //// inside ImageJ we could run
        // IJ.run(testImage, "Normalisation", "");
        // ImagePlus resultImage = IJ.getImage();
        //// but from within the IDE we need to do that:

        //ImagePlus resultImage = ImageNormalizerPlugin.normalize(testImage);
        //resultImage.setTitle("Result using IJ1");
        //resultImage.show(); */
    }
}
