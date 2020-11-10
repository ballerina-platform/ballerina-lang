class TestObj {
    function foo(int a) returns string;
    function bar(string a) returns int;
}

function TestObj.foo(int a) returns string {
    return <string>a;
}
