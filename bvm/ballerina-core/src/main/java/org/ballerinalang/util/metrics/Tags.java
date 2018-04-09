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
import java.util.Map;

/**
 * Utility methods to add tags to an existing list of {@link Tag Tags}.
 */
public class Tags {

    private Tags() {
    }

    /**
     * Populate tags from key/value pairs
     *
     * @param tags      An existing list of {@link Tag Tags}.
     * @param keyValues Must be an even number of arguments representing key/value pairs of tags.
     */
    public static void tags(ArrayList<Tag> tags, String... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return;
        }
        if (keyValues.length % 2 == 1) {
            throw new IllegalArgumentException("size must be even, it is a set of key=value pairs");
        }
        tags.ensureCapacity(keyValues.length / 2);
        for (int i = 0; i < keyValues.length; i += 2) {
            tags.add(Tag.of(keyValues[i], keyValues[i + 1]));
        }
    }

    /**
     * Populate tags from another collection of tags
     *
     * @param existingTags An existing list of {@link Tag Tags}.
     * @param tags         A collection of {@link Tag Tags}
     */
    public static void tags(ArrayList<Tag> existingTags, Iterable<Tag> tags) {
        tags.forEach(existingTags::add);
    }

    /**
     * Populate tags from a key/value pair
     *
     * @param tags  An existing list of {@link Tag Tags}.
     * @param key   The tag key.
     * @param value The tag value.
     */
    public static void tag(ArrayList<Tag> tags, String key, String value) {
        tags.ensureCapacity(1);
        tags.add(Tag.of(key, value));
    }

    /**
     * Populate tags from a map of key/value pairs
     *
     * @param tags    An existing list of {@link Tag Tags}.
     * @param tagsMap A map of key value pairs to be used as tags
     */
    public static void tags(ArrayList<Tag> tags, Map<String, String> tagsMap) {
        tags.ensureCapacity(tagsMap.size());
        tagsMap.forEach((key, value) -> tags.add(Tag.of(key, value)));
    }
}
