// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type SMS error <string, map<string>>;
type SMA error <string, map<any>>;
type CMS error <string, map<string>>;
type CMA error <string, map<any>>;

const ERROR1 = "Some Error One";
const ERROR2 = "Some Error Two";

function testBasicErrorVariableWithMapDetails() {
    SMS err1 = error ("Error One", {message: "Msg One", detail: "Detail Msg"});
    SMA err2 = error ("Error Two", {message: "Msg Two", fatal: true});

    SMS error (reason11, detail11) = err1;
    SMS error (reason12, {message: message12, detail: detail12, extra: extra12}) = err1;

    SMA error (reason11, detail21) = err2; // redeclared symbol
    SMS error (reason22, {message: message22, detail: detail22, extra: extra22}) = err2; // incompatible types: expected 'error', found 'error'

    boolean reasonTest = reason12; // incompatible types: expected 'boolean', found 'string'
    string detailMessage = detail12; // incompatible types: expected 'string', found 'string?'
}
