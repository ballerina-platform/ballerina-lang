/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.common;

/**
 * Common Constants used by gate way.
 */
public class Constants {
    public static final String PROTOCOL_NAME = "http";

    public static final String HTTP_VERSION = "HTTP_VERSION";

    public static final String HTTP_METHOD = "HTTP_METHOD";

    public static final String HTTP_CONTENT_TYPE = "Content-Type";

    public static final String HTTP_SOAP_ACTION = "SOAPAction";

    public static final String HTTP_CONTENT_ENCODING = "Accept-Encoding";

    public static final String HTTP_HOST = "Host";

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";

    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";

    public static final String CHNL_HNDLR_CTX = "CHNL_HNDLR_CTX";

    public static final String SRC_HNDLR = "SRC_HNDLR";

    public static final String ENGINE = "Engine";

    public static final String RESPONSE_CALLBACK = "RESPONSE_CALLBACK";

    public static final String DISRUPTOR = "Disruptor";

    public static final String BUSY_SPIN = "BUSY_SPIN";

    public static final String BLOCKING_WAIT = "BLOCKING_WAIT";

    public static final String LITE_BLOCKING = "LITE_BLOCKING";

    public static final String PHASED_BACKOFF = "PHASED_BACKOFF";

    public static final String TIME_BLOCKING = "TIME_BLOCKING";

    public static final String SLEEP_WAITING = "SLEEP_WAITING";


    public static final String TRANSPORT_ID = "netty-gw";

    public static final String WAIT_STRATEGY = "disruptor.wait.strategy";

    public static final String DISRUPTOR_BUFFER_SIZE = "disruptor.buffer.size";

    public static final String DISRUPTOR_COUNT = "disruptor.count";

    public static final String DISRUPTOR_EVENT_HANDLER_COUNT = "disruptor.eventhandler.count";

    public static final String CONTENT_QUEUE_SIZE = "content.queue.size";

    public static final String SHARE_DISRUPTOR_WITH_OUTBOUND = "share.disruptor.with.outbound";

    // Bootstrap related
    public static final String CLINET_BOOTSTRAP_TCP_NO_DELY = "client.bootstrap.nodelay";

    public static final String CLINET_BOOTSTRAP_KEEPALIVE = "client.bootstrap.keepalive";

    public static final String CLINET_BOOTSTRAP_SEND_BUFFER_SIZE = "client.bootstrap.sendbuffersize";

    public static final String CLINET_BOOTSTRAP_RECEIVE_BUFFER_SIZE = "client.bootstrap.recievebuffersize";

    public static final String CLINET_BOOTSTRAP_CONNECT_TIME_OUT = "client.bootstrap.connect.timeout";

    public static final String CLINET_BOOTSTRAP_SO_REUSE = "client.bootstrap.socket.reuse";

    // Connection Pool parameters

    public static final String NUMBER_OF_POOLS = "connection.pool.count";

    public static final String MAX_ACTIVE_CONNECTIONS_PER_POOL = "max.active.connections.per.pool";

    public static final String MIN_IDLE_CONNECTIONS_PER_POOL = "min.idle.connections.per.pool";

    public static final String MAX_IDLE_CONNECTIONS_PER_POOL = "max.idle.connections.per.pool";

    public static final String MIN_EVICTION_IDLE_TIME = "min.eviction.idle.time";

    public static final String ENABLE_GLOBAL_CONNECTION_POOLING = "enable.global.client.connection.pooling";

}


