// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

// ========================== Basics ==========================

function testValueTypeInUnion() returns string {
    int|string x = 5;
    if (!(x is int)) {
        int y = x;
        return "int: " + <string> y;
    } else {
        string s = x;
        return s;
    }
}

type A record {
    string a;
    !...
};

type B record {
    string b;
    string c;
    !...
};

function testSimpleRecordTypes_1() returns string {
    A x = {a:"foo"};
    any y = x;
     if (y is A) {
        return y.b + "-" + y.c;
    } else if (y is B) {
        return y.a;
    }

    return "n/a";
}

