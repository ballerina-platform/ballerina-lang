// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

public listener ABCD ep = new ABCD();
public listener ep1 = new ABCD();
listener ep2 = new ABCD();

int startCount = 0;

public class ABCD {

    public function 'start() returns error? {
        startCount += 1;
    }

    public function gracefulStop() returns error? {}

    public function immediateStop() returns error? {}

    public function attach(service object {} s, string[]|string? name = ()) returns error? {}

    public function detach(service object {} s) returns error? {}
}

service /sampleService1 on ep {

    resource function get foo(string b) {
    }

    resource function get bar(string b) {
    }
}

service /sampleService2 on ep1 {
    resource function get foo(string b) {}
}

service /sampleService3 on ep2 {
    resource function get foo(string b) {}
}

function init() returns error? {
    check ep.start();
    check ep1.start();
    check ep2.start();
    if (startCount != 3) {
        panic error("Started listener count differ");
    }
}
