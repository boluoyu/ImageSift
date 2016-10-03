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

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.model.Cluster;
import software.uncharted.model.ClusterResponse;
import software.uncharted.service.ClusterService;

import java.io.IOException;

@RestController
@RequestMapping("/clusters")
public class ClusterResource {

    @Autowired
    ClusterService clusterService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ClusterResponse getClusters(@RequestParam(value ="from", required = false) final Integer from) throws IOException {
        return clusterService.getTopClusters(from);
    }

    @RequestMapping(value = "/cluster/{cluster}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ClusterResponse getCluster(@PathVariable("cluster") final String lshCluster) {
        Cluster cluster = clusterService.getCluster(lshCluster);
        return new ClusterResponse()
                .setHasMore(false)
                .setClusters(Sets.newHashSet(cluster));
    }
}
