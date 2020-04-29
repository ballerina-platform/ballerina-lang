import ballerina/io;

public type Foo record {|
   string bar;
|};

public function main(string... args) {
    testExpressions();
}

function testExpressions() {
    //uni-expressions
    var a = -5;

    //binary-expressions
    int b = 1 + 1;
    io:println(a);
    io:println(b);

    // field-access-expr
    Foo foo = {bar: "templers rd"};
    io:println(foo.bar);
}
