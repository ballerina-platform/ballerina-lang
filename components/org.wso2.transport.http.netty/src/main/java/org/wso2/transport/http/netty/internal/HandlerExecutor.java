/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * The class that is responsible for engaging all the interceptors.
 */
public class HandlerExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(HandlerExecutor.class);
    private Map<String, MessagingHandler> handlers = new HashMap<>();

    public void executeAtSourceConnectionInitiation(String metadata) {
        try {
            handlers.forEach((k, v) -> v.invokeAtSourceConnectionInitiation(metadata));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Source connection initiation ", e);
        }
    }

    public void executeAtSourceConnectionTermination(String metadata) {
        try {
            handlers.forEach((k, v) -> v.invokeAtSourceConnectionTermination(metadata));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Source connection termination ", e);
        }
    }

    public void executeAtSourceRequestReceiving(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtSourceRequestReceiving(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Source request receiving ", e);
        }
    }

    public void executeAtSourceRequestSending(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtSourceRequestSending(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Source request sending ", e);
        }
    }

    public void executeAtTargetRequestReceiving(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtTargetRequestReceiving(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Target request receiving ", e);
        }
    }

    public void executeAtTargetRequestSending(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtTargetRequestSending(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Target request sending ", e);
        }
    }

    public void executeAtTargetResponseReceiving(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtTargetResponseReceiving(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Target response receiving ", e);
        }
    }

    public void executeAtTargetResponseSending(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtTargetResponseSending(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Target response sending ", e);
        }
    }

    public void executeAtSourceResponseReceiving(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtSourceResponseReceiving(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Source response receiving ", e);
        }
    }

    public void executeAtSourceResponseSending(HttpCarbonMessage carbonMessage) {
        try {
            handlers.forEach((k, v) -> v.invokeAtSourceResponseSending(carbonMessage));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Source response sending ", e);
        }
    }

    public void executeAtTargetConnectionInitiation(String metadata) {
        try {
            handlers.forEach((k, v) -> v.invokeAtTargetConnectionInitiation(metadata));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Target connection initiation ", e);
        }
    }

    public void executeAtTargetConnectionTermination(String metadata) {
        try {
            handlers.forEach((k, v) -> v.invokeAtTargetConnectionTermination(metadata));
        } catch (Exception e) {
            LOG.error("Error while executing handler at Target connection termination ", e);
        }
    }

    public void addHandler(MessagingHandler messagingHandler) {
        String handlerName = messagingHandler.handlerName();
        handlers.put(handlerName, messagingHandler);
        LOG.info("A new handler named {} is added to the Handler Executor", handlerName);
    }

    public void removeHandler(MessagingHandler messagingHandler) {
        String handlerName = messagingHandler.handlerName();
        handlers.remove(handlerName);
        LOG.info("Handler named {} is removed from the Handler Executor", handlerName);
    }
}
