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

package io.ballerina.runtime.values;

import io.ballerina.runtime.IteratorUtils;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.types.BStreamType;

import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * The {@link StreamValue} represents a stream in Ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 1.2.0
 */
public class StreamValue implements RefValue, BStream {

    private Type  type;
    private Type  constraintType;
    private Type  iteratorNextReturnType;
    private BObject iteratorObj;


    /**
     * The name of the underlying broker topic representing the stream object.
     */
    public String streamId;

    @Deprecated
    public StreamValue(Type  type) {
        this.constraintType = ((BStreamType) type).getConstrainedType();
        this.type = new BStreamType(constraintType);
        this.streamId = UUID.randomUUID().toString();
        this.iteratorObj = null;
    }

    public StreamValue(Type  type, BObject iteratorObj) {
        this.constraintType = ((BStreamType) type).getConstrainedType();
        this.type = new BStreamType(constraintType);
        this.streamId = UUID.randomUUID().toString();
        this.iteratorObj = iteratorObj;
    }

    public String getStreamId() {
        return streamId;
    }

    public BObject getIteratorObj() {
        return iteratorObj;
    }

    public Type  getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(constraintType);
        }

        return iteratorNextReturnType;
    }

    /**
     * {@inheritDoc}
     * @param parent The link to the parent node
     */
    public String stringValue(BLink parent) {
        return "stream <" + getType().toString() + ">";
    }

    /**
     * {@inheritDoc}
     * @param parent The link to the parent node
     */
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Object frozenCopy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    public Type getConstraintType() {
        return constraintType;
    }

    @Override
    public String toString() {
        return stringValue(null);
    }
}
