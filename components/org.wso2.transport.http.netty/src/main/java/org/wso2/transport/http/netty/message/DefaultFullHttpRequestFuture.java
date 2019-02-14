/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.message;

/**
 * Default implementation of the {@link FullHttpRequestFuture}.
 */
public class DefaultFullHttpRequestFuture implements FullHttpRequestFuture {

    private HttpCarbonMessage httpCarbonMessage;
    private FullHttpRequestListener messageListener;
    private Exception error;

    DefaultFullHttpRequestFuture(HttpCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    @Override
    public void addListener(FullHttpRequestListener messageListener) {
        this.messageListener = messageListener;
        if (httpCarbonMessage.isLastHttpContentArrived()) {
            notifySuccess();
        } else if (error != null) {
            notifyFailure(error);
        }
    }

    @Override
    public void removeListener() {
        messageListener = null;
    }

    @Override
    public void notifySuccess() {
        if (messageListener != null) {
            messageListener.onComplete();
            removeListener();
        }
    }

    @Override
    public void notifyFailure(Exception error) {
        this.error = error;
        if (messageListener != null) {
            messageListener.onError(error);
            this.error = null;
            removeListener();
        }
    }
}
