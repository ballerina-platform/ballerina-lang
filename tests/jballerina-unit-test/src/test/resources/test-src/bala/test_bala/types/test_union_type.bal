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

import testorg/foo;

function testUnionPositive() {
    foo:IntOrString a = 1;
    assertEquals(1, a);

    foo:FooBar b = foo:FOO;
    assertEquals("foo", b);

    foo:BazQux c = foo:BAZ;
    assertEquals("BAZ", c);
}

function testUnionNegative() {
    any a = true;
    assertFalse(a is foo:IntOrString);
    assertFalse(a is foo:FooBar);
    assertFalse(a is foo:BazQux);

    any b = "foo";
    assertTrue(b is foo:IntOrString);
    assertTrue(b is foo:FooBar);
    assertFalse(b is foo:BazQux);

    any c = "BAZ";
    assertTrue(c is foo:IntOrString);
    assertFalse(c is foo:FooBar);
    assertTrue(c is foo:BazQux);
}

function testUnionRuntimeToString() {
    any a = [1, 2];

    foo:IntOrString|error b = trap <foo:IntOrString> a;
    assertTrue(b is error);
    error err = <error> b;
    assertEquals("{ballerina}TypeCastError", err.message());
    assertEquals("incompatible types: '(any|error)[]' cannot be cast to 'testorg/foo:1:IntOrString'",
                 <string> checkpanic err.detail()["message"]);

    foo:FooBar|error c = trap <foo:FooBar> a;
    assertTrue(c is error);
    err = <error> c;
    assertEquals("{ballerina}TypeCastError", err.message());
    assertEquals("incompatible types: '(any|error)[]' cannot be cast to 'testorg/foo:1:FooBar'",
                 <string> checkpanic err.detail()["message"]);

    foo:BazQux|error d = trap <foo:BazQux> a;
    assertTrue(d is error);
    err = <error> d;
    assertEquals("{ballerina}TypeCastError", err.message());
    assertEquals("incompatible types: '(any|error)[]' cannot be cast to 'testorg/foo:1:BazQux'",
                 <string> checkpanic err.detail()["message"]);
}

function testTernaryWithQueryForModuleImportedVariable() {
    int|int[] thenResult = foo:IntOrNull is int ?
        from var _ in [1, 2]
        where foo:IntOrNull + 2 == 5
        select 2 : 2;
    assertEquals([2,2], thenResult);

    int|int[] elseResult = foo:IntOrNull is () ? 2 :
        from var _ in [1, 2]
        where foo:IntOrNull + 2 == 5
        select 2;
    assertEquals([2,2], elseResult);
}

function assertTrue(anydata actual) {
    return assertEquals(true, actual);
}

function assertFalse(anydata actual) {
    return assertEquals(false, actual);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected [${expected.toString()}], found [${actual.toString()}]`);
}
