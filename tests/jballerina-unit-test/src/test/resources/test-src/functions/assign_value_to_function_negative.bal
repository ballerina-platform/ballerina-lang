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

function foo() returns string => "Foo";
function bar() returns string => "Bar";

function testAssignValueToFunctionIdentifierNegative() {
    foo = function() returns string => "Bar";
    foo = foo;
    [foo, bar] = [function() returns string => "Bar", bar];
}

type BarRec record {|
    function() returns string func;
    int id;
|};

function testAssignValueToFunctionIdentifierInMapBindingPatternNegative() {
    int n;
    BarRec rec = {func : function() returns string => "Bar", id : 1};
    {func : foo, id : n} = rec;
}

type TestError error<BarRec>;

function testAssignValueToFunctionIdentifierInErrorBindingPatternNegative() {
    string reason;
    int n;
    TestError testError = error("Error", func = function() returns string => "Bar", id = 1);
    error(reason, func = foo, id = n) = testError;
}
