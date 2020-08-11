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
 * Default implementation of the {@link FullHttpMessageFuture}.
 */
public class DefaultFullHttpMessageFuture implements FullHttpMessageFuture {

    private final HttpCarbonMessage httpCarbonMessage;
    private FullHttpMessageListener messageListener;
    private Exception error;

    DefaultFullHttpMessageFuture(HttpCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    @Override
    public void addListener(FullHttpMessageListener messageListener) {
        synchronized (httpCarbonMessage) {
            this.messageListener = messageListener;
            if (httpCarbonMessage.isLastHttpContentArrived()) {
                notifySuccess();
            } else if (error != null) {
                notifyFailure(error);
            }
        }
    }

    @Override
    public synchronized void removeListener() {
        messageListener = null;
    }

    /**
     * The notifySuccess() method can get invoked by both I/O thread and Worker thread. AddListener is the caller
     * method for worker thread and it is synchronized using the {@link HttpCarbonMessage}. I/O thread uses
     * setLastHttpContentArrived() method which is a synchronized caller method. Therefore notifySuccess() is thread
     * safe and does not required to be synchronized.
     */
    @Override
    public void notifySuccess() {
        if (messageListener != null) {
            FullHttpMessageListener tempListener = messageListener;
            removeListener();
            tempListener.onComplete(httpCarbonMessage);
        }
    }

    @Override
    public void notifyFailure(Exception error) {
        this.error = error;
        if (messageListener != null) {
            FullHttpMessageListener tempListener = messageListener;
            removeListener();
            tempListener.onError(error);
            this.error = null;
        }
    }
}
