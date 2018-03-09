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
package org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit;

/**
 * Rate limiting of query output and producing state snapshot as output.
 */
public class SnapshotOutputRate extends OutputRate {

    private Long value;
    private OutputRate.Type type = OutputRate.Type.ALL;

    public SnapshotOutputRate(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public OutputRate.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "SnapshotOutputRate{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SnapshotOutputRate)) {
            return false;
        }

        SnapshotOutputRate that = (SnapshotOutputRate) o;

        if (type != that.type) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
