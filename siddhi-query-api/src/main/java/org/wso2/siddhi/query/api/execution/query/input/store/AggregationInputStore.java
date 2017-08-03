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

package org.wso2.siddhi.query.api.execution.query.input.store;

import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Aggregation Input Store
 */
public class AggregationInputStore extends ConditionInputStore {

    private final Within within;
    private final Expression per;

    protected AggregationInputStore(Store store, Expression onCondition, Within within, Expression per) {
        super(store, onCondition);
        this.within = within;
        this.per = per;
    }

    protected AggregationInputStore(Store store, Within within, Expression per) {
        super(store, null);
        this.within = within;
        this.per = per;
    }

    public Within getWithin() {
        return within;
    }

    public Expression getPer() {
        return per;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AggregationInputStore that = (AggregationInputStore) o;

        if (within != null ? !within.equals(that.within) : that.within != null) {
            return false;
        }
        return per != null ? per.equals(that.per) : that.per == null;
    }

    @Override
    public int hashCode() {
        int result = within != null ? within.hashCode() : 0;
        result = 31 * result + (per != null ? per.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AggregationInputStore{" +
                "store=" + store +
                ", onCondition=" + onCondition +
                ", within=" + within +
                ", per=" + per +
                '}';
    }
}
