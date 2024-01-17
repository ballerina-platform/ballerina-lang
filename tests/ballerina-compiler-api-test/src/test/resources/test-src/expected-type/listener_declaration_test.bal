// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public listener Listener test = ;


public type KeyStore record {|
    string path;
    string password;
|};

public type Key record {|
    string privateKey;
    string pubicCert;
|};

public type SecureSocket record {|
    KeyStore|Key key;
    string protocol;
    string auth?;
|};

public type Config record {|
    SecureSocket? secureSocket = ();
    int timeout = 60;
    string 'version = "2.0";
|};

public class Listener {

    private int port = 0;

    public function init(int port, *Config config) returns error? {

    }

    public function 'start() returns error? {
        return self.startEndpoint();
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        error err = error("not implemented");
        return err;
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function detach(service object {} s) returns error? {
        return ();
    }

    public function initEndpoint() returns error? {
        return ();
    }

    function register(service object {} s, string[]|string? name) returns error? {
        return ();
    }

    function startEndpoint() returns error? {
        return ();
    }
}

public listener Listener li = new (9090, secureSocket = {'key: {}});

