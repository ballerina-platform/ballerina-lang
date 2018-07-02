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
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds mapping of data required for store native data.
 *
 * @since 0.976.0
 */
public class DataMapper {

    private Map<String, Map<NativeDataKey, List<BMap>>> nativeDataMappings = new HashMap<>();
    private Map<String, Map<LocalPropKey, List<WorkerExecutionContext>>> localPropMappings = new HashMap<>();

    public void mapNativeData(String stateId, NativeDataKey key, BMap bStruct) {
        Map<NativeDataKey, List<BMap>> stateNativeData = nativeDataMappings
                .computeIfAbsent(stateId, k -> new HashMap<>());

        List<BMap> bMaps = stateNativeData.computeIfAbsent(key, k -> new ArrayList<>());
        bMaps.add(bStruct);
    }

    public void mapLocalProp(String stateId, LocalPropKey key, WorkerExecutionContext context) {
        Map<LocalPropKey, List<WorkerExecutionContext>> stateLocalProps = localPropMappings
                .computeIfAbsent(stateId, k -> new HashMap<>());

        List<WorkerExecutionContext> contexts = stateLocalProps.computeIfAbsent(key, k -> new ArrayList<>());
        contexts.add(context);
    }
}
