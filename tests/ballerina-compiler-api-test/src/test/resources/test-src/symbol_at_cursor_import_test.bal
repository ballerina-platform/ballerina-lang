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

import testorg/testproject;

function testImports() {
    string s = testproject:TRUE;
    int x = testproject:add(10, 20);

    testproject:Person p = {name: "John Doe", age: 20};
}

function area(float radius) returns float {
    return testproject:PI * radius * radius;
}

function testMethodUsage() {
    testproject:PersonObj p = new("John", 20);
    string s = p.getName();
}

function testMethodsInAbstractObject() {
    testproject:HumanObj ho = testproject:loadHuman();
    string eatResult = ho.eatFunction();
    int walkResult = ho.walkFunction();
    int age = ho.age;
}
