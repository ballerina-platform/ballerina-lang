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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.values.BStruct;

import static org.ballerinalang.net.http.HttpConstants.HTTP_CONNECTOR_ERROR;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;

/**
 * {@code DataContext} is the wrapper to hold {@code Context} and {@code CallableUnitCallback}.
 */
public class DataContext {
    public Context context;
    public CallableUnitCallback callback;

    public DataContext(Context context, CallableUnitCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void notifyReply(BStruct response, BStruct httpConnectorError) {
        if (response != null) {
            context.setReturnValues(response);
        } else if (httpConnectorError != null) {
            context.setReturnValues(httpConnectorError);
        } else {
            BStruct err = BLangConnectorSPIUtil.createBStruct(context, PROTOCOL_PACKAGE_HTTP, HTTP_CONNECTOR_ERROR,
                                                              "HttpClient failed");
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
}
