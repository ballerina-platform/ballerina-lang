/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.execution.query.output.stream;

import org.wso2.siddhi.query.api.util.SiddhiConstants;

/**
 * Query output stream inserting events in to another stream, table, or window
 */
public class InsertIntoStream extends OutputStream {

    private boolean isInnerStream;

    public InsertIntoStream(String streamId) {
        this(streamId, false, OutputEventType.CURRENT_EVENTS);
    }

    public InsertIntoStream(String streamId, OutputEventType outputEventType) {
        this(streamId, false, outputEventType);
    }

    public InsertIntoStream(String streamId, boolean isInnerStream) {
        this(streamId, isInnerStream, OutputEventType.CURRENT_EVENTS);
    }

    public InsertIntoStream(String streamId, boolean isInnerStream, OutputEventType outputEventType) {
        this.isInnerStream = isInnerStream;
        if (isInnerStream) {
            this.id = SiddhiConstants.INNER_STREAM_FLAG.concat(streamId);
        } else {
            this.id = streamId;
        }
        this.outputEventType = outputEventType;
    }

    public boolean isInnerStream() {
        return isInnerStream;
    }

    @Override
    public String toString() {
        return "InsertIntoStream{" +
                "isInnerStream=" + isInnerStream +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InsertIntoStream)) {
            return false;
        }

        InsertIntoStream that = (InsertIntoStream) o;

        if (isInnerStream != that.isInnerStream) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (isInnerStream ? 1 : 0);
    }
}
