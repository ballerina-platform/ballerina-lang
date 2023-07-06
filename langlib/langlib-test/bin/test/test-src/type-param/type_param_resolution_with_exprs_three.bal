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

type Foo readonly & record {| int i; |};

public readonly class Bar {
    Foo[] arr = [];
}

public class Baz {
    Foo[] arr = [];
}

function parse() returns error? {
    Bar[] s = [];
    s.push(check parseBar());
}

function parse2() returns error? {
    (Baz & readonly)[] s = [];
    s.push(check parseBaz());
}

function parseBar() returns Bar|error => new;

function parseBaz() returns (Baz & readonly)|error => error("error!");

function testTypeParamResolutionWithExpression() {
    assertEquality(0, (checkpanic parseBar()).arr.length());
    assertEquality(true, parse() is ());
    assertEquality(true, parse2() is error);
}

function assertEquality(any expected, any actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
