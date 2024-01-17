// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type AServiceType service object {
    string message;

    remote function foo(int i) returns int;
};

service class AServiceClass {
    *AServiceType;

    remote function foo(int i) returns int {
        return i + 100;
    }

    resource function get barPath() returns string {
        return self.message;
    }

    resource function get foo/path() returns string => self.message + "foo";

    resource function get .() returns string {
        return self.message + "dot";
    }

    resource function get foo/baz(string s) returns string {
        return s;
    }

    resource function get foo/[int i]() returns int {
        return i;
    }

    resource function get foo/[string s]/[string... r]() returns string {
        string result = s + ", ";
        foreach string rdash in r {
            result += rdash;
        }
        return result;
    }

    function init() {
        self.message = "returned from `barPath`";
    }
}

listener Listener lsn = new();

type ProcessingService service object {
    resource function get processRequest() returns json;
};

service ProcessingService / on lsn {

    public string magic = "The Somebody Else's Problem field";

    resource function get processRequest() returns json => { output: self.magic };

    function createError() returns @tainted error? => ();

    resource function get [int... rest] () returns string => "foo";
}

public class Listener {

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function attach(service object {} s, string[]? name = ()) returns error? {
    }
}

const string A = "SomeLongMsg";
const string B = "AnotherLongMsg";

client class ClientClassA {
    resource function accessor [A] () {
    }
}

client class ClientClassB {
    resource function accessor [A a]/[float f]/[B]/[string... s] () {
    }
}
