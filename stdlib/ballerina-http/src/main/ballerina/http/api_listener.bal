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

@Description {value:"Representation of an API Listener"}
@Field {value:"config: SecureEndpointConfiguration instance"}
@Field {value:"secureListener: Secure HTTP Listener instance"}
public type APIListener object {
    public {
        SecureEndpointConfiguration config;
        SecureListener secureListener;
    }

    new () {
        secureListener = new;
    }

    public function init (SecureEndpointConfiguration config);
    public function register (typedesc serviceType);
    public function start ();
    public function getCallerActions () returns (Connection);
    public function stop ();
};

public function APIListener::init (SecureEndpointConfiguration config) {
    self.secureListener.init(config);
}

public function APIListener::register (typedesc serviceType) {
    self.secureListener.register(serviceType);
}

public function APIListener::start () {
    self.secureListener.start();
}

public function APIListener::getCallerActions () returns (Connection) {
    return self.secureListener.getCallerActions();
}

public function APIListener::stop () {
    self.secureListener.stop();
}
