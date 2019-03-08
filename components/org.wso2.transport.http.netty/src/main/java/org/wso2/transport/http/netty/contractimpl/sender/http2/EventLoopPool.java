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

package org.wso2.transport.http.netty.contractimpl.sender.http2;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Per pool for eventloops.
 *
 * @since 6.0.273
 */
class EventLoopPool {

    private Map<String, PerRouteConnectionPool> perRouteConnectionPools = new HashMap<>();

    PerRouteConnectionPool fetchPerRoutePool(String key) {
        return perRouteConnectionPools.get(key);
    }

    Map<String, PerRouteConnectionPool> getPerRouteConnectionPools() {
        return perRouteConnectionPools;
    }

    /**
     * Entity which holds the pool of connections for a given http route.
     */
    static class PerRouteConnectionPool {

        private BlockingQueue<Http2ClientChannel> http2ClientChannels = new LinkedBlockingQueue<>();
        // Maximum number of allowed active streams
        private int maxActiveStreams;

        PerRouteConnectionPool(int maxActiveStreams) {
            this.maxActiveStreams = maxActiveStreams;
        }

        /**
         * Fetches an active {@code TargetChannel} from the pool.
         *
         * @return active TargetChannel
         */
        Http2ClientChannel fetchTargetChannel() {
            if (!http2ClientChannels.isEmpty()) {
                Http2ClientChannel http2ClientChannel = http2ClientChannels.peek();
                Channel channel = http2ClientChannel.getChannel();
                if (!channel.isActive()) {  // if channel is not active, forget it and fetch next one
                    http2ClientChannels.remove(http2ClientChannel);
                    return fetchTargetChannel();
                }
                // increment and get active stream count
                int activeSteamCount = http2ClientChannel.incrementActiveStreamCount();

                if (activeSteamCount < maxActiveStreams) {  // safe to fetch the Target Channel
                    return http2ClientChannel;
                } else if (activeSteamCount == maxActiveStreams) {  // no more streams except this one can be opened
                    http2ClientChannel.markAsExhausted();
                    http2ClientChannels.remove(http2ClientChannel);
                    return http2ClientChannel;
                } else {
                    http2ClientChannels.remove(http2ClientChannel);
                    return fetchTargetChannel();    // fetch the next one from the queue
                }
            }
            return null;
        }

        void addChannel(Http2ClientChannel http2ClientChannel) {
            http2ClientChannels.add(http2ClientChannel);
        }

        void removeChannel(Http2ClientChannel http2ClientChannel) {
            http2ClientChannels.remove(http2ClientChannel);
        }
    }
}
