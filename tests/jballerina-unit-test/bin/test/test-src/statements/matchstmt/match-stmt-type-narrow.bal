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

function narrowTypeInCaptureBindingPattern1(int|string v) returns string {
    match v {
        var a if a is int => {
            return a.toString();
        }
        _ => {
            return v;
        }
    }
}

function testNarrowTypeInCaptureBindingPattern1() {
    assertEquals("2", narrowTypeInCaptureBindingPattern1(2));
    assertEquals("str", narrowTypeInCaptureBindingPattern1("str"));
}

function narrowTypeInCaptureBindingPattern2(int|string|boolean v) returns string {
    match v {
        var a if a is int => {
            return a.toString();
        }
        var a if a is boolean => {
            return a.toString();
        }
        _ => {
            return v;
        }
    }
}

function testNarrowTypeInCaptureBindingPattern2() {
    assertEquals("str", narrowTypeInCaptureBindingPattern2("str"));
}

function narrowTypeInListBindingPattern1([int|string, int] v) returns string {
    match v {
        [var a, var b] if a is int => {
            return "";
        }
        _ => {
            return v[0];
        }
    }
}

function testNarrowTypeInListBindingPattern1() {
    assertEquals("str", narrowTypeInListBindingPattern1(["str", 2]));
}

function narrowTypeInListBindingPattern2([int|string|boolean, int] v) returns string {
    match v {
        [var a, var b] if a is int => {
            return "int";
        }
        [var a, var b] if a is boolean => {
            return "boolean";
        }
        _ => {
            return v[0];
        }
    }
}

function testNarrowTypeInListBindingPattern2() {
    assertEquals("str", narrowTypeInListBindingPattern2(["str", 2]));
}

function narrowTypeInListBindingPattern3([int|string, int|string] v) returns string {
    match v {
        [var a, var b] if a is int && b is int=> {
            return "match1";
        }
        [var a, var b] if a is int && b is string=> {
            return "match2";
        }
        [var a, var b] if a is string && b is int=> {
            return "match3";
        }
        _ => {
            return v[0] + v[1];
        }
    }
}

function testNarrowTypeInListBindingPattern3() {
    assertEquals("Hello World", narrowTypeInListBindingPattern3(["Hello ", "World"]));
}

type Insn record {
    string op;
};

function getValidInsn(Insn insn) returns string? {
    match insn {
        var {op} if op is "+" => { return "+"; }
        var {op} if op is "-" => { return "-"; }
        var {op} if op is "%" => { return "%"; }
    }
    return ();
}

function testMatchClauseWithTypeGuard1() {
    assertEquals("+", getValidInsn({op: "+"}));
    assertEquals("-", getValidInsn({op: "-"}));
    assertEquals((), getValidInsn({op: "="}));
}

const ONE = 1;
const ONE2 = 1;

function testMatchClauseWithTypeGuard2() {
    int value = 2;
    string matched = "Not Matched";

    match value {
        var a if a is 2 => {
            matched = "Two";
        }
        var a if a is 1 => {
            matched = "One";
        }
    }
    assertEquals("Two", matched);
}

function testMatchClauseWithTypeGuard3() {
    int value = 2;
    string matched = "Not Matched";

    match value {
        var a if a is 1|2 => {
            matched = "One or Two";
        }
        var a if a is 3|4 => {
            matched = "Three or Four";
        }
    }
    assertEquals("One or Two", matched);
}

type A 3|4;
type B 1|2;
type BB 3;

function testMatchClauseWithTypeGuard4() {
    int value = 2;
    string matched = "Not Matched";

    match value {
        var a if a is A => {
            matched = "Three or Four";
        }
        var a if a is B => {
            matched = "One or Two";
        }
    }
    assertEquals("One or Two", matched);

    match value {
        var a if a is B => {
            matched = "One or Two";
        }
        var a if a is BB => {
            matched = "Three";
        }
    }
    assertEquals("One or Two", matched);
}

function testMatchClauseWithTypeGuard5() {
    int|float value = 2;
    string matched = "Not Matched";

    match value {
        var a if a is boolean|float => {
            matched = "Boolean or Float";
        }
        var a if a is int|string => {
            matched = "Int or String";
        }
    }
    assertEquals("Int or String", matched);

    match value {
        var a if a is int|string => {
            matched = "Int or String";
        }
        var a if a is boolean|int|float => {
            matched = "Boolean or Int or Float";
        }
    }
    assertEquals("Int or String", matched);
}

type C int|string;
type D boolean|float;

function testMatchClauseWithTypeGuard6() {
    int|float value = 2;
    string matched = "Not Matched";

    match value {
        var a if a is C => {
            matched = "Int or String";
        }
        var a if a is D => {
            matched = "Boolean or Float";
        }
    }
    assertEquals("Int or String", matched);
}

type Person record {|
    string name;
    int age;
    Address address;
|};

type Address record {
    string street;
    int houseNo;
    string city;
};

type AddressWithProvince record {
    string street;
    int houseNo;
    string city;
    string province;
};

type E 100|200;

