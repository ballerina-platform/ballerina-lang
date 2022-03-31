// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type Person record {|
    string name;
    int age;
|};

type Employee record {|
    *Person;
    string designation;
|};

public class PersonClass {
    string name;
    int age;

    public function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    public function getName() returns string => self.name;

    public function getAge() returns int => self.age;
}

const ABC = "abc";

public type Digit 0|1|2|3|4|5|6|7|8|9;

public type ExampleErr distinct error;

listener ListenerCls aListner = new();

isolated service / on aListner {
    final string name = "example";

    final int id = 0;

    resource function get hello(int number) returns string {
        return "/" + number.toString();
    }

}

class ListenerCls {
    public function init() returns error? {
    }

    public function attach(service object {} s, string|string[]? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }
}
