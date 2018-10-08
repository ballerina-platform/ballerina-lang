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

package org.wso2.transport.http.netty.contract;

import io.netty.util.AttributeKey;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpClientConnector;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Queue;

/**
 * Common Constants used by gate way.
 */
public final class Constants {

    // Client Bootstrap related
    public static final String CLIENT_BOOTSTRAP_TCP_NO_DELY = "client.bootstrap.nodelay";
    public static final String CLIENT_BOOTSTRAP_KEEPALIVE = "client.bootstrap.keepalive";
    public static final String CLIENT_BOOTSTRAP_SEND_BUFFER_SIZE = "client.bootstrap.sendbuffersize";
    public static final String CLIENT_BOOTSTRAP_RECEIVE_BUFFER_SIZE = "client.bootstrap.recievebuffersize";
    public static final String CLIENT_BOOTSTRAP_CONNECT_TIME_OUT = "client.bootstrap.connect.timeout";
    public static final String CLIENT_BOOTSTRAP_SO_REUSE = "client.bootstrap.socket.reuse";
    public static final String CLIENT_BOOTSTRAP_SO_TIMEOUT = "client.bootstrap.socket.timeout";
    public static final String CLIENT_BOOTSTRAP_WORKER_GROUP_SIZE = "client.bootstrap.worker.group.size";

    //Server side SSL Parameters
    public static final String SSL_HANDLER = "ssl";
    public static final String CLIENT_SUPPORT_CIPHERS = "ciphers";
    public static final String CLIENT_SUPPORT_SSL_PROTOCOLS = "sslEnabledProtocols";
    public static final String CLIENT_ENABLE_SESSION_CREATION = "shareSession";

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

    //Event group size of server bootstrap
    public static final String EVENT_GROUP_EXECUTOR_THREAD_SIZE = "event.group.executor.thread.size";

    public static final String LISTENER_INTERFACE_ID = "listener.interface.id";

    //Server side SSL Parameters
    public static final String SERVER_SUPPORT_CIPHERS = "ciphers";
    public static final String SERVER_SUPPORT_SSL_PROTOCOLS = "sslEnabledProtocols";
    public static final String SERVER_ENABLE_SESSION_CREATION = "shareSession";
    public static final String SERVER_SUPPORTED_SERVER_NAMES = "server.suported.server.names";
    public static final String SERVER_SUPPORTED_SNIMATCHERS = "server.supported.snimatchers";
    public static final String SSL_VERIFY_CLIENT = "sslVerifyClient";
    public static final String SSL_PROTOCOL = "sslProtocol";

    public static final String IS_SECURED_CONNECTION = "IS_SECURED_CONNECTION";

    // Connection Pool parameters
    public static final String NUMBER_OF_POOLS = "client.connection.pool.count";
    public static final String MAX_ACTIVE_CONNECTIONS_PER_POOL = "client.max.active.connections.per.pool";
    public static final String MIN_IDLE_CONNECTIONS_PER_POOL = "client.min.idle.connections.per.pool";
    public static final String MAX_IDLE_CONNECTIONS_PER_POOL = "cleint.max.idle.connections.per.pool";
    public static final String MAX_WAIT_FOR_CLIENT_CONNECTION_POOL = "max.wait.for.client.connection.pool";
    public static final String MIN_EVICTION_IDLE_TIME = "client.min.eviction.idle.time";
    public static final String TIME_BETWEEN_EVICTION_RUNS = "client.time.between.eviction.runs";

    public static final String NO_THREADS_IN_EXECUTOR_SERVICE = "sender.thread.count";

    public static final String EXECUTOR_WORKER_POOL = "executor.workerpool";

    public static final String OUTPUT_CONTENT_BUFFER_SIZE = "output.content.buffer.size";
    public static final String CERTPASS = "certPass";
    public static final String KEYSTOREPASS = "keyStorePass";
    public static final String KEYSTOREFILE = "keyStoreFile";
    public static final String TRUSTSTOREFILE = "trustStoreFile";
    public static final String TRUSTSTOREPASS = "trustStorePass";


