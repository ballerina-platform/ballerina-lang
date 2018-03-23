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

package ballerina.net.http;

import ballerina/io;
////////////////////////////////
///// HTTP Client Endpoint /////
////////////////////////////////

@Description {value:"Represents an HTTP client endpoint"}
@Field {value:"epName: The name of the endpoint"}
@Field {value:"config: The configurations associated with the endpoint"}
public struct ClientEndpoint {
    string epName;
    ClientEndpointConfiguration config;
    HttpClient httpClient;
}

public enum Algorithm {
    NONE, // defaults to no algorithm as single URL is default
    LOAD_BALANCE,
    FAIL_OVER
}

@Description {value:"Represents the configurations applied to a particular service."}
@Field {value:"uri: Target service URI"}
@Field {value:"secureSocket: SSL/TLS related options"}
public struct TargetService {
    string uri;
    SecureSocket|null secureSocket;
}

@Description { value:"ClientEndpointConfiguration struct represents options to be used for HTTP client invocation" }
@Field {value:"circuitBreaker: Circuit Breaker configuration"}
@Field {value:"endpointTimeout: Endpoint timeout value in millisecond"}
@Field {value:"keepAlive: Specifies whether to reuse a connection for multiple requests"}
@Field {value:"transferEncoding: The types of encoding applied to the request"}
@Field {value:"chunking: The chunking behaviour of the request"}
@Field {value:"httpVersion: The HTTP version understood by the client"}
@Field {value:"forwarded: The choice of setting forwarded/x-forwarded header"}
@Field {value:"followRedirects: Redirect related options"}
@Field {value:"retry: Retry related options"}
@Field {value:"proxy: Proxy server related options"}
@Field {value:"connectionThrottling: Configurations for connection throttling"}
@Field {value:"targets: Service(s) accessible through the endpoint. Multiple services can be specified here when using techniques such as load balancing and fail over."}
@Field {value:"algorithm: The algorithm to be used for load balancing. The HTTP package provides 'roundRobin()' by default."}
@Field {value:"failoverConfig: Failover configuration"}
public struct ClientEndpointConfiguration {
    CircuitBreakerConfig|null circuitBreaker;
    int endpointTimeout;
    boolean keepAlive;
    TransferEncoding transferEncoding;
    Chunking chunking;
    string httpVersion;
    string forwarded;
    FollowRedirects|null followRedirects;
    Retry|null retry;
    Proxy|null proxy;
    ConnectionThrottling|null connectionThrottling;
    TargetService[] targets;
    string|FailoverConfig lbMode;
}

@Description {value:"Initializes the ClientEndpointConfiguration struct with default values."}
@Param {value:"config: The ClientEndpointConfiguration struct to be initialized"}
public function <ClientEndpointConfiguration config> ClientEndpointConfiguration() {
    config.chunking = Chunking.AUTO;
    config.transferEncoding = TransferEncoding.CHUNKING;
    config.httpVersion = "1.1";
    config.forwarded = "disable";
    config.endpointTimeout = 60000;
    config.keepAlive = true;
    config.lbMode = ROUND_ROBIN;
}

@Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
@Param {value:"ep: The endpoint to be initialized"}
@Param {value:"epName: The endpoint name"}
@Param {value:"config: The ClientEndpointConfiguration of the endpoint"}
public function <ClientEndpoint ep> init(ClientEndpointConfiguration config) {
    boolean httpClientRequired = false;
    string uri = config.targets[0].uri;
    match config.lbMode {
        FailoverConfig failoverConfig => {
            if (lengthof config.targets > 1) {
                ep.config = config;
                ep. httpClient = createFailOverClient(config, failoverConfig);
            } else {
                if (uri.hasSuffix("/")) {
                    int lastIndex = uri.length() - 1;
                    uri = uri.subString(0, lastIndex);
                }
                ep.config = config;
                ep.httpClient = createHttpClient(uri, config);
            }
        }

        string lbAlgorithm => {
            if (lengthof config.targets > 1) {
                ep.httpClient = createLoadBalancerClient(config, lbAlgorithm);
            } else {
                if (uri.hasSuffix("/")) {
                    int lastIndex = uri.length() - 1;
                    uri = uri.subString(0, lastIndex);
                }
                ep.config = config;
                var cbConfig = config.circuitBreaker;
                match cbConfig {
                    CircuitBreakerConfig cb => {
                        if (uri.hasSuffix("/")) {
                            int lastIndex = uri.length() - 1;
                            uri = uri.subString(0, lastIndex);
                        }
                        httpClientRequired = false;
                    }
                    int | null => {
                        httpClientRequired = true;
                    }
                }   
                if (httpClientRequired) {
                    ep.httpClient = createHttpClient(uri, config);
                } else {
                    ep.httpClient = createCircuitBreakerClient(uri, config);                    
                }   
                
            }
        }
    }
}

public function <ClientEndpoint ep> register(typedesc serviceType) {

}

public function <ClientEndpoint ep> start() {

}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
public function <ClientEndpoint ep> getClient() returns HttpClient {
    return ep.httpClient;
}

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public function <ClientEndpoint ep> stop() {

}

public native function createHttpClient(string uri, ClientEndpointConfiguration config) returns HttpClient;

@Description { value:"Retry struct represents retry related options for HTTP client invocation" }
@Field {value:"count: Number of retry attempts before giving up"}
@Field {value:"interval: Retry interval in milliseconds"}
public struct Retry {
    int count;
    int interval;
}

