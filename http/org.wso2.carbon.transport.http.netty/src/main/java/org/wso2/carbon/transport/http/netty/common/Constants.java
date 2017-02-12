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
public final class Constants {

    // Server state constants
    public static final String STATE_TRANSITION = "STATE_TRANSITION";

    public static final String STATE_STARTED = "STATE_STARTED";

    public static final String STATE_STOPPED = "STATE_STOPPED";

    // Client Bootstrap related
    public static final String CLIENT_BOOTSTRAP_TCP_NO_DELY = "client.bootstrap.nodelay";

    public static final String CLIENT_BOOTSTRAP_KEEPALIVE = "client.bootstrap.keepalive";

    public static final String CLIENT_BOOTSTRAP_SEND_BUFFER_SIZE = "client.bootstrap.sendbuffersize";

    public static final String CLIENT_BOOTSTRAP_RECEIVE_BUFFER_SIZE = "client.bootstrap.recievebuffersize";

    public static final String CLIENT_BOOTSTRAP_CONNECT_TIME_OUT = "client.bootstrap.connect.timeout";

    public static final String CLIENT_BOOTSTRAP_SO_REUSE = "client.bootstrap.socket.reuse";

    public static final String CLIENT_BOOTSTRAP_SO_TIMEOUT = "client.bootstrap.socket.timeout";

    //Server side SSL Parameters
    public static final String CLIENT_SUPPORT_CIPHERS = "client.ssl.ciphers";
    public static final String CLIENT_SUPPORT_HTTPS_PROTOCOLS = "client.ssl.http.protocols";
    public static final String CLIENT_ENABLE_SESSION_CREATION = "client.enable.session.creation";

    // Server Bootstrap related
    public static final String SERVER_BOOTSTRAP_TCP_NO_DELY = "server.bootstrap.nodelay";

    public static final String SERVER_BOOTSTRAP_KEEPALIVE = "server.bootstrap.keepalive";

    public static final String SERVER_BOOTSTRAP_SEND_BUFFER_SIZE = "server.bootstrap.sendbuffersize";

    public static final String SERVER_BOOTSTRAP_RECEIVE_BUFFER_SIZE = "server.bootstrap.recievebuffersize";

    public static final String SERVER_BOOTSTRAP_CONNECT_TIME_OUT = "server.bootstrap.connect.timeout";

    public static final String SERVER_BOOTSTRAP_SO_REUSE = "server.bootstrap.socket.reuse";

    public static final String SERVER_BOOTSTRAP_SO_BACKLOG = "server.bootstrap.socket.backlog";

    public static final String SERVER_BOOTSTRAP_SO_TIMEOUT = "server.bootstrap.socket.timeout";

    // Boss group size of the server bootstrap
    public static final String SERVER_BOOTSTRAP_BOSS_GROUP_SIZE = "server.bootstrap.boss.group.size";

    //Worker group size of the server bootstrap
    public static final String SERVER_BOOTSTRAP_WORKER_GROUP_SIZE = "server.bootstrap.worker.group.size";

    public static final String LISTENER_INTERFACE_ID = "listener.interface.id";

    //Server side SSL Parameters
    public static final String SERVER_SUPPORT_CIPHERS = "server.ssl.ciphers";
    public static final String SERVER_SUPPORT_HTTPS_PROTOCOLS = "server.ssl.http.protocols";
    public static final String SERVER_ENABLE_SESSION_CREATION = "server.enable.session.creation";
    public static final String SERVER_SUPPORTED_SERVER_NAMES = "server.suported.server.names";
    public static final String SERVER_SUPPORTED_SNIMATCHERS = "server.supported.snimatchers";
    public static final String SSL_VERIFY_CLIENT = "ssl.verify.client";

    public static final String IS_SECURED_CONNECTION = "IS_SECURED_CONNECTION";

    // Connection Pool parameters
    public static final String NUMBER_OF_POOLS = "client.connection.pool.count";

    public static final String MAX_ACTIVE_CONNECTIONS_PER_POOL = "client.max.active.connections.per.pool";

