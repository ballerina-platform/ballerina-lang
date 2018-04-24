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


import ballerina/io;

////////////////////////////////
///// HTTP Client Endpoint /////
////////////////////////////////

documentation {
    The HTTP client endpoint provides the capability for initiating contact with a remote HTTP service. The API it
    provides includes functions for the standard HTTP methods, forwarding a received request and sending requests
    using custom HTTP verbs.

    E{{}}
    F{{epName}} The name of the endpoint
    F{{config}} The configurations associated with the endpoint
    F{{httpClient}} The provider which implements the HTTP methods
}
public type Client object {
    public {
        string epName;
        ClientEndpointConfig config;
        CallerActions httpClient;
    }

    documentation {
        Gets invoked to initialize the endpoint. During initialization, configurations provided through the `config`
        record is used to determine which type of additional behaviours are added to the endpoint (e.g: caching,
        security, circuit breaking).

        P{{config}} The configurations to be used when initializing the endpoint
    }
    public function init(ClientEndpointConfig config);

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    documentation {
        Returns the HTTP actions associated with the endpoint.

        R{{}} The HTTP caller actions provider of the endpoint
    }
    public function getCallerActions() returns HttpClient {
        return self.httpClient;
    }

    documentation {
        Stops the registered service
    }
    public function stop() {
    }
};

documentation {
    Represents a single service and its related configurations.

    F{{url}} URL of the target service
    F{{secureSocket}} Configurations for secure communication with the remote HTTP endpoint
}
public type TargetService {
    string url,
    SecureSocket? secureSocket,
};

documentation {
    Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.

    F{{url}} URL of the target service
    F{{circuitBreaker}} Circuit Breaker behaviour configurations
    F{{timeoutMillis}} The maximum time to wait (in milliseconds) for a response before closing the connection
    F{{keepAlive}} Specifies whether to reuse a connection for multiple requests
    F{{transferEncoding}} The types of encoding applied to the request
    F{{chunking}} The chunking behaviour of the request
    F{{httpVersion}} The HTTP version understood by the client
    F{{forwarded}} The choice of setting `forwarded`/`x-forwarded` header
    F{{followRedirects}} Redirect related options
    F{{retryConfig}} Retry related options
    F{{proxy}} Proxy server related options
    F{{connectionThrottling}} Configurations for connection throttling
    F{{secureSocket}} SSL/TLS related options
    F{{cache}} HTTP caching related configurations
    F{{acceptEncoding}} Specifies the way of handling `accept-encoding` header
    F{{auth}} HTTP authentication releated configurations
}
public type ClientEndpointConfig {
    string url,
    CircuitBreakerConfig? circuitBreaker,
    int timeoutMillis = 60000,
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    TransferEncoding transferEncoding = "CHUNKING",
    Chunking chunking = "AUTO",
    string httpVersion = "1.1",
    string forwarded = "disable",
    FollowRedirects? followRedirects,
    RetryConfig? retryConfig,
    ProxyConfig? proxy,
    ConnectionThrottling? connectionThrottling,
    SecureSocket? secureSocket,
    CacheConfig cache,
    AcceptEncoding acceptEncoding = ACCEPT_ENCODING_AUTO,
    AuthConfig? auth,
};

public native function createHttpClient(string uri, ClientEndpointConfig config) returns CallerActions;

public native function createSimpleHttpClient(string uri, ClientEndpointConfig config) returns CallerActions;

documentation {
    Provides configurations for controlling the retry behaviour in failure scenarios.

    F{{count}} Number of retry attempts before giving up
    F{{interval}} Retry interval in milliseconds
    F{{backOffFactor}} Multiplier of the retry interval to exponentailly increase retry interval
    F{{maxWaitInterval}} Maximum time of the retry interval in milliseconds
}
public type RetryConfig {
    int count,
    int interval,
    float backOffFactor,
    int maxWaitInterval,
};

documentation {
    Provides configurations for facilitating secure communication with a remote HTTP endpoint.

    F{{trustStore}} TrustStore related options
    F{{keyStore}} KeyStore related options
    F{{protocols}} SSL/TLS protocol related options
    F{{certValidation}} Certificate validation against CRL or OCSP related options
    F{{ciphers}} List of ciphers to be used
                    eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
    F{{verifyHostname}} Enable/disable host name verification
    F{{shareSession}} Enable/disable new SSL session creation
    F{{ocspStapling}} Enable/disable OCSP stapling
}
public type SecureSocket {
    TrustStore? trustStore,
    KeyStore? keyStore,
    Protocols? protocol,
    ValidateCert? certValidation,
    string[] ciphers,
    boolean verifyHostname = true,
    boolean shareSession = true,
    boolean ocspStapling,
};

documentation {
    Provides configurations for controlling the endpoint's behaviour in response to HTTP redirect related responses.

    F{{enabled}} Enable/disable redirection
    F{{maxCount}} Maximun number of redirects to follow
}
public type FollowRedirects {
    boolean enabled = false,
    int maxCount = 5,
};

documentation {
    Proxy server configurations to be used with the HTTP client endpoint.

    F{{proxyHost}} Host name of the proxy server
    F{{proxyPort}} Proxy server port
    F{{proxyUserName}} Proxy server username
    F{{proxyPassword}} proxy server password
}
public type ProxyConfig {
    string host,
    int port,
    string userName,
    string password,
};