function testMatchClauseWithTypeGuard7() {
    Person person = {name: "John", age: 12, address: {street: "Main Street", houseNo:10, city: "Colombo",
                    "country": "Sri Lanka"}};
    string matched = "Not Matched";
    int age = 0;
    string street = "";
    Person newPerson = person;

    match person {
        {name: var a, ...var b} if a is string && b is record {|int age; AddressWithProvince address;|} => {
            matched = "Pattern1";
            age = b.age;
            street = b.address.street;
        }
        {name: var a, ...var b} if a is string && b is record {|int age; Address address;|} => {
            matched = "Pattern2";
            age = b.age;
            street = b.address.street;
            match b {
                {age: var c, address: var d} if age is 10 && d.houseNo is E => {
                    matched += " Pattern3";
                    newPerson = {name: a, age: c, address: d};
                }
                {age: var c, address: var d} if age is 12 && d.houseNo is C => {
                    matched += " Pattern4";
                    string newName = a + " Smith";
                    newPerson = {name: newName, age: c + 10, address: d};
                }
            }
        }
    }
    assertEquals("Pattern2 Pattern4", matched);
    assertEquals(12, age);
    assertEquals("Main Street", street);
    assertEquals("Main Street", street);
    assertEquals("John Smith", newPerson.name);
    assertEquals(22, newPerson.age);
    assertEquals(person.address, newPerson.address);
}

function testMatchClauseWithTypeGuard8() {
    [int, string, float] t1 = [12, "ABC", 20.34];
    string matched = "Not Matched";

    match t1 {
        var [a, b, ...c] if a is 12 && b is "DEF" => {
            matched = "Pattern1";
        }
        [var a, var b, ...var c] if a is 12 && b is "ABC" => {
            matched = "Pattern2";
        }
    }
    assertEquals("Pattern2", matched);
}

function testMatchClauseWithTypeGuard9() {
    [int, string, [int...]] t1 = [12, "ABC", [10, 20, 30, 40]];
    string matched = "Not Matched";

    match t1 {
        var [a, b, c] if a is 12 && c[0] is 20 => {
            matched = "Pattern1";
        }
        [var a, var b, var c] if a is 12 && c[0] is 10 => {
            matched = "Pattern2";
        }
    }
    assertEquals("Pattern2", matched);

    matched = "Not Matched";
    match t1 {
        var [a, b, c] if c is [1, 2] => {
            matched = "Pattern1";
        }
        [var a, var b, var c] if c is [10, 20, 30] => {
            matched = "Pattern2";
        }
    }
    assertEquals("Not Matched", matched);

    match t1 {
        var [a, b, c] if c is [1, 2, 3] => {
            matched = "Pattern1";
        }
        [var a, var b, var c] if c is [10, 20, 30] => {
            matched = "Pattern2";
        }
    }
    assertEquals("Not Matched", matched);
}

function testMatchClauseWithTypeGuard10() {
    int[] t1 = [10, 20, 30, 40];
    string matched = "Not Matched";

    match t1 {
        var a if a[0] is 10 && a[1] is int|string && a[2] is A => {
            matched = "Pattern1";
        }
        var a if a[0] is 10 && a[1] is int|string && a[2] is 30|40 => {
            matched = "Pattern2";
        }
    }
    assertEquals("Pattern2", matched);
}

function testMatchClauseWithTypeGuard11() {
    [int, string[], map<string>] t1 = [1, ["A", "B", "C", "D"], {name: "John"}];
    string matched = "Not Matched";
    match t1 {
        [var a, var b, var c] if a is ONE && b is string[] && c is map<string> => {
            matched = "Pattern1";
            match b {
                var [...d] if d[2] is "A"|"B" => {
                    matched += " Pattern2";
                }
                var [...d] if d[2] is "C"|"D" => {
                    matched += " Pattern3";
                }
            }
            match c {
                {name: var e} if e is "Anne"|"Mike" => {
                    matched += " Pattern4";
                }
                {name: var e} if e is "Frank"|"John" => {
                    matched += " Pattern5";
                }
            }
        }
    }
    assertEquals("Pattern1 Pattern3 Pattern5", matched);
}

function testMatchClauseWithTypeGuard12() {
    error err = error("Transaction Error", error("Internal Error", cause= {reason: "Connection failure", level: 3},
                retryCount= 5), flag=true, commitCount= 2, reportTo= "John");
    string matched = "Not Matched";

    match err {
        var error(a, b, flag= c, ...d) if a is "Internal Error" || a is "Transaction Error" => {
            matched = "Pattern1";
            match b {
                error("Internal Error", cause= var e, retryCount= var f) if e is map<string> && f is float => {
                    matched += " Pattern3";
                }
                error("Internal Error", cause= var e, retryCount= var f) if e is map<anydata> && f is 5 => {
                    matched += " Pattern4 with error retry count:" + f.toString();
                    match e {
                        {reason: var g, ...var h} if g is "Connection failure" || g is "Database failure" &&
                                             h is record {int level;} => {
                            matched += " Pattern5 with reason:" + g;
                        }
                    }
                }
            }
        }
        var error(a, b, flag= c, ...d) if a is "Transaction Error" => {
            matched = "Pattern2";
        }
    }
    assertEquals("Pattern1 Pattern4 with error retry count:5 Pattern5 with reason:Connection failure", matched);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
