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

type SMS error <record {| string message?; error cause?; string...; |}>;
type SMA error <record {| string message?; error cause?; anydata...; |}>;

function testBasicErrorVariableWithMapDetails() {
    SMS err1 = error SMS("Error One", message = "Msg One", detail = "Detail Msg");
    SMA err2 = error SMA("Error Two", message = "Msg Two", fatal = true);

    SMS error (reason11, ... detail11) = err1;
    SMS error (reason12) = err1;

    SMA error (reason11, ... detail21) = err2; // redeclared symbol
    SMS error (reason22, detail = detail22, extra = extra22) = err2; // incompatible types: expected 'error', found 'error'

    boolean reasonTest = reason12; // incompatible types = expected 'boolean', found 'string'

}

function testBasicErrorVariable() {
    SMS err1 = error SMS("Error One", message = "Msg One", detail = "Detail Msg");
    SMA err2 = error SMA("Error Two", message = "Msg Two", fatal = true);

    var error (reason11, ... detail11) = err1;
    var error (reason12, detail = detail12, extra = extra12) = err1;

    var error (reason11, ... detail21) = err2; // redeclared symbol
    var error (reason22, detail = detail22, extra = extra22) = err2;

    boolean reasonTest = reason12; // incompatible types: expected 'boolean', found 'string'
    string detailMessage = detail12; // incompatible types: expected 'string', found 'string?'
}

function testBasicErrorVariable2() {
    error err1 = error("Error One", message = "Msg One", detail = "Detail Msg");

    var error (reason11, ... detail11) = err1;
    var error (reason12, message = message12, detail = detail12, extra = extra12) = err1;
    var error (reason13, message = message, ...rest) = err1;
    var error (reason14, message = message, ...rest2) = err1;

    int detail = detail11; // incompatible types: expected 'int', found 'map<anydata|error>'
    var error (reason11, detail = detail11) = 12; // invalid error variable; expecting an error type but found 'int' in type definition
}

function errorVarInTupleVar() {
    [int, error] tuple = [100, error("Error Code")];
    var [a, error(reason, message = message)] = tuple;
    boolean r = reason; // incompatible types: expected 'boolean', found 'string'
    string m = message; // incompatible types: expected 'string', found 'Cloneable'
}

function errorVarWithConstrainedMap() {
    error err = error("Error Code", message = "Fatal");
    var error (reason, message = message) = err;
    string m = message; // incompatible types: expected 'string', found 'Cloneable'
}
