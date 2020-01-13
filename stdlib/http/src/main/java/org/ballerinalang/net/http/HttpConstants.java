/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG;
import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;

/**
 * Constants for HTTP.
 *
 * @since 0.8.0
 */
public class HttpConstants {

    public static final String HTTPS_ENDPOINT_STARTED = "[ballerina/http] started HTTPS/WSS listener ";
    public static final String HTTP_ENDPOINT_STARTED = "[ballerina/http] started HTTP/WS listener ";
    public static final String HTTPS_ENDPOINT_STOPPED = "[ballerina/http] stopped HTTPS/WSS listener ";
    public static final String HTTP_ENDPOINT_STOPPED = "[ballerina/http] stopped HTTP/WS listener ";

    public static final String BASE_PATH = "BASE_PATH";
    public static final String SUB_PATH = "SUB_PATH";
    public static final String EXTRA_PATH_INFO = "EXTRA_PATH_INFO";
    public static final String RAW_URI = "RAW_URI";
    public static final String RESOURCE_ARGS = "RESOURCE_ARGS";
    public static final String MATRIX_PARAMS = "MATRIX_PARAMS";
    public static final String QUERY_STR = "QUERY_STR";
    public static final String RAW_QUERY_STR = "RAW_QUERY_STR";

    public static final String DEFAULT_INTERFACE = "0.0.0.0:8080";
    public static final String DEFAULT_BASE_PATH = "/";
    public static final String DEFAULT_SUB_PATH = "/*";

    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_PACKAGE_HTTP = "ballerina" + ORG_NAME_SEPARATOR + "http";
    public static final BPackage PROTOCOL_HTTP_PKG_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "http");
    public static final String HTTP_CALLER_NAME = "ballerina/http:Caller";
    public static final String HTTP_MOCK_SERVER_ENDPOINT_NAME = "Tballerina/http:MockListener;";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String RESOLVED_REQUESTED_URI = "RESOLVED_REQUESTED_URI";
    public static final String HTTP_REASON_PHRASE = "HTTP_REASON_PHRASE";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String PROTOCOL = "PROTOCOL";
    public static final String TO = "TO";
    public static final String LOCAL_ADDRESS = "LOCAL_ADDRESS";
    public static final String HTTP_VERSION = "HTTP_VERSION";
    public static final String MUTUAL_SSL_RESULT = "MUTUAL_SSL_HANDSHAKE_RESULT";
    public static final String LISTENER_PORT = "LISTENER_PORT";
    public static final String HTTP_DEFAULT_HOST = "0.0.0.0";
    public static final String TLS_STORE_TYPE = "tlsStoreType";
    public static final String PKCS_STORE_TYPE = "PKCS12";
    public static final String AUTO = "AUTO";
    public static final String ALWAYS = "ALWAYS";
    public static final String NEVER = "NEVER";
    public static final String FORWARDED_ENABLE = "enable";
    public static final String FORWARDED_TRANSITION = "transition";
    public static final String FORWARDED_DISABLE = "disable";
    public static final String DISABLE = "disable";
    public static final String DEFAULT_HOST = "b7a.default";

    public static final String HTTP_PACKAGE_PATH = "ballerina" + ORG_NAME_SEPARATOR + "http";

    public static final String HTTP_REQUEST_METHOD = "method";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_PATCH = "PATCH";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
    public static final String HTTP_METHOD_HEAD = "HEAD";

    /* Annotations */
    public static final String ANN_NAME_RESOURCE_CONFIG = "ResourceConfig";
    public static final String ANN_NAME_INTERRUPTIBLE = "interruptible";
    public static final String ANN_RESOURCE_ATTR_METHODS = "methods";
    public static final String ANN_RESOURCE_ATTR_PATH = "path";
    public static final String ANN_RESOURCE_ATTR_BODY = "body";
    public static final String ANN_RESOURCE_ATTR_CONSUMES = "consumes";
    public static final String ANN_RESOURCE_ATTR_PRODUCES = "produces";
    public static final String ANN_NAME_CONFIG = "configuration";
    public static final String ANN_NAME_HTTP_SERVICE_CONFIG = "ServiceConfig";
    public static final String ANN_CONFIG_ATTR_HOST = "host";
    public static final String ANN_CONFIG_ATTR_PORT = "port";
    public static final String ANN_CONFIG_ATTR_HTTP_VERSION = "httpVersion";
    public static final String ANN_CONFIG_ATTR_HTTPS_PORT = "httpsPort";
    public static final String ANN_CONFIG_ATTR_KEEP_ALIVE = "keepAlive";
    public static final String ANN_CONFIG_ATTR_BASE_PATH = "basePath";
    public static final String ANN_CONFIG_ATTR_SCHEME = "scheme";
    public static final String ANN_CONFIG_ATTR_TLS_STORE_TYPE = "tlsStoreType";
    public static final String ANN_CONFIG_ATTR_KEY_STORE_FILE = "keyStoreFile";
    public static final String ANN_CONFIG_ATTR_KEY_STORE_PASS = "keyStorePassword";
    public static final String ANN_CONFIG_ATTR_TRUST_STORE_FILE = "trustStoreFile";
    public static final String ANN_CONFIG_ATTR_TRUST_STORE_PASS = "trustStorePassword";
    public static final String ANN_CONFIG_ATTR_CERT_PASS = "certPassword";
    public static final String ANN_CONFIG_ATTR_SSL_VERIFY_CLIENT = "sslVerifyClient";
    public static final String ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final String ANN_CONFIG_ATTR_CIPHERS = "ciphers";
    public static final String ANN_CONFIG_ATTR_SSL_PROTOCOL = "sslProtocol";
    public static final String ANN_CONFIG_ATTR_VALIDATE_CERT_ENABLED = "validateCertEnabled";
    public static final String ANN_CONFIG_ATTR_COMPRESSION = "compression";
    public static final String ANN_CONFIG_ATTR_COMPRESSION_ENABLE = "enable";
    public static final String ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES = "contentTypes";
    public static final String ANN_CONFIG_ATTR_CACHE_SIZE = "cacheSize";
    public static final String ANN_CONFIG_ATTR_CACHE_VALIDITY_PERIOD = "cacheValidityPeriod";
    public static final String ANN_CONFIG_ATTR_WEBSOCKET = "webSocket";
    public static final String ANN_CONFIG_ATTR_MAXIMUM_URL_LENGTH = "maxUriLength";
    public static final String ANN_CONFIG_ATTR_MAXIMUM_HEADER_SIZE = "maxHeaderSize";
    public static final String ANN_CONFIG_ATTR_MAXIMUM_ENTITY_BODY_SIZE = "maxEntityBodySize";
    public static final String ANN_CONFIG_ATTR_CHUNKING = "chunking";
    public static final String ANN_CONFIG_ATTR_PATTERN = "pattern";
    public static final String ANN_CONFIG_ATTR_ALLOW_NO_VERSION = "allowNoVersion";
    public static final String ANN_CONFIG_ATTR_MATCH_MAJOR_VERSION = "matchMajorVersion";
    public static final String ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE = "webSocketUpgrade";
    public static final String ANN_WEBSOCKET_ATTR_UPGRADE_PATH = "upgradePath";
    public static final String ANNOTATION_METHOD_GET = HTTP_METHOD_GET;
    public static final String ANNOTATION_METHOD_POST = HTTP_METHOD_POST;
    public static final String ANNOTATION_METHOD_PUT = HTTP_METHOD_PUT;
    public static final String ANNOTATION_METHOD_PATCH = HTTP_METHOD_PATCH;
    public static final String ANNOTATION_METHOD_DELETE = HTTP_METHOD_DELETE;
    public static final String ANNOTATION_METHOD_OPTIONS = HTTP_METHOD_OPTIONS;
    public static final String ANN_NAME_PARAM_ORDER_CONFIG = "ParamOrderConfig";
    public static final String ANN_FIELD_PATH_PARAM_ORDER = "pathParamOrder";

    public static final String VALUE_ATTRIBUTE = "value";

    public static final String COOKIE_HEADER = "Cookie";
    public static final String SESSION_ID = "BSESSIONID=";
    public static final String PATH = "Path=";
    public static final String RESPONSE_COOKIE_HEADER = "Set-Cookie";
    public static final String SESSION = "Session";
    public static final String HTTP_ONLY = "HttpOnly";
    public static final String SECURE = "Secure";

    public static final String ALLOW_ORIGIN = "allowOrigins";
    public static final String ALLOW_CREDENTIALS = "allowCredentials";
    public static final String ALLOW_METHODS = "allowMethods";
    public static final String MAX_AGE = "maxAge";
    public static final String ALLOW_HEADERS = "allowHeaders";
    public static final String EXPOSE_HEADERS = "exposeHeaders";
    public static final String PREFLIGHT_RESOURCES = "PREFLIGHT_RESOURCES";
    public static final String RESOURCES_CORS = "RESOURCES_CORS";
    public static final String LISTENER_INTERFACE_ID = "listener.interface.id";
    public static final String PACKAGE_BALLERINA_BUILTIN = BALLERINA_BUILTIN_PKG;

    public static final String CLIENT = "Client";
    public static final String HTTP_CLIENT = "HttpClient";

    public static final String SRC_HANDLER = "SRC_HANDLER";
    public static final String REMOTE_ADDRESS = "REMOTE_ADDRESS";
    public static final String ORIGIN_HOST = "ORIGIN_HOST";
    public static final String POOLED_BYTE_BUFFER_FACTORY = "POOLED_BYTE_BUFFER_FACTORY";
    public static final String HTTP_SERVICE = "HTTP_SERVICE";
    public static final String VERSION = "{version}";
    public static final String DEFAULT_VERSION = "v.{major}.{minor}";
    public static final String MAJOR_VERSION = "{major}";
    public static final String MINOR_VERSION = "{minor}";

    /* Annotations */
    public static final String ANNOTATION_NAME_SOURCE = "Source";
    public static final String ANNOTATION_NAME_BASE_PATH = "BasePath";
    public static final String ANNOTATION_NAME_PATH = "Path";
    public static final String HTTP_CLIENT_EXCEPTION_CATEGORY = "http-client";
    public static final String SERVICE_ENDPOINT = "Listener";
    public static final String CALLER = "Caller";
    public static final String REMOTE = "Remote";
    public static final String LOCAL = "Local";
    public static final String REQUEST = "Request";
    public static final String RESPONSE = "Response";
    public static final String HTTP_FUTURE = "HttpFuture";
    public static final String PUSH_PROMISE = "PushPromise";
    public static final String ENTITY = "Entity";
    public static final String RESPONSE_CACHE_CONTROL = "ResponseCacheControl";
    public static final String REQUEST_CACHE_CONTROL = "RequestCacheControl";
    public static final String STRUCT_GENERIC_ERROR = "error";
    public static final String HTTP_TIMEOUT_ERROR = "HttpTimeoutError";
    public static final String TYPE_STRING = "string";
    public static final String TRANSPORT_MESSAGE = "transport_message";
    public static final String QUERY_PARAM_MAP = "queryParamMap";
    public static final String TRANSPORT_HANDLE = "transport_handle";
    public static final String TRANSPORT_PUSH_PROMISE = "transport_push_promise";
    public static final String MESSAGE_OUTPUT_STREAM = "message_output_stream";
    public static final String HTTP_SESSION = "http_session";
    public static final String MUTUAL_SSL_HANDSHAKE_RECORD = "MutualSslHandshake";

    public static final String HTTP_TRANSPORT_CONF = "transports.netty.conf";
    public static final String CIPHERS = "ciphers";
    public static final String SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final int OPTIONS_STRUCT_INDEX = 0;

    public static final int HTTP_MESSAGE_INDEX = 0;
    public static final int ENTITY_INDEX = 1;

    public static final String HTTP_ERROR_CODE = "{ballerina/http}HTTPError";
    public static final String HTTP_ERROR_RECORD = "HTTPError";
    public static final String HTTP_ERROR_MESSAGE = "message";

    // ServeConnector struct indices
    public static final String HTTP_CONNECTOR_CONFIG_FIELD = "config";
    public static final String SERVICE_ENDPOINT_CONFIG_FIELD = "config";
    public static final String SERVICE_ENDPOINT_CONNECTION_FIELD = "caller";

    //Connection struct indexes
    public static final int CONNECTION_HOST_INDEX = 0;
    public static final int CONNECTION_PORT_INDEX = 0;

    //Request struct field names
    public static final String REQUEST_RAW_PATH_FIELD = "rawPath";
    public static final String REQUEST_METHOD_FIELD = "method";
    public static final String REQUEST_VERSION_FIELD = "httpVersion";
    public static final String REQUEST_USER_AGENT_FIELD = "userAgent";
    public static final String REQUEST_EXTRA_PATH_INFO_FIELD = "extraPathInfo";
    public static final String REQUEST_CACHE_CONTROL_FIELD = "cacheControl";
    public static final String REQUEST_REUSE_STATUS_FIELD = "dirtyRequest";
    public static final String REQUEST_NO_ENTITY_BODY_FIELD = "noEntityBody";
    public static final String REQUEST_MUTUAL_SSL_HANDSHAKE_FIELD = "mutualSslHandshake";
    public static final String REQUEST_MUTUAL_SSL_HANDSHAKE_STATUS = "status";

    //Response struct field names
    public static final String RESPONSE_STATUS_CODE_FIELD = "statusCode";
    public static final String RESPONSE_REASON_PHRASE_FIELD = "reasonPhrase";
    public static final String RESPONSE_SERVER_FIELD = "server";
    public static final String RESOLVED_REQUESTED_URI_FIELD = "resolvedRequestedURI";
    public static final String RESPONSE_CACHE_CONTROL_FIELD = "cacheControl";
    public static final String IN_RESPONSE_RECEIVED_TIME_FIELD = "receivedTime";

    //PushPromise struct field names
    public static final String PUSH_PROMISE_PATH_FIELD = "path";
    public static final String PUSH_PROMISE_METHOD_FIELD = "method";

    //Proxy server struct field names
    public static final int PROXY_STRUCT_INDEX = 3;
    public static final String PROXY_HOST_INDEX = "host";
    public static final String PROXY_PORT_INDEX = "port";
    public static final String PROXY_USER_NAME_INDEX = "userName";
    public static final String PROXY_PASSWORD_INDEX = "password";

    //Connection Throttling struct field names
    public static final int CONNECTION_THROTTLING_STRUCT_INDEX = 4;
    public static final String CONNECTION_THROTTLING_MAX_ACTIVE_CONNECTIONS_INDEX = "maxActiveConnections";
    public static final String CONNECTION_THROTTLING_WAIT_TIME_INDEX = "waitTime";

    //Retry Struct field names
    public static final int RETRY_STRUCT_FIELD = 4;
    public static final String RETRY_COUNT_FIELD = "count";
    public static final String RETRY_INTERVAL_FIELD = "intervalInMillis";

    // ResponseCacheControl struct field names
    public static final String RES_CACHE_CONTROL_MUST_REVALIDATE_FIELD = "mustRevalidate";
    public static final String RES_CACHE_CONTROL_NO_CACHE_FIELD = "noCache";
    public static final String RES_CACHE_CONTROL_NO_STORE_FIELD = "noStore";
    public static final String RES_CACHE_CONTROL_NO_TRANSFORM_FIELD = "noTransform";
    public static final String RES_CACHE_CONTROL_IS_PRIVATE_FIELD = "isPrivate";
    public static final String RES_CACHE_CONTROL_PROXY_REVALIDATE_FIELD = "proxyRevalidate";
    public static final String RES_CACHE_CONTROL_MAX_AGE_FIELD = "maxAge";
    public static final String RES_CACHE_CONTROL_S_MAXAGE_FIELD = "sMaxAge";
    public static final String RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD = "noCacheFields";
    public static final String RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD = "privateFields";

    // RequestCacheControl struct field names
    public static final String REQ_CACHE_CONTROL_NO_CACHE_FIELD = "noCache";
    public static final String REQ_CACHE_CONTROL_NO_STORE_FIELD = "noStore";
    public static final String REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD = "noTransform";
    public static final String REQ_CACHE_CONTROL_ONLY_IF_CACHED_FIELD = "onlyIfCached";
    public static final String REQ_CACHE_CONTROL_MAX_AGE_FIELD = "maxAge";
    public static final String REQ_CACHE_CONTROL_MAX_STALE_FIELD = "maxStale";
    public static final String REQ_CACHE_CONTROL_MIN_FRESH_FIELD = "minFresh";

    public static final String CONNECTION_HEADER = "Connection";
    public static final String HEADER_VAL_CONNECTION_CLOSE = "Close";
    public static final String HEADER_VAL_CONNECTION_KEEP_ALIVE = "Keep-Alive";
    public static final String HEADER_VAL_100_CONTINUE = "100-continue";

    //Response codes
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String HEADER_X_XID = "x-b7a-xid";
    public static final String HEADER_X_REGISTER_AT_URL = "x-b7a-register-at";


    public static final String HTTP_SERVER_CONNECTOR = "HTTP_SERVER_CONNECTOR";
    public static final String HTTP_SERVICE_REGISTRY = "HTTP_SERVICE_REGISTRY";
    public static final String WS_SERVICE_REGISTRY = "WS_SERVICE_REGISTRY";
    public static final String CONNECTOR_STARTED = "CONNECTOR_STARTED";

    //Service Endpoint
    public static final int SERVICE_ENDPOINT_NAME_INDEX = 0;
    public static final String SERVICE_ENDPOINT_CONFIG = "config";
    public static final String SERVER_NAME = "server";
    public static final String LISTENER_CONFIGURATION = "ListenerConfiguration";

    //Service Endpoint Config
    public static final String ENDPOINT_CONFIG_HOST = "host";
    public static final String ENDPOINT_CONFIG_PORT = "port";
    public static final String ENDPOINT_CONFIG_KEEP_ALIVE = "keepAlive";
    public static final String ENDPOINT_CONFIG_TIMEOUT = "timeoutInMillis";
    public static final String ENDPOINT_CONFIG_CHUNKING = "chunking";
    public static final String ENDPOINT_CONFIG_VERSION = "httpVersion";
    public static final String ENDPOINT_REQUEST_LIMITS = "requestLimits";
    public static final String REQUEST_LIMITS_MAXIMUM_URL_LENGTH = "maxUriLength";
    public static final String REQUEST_LIMITS_MAXIMUM_HEADER_SIZE = "maxHeaderSize";
    public static final String REQUEST_LIMITS_MAXIMUM_ENTITY_BODY_SIZE = "maxEntityBodySize";
    public static final String ENDPOINT_CONFIG_PIPELINING = "pipelining";
    public static final String ENABLE_PIPELINING = "enable";
    public static final String PIPELINING_REQUEST_LIMIT = "maxPipelinedRequests";

    public static final String ENDPOINT_CONFIG_SECURE_SOCKET = "secureSocket";

    public static final String ENDPOINT_CONFIG_TRUST_STORE = "trustStore";
    public static final String FILE_PATH = "path";
    public static final String PASSWORD = "password";
    public static final String SSL_PROTOCOL_VERSION = "name";
    public static final String ENABLED_PROTOCOLS = "versions";
    public static final String ENABLE = "enable";
    public static final String ENDPOINT_CONFIG_OCSP_STAPLING = "ocspStapling";
    public static final String ENDPOINT_CONFIG_KEY_STORE = "keyStore";
    public static final String ENDPOINT_CONFIG_PROTOCOLS = "protocol";
    public static final String ENDPOINT_CONFIG_VALIDATE_CERT = "certValidation";
    public static final String ENDPOINT_CONFIG_CERTIFICATE = "certFile";
    public static final String ENDPOINT_CONFIG_KEY = "keyFile";
    public static final String ENDPOINT_CONFIG_KEY_PASSWORD = "keyPassword";
    public static final String ENDPOINT_CONFIG_TRUST_CERTIFICATES = "trustedCertFile";
    public static final String ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT = "handshakeTimeoutInSeconds";
    public static final String ENDPOINT_CONFIG_SESSION_TIMEOUT = "sessionTimeoutInSeconds";
    public static final String ENDPOINT_CONFIG_DISABLE_SSL = "disable";

    //SslConfiguration indexes
    public static final String SSL_CONFIG_SSL_VERIFY_CLIENT = "sslVerifyClient";
    public static final String SSL_CONFIG_CIPHERS = "ciphers";
    public static final String SSL_CONFIG_CACHE_SIZE = "cacheSize";
    public static final String SSL_CONFIG_CACHE_VALIDITY_PERIOD = "cacheValidityPeriod";
    public static final String SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED = "verifyHostname";
    public static final String SSL_CONFIG_ENABLE_SESSION_CREATION = "shareSession";

    //Client Endpoint (CallerActions)
    public static final String CLIENT_ENDPOINT_SERVICE_URI = "url";
    public static final String CLIENT_ENDPOINT_CONFIG = "config";
    public static final int CLIENT_ENDPOINT_CONFIG_INDEX = 0;
    public static final int CLIENT_ENDPOINT_URL_INDEX = 0;
    public static final int CLIENT_GLOBAL_POOL_INDEX = 1;

    //Client Endpoint Config
    public static final String CLIENT_EP_CHUNKING = "chunking";
    public static final String CLIENT_EP_ENDPOINT_TIMEOUT = "timeoutInMillis";
    public static final String CLIENT_EP_IS_KEEP_ALIVE = "keepAlive";
    public static final String CLIENT_EP_HTTP_VERSION = "httpVersion";
    public static final String CLIENT_EP_FORWARDED = "forwarded";
    public static final String TARGET_SERVICES = "targets";
    public static final String CLIENT_EP_ACCEPT_ENCODING = "acceptEncoding";
    public static final String HTTP2_PRIOR_KNOWLEDGE = "http2PriorKnowledge";
    public static final String HTTP1_SETTINGS = "http1Settings";
    public static final String HTTP2_SETTINGS = "http2Settings";

    //Connection Throttling field names
    public static final String CONNECTION_THROTTLING_STRUCT_REFERENCE = "connectionThrottling";
    public static final String CONNECTION_THROTTLING_MAX_ACTIVE_CONNECTIONS = "maxActiveConnections";
    public static final String CONNECTION_THROTTLING_WAIT_TIME = "waitTime";
    public static final String CONNECTION_THROTTLING_MAX_ACTIVE_STREAMS_PER_CONNECTION =
            "maxActiveStreamsPerConnection";

    //Client connection pooling configs
    public static final String CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS = "maxActiveConnections";
    public static final String CONNECTION_POOLING_MAX_IDLE_CONNECTIONS = "maxIdleConnections";
    public static final String CONNECTION_POOLING_WAIT_TIME = "waitTimeInMillis";
    public static final String CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION = "maxActiveStreamsPerConnection";
    public static final String HTTP_CLIENT_CONNECTION_POOL = "PoolConfiguration";
    public static final String CONNECTION_MANAGER = "ConnectionManager";
    public static final int POOL_CONFIG_INDEX = 1;
    public static final String USER_DEFINED_POOL_CONFIG = "poolConfig";

    //FollowRedirect field names
    public static final String FOLLOW_REDIRECT_STRUCT_REFERENCE = "followRedirects";
    public static final String FOLLOW_REDIRECT_ENABLED = "enabled";
    public static final String FOLLOW_REDIRECT_MAXCOUNT = "maxCount";

    //Proxy field names
    public static final String PROXY_STRUCT_REFERENCE = "proxy";
    public static final String PROXY_HOST = "host";
    public static final String PROXY_PORT = "port";
    public static final String PROXY_USERNAME = "userName";
    public static final String PROXY_PASSWORD = "password";

    public static final String HTTP_SERVICE_TYPE = "Service";
    // Filter related
    public static final String ENDPOINT_CONFIG_FILTERS = "filters";
    public static final String FILTERS = "FILTERS";
    public static final String HTTP_REQUEST_FILTER_FUNCTION_NAME = "filterRequest";

    // Retry Config
    public static final String CLIENT_EP_RETRY = "retry";
    public static final String RETRY_COUNT = "count";
    public static final String RETRY_INTERVAL = "intervalInMillis";

    public static final String SERVICE_ENDPOINT_PROTOCOL_FIELD = "protocol";

    //Remote struct field names
    public static final String REMOTE_STRUCT_FIELD = "remoteAddress";
    public static final String REMOTE_HOST_FIELD = "host";
    public static final String REMOTE_PORT_FIELD = "port";

    //Local struct field names
    public static final String LOCAL_STRUCT_INDEX = "localAddress";
    public static final String LOCAL_HOST_FIELD = "host";
    public static final String LOCAL_PORT_FIELD = "port";

    //WebSocket Related constants for WebSocket upgrade
    public static final String NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER = "NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER";

    public static final int REQUEST_STRUCT_INDEX = 1;
    public static final boolean DIRTY_REQUEST = true;
    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";

    public static final String MOCK_LISTENER_ENDPOINT = "MockListener";
    public static final String HTTP_LISTENER_ENDPOINT = "Listener";

    public static final String COLON = ":";
    public static final String DOLLAR = "$";
    public static final String DOWBLE_SLASH = "//";
    public static final String SINGLE_SLASH = "/";

    public static final String HTTP_VERSION_1_1 = "1.1";

    public static final String HTTP_MODULE_VERSION = "1.0.0";
    public static final String REASON_RECORD = "Reason";
    public static final String PACKAGE = "ballerina";
    public static final String MODULE = "http";

    // Ballerina error types related constants
    public static final String HTTP_ERROR_DETAIL_RECORD = "Detail";

    private HttpConstants() {
    }
}
