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

/////////////////////////////
/// HTTP Service Endpoint ///
/////////////////////////////
# This is used for creating HTTP service endpoints. An HTTP service endpoint is capable of responding to
# remote callers. The `Listener` is responsible for initializing the endpoint using the provided configurations and
# providing the actions for communicating with the caller.
#
# + remote - The remote address
# + local - The local address
# + protocol - The protocol associated with the service endpoint
public type Listener object {

    @readonly public Remote remote;
    @readonly public Local local;
    @readonly public string protocol;

    private Connection conn;
    private ServiceEndpointConfiguration config;

    # Gets invoked during package initialization to initialize the endpoint.
    #
    # + c - Configurations for HTTP service endpoints
    public function init(ServiceEndpointConfiguration c);

    public extern function initEndpoint() returns error;

    # Gets invoked when binding a service to the endpoint.
    #
    # + serviceType - The type of the service to be registered
    public extern function register(typedesc serviceType);

    # Starts the registered service.
    public extern function start();

    # Returns the connector that client code uses.
    #
    # + return - The connector that client code uses
    public extern function getCallerActions() returns (Connection);

    # Stops the registered service.
    public extern function stop();
};

# Presents a read-only view of the remote address.
#
# + host - The remote host name/IP
# + port - The remote port
public type Remote record {
    @readonly string host;
    @readonly int port;
    !...
};

# Presents a read-only view of the local address.
#
# + host - The local host name/IP
# + port - The local port
public type Local record {
    @readonly string host;
    @readonly int port;
    !...
};

# Configures limits for requests. If these limits are violated, the request is rejected.
#
# + maxUriLength - Maximum allowed length for a URI. Exceeding this limit will result in a
#                  `414 - URI Too Long` response.
# + maxHeaderSize - Maximum allowed size for headers. Exceeding this limit will result in a
#                   `413 - Payload Too Large` response.
# + maxEntityBodySize - Maximum allowed size for the entity body. Exceeding this limit will result in a
#                       `413 - Payload Too Large` response.
public type RequestLimits record {
    int maxUriLength = -1;
    int maxHeaderSize = -1;
    int maxEntityBodySize = -1;
    !...
};

# Provides a set of configurations for HTTP service endpoints.
#
# + host - The host name/IP of the endpoint
# + port - The port to which the endpoint should bind to
# + keepAlive - Can be set to either `KEEPALIVE_AUTO`, which respects the `connection` header, or `KEEPALIVE_ALWAYS`,
#               which always keeps the connection alive, or `KEEPALIVE_NEVER`, which always closes the connection
# + secureSocket - The SSL configurations for the service endpoint. This needs to be configured in order to
#                  communicate through HTTPS.
# + httpVersion - Highest HTTP version supported by the endpoint
# + requestLimits - Configures the parameters for request validation
# + filters - If any pre-processing needs to be done to the request before dispatching the request to the
#             resource, filters can applied
# + timeoutMillis - Period of time in milliseconds that a connection waits for a read/write operation. Use value 0 to
#                   disable timeout
# + maxPipelinedRequests - Defines the maximum number of requests that can be processed at a given time on a single
#                          connection. By default 10 requests can be pipelined on a single cinnection and user can
#                          change this limit appropriately. This will be applicable only for HTTP 1.1
public type ServiceEndpointConfiguration record {
    string host;
    int port;
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    ServiceSecureSocket? secureSocket;
    string httpVersion = "1.1";
    RequestLimits? requestLimits;
    Filter[] filters;
    int timeoutMillis = DEFAULT_LISTENER_TIMEOUT;
    int maxPipelinedRequests = MAX_PIPELINED_REQUESTS;
    !...
};

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
# + ocspStapling - Enable/disable OCSP stapling
public type ServiceSecureSocket record {
    TrustStore? trustStore;
    KeyStore? keyStore;
    string certFile;
    string keyFile;
    string keyPassword;
    string trustedCertFile;
    Protocols? protocol;
    ValidateCert? certValidation;
    string[] ciphers;
    string sslVerifyClient;
    boolean shareSession = true;
    ServiceOcspStapling? ocspStapling;
    !...
};

# Defines the possible values for the keep-alive configuration in service and client endpoints.
public type KeepAlive "AUTO"|"ALWAYS"|"NEVER";

# Decides to keep the connection alive or not based on the `connection` header of the client request }
@final public KeepAlive KEEPALIVE_AUTO = "AUTO";
# Keeps the connection alive irrespective of the `connection` header value }
@final public KeepAlive KEEPALIVE_ALWAYS = "ALWAYS";
# Closes the connection irrespective of the `connection` header value }
@final public KeepAlive KEEPALIVE_NEVER = "NEVER";

function Listener::init (ServiceEndpointConfiguration c) {
    self.config = c;
    var err = self.initEndpoint();
    if (err != null) {
        throw err;
    }
}

//////////////////////////////////
/// WebSocket Service Endpoint ///
//////////////////////////////////
# Represents a WebSocket service endpoint.
#
# + id - The connection ID
# + negotiatedSubProtocol - The subprotocols negotiated with the client
# + isSecure - `true` if the connection is secure
# + isOpen - `true` if the connection is open
# + attributes - A `map` to store connection related attributes
public type WebSocketListener object {

    @readonly public string id;
    @readonly public string negotiatedSubProtocol;
    @readonly public boolean isSecure;
    @readonly public boolean isOpen;
    @readonly public map attributes;

    private WebSocketConnector conn;
    private ServiceEndpointConfiguration config;
    private Listener httpEndpoint;

    public new() {
    }

    # Gets invoked during package initialization to initialize the endpoint.
    #
    # + c - The `ServiceEndpointConfiguration` of the endpoint
    public function init(ServiceEndpointConfiguration c) {
        self.config = c;
        httpEndpoint.init(c);
    }

    # Gets invoked when binding a service to the endpoint.
    #
    # + serviceType - The service type
    public function register(typedesc serviceType) {
        httpEndpoint.register(serviceType);
    }

    # Starts the registered service.
    public function start() {
        httpEndpoint.start();
    }

    # Returns a WebSocket actions provider which can be used to communicate with the remote host.
    #
    # + return - The connector that listener endpoint uses
    public function getCallerActions() returns (WebSocketConnector) {
        return conn;
    }

    # Stops the registered service.
    public function stop() {
        WebSocketConnector webSocketConnector = getCallerActions();
        check webSocketConnector.close(statusCode = 1001, reason = "going away", timeoutInSecs = 0);
        httpEndpoint.stop();
    }
};
