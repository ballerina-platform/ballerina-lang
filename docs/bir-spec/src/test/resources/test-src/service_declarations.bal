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

type HelloWorld service object {
    remote function sayHello() returns string;
};

service HelloWorld /foo/bar on new Listener() {

    public string greeting = "Hello World!";

    resource function get greet () returns json => { output: self.greeting };

    remote function sayHello() returns string => self.greeting;
}

service on new Listener() {
    resource function get greet () returns json => { output: "Hello World!" };
}

service / on new Listener() {
    resource function get greet () returns json => { output: "Hello World!" };
}

service HelloWorld "GreetingService" on new Listener() {
    resource function get greet () returns json => { output: "Hello World!" };

    remote function sayHello() returns string => "Hello";
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

    public function attach(service object {} s, string[]|string|() name = ()) returns error? {
    }
}
