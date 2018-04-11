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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveStates {

    private static Map<String, List<State>> states = new HashMap<>();

    public static void add(String instanceId, State state) {
        List<State> stateList = get(instanceId);
        if (stateList == null) {
            stateList = new ArrayList<>();
            states.put(instanceId, stateList);
        }
        stateList.add(state);
    }

    public static List<State> get(String instanceId) {
        return states.get(instanceId);
    }

    public static void remove(String instanceId) {
        states.remove(instanceId);
    }
}
