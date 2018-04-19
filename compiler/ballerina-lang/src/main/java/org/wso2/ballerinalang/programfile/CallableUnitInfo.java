/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.programfile;


import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;
import org.wso2.ballerinalang.programfile.cpentries.WorkerInfoPool;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code CallableUnitInfo} contains common metadata of a Ballerina function/resource/action in the program file.
 *
 * @since 0.87
 */
public class CallableUnitInfo implements AttributeInfoPool, WorkerInfoPool {

    // Index to the PackageCPEntry
    public int pkgNameCPIndex;
    public int nameCPIndex;
    public int flags;
    public int attachedToTypeCPIndex = -1;

    public BType[] paramTypes;
    public BType[] retParamTypes;

    public int signatureCPIndex;

    public int requiredParamsCount;
    public int defaultableParamsCount;

    protected Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    // Key - data channel name
    private Map<String, WorkerDataChannelInfo> dataChannelInfoMap = new HashMap<>();

    public WorkerInfo defaultWorkerInfo;
    public Map<String, WorkerInfo> workerInfoMap = new HashMap<>();

    public WorkerInfo getWorkerInfo(String workerName) {
        return workerInfoMap.get(workerName);
    }

    public void addWorkerInfo(String workerName, WorkerInfo workerInfo) {
        workerInfoMap.put(workerName, workerInfo);
    }

    public WorkerInfo[] getWorkerInfoEntries() {
        return workerInfoMap.values().toArray(new WorkerInfo[0]);
    }

    @Override
    public AttributeInfo getAttributeInfo(AttributeInfo.Kind attributeKind) {
        return attributeInfoMap.get(attributeKind);
    }

    @Override
    public void addAttributeInfo(AttributeInfo.Kind attributeKind, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeKind, attributeInfo);
    }

    @Override
    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }

    @Override
    public void addWorkerDataChannelInfo(WorkerDataChannelInfo workerDataChannelInfo) {
        dataChannelInfoMap.put(workerDataChannelInfo.getChannelName(), workerDataChannelInfo);
    }

    @Override
    public WorkerDataChannelInfo getWorkerDataChannelInfo(String name) {
        return dataChannelInfoMap.get(name);
    }

    @Override
    public WorkerDataChannelInfo[] getWorkerDataChannelInfo() {
        return dataChannelInfoMap.values().toArray(new WorkerDataChannelInfo[0]);
    }

    @Override
    public Map<String, WorkerDataChannelInfo> getWorkerDataChannelMap() {
        return dataChannelInfoMap;
    }

}
