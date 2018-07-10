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
package org.ballerinalang.persistence.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the check points of given execution.
 *
 * @since 0.976.0
 */
public class PendingCheckpoints {

    private static Map<String, List<Integer>> checkpoints = new HashMap<>();

    public static void add(String instanceId, int ip) {
        List<Integer> instanceCPs = checkpoints.computeIfAbsent(instanceId, k -> new ArrayList<>());
        instanceCPs.add(ip);
    }

    public static boolean checkAndRemove(String instanceId, int ip) {
        List<Integer> instanceCPs = checkpoints.get(instanceId);
        return instanceCPs != null && instanceCPs.remove(ip) != null;
    }
}
