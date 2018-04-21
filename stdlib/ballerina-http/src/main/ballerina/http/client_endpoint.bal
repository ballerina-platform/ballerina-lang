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

@Description {value:"Represents an HTTP client endpoint"}
@Field {value:"epName: The name of the endpoint"}
@Field {value:"config: The configurations associated with the endpoint"}
public type Client object {
    public {
        string epName;
        ClientEndpointConfig config;
        CallerActions httpClient;
    }

    @Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
    @Param {value:"ep: The endpoint to be initialized"}
    @Param {value:"epName: The endpoint name"}
    @Param {value:"config: The ClientEndpointConfig of the endpoint"}
    public function init(ClientEndpointConfig config);

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    @Description { value:"Returns the connector that client code uses"}
    @Return { value:"The connector that client code uses" }
    public function getCallerActions() returns CallerActions {
        return self.httpClient;
    }

    @Description { value:"Stops the registered service"}
    @Return { value:"Error occured during registration" }
    public function stop() {
    }
};

public type Algorithm "NONE" | "LOAD_BALANCE" | "FAIL_OVER";

@Description {value:"Represents the configurations applied to a particular service."}
@Field {value:"url: Target service URI"}
@Field {value:"secureSocket: SSL/TLS related options"}
public type TargetService {
    string url,
    SecureSocket? secureSocket,
};

@Description { value:"ClientEndpointConfig struct represents options to be used for HTTP client invocation" }
@Field {value:"url: Target service URI"}
@Field {value:"circuitBreaker: Circuit Breaker configuration"}
@Field {value:"timeoutMillis: Endpoint timeout value in millisecond"}
@Field {value:"keepAlive: Specifies whether to reuse a connection for multiple requests"}
@Field {value:"transferEncoding: The types of encoding applied to the request"}
@Field {value:"chunking: The chunking behaviour of the request"}
@Field {value:"httpVersion: The HTTP version understood by the client"}
@Field {value:"forwarded: The choice of setting forwarded/x-forwarded header"}
@Field {value:"followRedirects: Redirect related options"}
@Field {value:"retryConfig: Retry related options"}
@Field {value:"proxy: Proxy server related options"}
@Field {value:"connectionThrottling: Configurations for connection throttling"}
@Field {value:"secureSocket: SSL/TLS related options"}
@Field {value:"cache: HTTP caching related configurations"}
@Field {value:"acceptEncoding: Specifies the way of handling accept-encoding header."}
@Field {value:"auth: HTTP authentication releated configurations."}
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

@Description { value:"RetryConfig struct represents retry related options for HTTP client invocation" }
@Field {value:"count: Number of retry attempts before giving up"}
@Field {value:"interval: Retry interval in milliseconds"}
@Field {value:"backOffFactor: Multiplier of the retry interval to exponentailly increase retry interval"}
@Field {value:"maxWaitInterval: Maximum time of the retry interval in milliseconds"}
public type RetryConfig {
    int count,
    int interval,
    float backOffFactor,
    int maxWaitInterval,
};

@Description { value:"SecureSocket struct represents SSL/TLS options to be used for HTTP client invocation" }
@Field {value: "trustStore: TrustStore related options"}
@Field {value: "keyStore: KeyStore related options"}
@Field {value: "protocols: SSL/TLS protocol related options"}
@Field {value: "certValidation: Certificate validation against CRL or OCSP related options"}
@Field {value:"ciphers: List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"}
@Field {value:"verifyHostname: Enable/disable host name verification"}
@Field {value:"shareSession: Enable/disable new ssl session creation"}
@Field {value:"ocspStapling: Enable/disable ocsp stapling"}
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

@Description { value:"FollowRedirects struct represents HTTP redirect related options to be used for HTTP client invocation" }
@Field {value:"enabled: Enable redirect"}
@Field {value:"maxCount: Maximun number of redirects to follow"}
public type FollowRedirects {
    boolean enabled = false,
    int maxCount = 5,
};

@Description { value:"ProxyConfig struct represents proxy server configurations to be used for HTTP client invocation" }
@Field {value:"proxyHost: host name of the proxy server"}
@Field {value:"proxyPort: proxy server port"}
@Field {value:"proxyUserName: Proxy server user name"}
@Field {value:"proxyPassword: proxy server password"}
public type ProxyConfig {
    string host,
    int port,
    string userName,
    string password,
};

@Description { value:"This struct represents the options to be used for connection throttling" }
@Field {value:"maxActiveConnections: Number of maximum active connections for connection throttling. Default value -1, indicates the number of connections are not restricted"}
@Field {value:"waitTime: Maximum waiting time for a request to grab an idle connection from the client connector"}
@Field {value:"maxActiveStreamsPerConnection: Maximum number of active streams allowed per an HTTP/2 connection"}
public type ConnectionThrottling {
    int maxActiveConnections = -1,
    int waitTime = 60000,
    int maxActiveStreamsPerConnection = -1,
};

@Description { value:"AuthConfig record represents the authentication mechanism that HTTP client uses" }
@Field {value:"scheme: scheme of the configuration. (basic, oauth, jwt etc.)"}
@Field {value:"username: username for basic authentication"}
@Field {value:"username: password for basic authentication"}
@Field {value:"accessToken: access token for oauth2 authentication"}
@Field {value:"refreshToken: refresh token for oauth2 authentication"}
@Field {value:"refreshToken: refresh token for oauth2 authentication"}
@Field {value:"refreshUrl: refresh token url for oauth2 authentication"}
@Field {value:"consumerKey: consume key for oauth2 authentication"}
@Field {value:"consumerKey: consume key for oauth2 authentication"}
@Field {value:"consumerSecret: consume secret for oauth2 authentication"}
@Field {value:"tokenUrl: token url for oauth2 authentication"}
@Field {value:"clientId: clietnt id for oauth2 authentication"}
@Field {value:"clientSecret: client secret for oauth2 authentication"}
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
