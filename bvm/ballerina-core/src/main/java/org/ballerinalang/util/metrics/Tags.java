/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility methods to get a list of {@link Tag Tags}.
 */
public class Tags {

    private Tags() {
    }

    /**
     * Get tags from key/value pairs
     *
     * @param keyValues Must be an even number of arguments representing key/value pairs of tags.
     * @return A list of {@link Tag Tags}.
     */
    public static List<Tag> tags(String... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return Collections.emptyList();
        }
        if (keyValues.length % 2 == 1) {
            throw new IllegalArgumentException("size must be even, it is a set of key=value pairs");
        }
        List<Tag> tags = new ArrayList<>(keyValues.length / 2);
        for (int i = 0; i < keyValues.length; i += 2) {
            tags.add(Tag.of(keyValues[i], keyValues[i + 1]));
        }
        return tags;
    }

    /**
     * Get tags from another collection of tags
     *
     * @param tags A collection of {@link Tag Tags}
     * @return A list of {@link Tag Tags}.
     */
    public static List<Tag> tags(Iterable<Tag> tags) {
        List<Tag> newTags = new ArrayList<>();
        tags.forEach(newTags::add);
        return newTags;
    }

    /**
     * Get tags from a key/value pair
     *
     * @param key   The tag key.
     * @param value The tag value.
     * @return A list of {@link Tag Tags}.
     */
    public static List<Tag> tag(String key, String value) {
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of(key, value));
        return tags;
    }

    /**
     * Get tags from a map of key/value pairs
     *
     * @param tags A map of key value pairs to be used as tags
     * @return A list of {@link Tag Tags}.
     */
    public static List<Tag> tags(Map<String, String> tags) {
        List<Tag> newTags = new ArrayList<>();
        tags.forEach((key, value) -> newTags.add(Tag.of(key, value)));
        return newTags;
    }
}
