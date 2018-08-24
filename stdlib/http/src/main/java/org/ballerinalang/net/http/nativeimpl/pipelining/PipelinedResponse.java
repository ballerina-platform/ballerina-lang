/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.nativeimpl.pipelining;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.DataContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Objects;

import static org.wso2.transport.http.netty.common.Constants.MEANINGFULLY_EQUAL;
import static org.wso2.transport.http.netty.common.Constants.RESPONSE_QUEUING_NOT_NEEDED;

/**
 * Represent a pipelined response. Response order can be determined based on the sequence number.
 *
 * @since
 */
public class PipelinedResponse implements Comparable<PipelinedResponse> {

    private final HttpCarbonMessage inboundRequestMsg;
    private final HttpCarbonMessage outboundResponseMsg;
    private DataContext dataContext;
    private BMap<String, BValue> outboundResponseStruct;
    private final int sequenceId; //Identifies the response order

    public PipelinedResponse(int sequenceId, HttpCarbonMessage inboundRequestMsg, HttpCarbonMessage outboundResponseMsg,
                             DataContext dataContext, BMap<String, BValue> outboundResponseStruct) {
        this.inboundRequestMsg = inboundRequestMsg;
        this.outboundResponseMsg = outboundResponseMsg;
        this.dataContext = dataContext;
        this.outboundResponseStruct = outboundResponseStruct;
        this.sequenceId = sequenceId;
    }

    public PipelinedResponse(int sequenceId, HttpCarbonMessage inboundRequestMsg,
                             HttpCarbonMessage outboundResponseMsg) {
        this.inboundRequestMsg = inboundRequestMsg;
        this.outboundResponseMsg = outboundResponseMsg;
        this.sequenceId = sequenceId;
    }

    int getSequenceId() {
        return sequenceId;
    }

    HttpCarbonMessage getOutboundResponseMsg() {
        return outboundResponseMsg;
    }

    HttpCarbonMessage getInboundRequestMsg() {
        return inboundRequestMsg;
    }

    DataContext getDataContext() {
        return dataContext;
    }

    BMap<String, BValue> getOutboundResponseStruct() {
        return outboundResponseStruct;
    }

    @Override
    public int compareTo(PipelinedResponse other) {
        return this.sequenceId - other.getSequenceId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this.sequenceId == RESPONSE_QUEUING_NOT_NEEDED) {
            return super.equals(obj);
        }
        return obj instanceof PipelinedResponse && compareTo((PipelinedResponse) obj) == MEANINGFULLY_EQUAL;
    }

    @Override
    public int hashCode() {
        if (this.sequenceId == RESPONSE_QUEUING_NOT_NEEDED) {
            return super.hashCode();
        }
        return Objects.hashCode(sequenceId);
    }
}
