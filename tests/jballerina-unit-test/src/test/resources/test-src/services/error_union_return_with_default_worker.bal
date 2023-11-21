// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import ballerina/jballerina.java;
import ballerina/test;

public class Listener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
        return externAttach(s);
    }
}

isolated function externAttach(service object {} s) returns error? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.MockListener",
    name: "attach"
} external;

listener Listener lsn = new();

service / on lsn {
    resource function get number() returns int|error {
        worker w1 returns error? {
            int a = 10;
            a -> function;
        }
        int b = <- w1;
        return b;
    }
}

function invokeResource(string name) returns future<int|error> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.MockListener",
    name: "invokeResourceWithUnionReturn"
} external;

public function testErrorUnionWithDefaultWorkerFunction() {
    future<int|error> num = invokeResource("$get$number");
    int|error res = wait num;
    if (res is int) {
        test:assertEquals(10, res);
    }

    Class c = new(10);
    test:assertEquals(10, checkpanic c.getId());
}

public class Class {
    private int id;

    public function init(int id) {
        self.id = id;
    }

    public function getId() returns int|error {
        worker w1 returns error? {
            self.id -> function;
        }
        return <- w1;
    }
}
