function test1()(int){
    function (int, int) returns (int) addFunction = func1;
    return addFunction(1, 2);
}

function func1 (int a, int b) (int c) {
    c = a + b;
    return;
}

function test2 () (string) {
    function (int , int )(string) sumFunction = function (int a, int b)(string) {
                                       int value =  a + b;
                                       return "sum is " + value;
                                   };
    return sumFunction(1,2);
}

function test3()(int){
    int x = test3Callee(1, func1);
    return x;
}

function test3Callee(int a, function (int x, int y) returns (int z) func ) returns (int){
    int x = a + func(1, 2);
    return x;
}