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

type SType service object {
    remote function onMesage(anydata data);
    resource function get foo/bar();
    resource function get foo/[string j]();
};

service class SClass {
    *SType;
}

service class ROne {
    resource function get foo() returns string {
        return "foo";
    }
}

// Not having a implementation for "get foo" resource method is expected as resource method is not part of the obj type.
service class RTwo {
    *ROne;
}

type RType service object {
    *ROne;
    *RTwo;
};

service class RTypeImpl {
    *RType;
}


service class DoDone {
    resource function 'do f() returns int => 0;

    resource function done f() returns int => 0;

    public function 'do() returns int => 0;
}

service class Do {
    *DoDone;
    public function 'do() returns int => 0;
}

function checkObjectAssignability() {
    Do 'do = new DoDone();
}

service class Foo {
    resource function get greeting() returns string => "hello";

    remote function hello() {

    }
}

service class Bar {
    remote function hello() {

    }
}

function positive() {
    Foo f = new Bar();
}

service class Baz {

}

function negative() {
    Foo f = new Baz();
}
