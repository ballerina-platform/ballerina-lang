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

documentation {
    Defines Secure Listener endpoint.

    F{{config}} SecureEndpointConfiguration instance
    F{{httpListener}} HTTP Listener instance
}
public type SecureListener object {

    public SecureEndpointConfiguration config;
    public Listener httpListener;

    new() {
        httpListener = new;
    }

    documentation {
        Gets called when the endpoint is being initialize during package init time.

        P{{c}} The `SecureEndpointConfiguration` of the endpoint
    }
    public function init(SecureEndpointConfiguration c);

    documentation {
        Initializes the endpoint.
    }
    public function initEndpoint() returns (error);

    documentation {
        Gets called every time a service attaches itself to this endpoint. Also happens at package initialization.

        P{{serviceType}} The type of the service to be registered
    }
    public function register(typedesc serviceType);

    documentation {
        Starts the registered service.
    }
    public function start();

    documentation {
        Returns the connector that client code uses.

        R{{}} The connector that client code uses
    }
    public function getCallerActions() returns (Connection);

    documentation {
        Stops the registered service.
    }
    public function stop();
};

documentation {
    Configuration for secure HTTP service endpoint.

    F{{host}} Host of the endpoint
    F{{port}} Port of the endpoint
    F{{keepAlive}} The keepAlive behaviour of the endpoint
    F{{secureSocket}} The SSL configurations for the `endpoint`
    F{{httpVersion}} Highest HTTP version supported
    F{{requestLimits}} Request validation limits configuration
    F{{filters}} Filters to be applied to the request before being dispatched to the actual `resource`
    F{{timeoutMillis}} Period of time in milliseconds that a connection waits for a read/write operation. Use value 0
                       to disable timeout
    F{{authProviders}} The array of authentication providers which are used to authenticate the users
}
public type SecureEndpointConfiguration record {
    string host,
    int port = 9090,
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    ServiceSecureSocket? secureSocket,
    string httpVersion = "1.1",
    RequestLimits? requestLimits,
    Filter[] filters,
    int timeoutMillis = DEFAULT_LISTENER_TIMEOUT,
    AuthProvider[]? authProviders,
};

documentation {
    Configuration for authentication providers.

    F{{scheme}} Authentication scheme
    F{{id}} Authentication provider instance id
    F{{authStoreProvider}} Authentication store provider (file, LDAP, etc.) implementation
    F{{issuer}} Identifier of the token issuer
    F{{audience}} Identifier of the token recipients
    F{{trustStore}} Trustore configurations
    F{{certificateAlias}} Token signed key alias
    F{{clockSkew}} Time in seconds to mitigate clock skew
    F{{keyStore}} `KeyStore` instance providing key store related configurations
    F{{keyAlias}} The Key Alias
    F{{keyPassword}} The Key password
    F{{expTime}} Expiry time
    F{{signingAlg}} The signing algorithm which is used to sign the JWT token
    F{{propagateJwt}} `true` if propagating authentication info as JWT
}
public type AuthProvider record {
    string scheme,
    string id,
    string authStoreProvider,
    string issuer,
    string audience,
    TrustStore? trustStore,
    string certificateAlias,
    int clockSkew,
    KeyStore? keyStore,
    string keyAlias,
    string keyPassword,
    int expTime,
    string signingAlg,
    boolean propagateJwt,
};

function SecureListener::init(SecureEndpointConfiguration c) {
    addAuthFiltersForSecureListener(c);
    self.httpListener.init(c);
}

documentation {
    Add authn and authz filters

    P{{config}} `SecureEndpointConfiguration` instance
}
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

documentation {
    Create an array of auth and authz filters.

    P{{config}} `SecureEndpointConfiguration` instance
    R{{}} Array of Filters comprising of authn and authz Filters
}
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
            if (authProvider.propagateJwt) {
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

documentation {
    The caller actions for responding to client requests to secure listener.
}
public type SecureListenerActions object {

    public Connection httpCallerActions;

    documentation {
        The secure listener caller actions initializer.

        P{{httpCallerActions}} HTTP caller actions reference
    }
    new (httpCallerActions) {}

    documentation {
        Sends the outbound response to the caller.

        P{{message}} The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} Returns an `error` if failed to respond
    }
    public function respond(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns error? {
        return httpCallerActions.respond(message);
    }

    documentation {
        Pushes a promise to the caller.

        P{{promise}} Push promise message
        R{{}} An `error` in case of failures
    }
    public function promise(PushPromise promise) returns error? {
        return httpCallerActions.promise(promise);
    }

    documentation {
        Sends a promised push response to the caller.

        P{{promise}} Push promise message
        P{{response}} The outbound response
        R{{}} An `error` in case of failures while responding with the promised response
    }
    public function pushPromisedResponse(PushPromise promise, Response response) returns error? {
        return httpCallerActions.pushPromisedResponse(promise, response);
    }

    documentation {
        Sends an upgrade request with custom headers.

        P{{headers}} A `map` of custom headers for handshake
    }
    public function acceptWebSocketUpgrade(map headers) returns WebSocketListener {
        return httpCallerActions.acceptWebSocketUpgrade(headers);
    }

    documentation {
        Cancels the handshake.

        P{{status}} Error Status code for cancelling the upgrade and closing the connection.
        This error status code need to be 4xx or 5xx else the default status code would be 400.
        P{{reason}} Reason for cancelling the upgrade
        R{{}} An `error` if an error occurs during cancelling the upgrade or nil
    }
    public function cancelWebSocketUpgrade(int status, string reason) returns error|() {
        return httpCallerActions.cancelWebSocketUpgrade(status, reason);
    }

    documentation {
        Sends a `100-continue` response to the caller.

        R{{}} Returns an `error` if failed to send the `100-continue` response
    }
    public function continue() returns error? {
        return httpCallerActions.continue();
    }

    documentation {
        Sends a redirect response to the user with the specified redirection status code.

        P{{response}} Response to be sent to the caller
        P{{code}} The redirect status code to be sent
        P{{locations}} An array of URLs to which the caller can redirect to
        R{{}} Returns an `error` if failed to send the redirect response
    }
    public function redirect(Response response, RedirectCode code, string[] locations) returns error? {
        return httpCallerActions.redirect(response, code, locations);
    }
};
