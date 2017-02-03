/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core;

import org.wso2.siddhi.query.api.ExecutionPlan;

/**
 *
 */
public interface SiddhiManagerService {

    /**
     * Method to add stream definitions, partitions and queries of an execution plan
     *
     * @param executionPlan executionPlan which contains stream definitions,queries and partitions
     * @return executionPlanRuntime corresponding to the given executionPlan
     */
    ExecutionPlanRuntime createExecutionPlanRuntime(ExecutionPlan executionPlan);

    /**
     * Method to add execution plan in the form of a string. You can add valid set of Siddhi queries as a String to
     * this method and receive {@link ExecutionPlanRuntime} object representing the queries.
     *
     * @param executionPlan String representation of Siddhi queries
     * @return Execution Plan Runtime
     */
    ExecutionPlanRuntime createExecutionPlanRuntime(String executionPlan);

    /**
     * Method to validate provided {@link ExecutionPlan} object. Method will throw
     * {@link org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException} if submitted Execution Plan has
     * errors.
     *
     * @param executionPlan Execution plan to be validated.
     */
    void validateExecutionPlan(ExecutionPlan executionPlan);

    /**
     * Method to validate provided String representation of Execution Plan. Method will throw
     * {@link org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException} if submitted Siddhi queries have
     * errors.
     *
     * @param executionPlan execution plan
     */
    void validateExecutionPlan(String executionPlan);
}
