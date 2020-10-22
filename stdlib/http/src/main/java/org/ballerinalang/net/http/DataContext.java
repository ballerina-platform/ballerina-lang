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

package org.ballerinalang.net.http;

import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.BalFuture;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Strand;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.STRUCT_GENERIC_ERROR;

/**
 * {@code DataContext} is the wrapper to hold {@code Context} and {@code CallableUnitCallback}.
 */
public class DataContext {
    private Strand strand;
    private HttpClientConnector clientConnector;
    private BObject requestObj;
    private BalFuture future;
    private HttpCarbonMessage correlatedMessage;

    public DataContext(Strand strand, HttpClientConnector clientConnector, BalFuture future,
                       BObject requestObj, HttpCarbonMessage outboundRequestMsg) {
        this.strand = strand;
        this.future = future;
        this.clientConnector = clientConnector;
        this.requestObj = requestObj;
        this.correlatedMessage = outboundRequestMsg;
    }

    public DataContext(Strand strand, BalFuture future, HttpCarbonMessage inboundRequestMsg) {
        this.strand = strand;
        this.future = future;
        this.clientConnector = null;
        this.requestObj = null;
        this.correlatedMessage = inboundRequestMsg;
    }

    public void notifyInboundResponseStatus(BObject inboundResponse, BError httpConnectorError) {
        //Make the request associate with this response consumable again so that it can be reused.
        if (inboundResponse != null) {
            getFuture().complete(inboundResponse);
        } else if (httpConnectorError != null) {
            getFuture().complete(httpConnectorError);
        } else {
            BMap<BString, Object> err = BValueCreator.createRecordValue(BALLERINA_BUILTIN_PKG_ID,
                                                                        STRUCT_GENERIC_ERROR);
            getFuture().complete(err);
        }
    }

    public void notifyOutboundResponseStatus(BError httpConnectorError) {
        getFuture().complete(httpConnectorError);
    }

    public HttpCarbonMessage getOutboundRequest() {
        return correlatedMessage;
    }

    public HttpClientConnector getClientConnector() {
        return clientConnector;
    }

    public BObject getRequestObj() {
        return requestObj;
    }

    public Strand getStrand() {
        return strand;
    }

    public BalFuture getFuture() {
        return future;
    }
}
