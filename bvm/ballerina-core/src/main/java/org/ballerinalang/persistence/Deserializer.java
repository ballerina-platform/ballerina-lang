/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.persistence;

import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BRefType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class manages the deserialization functionality.
 *
 * @since 0.976.0
 */
public class Deserializer {

    // These temporary data structures will be used to temporary store deserialize objects to manage sharable objects.
    private Map<String, BRefType> tempRefTypes = new ConcurrentHashMap<>();

    private Map<String, WorkerExecutionContext> tempContexts = new ConcurrentHashMap<>();

    private Map<String, CallableWorkerResponseContext> tempRespContexts = new ConcurrentHashMap<>();

    public void cleanUpDeserializer() {
        tempRefTypes.clear();
        tempContexts.clear();
        tempRespContexts.clear();
    }

    public Map<String, BRefType> getTempRefTypes() {
        return tempRefTypes;
    }

    public Map<String, WorkerExecutionContext> getTempContexts() {
        return tempContexts;
    }

    public Map<String, CallableWorkerResponseContext> getTempRespContexts() {
        return tempRespContexts;
    }

}
