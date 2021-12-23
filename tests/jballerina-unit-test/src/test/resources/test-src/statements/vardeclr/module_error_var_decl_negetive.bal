//  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type UserDefinedError error <BasicErrorDetail>;
type BasicErrorDetail record {|
    int basicErrorNo;
    anydata...;
|};
string message1 = "my message one";
// Redeclare message1 inside error variable
UserDefinedError error(message1, basicErrorNo = detail1) = error UserDefinedError("error message one", basicErrorNo = 1);
// Redeclare detail1 which is already declared inside error var
boolean detail1 = false;

// Only simple variables are allowed to be isolated
isolated UserDefinedError error(message3, basicErrorNo = detail3) = error UserDefinedError("error message three", basicErrorNo = 3);

// Only simple variables are allowed to be configurable
configurable UserDefinedError2 error(message4) = error UserDefinedError2("error message four");

const annotation annot on source function;

@annot
UserDefinedError2 error (message5) = error UserDefinedError2("error message five");

UserDefinedError error(message7, basicErrorNo = errorNo2, ...otherDetails) = error UserDefinedError(
                                                                    message6, basicErrorNo = errorNo1);
var error(message6, basicErrorNo = errorNo1) = getError();
boolean n = false;

// Test incompatible types
type VarTestErrorDetail record {
    [int] fieldA;
    map<boolean> fieldB;
    error fieldC;
};

type VarTestError error<VarTestErrorDetail>;

var error(m, error(c), fieldA = {a: varA}, fieldD = {a: varB}) = foo();

function foo() returns VarTestError =>
        error VarTestError("message", error("cause"), fieldA = [3], fieldC = error("fieldC message"));

function getError() returns UserDefinedError {
    return error UserDefinedError("error message one", basicErrorNo = 1);
}

type UserDefinedError2 error <BasicErrorDetail2>;
type BasicErrorDetail2 record {|
    int basicErrorNo?;
    anydata...;
|};
