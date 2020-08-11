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

package org.wso2.transport.http.netty.util.server.initializers.http2.expect100continue;

import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Settings;

import static io.netty.handler.logging.LogLevel.INFO;

/**
 * Represents the handler builder for 100-continue.
 */
public final class Abnormal100ContinueHandlerBuilder extends
                                                     AbstractHttp2ConnectionHandlerBuilder<Abnormal100ContinueHandler,
                                                             Abnormal100ContinueHandlerBuilder> {

    private static final Http2FrameLogger LOGGER = new Http2FrameLogger(INFO, Abnormal100ContinueHandler.class);

    Abnormal100ContinueHandlerBuilder() {
        frameLogger(LOGGER);
    }

    @Override
    protected Abnormal100ContinueHandler build() {
        return super.build();
    }

    @Override
    protected Abnormal100ContinueHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                               Http2Settings initialSettings) {
        Abnormal100ContinueHandler handler = new Abnormal100ContinueHandler(decoder, encoder, initialSettings);
        frameListener(handler);
        return handler;
    }
}
