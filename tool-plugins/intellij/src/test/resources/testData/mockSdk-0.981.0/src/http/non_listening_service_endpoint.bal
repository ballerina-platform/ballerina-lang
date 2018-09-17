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


@Description {value:"Mock service endpoint which does not open a listening port."}
public type NonListener object {
    private Connection conn;
    private ServiceEndpointConfiguration config;

    public function init (ServiceEndpointConfiguration c);
    public extern function initEndpoint () returns (error);
    public extern function register (typedesc serviceType);
    public extern function start ();
    public extern function getCallerActions() returns Connection;
    public extern function stop ();
};

function NonListener::init (ServiceEndpointConfiguration c) {
    self.config = c;
    var err = self.initEndpoint();
    if (err != null) {
        throw err;
    }
}
