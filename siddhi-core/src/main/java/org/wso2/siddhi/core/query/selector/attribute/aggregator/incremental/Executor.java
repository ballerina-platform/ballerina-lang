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
package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.event.ComplexEventChunk;

public interface Executor {

    /**
     * Execute the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    void execute(ComplexEventChunk complexEventChunk);

    /**
     * Get next executor element in the execution chain. Executed event should be sent to next executor
     *
     * @return next executor
     */
    Executor getNextExecutor();

    /**
     * Set next executor element in execution chain
     *
     */
    void setNextExecutor(Executor executor);

    /**
     * Set as the last element of the execution chain
     *
     */
    void setToLast(Executor executor);

    /**
     * Clone a copy of processor
     *
     * @param key partition key
     * @return cloned processor
     */
    Executor cloneExecutor(String key);

}