    public static final int DEFAULT_HTTP_PORT = 80;
    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final String DEFAULT_BASE_PATH = "/";

    public static final String TO = "TO";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = "https";
    public static final String HTTP_VERSION = "HTTP_VERSION";
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String JKS = "JKS";
    public static final String TLS_PROTOCOL = "TLS";
    public static final String REQUIRE = "require";

    public static final String TEXT_PLAIN = "text/plain";

    public static final String ENCODING_GZIP = "gzip";
    public static final String ENCODING_DEFLATE = "deflate";
    public static final String HTTP_TRANSFER_ENCODING_IDENTITY = "identity";

    // TODO: Move string constants for HTTP headers and header values to their own class
    public static final String HTTP_X_FORWARDED_FOR = "x-forwarded-for";
    public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    public static final String CONNECTION_CLOSE = "close";
    public static final String FORWARDED = "forwarded";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String X_FORWARDED_BY = "x-forwarded-by";
    public static final String X_FORWARDED_HOST = "x-forwarded-host";
    public static final String X_FORWARDED_PROTO = "x-forwarded-proto";

    public static final String HEADER_VAL_100_CONTINUE = "100-continue";

    public static final String HTTP_GET_METHOD = "GET";
    public static final String HTTP_POST_METHOD = "POST";
    public static final String HTTP_HEAD_METHOD = "HEAD";
    public static final String HTTP_PUT_METHOD = "PUT";
    public static final String HTTP_PATCH_METHOD = "PATCH";
    public static final String HTTP_DELETE_METHOD = "DELETE";

    //HTTP server connector creation parameters
    public static final String HTTP_HOST = "host";
    public static final String HTTP_PORT = "port";
    public static final String SCHEME = "scheme";
    public static final String KEEP_ALIVE = "keepAlive";
    public static final String HTTP_KEY_STORE_FILE = "keyStoreFile";
    public static final String HTTP_KEY_STORE_PASS = "keyStorePassword";
    public static final String HTTP_CERT_PASS = "certPassword";
    public static final String HTTP_DEFAULT_HOST = "0.0.0.0";
    public static final String HTTP_TRUST_STORE_FILE = "trustStoreFile";
    public static final String HTTP_TRUST_STORE_PASS = "trustStorePassword";
    public static final String TLS_STORE_TYPE = "tlsStoreType";

    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";
    public static final String RESOLVED_REQUESTED_URI = "RESOLVED_REQUESTED_URI";

    public static final String HTTP_REASON_PHRASE = "HTTP_REASON_PHRASE";

    public static final String CHNL_HNDLR_CTX = "CHNL_HNDLR_CTX";

    public static final String SRC_HANDLER = "SRC_HANDLER";
    public static final String POOLED_BYTE_BUFFER_FACTORY = "POOLED_BYTE_BUFFER_FACTORY";
    public static final String DEFAULT_VERSION_HTTP_1_1 = "HTTP/1.1";
    public static final float HTTP_1_1 = 1.1f;
    public static final float HTTP_1_0 = 1.0f;
    public static final float HTTP_2_0 = 2.0f;
    public static final String HTTP_VERSION_PREFIX = "HTTP/";
    public static final String HTTP_1_1_VERSION = "1.1";

    //Server Connection Related Parameters
    public static final String LOCAL_ADDRESS = "LOCAL_ADDRESS";
    public static final String LOCAL_NAME = "LOCAL_NAME";
    public static final String LOCAL_PORT = "LOCAL_PORT";
    public static final String REMOTE_ADDRESS = "REMOTE_ADDRESS";
    public static final String REMOTE_HOST = "REMOTE_HOST";
    public static final String REMOTE_PORT = "REMOTE_PORT";
    public static final String REQUEST_URL = "REQUEST_URL";
    public static final String ORIGIN_HOST = "ORIGIN_HOST";

