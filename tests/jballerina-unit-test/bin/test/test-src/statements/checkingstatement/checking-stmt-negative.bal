function testStaticTypeNotNil() {
    check 3; // error
    check check 3; // error
    check string `str ${check foo()}`; // error
    check [1, 2, 3]; // error
    check { i: 2, s: 3 }; // error
    check new Obj(); // error
    int|error x = 3;
    check x; // error
    int? y = ();
    check y; // error
}

function foo() returns string|error  {
    return error("MSG");
}

class Obj {
    function init() {
    }

    function foo() {
    }
}
