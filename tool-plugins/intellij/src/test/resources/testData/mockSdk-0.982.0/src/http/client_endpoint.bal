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

# The HTTP client provides the capability for initiating contact with a remote HTTP service. The API it
# provides includes functions for the standard HTTP methods, forwarding a received request and sending requests
# using custom HTTP verbs.

# + epName - The name of the client
# + config - The configurations associated with the client
# + httpClient - The provider which implements the HTTP methods
public type Client object {

    public string epName;
    public ClientEndpointConfig config;
    public CallerActions httpClient;

    # Gets invoked to initialize the endpoint. During initialization, configurations provided through the `config`
    # record is used to determine which type of additional behaviours are added to the endpoint (e.g: caching,
    # security, circuit breaking).
    #
    # + c - The configurations to be used when initializing the endpoint
    public function init(ClientEndpointConfig c);

    # Returns the HTTP actions associated with the endpoint.
    #
    # + return - The HTTP caller actions provider of the endpoint
    public function getCallerActions() returns CallerActions {
        return self.httpClient;
    }
};

# Represents a single service and its related configurations.
#
# + url - URL of the target service
# + secureSocket - Configurations for secure communication with the remote HTTP endpoint
public type TargetService record {
    string url;
    SecureSocket? secureSocket;
    !...
};

# Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
#
# + url - URL of the target service
# + circuitBreaker - Configurations associated with Circuit Breaker behaviour
# + timeoutMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + keepAlive - Specifies whether to reuse a connection for multiple requests
# + chunking - The chunking behaviour of the request
# + httpVersion - The HTTP version understood by the client
# + forwarded - The choice of setting `forwarded`/`x-forwarded` header
# + followRedirects - Configurations associated with Redirection
# + retryConfig - Configurations associated with Retry
# + proxy - Proxy server related options
# + connectionThrottling - Configurations for connection throttling
# + secureSocket - SSL/TLS related options
# + cache - HTTP caching related configurations
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + auth - HTTP authentication releated configurations
public type ClientEndpointConfig record {
    string url;
    CircuitBreakerConfig? circuitBreaker;
    int timeoutMillis = 60000;
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    Chunking chunking = "AUTO";
    string httpVersion = "1.1";
    string forwarded = "disable";
    FollowRedirects? followRedirects;
    RetryConfig? retryConfig;
    ProxyConfig? proxy;
    ConnectionThrottling? connectionThrottling;
    SecureSocket? secureSocket;
    CacheConfig cache;
    Compression compression = COMPRESSION_AUTO;
    AuthConfig? auth;
    !...
};

extern function createHttpClient(string uri, ClientEndpointConfig config) returns CallerActions;

extern function createSimpleHttpClient(string uri, ClientEndpointConfig config) returns CallerActions;

# Provides configurations for controlling the retry behaviour in failure scenarios.
#
# + count - Number of retry attempts before giving up
# + interval - Retry interval in milliseconds
# + backOffFactor - Multiplier of the retry interval to exponentailly increase retry interval
# + maxWaitInterval - Maximum time of the retry interval in milliseconds
public type RetryConfig record {
    int count;
    int interval;
    float backOffFactor;
    int maxWaitInterval;
    !...
};

# Provides configurations for facilitating secure communication with a remote HTTP endpoint.
#
# + trustStore - Configurations associated with TrustStore
# + keyStore - Configurations associated with KeyStore
# + certFile - A file containing the certificate of the client
# + keyFile - A file containing the private key of the client
# + keyPassword - Password of the private key if it is encrypted
# + trustedCertFile - A file containing a list of certificates or a single certificate that the client trusts
# + protocol - SSL/TLS protocol related options
# + certValidation - Certificate validation against CRL or OCSP related options
# + ciphers - List of ciphers to be used
#             eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
# + verifyHostname - Enable/disable host name verification
# + shareSession - Enable/disable new SSL session creation
# + ocspStapling - Enable/disable OCSP stapling
public type SecureSocket record {
    TrustStore? trustStore;
    KeyStore? keyStore;
    string certFile;
    string keyFile;
    string keyPassword;
    string trustedCertFile;
    Protocols? protocol;
    ValidateCert? certValidation;
    string[] ciphers;
    boolean verifyHostname = true;
    boolean shareSession = true;
    boolean ocspStapling;
    !...
};

