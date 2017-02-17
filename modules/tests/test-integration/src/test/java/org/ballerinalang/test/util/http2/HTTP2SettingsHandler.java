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
package org.ballerinalang.test.util.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.concurrent.TimeUnit;

/**
 * Reads the first {@link Http2Settings} object and notifies a {@link io.netty.channel.ChannelPromise}
 */
public class HTTP2SettingsHandler extends SimpleChannelInboundHandler<Http2Settings> {
    private ChannelPromise promise;

    /**
     * Create new instance
     *
     * @param promise Promise object used to notify when first settings are received
     */
    public HTTP2SettingsHandler(ChannelPromise promise) {
        this.promise = promise;
    }

    /**
     * Wait for this handler to be added after the upgrade to HTTP/2, and for initial preface
     * handshake to complete.
     *
     * @param timeout Time to wait
     * @param unit    {@link TimeUnit} for {@code HTTP2_RESPONSE_TIME_OUT}
     * @throws Exception if HTTP2_RESPONSE_TIME_OUT or other failure occurs
     */
    public void awaitSettings(long timeout, TimeUnit unit) throws Exception {
        if (!promise.awaitUninterruptibly(timeout, unit)) {
            throw new IllegalStateException("Timed out waiting for settings");
        }
        if (!promise.isSuccess()) {
            throw new RuntimeException(promise.cause());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2Settings msg) throws Exception {
        promise.setSuccess();

        // Only care about the first settings message
        ctx.pipeline().remove(this);
    }
}
