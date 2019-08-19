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
package org.wso2.transport.http.netty.contractimpl.listener.http2;


import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.CompressorHttp2ConnectionEncoder;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Settings;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.common.FrameLogger;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;

import static io.netty.handler.logging.LogLevel.TRACE;

/**
 * {@code Http2SourceConnectionHandlerBuilder} is used to build the HTTP2SourceConnectionHandler.
 */
public final class Http2SourceConnectionHandlerBuilder
        extends AbstractHttp2ConnectionHandlerBuilder
        <Http2SourceConnectionHandler, Http2SourceConnectionHandlerBuilder> {

    private String interfaceId;
    private ServerConnectorFuture serverConnectorFuture;
    private String serverName;
    private HttpServerChannelInitializer serverChannelInitializer;

    public Http2SourceConnectionHandlerBuilder(String interfaceId, ServerConnectorFuture serverConnectorFuture,
                                               String serverName,
                                               HttpServerChannelInitializer serverChannelInitializer) {
        this.interfaceId = interfaceId;
        this.serverConnectorFuture = serverConnectorFuture;
        this.serverName = serverName;
        this.serverChannelInitializer = serverChannelInitializer;
    }

    @Override
    public Http2SourceConnectionHandler build() {
        Http2Connection conn = new DefaultHttp2Connection(true);
        if (serverChannelInitializer.isHttpTraceLogEnabled()) {
            frameLogger(new FrameLogger(TRACE, Constants.TRACE_LOG_DOWNSTREAM));
        }
        connection(conn);
        Http2SourceConnectionHandler connectionHandler = super.build();
        if (connectionHandler != null) {
            return connectionHandler;
        }
        return null;
    }

    @Override
    public Http2SourceConnectionHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                              Http2Settings initialSettings) {
        Http2ConnectionEncoder compressEncoder = new CompressorHttp2ConnectionEncoder(encoder);
        Http2SourceConnectionHandler sourceConnectionHandler = new Http2SourceConnectionHandler(
                serverChannelInitializer, decoder, compressEncoder, initialSettings, interfaceId,
                serverConnectorFuture, serverName);
        frameListener(sourceConnectionHandler.getHttp2FrameListener());
        return sourceConnectionHandler;
    }
}
