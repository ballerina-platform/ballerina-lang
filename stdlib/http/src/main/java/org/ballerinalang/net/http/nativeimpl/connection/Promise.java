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

import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * {@code Promise} is the extern function to respond back to the client with a PUSH_PROMISE frame.
 */
public class Promise extends ConnectionAction {
    public static Object promise(BalEnv env, BObject connectionObj, BObject pushPromiseObj) {
        HttpCarbonMessage inboundRequestMsg = HttpUtil.getCarbonMsg(connectionObj, null);
        Strand strand = Scheduler.getStrand();
        DataContext dataContext = new DataContext(strand, env.markAsync(), inboundRequestMsg);
        HttpUtil.serverConnectionStructCheck(inboundRequestMsg);

        Http2PushPromise http2PushPromise = HttpUtil.getPushPromise(pushPromiseObj,
                HttpUtil.createHttpPushPromise(pushPromiseObj));
        HttpResponseFuture outboundRespStatusFuture = HttpUtil.pushPromise(inboundRequestMsg, http2PushPromise);
        setResponseConnectorListener(dataContext, outboundRespStatusFuture);
        return null;
    }
}
