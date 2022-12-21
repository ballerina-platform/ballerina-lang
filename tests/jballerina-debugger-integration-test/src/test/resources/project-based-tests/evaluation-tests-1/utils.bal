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

public client class Child {

    remote function getName(string firstName, string lastName = "") returns string|error {
        return firstName + lastName;
    }

    remote function getTotalMarks(int maths, int english) returns int {
        future<int> futureSum = @strand {thread: "any"} start sum(maths, english);
        int|error result = wait futureSum;
        if result is int {
            return result;
        } else {
            return -1;
        }
    }
}

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type, class;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = {name: "John"};

function sum(int a, int b) returns int {
    return a + b;
}

function getName(string name) returns string {
    return "Name: " + name;
}

public function getSum(int a, int b) returns int {
    future<int> futureSum = @strand {thread: "any"} start addition(a, b);
    int|error result = wait futureSum;
    if result is int {
        return result;
    } else {
        return -1;
    }
}

function processTypeDesc(typedesc t) returns typedesc {
    return t;
}
