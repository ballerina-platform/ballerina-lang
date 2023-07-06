 // Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 //
 // WSO2 Inc. licenses this file to you under the Apache License,
 // Version 2.0 (the "License"); you may not use this file except
 // in compliance with the License.
 // You may obtain a copy of the License at
 //
 //   http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing,
 // software distributed under the License is distributed on an
 // "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 // KIND, either express or implied.  See the License for the
 // specific language governing permissions and limitations
 // under the License.
 
import ballerina/lang.test;

 
 function testDataflow_1() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            msg = "2";
        } else {
            msg = "3";
        }
    } else if (true) {
        msg = "4";
    } else {
        msg = "5";
    }

    return msg;
}

function testDataflow_2() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            // do nothing
        } else {
            msg = "2";
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_3() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            msg = "2";
        } else {
            // do nothing
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_4() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        }
    } else {
        msg = "2";
    }
    
    return msg;
}

function testDataflow_5() returns string {
    string msg;
    if (true) {
        if (true) {
            //do nothing
        } else {
            msg = "1";
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_6() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            msg = "1";
        }
    } else {
        // do nothing
    }

    return msg;
}

function testDataflow_7() returns string {
    string msg;
    if (true) {
        msg = "1";
        if (true) {
        } else {
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_8() returns string {
    string msg;
    if (true) {
        if (true) {
            // do nothing
        } else {
            // do nothing
        }
        msg = "1";
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_9() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            msg = "2.1.1.1.1";
                        } else if (true) {
                            msg = "2.1.1.1.2";
                        } else {
                            msg = "2.1.1.1.3";
                        }
                    } else {
                        msg = "2.1.1.2";
                    }
                } else {
                    msg = "2.1.2";
                }
            } else {
                msg = "2.2";
            }
        }
    } else {
        msg = "3";
    }

    return msg;
}

function testDataflow_10() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            msg = "2.1.1.1.1";
                        } else if (true) {
                            // do nothing
                        } else {
                            msg = "2.1.1.1.3";
                        }
                    } else {
                        msg = "2.1.1.2";
                    }
                } else {
                    msg = "2.1.2";
                }
            } else {
                msg = "2.2";
            }
        }
    } else {
        msg = "3";
    }

    return msg;
}

function testUninitializedVarReferrencing() {
    map<any> m;
    int a;
    string s;
    error e = error(s);

    // assigning uninitialized var
    int b = a;

    // increment uninitialized var
    a = a + 1;

    // panic uninitialized var
    if (false) {
        panic getError(s);
    }

    // uninitialized var in:
    //    - 'if' condition
    //    - braced expr
    //    - binary expr
    if (a > 4) {
        // do nothing
    }

    // uninitialized var foreach
    foreach var val in m {
        m["msg"] = "hello";
    }

    // uninitialized var in while
    while (4 > a) {
        a = a + 1;
    }

    // uninitialized var in conversion
    string|error str = a.cloneWithType(string);

    // uninitialized var XML
    xml x1 = xml`<foo id="{{a}}" xmlns:ns0="{{a}}">
                    <bar> hello {{a}}</bar>
                    <!-- refering to uninitialized {{a}} -->
                    <?target content {{a}}?>
                </foo>`;

    // uninitialized var in string template
    string text = string `hello ${a}`;

    // uninitialized var index/field based access
    _ = m[s];

    // uninitialized var in function invocation, expression statement
    _ = m.hasKey(s);
    foo(a, s, s);

    // uninitialized var in range expression
    //
    var range = a...a+5;
}

function foo(int a, string str = "hello", string... args) {
    // do nothing
}

function getError(string msg) returns error {
    return error(msg);
}

function testDataflow_11() returns string {
    if (true) {
        string msg;
        if (true) {
            msg = "1";
        } else {
            return msg;
        }
        return msg;
    } else {
        return "n/a";
    }
}

int globalVar = 4;

class Foo {
    int a = globalVar;
    int b;
    int c;
    int d;
    int e;

    function init (int c, int f, int x, int e=4) {
        self.a = globalVar;
        self.b = e;
        self.c = c;
        self.e = e;
        self.f = f;
    }

    function getA() returns int {
        return self.a;
    }

    function getB() returns int {
        self.d = 46;
        return self.b;
    }

    function getC() returns int {
        return self.c;
    }

    function getD() returns int {
        return self.d;
    }
}

function testIfFollowedByIf() returns int {
    int x;
    if (true) {
    } else if (true) {
    } else {
    }

    if (true) {
        x = 1;
    } else if (true) {
        x = 2;
    } else {
        x = 3;
    }

    return x;
}

function testMatch_1() returns string {
    any x = 6;
    string val;
    match x {
        6 => {val = "int";}
        "" => {val = "string";}
        var y => {val = "any";}
    }

    return val;
}

listener test:MockListener echoEP = new(9090);

