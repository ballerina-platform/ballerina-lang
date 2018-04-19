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
    SimpleClient endpoint provides an HTTP endpoint without the resiliency aspects of the the Client endpoint

    F{{epName}} - Name of the endpoint
    F{{simpleConfig}} - The configurations for the endpoint to connect to. This contains all the configurations as the
                        ClientEndpointConfig, except for the resiliency related configurations.
}
public type SimpleClient object {
    public {
        string epName;
        SimpleClientEndpointConfiguration simpleConfig;
    }
    private {
        Client httpEP;
    }

    public function init(SimpleClientEndpointConfiguration simpleConfig);

    documentation {
        The register() function is not implemented for the SimpleClientEndpoint.
    }
    public function register(typedesc serviceType) {

    }

    documentation {
        The start() function is not implemented for the SimpleClientEndpoint.
    }
    public function start() {

    }

    documentation {
        Returns the backing HTTP client used by the endpoint.
    }
    public function getClient() returns HttpClient {
        return httpEP.httpClient;
    }

    documentation {
        The stop() function is not implemented for the SimpleClientEndpoint.
    }
    public function stop() {
    }
};

documentation {
    The configurations possible with the SimpleClient endpoint. This endpoint excludes the resiliency related configurations.

    F{{url}} - The URL of the HTTP endpoint to connect to
    F{{circuitBreaker}} - Circuit Breaker configuration
    F{{secureSocket}} - The SSL configurations for the endpoint
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
}
public type SimpleClientEndpointConfiguration {
    string url,
    CircuitBreakerConfig? circuitBreaker,
    SecureSocket? secureSocket,
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
    CacheConfig cache = {},
};

documentation {
    The initialization function for the SimpleClientEndpoint.

    P{{simpleConfig}} - The user provided configurations for the endpoint
}
public function SimpleClient::init(SimpleClientEndpointConfiguration simpleConfig) {
    string url = simpleConfig.url;
    if (url.hasSuffix("/")) {
        int lastIndex = url.length() - 1;
        url = url.subString(0, lastIndex);
    }
    self.httpEP = new;
    self.httpEP.config = {};
    self.httpEP.config.targets = [];

    self.httpEP.config.circuitBreaker = simpleConfig.circuitBreaker;
    self.httpEP.config.targets[0] = {url: simpleConfig.url, secureSocket: simpleConfig.secureSocket};
    self.httpEP.config.timeoutMillis = simpleConfig.timeoutMillis;
    self.httpEP.config.httpVersion = simpleConfig.httpVersion;
    self.httpEP.config.forwarded = simpleConfig.forwarded;
    self.httpEP.config.keepAlive = simpleConfig.keepAlive;
    self.httpEP.config.transferEncoding = simpleConfig.transferEncoding;
    self.httpEP.config.chunking = simpleConfig.chunking;
    self.httpEP.config.followRedirects = simpleConfig.followRedirects;
    self.httpEP.config.retryConfig = simpleConfig.retryConfig;
    self.httpEP.config.proxy = simpleConfig.proxy;
    self.httpEP.config.connectionThrottling = simpleConfig.connectionThrottling;

    var cbConfig = simpleConfig.circuitBreaker;
    match cbConfig  {
        CircuitBreakerConfig cb => {
            self.httpEP.httpClient = createCircuitBreakerClient(url, self.httpEP.config);
        }
        () => {
            if (simpleConfig.cache.enabled) {
                self.httpEP.config.cache = simpleConfig.cache;
                self.httpEP.httpClient = createHttpCachingClient(url, self.httpEP.config, self.httpEP.config.cache);
            } else {
                self.httpEP.httpClient = createHttpSecureClient(url, self.httpEP.config);
            }
        }
    }
}
