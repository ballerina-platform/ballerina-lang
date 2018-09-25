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

# Defines Secure Listener endpoint.
#
# + config - SecureEndpointConfiguration instance
# + httpListener - HTTP Listener instance
public type SecureListener object {

    public SecureEndpointConfiguration config;
    public Listener httpListener;

    new() {
        httpListener = new;
    }

    # Gets called when the endpoint is being initialize during package init time.
    #
    # + c - The `SecureEndpointConfiguration` of the endpoint
    public function init(SecureEndpointConfiguration c);

    # Initializes the endpoint.
    #
    # + return - An `error` if an error occurs during initialization of the endpoint
    public function initEndpoint() returns (error);

    # Gets called every time a service attaches itself to this endpoint. Also happens at package initialization.
    #
    # + serviceType - The type of the service to be registered
    public function register(typedesc serviceType);

    # Starts the registered service.
    public function start();

    # Returns the connector that client code uses.
    #
    # + return - The connector that client code uses
    public function getCallerActions() returns (Connection);

    # Stops the registered service.
    public function stop();
};

# Configuration for secure HTTP service endpoint.
#
# + host - Host of the endpoint
# + port - Port of the endpoint
# + keepAlive - The keepAlive behaviour of the endpoint
# + secureSocket - The SSL configurations for the `endpoint`
# + httpVersion - Highest HTTP version supported
# + requestLimits - Request validation limits configuration
# + filters - Filters to be applied to the request before being dispatched to the actual `resource`
# + timeoutMillis - Period of time in milliseconds that a connection waits for a read/write operation. Use value 0
#                   to disable timeout
# + maxPipelinedRequests - Defines the maximum number of requests that can be processed at a given time on a single
#                          connection. By default 10 requests can be pipelined on a single cinnection and user can
#                          change this limit appropriately. This will be applicable only for HTTP 1.1
# + authProviders - The array of authentication providers which are used to authenticate the users
public type SecureEndpointConfiguration record {
    string host;
    int port = 9090;
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    ServiceSecureSocket? secureSocket;
    string httpVersion = "1.1";
    RequestLimits? requestLimits;
    Filter[] filters;
    int timeoutMillis = DEFAULT_LISTENER_TIMEOUT;
    int maxPipelinedRequests = MAX_PIPELINED_REQUESTS;
    AuthProvider[]? authProviders;
    !...
};

# Configuration for authentication providers.
#
# + scheme - Authentication scheme
# + id - Authentication provider instance id
# + authStoreProvider - Authentication store provider (file, LDAP, etc.) implementation
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
# + propagateToken - `true` if propagating authentication info as JWT
public type AuthProvider record {
    string scheme;
    string id;
    string authStoreProvider;
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
    boolean propagateToken;
    !...
};

function SecureListener::init(SecureEndpointConfiguration c) {
    addAuthFiltersForSecureListener(c);
    self.httpListener.init(c);
}

