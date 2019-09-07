// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    string name = "";
    int age = 0;
};

function uninitAccess() {
    Person p;
    string name = p.name;
    name = "";

    p.age = 25;
    p["age"] = 30;

    Person p1 = getUninitializedRecord11(p);

    Person p2 = getUninitializedRecord21();
}

function getUninitializedRecord11(Person p) returns Person {
    Person p2 = getUninitializedRecord12(p);
    return p2;
}

function getUninitializedRecord12(Person p) returns Person {
    Person p2 = p;
    return p2;
}

function getUninitializedRecord21() returns Person {
    Person p3 = getUninitializedRecord22();
    return p3;
}

function getUninitializedRecord22() returns Person {
    Person p4;
    return p4;
}
