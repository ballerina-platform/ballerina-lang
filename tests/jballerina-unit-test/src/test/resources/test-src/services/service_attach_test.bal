// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

isolated boolean[] nameParamCheck = [false, false, false, false];

public class Listener1 {
    public isolated function 'start() returns error? { }

    public isolated function gracefulStop() returns error? { }

    public isolated function immediateStop() returns error? { }

    public isolated function detach(service object {} s) returns error? { }

    public isolated function attach(service object {} s, string? name) returns error? {
        lock {
            nameParamCheck[0] = name is () && !(<any> name is string[]);
        }
    }
}

service on new Listener1() {

}

public class Listener2 {
    public isolated function 'start() returns error? { }

    public isolated function gracefulStop() returns error? { }

    public isolated function immediateStop() returns error? { }

    public isolated function detach(service object {} s) returns error? { }

    public isolated function attach(service object {} s, string? name) returns error? {
        lock {
            nameParamCheck[1] = name is string && name == "Service";
        }
    }
}

service "Service" on new Listener2() {

}

public class Listener3 {
    public isolated function 'start() returns error? { }

    public isolated function gracefulStop() returns error? { }

    public isolated function immediateStop() returns error? { }

    public isolated function detach(service object {} s) returns error? { }

    public isolated function attach(service object {} s, string[]? name) returns error? {
        lock {
            nameParamCheck[2] = name is string[] && name[0] == "foo" && name[1] == "bar" && name.length() == 2;
        }
    }
}

service /foo/bar on new Listener3() {

}

public class Listener4 {
    public isolated function 'start() returns error? { }

    public isolated function gracefulStop() returns error? { }

    public isolated function immediateStop() returns error? { }

    public isolated function detach(service object {} s) returns error? { }

    public function attach(service object {} s, string[]? name) returns error? {
            lock {
                nameParamCheck[3] = name is string[] && name.length() == 0;
            }

    }
}

service / on new Listener4() {

}

public function testAttachMethodParams() {
    lock {
        if (nameParamCheck[0] && nameParamCheck[1] && nameParamCheck[2] && nameParamCheck[3]) {
            return;
        }
    }
    panic error("Unexpected value for 'name' param");
}
