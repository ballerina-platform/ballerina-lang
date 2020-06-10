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

import ballerina/runtime;
import ballerina/crypto;
import ballerina/java;

# The gRPC client endpoint provides the capability for initiating contact with a remote gRPC service. The API it
# provides includes functions to send request/error messages.
public type Client client object {

    private ClientConfiguration config = {};
    private string url;

    # Gets invoked to initialize the endpoint. During initialization, the configurations provided through the `config`
    # record are used for the endpoint initialization.
    #
    # + url - The server URL
    # + config - - The `grpc:ClientConfiguration` of the endpoint
    public function init(string url, ClientConfiguration? config = ()) {
        self.config = config ?: {};
        self.url = url;
        error? err = externInit(self, self.url, self.config, globalGrpcClientConnPool);
        if (err is error) {
            panic err;
        }
    }

# Calls when initializing the client endpoint with the service descriptor data extracted from the proto file.
# ```ballerina
# grpc:Error? result = grpcClient.initStub(self, "blocking", ROOT_DESCRIPTOR, getDescriptorMap());
# ```
#
# + clientEndpoint -  Client endpoint
# + stubType - Service Stub type. Possible values: blocking, nonblocking
# + descriptorKey - Key of the proto descriptor
# + descriptorMap - Proto descriptor map with all the dependent descriptors
# + return - A `grpc:Error` if an error occurs while initializing the stub or else `()`
    public function initStub(AbstractClientEndpoint clientEndpoint, string stubType, string descriptorKey, map<any> descriptorMap) returns Error? {
        return externInitStub(self, clientEndpoint, stubType, descriptorKey, descriptorMap);
    }

# Calls when executing a blocking call with a gRPC service.
# ```ballerina
# [anydata, grpc:Headers]|grpc:Error result = grpcClient->blockingExecute("HelloWorld/hello", req, headers);
# ```
#
# + methodID - Remote service method ID
# + payload - Request message. The message type varies with the remote service method parameter
# + headers - Optional headers parameter. The header value are passed only if needed. The default value is `()`
# + return - The response message and headers if executed successfully or else a `grpc:Error`
    public remote function blockingExecute(string methodID, anydata payload, Headers? headers = ()) returns ([anydata, Headers]|Error) {
        var retryConfig = self.config.retryConfiguration;
        if (retryConfig is RetryConfiguration) {
            return retryBlockingExecute(self, methodID, payload, headers, retryConfig);
        }
        return externBlockingExecute(self, methodID, payload, headers);
    }

# Calls when executing a non-blocking call with a gRPC service.
# ```ballerina
# grpc:Error? result = grpcClient->nonBlockingExecute("HelloWorld/hello", req, msgListener, headers);
# ```
#
# + methodID - Remote service method ID
# + payload - Request message. The message type varies with the remote service method parameter
# + listenerService - Call back listener service. This service listens to the response message from the service
# + headers - Optional headers parameter. The header values are passed only if needed. The default value is `()`
# + return - A `grpc:Error` if an error occurs while sending the request or else `()`
    public remote function nonBlockingExecute(string methodID, anydata payload, service listenerService, Headers? headers = ()) returns Error? {
         return externNonBlockingExecute(self, methodID, payload, listenerService, headers);
    }


# Calls when executing  a streaming call with a gRPC service.
# ```ballerina
# grpc:StreamingClient|grpc:Error result = grpcClient->streamingExecute("HelloWorld/lotsOfGreetings", msgListener, headers);
# ```
# + methodID - Remote service method ID
# + listenerService - Call back listener service. This service listens to the response message from the service
# + headers - Optional headers parameter. The header values are passed only if needed. The default value is `()`
# + return - A `grpc:StreamingClient` object if executed successfully or else `()`
    public remote function streamingExecute(string methodID, service listenerService, Headers? headers = ()) returns StreamingClient|Error {
        return externStreamingExecute(self, methodID, listenerService, headers);
    }
};

