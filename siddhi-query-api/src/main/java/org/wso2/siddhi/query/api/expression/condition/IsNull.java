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
package org.wso2.siddhi.query.api.expression.condition;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Condition {@link Expression} checking whether the event is null
 */
public class IsNull extends Expression {

    private static final long serialVersionUID = 1L;

    private String streamId;
    private Integer streamIndex;
    private boolean isInnerStream;
    private Expression expression;

    public IsNull(Expression expression) {
        this.expression = expression;
    }

    public IsNull(String streamId, Integer streamIndex, boolean isInnerStream) {

        this.streamId = streamId;
        this.streamIndex = streamIndex;
        this.isInnerStream = isInnerStream;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getStreamId() {
        return streamId;
    }

    public Integer getStreamIndex() {
        return streamIndex;
    }

    public boolean isInnerStream() {
        return isInnerStream;
    }

    @Override
    public String toString() {
        return "IsNull{" +
                "id='" + streamId + '\'' +
                ", streamIndex=" + streamIndex +
                ", isInnerStream=" + isInnerStream +
                ", expression=" + expression +
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IsNull)) {
            return false;
        }

        IsNull that = (IsNull) o;

        if (isInnerStream != that.isInnerStream) {
            return false;
        }
        if (expression != null ? !expression.equals(that.expression) : that.expression != null) {
            return false;
        }
        if (streamId != null ? !streamId.equals(that.streamId) : that.streamId != null) {
            return false;
        }
        if (streamIndex != null ? !streamIndex.equals(that.streamIndex) : that.streamIndex != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = streamId != null ? streamId.hashCode() : 0;
        result = 31 * result + (streamIndex != null ? streamIndex.hashCode() : 0);
        result = 31 * result + (isInnerStream ? 1 : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }

}
