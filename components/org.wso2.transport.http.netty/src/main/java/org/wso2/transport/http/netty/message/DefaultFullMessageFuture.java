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
 * Default implementation of the {@link FullMessageFuture}.
 */
public class DefaultFullMessageFuture implements FullMessageFuture {

    private FullMessageListener messageListener;
    private HttpFullCarbonMessage httpFullCarbonMessage;
    private Exception error;

    @Override
    public void addListener(FullMessageListener messageListener) {
        this.messageListener = messageListener;
        if (httpFullCarbonMessage != null) {
            messageListener.onComplete(httpFullCarbonMessage);
        } else if (error != null) {
            messageListener.onError(error);
        }
    }

    @Override
    public void notifySuccess(HttpFullCarbonMessage httpFullCarbonMessage) {
        this.httpFullCarbonMessage = httpFullCarbonMessage;
        if (messageListener != null) {
            messageListener.onComplete(httpFullCarbonMessage);
            this.httpFullCarbonMessage = null;
        }
    }

    @Override
    public void notifyFailure(Exception error) {
        this.error = error;
        if (messageListener != null) {
            messageListener.onError(error);
            this.error = null;
        }
    }
}
