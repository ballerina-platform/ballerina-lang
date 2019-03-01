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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code Http2ConnectionManager} Manages HTTP/2 connections.
 */
public class Http2ConnectionManager {

    private final ConcurrentHashMap<EventLoop, EventLoopPool> eventLoopPools = new ConcurrentHashMap<>();
    private final List<EventLoop> eventLoops = new ArrayList<>(); //When source handler is not present
    private PoolConfiguration poolConfiguration;

    public Http2ConnectionManager(PoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
    }

    public void addHttp2ClientChannel(EventLoop eventLoop, HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        eventLoops.add(eventLoop);
        final EventLoopPool eventLoopPool = createOrGetEventLoopPool(eventLoop);
        String key = generateKey(httpRoute);
        final EventLoopPool.PerRouteConnectionPool perRouteConnectionPool = createOrGetPerRoutePool(eventLoopPool, key);
        perRouteConnectionPool.addChannel(http2ClientChannel);

        // Configure a listener to remove connection from pool when it is closed
        http2ClientChannel.getChannel().closeFuture().
            addListener(future -> {
                            EventLoopPool.PerRouteConnectionPool pool = eventLoopPool.fetchConnectionPool(key);
                            if (pool != null) {
                                pool.removeChannel(http2ClientChannel);
                                http2ClientChannel.getDataEventListeners().
                                    forEach(Http2DataEventListener::destroy);
                            }
                        }
            );
    }

    private EventLoopPool.PerRouteConnectionPool createOrGetPerRoutePool(EventLoopPool eventLoopPool, String key) {
        final EventLoopPool.PerRouteConnectionPool perRouteConnectionPool = eventLoopPool.fetchConnectionPool(key);
        if (perRouteConnectionPool != null) {
            return perRouteConnectionPool;
        }
        return eventLoopPool.getConnectionPools().computeIfAbsent(key, p -> new EventLoopPool.PerRouteConnectionPool(
            poolConfiguration.getHttp2MaxActiveStreamsPerConnection()));
    }

    private EventLoopPool createOrGetEventLoopPool(EventLoop eventLoop) {
        final EventLoopPool pool = eventLoopPools.get(eventLoop);
        if (pool != null) {
            return pool;
        }
        return eventLoopPools.computeIfAbsent(eventLoop, e -> new EventLoopPool(eventLoop));
    }

    private String generateKey(HttpRoute httpRoute) {
        return httpRoute.getScheme() + ":" + httpRoute.getHost() + ":" + httpRoute.getPort();
    }

    public Http2ClientChannel borrowChannel(Http2SourceHandler http2SrcHandler, HttpRoute httpRoute) {
        EventLoopPool eventLoopPool;
        String key = generateKey(httpRoute);
        EventLoopPool.PerRouteConnectionPool perRouteConnectionPool;

        if (http2SrcHandler != null) {
            eventLoopPool = createOrGetEventLoopPool(http2SrcHandler.getChannelHandlerContext().channel().eventLoop());
            perRouteConnectionPool = createOrGetPerRoutePool(eventLoopPool, key);

        } else {
            if (eventLoops.isEmpty()) {
                return null;
            }
            //Need a better algorithm to pick an eventloop. May be round robin?
            eventLoopPool = createOrGetEventLoopPool(eventLoops.get(0));
            perRouteConnectionPool = createOrGetPerRoutePool(eventLoopPool, key);
        }

        Http2ClientChannel http2ClientChannel = null;
        if (perRouteConnectionPool != null) {
            http2ClientChannel = perRouteConnectionPool.fetchTargetChannel();
        }
        return http2ClientChannel;
    }

