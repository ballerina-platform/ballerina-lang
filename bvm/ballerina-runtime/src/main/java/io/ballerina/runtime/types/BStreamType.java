/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeConstants;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.values.StreamValue;

/**
 * {@link BStreamType} represents streaming data in Ballerina.
 *
 * @since 1.2.0
 */
public class BStreamType extends BType implements StreamType {

    private Type constraint;

    /**
     * Creates a {@link BStreamType} which represents the stream type.
     *
     * @param typeName   string name of the type
     * @param constraint the type by which this stream is constrained
     * @param pkgPath    package path
     */
    public BStreamType(String typeName, Type constraint, Module pkgPath) {
        super(typeName, pkgPath, StreamValue.class);
        this.constraint = constraint;
    }

    public BStreamType(Type constraint) {
        super(TypeConstants.STREAM_TNAME, null, StreamValue.class);
        this.constraint = constraint;
    }

    public Type getConstrainedType() {
        return constraint;
    }

    @Override
    public <V> V getZeroValue() {
        return (V) new StreamValue(this);
    }

    @Override
    public <V> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.STREAM_TAG;
    }

    @Override
    public String toString() {
        if (constraint == PredefinedTypes.TYPE_ANY) {
            return super.toString();
        } else {
            return "stream" + "<" + constraint.getName() + ">";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BStreamType)) {
            return false;
        }

        BStreamType other = (BStreamType) obj;
        if (constraint == other.constraint) {
            return true;
        }

        if (constraint == null || other.constraint == null) {
            return false;
        }

        return constraint.equals(other.constraint);
    }
}
