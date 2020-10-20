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
package io.ballerina.runtime.observability.metrics;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Tag representing key/value pair.
 */
public final class Tag implements Comparable<Tag> {

    private final String key;
    private final String value;
    private final int hashCode;

    public Tag(String key, String value) {
        this.key = requireNonNull(key);
        this.value = requireNonNull(value);
        // Compute hash of this immutable Tag
        this.hashCode = Objects.hash(key, value);
    }

    public static Tag of(String key, String value) {
        return new Tag(key, value);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(Tag o) {
        return key.compareTo(o.key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tag tag = (Tag) o;
        return Objects.equals(key, tag.key) &&
                Objects.equals(value, tag.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "Tag{" + "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