# Provides configurations for controlling the endpoint's behaviour in response to HTTP redirect related responses.
#
# + enabled - Enable/disable redirection
# + maxCount - Maximum number of redirects to follow
public type FollowRedirects record {
    boolean enabled = false;
    int maxCount = 5;
    !...
};

# Proxy server configurations to be used with the HTTP client endpoint.
#
# + host - Host name of the proxy server
# + port - Proxy server port
# + userName - Proxy server username
# + password - proxy server password
public type ProxyConfig record {
    string host;
    int port;
    string userName;
    string password;
    !...
};

# Provides configurations for throttling connections of the endpoint.
#
# + maxActiveConnections - Maximum number of active connections allowed for the endpoint. The default value, -1,
#                          indicates that the number of connections are not restricted.
# + waitTime - Maximum waiting time for a request to grab an idle connection from the client
# + maxActiveStreamsPerConnection - Maximum number of active streams allowed per an HTTP/2 connection
public type ConnectionThrottling record {
    int maxActiveConnections = -1;
    int waitTime = 60000;
    int maxActiveStreamsPerConnection = -1;
    !...
};

# AuthConfig record can be used to configure the authentication mechanism used by the HTTP endpoint.
#
# + scheme - Scheme of the configuration (Basic, OAuth2, JWT etc.)
# + username - Username for Basic authentication
# + password - Password for Basic authentication
# + accessToken - Access token for OAuth2 authentication
# + refreshToken - Refresh token for OAuth2 authentication
# + refreshUrl - Refresh token URL for OAuth2 authentication
# + consumerKey - Consumer key for OAuth2 authentication
# + consumerSecret - Consumer secret for OAuth2 authentication
# + tokenUrl - Token URL for OAuth2 authentication
# + clientId - Clietnt ID for OAuth2 authentication
# + clientSecret - Client secret for OAuth2 authentication
public type AuthConfig record {
    AuthScheme scheme;
    string username;
    string password;
    string accessToken;
    string refreshToken;
    string refreshUrl;
    string consumerKey;
    string consumerSecret;
    string tokenUrl;
    string clientId;
    string clientSecret;
    !...
};

function Client::init(ClientEndpointConfig c) {
    boolean httpClientRequired = false;
    string url = c.url;
    if (url.hasSuffix("/")) {
        int lastIndex = url.length() - 1;
        url = url.substring(0, lastIndex);
    }
    self.config = c;
    var cbConfig = c.circuitBreaker;
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
        var redirectConfigVal = c.followRedirects;
        match redirectConfigVal {
            FollowRedirects redirectConfig => {
                self.httpClient = createRedirectClient(url, c);
            }
            () => {
                self.httpClient = checkForRetry(url, c);
            }
        }
    } else {
        self.httpClient = createCircuitBreakerClient(url, c);
    }
}

function createRedirectClient(string url, ClientEndpointConfig configuration) returns CallerActions {
    var redirectConfigVal = configuration.followRedirects;
    match redirectConfigVal {
        FollowRedirects redirectConfig => {
            if (redirectConfig.enabled) {
                return new RedirectClient(url, configuration, redirectConfig, createRetryClient(url, configuration));
            } else {
                return createRetryClient(url, configuration);
            }
        }
        () => {
            return createRetryClient(url, configuration);
        }
    }
}

function checkForRetry(string url, ClientEndpointConfig config) returns CallerActions {
    var retryConfigVal = config.retryConfig;
    match retryConfigVal {
        RetryConfig retryConfig => {
            return createRetryClient(url, config);
        }
        () => {
            if (config.cache.enabled) {
                return createHttpCachingClient(url, config, config.cache);
            } else {
                return createHttpSecureClient(url, config);
            }
        }
    }
}

function createCircuitBreakerClient(string uri, ClientEndpointConfig configuration) returns CallerActions {
    var cbConfig = configuration.circuitBreaker;
    match cbConfig {
        CircuitBreakerConfig cb => {
            validateCircuitBreakerConfiguration(cb);
            boolean [] statusCodes = populateErrorCodeIndex(cb.statusCodes);
            CallerActions cbHttpClient = new;
            var redirectConfigVal = configuration.followRedirects;
            match redirectConfigVal {
                FollowRedirects redirectConfig => {
                    cbHttpClient = createRedirectClient(uri, configuration);
                }
                () => {
                    cbHttpClient = checkForRetry(uri, configuration);
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

function createRetryClient(string url, ClientEndpointConfig configuration) returns CallerActions {
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
