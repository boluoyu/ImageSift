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

package software.uncharted;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.uncharted.util.HTTPUtil;
import software.uncharted.util.JSONUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Reindex {
    public static void main(String[] args) throws IOException {
        // Get all images
        JsonNode response = HTTPUtil.getJSON("http://localhost:3030/images/all");
        final ObjectMapper mapper = new ObjectMapper();

        // Create a list of post requests
        List<JsonNode> indexRequestBodies = JSONUtil.getStringList(response,"files").stream()
                .map(file -> "http://localhost:3030/image/" + file)
                .map(url -> "{\"url\":\""+url+"\"}")
                .map(json -> {
                    try {
                        return mapper.readTree(json);
                    } catch (IOException e) {}
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Reindex each
        for (JsonNode body : indexRequestBodies) {
            System.out.println("Indexing " + body.get("url").asText());
            HTTPUtil.post("http://localhost:8080/index",body);
        }
    }
}