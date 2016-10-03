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

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Data
public class ImageSearchResult {
    Long duration;          // in milliseconds
    Set<Image> images;

    public static ImageSearchResult empty() {
        return new ImageSearchResult()
                .setImages(Sets.newHashSet())
                .setDuration(0L);
    }
}
