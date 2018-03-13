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

package org.wso2.transport.http.netty.sender;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.wso2.transport.http.netty.common.Constants;

/**
 * A future to check the connection availability.
 */
public class ConnectionAvailabilityFuture {

    private ChannelFuture channelFuture;
    private boolean isSSLEnabled = false;
    private ConnectionAvailabilityListener listener = null;
    private String protocol;
    private boolean socketAvailable = false;
    private boolean isFailure;

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;

        channelFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (isValidChannel(channelFuture)) {
                    socketAvailable = true;
                    if (listener != null && !isSSLEnabled) {
                        notifySuccess(Constants.HTTP_SCHEME);
                    }
                } else {
                    notifyFailure();
                }
            }

            private boolean isValidChannel(ChannelFuture channelFuture) {
                return (channelFuture.isDone() && channelFuture.isSuccess());
            }
        });
    }

    public void setSSLEnabled(boolean sslEnabled) {
        isSSLEnabled = sslEnabled;
    }

    public void notifySuccess(String protocol) {
        this.protocol = protocol;
        if (listener != null) {
            listener.onSuccess(protocol, channelFuture);
        }
    }

    public void notifyFailure() {
        isFailure = true;
        if (listener != null) {
            listener.onFailure(channelFuture);
        }
    }

    public void setListener(ConnectionAvailabilityListener listener) {
        this.listener = listener;
        if (protocol != null) {
            notifySuccess(protocol);
        } else if (!isSSLEnabled && socketAvailable) {
            notifySuccess(Constants.HTTP_SCHEME);
        } else if (isFailure) {
            notifyFailure();
        }
    }
}


