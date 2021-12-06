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

import ballerina/lang.runtime;

public string str1 = "qwerty1";
public string str2 = "qwerty2";
public int integer1 = 100;
public int integer2 = 200;

public function getInt(int a, int b) returns int {
    return a + b;
}

public function getString(string a, string b) returns string {
    return a + b + getStringVal(3, 4);
}

public function getStringOrError(string a, string b) returns string|error {
    return error("error");
}

function getStringVal(int a, int b) returns string {
    @strand {thread: "any"}
    worker w1 returns string {
        runtime:sleep(0.1);
        return a.toString();
    }

    @strand {thread: "any"}
    worker w2 returns string {
        runtime:sleep(0.2);
        return b.toString();
    }

    string ans1 = wait w1;
    string ans2 = wait w2;
    return ans1 + ans2;
}
