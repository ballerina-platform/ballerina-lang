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

type MyRecord record {|
|};

class MyClass {
}

type MyObject object {
};

type MyError error;

function MyFunction() {
}

listener Listener lstnr = new Listener();

service MyService on lstnr {
}

readonly class Listener {
    public isolated function __attach(service s, string? name = ()) returns error? {
    }

    public isolated function __detach(service s) returns error? {
    }

    public isolated function __start() returns error? {
    }

    public isolated function __gracefulStop() returns error? {
    }

    public isolated function __immediateStop() returns error? {
    }
}

const MyConstant = "const";

public isolated function main() {
    typedesc t1 = MyRecord;
    typedesc t2 = MyClass;
    typedesc t3 = MyObject;
    typedesc t4 = MyError;

    function () f = MyFunction;
    service ser = MyService;
    Listener l = lstnr;
    MyConstant mc = MyConstant;
}
