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

package software.uncharted.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.uncharted.elasticsearch.ElasticClient;
import software.uncharted.image.ImageProcessing;
import software.uncharted.image.StableDistLSH;
import software.uncharted.model.Image;
import software.uncharted.model.ImageIndexResponse;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Autowired
    protected ElasticClient client;

    @Autowired
    protected ObjectMapper mapper;

    protected List<StableDistLSH> imageHashers;

    @Value("${spring.image.similarityEpsilon}")
    private Integer epsilon;

    @PostConstruct
    public void init() {
        if (imageHashers == null) {
            imageHashers = Lists.newArrayList();
            imageHashers.add(new StableDistLSH(24, 64, 128.0, 911L));     //_0
            imageHashers.add(new StableDistLSH(24, 64, 128.0, 802L));     //_1
        }
    }

    /**
     * Given a url for an image, index it for retrieval later
     * @param url
     * @return
     * @throws IOException
     */
    public ImageIndexResponse index(String url) throws IOException {
        byte[] image = ImageProcessing.downloadImage(url);
        BufferedImage bi = ImageProcessing.byteArrayToBufferedImage(image);

        // Compute the sha1 hash, the color histogram, and the lsh hashes for the histogram
        String sha1 = ImageProcessing.generateSha1(image);
        String histogram = ImageProcessing.histogramHash(bi);
        List<String> lshs = imageHashers.stream().map(h -> h.calcLSHstring(histogram)).collect(Collectors.toList());

        // Create the image model
        Image imageModel = new Image()
                .setWidth(bi.getWidth())
                .setHeight(bi.getHeight())
                .setHistogram(histogram)
                .setHash(sha1)
                .setLsh(lshs)
                .setUrl(url);


        // Index the image
        byte[] serializedImage = mapper.writeValueAsBytes(imageModel);
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
                .setId(sha1)
                .setSource(serializedImage);
        IndexResponse response = client.executeIndex(indexRequestBuilder);

        return new ImageIndexResponse(response,imageModel);
    }

    /**
     * Given an image, search for it in our index and return a set of images that are similar to it
     * @param image
     * @return
     * @throws IOException
     */
    public Set<Image> search(BufferedImage image) throws IOException {
        final String histogram = getHistogram(image);
        return searchByHistogram(histogram);
    }

    public Set<Image> search(String url) throws IOException {
        byte[] imageBytes = ImageProcessing.downloadImage(url);
        return search(ImageProcessing.byteArrayToBufferedImage(imageBytes));
    }

    public Set<Image> searchByHash(String lsh) {
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                .setQuery(QueryBuilders.termQuery("lsh",lsh));
        SearchResponse response = client.executeSearch(searchRequestBuilder);

        Set<Image> results = Sets.newHashSet();
        for (SearchHit hit : response.getHits()) {
            String jsonString = hit.getSourceAsString();
            try {
                JsonNode node = mapper.readTree(jsonString);
                results.add(new Image(node));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return results;
    }


    public Set<Image> searchByHistogram(String histogram) {
        // Create a set of all the lsh clusters
        final Set<Image> lshUnions = Sets.newHashSet();
        imageHashers.stream()
                .map(hasher -> hasher.calcLSHstring(histogram))
                .forEach(lsh -> lshUnions.addAll(searchByHash(lsh)));

        // filter to once that have close histograms
        return lshUnions.stream()
                .filter(i -> i.hasSimilarHistogram(histogram))
                .collect(Collectors.toSet());
    }

    public String getHistogram(BufferedImage image) {
        byte[] histogramByteHash = ImageProcessing.histogramByteHash(image);
        return ImageProcessing.bytesToHex(histogramByteHash);
    }
}
