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

public class Person {
    public string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }
}

public function main() {
     _ = sum(1, 2);
    earlyReturnFunc(1,2);

    Person p1 = new Person("John");
    string name = p1.getName();
}

function earlyReturnFunc(int a, int b) {
    if (a == 1) {
        return;
    }

    _ = sum(a, b);
}

function sum(int a, int b) returns int {
    return a + b;
}
