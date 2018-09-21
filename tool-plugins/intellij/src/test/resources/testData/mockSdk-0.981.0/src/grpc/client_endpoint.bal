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

documentation {
    The gRPC client endpoint provides the capability for initiating contact with a remote gRPC service. The API it
    provides includes functions to send request/error messages.
}
public type Client object {

    documentation {
        Gets invoked to initialize the endpoint. During initialization, configurations provided through the `config`
        record is used for endpoint initialization.

        P{{config}} - The ClientEndpointConfig of the endpoint.
    }
    public extern function init(ClientEndpointConfig config);

    documentation {
        Gets called every time a service attaches itself to this endpoint - also happens at package init time.
        Not supported in client endpoint.

        P{{serviceType}} - The type of the service to be registered.
    }
    public extern function register(typedesc serviceType);

    documentation {
        Starts the registered service. Not supported in client endpoint.
    }
    public extern function start();

    documentation {
        Stops the registered. Not supported in client endpoint.
    }
    public extern function stop();

    documentation {
        Returns the client connection which is used to send message to server.

        R{{}} - Client connection.
    }
    public extern function getCallerActions() returns GrpcClient;
};

documentation {
    Represents client endpoint configuration.

    F{{url}} - The server url.
    F{{secureSocket}} - The SSL configurations for the client endpoint.
}
public type ClientEndpointConfig record {
    string url,
    SecureSocket? secureSocket,
};

documentation {
    SecureSocket struct represents SSL/TLS options to be used for gRPC client invocation.

    F{{trustStore}} - TrustStore related options.
    F{{keyStore}} - KeyStore related options.
    F{{protocol}} - SSL/TLS protocol related options.
    F{{certValidation}} - Certificate validation against CRL or OCSP related options.
    F{{ciphers}} - List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                   TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA.
    F{{verifyHostname}} - Enable/disable host name verification.
    F{{shareSession}} - Enable/disable new ssl session creation.
    F{{ocspStapling}} - Enable/disable ocsp stapling.
}
public type SecureSocket record {
    TrustStore? trustStore,
    KeyStore? keyStore,
    Protocols? protocol,
    ValidateCert? certValidation,
    string[] ciphers,
    boolean verifyHostname = true,
    boolean shareSession = true,
    boolean ocspStapling,
};
