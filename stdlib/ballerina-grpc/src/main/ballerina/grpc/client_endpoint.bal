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
    Represents the gRPC client endpoint
}
public type Client object {

    documentation {
        Gets called when the endpoint is being initialize during package init time

        P{{config}} - The ClientEndpointConfig of the endpoint.
    }
    public native function init(ClientEndpointConfig config);

    documentation {
        Gets called every time a service attaches itself to this endpoint - also happens at package
    init time. not supported in client connector

        P{{serviceType}} - The type of the service to be registered.
    }
    public native function register(typedesc serviceType);

    documentation {
        Starts the registered service
    }
    public native function start();

    documentation {
        Stops the registered service
    }
    public native function stop();

    documentation {
        Returns the client connection that servicestub code uses
    }
    public native function getCallerActions() returns (GrpcClient);
};

documentation {
    Represents the gRPC client endpoint configuration

    F{{url}} - The server url.
    F{{secureSocket}} - The SSL configurations for the client endpoint.
}
public type ClientEndpointConfig {
    string url,
    SecureSocket? secureSocket,
};

documentation {
    SecureSocket struct represents SSL/TLS options to be used for gRPC client invocation

    F{{trustStore}} - TrustStore related options.
    F{{keyStore}} - KeyStore related options.
    F{{protocols}} - SSL/TLS protocol related options.
    F{{certValidation}} - Certificate validation against CRL or OCSP related options.
    F{{ciphers}} - List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA.
    F{{verifyHostname}} - Enable/disable host name verification.
    F{{shareSession}} - Enable/disable new ssl session creation.
    F{{ocspStapling}} - Enable/disable ocsp stapling.
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
