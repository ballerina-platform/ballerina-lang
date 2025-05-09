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

public type ClientCredentialsGrantConfig record {|
    string tokenUrl;
    ClientConfiguration clientConfig = {};
|};

type AnydataConfig record {|
    anydata auth?;
|};
   

public type ClientConfiguration record {|
    *AnydataConfig;
    ClientAuth auth?;
|};

public type ClientAuth ClientCredentialsGrantConfig;

public function func(ClientAuth cl) {
    anydata a = cl; // OK
}

function testCyclicRecordResolution() {
    ClientAuth cl = {
        tokenUrl : "Token@1",
        clientConfig : {
            auth : {
                tokenUrl : "Token@2"
            } 
        }
    };

    assertValueEquality("Token@1", cl.tokenUrl);
    assertValueEquality("Token@2", cl.clientConfig?.auth?.tokenUrl);
}

final readonly & map<function> func1 = {
    "f1": f2
};

public isolated function f2() returns boolean {
    function? res = func1["f1"];
    if res is function {
        return true;
    }
    return false;
}

function testFunctionPointerNotCyclicViaRecordField() {
    assertValueEquality(true, f2());
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
