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

final Foo f1 = {i: 100};
final Foo f2 = {i: 200};

public readonly class Bar {
    Foo[] arr = [f1, f2];
}

function func() returns Bar[]|error {
    Bar[] s = [];
    s.push(check getBar());
    return s;
}

function getBar() returns Bar|error => new;

function testTypeParamResolutionWithExpression() {
    Bar[] arr = checkpanic func();
    assertEquality(1, arr.length());

    Bar bar = arr[0];
    Foo[] fooArr = bar.arr;
    assertEquality(2, fooArr.length());
    assertEquality(<Foo[]> [f1, f2], fooArr);
}

function assertEquality(any expected, any actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