string x = "x";
string y = "sample value";

service "echo" on echoEP {

    resource function get echo_1(string conn, string request) {
        string a = x;
        a = y;
        x = "init within echo_1";
    }

    resource function get echo_2(string conn, string request) {
        string a = x;
        a = y;
        x = "init within echo_2";
    }
}

function testCompoundAssignment() {
    int a;
    a += 2;
}


function testUninitVsPartiallyInit() returns [string, string] {
    string a;
    string b;
    if (true) {
        if (true) {
            // do nothing
        } else if (true) {
            b = "something";
        } else {
            // do nothing
        }
    } else {
        // do nothing
    }

    return [a, b];
}

class A {
    public int a;
    private int b;
    int c;

    function init() {
        self.a = 1;
        self.b = 2;
        self.c = 3;
    }
}

class B {
    public int a;
    private int b;
    int c;

    function init(int a, int b, int c) {
        self.a = a;
        self.b = b;
        self.c = c;
    }
}

class C {
    public int a;
    private int b;
    int c;

    function init() {
    }
}

public type D record {
    string a;
    int b;
    int c;
};


listener test:MockListener testEP = new(9092);

int a = 0;

service "testService" on testEP {

    resource function get resource_1(string conn, string request) {
        a = 5;
        int b = a;
        int c;

        if (true) {
            return;
        } else {
            c = 3;
        }

        int d = c;
    }

    resource function get resource_2(string conn, string request) {
        int b = a;
    }
}

function testDataflowWithPanic_1() returns string {
    error e = error("some error");
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            panic e;
        }
    } else {
        msg = "n/a";
    }

    return msg;
}

function testDataflowWithNestedPanic_1() returns string {
    error e = error("some error");
    string msg;
    if (true) {
        panic e;
        if (true) {
            msg = "1";
        } else {
            panic e;
        }
    } else {
        // do nothing
    }

    return msg;
}

function testDataflowWithNestedPanic_2() returns string {
    error e = error("some error");
    string msg;
    if (true) {
        panic e;
        if (true) {
            // do nothing
        } else {
            panic e;
        }
    } else {
        msg = "1";
    }

    return msg;
}

class E {
    public int a;
    private int b;
    int c;
}

type Age record {
    int age;
    string format;
};

type Person record {|
    string name;
    boolean married;
    Age age;
    [string, int] extra;
|};

function testVariableAssignment() returns [string, boolean, int, string] {
    string fName;
    boolean married;
    int theAge;
    string format;
    map<any|error> theMap;

    {name: fName, married, age: {age: theAge, format}, ...theMap} = getPerson();
    return [fName, married, theAge, format];
}

function getPerson() returns Person {
    return {name: "Peter", married: true, age: {age:12, format: "Y"}};
}

function testDataflow_12() returns string {
    any x = 6;
    string val;
    if (x is int) {
        val = "int";
    } else if (x is string) {
        val = "string";
    } else {
        if (x is int) {
            val = "int";
        } else if (x is string) {
            val = "";
        } else {
            val = "any";
        }
    }

    return val;
}

class F {
    public int a;
    public int b;
    string c;

    function init() {
        self.a = 1;
    }
}

public function testDataFlow_13(){
    var o = object { public string s; };
}

function testMatch2() returns int {
    any v = 1;
    int k;
    match v {
        1 => {k = 1;}
        2 => {k = 2;}
    }
    return k; // variable 'k' may not have been initialized
}

function testMatch3() returns int {
    any v = 1;
    int k;
    match v {
        1 => {k = 1;}
        2 => {k = 2;}
        _ => {k = 0;}
    }
    return k;
}

function testMatch4() returns int {
    any v = 1;
    int k;
    match v {
        1 => {k = 1;}
        2 => {}
        _ => {k = 0;}
    }
    return k; // variable 'k' may not have been initialized
}

function testMatch5() returns int {
    any v = 1;
    int k;
    match v {
        var [a, b] => {k = 1;}
        var {a, b} => {k = 2;}
    }
    return k; // variable 'k' may not have been initialized
}

function testMatch6() returns int {
    any v = 1;
    int k;
    match v {
        var [a, b] => {k = 1;}
        var {a, b} => {k = 2;}
        var x => {k = 0;}
    }
    return k;
}

function testMatch7() returns int {
    any v = 1;
    int k;
    match v {
        var [a, b] => {k = 1;}
        var {a, b} => {}
        var x => {k = 0;}
    }
    return k; // variable 'k' may not have been initialized
}

function testMatch8() returns int {
    any | error v = 1;
    int k;
    match v {
        1 => {
            k = 1;
        }
        2 => {
            k = 2;
        }
        3 => {
            k = 3;
        }
        4 => {
            k = 4;
        }
        _ => {
            k = 0;
        }
    }
    return k; // variable 'k' may not have been initialized
}
