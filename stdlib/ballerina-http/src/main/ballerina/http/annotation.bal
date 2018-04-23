// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


///////////////////////////
/// Service Annotations ///
///////////////////////////
@Description {value:"Configuration for an HTTP service"}
@Field {value:"endpoints: An array of endpoints the service would be attached to"}
@Field {value:"lifetime: The life time of the service"}
@Field {value:"basePath: Service base path"}
@Field {value:"compression: The status of compression {default value : AUTO}"}
@Field {value:"cors: The CORS configurations for the service"}
@Field {value:"authConfig: AuthConfig instance to secure the service"}
public type HttpServiceConfig {
    Listener[] endpoints,
    HttpServiceLifeTime lifetime,
    string basePath,
    Compression compression = "AUTO",
    Chunking chunking = CHUNKING_AUTO,
    CorsConfig cors,
    Versioning versioning,
    ListenerAuthConfig? authConfig,
};

@Description {value:"Configurations for CORS support"}
@Field {value:"allowHeaders: The array of allowed headers by the service"}
@Field {value:"allowMethods: The array of allowed methods by the service"}
@Field {value:"allowOrigins: The array of origins with which the response is shared by the service"}
@Field {value:"exposeHeaders: The whitelisted headers which clients are allowed to access"}
@Field {value:"allowCredentials: Specifies whether credentials are required to access the service"}
@Field {value:"maxAge: The maximum duration to cache the preflight from client side"}
public type CorsConfig {
    string[] allowHeaders,
    string[] allowMethods,
    string[] allowOrigins,
    string[] exposeHeaders,
    boolean allowCredentials,
    int maxAge= -1,
};


@Description {value:"Configurations for service versioning"}
@Field {value:"pattern: Expecting version pattern in the request url"}
@Field {value:"allowNoVersion: Allow to dispatch requests which does not hold version path segment in url"}
@Field {value:"matchMajorVersion: Allow to dispatch requests which specify only the major version in url"}
public type Versioning {
    string pattern = "v{major}.{minor}",
    boolean allowNoVersion = false,
    boolean matchMajorVersion = false,
};

@Description {value:"Configuration for a WebSocket service."}
@Field {value:"endpoints: An array of endpoints the service would be attached to"}
@Field {value:"basePath: Path of the WebSocket service"}
@Field {value:"subProtocols: Negotiable sub protocol by the service"}
@Field {value:"idleTimeoutInSeconds: Idle timeout for the client connection. This can be triggered by putting onIdleTimeout resource in WS service."}
@Field {value:"maxFrameSize: The maximum payload size of a WebSocket frame in bytes"}
public type WSServiceConfig {
    Listener[] endpoints,
    WebSocketListener[] webSocketEndpoints,
    string path,
    string[] subProtocols,
    int idleTimeoutInSeconds,
    int maxFrameSize,
};

//@Description {value:"This specifies the possible ways in which a service can be used when serving requests."}
//@Field {value:"REQUEST: Create a new instance of the service to process each request"}
//@Field {value:"CONNECTION: Create a new instance of the service for each connection"}
//@Field {value:"SESSION: Create a new instance of the service for each session"}
//@Field {value:"SINGLETON: Create a single instance of the service and use it to process all requests coming to an endpoint"}
//public enum HttpServiceLifeTime {
//    REQUEST,
//    CONNECTION,
//    SESSION,
//    SINGLETON
//}

public type HttpServiceLifeTime "REQUEST"|"CONNECTION"|"SESSION"|"SINGLETON";

@Description {value:"Configurations annotation for an HTTP service"}
public annotation <service> ServiceConfig HttpServiceConfig;

@Description {value:"Configurations annotation for a WebSocket service"}
public annotation <service> WebSocketServiceConfig WSServiceConfig;

////////////////////////////
/// Resource Annotations ///
////////////////////////////
@Description {value:"Configuration for an HTTP resource"}
@Field {value:"methods: The array of allowed HTTP methods"}
@Field {value:"path: The path of resource"}
@Field {value:"body: Inbound request entity body name which declared in signature"}
@Field {value:"consumes: The media types which are accepted by resource"}
@Field {value:"produces: The media types which are produced by resource"}
@Field {value:"cors: The CORS configurations for the resource. If not set, the resource will inherit the CORS behaviour of the enclosing service."}
@Field {value:"webSocket: Annotation to define HTTP to WebSocket upgrade"}
@Field {value:"authConfig: AuthConfig instance to secure the resource"}
public type HttpResourceConfig {
        string[] methods,
        string path,
        string body,
        string[] consumes,
        string[] produces,
        CorsConfig cors,
        boolean transactionInfectable = true,
        WebSocketUpgradeConfig? webSocketUpgrade,
        ListenerAuthConfig? authConfig,
};

public type WebSocketUpgradeConfig {
        string upgradePath,
        typedesc upgradeService,
};

@Description {value:"Representation of AuthConfig"}
@Field {value:"authentication: Authentication instance"}
@Field {value:"providers: array of providers"}
@Field {value:"scopes: array of scopes"}
public type ListenerAuthConfig {
    Authentication? authentication,
    string[]? authProviders,
    string[]? scopes,
};

@Description {value:"Representation of Authentication Config"}
@Field {value:"enabled: flag to enable/disable authentication"}
public type Authentication {
    boolean enabled,
};

@Description {value:"Configurations annotation for an HTTP resource"}
public annotation <resource> ResourceConfig HttpResourceConfig;
