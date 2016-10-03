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

package software.uncharted.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.uncharted.image.ImageProcessing;
import software.uncharted.model.ImageHistogramResponse;
import software.uncharted.service.ImageService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/image")
public class ImageResource {

    @Autowired
    ImageService service;

    @RequestMapping(value = "/histogram", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImageHistogramResponse getImageHistogram(@RequestParam("image") final MultipartFile file) throws IOException {
        String fileType = file.getContentType();
        if (fileType.startsWith("image")) {
            BufferedImage image = ImageIO.read(file.getInputStream());
            final String histogram = service.getHistogram(image);
            final String base64Str = ImageProcessing.toBase64(image);
            return new ImageHistogramResponse()
                    .setDataURI("data:image/png;base64," + base64Str)
                    .setHistogram(histogram);
        }
        return null;
    }
}
