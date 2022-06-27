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

type CustomType1 float|int[]|client object{}|int|string;
type CustomType2 float|int[]|(function (int a, string b) returns int|string)|int|string;

client object{} & readonly successClient = client object {

};

function testFunction(int a, string b) returns int|string {
    return 1;
}

public function testErrorInResourceMethodReturningObjectType() {
    var successClientObject = client object {
        resource function get .() returns client object {} {
            return successClient;
        }

        resource function get clientUnionFunctionReturnPath/[int a]/foo(string name) returns int|client object {}|
                (function (int a, string b) returns int|string)|string|record{int a;} {
            return testFunction;
        }

        resource function get readonlyClientReturnPath/[int a]/foo(string name) returns client object {} & readonly {
            return successClient;
        }

        resource function get nullableClientReturnPath/[int a]/foo(string name) returns client object {}? {
            return successClient;
        }

        resource function get nullableClientUnionFunctionReturnPath/[int a]/foo(string name)
            returns (int|client object {} |
                (function (int a, string b) returns int|string)|xml)? {
            return testFunction;
        }

        resource function get nullableReadonlyClientReturnPath/[int a]/foo(string name)
            returns client object {}? & readonly {
            return successClient;
        }
    };
}

public function testErrorInResourceMethodReturningFunctionType() {
    var successClientObject = client object {
        resource function get .() returns function (int a, string b) returns int|string {
            return testFunction;
        }

        resource function get readonlyFunctionReturnPath/[int a]/foo(string name) returns
                function (int a, string b) returns int|string & readonly {
            return testFunction;
        }

        resource function get nullableFunctionReturnPath/[int a]/foo(string name) returns
                (function (int a, string b) returns int|string)? {
            return testFunction;
        }

        resource function get nullableReadonlyFunctionReturnPath/[int a]/foo(string name) returns
                (function (int a, string b) returns int|string)? & readonly {
            return testFunction;
        }
    };
}

public function testErrorInResourceMethodReturningCustomObjectType() {
    var successClientObject = client object {
        resource function get .() returns CustomType1 {
            return successClient;
        }

        resource function get clientUnionFunctionReturnPath/[int a]/foo(string name)
                returns int|CustomType1|CustomType2 {
            return testFunction;
        }

        resource function get readonlyClientReturnPath/[int a]/foo(string name) returns CustomType1 & readonly {
            return successClient;
        }
    };
}

public function testErrorInResourceMethodReturningCustomFunctionType() {
    var successClientObject = client object {
        resource function get .() returns CustomType2 {
            return testFunction;
        }

        resource function get readonlyFunctionReturnPath/[int a]/foo(string name) returns CustomType2 & readonly {
            return testFunction;
        }

        resource function get nullableFunctionReturnPath/[int a]/foo(string name) returns CustomType2? {
            return testFunction;
        }

        resource function get nullableReadonlyFunctionReturnPath/[int a]/foo(string name)
                returns CustomType2? & readonly {
            return testFunction;
        }
    };
}
