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

package software.uncharted.elasticsearch;

import lombok.NoArgsConstructor;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Class to create, initialize and hold an app-wide singleton instance of an elastic search Java client.
 */
@Service
@NoArgsConstructor
public class ElasticClient {

  private String elasticHost = "localhost";
  private Integer elasticPort = 9300;
  private String elasticCluster = "elasticsearch";
  private String indexName = "image_similarity";
  private String indexType = "datum";

  private TransportClient client;

  public ElasticClient(Properties props) {
    elasticHost = props.getProperty("spring.elastic-connector.host");
    elasticPort = Integer.parseInt(props.getProperty("spring.elastic-connector.port"),10);
    elasticCluster = props.getProperty("spring.elastic-connector.cluster");
    indexName = props.getProperty("spring.elastic-connector.index");
    indexType = props.getProperty("spring.elastic-connector.type");
  }

  @PostConstruct
  public ElasticClient init() throws Exception {

    final Settings settings = Settings.settingsBuilder()
      .put("cluster.name", elasticCluster)
      .put("client.transport.sniff", false)
      .build();

    try {
      client = TransportClient.builder().settings(settings).build();
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort));
    } catch (final Exception ex) {
      throw new Exception("Could not connect to Elastic.", ex);
    }
    return this;
  }

  public TransportClient getClient() {
    return client;
  }

  public SearchRequestBuilder prepareSearch() {
    return client.prepareSearch().setIndices(indexName).setTypes(indexType);
  }

  public SearchResponse executeSearch(SearchRequestBuilder builder) {
    //TODO: logging
    return builder.get();
  }

  public IndexRequestBuilder prepareIndex() {
    return client.prepareIndex().setIndex(indexName).setType(indexType);
  }

  public IndexResponse executeIndex(IndexRequestBuilder builder) {
    //TODO: logging
    return builder.get();
  }
}
