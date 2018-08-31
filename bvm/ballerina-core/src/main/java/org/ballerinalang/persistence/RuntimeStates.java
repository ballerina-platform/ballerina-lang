/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is used to manage the active @{@link SerializableState}s of the system.
 *
 * @since 0.981.1
 */
public class RuntimeStates {

    private static final Logger log = LoggerFactory.getLogger(RuntimeStates.class);

    private static Map<String, SerializableState> states = new ConcurrentHashMap<>();

    public static void add(SerializableState state) {
        if (states.containsKey(state.getId())) {
            log.error("Duplicate state is found for id : " + state.getId());
        }
        states.put(state.getId(), state);
    }

    public static void add(WorkerExecutionContext context, String stateId) {
        add(new SerializableState(context, stateId));
    }

    public static SerializableState get(String stateId) {
        return states.get(stateId);
    }

    public static void remove(String stateId) {
        states.remove(stateId);
    }
}
