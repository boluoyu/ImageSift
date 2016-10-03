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

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.uncharted.model.Image;
import software.uncharted.model.ImageSearchResult;
import software.uncharted.service.ImageService;
import software.uncharted.util.JSONUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/search")
public class ImageSearchResource {

    @Autowired
    ImageService service;

    @RequestMapping(value = "/file", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImageSearchResult searchByFile(@RequestParam("image") final MultipartFile file) throws IOException {
        long start = System.nanoTime();
        String fileType = file.getContentType();
        if (fileType.startsWith("image")) {
            BufferedImage image = ImageIO.read(file.getInputStream());
            Set<Image> images = service.search(image);
            long duration = (System.nanoTime() - start) / 1000000L;
            return new ImageSearchResult()
                    .setImages(images)
                    .setDuration(duration);

        }
        return ImageSearchResult.empty();
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImageSearchResult searchByUrl(@RequestBody final JsonNode body) throws IOException {
        long start = System.nanoTime();
        String url = JSONUtil.getString(body, "url");
        Set<Image> images = service.search(url);
        long duration = (System.nanoTime() - start) / 1000000L;
        return new ImageSearchResult()
                .setImages(images)
                .setDuration(duration);
    }

    @RequestMapping(value = "/histogram/{histogram}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImageSearchResult searchByHash(@PathVariable("histogram") final String histogram) {
        long start = System.nanoTime();
        Set<Image> images = service.searchByHistogram(histogram);
        long duration = (System.nanoTime() - start) / 1000000L;
        return new ImageSearchResult()
                .setImages(images)
                .setDuration(duration);

    }
}
