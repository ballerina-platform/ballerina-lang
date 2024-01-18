/*
 *
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 * /
 *
 */

package io.ballerina.runtime.api;

import java.util.stream.Stream;

public record SimpleType(long all, long some) {

    public SimpleType intersection(SimpleType other) {
        long all = this.all & other.all;
        long some = (this.some | this.all) & (other.some | other.all);
        some &= ~all;
        return new SimpleType(all, some);
    }

    public SimpleType union(SimpleType other) {
        long all = this.all | other.all;
        long some = this.some | other.some;
        some &= ~all;
        return new SimpleType(all, some);
    }

    public SimpleType diff(SimpleType other) {
        long all = this.all & ~(other.all | other.some);
        long some = (this.all | this.some) & ~(other.all);
        some &= ~all;
        return new SimpleType(all, some);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleType other)) {
            return false;
        }
        return this.all == other.all && this.some == other.some;
    }

    @Override
    public String toString() {
        SimpleTypeTag[] allTags =
                Stream.of(SimpleTypeTag.values()).filter(tag -> (all & (1L << tag.ordinal())) != 0)
                        .toArray(SimpleTypeTag[]::new);
        SimpleTypeTag[] someTags =
                Stream.of(SimpleTypeTag.values()).filter(tag -> (some & (1L << tag.ordinal())) != 0)
                        .toArray(SimpleTypeTag[]::new);
        StringBuilder sb = new StringBuilder();
        sb.append("SimpleType all: {");
        for (SimpleTypeTag tag : allTags) {
            sb.append(tag).append(", ");
        }
        sb.append("} some: {");
        for (SimpleTypeTag tag : someTags) {
            sb.append(tag).append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}
