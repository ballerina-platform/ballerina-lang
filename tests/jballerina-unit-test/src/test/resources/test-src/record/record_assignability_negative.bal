// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testOpenRecordAssignabilityNegative() {
    record {int|string|boolean a; int|boolean b;} r1 = {a: 1, b: true};
    record {int|string|boolean a; int|string b;} r2 = r1;
    record {int|string|boolean a; int|string b;} _ = r1;
}

function testClosedRecordAssignabilityNegative() {
    record {|int|string|boolean a; int|boolean b;|} r1 = {a: 1, b: true};
    record {|int|string|boolean a; int|string b;|} r2 = r1;
    record {|int|string|boolean a; int|string b;|} _ = r1;

    record {|int|string|boolean a; int|boolean...;|} r3 = {a: 1, "c": true};
    record {|int|string|boolean a; int|string...;|} r4 = r3;
    record {|int|string|boolean a; int|string...;|} _ = r3;

    record {|int|string|boolean a; int|boolean b; int|boolean...;|} r5 = {a: 1, b: true, "c": true};
    record {|int|string|boolean a; int|string b; int|string...;|} r6 = r5;
    record {|int|string|boolean a; int|string b; int|string...;|} _ = r5;
}
