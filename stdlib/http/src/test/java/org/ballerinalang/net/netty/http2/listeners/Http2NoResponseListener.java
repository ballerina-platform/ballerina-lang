/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.ballerinalang.net.netty.http2.listeners;

import org.ballerinalang.net.netty.contract.HttpConnectorListener;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code Http2NoResponseListener} is a HttpConnectorListener which receives messages and does nothing with it.
 */
public class Http2NoResponseListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2NoResponseListener.class);

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        LOG.info("Message received");
    }

    @Override
    public void onError(Throwable throwable) {
    }
}
