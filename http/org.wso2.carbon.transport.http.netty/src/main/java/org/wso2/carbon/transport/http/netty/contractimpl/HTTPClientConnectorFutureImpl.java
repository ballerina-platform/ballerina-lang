/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * HTTPClientConnectorFutureImpl
 */
public class HTTPClientConnectorFutureImpl implements HTTPClientConnectorFuture {

    private HTTPConnectorListener httpConnectorListener = null;

    @Override
    public void setHTTPConnectorListener(HTTPConnectorListener connectorListener) {
        this.httpConnectorListener = connectorListener;
    }

    @Override
    public void removeHTTPListener(HTTPConnectorListener connectorListener) {
        this.httpConnectorListener = null;
    }

    @Override
    public void notifyHTTPListener(HTTPCarbonMessage httpMessage) {
        httpConnectorListener.onMessage(httpMessage);
    }
}
