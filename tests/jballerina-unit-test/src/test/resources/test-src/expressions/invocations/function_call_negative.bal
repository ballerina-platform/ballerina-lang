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
