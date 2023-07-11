// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import bir/objs;

objs:Qux quxMod = isolated service object {
    public isolated function execute() returns anydata|error {
         return "qux";
    }
};

function f1(objs:Foo foo) {
    assertTrue(foo is objs:Foo);
}

function f2(objs:Xyz xyz) {
    assertTrue(xyz is objs:Xyz);
}

function f3(any foo) {
    assertTrue(foo is objs:Foo);
}

function f4(any baz) {
    assertTrue(baz is objs:Baz);
}

function f5(any xyz) {
    assertTrue(xyz is objs:Xyz);
}

function f6(any qux) {
    assertTrue(qux is objs:Qux);
}

function f7(any foo) {
    assertFalse(foo is objs:Bar);
}

function testObjectTypeAssignability() {
    objs:Foo foo = isolated client object {
         public isolated function execute() returns anydata|error {
           return "foo";
        }
    };
    f1(foo);
    f3(foo);
    f7(foo);
    
    objs:Baz baz = isolated client object {
        public isolated function execute() returns anydata|error {
            return "baz";
        }
    };
    f4(baz);
    
    objs:Xyz xyz = isolated service object {
        public isolated function execute() returns anydata|error {
            return "xyz";
        }
    };
    f2(xyz);
    f5(xyz);
    
    objs:Qux qux = isolated service object {
        public isolated function execute() returns anydata|error {
            return "qux";
        }
    };
    f6(qux);
    f6(quxMod);
}

function assertTrue(anydata actual) {
    assertEquality(actual, true);
}

function assertFalse(anydata actual) {
    assertEquality(actual, false);
}

function assertEquality(anydata actual, anydata expected) {
    if expected == actual {
        return;
    }

    string expectedValAsString = expected.toString();
    string actualValAsString = actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
