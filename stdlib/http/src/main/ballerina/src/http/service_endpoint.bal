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

import ballerina/java;
import ballerina/cache;
import ballerina/crypto;
import ballerina/lang.'object as lang;
import ballerina/runtime;
import ballerina/system;

/////////////////////////////
/// HTTP Listener Endpoint ///
/////////////////////////////
# This is used for creating HTTP server endpoints. An HTTP server endpoint is capable of responding to
# remote callers. The `Listener` is responsible for initializing the endpoint using the provided configurations.
public class Listener {

    *lang:Listener;

    private int port = 0;
    private ListenerConfiguration config = {};
    private string instanceId;

    # Starts the registered service programmatically.
    #
    # + return - An `error` if an error occurred during the listener starting process
    public function __start() returns error? {
        return self.startEndpoint();
    }

    # Stops the service listener gracefully. Already-accepted requests will be served before connection closure.
    #
    # + return - An `error` if an error occurred during the listener stopping process
    public function __gracefulStop() returns error? {
        return self.gracefulStop();
    }

    # Stops the service listener immediately. It is not implemented yet.
    #
    # + return - An `error` if an error occurred during the listener stop process
    public function __immediateStop() returns error? {
        error err = error("not implemented");
        return err;
    }

    # Attaches a service to the listener.
    #
    # + s - The service that needs to be attached
    # + name - Name of the service
    # + return - An `error` an error occurred during the service attachment process or else nil
    public function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    # Detaches a Http or WebSocket service from the listener. Note that detaching a WebSocket service would not affect
    # The functionality of the existing connections.
    #
    # + s - The service to be detached
    # + return - An `error` if one occurred during detaching of a service or else `()`
    public function __detach(service s) returns error? {
        return self.detach(s);
    }

    # Gets invoked during module initialization to initialize the listener.
    #
    # + port - Listening port of the HTTP service listener
    # + config - Configurations for the HTTP service listener
    public function init(int port, ListenerConfiguration? config = ()) {
        self.instanceId = system:uuid();
        self.config = config ?: {};
        self.port = port;
        ListenerAuth? auth = self.config["auth"];
        if (auth is ListenerAuth) {
            if (auth.mandateSecureSocket) {
                ListenerSecureSocket? secureSocket = self.config.secureSocket;
                if (secureSocket is ()) {
                    error err = error("Secure sockets have not been configured in order to enable auth providers.");
                    panic err;
                }
            }
            addAuthFilters(self.config);
        }
        addAttributeFilter(self.config);
        error? err = self.initEndpoint();
        if (err is error) {
            panic err;
        }
    }
    
    public function initEndpoint() returns error? {
        return externInitEndpoint(self);
    }

    # Gets invoked when attaching a service to the endpoint.
    #
    # + s - The service that needs to be attached
    # + name - Name of the service
    # + return - An `error` if an error occurred during the service attachment process or else nil
    function register(service s, string? name) returns error? {
        return externRegister(self, s, name);
    }

    # Starts the registered service.
    #
    # + return - An `error` if an error occurred during the listener start process
    function startEndpoint() returns error? {
        return externStart(self);
    }

    # Stops the service listener gracefully.
    #
    # + return - An `error` if an error occurred during the listener stop process
    function gracefulStop() returns error? {
        return externGracefulStop(self);
    }

    # Disengage an attached service from the listener.
    #
    # + s - The service that needs to be detached
    # + return - An `error` if an error occurred during the service detachment process or else nil
    function detach(service s) returns error? {
        return externDetach(self, s);
    }
}

function externInitEndpoint(Listener listenerObj) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.serviceendpoint.InitEndpoint",
    name: "initEndpoint"
} external;

function externRegister(Listener listenerObj, service s, string? name) returns error? = @java:Method {
   'class: "org.ballerinalang.net.http.serviceendpoint.Register",
   name: "register"
} external;

function externStart(Listener listenerObj) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.serviceendpoint.Start",
    name: "start"
} external;

function externGracefulStop(Listener listenerObj) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.serviceendpoint.GracefulStop",
    name: "gracefulStop"
} external;

function externDetach(Listener listenerObj, service s) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.serviceendpoint.Detach",
    name: "detach"
} external;

# Presents a read-only view of the remote address.
#
# + host - The remote host IP
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

# Provides a set of configurations for HTTP service endpoints.
#
# + host - The host name/IP of the endpoint
# + http1Settings - Configurations related to HTTP/1.x protocol
# + secureSocket - The SSL configurations for the service endpoint. This needs to be configured in order to
#                  communicate through HTTPS.
# + httpVersion - Highest HTTP version supported by the endpoint
# + filters - If any pre-processing needs to be done to the request before dispatching the request to the
#             resource, filters can applied
# + timeoutInMillis - Period of time in milliseconds that a connection waits for a read/write operation. Use value 0 to
#                   disable timeout
# + auth - Listener authenticaton configurations
# + server - The server name which should appear as a response header
# + webSocketCompressionEnabled - Enable support for compression in WebSocket
public type ListenerConfiguration record {|
    string host = "0.0.0.0";
    ListenerHttp1Settings http1Settings = {};
    ListenerSecureSocket? secureSocket = ();
    string httpVersion = "1.1";
    //TODO: update as a optional field
    (RequestFilter | ResponseFilter)[] filters = [];
    int timeoutInMillis = DEFAULT_LISTENER_TIMEOUT;
    ListenerAuth auth?;
    string? server = ();
    boolean webSocketCompressionEnabled = true;
|};

