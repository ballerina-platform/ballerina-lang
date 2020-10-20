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

import io.ballerina.runtime.api.BErrorCreator;
import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.OutputStream;

import static org.ballerinalang.net.http.HttpUtil.extractEntity;

/**
 * {@code PushPromisedResponse} is the extern function to respond back the client with Server Push response.
 */
public class PushPromisedResponse extends ConnectionAction {

    public static Object pushPromisedResponse(BalEnv env, BObject connectionObj, BObject pushPromiseObj,
                                              BObject outboundResponseObj) {
        HttpCarbonMessage inboundRequestMsg = HttpUtil.getCarbonMsg(connectionObj, null);
        Strand strand = Scheduler.getStrand();
        DataContext dataContext = new DataContext(strand, env.markAsync(), inboundRequestMsg);
        HttpUtil.serverConnectionStructCheck(inboundRequestMsg);

        Http2PushPromise http2PushPromise = HttpUtil.getPushPromise(pushPromiseObj, null);
        if (http2PushPromise == null) {
            throw BErrorCreator.createError(BStringUtils.fromString(("invalid push promise")));
        }

        HttpCarbonMessage outboundResponseMsg = HttpUtil
                .getCarbonMsg(outboundResponseObj, HttpUtil.createHttpCarbonMessage(false));
        HttpUtil.prepareOutboundResponse(connectionObj, inboundRequestMsg, outboundResponseMsg, outboundResponseObj);
        pushResponseRobust(dataContext, inboundRequestMsg, outboundResponseObj, outboundResponseMsg,
                http2PushPromise);
        return null;
    }

    private static void pushResponseRobust(DataContext dataContext, HttpCarbonMessage requestMessage,
                                    BObject outboundResponseObj, HttpCarbonMessage responseMessage,
                                    Http2PushPromise http2PushPromise) {
        HttpResponseFuture outboundRespStatusFuture =
                HttpUtil.pushResponse(requestMessage, responseMessage, http2PushPromise);
        HttpMessageDataStreamer outboundMsgDataStreamer = getMessageDataStreamer(responseMessage);
        HttpConnectorListener outboundResStatusConnectorListener =
                new ResponseWriter.HttpResponseConnectorListener(dataContext, outboundMsgDataStreamer);
        outboundRespStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
        OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();

        BObject entityObj = extractEntity(outboundResponseObj);
        if (entityObj != null) {
            Object outboundMessageSource = EntityBodyHandler.getMessageDataSource(entityObj);
            serializeMsgDataSource(outboundMessageSource, entityObj, messageOutputStream);
        }
    }
}
