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

import ballerina/auth;
import ballerina/log;
import ballerina/system;

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
    private string instanceId;

    public new() {
        instanceId = system:uuid();
    }

    # Gets invoked during module initialization to initialize the endpoint.
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
# + authProviders - The array of authentication providers which are used to authenticate the users
# + positiveAuthzCache - Caching configurations for positive authorizations
# + negativeAuthzCache - Caching configurations for negative authorizations
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
    AuthProvider[]? authProviders;
    AuthCacheConfig positiveAuthzCache;
    AuthCacheConfig negativeAuthzCache;
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
    string[] ciphers = ["TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
    "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
    "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256"];
    string sslVerifyClient;
    boolean shareSession = true;
    ServiceOcspStapling? ocspStapling;
    !...
};

# Provides a set of configurations for controlling the authorization caching behaviour of the endpoint.
#
# + enabled - Specifies whether authorization caching is enabled. Caching is enabled by default.
# + capacity - The capacity of the cache
# + expiryTimeMillis - The number of milliseconds to keep an entry in the cache
# + evictionFactor - The fraction of entries to be removed when the cache is full. The value should be
#                    between 0 (exclusive) and 1 (inclusive).
public type AuthCacheConfig record {
    boolean enabled = true;
    int capacity = 100;
    int expiryTimeMillis = 5 * 1000; // 5 seconds;
    float evictionFactor = 1;
    !...
};

# Configuration for authentication providers.
#
# + scheme - Authentication scheme
# + id - Authentication provider instance id
# + authStoreProvider - Authentication store provider (file, LDAP, etc.) implementation
# + authStoreProviderConfig - Auth store related configurations
# + issuer - Identifier of the token issuer
# + audience - Identifier of the token recipients
# + trustStore - Trustore configurations
# + certificateAlias - Token signed key alias
# + clockSkew - Time in seconds to mitigate clock skew
# + keyStore - `KeyStore` instance providing key store related configurations
# + keyAlias - The Key Alias
# + keyPassword - The Key password
# + expTime - Expiry time
# + signingAlg - The signing algorithm which is used to sign the JWT token
# + propagateJwt - `true` if propagating authentication info as JWT
public type AuthProvider record {
    string scheme;
    string id;
    string authStoreProvider;
    auth:LdapAuthProviderConfig? authStoreProviderConfig;
    string issuer;
    string audience;
    TrustStore? trustStore;
    string certificateAlias;
    int clockSkew;
    KeyStore? keyStore;
    string keyAlias;
    string keyPassword;
    int expTime;
    string signingAlg;
    boolean propagateJwt;
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
    match self.config.authProviders {
        AuthProvider[] providers => {
            match self.config.secureSocket {
                ServiceSecureSocket secureSocket => addAuthFiltersForSecureListener(self.config, self.instanceId);
                () => {
                    error err = { message: "Secure sockets have not been cofigured in order to enable auth providers." };
                    throw err;
                }
            }
        }
        () => {}
    }
    var err = self.initEndpoint();
    if (err != null) {
        throw err;
    }
}

# Add authn and authz filters
#
# + config - `ServiceEndpointConfiguration` instance
# + instanceId - Endpoint instance id
function addAuthFiltersForSecureListener(ServiceEndpointConfiguration config, string instanceId) {
    // add authentication and authorization filters as the first two filters.
    // if there are any other filters specified, those should be added after the authn and authz filters.
    if (config.filters == null) {
        // can add authn and authz filters directly
        config.filters = createAuthFiltersForSecureListener(config, instanceId);
    } else {
        Filter[] newFilters = createAuthFiltersForSecureListener(config, instanceId);
        // add existing filters next
        int i = 0;
        while (i < lengthof config.filters) {
            newFilters[i + (lengthof newFilters)] = config.filters[i];
            i = i + 1;
        }
        config.filters = newFilters;
    }
}

# Create an array of auth and authz filters.
#
# + config - `ServiceEndpointConfiguration` instance
# + instanceId - Endpoint instance id
# + return - Array of Filters comprising of authn and authz Filters
function createAuthFiltersForSecureListener(ServiceEndpointConfiguration config, string instanceId) returns (Filter[]) {
    // parse and create authentication handlers
    AuthHandlerRegistry registry;
    AuthProvider[] authProviderList;
    Filter[] authFilters = [];

    match config.authProviders {
        AuthProvider[] providers => authProviderList = providers;
        () => return authFilters;
    }

    foreach provider in authProviderList {
        if (lengthof provider.id > 0) {
            registry.add(provider.id, createAuthHandler(provider, instanceId));
        } else {
            string providerId = system:uuid();
            registry.add(providerId, createAuthHandler(provider, instanceId));
        }
    }

    AuthnHandlerChain authnHandlerChain = new(registry);
    AuthnFilter authnFilter = new(authnHandlerChain);
    cache:Cache positiveAuthzCache = new(expiryTimeMillis = config.positiveAuthzCache.expiryTimeMillis,
        capacity = config.positiveAuthzCache.capacity,
        evictionFactor = config.positiveAuthzCache.evictionFactor);
    cache:Cache negativeAuthzCache = new(expiryTimeMillis = config.negativeAuthzCache.expiryTimeMillis,
        capacity = config.negativeAuthzCache.capacity,
        evictionFactor = config.negativeAuthzCache.evictionFactor);
    auth:AuthStoreProvider authStoreProvider;

    foreach provider in authProviderList {
        if (provider.scheme == AUTHN_SCHEME_BASIC) {
            if (provider.authStoreProvider == AUTH_PROVIDER_LDAP) {
                match provider.authStoreProviderConfig {
                    auth:LdapAuthProviderConfig authStoreProviderConfig => {
                        auth:LdapAuthStoreProvider ldapAuthStoreProvider = new(authStoreProviderConfig, instanceId);
                        authStoreProvider = <auth:AuthStoreProvider>ldapAuthStoreProvider;
                    }
                    () => {
                        error e = { message: "Authstore config not provided for : " + provider.authStoreProvider };
                        throw e;
                    }
                }
            } else if (provider.authStoreProvider == AUTH_PROVIDER_CONFIG) {
                auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
                authStoreProvider = <auth:AuthStoreProvider>configAuthStoreProvider;
            } else {
                error configError = { message: "Unsupported auth store provider : " + provider.authStoreProvider };
                throw configError;
            }
        }
    }

    HttpAuthzHandler authzHandler = new(authStoreProvider, positiveAuthzCache, negativeAuthzCache);
    AuthzFilter authzFilter = new(authzHandler);
    authFilters[0] = authnFilter;
    authFilters[1] = authzFilter;
    return authFilters;
}

