//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//Test module level list binding pattern
[int ,float] [a, b] = [1, 2.5];
[boolean, float, string] [c, d, e] = [true, 2.25, "Jhone"];
public function testBasic() {
    while (d < 3) {
        d += 1;
    }
    assertTrue(c);
    assertEquality(3.25, d);
    assertEquality("Jhone", e);
}

//Recursive list binding pattern with objects
Foo foo = {name:"Test", age:23};
Bar bar = {id:34, flag:true};
FooObj fooObj = new ("Fooo", 3.7, 23);
BarObj barObj = new (true, 56);
[string, [Foo, [BarObj, FooObj]], [Bar, int]] [a2, [b2, [c2, d2]], [e2, f2]] = [foo.name, [foo, [barObj, fooObj]], [bar, barObj.i]];

function testTupleBindingWithRecordsAndObjects() {
    assertEquality("Test", a2);
    assertEquality("Test", b2.name);
    assertEquality(23, b2.age);
    assertEquality(34, e2.id);
    assertTrue(e2.flag);
    assertEquality("Fooo", d2.s);
    assertEquality(3.7, d2.f);
    assertEquality(23, d2.b);
    assertTrue(c2.b);
    assertEquality(56, c2.i);
    assertEquality(56, f2);
}

type Foo record {
    string name;
    int age;
};

type Bar record {
    int id;
    boolean flag;
};

class FooObj {
    public string s;
    public float f;
    public byte b;
    public function init(string s, float f, byte b) {
        self.s = s;
        self.f = f;
        self.b = b;
    }
}

class BarObj {
    public boolean b;
    public int i;
    public function init(boolean b, int i) {
        self.b = b;
        self.i = i;
    }
}

const annotation annot on source var;

@annot
[int, int] [j, k] = [1, 2];


type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

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

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
