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
import ballerina/lang.'object as lang;
import ballerinax/java;

# Represents server listener where one or more services can be registered. so that ballerina program can offer
# service through this listener.
public type Listener object {

    *lang:Listener;

    private int port = 0;
    private ListenerConfiguration config = {};

    # Starts the registered service.
    #
    # + return - Returns an error if encounters an error while starting the server, returns nil otherwise.
    public function __start() returns error? {
        return externStart(self);
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    # Stops the registered service.
    #
    # + return - Returns an error if encounters an error while stopping the server, returns nil otherwise.
    public function __immediateStop() returns error? {
        return externStop(self);
    }

    # Gets called every time a service attaches itself to this endpoint - also happens at module init time.
    #
    # + s - The type of the service to be registered.
    # + name - Name of the service.
    # + return - Returns an error if encounters an error while attaching the service, returns nil otherwise.
    public function __attach(service s, string? name = ()) returns error? {
        return externRegister(self, s, name);
    }

    public function __detach(service s) returns error? {
    }

    # Gets called when the endpoint is being initialize during module init time.
    #
    # + port - Listener port.
    # + config - The ServiceEndpointConfiguration of the endpoint.
    public function __init(int port, ListenerConfiguration? config = ()) {
        self.config = config ?: {};
        self.port = port;
        error? err = externInitEndpoint(self);
        if (err is error) {
            panic err;
        }
    }
};

function externInitEndpoint(Listener listenerObject) returns error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.serviceendpoint.InitEndpoint"
} external;

function externRegister(Listener listenerObject, service serviceType, string? name) returns error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.serviceendpoint.Register"
} external;

function externStart(Listener listenerObject) returns error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.serviceendpoint.Start"
} external;

function externStop(Listener listenerObject) returns error? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.serviceendpoint.Stop"
} external;

# Maximum number of requests that can be processed at a given time on a single connection.
const int MAX_PIPELINED_REQUESTS = 10;

# Constant for the default listener endpoint timeout
const int DEFAULT_LISTENER_TIMEOUT = 120000; //2 mins

# Represents the gRPC server endpoint configuration.
#
# + host - The server hostname.
# + secureSocket - The SSL configurations for the client endpoint.
# + timeoutInMillis - Period of time in milliseconds that a connection waits for a read/write operation. Use value 0 to
#                   disable timeout.
public type ListenerConfiguration record {|
    string host = "0.0.0.0";
    ListenerSecureSocket? secureSocket = ();
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
# + sslVerifyClient - The type of client certificate verification. (e.g.: "require" or "optional")
# + shareSession - Enable/disable new SSL session creation
# + ocspStapling - Enable/disable OCSP stapling
# + handshakeTimeoutInSeconds - SSL handshake time out
# + sessionTimeoutInSeconds - SSL session time out
public type ListenerSecureSocket record {|
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
    ListenerOcspStapling? ocspStapling = ();
    int handshakeTimeoutInSeconds?;
    int sessionTimeoutInSeconds?;
|};

