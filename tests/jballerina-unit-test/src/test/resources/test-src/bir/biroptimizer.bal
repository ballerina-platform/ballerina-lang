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

import ballerina/lang.test;

public function simpleAddition() {
    int a = 3;
    int c = a + 2;
    int d = c + 1;
}

public function complexAddition() {
    int a = 3;
    int c = a + 2;
    int b = 1;
    if(c>3) {
        int d = b + c + 1;
        if (d > 4) {
            //do nothing
        }
    }
    if(a>1) {
        a-=1;
    }
}

int globalA = 5;
string s = "a";
public function globalVarsAndAnonFunctions() {
 int a = 3;
 string l = "b";
    var foo = function (int b) returns int {
        int c = 34;
        if (b == 3) {
            c = c + b + a + globalA;
        }
        string m = s+ l;
        return c + a;
    };
}

service /hello on new test:MockListener(9090) {
    resource function get sayHello(string s) returns error? {
        test:Caller caller = new test:Caller();
        string res = caller -> respond(s);
    }
}

type Person record {|
    string name;
    int age;
    string address = "";
|};

type Employee record {
    string name;
    int age;
};

function mapInits() returns [string?, int?] {
    map<Employee> emp = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    emp["jack"] = jack;
    return [emp["jack"]["name"], emp["jack"]["age"]];
}

function failWithinOnFail() returns string|error {
    int i = 0;
    string str = "";
    while (i < 2) {
        do {
            str += "-> Within do statement";
            i = i + 1;
            fail error("custom error", message = "error value");
        } on fail error e {
            str += "-> Error caught in inner on fail";
            fail e;
        }
        str += "-> After do statement";
    }
    str += "-> Execution completed.";
    return str;
}

int lockWithinLockInt = 0;
string lockWithinLockString = "";
function failLockWithinLock() returns [int, string] {
    lock {
        lockWithinLockInt = 50;
        lockWithinLockString = "sample value";
        lock {
            lockWithinLockString = "second sample value";
            lockWithinLockInt = 99;
            lock {
                lockWithinLockInt = 90;
            }
            error err = error("custom error", message = "error value");
            fail err;
        }
    } on fail error e {
        lockWithinLockInt = 100;
        lockWithinLockString = "Error caught";
    }
    return [lockWithinLockInt, lockWithinLockString];
}
