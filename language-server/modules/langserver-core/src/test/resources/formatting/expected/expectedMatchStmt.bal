import ballerina/io;

type Foo record {
    string s;
    int i;
    float f;
};

function testStructuredMatchPatternsBasic1() returns string {
    Foo foo1 = {
        s: "S",
        i: 23,
        f: 5.6
    };

    match foo1 {
        var {s, i: integer, f} => {
            return "Matched Values : " + s + ", " + integer.toString() + ", " + f.toString();
        }
    }

    return "Default";
}

function testStructuredMatchPatternsBasic2() returns string {
    [string, [int, float]] a = ["S", [23, 5.6]];

    match a {
        var [s, [i, f]] => {
            return "Matched Values : " + s + ", " + i.toString() + ", " + f.toString();
        }
    }

    return "Default";
}

function testStructuredMatchPatternsBasic3() returns string {
    [string, int, float] a = ["S", 23, 5.6];

    match a {
        var [s, i, f] => {
            return "Matched Values : " + s + ", " + i.toString() + ", " + f.toString();
        }
    }

    return "Default";
}

type Foo1 record {
    string s;
    int i;
    [float, int, boolean] fib;
};

function testStructuredMatchPatternsBasic4() returns string {
    Foo1 foo2 = {
        s: "S",
        i: 23,
        fib: [
            5.6,
            3,
            true
        ]
    };

    match foo2 {
        var {s, i: integer, fib: [a, b, c]} => {
            integer += 1;
            a += 1;
            b += 1;
            return "Matched Values : " + s + ", " + integer.toString() + ", " + a.toString() + ", " + b.toString() + ", " + c.toString();
        }
    }

    return "Default";
}

function foo(string|int|boolean a) returns string {
    match a {
        12 => {
            return "Value is '12'";
        }
        "Hello" => {
            return "Value is 'Hello'";
        }
        15 => {
            return "Value is '15'";
        }
        true => {
            return "Value is 'true'";
        }
        false => {
            return "Value is 'false'";
        }
        "HelloAgain" => {
            return "Value is 'HelloAgain'";
        }
    }

    return "Value is 'Default'";
}

function typeGuard1([string, int]|Foo|error|[int, boolean]|int|float x) returns string {
    match x {
        var [s, i] if s is string => {
            return "Matched with string : " + s + " added text with " + io:sprintf("%s", i);
        }
        var [s, i] if s is int => {
            return "Matched with float : " + io:sprintf("%s", s + 4.5) + " with " + io:sprintf("%s", i);
        }
        var {var2} if var2 is Foo => {
            return "Matched with record int : " + io:sprintf("%s", var2) + " with " + io:sprintf("%s", var2.toString() + "12");
        }
        var {var1, var2} => {
            return "Matched with record with ClosedBar1 : " + io:sprintf("%s", var1) + " with " + io:sprintf("%s", var2);
        }
        var [s, i] if i is boolean => {
            return "Matched with boolean : " + io:sprintf("%s", s) + ", " + io:sprintf("%s", i);
        }
        var y => {
            return "Matched with default type - float : " + io:sprintf("%s", y);
        }
    }
}
