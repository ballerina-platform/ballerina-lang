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

@Description {value:"Mock service endpoint which does not open a listening port."}
public type NonListeningServiceEndpoint object{
    private {
        Connection conn;
        ServiceEndpointConfiguration config;
    }

    public function init (ServiceEndpointConfiguration config);
    public native function initEndpoint () returns (error);
    public native function register (typedesc serviceType);
    public native function start ();
    public native function getClient() returns Connection;
    public native function stop ();
};

public function NonListeningServiceEndpoint::init (ServiceEndpointConfiguration config) {
    self.config = config;
    var err = self.initEndpoint();
    if (err != null) {
        throw err;
    }
}
