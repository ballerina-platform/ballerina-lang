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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.http.HttpConstants.PACKAGE_BALLERINA_BUILTIN;

/**
 * {@code DataContext} is the wrapper to hold {@code Context} and {@code CallableUnitCallback}.
 */
public class DataContext {
    private final Strand strand;
    private final ObjectValue clientObj;
    private final ObjectValue requestObj;
    private final NonBlockingCallback callback;
    private HttpCarbonMessage correlatedMessage;

    public DataContext(Strand strand, ObjectValue clientObj, ObjectValue requestObj,
                       HttpCarbonMessage outboundRequestMsg) {
        this.strand = strand;
        this.clientObj = clientObj;
        this.requestObj = requestObj;
        this.correlatedMessage = outboundRequestMsg;
        this.callback = null;
    }

    public DataContext(Strand strand, NonBlockingCallback callback, ObjectValue clientObj,
                       ObjectValue requestObj,
                       HttpCarbonMessage outboundRequestMsg) {
        this.strand = strand;
        //TODO : NonBlockingCallback is used to handle non blocking call
        this.callback = callback;
        this.clientObj = clientObj;
        this.requestObj = requestObj;
        this.correlatedMessage = outboundRequestMsg;
    }

    public void notifyInboundResponseStatus(ObjectValue inboundResponse, ErrorValue httpConnectorError) {
        //Make the request associate with this response consumable again so that it can be reused.
        if (inboundResponse != null) {
            getCallback().setReturnValues(inboundResponse);
        } else if (httpConnectorError != null) {
            getCallback().setReturnValues(httpConnectorError);
        } else {
            MapValue<String, Object> err = BallerinaValues.createRecordValue(PACKAGE_BALLERINA_BUILTIN,
                                                                             STRUCT_GENERIC_ERROR);
            getCallback().setReturnValues(err);
        }
        getCallback().notifySuccess();
    }

    public void notifyOutboundResponseStatus(ErrorValue httpConnectorError) {
        if (httpConnectorError == null) {
            getCallback().setReturnValues(null);
        } else {
            getCallback().setReturnValues(httpConnectorError);
        }
        getCallback().notifySuccess();
    }

    public HttpCarbonMessage getOutboundRequest() {
        return correlatedMessage;
    }

    public ObjectValue getClientObj() {
        return clientObj;
    }

    public ObjectValue getRequestObj() {
        return requestObj;
    }

    public Strand getStrand() {
        return strand;
    }

    public NonBlockingCallback getCallback() {
        return callback;
    }
}
