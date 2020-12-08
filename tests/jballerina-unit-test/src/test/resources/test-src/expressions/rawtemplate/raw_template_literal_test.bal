// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'object as ob;

function testBasicUsage() {
    string name = "Pubudu";
    ob:RawTemplate rt = `Hello ${name}!`;

    assert(<string[]>["Hello ", "!"], rt.strings);
    assert("Pubudu", <string>rt.insertions[0]);
}

function testEmptyLiteral() {
    ob:RawTemplate rt = ``;

    assert(<string[]>[], rt.strings);
    assert(0, rt.insertions.length());
}

function testLiteralWithNoInterpolations() {
    ob:RawTemplate rt = `Hello World!`;

    assert(<string[]>["Hello World!"], rt.strings);
    assert(0, rt.insertions.length());
}

function testLiteralWithNoStrings() {
    string hello = "Hello";
    int n = 10;
    string world = "World";

    ob:RawTemplate rt = `${hello}${n}${world}`;

    assert(<string[]>["", "", "", ""], rt.strings);
    assert("Hello", <string>rt.insertions[0]);
    assert(10, <int>rt.insertions[1]);
    assert("World", <string>rt.insertions[2]);
}

class Person {
    string name;
    int age;

    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    function toString() returns string {
        return string `name: ${self.name}, age: ${self.age}`;
    }
}

function testComplexExpressions() {
    int x = 10;
    int y = 20;

    ob:RawTemplate rt1 = `x + y = ${x + y}`;
    assert(<string[]>["x + y = ", ""], rt1.strings);
    assert(30, <int>rt1.insertions[0]);

    var fn = function () returns string { return "Pubudu"; };

    ob:RawTemplate rt2 = `Hello ${fn()}!`;
    assert(<string[]>["Hello ", "!"], rt2.strings);
    assert("Pubudu", <string>rt2.insertions[0]);

    Person p = new("John Doe", 20);

    ob:RawTemplate rt3 = `${p} is a person`;
    assert(<string[]>["", " is a person"], rt3.strings);
    assert("name: John Doe, age: 20", rt3.insertions[0].toString());
}

type Template1 object {
    public (readonly & string[]) strings;
    public int[] insertions;
};

function testSubtyping1() {
    int x = 10;
    int y = 20;

    Template1 t = `${x} + ${y} = ${x + y}`;
    assert(<string[]>["", " + ", " = ", ""], t.strings);
    assert(<int[]>[10, 20, 30], t.insertions);
}

type Template2 object {
    public (readonly & string[]) strings;
    public [int, string, float] insertions;
};

function testSubtyping2() {
    int x = 25;
    string s = "foo";
    float f = 12.34;

    Template2 t = `Using tuples: ${x}, ${s}, ${f}`;
    assert(<string[]>["Using tuples: ", ", ", ", ", ""], t.strings);
    assert(x, t.insertions[0]);
    assert(s, t.insertions[1]);
    assert(f, t.insertions[2]);

    object {
        public (readonly & string[]) strings;
        public [int, string, anydata...] insertions;
    } temp2 = `Using tuples 2: ${x}, ${s}, ${f}`;

    assert(<string[]>["Using tuples 2: ", ", ", ", ", ""], temp2.strings);
    assert(x, temp2.insertions[0]);
    assert(s, temp2.insertions[1]);
    assert(f, temp2.insertions[2]);

    object {
        public (readonly & string[]) strings;
        public [anydata...] insertions;
    } temp3 = `Using tuples 3: ${x}, ${s}, ${f}`;

    assert(<string[]>["Using tuples 3: ", ", ", ", ", ""], temp3.strings);
    assert(x, temp2.insertions[0]);
    assert(s, temp3.insertions[1]);
    assert(f, temp3.insertions[2]);
}

const FOO = "Foo";
const BAR = "Bar";

type FooBar FOO|BAR;

function testSubtyping3() {
    int x = 10;

    object {
        public (readonly & FooBar[]) strings;
        public int[] insertions;
    } temp1 = `Foo${x}Bar`;

    assert(<string[]>["Foo", "Bar"], temp1.strings);
    assert(10, temp1.insertions[0]);
}

