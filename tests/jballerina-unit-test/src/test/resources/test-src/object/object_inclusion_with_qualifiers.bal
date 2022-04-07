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

class ObjectOne {
    int i = 1;
}

class ObjectTwo {
    int j = 1;
}

class ObjectThree {
    *ObjectOne;
    *ObjectTwo;
    int i = 2;
    int j = 3;
}

type ObjectFour object {
    *ObjectOne;
    *ObjectTwo;
};

ObjectThree obThree = object ObjectOne {
    int i = 20;
    int j = 30;
};

function testReadOnlyClassInclusion() {
    var ob = object ObjectTwo {
        int j = 40;
    };
}

isolated class Foo {
    final isolated object{} i = object {};
}

client class Bar {
    int j = 2;

    remote function bar() {

    }
}

type Baz service object {
    int k;
};

isolated service class Qux {
    final isolated object{} l = object {};
}

isolated client class Quux {
    *Foo;
    *Bar;

    final isolated object{} i = object {};
    final int j = 20;
    final int k = 30;
    final isolated object{} l = object {};

    remote function bar() {

    }
}

type Quuz isolated service object {
    *Foo;
    *Baz;
    *Qux;
};

Foo foo = isolated object Foo {
    final isolated object{} i = object {};
};

object {} bar = isolated client object Bar {
    final int j = 2;

    remote function bar() {

    }
};

function testInclusion() {
    var ob = isolated service object Qux {
        final isolated object{} l = object {};
    };
}

readonly class Corge {
    int x;
    int y = 100;

    function init(int x) {
        self.x = x;
    }

    function getSum() returns int => 0;
}

function testObjectConstructorWithReadOnlyReference() {
    object {} ob = object Corge {
        int x;
        int y;
        int z = 202;

        function init() {
            self.x = 200;
            self.y = 201;
        }

        function getSum() returns int => self.x + self.y + self.z;
    };

    any val = ob;
    assertTrue(val is readonly & object {int x; int y; int z; function getSum() returns int;});

    var obVal = <readonly & object {int x; int y; int z; function getSum() returns int; }> val;
    assertEquality(200, obVal.x);
    assertEquality(201, obVal.y);
    assertEquality(202, obVal.z);
    assertEquality(603, obVal.getSum());
}

public type Timestamp readonly & object {
    int value;

    public function toMillisecondsInt() returns int;
    public function toString() returns string;
};

readonly class TimestampImpl  {
    *Timestamp;

    function init(decimal value) {
        self.value = <int> value;
    }

    public function toMillisecondsInt() returns int => self.value;

    public function toString() returns string => "test";
}

class Function {
    final string name = "";
}

class FunctionWithSingleCharName {
    *Function; // OK, since `Function` isn't readonly even though it has only immutable fields.
    final string:Char name;

    function init(string:Char name) {
        self.name = name;
    }
}

class FunctionDefn {
    *Function;
    string[] body;

    function init(string name, string[] body) {
        self.name = name;
        self.body = body;
    }
}

type PublicFunction object {
    *Function;
    string module;
};

function testReadOnlyAndObjectIntersectionInclusion() {
    Timestamp tp = new TimestampImpl(12345.2);
    assertTrue(<any> tp is TimestampImpl);
    assertEquality(12345, tp.value);
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
