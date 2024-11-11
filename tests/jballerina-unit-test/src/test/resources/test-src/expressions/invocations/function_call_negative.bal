function testInvalidArgType() {
    string val = "sample";
    intArgTest(val);
}

function intArgTest(int val) {
    string result = val.toString() + "abc";
}

function testUndefinedFunction() {
    foo();
}

private function testMyFunc() {

}

function testInvalidArgsViaVararg() {
    var a = [1, true, false];
    [float, string...] b = [1.0, "str"];
    
    string s = bar(1, ...a); // 16: incompatible types: expected 'string', found 'int'
                             // 26: incompatible types: expected 'boolean[]', found '[int,boolean,boolean]'
    int i = bar(1, ...["hello", false]); // 24: incompatible types: expected 'boolean', found 'string'
    
    _ = bar(1, false, ...[1, "world"]); // 27: incompatible types: expected 'boolean', found 'int'
                                        // 30: incompatible types: expected 'boolean', found 'string'
    bar(...b);  // 5: variable assignment is required
                // 12: incompatible types: expected '[int,boolean...]', found '[float,string]'

    baz(1, ...a);   // 9: incompatible types: expected 'string', found 'int'
                    // 15: incompatible types: expected '[float,boolean...]', found '[int,boolean,boolean]'
    baz(...b);  // 12: incompatible types: expected '[string,float,boolean...]', found '[float,string...]'
}

function testInvalidIndividualArgsWithVararg() {
    boolean[] a = [true, false];

    _ = bar(i = 1, ...a); // 20: rest argument not allowed after named arguments

    _ = baz("str", ...a); // 23: incompatible types: expected '[float,boolean...]', found 'boolean[]'

    var fn = function () returns [boolean, boolean...] {
        return [true, false, false];
    };

    int i = bar(1, 1.0, ...fn()); // 20: incompatible types: expected 'boolean', found 'float'
}

function bar(int i, boolean... b) returns int {
    return i;
}

function baz(string s, float f = 2.0, boolean... b) {
}

function testInvalidArrayArg() {
    int[1] a = [1];
    int m = allInts(...a); // incompatible types: expected '([int,int,int...]|record {| int i; int j; |})', found 'int[1]'

    (int|string)[3] b = [1, 2, 3];
    int n = allInts(...b); // incompatible types: expected '([int,int,int...]|record {| int i; int j; |})', found '(int|string)[3]'
    int o = intsWithStringRestParam(...b); // incompatible types: expected '([int,int,string...]|record {| int i; int j; |})', found '(int|string)[3]'

    int[5] c = [1, 2, 3, 1, 2];
    int p = intParam(...c); // incompatible types: expected '([int]|record {| int i; |})', found 'int[5]'

    int[] d = [1, 1, 2, 2, 1];
    int q = intsWithStringRestParam(...c); // incompatible types: expected '([int,int,string...]|record {| int i; int j; |})', found 'int[5]'
    int r = intsWithStringRestParam(...d); // incompatible types: expected '([int,int,string...]|record {| int i; int j; |})', found 'int[]'

    anydata[] e = [1, 2, 3, 4];
    int s = intRestParam(...b); // incompatible types: expected 'int[]', found '(int|string)[3]'
    int t = intRestParam(...e); // incompatible types: expected 'int[]', found 'anydata[]'
}

function allInts(int i, int j, int... k) returns int {
    int tot = i + j;
    foreach int x in k {
        tot += x;
    }
    return tot;
}

function intsWithStringRestParam(int i, int j, string... k) returns int {
    return 2 * (i + j + k.length());
}

function intRestParam(int... i) returns int {
     int tot = 0;
     foreach int x in i {
         tot += x;
     }
     return tot;
}

function intParam(int i) returns int => i;

function invalidNamedArg1() returns error {
     int[] y = [5,6];
     string x = "5";
     _ = int:sum(ns=y);
     _ = int:sum(ss=y);
     _ = check int:fromString(s=x); // no error
     _ = check int:fromString(s=y);
     _ = check int:fromString(ss=x);
}

function testFuncWithNilReturnTypeWithoutVariableAssignment() {
    f1();
    f2();
    f3();
}

function f1() returns ()|int|() => ();
function f2() returns 1|null => null;
function f3() returns null|1|null => null;

function invalidRestParam() {
    int x = 1;
    func1(i = x); // named argument not allowed for rest parameter
    func1(i = 2); // named argument not allowed for rest parameter
    func2(x, i = x); // named argument not allowed for rest parameter
    func2(t = x, i = x); // named argument not allowed for rest parameter
}

function func1(int... i) {}

function func2(int t, int... i) {}
