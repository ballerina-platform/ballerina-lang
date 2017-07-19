/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.aggregation;

import org.wso2.siddhi.query.api.expression.Expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * With time period for Incremental Aggregation.
 */
public class Within implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<Expression> timeRange = new ArrayList<>(2);

    private Within(Expression start, Expression end) {
        timeRange.add(start);
        timeRange.add(end);
    }

    private Within(Expression pattern) {
        timeRange.add(pattern);
    }

    public static Within within(Expression start, Expression end) {
        return new Within(start, end);
    }

    public static Within within(Expression pattern) {
        return new Within(pattern);
    }

    public List<Expression> getTimeRange() {
        return timeRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Within within = (Within) o;

        return timeRange != null ? timeRange.equals(within.timeRange) : within.timeRange == null;
    }

    @Override
    public int hashCode() {
        return timeRange != null ? timeRange.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Within{" +
                "timeRange=" + timeRange +
                '}';
    }
}
