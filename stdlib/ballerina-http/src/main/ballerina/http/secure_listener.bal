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

package ballerina.http;

import ballerina/auth;

@Description {value:"Representation of an Secure Listener"}
@Field {value:"config: SecureEndpointConfiguration instance"}
@Field {value:"httpListener: HTTP Listener instance"}
public type SecureListener object {
    public {
        SecureEndpointConfiguration config;
        Listener httpListener;
    }

    new () {
        httpListener = new;
    }

    public function init(SecureEndpointConfiguration config);

    @Description {value:"Gets called when the endpoint is being initialize during package init time"}
    @Return {value:"Error occured during initialization"}
    public function initEndpoint() returns (error);

    @Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
    @Param {value:"ep: The endpoint to which the service should be registered to"}
    @Param {value:"serviceType: The type of the service to be registered"}
    public function register(typedesc serviceType);

    @Description {value:"Starts the registered service"}
    public function start();

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public function getClient() returns (Connection);

    @Description {value:"Stops the registered service"}
    public function stop();
};

@Description {value:"Configuration for secure HTTP service endpoint"}
@Field {value:"host: Host of the service"}
@Field {value:"port: Port number of the service"}
@Field {value:"exposeHeaders: The array of allowed headers which are exposed to the client"}
@Field {value:"keepAlive: The keepAlive behaviour of the connection for a particular port"}
@Field {value:"transferEncoding: The types of encoding applied to the response"}
@Field {value:"chunking: The chunking behaviour of the response"}
@Field {value:"secureSocket: The SSL configurations for the service endpoint"}
@Field {value:"httpVersion: Highest HTTP version supported"}
@Field {value:"requestLimits: Request validation limits configuration"}
@Field {value:"filters: Filters to be applied to the request before dispatched to the actual resource"}
@Field {value:"authProviders: The array of AuthProviders which are used to authenticate the users"}
public type SecureEndpointConfiguration {
    string host,
    int port = 9090,
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    TransferEncoding transferEncoding = TRANSFERENCODE_CHUNKING,
    ServiceSecureSocket? secureSocket,
    string httpVersion = "1.1",
    RequestLimits? requestLimits,
    Filter[] filters,
    AuthProvider[]? authProviders,
};

@Description {value:"Configuration for authentication providers"}
@Field {value:"scheme: Authentication schem"}
@Field {value:"id: Authention provider instance id"}
public type AuthProvider {
    string scheme,
    string id,
    string authProvider,
    string filePath,
    string issuer,
    string audience,
    TrustStore? trustStore,
    string certificateAlias,
    int timeSkew,
};

public function SecureListener::init (SecureEndpointConfiguration config) {
    addAuthFiltersForSecureListener(config);
    self.httpListener.init(config);
}

@Description {value:"Add authn and authz filters"}
@Param {value:"config: SecureEndpointConfiguration instance"}
function addAuthFiltersForSecureListener (SecureEndpointConfiguration config) {
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

@Description {value:"Create an array of auth and authz filters"}
@Param {value:"config: SecureEndpointConfiguration instance"}
@Return {value:"Array of Filters comprising of authn and authz Filters"}
function createAuthFiltersForSecureListener (SecureEndpointConfiguration config) returns (Filter[]) {
    // parse and create authentication handlers
    //AuthHandlerRegistry registry = new;
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
    //AuthnHandlerChain authnHandlerChain = new(registry);
    AuthnFilter authnFilter = new(authnRequestFilterFunc, responseFilterFunc);
    AuthzFilter authzFilter = new(authzRequestFilterFunc, responseFilterFunc);
    authFilters[0] = <Filter>authnFilter;
    authFilters[1] = authzFilter;
    return authFilters;
}

function createBasicAuthHandler () returns HttpAuthnHandler  {
    auth:ConfigAuthProvider configAuthProvider = new;
    auth:AuthProvider authProvider1 = <auth:AuthProvider> configAuthProvider;
    HttpBasicAuthnHandler basicAuthHandler = new(authProvider1);
    return check <HttpAuthnHandler> basicAuthHandler;
}

function createAuthHandler (AuthProvider authProvider) returns HttpAuthnHandler {
    if (authProvider.scheme == AUTHN_SCHEME_BASIC) {
        auth:AuthProvider authProvider1;
        if (authProvider.authProvider == AUTH_PROVIDER_CONFIG) {
            auth:ConfigAuthProvider configAuthProvider = new;
            authProvider1 = <auth:AuthProvider> configAuthProvider;
        } else {
            // other auth providers are unsupported yet
            error e = {message:"Invalid auth provider: " + authProvider.authProvider };
            throw e;
        }
        HttpBasicAuthnHandler basicAuthHandler = new(authProvider1);
        return check <HttpAuthnHandler> basicAuthHandler;
    } else if(authProvider.scheme == AUTH_SCHEME_JWT){
        auth:JWTAuthProviderConfig jwtConfig = {};
        jwtConfig.issuer = authProvider.issuer;
        jwtConfig.audience = authProvider.audience;
        jwtConfig.certificateAlias = authProvider.certificateAlias;
        jwtConfig.trustStoreFilePath = authProvider.trustStore.filePath but {() => ""};
        jwtConfig.trustStorePassword = authProvider.trustStore.password but {() => ""};
        auth:JWTAuthProvider jwtAuthProvider = new (jwtConfig);
        HttpJwtAuthnHandler jwtAuthnHandler = new(jwtAuthProvider);
        return check <HttpAuthnHandler> jwtAuthnHandler;
    } else {
        // TODO: create other HttpAuthnHandlers
        error e = {message:"Invalid auth scheme: " + authProvider.scheme };
        throw e;
    }
}

@Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
@Param {value:"ep: The endpoint to which the service should be registered to"}
@Param {value:"serviceType: The type of the service to be registered"}
public function SecureListener::register (typedesc serviceType) {
    self.httpListener.register(serviceType);
}

@Description {value:"Starts the registered service"}
public function SecureListener::start () {
    self.httpListener.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function SecureListener::getClient () returns (Connection) {
    return self.httpListener.getClient();
}

@Description {value:"Stops the registered service"}
public function SecureListener::stop () {
    self.httpListener.stop();
}