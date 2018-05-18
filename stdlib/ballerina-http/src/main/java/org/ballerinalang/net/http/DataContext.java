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

import io.netty.handler.codec.http.DefaultLastHttpContent;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.net.http.HttpConstants.PACKAGE_BALLERINA_BUILTIN;
import static org.ballerinalang.net.http.HttpConstants.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;

/**
 * {@code DataContext} is the wrapper to hold {@code Context} and {@code CallableUnitCallback}.
 */
public class DataContext {
    public Context context;
    public CallableUnitCallback callback;
    private HTTPCarbonMessage correlatedMessage;

    public DataContext(Context context, CallableUnitCallback callback, HTTPCarbonMessage correlatedMessage) {
        this.context = context;
        this.callback = callback;
        this.correlatedMessage = correlatedMessage;
    }

    public void notifyReply(BStruct response, BStruct httpConnectorError) {
        //Make the request associate with this response consumable again so that it can be reused.
        if (correlatedMessage != null) { //Null check is needed because of http2 scenarios
            BStruct requestStruct = ((BStruct) context.getNullableRefArgument(1));
            if (requestStruct != null) {
                BStruct entityStruct = extractEntity(requestStruct);
                if (entityStruct != null) {
                    MessageDataSource messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
                    if (messageDataSource == null && EntityBodyHandler.getByteChannel(entityStruct) == null) {
                        correlatedMessage.addHttpContent(new DefaultLastHttpContent());
                    } else {
                        correlatedMessage.waitAndReleaseAllEntities();
                    }
                } else {
                    correlatedMessage.addHttpContent(new DefaultLastHttpContent());
                }
            }
        }
        if (response != null) {
            context.setReturnValues(response);
        } else if (httpConnectorError != null) {
            context.setReturnValues(httpConnectorError);
        } else {
            BStruct err = BLangConnectorSPIUtil.createBStruct(context, PACKAGE_BALLERINA_BUILTIN,
                    STRUCT_GENERIC_ERROR, "HttpClient failed");
            context.setReturnValues(err);
        }
        callback.notifySuccess();
    }

    public void notifyOutboundResponseStatus(BStruct httpConnectorError) {
        if (httpConnectorError == null) {
            context.setReturnValues();
        } else {
            context.setReturnValues(httpConnectorError);
        }
        callback.notifySuccess();
    }

    public HTTPCarbonMessage getOutboundRequest() {
        return correlatedMessage;
    }
}
