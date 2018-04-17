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
package org.ballerinalang.bre.bvm.persistency.reftypes;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataMapper {

    private Map<String, Map<NativeDataKey, List<BStruct>>> nativeDataMappings = new HashMap<>();
    private Map<String, Map<LocalPropKey, List<WorkerExecutionContext>>> localPropMappings = new HashMap<>();

    public void mapNativeData(String stateId, NativeDataKey key, BStruct bStruct) {
        Map<NativeDataKey, List<BStruct>> stateNativeData = nativeDataMappings.get(stateId);
        if (stateNativeData == null) {
            stateNativeData = new HashMap<>();
            nativeDataMappings.put(stateId, stateNativeData);
        }

        List<BStruct> bStructs = stateNativeData.get(key);
        if (bStructs == null) {
            bStructs = new ArrayList<BStruct>();
            stateNativeData.put(key, bStructs);
        }
        bStructs.add(bStruct);
    }

    public List<BStruct> getNativeDataMappings(String stateId, NativeDataKey key) {
        Map<NativeDataKey, List<BStruct>> stateNativeData = nativeDataMappings.get(stateId);
        if (stateNativeData == null) {
            return null;
        }
        return stateNativeData.get(key);
    }

    public void mapLocalProp(String stateId, LocalPropKey key, WorkerExecutionContext context) {
        Map<LocalPropKey, List<WorkerExecutionContext>> stateLocalProps = localPropMappings.get(stateId);
        if (stateLocalProps == null) {
            stateLocalProps = new HashMap<>();
            localPropMappings.put(stateId, stateLocalProps);
        }

        List<WorkerExecutionContext> contexts = stateLocalProps.get(key);
        if (contexts == null) {
            contexts = new ArrayList<WorkerExecutionContext>();
            stateLocalProps.put(key, contexts);
        }
        contexts.add(context);
    }

    public List<WorkerExecutionContext> getLocalPropMappings(String stateId, LocalPropKey key) {
        Map<LocalPropKey, List<WorkerExecutionContext>> stateLocalProps = localPropMappings.get(stateId);
        if (stateLocalProps == null) {
            return null;
        }
        return stateLocalProps.get(key);
    }

    public void inject(String dataIdentifier, Object data) {

    }
}
