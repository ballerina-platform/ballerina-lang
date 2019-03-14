/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.channel.EventLoop;
import org.wso2.transport.http.netty.contractimpl.common.HttpRoute;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code Http2ConnectionManager} Manages HTTP/2 connections.
 */
public class Http2ConnectionManager {

    private final ConcurrentHashMap<EventLoop, EventLoopPool> eventLoopPools = new ConcurrentHashMap<>();
    private final Deque<EventLoop> eventLoops = new ArrayDeque<>(); //When source handler is not present
    private PoolConfiguration poolConfiguration;

    public Http2ConnectionManager(PoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
    }

    /**
     * Add a given {@link Http2ClientChannel} to the relevant pool.
     *
     * @param eventLoop          netty event loop
     * @param httpRoute          http route
     * @param http2ClientChannel newly created http/2 client channel
     */
    public void addHttp2ClientChannel(EventLoop eventLoop, HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        if (!eventLoops.contains(eventLoop)) {
            eventLoops.add(eventLoop);
        }
        final EventLoopPool eventLoopPool = getOrCreateEventLoopPool(eventLoop);
        String key = generateKey(httpRoute);
        final EventLoopPool.PerRouteConnectionPool perRouteConnectionPool = getOrCreatePerRoutePool(eventLoopPool, key);
        perRouteConnectionPool.addChannel(http2ClientChannel);

        // Configure a listener to remove connection from pool when it is closed
        http2ClientChannel.getChannel().closeFuture().
            addListener(future -> {
                            EventLoopPool.PerRouteConnectionPool pool = eventLoopPool.fetchPerRoutePool(key);
                            if (pool != null) {
                                pool.removeChannel(http2ClientChannel);
                                http2ClientChannel.getDataEventListeners().
                                    forEach(Http2DataEventListener::destroy);
                            }
                        }
            );
    }

    /**
     * Get or create the pool that is bound to the given eventloop.
     *
     * @param eventLoop netty event loop
     * @return EventLoopPool
     */
    private EventLoopPool getOrCreateEventLoopPool(EventLoop eventLoop) {
        final EventLoopPool pool = eventLoopPools.get(eventLoop);
        if (pool != null) {
            return pool;
        }
        return eventLoopPools.computeIfAbsent(eventLoop, e -> new EventLoopPool());
    }

    /**
     * Get or creat the per route pool.
     *
     * @param eventLoopPool the pool that is bound to the eventloop
     * @param key           the route key
     * @return PerRouteConnectionPool
     */
    private EventLoopPool.PerRouteConnectionPool getOrCreatePerRoutePool(EventLoopPool eventLoopPool, String key) {
        final EventLoopPool.PerRouteConnectionPool perRouteConnectionPool = eventLoopPool.fetchPerRoutePool(key);
        if (perRouteConnectionPool != null) {
            return perRouteConnectionPool;
        }
        return eventLoopPool.getPerRouteConnectionPools().computeIfAbsent(key,
                                                                  p -> new EventLoopPool.PerRouteConnectionPool(
                                                                      poolConfiguration
                                                                          .getHttp2MaxActiveStreamsPerConnection()));
    }

    /**
     * Borrow an HTTP/2 client channel.
     *
     * @param http2SrcHandler Relevant http/2 source handler where the source connection belongs to
     * @param httpRoute       the http route
     * @return Http2ClientChannel
     */
    public Http2ClientChannel borrowChannel(Http2SourceHandler http2SrcHandler, HttpRoute httpRoute) {
        EventLoopPool eventLoopPool;
        String key = generateKey(httpRoute);
        EventLoopPool.PerRouteConnectionPool perRouteConnectionPool;

        if (http2SrcHandler != null) {
            eventLoopPool = getOrCreateEventLoopPool(http2SrcHandler.getChannelHandlerContext().channel().eventLoop());
            perRouteConnectionPool = getOrCreatePerRoutePool(eventLoopPool, key);

        } else {
            if (eventLoops.isEmpty()) {
                return null;
            }
            eventLoopPool = getOrCreateEventLoopPool(eventLoops.peek());
            perRouteConnectionPool = getOrCreatePerRoutePool(eventLoopPool, key);
        }

        Http2ClientChannel http2ClientChannel = null;
        if (perRouteConnectionPool != null) {
            http2ClientChannel = perRouteConnectionPool.fetchTargetChannel();
        }
        return http2ClientChannel;
    }

    /**
     * Return the http/2 client channel to per route pool.
     *
     * @param httpRoute          the http route
     * @param http2ClientChannel represents the http/2 client channel
     */
    void returnClientChannel(HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        EventLoopPool.PerRouteConnectionPool perRouteConnectionPool = fetchPerRoutePool(httpRoute,
                                                                                        http2ClientChannel.getChannel()
                                                                                            .eventLoop());
        if (perRouteConnectionPool != null) {
            perRouteConnectionPool.addChannel(http2ClientChannel);
        }
    }

    /**
     * Remove http/2 client channel from per route pool.
     *
     * @param httpRoute          the http route
     * @param http2ClientChannel represents the http/2 client channel to be removed
     */
    void removeClientChannel(HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        EventLoopPool.PerRouteConnectionPool perRouteConnectionPool = fetchPerRoutePool(httpRoute,
                                                                                        http2ClientChannel.getChannel()
                                                                                            .eventLoop());
        if (perRouteConnectionPool != null) {
            perRouteConnectionPool.removeChannel(http2ClientChannel);
        }
    }

    private EventLoopPool.PerRouteConnectionPool fetchPerRoutePool(HttpRoute httpRoute,
                                                                   EventLoop eventLoop) {
        String key = generateKey(httpRoute);
        return eventLoopPools.get(eventLoop).fetchPerRoutePool(key);
    }

    private String generateKey(HttpRoute httpRoute) {
        return httpRoute.getScheme() + ":" + httpRoute.getHost() + ":" + httpRoute.getPort();
    }
}
