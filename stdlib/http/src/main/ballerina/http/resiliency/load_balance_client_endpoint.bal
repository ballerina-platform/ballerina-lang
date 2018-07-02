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


documentation {
    LoadBalanceClient endpoint provides load balancing functionality over multiple HTTP clients.

    E{{}}
    F{{epName}} Name of the endpoint
    F{{loadBalanceClientConfig}} The configurations for the load balance client endpoint
}
public type LoadBalanceClient object {

    public string epName;
    public LoadBalanceClientEndpointConfiguration loadBalanceClientConfig;

    private Client httpEP;

    documentation {
        The initialization function for the load balance client endpoint.

        P{{lbClientConfig}} The user provided configurations for the load balance client endpoint
    }
    public function init(LoadBalanceClientEndpointConfiguration lbClientConfig);

    documentation {
        Returns the HTTP LoadBalancer actions associated with the endpoint.

        R{{}} The HTTP LoadBalancer actions associated with the endpoint
    }
    public function getCallerActions() returns LoadBalancerActions {
        return check <LoadBalancerActions> httpEP.httpClient;
    }
};

documentation {
    The configurations related to the load balance client endpoint.

    F{{circuitBreaker}} Circuit Breaker configuration
    F{{timeoutMillis}} The maximum time to wait (in milli seconds) for a response before closing the connection
    F{{httpVersion}} The HTTP version to be used to communicate with the endpoint
    F{{forwarded}} The choice of setting forwarded/x-forwarded header
    F{{keepAlive}} Specifies whether to keep the connection alive (or not) for multiple request/response pairs
    F{{chunking}} The chunking behaviour of the request
    F{{followRedirects}} Redirect related options
    F{{retryConfig}} Retry related options
    F{{proxy}} Proxy related options
    F{{connectionThrottling}} The configurations for controlling the number of connections allowed concurrently
    F{{targets}} The upstream HTTP endpoints among which the incoming HTTP traffic load should be distributed
    F{{cache}} The configurations for controlling the caching behaviour
    F{{compression}} Specifies the way of handling compression (`accept-encoding`) header
    F{{auth}} HTTP authentication releated configurations
    F{{algorithm}} The algorithm to be used for load balancing. The HTTP package provides 'roundRobin()' by default
    F{{failover}} Configuration for load balancer whether to fail over in case of a failure
}
public type LoadBalanceClientEndpointConfiguration record {
    CircuitBreakerConfig? circuitBreaker,
    int timeoutMillis = 60000,
    string httpVersion = "1.1",
    string forwarded = "disable",
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    Chunking chunking = "AUTO",
    FollowRedirects? followRedirects,
    RetryConfig? retryConfig,
    ProxyConfig? proxy,
    ConnectionThrottling? connectionThrottling,
    TargetService[] targets,
    CacheConfig cache = {},
    Compression compression = COMPRESSION_AUTO,
    AuthConfig? auth,
    string algorithm = ROUND_ROBIN,
    boolean failover = true;
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
