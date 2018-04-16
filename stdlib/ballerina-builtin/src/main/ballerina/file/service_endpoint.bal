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

package ballerina.file;

/////////////////////////
/// Listener Endpoint ///
/////////////////////////
public type Listener object {
    private {
        Connection conn;
        ListenerEndpointConfiguration config;
    }

    @Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
    @Param {value:"config: The ServiceEndpointConfiguration of the endpoint"}
    @Return {value:"Error occured during initialization"}
    public function init(ListenerEndpointConfiguration config);

    @Description {value:"Gets called when the endpoint is being initialized during the package initialization time"}
    @Return {value:"Error occured during initialization"}
    public native function initEndpoint () returns (error);

    @Description {value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
    @Param {value:"ep: The endpoint to which the service should be registered to"}
    @Param {value:"serviceType: The type of the service to be registered"}
    public native function register (typedesc serviceType);

    @Description {value:"Starts the registered service"}
    public native function start ();

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public native function getClient () returns (Connection);

    @Description {value:"Stops the registered service"}
    public native function stop ();
};

@Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
@Param {value:"config: The ServiceEndpointConfiguration of the endpoint"}
@Return {value:"Error occured during initialization"}
public function Listener::init (ListenerEndpointConfiguration config) {
    self.config = config;
    var err = self.initEndpoint();
    if (err != null) {
        throw err;
    }
}

@Description {value:"Configuration for local file system service endpoint"}
@Field {value:"path: Listener directory path"}
@Field {value: "recursive: Recursively monitor all folders in the given folder path"}
public type ListenerEndpointConfiguration {
    @readonly string path,
    @readonly boolean recursive,
};

public type Connection {};
