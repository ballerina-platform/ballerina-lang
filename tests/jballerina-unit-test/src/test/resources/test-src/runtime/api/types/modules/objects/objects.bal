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

import ballerina/jballerina.java;

public client class PublicClientObject {
    remote function getRemoteCounter(int num, decimal value, string msg = "test message") {
    }

    public function testFunction(int num, decimal value, string msg = "test message") {
    // do nothing
    }
}

public isolated service class Service {

    isolated resource function get resourceFunction(string test) returns string {
        return "foo";
    }

    isolated remote function remoteFunction(int num, decimal value, string msg = "test message") {
    }
}

public distinct class Common {

}

public distinct service class Iterable {
    string name;

    public function init(string name) {
        self.name = name;
    }

    isolated resource function get name() returns string {
        return self.name;
    }
}

public distinct service class Collection  {
    *Iterable;
    *Common;

    public function init(string name) {
        self.name = name;
    }

    isolated resource function get name() returns string {
        return self.name;
    }
}

public distinct class Fruit {
    string color;

    public function init(string color) {
        self.color = color;
    }

    function getColor() returns string {
        return self.color;
    }
}

public distinct class Apple  {
    *Fruit;
    *Common;

    public function init(string color) {
        self.color = color;
    }

    function getColor() returns string {
        return self.color;
    }
}

public function getParameters(PublicClientObject obj, string name) returns [string, boolean, string][] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getFunctionString(object {} obj, string name) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getParamTypesString(function func) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getConstituentTypes(any value) returns string[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

public function getTypeIds(Common common) returns string[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

