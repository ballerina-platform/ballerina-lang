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
# + host - Domain name of the service
# + basePath - Service base path
# + compression - The status of compression
# + chunking - Configures the chunking behaviour for the service
# + cors - The cross origin resource sharing configurations for the service
# + versioning - The version of the service to be used
# + auth - Authentication configurations for secure the service
public type HttpServiceConfig record {|
    string host = "b7a.default";
    string basePath = "";
    CompressionConfig compression = {};
    Chunking chunking = CHUNKING_AUTO;
    CorsConfig cors = {};
    Versioning versioning = {};
    ServiceAuth auth?;
|};

# Configurations for CORS support.
#
# + allowHeaders - The array of allowed headers by the service
# + allowMethods - The array of allowed methods by the service
# + allowOrigins - The array of origins with which the response is shared by the service
# + exposeHeaders - The whitelisted headers which clients are allowed to access
# + allowCredentials - Specifies whether credentials are required to access the service
# + maxAge - The maximum duration to cache the preflight from client side
public type CorsConfig record {|
    string[] allowHeaders = [];
    string[] allowMethods = [];
    string[] allowOrigins = [];
    string[] exposeHeaders = [];
    boolean allowCredentials = false;
    int maxAge= -1;
|};


# Configurations for service versioning.
#
# + pattern - Expected version pattern in the request URL
# + allowNoVersion - Allow requests with missing version path segment in the URL to be dispatched
# + matchMajorVersion - Allow requests with only the major version specified in the URL to be dispatched
public type Versioning record {|
    string pattern = "v{major}.{minor}";
    boolean allowNoVersion = false;
    boolean matchMajorVersion = false;
|};

# Configurations for a WebSocket service.
#
# + path - Path of the WebSocket service
# + subProtocols - Negotiable sub protocol by the service
# + idleTimeoutInSeconds - Idle timeout for the client connection. Upon timeout, `onIdleTimeout` resource (if defined)
#                          in the server service will be triggered. Note that this overrides the `timeoutInMillis` config
#                          in the `http:Listener`.
# + maxFrameSize - The maximum payload size of a WebSocket frame in bytes.
#                  If this is not set or is negative or zero, the default frame size will be used.
public type WSServiceConfig record {|
    string path = "";
    string[] subProtocols = [];
    int idleTimeoutInSeconds = 0;
    int maxFrameSize = 0;
|};

// TODO: Enable this when Ballerina supports service life time
//public type HttpServiceLifeTime "REQUEST"|"CONNECTION"|"SESSION"|"SINGLETON";

# The annotation which is used to configure an HTTP service.
public annotation HttpServiceConfig ServiceConfig on service;

# The annotation which is used to configure a WebSocket service.
public annotation WSServiceConfig WebSocketServiceConfig on service;

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
# + auth - Authentication Configs to secure the resource
public type HttpResourceConfig record {|
    string[] methods = [];
    string path = "";
    string body = "";
    string[] consumes = [];
    string[] produces = [];
    CorsConfig cors = {};
    boolean transactionInfectable = true;
    WebSocketUpgradeConfig? webSocketUpgrade = ();
    ResourceAuth auth?;
|};

# Resource configuration to upgrade from HTTP to WebSocket.
#
# + upgradePath - Path which is used to upgrade from HTTP to WebSocket
# + upgradeService - Callback service for a successful upgrade
public type WebSocketUpgradeConfig record {|
    string upgradePath = "";
    service upgradeService?;
|};

# Configures the authentication scheme for a service.
#
# + enabled - Specifies whether authentication is enabled
# + authHandlers - An array of inbound authentication handlers or an array consisting of arrays of inbound authentication handlers.
# An array is used to indicate that at least one of the authentication handlers should be successfully authenticated. An array consisting of arrays
# is used to indicate that at least one authentication handler from the sub-arrays should be successfully authenticated.
# + scopes - An array of scopes or an array consisting of arrays of scopes. An array is used to indicate that at least one of the scopes should
# be successfully authorized. An array consisting of arrays is used to indicate that at least one scope from the sub-arrays 
# should be successfully authorized.
public type ServiceAuth record {|
    boolean enabled = true;
    InboundAuthHandler[]|InboundAuthHandler[][] authHandlers?;
    string[]|string[][] scopes?;
|};

# Configures the authentication scheme for a resource.
#
# + enabled - Specifies whether authentication is enabled
# + authHandlers - An array of inbound authentication handlers or an array consisting of arrays of inbound authentication handlers.
# An array is used to indicate that at least one of the authentication handlers should be successfully authenticated. An array consisting of arrays
# is used to indicate that at least one authentication handler from the sub-arrays should be successfully authenticated.
# + scopes - An array of scopes or an array consisting of arrays of scopes. An array is used to indicate that at least one of the scopes should
# be successfully authorized. An array consisting of arrays is used to indicate that at least one scope from the sub-arrays
# should be successfully authorized.
public type ResourceAuth record {|
    boolean enabled?;
    InboundAuthHandler[]|InboundAuthHandler[][] authHandlers?;
    string[]|string[][] scopes?;
|};

# The annotation which is used to configure an HTTP resource.
public annotation HttpResourceConfig ResourceConfig on resource function;

# Path param order config keep the signature path param index against the variable names for runtime path param processing.
#
# + pathParamOrder - Specifies index of signature path param against the param variable name
type HttpParamOrderConfig record {|
    map<int> pathParamOrder = {};
|};

# The annotation which is used to configure an path param order.
annotation HttpParamOrderConfig ParamOrderConfig on resource function;
