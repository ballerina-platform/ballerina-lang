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

package org.wso2.transport.http.netty.message;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.HttpPipeliningListener;

/**
 * Defines pipelining listener to be called in future.
 *
 * @since 6.0.228
 */
public class HttpPipeliningFuture {
    private static final Logger LOG = LoggerFactory.getLogger(HttpPipeliningFuture.class);
    private HttpPipeliningListener pipeliningListener;

    public void setPipeliningListener(HttpPipeliningListener pipeliningListener) {
        this.pipeliningListener = pipeliningListener;
    }

    public void notifyPipeliningListener(ChannelHandlerContext sourceContext) {
        if (this.pipeliningListener != null) {
            this.pipeliningListener.onLastHttpContentSent(sourceContext);
        } else {
            LOG.error("Http pipelining listener is not set");
        }
    }
}
