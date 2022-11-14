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

type Foo record {|
    Bar x;
|};

type Bar record {|
    *Foo;
|};

function testCyclicInclusionViaFieldWithTypeReferenceTypesNegative() {
    Foo f = {};
    f.x = 1;

    Foo _ = {x: 1};

    Foo _ = {x: {x: 1}};
}

public type ErrorRecord record {
    string code?;
    record {
        *ErrorRecord;
    } innerError;
};

function testCyclicInclusionViaFieldWithAnonymousTypesNegative() {
    ErrorRecord e = {code: "NotFound"};
    e.innerError = 1;

    var nilableInnerError = e.innerError;
    int _ = nilableInnerError;
}
