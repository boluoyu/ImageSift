/*
 * Copyright 2016 Uncharted Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.uncharted.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by chrisdickson on 2016-04-09.
 */
public class StableDistLSH {
  private final static Logger logger = LoggerFactory.getLogger(StableDistLSH.class);

  long randSeed;
  int numPlanes;
  int numDim;
  double width;
  double[][] hyperplanes = null;
  double[] offsets = null;

  /**
   *
   * @param numPlanes -- number of planes to use (higher == more accurate, but takes more computations)
   * @param numDim -- number of dimensions of input feature vectors for this LSH implmentation
   * @param width -- tunes the size of each LSH bin (higher == larger/coarser LSH bins)
   * @param randNumSeed -- random number seed to use for generating LSH matrix
   */
  public StableDistLSH(int numPlanes, int numDim, double width, long randNumSeed) {
    this.numPlanes = numPlanes;
    this.numDim = numDim;
    this.width = width;
    this.randSeed = randNumSeed;
    if (hyperplanes != null) {
      logger.warn("Hyperplanes matrix may be already initialized?");
    }

    //---- init hyperplanes matrix
    Random rand = new Random(randSeed);
    hyperplanes = new double[numPlanes][numDim];
    offsets = new double[numPlanes];

    for (int p=0; p<numPlanes; p++) {
      offsets[p] = rand.nextDouble() * width; // init offsets as uniform rand number btwn 0 and width
      for (int d=0; d<numDim; d++) {
        // populate hyperplanes matrix with random gaussian values (rand normal dist)
        double randVal = rand.nextGaussian();
        hyperplanes[p][d] = randVal;
      }
    }
  }



  /**
   * calcLSH -- calculate LSH result for a given feature vector
   *
   * @param featureVec -- input feature vector
   * @return byte[] LSH result
   */
  public byte[] calcLSH(double[] featureVec) {

    byte[] lsh = new byte[numPlanes];

    try {
      for (int p = 0; p < numPlanes; p++) {
        double dotProd = dotProduct(featureVec, hyperplanes[p]);
        int lshVal = (int)Math.floor((dotProd + offsets[p])/width);
        lsh[p] = (byte)(lshVal & 0xFF);
      }
    }
    catch (Exception e) {
      logger.error(e.getStackTrace().toString());
    }

    return lsh;
  }

  /**
   * Computes the dot product of two equal sized vectors.
   * @param a
   * @param b
   * @return
   */
  protected double dotProduct(double[] a, double[] b) {
    double sum = 0.0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i]*b[i];
    }
    return sum;
  }

  /**
   * calcLSH -- calculate LSH result for a given feature vector
   * and return result as a hex string
   *
   * @param featureVec
   * @return
   */
  public String calcLSHstring(double[] featureVec) {
    return ImageProcessing.bytesToHex(calcLSH(featureVec));
  }

  /**
   * calcLSHstring -- calculate the LSH result for a given feature vector (image histogram) and return
   * the result as a simple java string
   * @param imageHistogram - the color histogram as a hex string
   * @return
   */
  public String calcLSHstring(String imageHistogram) {
    byte [] byteHistogram = ImageProcessing.hexToBytes(imageHistogram);
    double []histogramAsFeatureVector = new double[byteHistogram.length];
    for (int i = 0; i < byteHistogram.length; i++) {
      histogramAsFeatureVector[i] = (double)(byteHistogram[i] & 0xFF);
    }
    return calcLSHstring(histogramAsFeatureVector);
  }
}
