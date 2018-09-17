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

package org.ballerinalang.net.http.nativeimpl.connection;

import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.DataContext;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.OutputStream;

import static org.ballerinalang.net.http.nativeimpl.connection.ResponseWriter.getResponseDataStreamer;
import static org.ballerinalang.net.http.nativeimpl.connection.ResponseWriter.sendResponseRobust;
import static org.ballerinalang.net.http.nativeimpl.connection.ResponseWriter.serializeDataSource;

/**
 * {@code {@link ConnectionAction}} represents a Abstract implementation of Native Ballerina Connection Function.
 *
 * @since 0.96
 */
public abstract class ConnectionAction implements NativeCallableUnit {

    void sendOutboundResponseRobust(DataContext dataContext, HttpCarbonMessage requestMessage,
                                    BMap<String, BValue> outboundResponseStruct,
                                    HttpCarbonMessage responseMessage) {
        sendResponseRobust(dataContext, requestMessage, outboundResponseStruct, responseMessage);
    }

    void setResponseConnectorListener(DataContext dataContext, HttpResponseFuture outResponseStatusFuture) {
        HttpConnectorListener outboundResStatusConnectorListener =
                new ResponseWriter.HttpResponseConnectorListener(dataContext);
        outResponseStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
    }

    void serializeMsgDataSource(BValue outboundMessageSource, BMap<String, BValue> entityStruct,
                                OutputStream messageOutputStream) {
        serializeDataSource(outboundMessageSource, entityStruct, messageOutputStream);
    }

    HttpMessageDataStreamer getMessageDataStreamer(HttpCarbonMessage outboundResponse) {
        return getResponseDataStreamer(outboundResponse);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