function retryBlockingExecute(Client grpcClient, string methodID, anydata payload, Headers? headers,
    RetryConfiguration retryConfig) returns ([anydata, Headers]|Error) {
    int currentRetryCount = 0;
    int retryCount = retryConfig.retryCount;
    int interval = retryConfig.intervalInMillis;
    int maxInterval = retryConfig.maxIntervalInMillis;
    int backoffFactor = retryConfig.backoffFactor;
    ErrorType[] errorTypes = retryConfig.errorTypes;
    error? cause = ();

    while (currentRetryCount <= retryCount) {
        var result = externBlockingExecute(grpcClient, methodID, payload, headers);
        if (result is [anydata, Headers]) {
            return result;
        } else {
            if (!(checkErrorForRetry(result, errorTypes))) {
                return result;
            } else {
                cause = result;
            }
        }
        runtime:sleep(interval);
        int newInterval = interval * backoffFactor;
        interval = (newInterval > maxInterval) ? maxInterval : newInterval;
        currentRetryCount += 1;
    }
    return prepareError(ALL_RETRY_ATTEMPTS_FAILED, "Maximum retry attempts completed without getting a result", cause);
}

function externInit(Client clientEndpoint, string url, ClientConfiguration config, PoolConfiguration globalPoolConfig) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.client.FunctionUtils"
} external;

function externInitStub(Client genericEndpoint, AbstractClientEndpoint clientEndpoint, string stubType, string descriptorKey, map<any> descriptorMap) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.client.FunctionUtils"
} external;

function externBlockingExecute(Client clientEndpoint, string methodID, anydata payload, Headers? headers) returns ([anydata, Headers]|Error) =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.client.FunctionUtils"
} external;

function externNonBlockingExecute(Client clientEndpoint, string methodID, anydata payload, service listenerService, Headers? headers) returns Error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.client.FunctionUtils"
} external;

function externStreamingExecute(Client clientEndpoint, string methodID, service listenerService, Headers? headers) returns StreamingClient|Error =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.client.FunctionUtils"
} external;


# Represents the abstract gRPC client endpoint. This abstract object is used in client endpoints generated by the
# Protocol Buffer tool.
public type AbstractClientEndpoint abstract object {};

# Represents grpc client retry functionality configurations.
#
# + retryCount - Maximum number of retry attempts in an failure scenario
# + intervalInMillis - Initial interval between retry attempts
# + maxIntervalInMillis - Maximum interval between two retry attempts
# + backoffFactor - Retry interval will be multiplied by this factor, in between retry attempts
# + errorTypes - Error reasons which should be considered as failure scenarios to retry
public type RetryConfiguration record {|
   int retryCount;
   int intervalInMillis;
   int maxIntervalInMillis;
   int backoffFactor;
   ErrorType[] errorTypes = [INTERNAL_ERROR];
|};

# Represents client endpoint configuration.
#
# + timeoutInMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + poolConfig - Connection pool configuration
# + secureSocket - SSL/TLS related options
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + retryConfiguration - Configures the retry functionality
public type ClientConfiguration record {|
    int timeoutInMillis = 60000;
    PoolConfiguration? poolConfig = ();
    SecureSocket? secureSocket = ();
    Compression compression = COMPRESSION_AUTO;
    RetryConfiguration? retryConfiguration = ();
|};

# Provides the configurations for facilitating secure communication with a remote HTTP endpoint.
#
# + disable - Disable the SSL validation
# + trustStore - Configurations associated with the TrustStore
# + keyStore - Configurations associated with the KeyStore
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
# + handshakeTimeoutInSeconds - SSL handshake time out
# + sessionTimeoutInSeconds - SSL session time out
public type SecureSocket record {|
    boolean disable = false;
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
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
    int handshakeTimeoutInSeconds?;
    int sessionTimeoutInSeconds?;
|};
