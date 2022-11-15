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

public type A A[];

public type B (B|string)[];
public type C (C|A)[];
public type D (D|B|int)[];
public type E (E|B|int|boolean[])[];
public type F (F|B[])[];

//public type G (readonly & G|string)[];
//public type H (readonly & H|readonly & A)[];
//public type I (readonly & I|readonly & G|int)[];
//public type J (readonly & J|readonly & G|int|boolean[])[];
//public type K (K|readonly & G[])[];

function testCyclicArrayTypeDefinitions() {
    A a = [];
    //A a2 = [a, [], a];
    //A a3 = [a, [], a, a2];

    assert(a is A, true);
    //assert(a2 is A, true);
    //assert(a3 is A, true);

    //assert(a3[0] is A, true);
    //assert(a3[2] is A, true);
    //assert(a3[3] is A, true);

    C z = [];
    assert(z is C, true);

    //H h = [];
    //assert(h is H, true);
}

function testCyclicArrayTypeDefinitions2() {
    B a = [];
    B a2 = ["AAA", "ABC"];

    assert(a is B, true);
    assert(a2 is B, true);
    assert(a2[0], "AAA");
    assert(a2[1], "ABC");

    D d = [];
    D d2 = [["AAA", "ABC"], ["AAA", "ABC"]];
    D d3 = [1, 2];
    D d4 = [1, ["AAA"]];

    assert(d is D, true);
    assert(d2 is D, true);
    assert(d3 is D, true);
    assert(d4 is D, true);

    assert(d2[0], ["AAA", "ABC"]);
    assert((<string[]> d2[0])[0], "AAA");
    assert(d3[0], 1);
    assert(d3[1], 2);
    assert(d4[0], 1);
    assert(d4[1], ["AAA"]);
    assert((<string[]> d4[1])[0], "AAA");

    E b = [];
    E b2 = [["AAA", "ABC"], ["AAA", "ABC"]];
    E b3 = [1, 2];
    E b4 = [1, ["AAA"]];
    E b5 = [[true, false], [true]];
    E b6 = [1, ["AAA", "BBB"], [true, true]];

    assert(b is E, true);
    assert(b2 is E, true);
    assert(b3 is E, true);
    assert(b4 is E, true);
    assert(b5 is E, true);
    assert(b6 is E, true);

    assert(b2[0], ["AAA", "ABC"]);
    assert((<string[]> b2[0])[0], "AAA");
    assert(b3[0], 1);
    assert(b3[1], 2);
    assert(b4[0], 1);
    assert(b4[1], ["AAA"]);
    assert((<string[]> b4[1])[0], "AAA");
    assert(b5[0], [true, false]);
    assert((<boolean[]> b5[0])[1], false);
    assert((<boolean[]> b6[2])[0], true);

    F c = [];
    F c2 = [["AAA", "ABC"], ["BBB"]];

    assert(c is F, true);
    assert(c2 is F, true);
    assert((<string[]> c2[0])[0], "AAA");
    assert((<string[]> c2[1])[0], "BBB");
}



// function testCyclicArrayTypeDefinitions3() {
//     G a = [];
//     G a2 = ["AAA", "ABC"];

//     assert(a is B, true);
//     assert(a2 is B, true);
//     assert(a2[0], "AAA");
//     assert(a2[1], "ABC");

//     I d = [];
//     I d2 = [["AAA", "ABC"], ["AAA", "ABC"]];
//     I d3 = [1, 2];
//     I d4 = [1, ["AAA"]];

//     assert(d is D, true);
//     assert(d2 is D, true);
//     assert(d3 is D, true);
//     assert(d4 is D, true);

//     assert(d2[0], ["AAA", "ABC"]);
//     assert((<string[]> d2[0])[0], "AAA");
//     assert(d3[0], 1);
//     assert(d3[1], 2);
//     assert(d4[0], 1);
//     assert(d4[1], ["AAA"]);
//     assert((<string[]> d4[1])[0], "AAA");

//     J b = [];
//     J b2 = [["AAA", "ABC"], ["AAA", "ABC"]];
//     J b3 = [1, 2];
//     J b4 = [1, ["AAA"]];
//     J b5 = [[true, false], [true]];
//     J b6 = [1, ["AAA", "BBB"], [true, true]];

//     assert(b is E, true);
//     assert(b2 is E, true);
//     assert(b3 is E, true);
//     assert(b4 is E, true);
//     assert(b5 is E, true);
//     assert(b6 is E, true);

//     assert(b2[0], ["AAA", "ABC"]);
//     assert((<string[]> b2[0])[0], "AAA");
//     assert(b3[0], 1);
//     assert(b3[1], 2);
//     assert(b4[0], 1);
//     assert(b4[1], ["AAA"]);
//     assert((<string[]> b4[1])[0], "AAA");
//     assert(b5[0], [true, false]);
//     assert((<boolean[]> b5[0])[1], false);
//     assert((<boolean[]> b6[2])[0], true);

//     K c = [];
//     K c2 = [["AAA", "ABC"], ["BBB"]];

//     assert(c is F, true);
//     assert(c2 is F, true);
//     assert((<string[]> c2[0])[0], "AAA");
//     assert((<string[]> c2[1])[0], "BBB");
// }

public type A2 map<A2>;
public type B2 map<B2|string>;
public type C2 map<C2|int[]>;
public type D2 map<D2|B2|C2>;

function testCyclicMapDefinitions() {
    A2 a = {};
    B2 a2 = {"a": "string1"};
    C2 a3 = {"a": [1, 2, 3]};
    D2 a4 = {"a": {"a": "string1"}, "c": {"a": [1, 2, 3]}};

    assert(a is A2, true);
    assert(a2 is B2, true);
    assert(a3 is C2, true);
    assert(a4 is D2, true);

    assert(a["a"] is (), true);
    assert(a2["a"], "string1");
    assert(a3["a"], [1, 2, 3]);
    assert(a4["a"], {"a": "string1"});
}

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
