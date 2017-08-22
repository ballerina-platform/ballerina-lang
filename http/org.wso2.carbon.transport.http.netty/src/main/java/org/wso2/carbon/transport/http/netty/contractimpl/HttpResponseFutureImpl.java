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

import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Implementation of the client connector future.
 */
public class HttpResponseFutureImpl implements HttpResponseFuture {

    private HttpConnectorListener httpConnectorListener = null;
    private HTTPCarbonMessage httpCarbonMessage;
    private Throwable throwable;

    @Override
    public void setHttpConnectorListener(HttpConnectorListener connectorListener) {
        this.httpConnectorListener = connectorListener;
        if (httpCarbonMessage != null) {
            notifyHttpListener(httpCarbonMessage);
            httpCarbonMessage = null;
        }
        if (this.throwable != null) {
            notifyHttpListener(this.throwable);
            this.throwable =  null;
        }
    }

    @Override
    public void removeHttpListener() {
        this.httpConnectorListener = null;
    }

    @Override
    public void notifyHttpListener(HTTPCarbonMessage httpMessage) {
        if (httpConnectorListener == null) {
            httpCarbonMessage = httpMessage;
        } else {
            httpConnectorListener.onMessage(httpMessage);
        }
    }

    @Override
    public void notifyHttpListener(Throwable throwable) {
        if (httpConnectorListener == null) {
            this.throwable = throwable;
        } else {
            httpConnectorListener.onError(throwable);
        }
    }
}