# Provides settings related to HTTP/1.x protocol.
#
# + keepAlive - Can be set to either `KEEPALIVE_AUTO`, which respects the `connection` header, or `KEEPALIVE_ALWAYS`,
#               which always keeps the connection alive, or `KEEPALIVE_NEVER`, which always closes the connection
# + maxPipelinedRequests - Defines the maximum number of requests that can be processed at a given time on a single
#                          connection. By default 10 requests can be pipelined on a single cinnection and user can
#                          change this limit appropriately.
# + maxUriLength - Maximum allowed length for a URI. Exceeding this limit will result in a
#                  `414 - URI Too Long` response.
# + maxHeaderSize - Maximum allowed size for headers. Exceeding this limit will result in a
#                   `413 - Payload Too Large` response.
# + maxEntityBodySize - Maximum allowed size for the entity body. By default it is set to -1 which means there
#                       is no restriction `maxEntityBodySize`, On the Exceeding this limit will result in a
#                       `413 - Payload Too Large` response.
public type ListenerHttp1Settings record {|
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    int maxPipelinedRequests = MAX_PIPELINED_REQUESTS;
    int maxUriLength = 4096;
    int maxHeaderSize = 8192;
    int maxEntityBodySize = -1;
|};

# Authentication configurations for the listener.
#
# + authHandlers - An array of inbound authentication handlers or an array consisting of arrays of inbound authentication handlers
# An array is used to indicate that at least one of the authentication handlers should be successfully authenticated. An array consisting of arrays
# is used to indicate that at least one authentication handler from the sub-arrays should be successfully authenticated.
# + scopes - An array of scopes or an array consisting of arrays of scopes. An array is used to indicate that at least one of the scopes should
# be successfully authorized. An array consisting of arrays is used to indicate that at least one scope from the sub-arrays
# should successfully be authorized
# + positiveAuthzCache - The `cache:Cache` object for positive authorizations
# + negativeAuthzCache - The `cache:Cache` object for negative authorizations
# + mandateSecureSocket - Specify whether secure socket configurations are mandatory or not
# + position - The authn/authz filter position of the filter array. The position values starts from 0 and it is set to 0 implicitly
public type ListenerAuth record {|
    InboundAuthHandlers authHandlers;
    Scopes scopes?;
    cache:Cache? positiveAuthzCache = new;
    cache:Cache? negativeAuthzCache = new;
    boolean mandateSecureSocket = true;
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
# + sslVerifyClient - The type of client certificate verification. (e.g.: "require" or "optional")
# + shareSession - Enable/disable new SSL session creation
# + handshakeTimeoutInSeconds - SSL handshake time out
# + sessionTimeoutInSeconds - SSL session time out
# + ocspStapling - Enable/disable OCSP stapling
public type ListenerSecureSocket record {|
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
    ListenerOcspStapling? ocspStapling = ();
|};

# Defines the possible values for the keep-alive configuration in service and client endpoints.
public type KeepAlive KEEPALIVE_AUTO|KEEPALIVE_ALWAYS|KEEPALIVE_NEVER;

# Decides to keep the connection alive or not based on the `connection` header of the client request }
public const KEEPALIVE_AUTO = "AUTO";
# Keeps the connection alive irrespective of the `connection` header value }
public const KEEPALIVE_ALWAYS = "ALWAYS";
# Closes the connection irrespective of the `connection` header value }
public const KEEPALIVE_NEVER = "NEVER";

# Constant for the service name reference.
public const SERVICE_NAME = "SERVICE_NAME";
# Constant for the resource name reference.
public const RESOURCE_NAME = "RESOURCE_NAME";
# Constant for the request method reference.
public const REQUEST_METHOD = "REQUEST_METHOD";

# Adds authentication and authorization filters.
#
# + config - `ServiceEndpointConfiguration` instance
function addAuthFilters(ListenerConfiguration config) {
    // Add authentication and authorization filters as the first two filters if there are no any filters specified OR
    // the auth filter position is specified as 0. If there are any filters specified, the authentication and
    // authorization filters should be added into the position specified.

    ListenerAuth? auth = config["auth"];
    if (auth is ListenerAuth) {
        InboundAuthHandlers authHandlers = auth.authHandlers;
        AuthnFilter authnFilter = new(authHandlers);

        cache:Cache? positiveAuthzCache = auth.positiveAuthzCache ?: ();
        cache:Cache? negativeAuthzCache = auth.negativeAuthzCache ?: ();
        AuthzHandler authzHandler = new(positiveAuthzCache, negativeAuthzCache);
        Scopes? scopes = auth["scopes"];
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

class AttributeFilter {

    *RequestFilter;

    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean {
        runtime:InvocationContext ctx = runtime:getInvocationContext();
        ctx.attributes[SERVICE_NAME] = context.getServiceName();
        ctx.attributes[RESOURCE_NAME] = context.getResourceName();
        ctx.attributes[REQUEST_METHOD] = request.method;
        return true;
    }
}

function addAttributeFilter(ListenerConfiguration config) {
    AttributeFilter attributeFilter = new;
    config.filters.unshift(attributeFilter);
}