function testUsageWithQueryExpressions() {
    int[] arr = [100, 200, 300, 400, 500];

    ob:RawTemplate[] queries = from var x in arr
        select `INSERT INTO product ('id', 'name', 'price') VALUES (${x}, ${x + 10}, ${x + 20})`;

    foreach var x in queries {
        assert("[\"INSERT INTO product ('id', 'name', 'price') VALUES (\",\", \",\", \",\")\"]", x.strings.toString());
    }

    int i = 0;
    foreach var x in queries {
        assert(string `[${arr[i]},${arr[i] + 10},${arr[i] + 20}]`, x.insertions.toString());
        i += 1;
    }
}

public type Value ()|int|float|decimal|string|xml;

public type ParameterizedQuery object {
    public (readonly & string[]) strings;
    public Value[] insertions;
};

function testUsageWithQueryExpressions2() {
    var data = [
        {name: "Alice1"},
        {name: "Alice2"},
        {name: "Alice3"}
    ];

    ParameterizedQuery[] queries = from var rec in data select `INSERT INTO People (name) values (${rec.name});`;

    foreach var x in queries {
        assert("[\"INSERT INTO People (name) values (\",\");\"]", x.strings.toString());
    }

    int i = 0;
    foreach var x in queries {
        assert("[\"" + data[i]["name"]+ "\"]", x.insertions.toString());
        i += 1;
    }
}

function testUseWithVar() {
    string name = "Pubudu";
    var rt = `Hello ${name}!`;
    typedesc<any> td = typeof rt;

    assert("typedesc $rawTemplate$RawTemplate$_12", td.toString());
}

function testUseWithAny() {
    string name = "Pubudu";
    any rt = `Hello ${name}!`;
    typedesc<any> td = typeof rt;

    assert("typedesc $rawTemplate$RawTemplate$_13", td.toString());
}

public type Template3 object {
    public (string[2] & readonly) strings;
    public int[1] insertions;
};

function testFixedLengthArrayFields() {
    Template3 t = `Count:${1}`;

    assert(<string[]>["Count:", ""], t.strings);
    assert(<int[]>[1], t.insertions);
}

function testIndirectAssignmentToConcreteType() {
    Template1 rt = `Count: ${10}, ${20}`;

    object {
        public string[] strings;
        public anydata[] insertions;
    } rt2 = rt;

    rt2.insertions.push(30);

    assert(<string[]>["Count: ", ", ", ""], rt.strings);
    assert(<int[]>[10, 20, 30], rt.insertions);

    error err = <error>trap rt2.insertions.push(12.34);

    assert("{ballerina/lang.array}InherentTypeViolation", err.message());
    assert("incompatible types: expected 'int', found 'float'", <string>err.detail().get("message"));
}

class CompatibleObj {
   public string[] strings = [];
   public anydata[] insertions = [];
}

function testModifyStringsField() {
    Template1 rt = `Count: ${10}, ${20}`;
    CompatibleObj rt2 = rt;
    error err = <error>trap rt2.strings.push("Invalid");

    assert("{ballerina/lang.array}InvalidUpdate", err.message());
    assert("modification not allowed on readonly value", <string>err.detail().get("message"));
}

function testAnyInUnion() {
    any|error ae = `INSERT INTO Details VALUES (${"Foo"}, ${20})`;
    ob:RawTemplate rt = <ob:RawTemplate>ae;

    assert(<string[]>["INSERT INTO Details VALUES (", ", ", ")"], rt.strings);
    assert("Foo", <string>rt.insertions[0]);
    assert(20, <int>rt.insertions[1]);
}

function testAssignmentToUnion() {
    Template1|string rt = `Count: ${10}, ${20}`;
    Template1 t1 = <Template1>rt;

    assert(<string[]>["Count: ", ", ", ""], t1.strings);
    assert(<int[]>[10, 20], t1.insertions);
}

// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "AssertionError";
        string detail = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason, message = detail);
        panic e;
    }
}
