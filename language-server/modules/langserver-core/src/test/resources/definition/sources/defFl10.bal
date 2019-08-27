function foo(string | int | boolean a) returns string {
    string sampleRet = "sample";
    match a {
        12 => {return "Value is '12'";};
        "Hello" => {return "Value is 'Hello'";};
        15 => {return "Value is '15'";};
        true => {return "Value is 'true'";};
        false => {return "Value is 'false'";};
        "HelloAgain" => {return sampleRet;};
    }

    return "Value is 'Default'";
}

type Foo2 record {
    string s;
    int i;
    [float, int, boolean] fib;
};

function testStructuredMatchPatternsBasic1() returns string {
    Foo2 f = {s: "S", i: 23, fib: [5.6, 3, true]};

    match f {
        var {s, i: integer, fib: [a, b, c]} => {
            integer += 1;
            a += 1;
            b += 1;
            return "Matched Values : " + s + ", " + integer + ", " + a + ", " + b + ", " + c;
        }
    }

    return "Default";
}

function testStructuredMatchPatternsBasic2() returns string {
    [string, [int, float]] a = ["S", [23, 5.6]];

    match a {
        var [s, [i, f]] => return "Matched Values : " + s + ", " + i + ", " + f;
    }

    return "Default";
}