function createBasicAuthHandler() returns HttpAuthnHandler {
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    auth:AuthStoreProvider authStoreProvider = <auth:AuthStoreProvider>configAuthStoreProvider;
    HttpBasicAuthnHandler basicAuthHandler = new(authStoreProvider);
    return <HttpAuthnHandler>basicAuthHandler;
}

function createAuthHandler(AuthProvider authProvider, string instanceId) returns HttpAuthnHandler {
    if (authProvider.scheme == AUTHN_SCHEME_BASIC) {
        auth:AuthStoreProvider authStoreProvider;
        if (authProvider.authStoreProvider == AUTH_PROVIDER_CONFIG) {
            if (authProvider.propagateJwt) {
                auth:ConfigJwtAuthProvider configAuthProvider = new(getInferredJwtAuthProviderConfig(authProvider));
                authStoreProvider = <auth:AuthStoreProvider>configAuthProvider;
            } else {
                auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
                authStoreProvider = <auth:AuthStoreProvider>configAuthStoreProvider;
            }
        } else if (authProvider.authStoreProvider == AUTH_PROVIDER_LDAP) {
            match authProvider.authStoreProviderConfig {
                auth:LdapAuthProviderConfig authStoreProviderConfig => {
                    auth:LdapAuthStoreProvider ldapAuthStoreProvider = new(authStoreProviderConfig, instanceId);
                    if (authProvider.propagateJwt) {
                        auth:LdapJwtAuthProvider ldapAuthProvider =
                        new(getInferredJwtAuthProviderConfig(authProvider),ldapAuthStoreProvider);
                        authStoreProvider = <auth:AuthStoreProvider>ldapAuthProvider;
                    } else {
                        authStoreProvider = <auth:AuthStoreProvider>ldapAuthStoreProvider;
                    }
                }
                () => {
                    error e = {message: "Authstore config not provided for : " + authProvider.authStoreProvider };
                    throw e;
                }
            }
        } else {
            // other auth providers are unsupported yet
            error e = {message: "Invalid auth provider: " + authProvider.authStoreProvider };
            throw e;
        }
        HttpBasicAuthnHandler basicAuthHandler = new(authStoreProvider);
        return <HttpAuthnHandler>basicAuthHandler;
    } else if (authProvider.scheme == AUTH_SCHEME_JWT){
        auth:JWTAuthProviderConfig jwtConfig = {};
        jwtConfig.issuer = authProvider.issuer;
        jwtConfig.audience = authProvider.audience;
        jwtConfig.certificateAlias = authProvider.certificateAlias;
        jwtConfig.clockSkew = authProvider.clockSkew;
        jwtConfig.trustStoreFilePath = authProvider.trustStore.path but { () => "" };
        jwtConfig.trustStorePassword = authProvider.trustStore.password but { () => "" };
        auth:JWTAuthProvider jwtAuthProvider = new(jwtConfig);
        HttpJwtAuthnHandler jwtAuthnHandler = new(jwtAuthProvider);
        return <HttpAuthnHandler>jwtAuthnHandler;
    } else {
        error e = {message: "Invalid auth scheme: " + authProvider.scheme};
        throw e;
    }
}

function getInferredJwtAuthProviderConfig(AuthProvider authProvider) returns auth:InferredJwtAuthProviderConfig {
    //ConfigJwtAuthProviderConfig
    string defaultIssuer = "ballerina";
    string defaultAudience = "ballerina";
    int defaultExpTime = 300; // in seconds
    string defaultSignAlg = "RS256";

    auth:InferredJwtAuthProviderConfig jwtAuthConfig = {};
    jwtAuthConfig.issuer = authProvider.issuer == "" ? defaultIssuer : authProvider.issuer;
    jwtAuthConfig.expTime = authProvider.expTime == 0 ? defaultExpTime : authProvider.expTime;
    jwtAuthConfig.signingAlg = authProvider.signingAlg == "" ? defaultSignAlg : authProvider.signingAlg;
    jwtAuthConfig.audience = authProvider.audience == "" ? defaultAudience : authProvider.audience;
    jwtAuthConfig.keyAlias = authProvider.keyAlias;
    jwtAuthConfig.keyPassword = authProvider.keyPassword;
    jwtAuthConfig.keyStoreFilePath = authProvider.keyStore.path but { () => "" };
    jwtAuthConfig.keyStorePassword = authProvider.keyStore.password but { () => "" };
    return jwtAuthConfig;
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

    # Gets invoked during module initialization to initialize the endpoint.
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
