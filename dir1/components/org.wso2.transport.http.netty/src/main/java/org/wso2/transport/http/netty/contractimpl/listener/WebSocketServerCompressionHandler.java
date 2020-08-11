/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtensionHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.DeflateFrameServerExtensionHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateServerExtensionHandshaker;

import static io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateServerExtensionHandshaker.MAX_WINDOW_SIZE;

/**
 * Extends {@code io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerExtensionHandler} to
 * handle the WebSocket Compression Extensions.
 */
public class WebSocketServerCompressionHandler extends WebSocketServerExtensionHandler {

    public WebSocketServerCompressionHandler() {
        super(new PerMessageDeflateServerExtensionHandshaker(),
              new PerMessageDeflateServerExtensionHandshaker(6, ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(),
                                                             MAX_WINDOW_SIZE, true, false),
              new PerMessageDeflateServerExtensionHandshaker(6, ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(),
                                                             MAX_WINDOW_SIZE, false, true),
              new PerMessageDeflateServerExtensionHandshaker(6, ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(),
                                                             MAX_WINDOW_SIZE, true, true),
              new DeflateFrameServerExtensionHandshaker());
    }

}