documentation {
    Provides configurations for throttling connections of the endpoint.

    F{{maxActiveConnections}} Maximum number of active connections allowed for the endpoint. The default value, -1,
                              indicates that the number of connections are not restricted.
    F{{waitTime}} Maximum waiting time for a request to grab an idle connection from the client
    F{{maxActiveStreamsPerConnection}} Maximum number of active streams allowed per an HTTP/2 connection
}
public type ConnectionThrottling {
    int maxActiveConnections = -1,
    int waitTime = 60000,
    int maxActiveStreamsPerConnection = -1,
};

documentation {
    AuthConfig record can be used to configure the authentication mechanism used by the HTTP endpoint.

    F{{scheme}} Scheme of the configuration (Basic, OAuth, JWT etc.)
    F{{username}} Username for Basic authentication
    F{{password}} Password for Basic authentication
    F{{accessToken}} Access token for OAuth2 authentication
    F{{refreshToken}} Refresh token for OAuth2 authentication
    F{{refreshToken}} Refresh token for OAuth2 authentication
    F{{refreshUrl}} Refresh token URL for OAuth2 authentication
    F{{consumerKey}} Consumer key for OAuth2 authentication
    F{{consumerKey}} Consumer key for OAuth2 authentication
    F{{consumerSecret}} Consumer secret for OAuth2 authentication
    F{{tokenUrl}} Token URL for OAuth2 authentication
    F{{clientId}} Clietnt ID for OAuth2 authentication
    F{{clientSecret}} Client secret for OAuth2 authentication
}
public type AuthConfig {
    string scheme,
    string username,
    string password,
    string accessToken,
    string refreshToken,
    string refreshUrl,
    string consumerKey,
    string consumerSecret,
    string tokenUrl,
    string clientId,
    string clientSecret,
};

public function Client::init(ClientEndpointConfig config) {
    boolean httpClientRequired = false;
    string url = config.url;
    if (url.hasSuffix("/")) {
        int lastIndex = url.length() - 1;
        url = url.substring(0, lastIndex);
    }
    self.config = config;
    var cbConfig = config.circuitBreaker;
    match cbConfig {
        CircuitBreakerConfig cb => {
            if (url.hasSuffix("/")) {
                int lastIndex = url.length() - 1;
                url = url.substring(0, lastIndex);
            }
            httpClientRequired = false;
        }
        () => {
            httpClientRequired = true;
        }
    }
    if (httpClientRequired) {
        var retryConfigVal = config.retryConfig;
        match retryConfigVal {
            RetryConfig retryConfig => {
                self.httpClient = createRetryClient(url, config);
            }
            () => {
                if (config.cache.enabled) {
                    self.httpClient = createHttpCachingClient(url, config, config.cache);
                } else {
                    self.httpClient = createHttpSecureClient(url, config);
                }
            }
        }
    } else {
        self.httpClient = createCircuitBreakerClient(url, config);
    }
}

function createCircuitBreakerClient (string uri, ClientEndpointConfig configuration) returns CallerActions {
    var cbConfig = configuration.circuitBreaker;
    match cbConfig {
        CircuitBreakerConfig cb => {
            validateCircuitBreakerConfiguration(cb);
            boolean [] statusCodes = populateErrorCodeIndex(cb.statusCodes);
            CallerActions cbHttpClient = new;
            var retryConfigVal = configuration.retryConfig;
            match retryConfigVal {
                RetryConfig retryConfig => {
                    cbHttpClient = createRetryClient(uri, configuration);
                }
                () => {
                    if (configuration.cache.enabled) {
                        cbHttpClient = createHttpCachingClient(uri, configuration, configuration.cache);
                    } else{
                        cbHttpClient = createHttpSecureClient(uri, configuration);
                    }
                }
            }

            time:Time circuitStartTime = time:currentTime();
            int numberOfBuckets = (cb.rollingWindow.timeWindowMillis/ cb.rollingWindow.bucketSizeMillis);
            Bucket[] bucketArray = [];
            int bucketIndex = 0;
            while (bucketIndex < numberOfBuckets) {
                bucketArray[bucketIndex] = {};
                bucketIndex = bucketIndex + 1;
            }

            CircuitBreakerInferredConfig circuitBreakerInferredConfig = {
                                                                failureThreshold:cb.failureThreshold,
                                                                resetTimeMillis:cb.resetTimeMillis,
                                                                statusCodes:statusCodes,
                                                                noOfBuckets:numberOfBuckets,
                                                                rollingWindow:cb.rollingWindow
                                                            };
            CircuitHealth circuitHealth = {startTime:circuitStartTime, totalBuckets: bucketArray};
            return new CircuitBreakerClient(uri, configuration, circuitBreakerInferredConfig, cbHttpClient, circuitHealth);
        }
        () => {
            //remove following once we can ignore
            if (configuration.cache.enabled) {
                return createHttpCachingClient(uri, configuration, configuration.cache);
            } else {
                return createHttpSecureClient(uri, configuration);
            }
        }
    }
}

function createRetryClient (string url, ClientEndpointConfig configuration) returns CallerActions {
    var retryConfigVal = configuration.retryConfig;
    match retryConfigVal {
        RetryConfig retryConfig => {
            if (configuration.cache.enabled) {
                return new RetryClient(url, configuration, retryConfig, createHttpCachingClient(url, configuration, configuration.cache));
            } else{
                return new RetryClient(url, configuration, retryConfig, createHttpSecureClient(url, configuration));
            }
        }
        () => {
            //remove following once we can ignore
            if (configuration.cache.enabled) {
                return createHttpCachingClient(url, configuration, configuration.cache);
            } else {
                return createHttpSecureClient(url, configuration);
            }
        }
    }
}