    public static final String CHANNEL_ID = "CHANNEL_ID";

    public static final String DEFAULT_ADDRESS = "0.0.0.0";

    public static final String LOCALHOST = "localhost";

    public static final String HTTP_OBJECT_AGGREGATOR = "HTTP_OBJECT_AGGREGATOR";
    public static final String WS_SCHEME = "ws";
    public static final String WSS_SCHEME = "wss";
    public static final String WEBSOCKET_UPGRADE = "websocket";
    public static final String WEBSOCKET_FRAME_HANDLER = "WEBSOCKET_FRAME_HANDLER";
    public static final String MESSAGE_QUEUE_HANDLER = "MESSAGE_QUEUE_HANDLER";
    public static final int WEBSOCKET_STATUS_CODE_NORMAL_CLOSURE = 1000;
    public static final int WEBSOCKET_STATUS_CODE_GOING_AWAY = 1001;
    public static final int WEBSOCKET_STATUS_CODE_PROTOCOL_ERROR = 1002;
    public static final int WEBSOCKET_STATUS_CODE_ABNORMAL_CLOSURE = 1006;
    public static final int WEBSOCKET_STATUS_CODE_UNEXPECTED_CONDITION = 1011;

    // Callback related parameters
    public static final String HTTP_CONNECTION_CLOSE = "close";

    // Message direction related parameters
    public static final String DIRECTION = "DIRECTION";
    public static final String DIRECTION_REQUEST = "DIRECTION_REQUEST";
    public static final String DIRECTION_RESPONSE = "DIRECTION_RESPONSE";

    // Proxy related parameters
    public static final String IS_PROXY_ENABLED = "IS_PROXY_ENABLED";

    // HTTP2 Related Parameters
    public static final String UPGRADE_RESPONSE_HEADER = "http-to-http2-upgrade";
    public static final String HTTP_VERSION_2_0 = "HTTP/2.0";
    public static final String HTTP2_VERSION = "2.0";
    public static final String HTTP2_CLEARTEXT_PROTOCOL = "h2c";
    public static final String HTTP2_TLS_PROTOCOL = "h2";
    public static final String AUTHORITY = "AUTHORITY";
    public static final String HTTP2_METHOD = ":method";
    public static final String HTTP2_PATH = ":path";
    public static final String HTTP2_AUTHORITY = ":authority";
    public static final String HTTP2_SCHEME = ":scheme";

    public static final String HTTP_SOURCE_HANDLER = "SourceHandler";
    public static final String HTTP_ENCODER = "encoder";
    public static final String HTTP_COMPRESSOR = "compressor";
    public static final String HTTP_CHUNK_WRITER = "chunkWriter";
    public static final String HTTP_DECODER = "decoder";
    public static final String HTTP_CLIENT_CODEC = "codec";
    public static final String HTTP_SERVER_CODEC = "ServerCodec";
    public static final String HTTP2_SOURCE_HANDLER = "Http2SourceHandler";
    public static final String HTTP2_SOURCE_CONNECTION_HANDLER = "Http2SourceConnectionHandler";
    public static final String HTTP2_ALPN_HANDLER = "Http2ALPNHandler";
    public static final String PROXY_HANDLER = "proxyServerHandler";
    public static final String SSL_COMPLETION_HANDLER = "sslHandshakeCompletionHandler";
    public static final String HTTP_CERT_VALIDATION_HANDLER = "certificateValidation";
    public static final String CONNECTION_HANDLER = "connectionHandler";
    public static final String HTTP2_TARGET_HANDLER = "http2TargetHandler";
    public static final String TARGET_HANDLER = "targetHandler";
    public static final String HTTP2_TIMEOUT_HANDLER = "Http2TimeoutHandler";
    public static final String OUTBOUND_THROTTLING_HANDLER = "outboundThrottlingHandler";
    public static final String HTTP2_UPGRADE_HANDLER = "Http2UpgradeHandler";
    public static final String HTTP2_TO_HTTP_FALLBACK_HANDLER = "Http2ToHttpFallbackHandler";
    public static final String DECOMPRESSOR_HANDLER = "deCompressor";
    public static final String IDLE_STATE_HANDLER = "idleStateHandler";
    public static final String HTTP_TRACE_LOG_HANDLER = "http-trace-logger";
    public static final String HTTP_ACCESS_LOG_HANDLER = "http-access-logger";
    public static final String WEBSOCKET_SERVER_HANDSHAKE_HANDLER = "websocket-server-handshake-handler";
    public static final String WEBSOCKET_CLIENT_HANDSHAKE_HANDLER = "websocket-client-handshake-handler";

