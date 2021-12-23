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
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        if (b is int) {
            msg = "1";
        } else if (b is string) {
            msg = "2";
        } else {
            msg = "3";
        }
    } else if (a is string) {
        msg = "4";
    } else {
        msg = "5";
    }

    return msg;
}

function testDataflow_2() returns string {
    string msg;
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        if (b is int) {
            msg = "1";
        } else if (b is string) {
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
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        if (b is int) {
            msg = "1";
        } else if (b is string) {
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
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        if (b is int) {
            msg = "1";
        }
    } else {
        msg = "2";
    }
    
    return msg;
}

function testDataflow_5() returns string {
    string msg;
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        if (b is int) {
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
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        if (b is int) {
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
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        msg = "1";
        if (b is int) {
        } else {
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_8() returns string {
    string msg;
    int|string? a = 10;
    if (a is int) {
        int|string? b = 10;
        if (b is int) {
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
    int|string? a = 10;
    if (a is int) {
        int|string|boolean|float? b = 10;
        if (b is int) {
            msg = "1";
        } else {
            if (b is string|boolean|float) {
                if (b is string|boolean) {
                    if (b is string) {
                        int|string? c = 10;
                        if (c is string) {
                            msg = "2.1.1.1.1";
                        } else if (c is int) {
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
    int|string? a = 10;
    if (a is string) {
        int|string|boolean|float? b = 10;
        if (b is int) {
            msg = "1";
        } else {
            if (b is string|boolean|float) {
                if (b is string|boolean) {
                    if (b is string) {
                        int|string? c = 10;
                        if (c is int) {
                            msg = "2.1.1.1.1";
                        } else if (c is string) {
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
    int _ = a;

    // increment uninitialized var
    a = a + 1;

    int? x = 10;
    // panic uninitialized var
    if (x is int) {
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
    xml _ = xml`<foo id="{{a}}" xmlns:ns0="{{a}}">
                    <bar> hello {{a}}</bar>
                    <!-- refering to uninitialized {{a}} -->
                    <?target content {{a}}?>
                </foo>`;

    // uninitialized var in string template
    string _ = string `hello ${a}`;

    // uninitialized var index/field based access
    _ = m[s];

    // uninitialized var in function invocation, expression statement
    _ = m.hasKey(s);
    foo(a, s, s);

    // uninitialized var in range expression
    //
    var _ = a...a+5;
}

function foo(int a, string str = "hello", string... args) {
    // do nothing
}

function getError(string msg) returns error {
    return error(msg);
}

function testDataflow_11() returns string {
    int? a = 10;
    if (a is int) {
        int? b = 10;
        string msg;
        if (b is int) {
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
    int|string? a = 10;
    if (a is int) {
    } else if (a is string) {
    } else {
    }

    int|string? b = 10;
    if (b is int) {
        x = 1;
    } else if (b is string) {
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
    int? c = 10;
    if (c is int) {
        int|string? d = 10;
        if (d is int) {
            // do nothing
        } else if (d is string) {
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
        int _ = a;
        int c;
        int? x = 10;

        if (x is int) {
            return;
        } else {
            c = 3;
        }

        int _ = c;
    }

    resource function get resource_2(string conn, string request) {
        int _ = a;
    }
}

function testDataflowWithPanic_1() returns string {
    error e = error("some error");
    string msg;
    int|string? a = 10;
    if (a is int|string) {
        if (a is int) {
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
    int|string? a = 10;
    if (a is int|string) {
        panic e;
        if (a is int) {
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
    int|string? a = 10;
    if (a is int|string) {
        panic e;
        if (a is int) {
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
    return {name: "Peter", married: true, age: {age:12, format: "Y"}, extra: ["a", 1]};
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
    var _ = object { public string s; };
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

function testUninitializedVarWithContinueAndBreakInWhile() {
    int a;
    while false {
        if true {
            continue;
        } else {
            a = 1;
        }
        int _ = a; // OK
    }
    int _ = a; // variable 'a' is not initialized

    int b;
    while false {
        if true {
            break;
        } else {
            b = 1;
        }
        int _ = b; // OK
    }
    int k = b; // variable 'b' is not initialized
}

function testUninitializedVarWithWhile1() {
    int a;
    string b;
    boolean e = false;
    while e {
        if e {
            a = 1;
        } else {
            a = 5;
        }
        a = 10;
    }
    int j = a; // variable 'a' may not have been initialized
    string _ = b; // variable 'b' is not initialized
}

function testUninitializedVarWithWhile2() {
    int a;

    while true {
        break;
    }
    int j = a; // variable 'a' is not initialized
}

function testUninitializedVarWithLet() {
   int i;
   int _ = let int x = 4 + i in 2 * x * i;
}

function testUninitializedVariablesWithMemberAccess() {
    int i;
    int[] m = [];
    m[i] = 1; // variable 'i' is not initialized
    int _ = m[i]; // variable 'i' is not initialized

    int[] n;
    n[i] = 2; // variable 'n' is not initialized, variable 'i' is not initialized
    int _ = n[i]; // variable 'n' is not initialized, variable 'i' is not initialized

    int j = 0;
    m[j] = 3; // OK
    int _ = m[j]; // OK
}

function testUninitializedVariablesWithFunctionCalls() {
    function () f1;
    f1(); // uninitialized `f1`

    f1 = function () {};
    f1(); // OK

    function (int i, string j = "", boolean... k) f2;
    int i;
    string j;
    boolean[] k;
    f2(i, j, ...k); // uninitialized `f2`, `i`, `j`, `k`

    function (int x, string y = "", boolean... z) f3 = function (int m, string n = "", boolean... o) {};
    int i2 = 0;
    string j2 = "";
    boolean[] k2 = [true];
    f2(i2, j2, ...k2); // uninitialized `f2`
    f3(i2, j2, ...k2); // OK
    f3(i, j, ...k); // uninitialized `i`, `j`, `k`

    Bar b;
    b.fn(i); // uninitialized `b`, `i`
    b.method(); // uninitialized `b`

    i = 1;
    b = new(function (int v) {});
    b.fn(i); // OK
    b.method(); // OK
}

class Bar {
    function (int i) fn;

    function init(function (int i) fn) {
        self.fn = fn;
    }

    function method() {
    }
}

function testUninitializedVariablesInTheMatchGuard() {
    int[] & readonly arr = [];

    int i;
    anydata[] anydataArr;
    function () returns boolean fn2;

    int i2 = 0;
    anydata[] anydataArr2 = [];

    match arr {
        [] if fn(i, ...anydataArr) => { // uninitialized `i`, `anydataArr`
        }
        [1, 2] if fn2() => { // uninitialized `fn2`
        }
        var [x, y] if i < 3 => { // uninitialized `i`
        }
        [1, 2, 3] if fn(i2, ...anydataArr2) => { // OK
        }
    }
}

isolated function fn(any... x) returns boolean => true;

function testUninitializedVariablesInTheTypeNodeOfLocalDeclStmts() {
    final int j;
    record {| int i = j; |} a1; // uninitialized `j`
    record {| int i = j; |} _ = {}; // uninitialized `j`

    final int k = 1;
    record {| int i = k; |} a2; // OK
    record {| int i = k; |} _ = {}; // OK
}

annotation v1 on type;

function testUninitializedVariablesInAnnotationAccessExprs() {
    typedesc<any> t1;
    _ = t1.@v1; // uninitialized `t1`

    typedesc t2 = int;
    _ = t2.@v1; // OK
}

function testUninitializedVariablesTernaryExpr() {
    boolean condition;

    string a;
    string b;

    if condition {
        b = "";
    }

    string _ = condition ? a : b; // `condition`, ``a` uninitialized, `b` may not be initialized

    condition = false;
    a = "";
    b = "";

    string _ = condition ? a : b; // OK
}

function testPotentiallyUninitVarWithWhile1(boolean b = false) {
    int i;

    while b {
        i = 1;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testPotentiallyUninitVarWithWhile2() {
    int? a = 10;
    int i;

    while a is () {
        i = 1;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testPotentiallyUninitVarWithWhile3() {
    int a = 10;
    int i;

    while a == 20 {
        i = 1;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testPotentiallyUninitVarWithWhile4() {
    int a = 10;
    int i;

    while a < 20 {
        i = 1;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testPotentiallyUninitVarWithWhile5() {
    int i;

    while true {
        i = 1;
    }

    int _ = i; // unreachable code
}

function testUninitVarWithWhile6() {
    int i;

    while false {
        i = 1; // unreachable code
    }

    int _ = i; // variable 'i' is not initialized
}

function testPotentiallyUninitVarWithWhile7() {
    boolean b = true;
    final int i;

    while b {
        i = 1;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testPotentiallyUninitVarWithWhile8() {
    int i;
    boolean b = false;

    while true {
        while b {
            i = 1;
        }
        break;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testUninitVarWithWhile9() {
    int i;
    boolean b = false;

    while false {
        while b {
            i = 1;
        }
        break;
    }

    int _ = i; // variable 'i' is not initialized
}

function testPotentiallyUninitVarWithWhile10() {
    int i;
    boolean b = false;

    while b {
        while true {
            i = 1;
            break;
        }
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testUninitVarWithWhile11() {
    int i;
    boolean b = false;

    while b {
        while false {
            i = 1;
        }
        break;
    }

    int _ = i; // variable 'i' is not initialized
}

function testPotentiallyUninitVarWithWhile12() {
    int i;
    boolean b = false;

    while true {
        if true {
            while b {
                i = 1;
            }
        }
        break;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testPotentiallyUninitVarWithWhile13() {
    int i;
    boolean b = false;

    while true {
        if true {
            while b {
                i = 1;
            }
        }

        int _ = i; // variable 'i' may not have been initialized
        break;
    }
}

function testPotentiallyUninitVarWithWhile14(boolean c = true) {
    int i;
    boolean b = false;

    while b {
        if true {
            if c {
                while true {
                    i = 1;
                }
            }
        }
        break;
    }

    int _ = i; // variable 'i' may not have been initialized
}

function testUninitVarWithWhile15(boolean c = true) {
    int i;
    boolean b = false;

    while b {
        if true {
            if c {
                while false {
                    i = 1;
                }
            }
        }
        break;
    }

    int _ = i; // variable 'i' is not initialized
}

function testUninitVarWithWhile16(boolean c = true) {
    int i;
    boolean b = true;

    if b {
        while c {
            int? d = 10;
            while true {
                if d is int {
                    i = 1;
                } else {
                    i = 2;
                }
                break;
            }
            break;
        }
    }

    int _ = i; // variable 'i' may not have been initialized
}