    void returnClientChannel(HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        EventLoopPool.PerRouteConnectionPool perRouteConnectionPool = fetchPerRoutePool(httpRoute,
                                                                                        http2ClientChannel.getChannel()
                                                                                            .eventLoop());
        if (perRouteConnectionPool != null) {
            perRouteConnectionPool.addChannel(http2ClientChannel);
        }
    }

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
        return eventLoopPools.get(eventLoop).fetchConnectionPool(key);
    }

   /* // Per route connection pools
    private static ConcurrentHashMap<String, PerRouteConnectionPool> connectionPools = new ConcurrentHashMap<>();
    // Lock for synchronizing access
    private Lock lock = new ReentrantLock();
    private PoolConfiguration poolConfiguration;

    public Http2ConnectionManager(PoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
    }

    *//**
     * Borrows an already active {@link Http2ClientChannel} for a given http route.
     * This will not try to create new connections.
     *
     * @param httpRoute http route
     * @return an active {@code Http2ClientChannel}
     *//*
    public Http2ClientChannel borrowChannel(HttpRoute httpRoute) {
        String key = generateKey(httpRoute);
        PerRouteConnectionPool perRouteConnectionPool = fetchConnectionPool(key);
        Http2ClientChannel http2ClientChannel = null;
        if (perRouteConnectionPool != null) {
            http2ClientChannel = perRouteConnectionPool.fetchTargetChannel();
        }
        return http2ClientChannel;
    }

    private PerRouteConnectionPool fetchConnectionPool(String key) {
        return connectionPools.get(key);
    }

    private void registerConnectionPool(String key, PerRouteConnectionPool perRouteConnectionPool) {
        connectionPools.put(key, perRouteConnectionPool);
    }

    private String generateKey(HttpRoute httpRoute) {
        return httpRoute.getScheme() + ":" + httpRoute.getHost() + ":" + httpRoute.getPort();
    }

    *//**
     * Adds a Http2 Client Channel to the connection pool.
     * Upgraded connection from HTTP/1.1 to HTTP/2 should arrive here.
     *
     * @param httpRoute          host:port pair
     * @param http2ClientChannel Http2 Client Channel
     *//*
    public void addHttp2ClientChannel(HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        String key = generateKey(httpRoute);
        PerRouteConnectionPool perRouteConnectionPool = fetchConnectionPool(key);
        if (perRouteConnectionPool != null) {
            perRouteConnectionPool.addChannel(http2ClientChannel);
        } else {
            lock.lock();
            try {
                perRouteConnectionPool = fetchConnectionPool(key);

                if (perRouteConnectionPool == null) {
                    perRouteConnectionPool = new PerRouteConnectionPool(
                        poolConfiguration.getHttp2MaxActiveStreamsPerConnection());
                    registerConnectionPool(key, perRouteConnectionPool);
                }
                perRouteConnectionPool.addChannel(http2ClientChannel);
            } finally {
                lock.unlock();
            }
        }
        // Configure a listener to remove connection from pool when it is closed
        http2ClientChannel.getChannel().closeFuture().
                addListener(future -> {
                            PerRouteConnectionPool pool = connectionPools.get(key);
                            if (pool != null) {
                                pool.removeChannel(http2ClientChannel);
                                http2ClientChannel.getDataEventListeners().
                                        forEach(Http2DataEventListener::destroy);
                            }
                        }
                );
    }

    *//**
     * Returns the previously exhausted {@code Http2ClientChannel} back to the pool.
     *
     * @param httpRoute          http route
     * @param http2ClientChannel previously exhausted Http2ClientChannel
     *//*
    void returnClientChannel(HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        String key = generateKey(httpRoute);
        PerRouteConnectionPool perRouteConnectionPool = fetchConnectionPool(key);
        if (perRouteConnectionPool != null) {
            perRouteConnectionPool.addChannel(http2ClientChannel);
        }
    }

    *//**
     * Removes the {@code Http2ClientChannel} from pool.
     *
     * @param httpRoute          the http route
     * @param http2ClientChannel the {@code Http2ClientChannel} to be removed
     *//*
    void removeClientChannel(HttpRoute httpRoute, Http2ClientChannel http2ClientChannel) {
        String key = generateKey(httpRoute);
        PerRouteConnectionPool perRouteConnectionPool = fetchConnectionPool(key);
        if (perRouteConnectionPool != null) {
            perRouteConnectionPool.removeChannel(http2ClientChannel);
        }
    }

    *//**
     * Entity which holds the pool of connections for a given http route.
     *//*
    private static class PerRouteConnectionPool {

        private BlockingQueue<Http2ClientChannel> http2ClientChannels = new LinkedBlockingQueue<>();
        // Maximum number of allowed active streams
        private int maxActiveStreams;

        PerRouteConnectionPool(int maxActiveStreams) {
            this.maxActiveStreams = maxActiveStreams;
        }

        *//**
     * Fetches an active {@code TargetChannel} from the pool.
     *
     * @return active TargetChannel
     *//*
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
    }*/


}
