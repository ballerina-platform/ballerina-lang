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
package org.wso2.ballerinalang.programfile;


import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code WorkerInfo} represents a worker in Ballerina program file.
 *
 * @since 0.87
 */
@Deprecated
public class WorkerInfo implements AttributeInfoPool {

    private String workerName;
    private int workerNameCPIndex;

    private int wrkrDtChnlRefCPIndex = -1;
    private WorkerDataChannelInfo workerDataChannelInfoForForkJoin;

    public CodeAttributeInfo codeAttributeInfo = new CodeAttributeInfo();

    private ForkjoinInfo[] forkjoinInfos;
    private List<ForkjoinInfo> forkjoinInfoList = new ArrayList<>();

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    public WorkerInfo(int workerNameCPIndex, String workerName) {
        this.workerName = workerName;
        this.workerNameCPIndex = workerNameCPIndex;
        this.attributeInfoMap.put(AttributeInfo.Kind.CODE_ATTRIBUTE, codeAttributeInfo);
    }

    public String getWorkerName() {
        return workerName;
    }

    public int getWorkerNameCPIndex() {
        return workerNameCPIndex;
    }

    public AttributeInfo getAttributeInfo(AttributeInfo.Kind attributeKind) {
        return attributeInfoMap.get(attributeKind);
    }

    public void addAttributeInfo(AttributeInfo.Kind attributeKind, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeKind, attributeInfo);
    }

    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }

    public int getWrkrDtChnlRefCPIndex() {
        return wrkrDtChnlRefCPIndex;
    }

    public void setWrkrDtChnlRefCPIndex(int wrkrDtChnlRefCPIndex) {
        this.wrkrDtChnlRefCPIndex = wrkrDtChnlRefCPIndex;
    }

    public WorkerDataChannelInfo getWorkerDataChannelInfoForForkJoin() {
        return workerDataChannelInfoForForkJoin;
    }

    public void setWorkerDataChannelInfoForForkJoin(WorkerDataChannelInfo workerDataChannelInfoForForkJoin) {
        this.workerDataChannelInfoForForkJoin = workerDataChannelInfoForForkJoin;
    }

    public ForkjoinInfo[] getForkjoinInfos() {
        return forkjoinInfoList.toArray(new ForkjoinInfo[0]);
    }

    public void addForkJoinInfo(ForkjoinInfo forkjoinInfo) {
        forkjoinInfoList.add(forkjoinInfo);
    }

    public void setForkjoinInfos(ForkjoinInfo[] forkjoinInfos) {
        this.forkjoinInfos = forkjoinInfos;
    }
}
