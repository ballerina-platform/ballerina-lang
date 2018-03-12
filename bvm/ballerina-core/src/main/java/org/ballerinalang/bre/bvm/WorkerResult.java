/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.values.BRefValueArray;

/**
 * This class holds data about the result of a given worker.
 * 
 * @since 0.965.0
 */
public class WorkerResult {
    private String workerName;
    private BRefValueArray result;

    public WorkerResult(String workerName, BRefValueArray result) {
        this.workerName = workerName;
        this.result = result;
    }

    public String getWorkerName() {
        return workerName;
    }

    public BRefValueArray getResult() {
        return result;
    }
}
