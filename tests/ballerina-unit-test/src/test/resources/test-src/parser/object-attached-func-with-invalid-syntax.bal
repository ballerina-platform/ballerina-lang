type TestObj object {
    function foo(int a) returns string;
    function bar(string a) returns int;
};

function TestObj.foo(int a) returns string {
    return <string>a;
}

function <TestObj t>bar(string a) returns int {
    return 123;
}