@Description { value:"SecureSocket struct represents SSL/TLS options to be used for HTTP client invocation" }
@Field {value: "trustStore: TrustStore related options"}
@Field {value: "keyStore: KeyStore related options"}
@Field {value: "protocols: SSL/TLS protocol related options"}
@Field {value: "validateCert: Certificate validation against CRL or OCSP related options"}
@Field {value:"ciphers: List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"}
@Field {value:"hostNameVerificationEnabled: Enable/disable host name verification"}
@Field {value:"sessionCreationEnabled: Enable/disable new ssl session creation"}
public struct SecureSocket {
    TrustStore|null trustStore;
    KeyStore|null keyStore;
    Protocols|null protocols;
    ValidateCert|null validateCert;
    string ciphers;
    boolean hostNameVerification;
    boolean sessionCreation;
}

@Description {value:"Initializes the SecureSocket struct with default values."}
@Param {value:"config: The SecureSocket struct to be initialized"}
public function <SecureSocket config> SecureSocket() {
    config.hostNameVerification = true;
    config.sessionCreation = true;
}

@Description { value:"FollowRedirects struct represents HTTP redirect related options to be used for HTTP client invocation" }
@Field {value:"enabled: Enable redirect"}
@Field {value:"maxCount: Maximun number of redirects to follow"}
public struct FollowRedirects {
    boolean enabled;
    int maxCount;
}

@Description {value:"Initializes the FollowRedirects struct with default values."}
@Param {value:"config: The FollowRedirects struct to be initialized"}
public function <FollowRedirects config> FollowRedirects() {
    config.enabled = false;
    config.maxCount = 5;
}

@Description { value:"Proxy struct represents proxy server configurations to be used for HTTP client invocation" }
@Field {value:"proxyHost: host name of the proxy server"}
@Field {value:"proxyPort: proxy server port"}
@Field {value:"proxyUserName: Proxy server user name"}
@Field {value:"proxyPassword: proxy server password"}
public struct Proxy {
    string host;
    int port;
    string userName;
    string password;
}

@Description { value:"This struct represents the options to be used for connection throttling" }
@Field {value:"maxActiveConnections: Number of maximum active connections for connection throttling. Default value -1, indicates the number of connections are not restricted"}
@Field {value:"waitTime: Maximum waiting time for a request to grab an idle connection from the client connector"}
public struct ConnectionThrottling {
    int maxActiveConnections;
    int waitTime;
}

@Description {value:"Initializes the ConnectionThrottling struct with default values."}
@Param {value:"config: The ConnectionThrottling struct to be initialized"}
public function <ConnectionThrottling config> ConnectionThrottling() {
    config.maxActiveConnections = -1;
    config.waitTime = 60000;
}

function createCircuitBreakerClient (string uri, ClientEndpointConfiguration configuration) returns HttpClient {
    var cbConfig = configuration.circuitBreaker;
    match cbConfig {
        CircuitBreakerConfig cb => {
            validateCircuitBreakerConfiguration(cb);
            boolean [] httpStatusCodes = populateErrorCodeIndex(cb.httpStatusCodes);
            CircuitBreakerInferredConfig circuitBreakerInferredConfig = {
                                                                            failureThreshold:cb.failureThreshold,
                                                                            resetTimeout:cb.resetTimeout,
                                                                            httpStatusCodes:httpStatusCodes
                                                                        };
            HttpClient cbHttpClient = createHttpClient(uri, configuration);
            CircuitBreakerClient cbClient = {
                                                serviceUri:uri,
                                                config:configuration,
                                                circuitBreakerInferredConfig:circuitBreakerInferredConfig,
                                                httpClient:cbHttpClient,
                                                circuitHealth:{},
                                                currentCircuitState:CircuitState.CLOSED
                                            };
            HttpClient httpClient =  cbClient;
            return httpClient;
        }
        int | null => {
            //remove following once we can ignore
            return createHttpClient(uri, configuration);
        }
    }
}

function createLoadBalancerClient(ClientEndpointConfiguration config, string lbAlgorithm) returns HttpClient {
    HttpClient[] lbClients = createHttpClientArray(config);

    LoadBalancer lb = {
                        serviceUri: config.targets[0].uri,
                        config: config,
                        loadBalanceClientsArray: lbClients,
                        algorithm: lbAlgorithm
                      };
    HttpClient lbClient = lb;
    return lbClient;
}

public function createFailOverClient(ClientEndpointConfiguration config, FailoverConfig foConfig) returns HttpClient {
        HttpClient[] clients = createHttpClientArray(config);

        boolean[] failoverCodes = populateErrorCodeIndex(foConfig.failoverCodes);
        FailoverInferredConfig failoverInferredConfig = {failoverClientsArray : clients,
                                                            failoverCodesIndex : failoverCodes,
                                                            failoverInterval : foConfig.interval};

        Failover failover = {serviceUri:config.targets[0].uri, config:config,
                                failoverInferredConfig:failoverInferredConfig};
        HttpClient foClient = failover;
        return foClient;
}

//function createFailOverClient(ClientEndpointConfiguration config) returns HttpClient {
//    HttpClient[] clients = createHttpClientArray(config);
//    boolean[] failoverCodes = populateErrorCodeIndex(config.failoverConfig.failoverCodes);
//    FailoverInferredConfig failoverInferredConfig = {failoverClientsArray : clients,
//                                          failoverCodesIndex : failoverCodes,
//                                          failoverInterval : config.failoverConfig.interval};
//
//    Failover failover = {serviceUri:config.targets[0].uri, config:config,
//                            failoverInferredConfig:failoverInferredConfig};
//    var httpClient, _ = (HttpClient) failover;
//    return httpClient;
//}
