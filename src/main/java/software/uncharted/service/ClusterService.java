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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.uncharted.elasticsearch.ElasticClient;
import software.uncharted.model.Cluster;
import software.uncharted.model.ClusterResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClusterService {

    @Autowired
    protected ElasticClient client;

    @Autowired
    protected ObjectMapper mapper;

    public static final Integer CLUSTERS_PER_PAGE = 10;

    public static final String preference = "ARandomStringSoAggsComeOutTheSame";

    public ClusterResponse getTopClusters(Integer from_) {
        final Integer from = from_ != null ? from_ : 0;

        final String AGGREGATION_NAME = "clusters";
        TermsBuilder termsBuilder = AggregationBuilders.terms(AGGREGATION_NAME)
                .field("lsh")
                .size(from + CLUSTERS_PER_PAGE);


        SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(0)
                .setPreference(preference)
                .addAggregation(termsBuilder);

        SearchResponse searchResponse = client.executeSearch(searchRequestBuilder);

        List<String> topBuckets = ((Terms) searchResponse.getAggregations().get(AGGREGATION_NAME)).getBuckets().stream()
                .map(MultiBucketsAggregation.Bucket::getKeyAsString)    // pull out the term as a string
                .collect(Collectors.toList());

        topBuckets = topBuckets.subList(from, Math.min(topBuckets.size(), from + CLUSTERS_PER_PAGE));

        Set<Cluster> clusters = topBuckets.stream()
                .map(lsh -> getCluster(lsh))
                .collect(Collectors.toSet());

        final boolean hasMore =  ((Terms) searchResponse.getAggregations().get(AGGREGATION_NAME)).getSumOfOtherDocCounts() > 0L;

        return new ClusterResponse()
                .setClusters(clusters)
                .setHasMore(hasMore);

    }

    public Cluster getCluster(String lsh) {
        final String AGGREGATION_NAME = "imageurls";
        TermsBuilder termsBuilder = AggregationBuilders.terms(AGGREGATION_NAME)
                .field("url")
                .size(0);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                .setQuery(QueryBuilders.termQuery("lsh",lsh))
                .addAggregation(termsBuilder)
                .setPreference(preference)
                .setSize(0);

        SearchResponse searchResponse = client.executeSearch(searchRequestBuilder);

        Set<String> urls = ((Terms) searchResponse.getAggregations().get(AGGREGATION_NAME)).getBuckets().stream()
                .map(MultiBucketsAggregation.Bucket::getKeyAsString)    // pull out the term as a string
                .collect(Collectors.toSet());

        return new Cluster()
                .setLsh(lsh)
                .setUrls(urls);
    }
}
