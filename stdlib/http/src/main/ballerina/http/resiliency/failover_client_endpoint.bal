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


# An HTTP client endpoint which provides failover support over multiple HTTP clients.
#
# + epName - Name of the endpoint
# + failoverClientConfig - The configurations for the failover client endpoint
public type FailoverClient object {

    public string epName;
    public FailoverClientEndpointConfiguration failoverClientConfig;

    private Client httpEP;

    # Initializes the endpoint using the configurations provided.
    #
    # + foClientConfig - The configurations to be used when initializing the endpoint
    public function init(FailoverClientEndpointConfiguration foClientConfig);

    # Returns the HTTP failover actions associated with the endpoint.
    #
    # + return - The HTTP failover actions associated with the endpoint
    public function getCallerActions() returns FailoverActions {
        return check <FailoverActions>httpEP.httpClient;
    }
};

# Provides a set of HTTP related configurations and failover related configurations.
#
# + circuitBreaker - Circuit Breaker behaviour configurations
# + timeoutMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + httpVersion - The HTTP version supported by the endpoint
# + forwarded - The choice of setting `forwarded`/`x-forwarded` header
# + keepAlive - Specifies whether to reuse a connection for multiple requests
# + chunking - The chunking behaviour of the request
# + followRedirects - Redirect related options
# + retryConfig - Retry related options
# + proxy - Proxy related options
# + connectionThrottling - The configurations for controlling the number of connections allowed concurrently
# + targets - The upstream HTTP endpoints among which the incoming HTTP traffic load should be sent on failover
# + cache - The configurations for controlling the caching behaviour
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + auth - HTTP authentication releated configurations
# + failoverCodes - Array of HTTP response status codes for which the failover behaviour should be triggered
# + intervalMillis - Failover delay interval in milliseconds
public type FailoverClientEndpointConfiguration record {
    CircuitBreakerConfig? circuitBreaker;
    int timeoutMillis = 60000;
    string httpVersion = "1.1";
    string forwarded = "disable";
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    Chunking chunking = "AUTO";
    FollowRedirects? followRedirects;
    RetryConfig? retryConfig;
    ProxyConfig? proxy;
    ConnectionThrottling? connectionThrottling;
    TargetService[] targets;
    CacheConfig cache = {};
    Compression compression = COMPRESSION_AUTO;
    AuthConfig? auth;
    int[] failoverCodes = [501, 502, 503, 504];
    int intervalMillis;
    !...
};

function FailoverClient::init(FailoverClientEndpointConfiguration foClientConfig) {
    self.httpEP.httpClient = createFailOverClient(foClientConfig);
    self.httpEP.config.circuitBreaker = foClientConfig.circuitBreaker;
    self.httpEP.config.timeoutMillis = foClientConfig.timeoutMillis;
    self.httpEP.config.httpVersion = foClientConfig.httpVersion;
    self.httpEP.config.forwarded = foClientConfig.forwarded;
    self.httpEP.config.keepAlive = foClientConfig.keepAlive;
    self.httpEP.config.chunking = foClientConfig.chunking;
    self.httpEP.config.followRedirects = foClientConfig.followRedirects;
    self.httpEP.config.retryConfig = foClientConfig.retryConfig;
    self.httpEP.config.proxy = foClientConfig.proxy;
    self.httpEP.config.connectionThrottling = foClientConfig.connectionThrottling;
}

function createClientEPConfigFromFailoverEPConfig(FailoverClientEndpointConfiguration foConfig,
                                                  TargetService target) returns ClientEndpointConfig {
    ClientEndpointConfig clientEPConfig = {
        url:target.url,
        circuitBreaker:foConfig.circuitBreaker,
        timeoutMillis:foConfig.timeoutMillis,
        keepAlive:foConfig.keepAlive,
        chunking:foConfig.chunking,
        httpVersion:foConfig.httpVersion,
        forwarded:foConfig.forwarded,
        followRedirects:foConfig.followRedirects,
        retryConfig:foConfig.retryConfig,
        proxy:foConfig.proxy,
        connectionThrottling:foConfig.connectionThrottling,
        secureSocket:target.secureSocket,
        cache:foConfig.cache,
        compression:foConfig.compression,
        auth:foConfig.auth
    };
    return clientEPConfig;
}


function createFailOverClient(FailoverClientEndpointConfiguration failoverClientConfig) returns CallerActions {
    ClientEndpointConfig config = createClientEPConfigFromFailoverEPConfig(
                                      failoverClientConfig,
                                      failoverClientConfig.targets[0]);
    CallerActions[] clients = createFailoverHttpClientArray(failoverClientConfig);
    boolean[] failoverCodes = populateErrorCodeIndex(failoverClientConfig.failoverCodes);
    FailoverInferredConfig failoverInferredConfig = {
        failoverClientsArray:clients,
        failoverCodesIndex:failoverCodes,
        failoverInterval:failoverClientConfig.intervalMillis
    };
    return new FailoverActions(config.url, config, failoverInferredConfig);
}

function createFailoverHttpClientArray(FailoverClientEndpointConfiguration failoverClientConfig) returns CallerActions[] {
    CallerActions[] httpClients = [];
    int i = 0;
    boolean httpClientRequired = false;
    string uri = failoverClientConfig.targets[0].url;
    var cbConfig = failoverClientConfig.circuitBreaker;
    match cbConfig {
        CircuitBreakerConfig cb => {
            if (uri.hasSuffix("/")) {
                int lastIndex = uri.length() - 1;
                uri = uri.substring(0, lastIndex);
            }
            httpClientRequired = false;
        }
        () => {
            httpClientRequired = true;
        }
    }

    foreach target in failoverClientConfig.targets {
        ClientEndpointConfig epConfig = createClientEPConfigFromFailoverEPConfig(failoverClientConfig, target);
        uri = target.url;
        if (uri.hasSuffix("/")) {
            int lastIndex = uri.length() - 1;
            uri = uri.substring(0, lastIndex);
        }
        if (!httpClientRequired) {
            httpClients[i] = createCircuitBreakerClient(uri, epConfig);
        } else {
            var retryConfigVal = epConfig.retryConfig;
            match retryConfigVal {
                RetryConfig retryConfig => {
                    httpClients[i] = createRetryClient(uri, epConfig);
                }
                () => {
                    if (epConfig.cache.enabled) {
                        httpClients[i] = createHttpCachingClient(uri, epConfig, epConfig.cache);
                    } else {
                        httpClients[i] = createHttpSecureClient(uri, epConfig);
                    }
                }
            }
        }
        httpClients[i].config = epConfig;
        i = i + 1;
    }
    return httpClients;
}
