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

package org.ballerinalang.siddhi.query.api.execution.partition;

import org.ballerinalang.siddhi.query.api.expression.Expression;

/**
 * Partition type supporting values.
 */
public class ValuePartitionType implements PartitionType {
    private static final long serialVersionUID = 1L;
    private Expression expression;
    private String streamId;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public ValuePartitionType(String streamId, Expression expression) {
        this.streamId = streamId;
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getStreamId() {
        return streamId;
    }

    @Override
    public String toString() {
        return "ValuePartitionType{" +
                "expression=" + expression +
                ", id='" + streamId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValuePartitionType that = (ValuePartitionType) o;

        if (!expression.equals(that.expression)) {
            return false;
        }
        if (!streamId.equals(that.streamId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + streamId.hashCode();
        return result;
    }

    @Override
    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    @Override
    public void setQueryContextStartIndex(int[] lineAndColumn) {
        queryContextStartIndex = lineAndColumn;
    }

    @Override
    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public void setQueryContextEndIndex(int[] lineAndColumn) {
        queryContextEndIndex = lineAndColumn;
    }
}
