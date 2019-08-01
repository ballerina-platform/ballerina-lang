// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/crypto;
import ballerina/log;
import ballerina/system;
import ballerina/'lang\.object as lang;

/////////////////////////////
/// HTTP Listener Endpoint ///
/////////////////////////////
# This is used for creating HTTP server endpoints. An HTTP server endpoint is capable of responding to
# remote callers. The `Listener` is responsible for initializing the endpoint using the provided configurations.
public type Listener object {

    *lang:AbstractListener;

    private int port = 0;
    private ServiceEndpointConfiguration config = {};
    private string instanceId;

    public function __start() returns error? {
        return self.start();
    }

    public function __stop() returns error? {
        return self.stop();
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function __init(int port, public ServiceEndpointConfiguration? config = ()) {
        self.instanceId = system:uuid();
        self.config = config ?: {};
        self.port = port;
        self.init(self.config);
    }

    # Gets invoked during module initialization to initialize the endpoint.
    #
    # + c - Configurations for HTTP service endpoints
    public function init(ServiceEndpointConfiguration c) {
        self.config = c;
        var auth = self.config["auth"];
        if (auth is ListenerAuth) {
            var authHandlers = auth.authHandlers;
            if (authHandlers is InboundAuthHandler[]) {
                if (authHandlers.length() > 0) {
                    initListener(self.config);
                }
            } else {
                if (authHandlers[0].length() > 0) {
                    initListener(self.config);
                }
            }
        }
        var err = self.initEndpoint();
        if (err is error) {
            panic err;
        }
    }

    public function initEndpoint() returns error? = external;

    # Gets invoked when attaching a service to the endpoint.
    #
    # + s - The service that needs to be attached
    # + name - Name of the service
    # + return - An `error` if there is any error occurred during the service attachment process or else nil
    function register(service s, string? name) returns error? = external;

    # Starts the registered service.
    # + return - An `error` if there is any error occurred during the listener start process
    function start() returns error? = external;

    # Stops the registered service.
    function stop() = external;

    # Disengage an attached service from the listener.
    #
    # + s - The service that needs to be detached
    # + return - An `error` if there is any error occurred during the service detachment process or else nil
    public function detach(service s) returns error? = external;
};

function initListener(ServiceEndpointConfiguration config) {
    var secureSocket = config.secureSocket;
    if (secureSocket is ServiceSecureSocket) {
        addAuthFiltersForSecureListener(config);
    } else {
        error err = error("Secure sockets have not been cofigured in order to enable auth providers.");
        panic err;
    }
}

# Presents a read-only view of the remote address.
#
# + host - The remote host name/IP
# + port - The remote port
public type Remote record {|
    string host = "";
    int port = 0;
|};

# Presents a read-only view of the local address.
#
# + host - The local host name/IP
# + port - The local port
public type Local record {|
    string host = "";
    int port = 0;
|};

# Configures limits for requests. If these limits are violated, the request is rejected.
#
# + maxUriLength - Maximum allowed length for a URI. Exceeding this limit will result in a
#                  `414 - URI Too Long` response.
# + maxHeaderSize - Maximum allowed size for headers. Exceeding this limit will result in a
#                   `413 - Payload Too Large` response.
# + maxEntityBodySize - Maximum allowed size for the entity body. By default it is set to -1 which means there
#                       is no restriction `maxEntityBodySize`, On the Exceeding this limit will result in a
#                       `413 - Payload Too Large` response.
public type RequestLimits record {|
    int maxUriLength = 4096;
    int maxHeaderSize = 8192;
    int maxEntityBodySize = -1;
|};

# Provides a set of configurations for HTTP service endpoints.
#
# + host - The host name/IP of the endpoint
# + http1Settings - Configurations related to HTTP/1.x protocol
# + secureSocket - The SSL configurations for the service endpoint. This needs to be configured in order to
#                  communicate through HTTPS.
# + httpVersion - Highest HTTP version supported by the endpoint
# + requestLimits - Configures the parameters for request validation
# + filters - If any pre-processing needs to be done to the request before dispatching the request to the
#             resource, filters can applied
# + timeoutInMillis - Period of time in milliseconds that a connection waits for a read/write operation. Use value 0 to
#                   disable timeout
# + auth - Listener authenticaton configurations
# + server - The server name which should appear as a response header
public type ServiceEndpointConfiguration record {|
    string host = "0.0.0.0";
    ServiceHttp1Settings http1Settings = {};
    ServiceSecureSocket? secureSocket = ();
    string httpVersion = "1.1";
    RequestLimits requestLimits = {};
    //TODO: update as a optional field
    (RequestFilter | ResponseFilter)[] filters = [];
    int timeoutInMillis = DEFAULT_LISTENER_TIMEOUT;
    ListenerAuth auth?;
    string? server = ();
|};

# Provides settings related to HTTP/1.x protocol.
#
# + keepAlive - Can be set to either `KEEPALIVE_AUTO`, which respects the `connection` header, or `KEEPALIVE_ALWAYS`,
#               which always keeps the connection alive, or `KEEPALIVE_NEVER`, which always closes the connection
# + maxPipelinedRequests - Defines the maximum number of requests that can be processed at a given time on a single
#                          connection. By default 10 requests can be pipelined on a single cinnection and user can
#                          change this limit appropriately.
public type ServiceHttp1Settings record {|
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    int maxPipelinedRequests = MAX_PIPELINED_REQUESTS;
|};

# Authentication configurations for the listener.
#
# + authHandlers - An array of inbound authentication handlers or an array consisting of arrays of inbound authentication handlers.
# An array is used to indicate that at least one of the authentication handlers should be successfully authenticated. An array consisting of arrays
# is used to indicate that at least one authentication handler from the sub-arrays should be successfully authenticated.
# + scopes - An array of scopes or an array consisting of arrays of scopes. An array is used to indicate that at least one of the scopes should
# be successfully authorized. An array consisting of arrays is used to indicate that at least one scope from the sub-arrays
# should successfully be authorozed.
# + positiveAuthzCache - The caching configurations for positive authorizations.
# + negativeAuthzCache - The caching configurations for negative authorizations.
# + position - The authn/authz filter position of the filter array. The position values starts from 0 and it is set to 0 implicitly.
public type ListenerAuth record {|
    InboundAuthHandler[]|InboundAuthHandler[][] authHandlers;
    string[]|string[][] scopes?;
    AuthCacheConfig positiveAuthzCache = {};
    AuthCacheConfig negativeAuthzCache = {};
    int position = 0;
|};

# Configures the SSL/TLS options to be used for HTTP service.
#
# + trustStore - Configures the trust store to be used
# + keyStore - Configures the key store to be used
# + certFile - A file containing the certificate of the server
# + keyFile - A file containing the private key of the server
# + keyPassword - Password of the private key if it is encrypted
# + trustedCertFile - A file containing a list of certificates or a single certificate that the server trusts
# + protocol - SSL/TLS protocol related options
# + certValidation - Certificate validation against CRL or OCSP related options
# + ciphers - List of ciphers to be used (e.g.: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
#             TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA)
# + sslVerifyClient - The type of client certificate verification
# + shareSession - Enable/disable new SSL session creation
# + handshakeTimeoutInSeconds - SSL handshake time out
# + sessionTimeoutInSeconds - SSL session time out
# + ocspStapling - Enable/disable OCSP stapling
public type ServiceSecureSocket record {|
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
    string certFile = "";
    string keyFile = "";
    string keyPassword = "";
    string trustedCertFile = "";
    Protocols? protocol = ();
    ValidateCert? certValidation = ();
    string[] ciphers = ["TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                        "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256"];
    string sslVerifyClient = "";
    boolean shareSession = true;
    int? handshakeTimeoutInSeconds = ();
    int? sessionTimeoutInSeconds = ();
    ServiceOcspStapling? ocspStapling = ();
|};

# Provides a set of configurations for controlling the authorization caching behaviour of the endpoint.
#
# + enabled - Specifies whether authorization caching is enabled. Caching is enabled by default.
# + capacity - The capacity of the cache
# + expiryTimeInMillis - The number of milliseconds to keep an entry in the cache
# + evictionFactor - The fraction of entries to be removed when the cache is full. The value should be
#                    between 0 (exclusive) and 1 (inclusive).
public type AuthCacheConfig record {|
    boolean enabled = true;
    int capacity = 100;
    int expiryTimeInMillis = 5 * 1000; // 5 seconds;
    float evictionFactor = 1;
|};

# Defines the possible values for the keep-alive configuration in service and client endpoints.
public type KeepAlive KEEPALIVE_AUTO|KEEPALIVE_ALWAYS|KEEPALIVE_NEVER;

# Decides to keep the connection alive or not based on the `connection` header of the client request }
public const KEEPALIVE_AUTO = "AUTO";
# Keeps the connection alive irrespective of the `connection` header value }
public const KEEPALIVE_ALWAYS = "ALWAYS";
# Closes the connection irrespective of the `connection` header value }
public const KEEPALIVE_NEVER = "NEVER";

# Adds authentication and authorization filters.
#
# + config - `ServiceEndpointConfiguration` instance
function addAuthFiltersForSecureListener(ServiceEndpointConfiguration config) {
    // Add authentication and authorization filters as the first two filters if there are no any filters specified OR
    // the auth filter position is specified as 0. If there are any filters specified, the authentication and
    // authorization filters should be added into the position specified.

    var auth = config["auth"];
    if (auth is ListenerAuth) {
        InboundAuthHandler[]|InboundAuthHandler[][] authHandlers = auth.authHandlers;
        AuthnFilter authnFilter = new(authHandlers);

        var scopes = auth["scopes"];
        cache:Cache positiveAuthzCache = new(auth.positiveAuthzCache.expiryTimeInMillis,
                                             auth.positiveAuthzCache.capacity,
                                             auth.positiveAuthzCache.evictionFactor);
        cache:Cache negativeAuthzCache = new(auth.negativeAuthzCache.expiryTimeInMillis,
                                             auth.negativeAuthzCache.capacity,
                                             auth.negativeAuthzCache.evictionFactor);
        AuthzHandler authzHandler = new(positiveAuthzCache, negativeAuthzCache);
        AuthzFilter authzFilter = new(authzHandler, scopes);

        if (auth.position == 0) {
            config.filters.unshift(authnFilter, authzFilter);
        } else {
            if (auth.position < 0 || auth.position > config.filters.length()) {
                error err = error("Position of the auth filters should be beteween 0 and length of the filter array.");
                panic err;
            }
            int count = 0;
            while (count < auth.position) {
                config.filters.push(config.filters.shift());
                count += 1;
            }
            config.filters.unshift(authnFilter, authzFilter);
            while (count > 0) {
                config.filters.unshift(config.filters.pop());
                count -= 1;
            }
        }
    }
    // No need to validate else part since the function is called if and only if the `auth is ListenerAuth`
}

//////////////////////////////////
/// WebSocket Service Endpoint ///
//////////////////////////////////
# Represents a WebSocket service endpoint.
// public type WebSocketListener Listener;
public type WebSocketListener object {

    *lang:AbstractListener;

    private Listener httpEndpoint;

    public function __start() returns error? {
        return self.httpEndpoint.start();
    }

    public function __stop() returns error? {
        return self.httpEndpoint.stop();
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.httpEndpoint.register(s, name);
    }


    # Gets invoked during module initialization to initialize the endpoint.
    #
    # + port - The port of the endpoint
    # + config - The `ServiceEndpointConfiguration` of the endpoint
    public function __init(int port, ServiceEndpointConfiguration? config = ()) {
        self.httpEndpoint = new(port, config);
    }

};
