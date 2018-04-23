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
    FailoverClient endpoint provides failover support over multiple HTTP clients.

    F{{epName}} - Name of the endpoint
    F{{failoverClientConfig}} - The configurations for the failover client endpoint
}
public type FailoverClient object {
    public {
        string epName;
        FailoverClientEndpointConfiguration failoverClientConfig;
    }
    private {
        Client httpEP;
    }

    public function init(FailoverClientEndpointConfiguration failoverClientConfig);

    documentation {
        The register() function is not implemented for the failover client endpoint.
    }
    public function register(typedesc serviceType) {

    }

    documentation {
        The start() function is not implemented for the failover client endpoint.
    }
    public function start() {

    }

    documentation {
        Returns the backing HTTP client used by the endpoint.
    }
    public function getCallerActions() returns CallerActions {
        return httpEP.httpClient;
    }

    documentation {
        The stop() function is not implemented for the failover client endpoint.
    }
    public function stop() {
    }
};

documentation {
    The configurations related to the failover client endpoint.

    F{{circuitBreaker}} - Circuit Breaker configuration
    F{{timeoutMillis}} - The maximum time to wait (in milli seconds) for a response before closing the connection
    F{{httpVersion}} - The HTTP version to be used to communicate with the endpoint
    F{{forwarded}} - The choice of setting forwarded/x-forwarded header
    F{{keepAlive}} - Specifies whether to keep the connection alive (or not) for multiple request/response pairs
    F{{transferEncoding}} - The types of encoding applied to the request
    F{{chunking}} - The chunking behaviour of the request
    F{{followRedirects}} - Redirect related options
    F{{retryConfig}} - Retry related options
    F{{proxy}} - Proxy related options
    F{{connectionThrottling}} - The configurations for controlling the number of connections allowed concurrently
    F{{cache}} - The configurations for controlling the caching behaviour
    F{{acceptEncoding}} - Specifies the way of handling accept-encoding header
    F{{auth}} - HTTP authentication releated configurations
    F{{failoverCodes}} - Array of http response status codes which required failover the requests
    F{{intervalMillis}} - Failover delay interval in milliseconds
}
public type FailoverClientEndpointConfiguration {
    CircuitBreakerConfig? circuitBreaker,
    int timeoutMillis = 60000,
    string httpVersion = "1.1",
    string forwarded = "disable",
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    TransferEncoding transferEncoding = "CHUNKING",
    Chunking chunking = "AUTO",
    FollowRedirects? followRedirects,
    RetryConfig? retryConfig,
    ProxyConfig? proxy,
    ConnectionThrottling? connectionThrottling,
    TargetService[] targets,
    CacheConfig cache = {},
    AcceptEncoding acceptEncoding = ACCEPT_ENCODING_AUTO,
    AuthConfig? auth,
    int[] failoverCodes = [501, 502, 503, 504],
    int intervalMillis,
};

documentation {
    The initialization function for the failover client endpoint.

    P{{failoverClientConfig}} - The user provided configurations for the endpoint
}
public function FailoverClient::init(FailoverClientEndpointConfiguration failoverClientConfig) {
    self.httpEP.httpClient = createFailOverClient(failoverClientConfig);
    self.httpEP.config.circuitBreaker = failoverClientConfig.circuitBreaker;
    self.httpEP.config.timeoutMillis = failoverClientConfig.timeoutMillis;
    self.httpEP.config.httpVersion = failoverClientConfig.httpVersion;
    self.httpEP.config.forwarded = failoverClientConfig.forwarded;
    self.httpEP.config.keepAlive = failoverClientConfig.keepAlive;
    self.httpEP.config.transferEncoding = failoverClientConfig.transferEncoding;
    self.httpEP.config.chunking = failoverClientConfig.chunking;
    self.httpEP.config.followRedirects = failoverClientConfig.followRedirects;
    self.httpEP.config.retryConfig = failoverClientConfig.retryConfig;
    self.httpEP.config.proxy = failoverClientConfig.proxy;
    self.httpEP.config.connectionThrottling = failoverClientConfig.connectionThrottling;
}

function createClientEPConfigFromFailoverEPConfig(FailoverClientEndpointConfiguration foConfig,
                                                  TargetService target) returns ClientEndpointConfig {
    ClientEndpointConfig clientEPConfig = {
        url:target.url,
        circuitBreaker:foConfig.circuitBreaker,
        timeoutMillis:foConfig.timeoutMillis,
        keepAlive:foConfig.keepAlive,
        transferEncoding:foConfig.transferEncoding,
        chunking:foConfig.chunking,
        httpVersion:foConfig.httpVersion,
        forwarded:foConfig.forwarded,
        followRedirects:foConfig.followRedirects,
        retryConfig:foConfig.retryConfig,
        proxy:foConfig.proxy,
        connectionThrottling:foConfig.connectionThrottling,
        secureSocket:target.secureSocket,
        cache:foConfig.cache,
        acceptEncoding:foConfig.acceptEncoding,
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
    return new Failover(config.url, config, failoverInferredConfig);
}

function createFailoverHttpClientArray (FailoverClientEndpointConfiguration failoverClientConfig) returns CallerActions[] {
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