# Add authn and authz filters
#
# + config - `SecureEndpointConfiguration` instance
function addAuthFiltersForSecureListener(SecureEndpointConfiguration config) {
    // add authentication and authorization filters as the first two filters.
    // if there are any other filters specified, those should be added after the authn and authz filters.
    if (config.filters == null) {
        // can add authn and authz filters directly
        config.filters = createAuthFiltersForSecureListener(config);
    } else {
        Filter[] newFilters = createAuthFiltersForSecureListener(config);
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
# + config - `SecureEndpointConfiguration` instance
# + return - Array of Filters comprising of authn and authz Filters
function createAuthFiltersForSecureListener(SecureEndpointConfiguration config) returns (Filter[]) {
    // parse and create authentication handlers
    AuthHandlerRegistry registry;
    match config.authProviders {
        AuthProvider[] providers => {
            int i = 1;
            foreach provider in providers {
                if (lengthof provider.id > 0) {
                    registry.add(provider.id, createAuthHandler(provider));
                } else {
                    registry.add(provider.scheme + "-" + i, createAuthHandler(provider));
                }
                i++;
            }
        }
        () => {
            // if no auth providers are specified, add basic authn handler with config based auth provider
            registry.add("basic", createBasicAuthHandler());
        }
    }
    Filter[] authFilters = [];
    AuthnHandlerChain authnHandlerChain = new(registry);
    AuthnFilter authnFilter = new(authnHandlerChain);
    cache:Cache authzCache = new(expiryTimeMillis = 300000);
    auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
    auth:AuthStoreProvider authStoreProvider = <auth:AuthStoreProvider>configAuthStoreProvider;
    HttpAuthzHandler authzHandler = new(authStoreProvider, authzCache);
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

function createAuthHandler(AuthProvider authProvider) returns HttpAuthnHandler {
    if (authProvider.scheme == AUTHN_SCHEME_BASIC) {
        auth:AuthStoreProvider authStoreProvider;
        if (authProvider.authStoreProvider == AUTH_PROVIDER_CONFIG) {
            if (authProvider.propagateToken) {
                auth:ConfigJwtAuthProvider configAuthProvider = new(getConfigJwtAuthProviderConfig(authProvider));
                authStoreProvider = <auth:AuthStoreProvider>configAuthProvider;
            } else {
                auth:ConfigAuthStoreProvider configAuthStoreProvider = new;
                authStoreProvider = <auth:AuthStoreProvider>configAuthStoreProvider;
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
        // TODO: create other HttpAuthnHandlers
        error e = {message: "Invalid auth scheme: " + authProvider.scheme};
        throw e;
    }
}

function SecureListener::register(typedesc serviceType) {
    self.httpListener.register(serviceType);
}

function SecureListener::initEndpoint() returns (error) {
    return self.httpListener.initEndpoint();
}

function SecureListener::start() {
    self.httpListener.start();
}

function SecureListener::getCallerActions() returns (SecureListenerActions) {
    SecureListenerActions secureListenerActions = new (self.httpListener.getCallerActions());
    return secureListenerActions;
}

function SecureListener::stop() {
    self.httpListener.stop();
}

function getConfigJwtAuthProviderConfig(AuthProvider authProvider) returns auth:ConfigJwtAuthProviderConfig {
    //ConfigJwtAuthProviderConfig
    string defaultIssuer = "ballerina";
    string defaultAudience = "ballerina";
    int defaultExpTime = 300; // in seconds
    string defaultSignAlg = "RS256";

    auth:ConfigJwtAuthProviderConfig configjwtAuth = {};
    configjwtAuth.issuer = authProvider.issuer == "" ? defaultIssuer : authProvider.issuer;
    configjwtAuth.expTime = authProvider.expTime == 0 ? defaultExpTime : authProvider.expTime;
    configjwtAuth.signingAlg = authProvider.signingAlg == "" ? defaultSignAlg : authProvider.signingAlg;
    configjwtAuth.audience = authProvider.audience == "" ? defaultAudience : authProvider.audience;
    configjwtAuth.keyAlias = authProvider.keyAlias;
    configjwtAuth.keyPassword = authProvider.keyPassword;
    configjwtAuth.keyStoreFilePath = authProvider.keyStore.path but { () => "" };
    configjwtAuth.keyStorePassword = authProvider.keyStore.password but { () => "" };
    return configjwtAuth;
}

# The caller actions for responding to client requests to secure listener.
#
# + httpCallerActions - HTTP caller actions reference
public type SecureListenerActions object {

    public Connection httpCallerActions;

    # The secure listener caller actions initializer.
    #
    # + httpCallerActions - HTTP caller actions reference
    new (httpCallerActions) {}

    # Sends the outbound response to the caller.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - Returns an `error` if failed to respond
    public function respond(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns error? {
        return httpCallerActions.respond(message);
    }

    # Pushes a promise to the caller.
    #
    # + promise - Push promise message
    # + return - An `error` in case of failures
    public function promise(PushPromise promise) returns error? {
        return httpCallerActions.promise(promise);
    }

    # Sends a promised push response to the caller.
    #
    # + promise - Push promise message
    # + response - The outbound response
    # + return - An `error` in case of failures while responding with the promised response
    public function pushPromisedResponse(PushPromise promise, Response response) returns error? {
        return httpCallerActions.pushPromisedResponse(promise, response);
    }

    # Sends an upgrade request with custom headers.
    #
    # + headers - A `map` of custom headers for handshake
    # + return - WebSocket service endpoint
    public function acceptWebSocketUpgrade(map<string> headers) returns WebSocketListener {
        return httpCallerActions.acceptWebSocketUpgrade(headers);
    }

    # Cancels the handshake.
    #
    # + status - Error Status code for cancelling the upgrade and closing the connection.
    #            This error status code need to be 4xx or 5xx else the default status code would be 400.
    # + reason - Reason for cancelling the upgrade
    # + return - An `error` if an error occurs during cancelling the upgrade or nil
    public function cancelWebSocketUpgrade(int status, string reason) returns error|() {
        return httpCallerActions.cancelWebSocketUpgrade(status, reason);
    }

    # Sends a `100-continue` response to the caller.
    #
    # + return - Returns an `error` if failed to send the `100-continue` response
    public function continue() returns error? {
        return httpCallerActions.continue();
    }

    # Sends a redirect response to the user with the specified redirection status code.
    #
    # + response - Response to be sent to the caller
    # + code - The redirect status code to be sent
    # + locations - An array of URLs to which the caller can redirect to
    # + return - Returns an `error` if failed to send the redirect response
    public function redirect(Response response, RedirectCode code, string[] locations) returns error? {
        return httpCallerActions.redirect(response, code, locations);
    }
};
