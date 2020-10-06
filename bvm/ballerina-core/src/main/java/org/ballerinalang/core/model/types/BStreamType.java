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
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BStream;
import org.ballerinalang.core.model.values.BValue;

/**
 * {@code BStreamType} represents streaming data in Ballerina.
 *
 * @since 1.2.0
 */
public class BStreamType extends BType {

    private BType constraint;

    /**
     * Creates a {@code BStreamType} which represents the stream type.
     *
     * @param typeName   string name of the type
     * @param constraint the type by which this stream is constrained
     * @param pkgPath    package path
     */
    BStreamType(String typeName, BType constraint, String pkgPath) {
        super(typeName, pkgPath, BStream.class);
        this.constraint = constraint;
    }

    public BStreamType(BType constraint) {
        super(TypeConstants.STREAM_TNAME, null, BStream.class);
        this.constraint = constraint;
    }

    public BType getConstrainedType() {
        return constraint;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return null; //TODO:check - return (V) new BStream();
    }

    @Override
    public int getTag() {
        return TypeTags.STREAM_TAG;
    }

    @Override
    public String toString() {
        if (constraint == BTypes.typeAny) {
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
