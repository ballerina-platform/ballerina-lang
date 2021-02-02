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

function test() {
    [int, string, float] tup = [10, "foo", 12.34];

    record {|
        string name;
        int age;
    |} person = {name: "John Doe", age: 20};

    var tab = table [{"name":"John Doe", age:24}];

    object {
        string name;
        function getName() returns string;
    } person = object {
        string name = "Anon";

        function getName() returns string => self.name;
    };

    PersonObj p1 = new("Pubudu");
    PersonObj p2 = new PersonObj("Pubudu");

    error err1 = error("IOError");
    TimeOutError err2 = error TimeOutError("TimeOutError", url = "https://ballerina.io");
}

class PersonObj {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string => self.name;
}

type TimeOutErrorData record {|
    string message = "";
    error cause?;
    string url;
|};

type TimeOutError error<TimeOutErrorData>;
