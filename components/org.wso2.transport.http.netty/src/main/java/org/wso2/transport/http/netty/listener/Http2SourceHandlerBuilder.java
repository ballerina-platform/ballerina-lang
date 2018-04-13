/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.transport.http.netty.listener;


import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Settings;
import org.wso2.transport.http.netty.common.FrameLogger;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

import static io.netty.handler.logging.LogLevel.TRACE;

/**
 * {@code HTTP2SourceHandlerBuilder} is used to build the HTTP2SourceHandler.
 */
public final class Http2SourceHandlerBuilder
        extends AbstractHttp2ConnectionHandlerBuilder<Http2SourceHandler, Http2SourceHandlerBuilder> {

    private String interfaceId;
    private ServerConnectorFuture serverConnectorFuture;
    private String serverName;
    private HttpServerChannelInitializer serverChannelInitializer;

    public Http2SourceHandlerBuilder(String interfaceId, ServerConnectorFuture serverConnectorFuture,
                                     String serverName, HttpServerChannelInitializer serverChannelInitializer) {
        this.interfaceId = interfaceId;
        this.serverConnectorFuture = serverConnectorFuture;
        this.serverName = serverName;
        this.serverChannelInitializer = serverChannelInitializer;
    }

    @Override
    public Http2SourceHandler build() {
        Http2Connection conn = new DefaultHttp2Connection(true);
        if (serverChannelInitializer.isHttpTraceLogEnabled()) {
            frameLogger(new FrameLogger(TRACE, "tracelog.http.downstream"));
        }
        connection(conn);
        return super.build();
    }

    @Override
    protected Http2SourceHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                       Http2Settings initialSettings) {
        Http2SourceHandler handler = new Http2SourceHandler(
                decoder, encoder, initialSettings, interfaceId, connection(), serverConnectorFuture, serverName);
        frameListener(handler.getHttp2FrameListener());
        return handler;
    }
}
