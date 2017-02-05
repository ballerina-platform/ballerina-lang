/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.transport.http.netty.listener.http2;


import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Settings;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import static io.netty.handler.logging.LogLevel.DEBUG;

/**
 * {@code HTTP2SourceHandlerBuilder} is used to build the http2 response handler with frame listener and connection
 * manager.
 */
public final class HTTP2SourceHandlerBuilder
        extends AbstractHttp2ConnectionHandlerBuilder<HTTP2SourceHandler, HTTP2SourceHandlerBuilder> {

    private ConnectionManager connectionManager;
    private ListenerConfiguration listenerConfiguration;
    private static final Http2FrameLogger logger = new Http2FrameLogger(DEBUG, HTTP2SourceHandler.class);

    public HTTP2SourceHandlerBuilder(ConnectionManager connectionManager, ListenerConfiguration listenerConfiguration) {
        frameLogger(logger);
        this.listenerConfiguration = listenerConfiguration;
        this.connectionManager = connectionManager;
    }

    @Override
    public HTTP2SourceHandler build() {
        return super.build();
    }

    @Override
    protected HTTP2SourceHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                       Http2Settings initialSettings) {
        HTTP2SourceHandler handler = new HTTP2SourceHandler(decoder, encoder, initialSettings, connectionManager,
                listenerConfiguration);
        frameListener(handler);
        connection(new DefaultHttp2Connection(true));
        return handler;
    }
}
