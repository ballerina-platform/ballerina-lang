/*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.codegen;

import org.ballerinalang.runtime.worker.WorkerDataChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code WorkerInfo} represents a worker in Ballerina program file.
 *
 * @since 0.87
 */
public class WorkerInfo {

    private String workerName;
    private int workerNameCPIndex;
    private int workerEndIP;

    private CallableUnitInfo callableUnitInfo;

    private WorkerDataChannel workerDataChannelForForkJoin;

    private CodeAttributeInfo codeAttributeInfo;

    private Map<String, AttributeInfo> attributeInfoMap = new HashMap<>();

    public WorkerInfo(String workerName, int workerNameCPIndex) {
        this.workerName = workerName;
        this.workerNameCPIndex = workerNameCPIndex;
        this.codeAttributeInfo = new CodeAttributeInfo();
    }

    public String getWorkerName() {
        return workerName;
    }

    public CodeAttributeInfo getCodeAttributeInfo() {
        return codeAttributeInfo;
    }

    public void setCodeAttributeInfo(CodeAttributeInfo codeAttributeInfo) {
        this.codeAttributeInfo = codeAttributeInfo;
    }

    public AttributeInfo getAttributeInfo(String attributeName) {
        return attributeInfoMap.get(attributeName);
    }

    public void addAttributeInfo(String attributeName, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeName, attributeInfo);
    }

    public WorkerDataChannel getWorkerDataChannelForForkJoin() {
        return workerDataChannelForForkJoin;
    }

    public void setWorkerDataChannelForForkJoin(WorkerDataChannel workerDataChannelForForkJoin) {
        this.workerDataChannelForForkJoin = workerDataChannelForForkJoin;
    }

    public int getWorkerEndIP() {
        return workerEndIP;
    }

    public void setWorkerEndIP(int workerEndIP) {
        this.workerEndIP = workerEndIP;
    }
}
