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

# Contains the configurations for an HTTP service.
#
# + endpoints - An array of endpoints the service would be attached to
# + host - Domain name of the service
# + basePath - Service base path
# + compression - The status of compression
# + chunking - Configures the chunking behaviour for the service
# + cors - The cross origin resource sharing configurations for the service
# + versioning - The version of the service to be used
# + authConfig - Authentication configurations for securing the service
public type HttpServiceConfig record {
    Listener[] endpoints;
    string host = "b7a.default";
    string basePath;
    CompressionConfig compression;
    Chunking chunking = CHUNKING_AUTO;
    CorsConfig cors;
    Versioning versioning;
    ListenerAuthConfig? authConfig;
    !...
};

# Configurations for CORS support.
#
# + allowHeaders - The array of allowed headers by the service
# + allowMethods - The array of allowed methods by the service
# + allowOrigins - The array of origins with which the response is shared by the service
# + exposeHeaders - The whitelisted headers which clients are allowed to access
# + allowCredentials - Specifies whether credentials are required to access the service
# + maxAge - The maximum duration to cache the preflight from client side
public type CorsConfig record {
    string[] allowHeaders;
    string[] allowMethods;
    string[] allowOrigins;
    string[] exposeHeaders;
    boolean allowCredentials;
    int maxAge= -1;
    !...
};


# Configurations for service versioning.
#
# + pattern - Expected version pattern in the request URL
# + allowNoVersion - Allow requests with missing version path segment in the URL to be dispatched
# + matchMajorVersion - Allow requests with only the major version specified in the URL to be dispatched
public type Versioning record {
    string pattern = "v{major}.{minor}";
    boolean allowNoVersion = false;
    boolean matchMajorVersion = false;
    !...
};

# Configurations for a WebSocket service.
#
# + endpoints - An array of endpoints the service would be attached to
# + webSocketEndpoints - An array of endpoints the service would be attached to
# + path - Path of the WebSocket service
# + subProtocols - Negotiable sub protocol by the service
# + idleTimeoutInSeconds - Idle timeout for the client connection. This can be triggered by putting
#                          an `onIdleTimeout` resource in the WebSocket service.
# + maxFrameSize - The maximum payload size of a WebSocket frame in bytes
public type WSServiceConfig record {
    Listener[] endpoints;
    WebSocketListener[] webSocketEndpoints;
    string path;
    string[] subProtocols;
    int idleTimeoutInSeconds;
    int maxFrameSize;
    !...
};

// TODO: Enable this when Ballerina supports service life time
//public type HttpServiceLifeTime "REQUEST"|"CONNECTION"|"SESSION"|"SINGLETON";

# The annotation which is used to configure an HTTP service.
public annotation <service> ServiceConfig HttpServiceConfig;

# The annotation which is used to configure a WebSocket service.
public annotation <service> WebSocketServiceConfig WSServiceConfig;

////////////////////////////
/// Resource Annotations ///
////////////////////////////
# Configuration for an HTTP resource.
#
# + methods - The array of allowed HTTP methods
# + path - The path of resource
# + body - Inbound request entity body name which declared in signature
# + consumes - The media types which are accepted by resource
# + produces - The media types which are produced by resource
# + cors - The cross origin resource sharing configurations for the resource. If not set, the resource will inherit the CORS behaviour of the enclosing service.
# + transactionInfectable - Allow to participate in the distributed transactions if value is true
# + webSocketUpgrade - Annotation to define HTTP to WebSocket upgrade
# + authConfig - Authentication Configs to secure the resource
public type HttpResourceConfig record {
    string[] methods;
    string path;
    string body;
    string[] consumes;
    string[] produces;
    CorsConfig cors;
    boolean transactionInfectable = true;
    WebSocketUpgradeConfig? webSocketUpgrade;
    ListenerAuthConfig? authConfig;
    !...
};

# Configures the HTTP to WebSocket upgrade.
#
# + upgradePath - Path which is used to upgrade from HTTP to WebSocket
# + upgradeService - WebSocket service which should be used after a successful upgrade
public type WebSocketUpgradeConfig record {
    string upgradePath;
    typedesc upgradeService;
    !...
};

# Configures the authentication scheme for a service or a resource.
#
# + authentication - Enables/disables authentication
# + authProviders - Array of authentication provider IDs
# + scopes - Array of scopes
public type ListenerAuthConfig record {
    Authentication? authentication;
    string[]? authProviders;
    string[]? scopes;
    !...
};

# Can be used for enabling/disabling authentication in an HTTP service.
#
# + enabled - Specifies whether authentication is enabled
public type Authentication record {
    boolean enabled;
    !...
};

# The annotation which is used to configure an HTTP resource.
public annotation <resource> ResourceConfig HttpResourceConfig;
