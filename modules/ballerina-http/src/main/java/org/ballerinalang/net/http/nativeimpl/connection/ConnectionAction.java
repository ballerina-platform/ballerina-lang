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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code {@link ConnectionAction}} represents a Abstract implementation of Native Ballerina Connection Function.
 *
 * @since 0.96
 */
public abstract class ConnectionAction extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        BStruct responseStruct = (BStruct) getRefArgument(context, 1);
        HTTPCarbonMessage requestMessage = HttpUtil.getCarbonMsg(connectionStruct, null);

        HttpUtil.checkFunctionValidity(connectionStruct, requestMessage);
        HTTPCarbonMessage responseMessage = HttpUtil
                .getCarbonMsg(responseStruct, HttpUtil.createHttpCarbonMessage(false));

        return HttpUtil.prepareResponseAndSend(context, this, requestMessage, responseMessage,
                responseStruct);
    }
}
