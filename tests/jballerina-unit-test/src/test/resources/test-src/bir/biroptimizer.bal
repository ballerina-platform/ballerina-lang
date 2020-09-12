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

import ballerina/http;
import ballerina/io;
import ballerina/cache;

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
        if(d>4) {
            io:println(d);
        }
    }
    if(a>1) {
        a-=1;
    }
    io:println(a);
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

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller,
        http:Request req) returns error? {
        check caller->respond("Hello, World!");
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

public function cacheInserts() {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put("A", "1");
    checkpanic cache.put("B", "2");
    checkpanic cache.put("C", "3");
    checkpanic cache.put("D", "4");
    checkpanic cache.put("E", "5");
    checkpanic cache.put("F", "6");
    checkpanic cache.put("G", "7");
    checkpanic cache.put("H", "8");
    checkpanic cache.put("I", "9");
    checkpanic cache.put("J", "10");
    checkpanic cache.put("K", "11");
    var res = [cache.keys(), cache.size()];
}
