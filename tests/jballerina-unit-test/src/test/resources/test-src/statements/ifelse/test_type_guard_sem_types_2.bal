// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type P record {|
    int i;
|};

type Q record {|
    string i;
|};

type R record {|
    anydata i;
|};

function f1(P|Q v) {
    if v is R {

    } else {
        _ = v; // unreachable
    }
}

function f2(any|record {| error x; |} d) {
    if d is any {
        any _  = d;
    } else {
        record {| error x; |} _ = d; // unreachable
    }
}

public type Seconds decimal;
public type Utc readonly & [int, decimal];

public type ValueType int|float|string|boolean|Utc|Seconds|record {}|()|byte[];

isolated function f3(ValueType anyMessage) returns string {
    if anyMessage is float {

    } else if anyMessage is int {

    } else if anyMessage is string {

    } else if anyMessage is byte[] {

    } else if anyMessage is boolean {

    } else if anyMessage is Utc {

    } else if anyMessage is Seconds {

    } else if anyMessage is () {

    } else if anyMessage is record {||} {

    } else if anyMessage is record {} {
        return externGetNameFromRecord(anyMessage);
    }
    return "";
}

isolated function externGetNameFromRecord(record {} rec) returns string = external;
