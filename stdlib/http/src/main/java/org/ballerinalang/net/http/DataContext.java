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

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * {@code DataContext} is the wrapper to hold {@code Context} and {@code CallableUnitCallback}.
 */
public class DataContext {
    private final Strand strand;
    private final ObjectValue clientObj;
    private final ObjectValue requestObj;
    //    public Context context;
//    public CallableUnitCallback callback;
    private HttpCarbonMessage correlatedMessage;

//    public DataContext(Context context, CallableUnitCallback callback, HttpCarbonMessage correlatedMessage) {
//        this.context = context;
//        this.callback = callback;
//        this.correlatedMessage = correlatedMessage;
//    }

    public DataContext(Strand strand, ObjectValue clientObj, ObjectValue requestObj, HttpCarbonMessage outboundRequestMsg) {
        this.strand = strand;
        this.clientObj = clientObj;
        this.requestObj = requestObj;
        this.correlatedMessage = outboundRequestMsg;
    }

//    public void notifyInboundResponseStatus(BMap<String, BValue> inboundResponse, BError httpConnectorError) {
//        //Make the request associate with this response consumable again so that it can be reused.
//        if (inboundResponse != null) {
//            context.setReturnValues(inboundResponse);
//        } else if (httpConnectorError != null) {
//            context.setReturnValues(httpConnectorError);
//        } else {
//            BMap<String, BValue> err = BLangConnectorSPIUtil.createBStruct(context, PACKAGE_BALLERINA_BUILTIN,
//                    STRUCT_GENERIC_ERROR, "HttpClient failed");
//            context.setReturnValues(err);
//        }
//        callback.notifySuccess();
//    }

//    public void notifyOutboundResponseStatus(BError httpConnectorError) {
//        if (httpConnectorError == null) {
//            context.setReturnValues();
//        } else {
//            context.setReturnValues(httpConnectorError);
//        }
//        callback.notifySuccess();
//    }

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
}
