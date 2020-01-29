/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rendering;

import ij.IJ;
import org.jfree.chart.renderer.PaintScale;
import java.awt.Paint;
import java.awt.Color;

/**
 *
 * @author Janel
 */
public  class SpectrumPaintScale implements PaintScale {

        private static final float H1 = 0.1f;
        private static final float H2 = 1f;//1f;
        private final double lowerBound;
        private final double upperBound;

        public SpectrumPaintScale(double lowerBound, double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        public double getLowerBound() {
            return lowerBound;
        }

        @Override
        public double getUpperBound() {
            return upperBound;
        }

        @Override
        public Color getPaint(double value) {
            float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
            float scaledH = H1 + scaledValue * (H2 - H1);
          //   IJ.log("scaled:" +scaledH);
           //IJ.log("scaledV:" +scaledValue);
           //  IJ.log("h1:" +H1);
          // IJ.log("h2:" +H2);
            return Color.getHSBColor(scaledH, 1f, 1f);
        }
        
        
          
      public float getFPaint(double value) {
            float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
            float scaledH = H1 + scaledValue * (H2 - H1);
            //IJ.log("scaled:" +scaledH);
           // IJ.log("scaledV:" +scaledValue);
            
            return scaledValue ;
        }
      
      public Color getColor(float value){
          return Color.getHSBColor(value, 1f, 1f);
      }
        
       
    }

