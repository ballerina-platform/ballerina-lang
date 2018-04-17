/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.bre.bvm.persistency.reftypes.DataMapper;
import org.ballerinalang.model.values.BStruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StateStore {

    private static StateStore stateStore;

    private static DataMapper dataMapper;

    public static Map<String, Map<String, BStruct>> tempBStructs = new HashMap<>();

    public static StateStore getInstance() {
        if (stateStore == null) {
            stateStore = new InMemoryStore();
        }
        return stateStore;
    }

    public static void setStateStore(StateStore stateStore) {
        StateStore.stateStore = stateStore;
    }

    public abstract void persistState(String instanceId, State state);

    public abstract void persistFaildState(String instanceId, State state);

    public abstract List<State> getStates();

    public abstract List<State> getStates(String instanceId);

    public abstract List<State> getFailedStates(String instanceId);

    public abstract void removeFailedStates(String instanceId);

    public static DataMapper getDataMapper() {
        if (dataMapper == null) {
            dataMapper = new DataMapper();
        }
        return dataMapper;
    }
}
