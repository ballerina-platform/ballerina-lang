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

type Age record {
    int age;
    string format;
};

type Person record {
    string name;
    boolean married;
    Age age;
    (string, int) extra;
    !...
};

function testVariableAssignment() returns (string, boolean) {
    string fName;
    boolean married;
    int theAge;
    string format;
    string extraLetter;
    int extraInt;
    map theMap;

    Age a = {age:12, format: "Y", three: "three"};
    //Person p = {name: "Peter", married: true, age: a};

    {name: fName, married, age: { age: theAge, format, !...}, ...theMap} = {name: "Peter", married: true, age: {age:12, format: "Y"}};
    //(string, boolean) x = ("Peter", true);
    //(fName, married) = x;
    return (fName, married);
}

