import testorg/foo as foo;

function testInvokeFunctionInMixOrder1() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, e="Bob", 20.0, c="Alex", 40, 50);
}

function testInvokeFunctionInMixOrder2() returns [int, float, string, int, string, int[]] {
    int[] array = [40, 50, 60];
    return foo:functionWithAllTypesParams(10, e="Bob", 20.0, c="Alex", ...array, d=30);
}

function funcInvocAsRestArgs() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob", ...getIntArray());
}

function testInvokeFunctionWithRequiredAndRestArgs() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, 40, 50, 60);
}

function testInvalidArgsViaVararg() {
    var a = [1, true, false];
    [float, string...] b = [1.0, "str"];

    string s = foo:bar(1, ...a); // 16: incompatible types: expected 'string', found 'int'
                             // 30: incompatible types: expected 'boolean[]', found '[int,boolean,boolean]'
    int i = foo:bar(1, ...["hello", false]); // 28: incompatible types: expected 'boolean', found 'string'

    _ = foo:bar(1, false, ...[1, "world"]); // 31: incompatible types: expected 'boolean', found 'int'
                                        // 34: incompatible types: expected 'boolean', found 'string'
    foo:bar(...b);  // 5: variable assignment is required
                // 16: incompatible types: expected '[int,boolean...]', found '[float,string]'

    foo:baz(1, ...a);   // 13: incompatible types: expected 'string', found 'int'
                    // 19: incompatible types: expected '[float,boolean...]', found '[int,boolean,boolean]'
    foo:baz(...b);  // 16: incompatible types: expected '[string,float,boolean...]', found '[float,string...]'
}

function testInvalidIndividualArgsWithVararg() {
    boolean[] a = [true, false];

    _ = foo:bar(i = 1, ...a); // 24: rest argument not allowed after named arguments

    _ = foo:baz("str", ...a); // 27: incompatible types: expected '[float,boolean...]', found 'boolean[]'

    var fn = function () returns [boolean, boolean...] {
        return [true, false, false];
    };

    int i = foo:bar(1, 1.0, ...fn()); // 24: incompatible types: expected 'boolean', found 'float'
}

function testInvalidArgsViaVarargForObject() {
    foo:Foo f = new;

    var a = [1, true, false];
    [float, string...] b = [1.0, "str"];

    string s = f.bar(1, ...a); // 16: incompatible types: expected 'string', found 'int'
                               // 28: incompatible types: expected 'boolean[]', found '[int,boolean,boolean]'
    int i = f.bar(1, ...["hello", false]); // 26: incompatible types: expected 'boolean', found 'string'

    _ = f.bar(1, false, ...[1, "world"]); // 29: incompatible types: expected 'boolean', found 'int'
                                          // 32: incompatible types: expected 'boolean', found 'string'
    f.bar(...b);  // 5: variable assignment is required
                  // 14: incompatible types: expected '[int,boolean...]', found '[float,string]'

    f->baz(1, ...a);   // 12: incompatible types: expected 'string', found 'int'
                       // 18: incompatible types: expected '[float,boolean...]', found '[int,boolean,boolean]'
    f->baz(...b);  // 15: incompatible types: expected '[string,float,boolean...]', found '[float,string...]'
}

function testInvalidIndividualArgsWithVarargForObject() {
    foo:Foo f = new;

    boolean[] a = [true, false];

    _ = f.bar(i = 1, ...a); // 22: rest argument not allowed after named arguments

    _ = f->baz("str", ...a); // 26: incompatible types: expected '[float,boolean...]', found 'boolean[]'

    var fn = function () returns [boolean, boolean...] {
        return [true, false, false];
    };

    int i = f.bar(1, 1.0, ...fn()); // 22: incompatible types: expected 'boolean', found 'float'
}
