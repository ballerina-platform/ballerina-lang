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

service /MyService on lstnr {
}

readonly class Listener {
    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return;
    }

    public isolated function detach(service object {} s) returns error? {
        return;
    }

    public isolated function 'start() returns error? {
        return;
    }

    public isolated function gracefulStop() returns error? {
        return;
    }

    public isolated function immediateStop() returns error? {
        return;
    }
}

const MyConstant = "const";

public isolated function main() {
    typedesc _ = MyRecord;
    typedesc _ = MyClass;
    typedesc _ = MyObject;
    typedesc _ = MyError;

    function () _ = MyFunction;
    Listener _ = lstnr;
    MyConstant _ = MyConstant;
}
