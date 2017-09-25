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
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@code WorkerDataChannelInfo} represents data channels used in Ballerina in order to communicate between workers.
 *
 * @since 0.90
 */
public class WorkerDataChannelInfo {

    private int sourceCPIndex;
    private String source;

    private int targetCPIndex;
    private String target;

    private int uniqueNameCPIndex;
    private String uniqueName;

    private int dataChannelRefIndex;

    private BlockingQueue<Object[]> channel;
    private BType[] types;
    private static final Logger log = LoggerFactory.getLogger(WorkerDataChannel.class);

    public WorkerDataChannelInfo(int sourceCPIndex, String source, int targetCPIndex, String target) {
        this.sourceCPIndex = sourceCPIndex;
        this.source = source;
        this.targetCPIndex = targetCPIndex;
        this.target = target;
        this.channel =  new LinkedBlockingQueue<>();
    }

    public void putData(Object[] data) {
        try {
            if (data != null) {
                channel.put(data);
            }
        } catch (InterruptedException e) {
            // Handle the error properly
            log.error("Error occurred when inserting data to the channel");
        }
    }

    public Object[] takeData() {
        Object[] data = null;
        try {
            data = channel.poll(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Handle the error properly
            log.error("Error occurred when taking data from the channel");
        }
        return data;
    }

    public String getChannelName() {
        return source + "->" + target;
    }

    public int getSourceCPIndex() {
        return sourceCPIndex;
    }

    public void setSourceCPIndex(int sourceCPIndex) {
        this.sourceCPIndex = sourceCPIndex;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getTargetCPIndex() {
        return targetCPIndex;
    }

    public void setTargetCPIndex(int targetCPIndex) {
        this.targetCPIndex = targetCPIndex;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public BType[] getTypes() {
        return types;
    }

    public void setTypes(BType[] types) {
        this.types = types;
    }

    public int getUniqueNameCPIndex() {
        return uniqueNameCPIndex;
    }

    public void setUniqueNameCPIndex(int uniqueNameCPIndex) {
        this.uniqueNameCPIndex = uniqueNameCPIndex;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public int getDataChannelRefIndex() {
        return dataChannelRefIndex;
    }

    public void setDataChannelRefIndex(int dataChannelRefIndex) {
        this.dataChannelRefIndex = dataChannelRefIndex;
    }
}
