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
type CustomType3 CustomType1|CustomType2;
type CustomType4 float|int[]|client object{}|int|string;
type CustomType5 float|int[]|client object{}|int|string;
type CustomType6 CustomType4|CustomType5;
type CustomType7 CustomType6;
type CustomType8 float|int[]|CustomType9|int|string;
type CustomType9 client object{};
type CustomType10 CustomType9 | CustomType8;

client object{} & readonly successClient = client object {

};

function testFunction(int a, string b) returns int|string {
    return 1;
}

public function testErrorInResourceMethodReturningObjectType() {
    var successClientObject = client object {
        resource function get clientReturnPath() returns client object {} {
            return successClient;
        }

        resource function get clientUnionFunctionReturnPath() returns int|client object {}|
                (function (int a, string b) returns int|string)|string|record{int a;} {
            return testFunction;
        }

        resource function get readonlyClientReturnPath() returns client object {} & readonly {
            return successClient;
        }

        resource function get nullableClientReturnPath() returns client object {}? {
            return successClient;
        }

        resource function get nullableClientUnionFunctionReturnPath()
            returns (int|client object {}|(function (int a, string b) returns int|string)|xml)? {
            return testFunction;
        }

        resource function get nullableReadonlyClientReturnPath()
            returns client object {}? & readonly {
            return successClient;
        }
    };
}

public function testErrorInResourceMethodReturningFunctionType() {
    var successClientObject = client object {
        resource function get functionReturnPath() returns function (int a, string b) returns int|string {
            return testFunction;
        }

        resource function get readonlyFunctionReturnPath() returns
                function (int a, string b) returns int|string & readonly {
            return testFunction;
        }

        resource function get nullableFunctionReturnPath() returns
                (function (int a, string b) returns int|string)? {
            return testFunction;
        }

        resource function get nullableReadonlyFunctionReturnPath() returns
                (function (int a, string b) returns int|string)? & readonly {
            return testFunction;
        }
    };
}

public function testErrorInResourceMethodReturningCustomObjectType() {
    var successClientObject = client object {
        resource function get clientReturnPath() returns CustomType1 {
            return successClient;
        }

        resource function get clientUnionFunctionReturnPath()
                returns int|CustomType1|CustomType2 {
            return testFunction;
        }

        resource function get readonlyClientReturnPath() returns CustomType1 & readonly {
            return successClient;
        }
    };
}

public function testErrorInResourceMethodReturningCustomFunctionType() {
    var successClientObject = client object {
        resource function get functionReturnPath() returns CustomType2 {
            return testFunction;
        }

        resource function get readonlyFunctionReturnPath() returns readonly & CustomType2 {
            return testFunction;
        }

        resource function get nullableFunctionReturnPath() returns CustomType2? {
            return testFunction;
        }

        resource function get nullableReadonlyFunctionReturnPath()
                returns CustomType2? & readonly {
            return testFunction;
        }

        resource function get functionAndClientObjectUnionReturnPath() returns CustomType1|CustomType2 {
            return testFunction;
        }

        resource function get functionAndClientObjectUnionReturnPath2() returns CustomType3 {
            return testFunction;
        }
    };
}

public function testErrorInResourceMethodReturningNestedCustomTypes() {
    var successClientObject = client object {
        resource function get functionUnionClientObjectCustomTypeReturnPath() returns CustomType7 {
            return successClient;
        }

        resource function get functionUnionClientObjectCustomTypeReturnPath2() returns CustomType10 {
            return successClient;
        }
    };
}
