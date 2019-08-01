// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/reflect;

annotation map<int> t1 on service;
annotation map<string> t2 on resource function;

int globalInt = 10;
string globalString = "hello";

public function testAnnotEvaluation() returns boolean[6] {
    var [a1, b1, c1] = getAnnotsAndService();
    int origGlobalInt = 10;
    string origGlobalString = "hello";

    boolean v1 = a1 is map<int> && a1["one"] == origGlobalInt && a1["two"] == 22;
    boolean v2 = b1 is map<string> && b1["strOne"] == "one" && b1["two"] == origGlobalString;

    int u1 = 321;
    string u2 = "world";
    globalInt = u1;
    globalString = u2;

    var [a2, b2, _] = getAnnotsAndService();

    // Annot values should now be updated.
    boolean v3 = a2 is map<int> && a2["one"] == u1 && a2["two"] == 22;
    boolean v4 = b2 is map<string> && b2["strOne"] == "one" && b2["two"] == u2;

    // Annots of the original service returned should not have changed.
    typedesc<any> t = typeof c1;
    map<int>? m1 = t.@t1;
    map<string>? m2 = <map<string>?> reflect:getResourceAnnotations(c1, "res", "t2");

    boolean v5 = m1 is map<int> && m1["one"] == origGlobalInt && m1["two"] == 22;
    boolean v6 = m2 is map<string> && m2["strOne"] == "one" && m2["two"] == origGlobalString;

    return [v1, v2, v3, v4, v5, v6];
}

function getAnnotsAndService() returns [map<int>?, map<string>?, service] {
    service ser = @t1 {
        one: globalInt,
        two: 22
    } service {

        @t2 {
            strOne: "one",
            two: globalString
        }
        resource function res() {

        }
    };

    typedesc<any> t = typeof ser;
    map<int>? m1 = t.@t1;
    map<string>? m2 = <map<string>?> reflect:getResourceAnnotations(ser, "res", "t2");
    return [m1, m2, ser];
}
