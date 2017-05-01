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
package org.wso2.siddhi.query.api.execution.partition;

import org.wso2.siddhi.query.api.expression.Expression;

import java.util.Arrays;

/**
 * Partition type supporting value ranges
 */
public class RangePartitionType implements PartitionType {

    private final String streamId;
    private final RangePartitionProperty[] rangePartitionProperties;

    public RangePartitionType(String streamId, RangePartitionProperty[] rangePartitionProperties) {

        this.streamId = streamId;
        this.rangePartitionProperties = Arrays.copyOfRange(rangePartitionProperties, 0, rangePartitionProperties
                .length);
    }

    public String getStreamId() {
        return streamId;
    }

    public RangePartitionProperty[] getRangePartitionProperties() {
        return Arrays.copyOfRange(rangePartitionProperties, 0, rangePartitionProperties.length);
    }

    @Override
    public String toString() {
        return "RangePartitionType{" +
                "id='" + streamId + '\'' +
                ", rangePartitionProperties=" + Arrays.toString(rangePartitionProperties) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RangePartitionType)) {
            return false;
        }

        RangePartitionType that = (RangePartitionType) o;

        if (!Arrays.equals(rangePartitionProperties, that.rangePartitionProperties)) {
            return false;
        }
        if (streamId != null ? !streamId.equals(that.streamId) : that.streamId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = streamId != null ? streamId.hashCode() : 0;
        result = 31 * result + (rangePartitionProperties != null ? Arrays.hashCode(rangePartitionProperties) : 0);
        return result;
    }

    /**
     * Each range partition property
     */
    public static class RangePartitionProperty {
        private final String partitionKey;
        private final Expression condition;

        public RangePartitionProperty(String partitionKey, Expression condition) {

            this.partitionKey = partitionKey;
            this.condition = condition;
        }

        public String getPartitionKey() {
            return partitionKey;
        }

        public Expression getCondition() {
            return condition;
        }

        @Override
        public String toString() {
            return "RangePartitionProperty{" +
                    "partitionKey='" + partitionKey + '\'' +
                    ", condition=" + condition +
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

            RangePartitionProperty that = (RangePartitionProperty) o;

            if (!condition.equals(that.condition)) {
                return false;
            }
            if (!partitionKey.equals(that.partitionKey)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = partitionKey.hashCode();
            result = 31 * result + condition.hashCode();
            return result;
        }
    }
}
