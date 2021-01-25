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

type userDefinedError error <basicErrorDetail>;
type basicErrorDetail record {|
    int basicErrorNo?;
    anydata...;
|};
string message1 = "my message one";
// Redeclare message1 inside error variable
userDefinedError error(message1, basicErrorNo = detail1) = error userDefinedError("error message one", basicErrorNo = 1);
// Redeclare detail1 which is already declared inside error var
boolean detail1 = false;

// Only simple variables are allowed to be isolated
isolated userDefinedError error(message3, basicErrorNo = detail3) = error userDefinedError("error message three", basicErrorNo = 3);

// Only simple variables are allowed to be configurable
configurable userDefinedError error(message4) = error userDefinedError("error message four");

const annotation annot on source function;

@annot
userDefinedError error (message5) = error userDefinedError("error message five");

userDefinedError error(message7, basicErrorNo = errorNo2, ...otherDetails) = error userDefinedError(
                                                                    message6, basicErrorNo = errorNo1, recoverable = n);
var error(message6, basicErrorNo = errorNo1) = getError();
boolean n = false;

function getError() returns userDefinedError {
    return error userDefinedError("error message one", basicErrorNo = 1);
}
