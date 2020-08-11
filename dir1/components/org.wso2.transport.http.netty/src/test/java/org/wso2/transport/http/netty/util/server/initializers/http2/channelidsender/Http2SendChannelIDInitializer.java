/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.util.server.initializers.http2.channelidsender;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import org.wso2.transport.http.netty.util.server.initializers.http2.Http2ServerInitializer;

/**
 * Handler responsible for sending out the channel ID.
 */
public class Http2SendChannelIDInitializer extends Http2ServerInitializer {
    @Override
    protected ChannelHandler getBusinessLogicHandler() {
        return new H2ChannelIdHandlerBuilder().build();
    }

    @Override
    protected Http2ConnectionHandler getBusinessLogicHandlerViaBuiler() {
        return new H2ChannelIdHandlerBuilder().build();
    }
}