    public static final String MIN_IDLE_CONNECTIONS_PER_POOL = "client.min.idle.connections.per.pool";

    public static final String MAX_IDLE_CONNECTIONS_PER_POOL = "cleint.max.idle.connections.per.pool";

    public static final String MIN_EVICTION_IDLE_TIME = "client.min.eviction.idle.time";

    public static final String ENABLE_GLOBAL_CONNECTION_POOLING = "enable.global.client.connection.pooling";

    public static final String NO_THREADS_IN_EXECUTOR_SERVICE = "sender.thread.count";


    public static final String EXECUTOR_WORKER_POOL = "executor.workerpool";

    public static final String OUTPUT_CONTENT_BUFFER_SIZE = "output.content.buffer.size";

    public static final String CERTPASS = "certPass";

    public static final String KEYSTOREPASS = "keyStorePass";

    public static final String KEYSTOREFILE = "keyStoreFile";

    public static final String TRUSTSTOREFILE = "trustStoreFile";

    public static final String TRUSTSTOREPASS = "trustStorePass";

    public static final String RESPONSE_CALLBACK = "RESPONSE_CALLBACK";

    public static final String HOST = "HOST";

    public static final String PORT = "PORT";

    public static final String TO = "TO";

    public static final String PROTOCOL = "PROTOCOL";

    public static final String PROTOCOL_NAME = "http";

    public static final String HTTP_VERSION = "HTTP_VERSION";

    public static final String HTTP_METHOD = "HTTP_METHOD";

    public static final String HTTP_CONTENT_TYPE = "Content-Type";

    public static final String TEXT_XML = "text/xml";

    public static final String TEXT_PLAIN = "text/plain";

    public static final String APPLICATION_XML = "application/xml";

    public static final String CONTENT_ENCODING = "Content-Encoding";

    public static final String ENCODING_GZIP = "gzip";

    public static final String ENCODING_DEFLATE = "deflate";

    public static final String HTTP_CONTENT_LENGTH = "Content-Length";

    public static final String HTTP_TRANSFER_ENCODING = "Transfer-Encoding";

    public static final String HTTP_CONNECTION = "Connection";

    public static final String KEEP_ALIVE = "keep-alive";

    public static final String HTTP_SOAP_ACTION = "SOAPAction";

    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    public static final String HTTP_HOST = "Host";

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";

    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";

    public static final String HTTP_REASON_PHRASE = "HTTP_REASON_PHRASE";

    public static final String CHNL_HNDLR_CTX = "CHNL_HNDLR_CTX";

    public static final String SRC_HNDLR = "SRC_HNDLR";

    //Server Connection Related Parameters
    public static final String LOCAL_ADDRESS = "LOCAL_ADDRESS";
    public static final String LOCAL_NAME = "LOCAL_NAME";
    public static final String LOCAL_PORT = "LOCAL_PORT";
    public static final String REMOTE_ADDRESS = "REMOTE_ADDRESS";
    public static final String REMOTE_HOST = "REMOTE_HOST";
    public static final String REMOTE_PORT = "REMOTE_PORT";
    public static final String REQUEST_URL = "REQUEST_URL";

    public static final String CHANNEL_ID = "CHANNEL_ID";

    public static final String LOOP_BACK_ADDRESS = "127.0.0.1";

    public static final String DEFAULT_ADDRESS = "0.0.0.0";

    public static final String LOCALHOST = "localhost";

    // Callback related parameters
    public static final String HTTP_CONNECTION_CLOSE = "close";

    // HTTP2 Related Parameters
    public static final String UPGRADE_RESPONSE_HEADER = "http-to-http2-upgrade";
    public static final String HTTP2_VERSION = "HTTP/2.0";
    public static final String STREAM_ID = "STREAM_ID";
    public static final String SCHEME = "SCHEME";
    public static final String AUTHORITY = "AUTHORITY";
    public static final String HTTP2_METHOD = ":method";
    public static final String HTTP2_PATH = ":path";
    public static final String HTTP2_AUTHORITY = ":authority";
    public static final String HTTP2_SCHEME = ":scheme";

    private Constants() {
    }

}


