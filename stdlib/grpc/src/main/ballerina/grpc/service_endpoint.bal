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
import ballerina/system;
import ballerina/io;

# TODO: check
# Represents service endpoint where one or more services can be registered. so that ballerina program can offer
# service through this endpoint.
#
# + id - Caller endpoint id.
public type Server object {

    *AbstractListener;

    public function __start() returns error? {
        io:println("Calling start.....");
        return self.start();
    }

    public function __stop() returns error? {
        io:println("Calling stop.....");
        return self.stop();
    }

    public function __attach(service s, map annotationData) returns error? {
        io:println("Calling attach.....");
        return self.register(s, annotationData);
    }

    //@readonly public Remote remoteDetails = {};
    //@readonly public Local local = {};
    //@readonly public string protocol = "";

    //private string instanceId;

    public function __init(ServiceEndpointConfiguration config) {
        io:println("Calling init.....");
        //self.instanceId = system:uuid();
        //self.config = config;
        self.init(config);
    }

    # Gets called when the endpoint is being initialize during module init time.
    #
    # + config - The ServiceEndpointConfiguration of the endpoint.
    public extern function init(ServiceEndpointConfiguration config);

    //public extern function initEndpoint() returns error?;

    # Gets called every time a service attaches itself to this endpoint - also happens at module init time.
    #
    # + serviceType - The type of the service to be registered.
    # + annotationData - Annotations attached to the service.
    public extern function register(service serviceType, map annotationData) returns error?;

    # Starts the registered service.
    public extern function start() returns error?;

    # Returns the connector that client code uses.
    #
    # + return - The connector that client code uses
    public extern function getCallerActions() returns Caller;

    # Stops the registered service.
    public extern function stop() returns error?;
};

# Presents a read-only view of the remote address.
#
# + host - The remote host name/IP
# + port - The remote port
public type Remote record {
    @readonly string host = "";
    @readonly int port = 0;
    !...
};

# Presents a read-only view of the local address.
#
# + host - The local host name/IP
# + port - The local port
public type Local record {
    @readonly string host = "";
    @readonly int port = 0;
    !...
};

# Represents the gRPC server endpoint configuration.
#
# + host - The server hostname.
# + port - The server port.
# + secureSocket - The SSL configurations for the client endpoint.
public type ServiceEndpointConfiguration record {
    string host = "0.0.0.0";
    int port = 0;
    ServiceSecureSocket? secureSocket = ();
    !...
};

# SecureSocket struct represents SSL/TLS options to be used for gRPC service.
#
# + trustStore - TrustStore related options.
# + keyStore - KeyStore related options.
# + certFile - A file containing the certificate of the server.
# + keyFile - A file containing the private key of the server.
# + keyPassword - Password of the private key if it is encrypted.
# + trustedCertFile - A file containing a list of certificates or a single certificate that the server trusts.
# + protocol - SSL/TLS protocol related options.
# + certValidation - Certificate validation against CRL or OCSP related options.
# + ciphers - List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
#             TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA.
# + sslVerifyClient - The type of client certificate verification.
# + shareSession - Enable/disable new ssl session creation.
# + ocspStapling - Enable/disable ocsp stapling.
public type ServiceSecureSocket record {
    TrustStore? trustStore = ();
    KeyStore? keyStore = ();
    string certFile = "";
    string keyFile = "";
    string keyPassword = "";
    string trustedCertFile = "";
    Protocols? protocol = ();
    ValidateCert? certValidation = ();
    string[] ciphers = [];
    string sslVerifyClient = "";
    boolean shareSession = true;
    ServiceOcspStapling? ocspStapling = ();
    !...
};
