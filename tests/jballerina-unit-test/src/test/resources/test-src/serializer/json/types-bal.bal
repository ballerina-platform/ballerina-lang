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

import ballerina/io;

type Student record {|
    string name = "";
    int age = 0;
    Grades grades = {};
    string...;
|};

type Grades record {|
    int maths = 0;
    int physics = 0;
    int chemistry = 0;
|};

function getThatStudent() returns Student {
    Grades g = { maths: 100, physics:100, chemistry:100 };
    Student s = { name: "Mic", age:17, grades:g };
    return s;
}

function giveATuple() returns (int ,(string, int, float)) {
    return (1, ("ABC", 42, 0.012345));
}
