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

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Condition Input Store
 */
public class ConditionInputStore implements InputStore {

    protected final Store store;
    protected Expression onCondition = null;

    protected ConditionInputStore(Store store, Expression onCondition) {
        this.store = store;
        this.onCondition = onCondition;
    }

    @Override
    public String getStoreReferenceId() {
        return store.getStoreReferenceId();
    }

    @Override
    public String getStoreId() {
        return store.getStoreId();
    }

    public Store getStore() {
        return store;
    }

    public Expression getOnCondition() {
        return onCondition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConditionInputStore that = (ConditionInputStore) o;

        if (store != null ? !store.equals(that.store) : that.store != null) {
            return false;
        }
        return onCondition != null ? onCondition.equals(that.onCondition) : that.onCondition == null;
    }

    @Override
    public int hashCode() {
        int result = store != null ? store.hashCode() : 0;
        result = 31 * result + (onCondition != null ? onCondition.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConditionInputStore{" +
                "store=" + store +
                ", onCondition=" + onCondition +
                '}';
    }
}
