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

# The gRPC client endpoint provides the capability for initiating contact with a remote gRPC service. The API it
# provides includes functions to send request/error messages.
public type Client client object {

    # Gets invoked to initialize the endpoint. During initialization, configurations provided through the `config`
    # record is used for endpoint initialization.
    #
    # + url - The server url.
    # + config - - The ClientEndpointConfig of the endpoint.
    public extern function init(string url, ClientEndpointConfig config);

    # Calls when initializing client endpoint with service descriptor data extracted from proto file.
    #
    # + stubType - Service Stub type. possible values: blocking, nonblocking.
    # + descriptorKey - Proto descriptor key. Key of proto descriptor.
    # + descriptorMap - Proto descriptor map. descriptor map with all dependent descriptors.
    # + return - Returns an error if encounters an error while initializing the stub, returns nill otherwise.
    public extern function initStub(string stubType, string descriptorKey, map<any> descriptorMap)
                               returns error?;

    # Calls when executing blocking call with gRPC service.
    #
    # + methodID - Remote service method id.
    # + payload - Request message. Message type varies with remote service method parameter.
    # + headers - Optional headers parameter. Passes header value if needed. Default sets to nil.
    # + return - Returns response message and headers if executes successfully, error otherwise.
    public remote extern function blockingExecute(string methodID, any payload, Headers? headers = ())
                               returns ((any, Headers)|error);

    # Calls when executing non-blocking call with gRPC service.
    #
    # + methodID - Remote service method id.
    # + payload - Request message. Message type varies with remote service method parameter..
    # + listenerService - Call back listener service. This service listens the response message from service.
    # + headers - Optional headers parameter. Passes header value if needed. Default sets to nil.
    # + return - Returns an error if encounters an error while sending the request, returns nil otherwise.
    public remote extern function nonBlockingExecute(string methodID, any payload, service listenerService,
                                              Headers? headers = ()) returns error?;


    # Calls when executing streaming call with gRPC service.
    #
    # + methodID - Remote service method id.
    # + listenerService - Call back listener service. This service listens the response message from service.
    # + headers - Optional headers parameter. Passes header value if needed. Default sets to nil.
    # + return - Returns client connection if executes successfully, error otherwise.
    public remote extern function streamingExecute(string methodID, service listenerService, Headers? headers = ())
                               returns StreamingClient|error;
};

# Represents client endpoint configuration.
#
# + timeoutMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + keepAlive - Specifies whether to reuse a connection for multiple requests
# + httpVersion - The HTTP version understood by the client
# + chunking - The chunking behaviour of the request
# + forwarded - The choice of setting `forwarded`/`x-forwarded` header
# + proxy - Proxy server related options
# + connectionThrottling - Configurations for connection throttling
# + secureSocket - SSL/TLS related options
# + compression - Specifies the way of handling compression (`accept-encoding`) header
public type ClientEndpointConfig record {
    int timeoutMillis = 60000;
    KeepAlive keepAlive = KEEPALIVE_AUTO;
    string httpVersion = "2.0";
    Chunking chunking = CHUNKING_NEVER;
    string forwarded = "disable";
    ProxyConfig? proxy = ();
    ConnectionThrottling? connectionThrottling = {};
    SecureSocket? secureSocket = ();
    Compression compression = COMPRESSION_AUTO;
    !...;
};

# Proxy server configurations to be used with the HTTP client endpoint.
#
# + host - Host name of the proxy server
# + port - Proxy server port
# + userName - Proxy server username
# + password - proxy server password
public type ProxyConfig record {
    string host = "";
    int port = 0;
    string userName = "";
    string password = "";
    !...;
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
    // In order to distribute the workload among multiple connections in HTTP/2 scenario.
    int maxActiveStreamsPerConnection = 20000;
    !...;
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
    TrustStore? trustStore = ();
    KeyStore? keyStore = ();
    string certFile = "";
    string keyFile = "";
    string keyPassword = "";
    string trustedCertFile = "";
    Protocols? protocol = ();
    ValidateCert? certValidation = ();
    string[] ciphers = [];
    boolean verifyHostname = true;
    boolean shareSession = true;
    boolean ocspStapling = false;
    !...;
};
