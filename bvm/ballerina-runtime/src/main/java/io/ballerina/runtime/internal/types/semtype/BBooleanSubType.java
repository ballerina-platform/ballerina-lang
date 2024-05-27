/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.SubType;

/**
 * Runtime representation of BooleanSubType.
 *
 * @since 2201.10.0
 */
public final class BBooleanSubType extends SubType {

    private final BBooleanSubTypeData data;
    private static final BBooleanSubType ALL = new BBooleanSubType(BBooleanSubTypeData.ALL);
    private static final BBooleanSubType NOTHING = new BBooleanSubType(BBooleanSubTypeData.NOTHING);
    private static final BBooleanSubType TRUE = new BBooleanSubType(BBooleanSubTypeData.TRUE);
    private static final BBooleanSubType FALSE = new BBooleanSubType(BBooleanSubTypeData.FALSE);

    private BBooleanSubType(BBooleanSubTypeData data) {
        super(data.isAll(), data.isNothing());
        this.data = data;
    }

    public static BBooleanSubType from(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public SubType union(SubType otherSubtype) {
        if (!(otherSubtype instanceof BBooleanSubType other)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        if (this.isAll()) {
            return this;
        }
        if (other.isAll()) {
            return other;
        }
        if (this.isNothing()) {
            return other;
        }
        if (other.isNothing()) {
            return this;
        }
        if (this.data.value == other.data.value) {
            return this;
        }
        return ALL;
    }

    @Override
    public SubType intersect(SubType otherSubtype) {
        if (!(otherSubtype instanceof BBooleanSubType other)) {
            throw new IllegalArgumentException("intersection of different subtypes");
        }
        if (this.isAll()) {
            return other;
        }
        if (other.isAll()) {
            return this;
        }
        if (this.isNothing() || other.isNothing()) {
            return NOTHING;
        }
        if (this.data.value == other.data.value) {
            return this;
        }
        return NOTHING;
    }

    @Override
    public SubType diff(SubType otherSubtype) {
        if (!(otherSubtype instanceof BBooleanSubType other)) {
            throw new IllegalArgumentException("diff of different subtypes");
        }
        if (this.isAll() && other.isAll()) {
            return NOTHING;
        }
        if (this.isNothing() || other.isAll()) {
            return NOTHING;
        }
        if (other.isNothing()) {
            return this;
        }
        if (this.isAll()) {
            if (other.isNothing()) {
                return this;
            }
            return from(!other.data.value);
        }
        return this.data.value == other.data.value ? NOTHING : this;
    }

    @Override
    public SubType complement() {
        if (isAll()) {
            return NOTHING;
        }
        if (isNothing()) {
            return ALL;
        }
        return from(!data.value);
    }

    @Override
    public boolean isEmpty() {
        return data.isNothing();
    }

    @Override
    public SubTypeData data() {
        return data.toData();
    }

    private record BBooleanSubTypeData(boolean isAll, boolean isNothing, boolean value) {

        private static final BBooleanSubTypeData ALL = new BBooleanSubTypeData(true, false, false);
        private static final BBooleanSubTypeData NOTHING = new BBooleanSubTypeData(false, true, false);
        private static final BBooleanSubTypeData TRUE = new BBooleanSubTypeData(false, false, true);
        private static final BBooleanSubTypeData FALSE = new BBooleanSubTypeData(false, false, false);

        static BBooleanSubTypeData from(boolean value) {
            return value ? TRUE : FALSE;
        }

        SubTypeData toData() {
            if (isAll()) {
                return AllOrNothing.ALL;
            } else if (isNothing()) {
                return AllOrNothing.NOTHING;
            }
            return new BooleanSubTypeData(value());
        }
    }

    private record BooleanSubTypeData(boolean value) implements SubTypeData {

    }
}
