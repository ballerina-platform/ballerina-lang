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

public const COMMA = ",";

public type Separator COMMA|string;

public class Foo {
    Separator s;

    function init(Separator fs = ",") {
        self.s = fs;
    }
}

function testUnionTypeInInitParameter() {
    Foo f = new;
    assertEquality(f.s, COMMA);
}

int a = 3;

public class Bar {
    int a = 4;
    int b = a;

    function init() {
    }
}

function testModuleLevelVariableAsFieldDefault() {
    Bar b = new();
    a = 6;
    assertEquality(b.b, 3);
    Bar b2 = new;
    assertEquality(b2.b, 6);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata actual, anydata expected) {
    if (actual == expected) {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                 message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
