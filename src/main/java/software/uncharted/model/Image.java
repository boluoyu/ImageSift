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

package software.uncharted.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.image.ImageProcessing;
import software.uncharted.util.JSONUtil;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Image implements Serializable {
  private String hash;
  private String histogram;
  private Integer width;
  private Integer height;
  private String url;
  private List<String> lsh;

  @JsonIgnore
  private static Integer similarityEpsilon = 10;

  public Image(JsonNode node) {
    hash = JSONUtil.getString(node,"hash");
    histogram = JSONUtil.getString(node,"histogram");
    width = JSONUtil.getInt(node,"width");
    height = JSONUtil.getInt(node,"height");
    url = JSONUtil.getString(node,"url");

    lsh = Lists.newArrayList();
    ArrayNode arr = JSONUtil.getArray(node,"lsh");
    arr.forEach(jsonNode -> lsh.add(jsonNode.asText()));
  }

  public boolean hasSimilarHistogram(String histogram) {
    return ImageProcessing.hasSimilarHistograms(this.histogram,histogram,similarityEpsilon);
  }
}
