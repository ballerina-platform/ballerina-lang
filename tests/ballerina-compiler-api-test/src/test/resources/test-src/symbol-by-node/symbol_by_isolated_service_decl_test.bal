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

@v1 {
    foo: "annot on service"
}
isolated service HelloWorld /foo/bar on new Listener({
                                                host: "pop.example.com",
                                                username: "abc@example.com",
                                                password: "pass123",
                                                pollingInterval: 2,
                                                port: 995
                                            }) {

    public string greeting = "Hello World!";

    isolated resource function get greet/[int x]/hello/[float y]/[string... rest] () returns json => { output: self
    .greeting };

    resource function delete pets () returns record{|Error body;|} {
        return {body:{id: 10, code: "FailedToDelete"}};
    }

    remote function sayHello() returns string => self.greeting;

    function createError() returns @tainted error? => ();
}

public class Listener {

    public function init(PopListenerConfiguration config) {
    }

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

type Error record {
    int id;
    string code;
};

type Annot record {
    string foo;
    int bar?;
};

public const annotation Annot v1 on source service;

public type PopListenerConfiguration record {|
    string host;
    string username;
    string password;
    decimal pollingInterval = 60;
    int port = 995;
|};
