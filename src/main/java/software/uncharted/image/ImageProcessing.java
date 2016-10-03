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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Image processing and feature extraction functions
 *
 */
public class ImageProcessing {

    private final static Logger logger = LoggerFactory.getLogger(ImageProcessing.class);

    final protected static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();


  //---- Constants for color histogram calc
    private static final int COLOR_DEPTH = 4;                                   // num distinct color values per channel
    private static final int COLOR_DIVISOR = 256/COLOR_DEPTH;                   // to quantize histogram
    private static final int DISTINCT_COLORS = (int)Math.pow(COLOR_DEPTH,3);    // num histogram bins
    private static final int RATIO_MULTIPLIER = 0xFF;                           // = 255
    private static final double HIST_NORM_FACTOR = 1.0; //0.5;                  // histogram normalization factor (btwn 0 and 1)
                                                                                // (lower value reduces rounding --> 0 for low hist values,
                                                                                // but can cause saturation of very high values)


    /**
     * downloadImage -- download an image to a byte array
     *
     * @param urlToRead
     * @return
     */
    public static byte[] downloadImage(String urlToRead) {
        String urlStr  = null;
        HttpURLConnection conn;
        if (urlToRead==null || urlToRead.compareToIgnoreCase("null")==0 || urlToRead.compareToIgnoreCase("\\N")==0) {
            logger.warn("downloadImage failed. Invalid image URL: " + urlToRead);
            return null;
        }

        try {
            urlStr = urlToRead;
            //if (!urlStr.startsWith("http")) {
            //    url = new URL("https://s3.amazonaws.com/roxyimages/" + urlStr);    //TODO is this necessary?? .... do we need to add an optional 'prefix' string here?
            //}
            urlStr = urlStr.replace(" ", "%20");
            conn = (HttpURLConnection)(new URL(urlStr).openConnection());
            conn.setRequestMethod("GET");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];              // size of byte chunks to read in at a time
            InputStream is = conn.getInputStream();
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();    // now full image is in 'buffer', so convert to byte array
            return byteArray;
        } catch (Exception e) {
            logger.trace("downloadImage failed. Failed to read URL: " + urlStr + " <" + e.getMessage() + ">");
        }
        return null;
    }

    public static byte[] bufferedImageToByteArray(BufferedImage bi) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( bi, "jpg", baos );
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    public static BufferedImage byteArrayToBufferedImage(byte[] img) throws IOException {
        InputStream in = new ByteArrayInputStream(img);
        return ImageIO.read(in);
    }

    /**
     * generateSha1 -- generate SHA-1 hash of an image
     *
     * @param image
     * @return
     */
    public static String generateSha1(byte[] image) {
        byte[] sha1Bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            sha1Bytes = md.digest(image);
        } catch (java.security.NoSuchAlgorithmException e) {
            logger.error("SHA hashing not available");
        }
        return bytesToHex(sha1Bytes);
    }

  /**
   * generateSha1 -- generate SHA-1 hash of an image
   *
   * @param inputStream
   * @return
   */
  public static String generateSha1(InputStream inputStream) throws IOException {
    return generateSha1(IOUtils.toByteArray(inputStream));
  }

  public static String toBase64(BufferedImage bi) throws IOException {
      return Base64.getEncoder().encodeToString(bufferedImageToByteArray(bi));
  }

    /**
     * histogramByteHash -- compute a color histogram of an image
     *                      returned as a byte[]
     * @param img
     * @return byte[] histogram
     */
    public static byte[] histogramByteHash(BufferedImage img) {

        Raster raster = img.getData();
        int h = raster.getHeight();
        int w = raster.getWidth();
        int components = img.getColorModel().getNumComponents();
        int pixels = w*h;
        int[] colors = new int[pixels*components];
        raster.getPixels(0, 0, w, h, colors);
        int[] counts = new int[DISTINCT_COLORS];
        int grayScaleCount = 0;
        for (int i=0; i<DISTINCT_COLORS; i++) counts[i] = 0;

        int cIndx = 0;   // 'colours' array index
        for (int i=0; i<pixels; i++) {
            int r = colors[cIndx]/COLOR_DIVISOR;      // quantizes down to 'COLOR_DEPTH' range
            int g = (colors[cIndx+1])/COLOR_DIVISOR;
            int b = (colors[cIndx+2])/COLOR_DIVISOR;
            int truncColor = (r*COLOR_DEPTH+g)*COLOR_DEPTH+b;   // converts 3D histogram values to 1D concatenated histogram
            counts[truncColor]++;
            if (r==g&&r==b) grayScaleCount++;
            cIndx += components;
        }
        byte[] result = new byte[DISTINCT_COLORS];

        if (grayScaleCount>pixels*0.95) {
            //---- grayscale image detected!
            // set black and white hist bins == max value
            // and set all other bins == hist values along one of the colour axes
            // (since r-axis vals = g-axis = b-axis for grayscale)
            counts[0] = pixels;
            counts[DISTINCT_COLORS-1] = pixels;
            for (int i=1; i<DISTINCT_COLORS-1; i++) {
                counts[i]=0;
            }
            for (int i=0; i<pixels; i++) {
                int idx = colors[i*components]*(DISTINCT_COLORS-2)/256 + 1;
                counts[idx]++;
            }
        }

        //---- normalize final histogram
        for (int i=0; i<DISTINCT_COLORS; i++) {
            //int count = (int)Math.ceil((counts[i]*RATIO_MULTIPLIER)/pixels);
            int count = (int) Math.round( (counts[i]*RATIO_MULTIPLIER)/((double)pixels*HIST_NORM_FACTOR) );
            result[i] = (byte)( Math.min(count,RATIO_MULTIPLIER) & 0xFF );  // Min here to handle potential saturation of hist values
        }

        return result;
    }

    /**
     * histogramHash -- compute a color histogram of an image
     *                  returned as a hex string
     * @param img
     * @return
     */
    public static String histogramHash(BufferedImage img) {

      byte[] result = histogramByteHash(img);

        return bytesToHex(result);
    }

    public static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];
      for ( int j = 0; j < bytes.length; j++ ) {
        int v = bytes[j] & 0xFF;
        hexChars[j * 2] = HEX_CHARS[v >>> 4];
        hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
      }
      return new String(hexChars);
    }

    public static int hexToInt(char hex) {
        if (hex>='0'&&hex<='9') return (int)(hex-'0');
        else if (hex>='A'&&hex<='F') return (int)(10+hex-'A');
        return (int)(10+hex-'a');
      }

    public static byte[] hexToBytes(String hexStr) {
      char[] hex = hexStr.toCharArray();
      byte[] result = new byte[hex.length/2];
      for ( int j = 0; j < result.length; j++ ) {
        int cval = 0x10*hexToInt(hex[j*2])+hexToInt(hex[j*2+1]);
        result[j] = (byte)cval;
      }
      return result;
    }

  public static boolean hasSimilarHistograms(byte[] h1, byte[] h2, int epsilon) {

    if (h1 == null || h2 == null || h1.length == 0 || h2.length == 0 || h1.length != h2.length ) {
      return false;
    }

    int totalDelta = 0;
    for (int i = 0; i < h1.length; i++) {
      totalDelta += Math.abs(h1[i] - h2[i]);
    }

    return totalDelta <= epsilon;
  }

  public static boolean hasSimilarHistograms(String h1, String h2, int epsilon) {
    byte[] h1Bytes = hexToBytes(h1);
    byte[] h2Bytes = hexToBytes(h2);
    return hasSimilarHistograms(h1Bytes,h2Bytes,epsilon);
  }
}
