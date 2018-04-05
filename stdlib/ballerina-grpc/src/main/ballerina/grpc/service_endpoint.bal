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
package ballerina.grpc;

@Description {value:"Represents the gRPC service endpoint"}
@Field {value:"config: gRPC service endpoint configuration"}
public type Service object {
    public {
        ServiceEndpointConfiguration config;
    }

    @Description { value:"Gets called when the endpoint is being initialized during the package initialization."}
    @Param { value:"epName: The endpoint name" }
    @Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
    @Return { value:"Error occured during initialization" }
    public function init (ServiceEndpointConfiguration config) {
        self.config = config;
        var err = self.initEndpoint();
        if (err != null) {
            throw err;
        }
    }

    public native function initEndpoint() returns (error);

    @Description { value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
    @Param { value:"ep: The endpoint to which the service should be registered to" }
    @Param { value:"serviceType: The type of the service to be registered" }
    public native function register (typedesc serviceType);

    @Description { value:"Starts the registered service"}
    public native function start ();

    @Description { value:"Stops the registered service"}
    public native function stop ();

    @Description { value:"Returns the connector that client code uses"}
    @Return { value:"The connector that client code uses" }
    public native function getClient () returns (ClientResponder);
}

@Description {value:"Represents the gRPC server endpoint configuration"}
@Field {value:"host: The server hostname"}
@Field {value:"port: The server port"}
@Field {value:"ssl: The SSL configurations for the service endpoint"}
public type ServiceEndpointConfiguration {
        string host,
        int port,
        SslConfiguration ssl
}

@Description {value:"Represents the SSL configurations for the service endpoint"}
public type SslConfiguration {
        string trustStoreFile,
        string trustStorePassword,
        string keyStoreFile,
        string keyStorePassword,
        string sslEnabledProtocols,
        string ciphers,
        string sslProtocol,
        boolean validateCertEnabled,
        string sslVerifyClient,
        string certPassword,
        string tlsStoreType,
        int cacheSize,
        int cacheValidityPeriod
}

public type Endpoint object {
    function getEndpoint() returns (Service) {
        return {};
    }
}
