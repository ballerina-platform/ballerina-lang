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


# LoadBalanceClient endpoint provides load balancing functionality over multiple HTTP clients.
#
# + epName - Name of the endpoint
# + loadBalanceClientConfig - The configurations for the load balance client endpoint
public type LoadBalanceClient object {

    public string epName;
    public LoadBalanceClientEndpointConfiguration loadBalanceClientConfig;

    private Client httpEP;

    # The initialization function for the load balance client endpoint.
    #
    # + lbClientConfig - The user provided configurations for the load balance client endpoint
    public function init(LoadBalanceClientEndpointConfiguration lbClientConfig);

    # Returns the HTTP LoadBalancer actions associated with the endpoint.
    #
    # + return - The HTTP LoadBalancer actions associated with the endpoint
    public function getCallerActions() returns LoadBalancerActions {
        return check <LoadBalancerActions> httpEP.httpClient;
    }
};

# The configurations related to the load balance client endpoint.
#
# + circuitBreaker - Circuit Breaker configuration
# + timeoutMillis - The maximum time to wait (in milli seconds) for a response before closing the connection
# + httpVersion - The HTTP version to be used to communicate with the endpoint
# + forwarded - The choice of setting forwarded/x-forwarded header
# + keepAlive - Specifies whether to keep the connection alive (or not) for multiple request/response pairs
# + chunking - The chunking behaviour of the request
# + followRedirects - Redirect related options
# + retryConfig - Retry related options
# + proxy - Proxy related options
# + connectionThrottling - The configurations for controlling the number of connections allowed concurrently
# + targets - The upstream HTTP endpoints among which the incoming HTTP traffic load should be distributed
# + cache - The configurations for controlling the caching behaviour
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + auth - HTTP authentication releated configurations
# + algorithm - The algorithm to be used for load balancing. The HTTP package provides 'roundRobin()' by default
# + failover - Configuration for load balancer whether to fail over in case of a failure
public type LoadBalanceClientEndpointConfiguration record {
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
    string algorithm = ROUND_ROBIN;
    boolean failover = true;
    !...
};

function LoadBalanceClient::init(LoadBalanceClientEndpointConfiguration lbClientConfig) {
    self.httpEP.httpClient = createLoadBalancerClient(lbClientConfig);
    self.httpEP.config.circuitBreaker = lbClientConfig.circuitBreaker;
    self.httpEP.config.timeoutMillis = lbClientConfig.timeoutMillis;
    self.httpEP.config.httpVersion = lbClientConfig.httpVersion;
    self.httpEP.config.forwarded = lbClientConfig.forwarded;
    self.httpEP.config.keepAlive = lbClientConfig.keepAlive;
    self.httpEP.config.chunking = lbClientConfig.chunking;
    self.httpEP.config.followRedirects = lbClientConfig.followRedirects;
    self.httpEP.config.retryConfig = lbClientConfig.retryConfig;
    self.httpEP.config.proxy = lbClientConfig.proxy;
    self.httpEP.config.connectionThrottling = lbClientConfig.connectionThrottling;
}

function createClientEPConfigFromLoalBalanceEPConfig(LoadBalanceClientEndpointConfiguration lbConfig,
                                                     TargetService target) returns ClientEndpointConfig {
    ClientEndpointConfig clientEPConfig = {
        url:target.url,
        circuitBreaker:lbConfig.circuitBreaker,
        timeoutMillis:lbConfig.timeoutMillis,
        keepAlive:lbConfig.keepAlive,
        chunking:lbConfig.chunking,
        httpVersion:lbConfig.httpVersion,
        forwarded:lbConfig.forwarded,
        followRedirects:lbConfig.followRedirects,
        retryConfig:lbConfig.retryConfig,
        proxy:lbConfig.proxy,
        connectionThrottling:lbConfig.connectionThrottling,
        secureSocket:target.secureSocket,
        cache:lbConfig.cache,
        compression:lbConfig.compression,
        auth:lbConfig.auth
    };
    return clientEPConfig;
}

function createLoadBalancerClient(LoadBalanceClientEndpointConfiguration loadBalanceClientConfig)
                                                                                    returns CallerActions {
    ClientEndpointConfig config = createClientEPConfigFromLoalBalanceEPConfig(loadBalanceClientConfig,
                                                                            loadBalanceClientConfig.targets[0]);
    CallerActions[] lbClients = createLoadBalanceHttpClientArray(loadBalanceClientConfig);
    return new LoadBalancerActions(loadBalanceClientConfig.targets[0].url, config, lbClients,
                                            loadBalanceClientConfig.algorithm, 0, loadBalanceClientConfig.failover);
}

function createLoadBalanceHttpClientArray(LoadBalanceClientEndpointConfiguration loadBalanceClientConfig)
                                                                                    returns CallerActions[] {
    CallerActions[] httpClients = [];
    int i = 0;
    boolean httpClientRequired = false;
    string uri = loadBalanceClientConfig.targets[0].url;
    var cbConfig = loadBalanceClientConfig.circuitBreaker;
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

    foreach target in loadBalanceClientConfig.targets {
        ClientEndpointConfig epConfig = createClientEPConfigFromLoalBalanceEPConfig(loadBalanceClientConfig, target);
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
