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

package software.uncharted.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by cdickson on 2016-01-26.
 */
public class JSONUtil {
  private static String getArrayKey(String accessor) {
    return accessor.substring(0,accessor.indexOf("["));
  }

  private static int getArrayIndex(String accessor) {
    return Integer.parseInt(accessor.substring(accessor.indexOf("[") + 1, accessor.indexOf("]")));
  }


  private static JsonNode get(JsonNode obj, String accessor) {
    int keyIdx = accessor.indexOf(".");
    if (keyIdx == -1) {

      String key = accessor;

      if (key.contains("[")) {
        String arrayKey = getArrayKey(key);
        int arrayIdx = getArrayIndex(key);
        JsonNode a = obj.get(arrayKey);
        return a.get(arrayIdx);
      } else {
        return obj.get(key);
      }

    } else {
      String key = accessor.substring(0, keyIdx);
      String remainingAccessor = accessor.substring(keyIdx+1);

      if (key.contains("[")) {
        String arrayKey = getArrayKey(key);
        int arrayIdx = getArrayIndex(key);
        JsonNode a = obj.get(arrayKey);
        return get(a.get(arrayIdx), remainingAccessor);

      } else {
        return get(obj.get(key), remainingAccessor);
      }
    }
  }

  public static ArrayNode getArray(JsonNode obj, String accessor)  {
    return (ArrayNode)get(obj,accessor);
  }

  public static List<String> getStringList(JsonNode obj, String accessor) {
    ArrayNode arrayNode = getArray(obj,accessor);
    List<String> stringList = Lists.newArrayList();
    for (JsonNode n : arrayNode) {
      stringList.add(n.asText());
    }
    return stringList;
  }

  public static JsonNode getObject(JsonNode obj, String accessor)  {
    return (JsonNode)get(obj,accessor);
  }

  public static String getString(JsonNode obj, String accessor)  {
    return get(obj,accessor).asText();
  }

  public static Integer getInt(JsonNode obj, String accessor)  {
    return get(obj,accessor).asInt();
  }

  public static Long getLong(JsonNode obj, String accessor)  {
    return get(obj,accessor).asLong();
  }

  public static Boolean getBoolean(JsonNode obj, String accessor)  {
    return get(obj,accessor).asBoolean();
  }

  public static Double getDouble(JsonNode obj, String accessor)  {
    return get(obj,accessor).asDouble();
  }
}
