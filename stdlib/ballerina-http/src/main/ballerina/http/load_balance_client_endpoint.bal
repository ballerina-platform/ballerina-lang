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

documentation {
    LoadBalanceClient endpoint provides load balancing functionality over multiple HTTP clients.

    F{{epName}} - Name of the endpoint
    F{{loadBalanceClientConfig}} - The configurations for the load balance client endpoint
}
public type LoadBalanceClient object {
    public {
        string epName;
        LoadBalanceClientEndpointConfiguration loadBalanceClientConfig;
    }
    private {
        Client httpEP;
    }

    public function init(LoadBalanceClientEndpointConfiguration loadBalanceClientConfig);

    documentation {
        The register() function is not implemented for the load balance client endpoint.
    }
    public function register(typedesc serviceType) {

    }

    documentation {
        The start() function is not implemented for the load balance client endpoint.
    }
    public function start() {

    }

    documentation {
        Returns the backing HTTP client used by the load balance client endpoint.
    }
    public function getClient() returns HttpClient {
        return httpEP.httpClient;
    }

    documentation {
        The stop() function is not implemented for the load balance client endpoint.
    }
    public function stop() {
    }
};

documentation {
    The configurations related to the load balance client endpoint.

    F{{circuitBreaker}} - Circuit Breaker configuration
    F{{timeoutMillis}} - The maximum time to wait (in milli seconds) for a response before closing the connection
    F{{httpVersion}} - The HTTP version to be used to communicate with the endpoint
    F{{forwarded}} - The choice of setting forwarded/x-forwarded header
    F{{keepAlive}} - Specifies whether to keep the connection alive (or not) for multiple request/response pairs
    F{{transferEncoding}} - The types of encoding applied to the request
    F{{chunking}} - The chunking behaviour of the request
    F{{followRedirects}} - Redirect related options
    F{{retry}} - Retry related options
    F{{proxy}} - Proxy related options
    F{{connectionThrottling}} - The configurations for controlling the number of connections allowed concurrently
    F{{cache}} - The configurations for controlling the caching behaviour
    F{{acceptEncoding}} - Specifies the way of handling accept-encoding header
    F{{auth}} - HTTP authentication releated configurations
    F{{algorithm}} - The algorithm to be used for load balancing. The HTTP package provides 'roundRobin()' by default
}
public type LoadBalanceClientEndpointConfiguration {
    CircuitBreakerConfig? circuitBreaker,
    int timeoutMillis = 60000,
    string httpVersion = "1.1",
    string forwarded = "disable",
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    TransferEncoding transferEncoding = "CHUNKING",
    Chunking chunking = "AUTO",
    FollowRedirects? followRedirects,
    Retry? retry,
    ProxyConfig? proxy,
    ConnectionThrottling? connectionThrottling,
    TargetService[] targets,
    CacheConfig cache = {},
    string acceptEncoding = "auto",
    AuthConfig? auth,
    string algorithm = ROUND_ROBIN,
};

documentation {
    The initialization function for the load balance client endpoint.

    P{{loadBalanceClientConfig}} - The user provided configurations for the load balance client endpoint
}
public function LoadBalanceClient::init(LoadBalanceClientEndpointConfiguration loadBalanceClientConfig) {
    self.httpEP.httpClient = createLoadBalancerClient(loadBalanceClientConfig);
    self.httpEP.config.circuitBreaker = loadBalanceClientConfig.circuitBreaker;
    self.httpEP.config.timeoutMillis = loadBalanceClientConfig.timeoutMillis;
    self.httpEP.config.httpVersion = loadBalanceClientConfig.httpVersion;
    self.httpEP.config.forwarded = loadBalanceClientConfig.forwarded;
    self.httpEP.config.keepAlive = loadBalanceClientConfig.keepAlive;
    self.httpEP.config.transferEncoding = loadBalanceClientConfig.transferEncoding;
    self.httpEP.config.chunking = loadBalanceClientConfig.chunking;
    self.httpEP.config.followRedirects = loadBalanceClientConfig.followRedirects;
    self.httpEP.config.retry = loadBalanceClientConfig.retry;
    self.httpEP.config.proxy = loadBalanceClientConfig.proxy;
    self.httpEP.config.connectionThrottling = loadBalanceClientConfig.connectionThrottling;
}

function createClientendpointConfigFromLoalBalanceEPConfig(LoadBalanceClientEndpointConfiguration lbConfig,
                                                           TargetService target) returns ClientEndpointConfig {
    ClientEndpointConfig clientEPConfig = {
        url:target.url,
        circuitBreaker:lbConfig.circuitBreaker,
        timeoutMillis:lbConfig.timeoutMillis,
        keepAlive:lbConfig.keepAlive,
        transferEncoding:lbConfig.transferEncoding,
        chunking:lbConfig.chunking,
        httpVersion:lbConfig.httpVersion,
        forwarded:lbConfig.forwarded,
        followRedirects:lbConfig.followRedirects,
        retry:lbConfig.retry,
        proxy:lbConfig.proxy,
        connectionThrottling:lbConfig.connectionThrottling,
        secureSocket:target.secureSocket,
        cache:lbConfig.cache,
        acceptEncoding:lbConfig.acceptEncoding,
        auth:lbConfig.auth
    };
    return clientEPConfig;
}

function createLoadBalancerClient(LoadBalanceClientEndpointConfiguration loadBalanceClientConfig) returns HttpClient {
    ClientEndpointConfig config = createClientendpointConfigFromLoalBalanceEPConfig(loadBalanceClientConfig,
                                                                            loadBalanceClientConfig.targets[0]);
    HttpClient[] lbClients = createLoadBalanceHttpClientArray(loadBalanceClientConfig);
    return new LoadBalancer(loadBalanceClientConfig.targets[0].url,
                                                config, lbClients, loadBalanceClientConfig.algorithm, 0);
}

function createLoadBalanceHttpClientArray (LoadBalanceClientEndpointConfiguration loadBalanceClientConfig)
                                                                                                returns HttpClient[] {
    HttpClient[] httpClients = [];
    int i = 0;
    boolean httpClientRequired = false;
    string uri = loadBalanceClientConfig.targets[0].url;
    var cbConfig = loadBalanceClientConfig.circuitBreaker;
    match cbConfig {
        CircuitBreakerConfig cb => {
            if (uri.hasSuffix("/")) {
                int lastIndex = uri.length() - 1;
                uri = uri.subString(0, lastIndex);
            }
            httpClientRequired = false;
        }
        () => {
            httpClientRequired = true;
        }
    }

    foreach target in loadBalanceClientConfig.targets {
        ClientEndpointConfig epConfig = createClientendpointConfigFromLoalBalanceEPConfig(loadBalanceClientConfig, target);
        uri = target.url;
        if (uri.hasSuffix("/")) {
            int lastIndex = uri.length() - 1;
            uri = uri.subString(0, lastIndex);
        }
        if (!httpClientRequired) {
            httpClients[i] = createCircuitBreakerClient(uri, epConfig);
        } else {
            var retryConfig = epConfig.retry;
            match retryConfig {
                Retry retry => {
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
