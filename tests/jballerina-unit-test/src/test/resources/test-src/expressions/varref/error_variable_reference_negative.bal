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

type SMS error <record {| string message; error cause?; string detail; string...; |}>;

function testDuplicateBinding() {
    string? s;
    SMS err1 = error SMS("Error One", message = "Msg One", detail = "Detail Msg");
    error(s, message = s, detail = s) = err1;
}

public function testAssigningValuesToFinalVars() {
    BazError e = error("ErrReason", message = "error message", abc = 1, def = 2.0);
    final var error(r, message = message, abc = abc) = e;
    error(r, message = message, abc = abc) = e;

    final var error(r2, message = message2, ...rest) = e;
    // error(r2, message = message2, ...rest) = e;

    BarError e3 = error BarError("bar", message = "error message", code = 1);
    final var error BarError(r3, message = message3) = e3;
    error(_, message = message3) = e3;
}

type BarError error<record {string message; error cause?; int code;}>;

type BazError error<record {string message; int abc; anydata def?;}>;
