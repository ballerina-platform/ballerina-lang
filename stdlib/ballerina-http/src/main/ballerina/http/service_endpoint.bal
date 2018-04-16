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

package ballerina.http;

/////////////////////////////
/// HTTP Service Endpoint ///
/////////////////////////////
documentation {
    Represents service endpoint where one or more services can be registered. so that ballerina program can offer service through this endpoint.

    F{{conn}}  - Service endpoint connection.
    F{{config}} - ServiceEndpointConfiguration configurations.
    F{{remote}}  - The details of remote address.
    F{{local}} - The details of local address.
    F{{protocol}}  - The protocol associate with the service endpoint.
}
public type Listener object {
    public {
        @readonly Remote remote;
        @readonly Local local;
        @readonly string protocol;
    }
    private {
        Connection conn;
        ServiceEndpointConfiguration config;
    }
    public function init(ServiceEndpointConfiguration config);

    @Description {value:"Gets called when the endpoint is being initialize during package init time"}
    @Return {value:"Error occured during initialization"}
    public native function initEndpoint() returns (error);

    @Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
    @Param {value:"ep: The endpoint to which the service should be registered to"}
    @Param {value:"serviceType: The type of the service to be registered"}
    public native function register(typedesc serviceType);

    @Description {value:"Starts the registered service"}
    public native function start();

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public native function getClient() returns (Connection);

    @Description {value:"Stops the registered service"}
    public native function stop();
};

documentation {
    Represents the details of remote address.

    F{{host}}  - The remote server host.
    F{{port}} - The remote server port.
}
public type Remote {
    @readonly string host;
    @readonly int port;
};

documentation {
    Represents the details of local address.

    F{{host}}  - The local server host.
    F{{port}} - The local server port.
}
public type Local {
    @readonly string host;
    @readonly int port;
};

@Description {value:"Request validation limits configuration for HTTP service endpoint"}
@Field {value:"maxUriLength: Maximum length allowed in the URL"}
@Field {value:"maxHeaderSize: Maximum size allowed in the headers"}
@Field {value:"maxEntityBodySize: Maximum size allowed in the entity body"}
public type RequestLimits {
    int maxUriLength = -1;
    int maxHeaderSize = -1;
    int maxEntityBodySize = -1;
};

@Description {value:"Configuration for HTTP service endpoint"}
@Field {value:"host: Host of the service"}
@Field {value:"port: Port number of the service"}
@Field {value:"exposeHeaders: The array of allowed headers which are exposed to the client"}
@Field {value:"keepAlive: The keepAlive behaviour of the connection for a particular port"}
@Field {value:"transferEncoding: The types of encoding applied to the response"}
@Field {value:"chunking: The chunking behaviour of the response"}
@Field {value:"secureSocket: The SSL configurations for the service endpoint"}
@Field {value:"httpVersion: Highest HTTP version supported"}
@Field {value:"requestLimits: Request validation limits configuration"}
@Field {value:"filters: Filters to be applied to the request before dispatched to the actual resource"}
public type ServiceEndpointConfiguration {
    string host,
    int port = 9090,
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    TransferEncoding transferEncoding = TRANSFERENCODE_CHUNKING,
    ServiceSecureSocket? secureSocket,
    string httpVersion = "1.1",
    RequestLimits? requestLimits,
    Filter[] filters,
};

@Description {value:"SecureSocket struct represents SSL/TLS options to be used for HTTP service"}
@Field {value:"trustStore: TrustStore related options"}
@Field {value:"keyStore: KeyStore related options"}
@Field {value:"protocols: SSL/TLS protocol related options"}
@Field {value:"certValidation: Certificate validation against CRL or OCSP related options"}
@Field {value:"ciphers: List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"}
@Field {value:"sslVerifyClient: The type of client certificate verification"}
@Field {value:"shareSession: Enable/disable new ssl session creation"}
@Field {value:"ocspStapling: Enable/disable ocsp stapling"}
public type ServiceSecureSocket {
    TrustStore? trustStore,
    KeyStore? keyStore,
    Protocols? protocol,
    ValidateCert? certValidation,
    string[] ciphers,
    string sslVerifyClient,
    boolean shareSession = true,
    ServiceOcspStapling? ocspStapling,
};

public type KeepAlive "AUTO"|"ALWAYS"|"NEVER";

@final KeepAlive KEEPALIVE_AUTO = "AUTO";
@final KeepAlive KEEPALIVE_ALWAYS = "ALWAYS";
@final KeepAlive KEEPALIVE_NEVER = "NEVER";

@Description { value:"Gets called when the endpoint is being initialized during the package initialization."}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function Listener::init (ServiceEndpointConfiguration config) {
    self.config = config;
    var err = self.initEndpoint();
    if (err != null) {
        throw err;
    }
    // if filters are defined, call init on them
    if (config.filters != null) {
        foreach filter in config.filters {
            filter.init();
        }
    }
}

//////////////////////////////////
/// WebSocket Service Endpoint ///
//////////////////////////////////
public type WebSocketListener object {
    public {
        string id;
        string negotiatedSubProtocol;
        boolean isSecure;
        boolean isOpen;
        map attributes;
    }

    private {
        WebSocketConnector conn;
        ServiceEndpointConfiguration config;
        Listener httpEndpoint;
    }

    public new () {
    }

    @Description {value:"Gets called when the endpoint is being initialize during package init time"}
    @Param {value:"epName: The endpoint name"}
    @Param {value:"config: The ServiceEndpointConfiguration of the endpoint"}
    @Return {value:"Error occured during initialization"}
    public function init(ServiceEndpointConfiguration config) {
        self.config = config;
        httpEndpoint.init(config);
    }

    @Description {value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
    @Param {value:"conn: The server connector connection"}
    @Param {value:"res: The outbound response message"}
    @Return {value:"Error occured during registration"}
    public function register(typedesc serviceType) {
        httpEndpoint.register(serviceType);
    }

    @Description {value:"Starts the registered service"}
    @Return {value:"Error occured during registration"}
    public function start() {
        httpEndpoint.start();
    }

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    @Return {value:"Error occured during registration"}
    public function getClient() returns (WebSocketConnector) {
        return conn;
    }

    @Description {value:"Stops the registered service"}
    @Return {value:"Error occured during registration"}
    public function stop() {
        httpEndpoint.stop();
    }
};
