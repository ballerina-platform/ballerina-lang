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
public type Person record {
    string name;
};

public type Department record {
    string name = "IT";
};

public readonly class Employee {
    public Department dept;
    public int id;

    public function init(Department & readonly dept, int id) {
        self.dept = dept;
        self.id = id;
    }
}

public function add(int a, int b) returns int {
    return a + b + 10;
}

public function main() {
    // do nothing
}

public isolated client class Client {
    final string greeting;

    public isolated function init(string greeting = "Hello World!") returns error? {
        self.greeting = greeting;
        return;
    }

    resource isolated function get users\.getPresence(map<string|string[]> headers = {}) returns string|error {
        return string `/users.getPresence ${self.greeting}`;
    }
}
