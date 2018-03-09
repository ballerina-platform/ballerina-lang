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
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Store
 */
public class Store extends BasicSingleInputStream implements InputStore {

    private static final long serialVersionUID = 1L;

    protected Store(String streamId) {
        super(streamId);
    }

    protected Store(String storeReferenceId, String storeId) {
        super(storeReferenceId, storeId);
    }

    public InputStore on(Expression onCondition, Within within, Expression per) {
        return new AggregationInputStore(this, onCondition, within, per);
    }

    public InputStore on(Expression onCondition) {
        return new ConditionInputStore(this, onCondition);
    }

    public InputStore on(Within within, Expression per) {
        return new AggregationInputStore(this, within, per);
    }

    @Override
    public String getStoreReferenceId() {
        return streamReferenceId;
    }

    @Override
    public String getStoreId() {
        return streamId;
    }

    @Override
    public String toString() {
        return "Store{" +
                "isInnerStream=" + isInnerStream +
                ", streamId='" + streamId + '\'' +
                ", streamReferenceId='" + streamReferenceId + '\'' +
                ", streamHandlers=" + streamHandlers +
                ", windowPosition=" + windowPosition +
                '}';
    }


}