    public static final AttributeKey<Integer> REDIRECT_COUNT = AttributeKey.valueOf("REDIRECT_COUNT");
    public static final AttributeKey<String> RESOLVED_REQUESTED_URI_ATTR = AttributeKey
            .valueOf("RESOLVED_REQUESTED_URI_ATTR");
    public static final AttributeKey<HttpCarbonMessage> ORIGINAL_REQUEST = AttributeKey.valueOf("ORIGINAL_REQUEST");
    public static final AttributeKey<HttpResponseFuture> RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL = AttributeKey
            .valueOf("RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL");
    public static final AttributeKey<Long> ORIGINAL_CHANNEL_START_TIME = AttributeKey
            .valueOf("ORIGINAL_CHANNEL_START_TIME");
    public static final AttributeKey<Integer> ORIGINAL_CHANNEL_TIMEOUT = AttributeKey
            .valueOf("ORIGINAL_CHANNEL_TIMEOUT");
    public static final AttributeKey<TargetChannel> TARGET_CHANNEL_REFERENCE = AttributeKey
            .valueOf("TARGET_CHANNEL_REFERENCE");
    public static final AttributeKey<DefaultHttpClientConnector> CLIENT_CONNECTOR = AttributeKey
            .valueOf("CLIENT_CONNECTOR");
    public static final int REDIRECT_SEE_OTHER_303 = 303;

    public static final AttributeKey<Long> MAX_RESPONSES_ALLOWED_TO_BE_QUEUED =
            AttributeKey.valueOf("MAX_RESPONSES_ALLOWED_TO_BE_QUEUED");
    public static final AttributeKey<Queue> RESPONSE_QUEUE = AttributeKey.valueOf("RESPONSE_QUEUE");
    public static final AttributeKey<Long> NEXT_SEQUENCE_NUMBER = AttributeKey.valueOf("NEXT_SEQUENCE_NUMBER");
    public static final long EXPECTED_SEQUENCE_NUMBER = 1L;
    public static final int NUMBER_OF_INITIAL_EVENTS_HELD = 3;
    public static final int MEANINGFULLY_EQUAL = 0;
    public static final int UNBOUNDED_RESPONSE_QUEUE = -1;

    public static final String UTF8 = "UTF-8";
    public static final String URL_AUTHORITY = "://";
    public static final String FORWRD_SLASH = "/";
    public static final String COLON = ":";
    public static final int MAX_REDIRECT_COUNT = 5;

    public static final int ENDPOINT_TIMEOUT = 5 * 60000;
    public static final String ENDPOINT_TIMEOUT_MSG = "Endpoint timed out";
    public static final String CHUNKED = "chunked";
    public static final String CHUNKING_CONFIG = "chunking_config";

    // Trace Logger related parameters
    public static final String TRACE_LOG_UPSTREAM = "http.tracelog.upstream";
    public static final String TRACE_LOG_DOWNSTREAM = "http.tracelog.downstream";

    // Access Logger related parameters
    public static final String ACCESS_LOG = "http.accesslog";
    public static final String ACCESS_LOG_FORMAT =
            "%1$s - - [%2$td/%2$tb/%2$tY:%2$tT %2$tz] \"%3$s %4$s %5$s\" %6$d %7$d \"%8$s\" \"%9$s\"";

