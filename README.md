# RainbowSTORM
RainbowSTORM: An open-source ImageJ plug-in for spectroscopic single-molecule localization microscopy (sSMLM) data analysis and image reconstruction.

# About RainbowSTORM

RainbowSTORM is an open-source, user-friendly ImageJ/Fiji plug-in for end-to-end spectroscopic analysis and visualization of sSMLM data. RainbowSTORM provides a variety of spectroscopic processing and post-processing methods that allow users to calibrate, preview, and quantitatively analyze emission spectra acquired using a wide range of sSMLM systems and fluorescent molecules.

# Features
- **User-Friendly:** RainbowSTORM provides an easy to use framework and a variety of automatic processing options for spectroscopic analysis.

- **Comprehensive Spectroscopic Calibration:** RainbowSTORM can be used to calibrate both grating-based and prism-based sSMLM systems

- **Spectroscopic Data Analysis:** RainbowSTORM provides analysis for 2D sSMLM images as well as 3D sSMLM images acquired using the astigmatism method. The RainbowSTORM sSMLM analysis platform relates each localization to its corresponding spectral image. RainbowSTORM's background subtraction module allows for thresholds to be automatically generated or user-defined. by default, RainbowSTORM's spectra processing module is optimized for processing far-red dyes which are commonly used for sSMLM, however, users can update the spectrum window to process a range of super-resolution dyes. Results of sSMLM analysis can be previewed and adjusted before analyzing the full dataset. Spectral images that overlap in space can be automatically removed. Running the sSMLM analysis extracts the line spectra from the spectral images and calibrates them using the calibration information. RainbowSTORM also calculates a range of spectral fields including spectral centroids, spectrum width, spectral photon counts, and spectral uncertainty.

- **Data Visualization:** The spectral images, line plot of the average line spectra, and scatter plots of the centroids versus the spectral photon counts are displayed on the visualization screen. RainbowSTORM also allows users to plot histograms of the spatial and spectral fields.

- **Spectroscopic Super-Resolution Image Rendering:** RainbowSTORM uses the spectral centroids and the coordinates of each localization to render Pseudo-colored super-resolution reconstructions. 

- **sSMLM Post-Processing:** After spectroscopic analysis has been performed, users can perform a variety of post-processing steps including drift correction, image quality analysis using Fourier correlation analysis, and generation of multi-colored super-resolution images with each color channel classified based on the spectral centroids. The histograms of the spatial and spectral fields can be visualized and used to select specific segments localizations or remove outliers. Application of the post-processing filters updates the Pseudo-colored reconstructions, spectral images, line plot of the average spectra as well as the centroid versus photon scatter plot. The original sSMLM data can be reloaded using RainbowSTORM's reset option.

- **sSMLM Summary:** RainbowSTORM keeps track of all post-processing methods which have been applied. Also, the average spectral photon counts, average spectral uncertainties and the number of localizations displayed are shown on the visualization screen.


# Getting Started
RainbowSTORM requires: [ImageJ](https://imagej.nih.gov/ij/download.html) or [FIJI](https://imagej.net/Fiji/Downloads) and [ThunderSTORM](https://github.com/zitmen/thunderstorm/wiki/Downloads). For ImageJ installations the [Bioformats plugin](https://www.openmicroscopy.org/bio-formats/downloads/) is also needed to load the provided test datasets.  Test data is available [here](https://github.com/FOIL-NU/RainbowSTORM/tree/master/rs-ij-plugin-v1/RainbowSTORM%20Test) and on our [dropbox](https://www.dropbox.com/sh/44uihyzrxh93jh8/AAAwjqpeNVz6fXnARH45s7wTa?dl=0).

Locate the latest RainbowSTORM release in the [here](https://github.com/FOIL-NU/RainbowSTORM/tree/master/rs-ij-plugin-v1/Current%20Release).

Install the RainbowSTORM plugin (Rainbow_STORM.jar)by copying the file into the Plugins subfolder of your ImageJ installation (e.g. “C:\Program Files\ImageJ\plugins”).  Verify the successful installation of RainbowSTORM by restarting ImageJ and locating RainbowSTORM under the Plugins menu.


# How to Cite
If you use RainbowSTORM for data analysis, please cite our paper:
- Davis, J. L., Soetikno, B., Song, K., Zhang, Y., Sun, C.; Zhang, H. F. RainbowSTORM: An open-source ImageJ plugin for spectroscopic single-molecule localization microscopy (sSMLM) data analysis and image reconstruction. bioRxiv 2020 [link](https://doi.org/10.1101/2020.03.10.986018)
