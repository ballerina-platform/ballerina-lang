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

import ballerina/crypto;
import ballerina/'lang\.object as lang;

# Represents server listener where one or more services can be registered. so that ballerina program can offer
# service through this listener.
public type Listener object {

    *lang:AbstractListener;

    private int port = 0;
    private ServiceEndpointConfiguration config = {};

    # Starts the registered service.
    #
    # + return - Returns an error if encounters an error while starting the server, returns nil otherwise.
    public function __start() returns error? {
        return self.start();
    }

    # Stops the registered service.
    #
    # + return - Returns an error if encounters an error while stopping the server, returns nil otherwise.
    public function __stop() returns error? {
        return self.stop();
    }

    # Gets called every time a service attaches itself to this endpoint - also happens at module init time.
    #
    # + s - The type of the service to be registered.
    # + name - Name of the service.
    # + return - Returns an error if encounters an error while attaching the service, returns nil otherwise.
    public function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    # Gets called when the endpoint is being initialize during module init time.
    #
    # + port - Listener port.
    # + config - The ServiceEndpointConfiguration of the endpoint.
    public function __init(int port, ServiceEndpointConfiguration? config = ()) {
        self.config = config ?: {};
        self.port = port;
        error? err = self.initEndpoint();
        if (err is error) {
            panic err;
        }
    }

    function initEndpoint() returns error? = external;

    function register(service serviceType, string? name) returns error? = external;

    function start() returns error? = external;

    function stop() returns error? = external;
};

# Maximum number of requests that can be processed at a given time on a single connection.
const int MAX_PIPELINED_REQUESTS = 10;

# Constant for the default listener endpoint timeout
const int DEFAULT_LISTENER_TIMEOUT = 120000; //2 mins

# Represents the gRPC server endpoint configuration.
#
# + host - The server hostname.
# + secureSocket - The SSL configurations for the client endpoint.
# + httpVersion - HTTP version supported by the endpoint. This should be 2.0 as gRPC works only with HTTP/2.
# + timeoutInMillis - Period of time in milliseconds that a connection waits for a read/write operation. Use value 0 to
#                   disable timeout.
# + requestLimits - Configures the parameters for request validation.
public type ServiceEndpointConfiguration record {|
    string host = "0.0.0.0";
    ServiceSecureSocket? secureSocket = ();
    string httpVersion = "2.0";
    RequestLimits? requestLimits = ();
    int timeoutInMillis = DEFAULT_LISTENER_TIMEOUT;
|};

# Configures the SSL/TLS options to be used for HTTP service.
#
# + trustStore - Configures the trust store to be used
# + keyStore - Configures the key store to be used
# + certFile - A file containing the certificate of the server
# + keyFile - A file containing the private key of the server
# + keyPassword - Password of the private key if it is encrypted
# + trustedCertFile - A file containing a list of certificates or a single certificate that the server trusts
# + protocol - SSL/TLS protocol related options
# + certValidation - Certificate validation against CRL or OCSP related options
# + ciphers - List of ciphers to be used (e.g.: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
#             TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA)
# + sslVerifyClient - The type of client certificate verification
# + shareSession - Enable/disable new SSL session creation
# + ocspStapling - Enable/disable OCSP stapling
# + handshakeTimeoutInSeconds - SSL handshake time out
# + sessionTimeoutInSeconds - SSL session time out
public type ServiceSecureSocket record {|
    crypto:TrustStore? trustStore = ();
    crypto:KeyStore? keyStore = ();
    string certFile = "";
    string keyFile = "";
    string keyPassword = "";
    string trustedCertFile = "";
    Protocols? protocol = ();
    ValidateCert? certValidation = ();
    string[] ciphers = ["TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                        "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256"];
    string sslVerifyClient = "";
    boolean shareSession = true;
    ServiceOcspStapling? ocspStapling = ();
    int handshakeTimeoutInSeconds?;
    int sessionTimeoutInSeconds?;
|};

# Configures limits for requests. If these limits are violated, the request is rejected.
#
# + maxUriLength - Maximum allowed length for a URI. Exceeding this limit will result in a
#                  `414 - URI Too Long` response.
# + maxHeaderSize - Maximum allowed size for headers. Exceeding this limit will result in a
#                   `413 - Payload Too Large` response.
# + maxEntityBodySize - Maximum allowed size for the entity body. Exceeding this limit will result in a
#                       `413 - Payload Too Large` response.
public type RequestLimits record {|
    int maxUriLength = -1;
    int maxHeaderSize = -1;
    int maxEntityBodySize = -1;
|};
