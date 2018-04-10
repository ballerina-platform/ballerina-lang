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

@Description {value:"Representation of an Secure Listener"}
@Field {value:"config: SecureEndpointConfiguration instance"}
@Field {value:"httpListener: HTTP Listener instance"}
public type SecureListener object {
    public {
        SecureEndpointConfiguration config;
        Listener httpListener;
    }

    new () {
        httpListener = new;
    }

    public function init(SecureEndpointConfiguration config);

    @Description {value:"Gets called when the endpoint is being initialize during package init time"}
    @Return {value:"Error occured during initialization"}
    public function initEndpoint() returns (error);

    @Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
    @Param {value:"ep: The endpoint to which the service should be registered to"}
    @Param {value:"serviceType: The type of the service to be registered"}
    public function register(typedesc serviceType);

    @Description {value:"Starts the registered service"}
    public function start();

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public function getClient() returns (Connection);

    @Description {value:"Stops the registered service"}
    public function stop();
};

@Description {value:"Configuration for secure HTTP service endpoint"}
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
@Field {value:"authProviders: The array of AuthProviders which are used to authenticate the users"}
public type SecureEndpointConfiguration {
    string host,
    int port = 9090,
    KeepAlive keepAlive = KEEPALIVE_AUTO,
    TransferEncoding transferEncoding = TRANSFERENCODE_CHUNKING,
    Chunking chunking = CHUNKING_AUTO,
    ServiceSecureSocket? secureSocket,
    string httpVersion = "1.1",
    RequestLimits? requestLimits,
    Filter[] filters,
    AuthProvider[] authProviders,
};

@Description {value:"Configuration for authentication providers"}
@Field {value:"scheme: Authentication schem"}
@Field {value:"id: Authention provider instance id"}
public type AuthProvider {
    string scheme,
    string id,
};



public function SecureListener::init (SecureEndpointConfiguration config) {
    //TODO init auth handlers.
    addAuthFilters(config);
    self.httpListener.init(config);
}

@Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
@Param {value:"ep: The endpoint to which the service should be registered to"}
@Param {value:"serviceType: The type of the service to be registered"}
public function SecureListener::register (typedesc serviceType) {
    self.httpListener.register(serviceType);
}

@Description {value:"Starts the registered service"}
public function SecureListener::start () {
    self.httpListener.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function SecureListener::getClient () returns (Connection) {
    return self.httpListener.getClient();
}

@Description {value:"Stops the registered service"}
public function SecureListener::stop () {
    self.httpListener.stop();
}