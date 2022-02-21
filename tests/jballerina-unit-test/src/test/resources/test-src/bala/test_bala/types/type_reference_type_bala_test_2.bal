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

import testorg/typereftypes2 as tr;

type StringDefn tr:ConstPointerValue;

type Module record {|
    map<StringDefn> stringDefns = {};
|};

type FooFunctionDecl tr:FunctionDecl;

type Object1 object {
    public int a;
    *tr:Object2;
};

function testFn() {
    Module m = {stringDefns: {a: new (1234)}};
    assertEquality(1, m.stringDefns.length());
    assertEquality(1234, m.stringDefns.get("a").i);

    tr:FooFunction fn1 = new ("llvmFunction1");
    assertEquality("llvmFunction1", fn1.getFuncName());

    tr:FunctionDecl fn2 = new ("llvmFunction2");
    assertEquality("llvmFunction2", fn2.getFuncName());

    FooFunctionDecl fn3 = new ("llvmFunction3");
    assertEquality("llvmFunction3", fn3.getFuncName());

    Object1 obj = object {
        public int a = 1;
        public int b = 2;
        public int c = 3;
    };
    assertEquality(1, obj.a);
    assertEquality(2, obj.b);
    assertEquality(3, obj.c);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
