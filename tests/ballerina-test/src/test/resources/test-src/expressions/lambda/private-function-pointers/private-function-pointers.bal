import a.b;

function test1() returns (int){
    function (int, int) returns (int) addFunction = b:Fn1();
    return addFunction(1, 2);
}

function test2() returns (int){
    function (int, int) returns (int) addFunction = b:Fn2();
    return addFunction(1, 2);
}
