function withError() returns int|string|error {
    return "";
}

function getInt() returns int {
    return 1;
}

class MyClass {

    function test() {
        return getInt();
    }

    function testTuple() returns int {
        return ["string", 10];
    }

    function testWithReturn(int val) returns int {
        return withError();
    }
}