    public static final String LISTENER_PORT = "LISTENER_PORT";

    public static final String REQUEST_LINE_TOO_LONG = "An HTTP line is larger than";
    public static final String REQUEST_HEADER_TOO_LARGE = "HTTP header is larger than";

    // Idle timeout error scenarios
    public static final String IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST
            = "Idle timeout triggered before initiating inbound request";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_HEADERS
            = "Idle timeout triggered while reading inbound request headers";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_BODY
            = "Idle timeout triggered while reading inbound request entity body";
    public static final String IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_RESPONSE
            = "Idle timeout triggered before initiating outbound response";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS
            = "Idle timeout triggered while writing outbound response headers";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY
            = "Idle timeout triggered while writing outbound response entity body";
    public static final String IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_100_CONTINUE_RESPONSE
            = "Idle timeout triggered before initiating 100 continue response";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_100_CONTINUE_RESPONSE
            = "Idle timeout triggered while writing 100 continue response";

    public static final String IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_REQUEST
            = "Idle timeout triggered before initiating outbound request";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS
            = "Idle timeout triggered while writing outbound request headers";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY
            = "Idle timeout triggered while writing outbound request entity body";
    public static final String IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE
            = "Idle timeout triggered before initiating inbound response";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE_HEADERS
            = "Idle timeout triggered while reading inbound response headers";
    public static final String IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE_BODY
            = "Idle timeout triggered while reading inbound response entity body";

    public static final String EXCEPTION_CAUGHT_WHILE_READING_REQUEST
            = "Exception caught while reading inbound request";
    public static final String EXCEPTION_CAUGHT_WHILE_READING_RESPONSE
            = "Exception caught while reading inbound response";

    // Client connection closure error scenarios
    public static final String REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST
            = "Remote client closed the connection before initiating inbound request";
    public static final String REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS
            = "Remote client closed the connection while reading inbound request headers";
    public static final String REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY
            = "Remote client closed the connection while reading inbound request entity body";
    public static final String REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE
            = "Remote client closed the connection before initiating outbound response";
    public static final String REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS
            = "Remote client closed the connection while writing outbound response headers";
    public static final String REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY
            = "Remote client closed the connection while writing outbound response entity body";
    public static final String REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE
            = "Remote client closed the connection before initiating 100 continue response";
    public static final String REMOTE_CLIENT_CLOSED_WHILE_WRITING_100_CONTINUE_RESPONSE
            = "Remote client closed the connection while writing 100 continue response";

    // Server connection closure error scenarios
    public static final String REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST
            = "Remote host closed the connection before initiating outbound request";
    public static final String REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS
            = "Remote host closed the connection while writing outbound request headers";
    public static final String REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY
            = "Remote host closed the connection while writing outbound request entity body";
    public static final String REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE
            = "Remote host closed the connection before initiating inbound response";
    public static final String REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_HEADERS
            = "Remote host closed the connection while reading inbound response headers";
    public static final String REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_BODY
            = "Remote host closed the connection while reading inbound response body";

    public static final String REMOTE_CLIENT_TO_HOST_CONNECTION_CLOSED
            = "Connection between remote client and host is closed";
    public static final String CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED
            = "Connection between client and remote host is closed";

    public static final String PROMISED_STREAM_REJECTED_ERROR
            = "Promised stream is already rejected or stream is no longer valid";

    public static final String MAXIMUM_WAIT_TIME_EXCEED = "Could not obtain a connection within maximum wait time";

    public static final String JMX_AGENT_NAME = "jmx.agent.name";

    public static final String HTTP_RESOURCE = "httpResource";

    public static final String INBOUND_RESPONSE_ALREADY_RECEIVED = "Inbound response message already received";

    private Constants() {
    }
}
