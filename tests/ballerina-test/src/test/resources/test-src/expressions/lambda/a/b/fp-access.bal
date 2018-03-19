package a.b;

public function Fn1() (function (int, int) (int)) {
    return privateFunc1;
}

public function Fn2() (function (int, int) (int)) {
    return function (int a, int b) returns (int) {
                return a + b;
           };
}

function privateFunc1 (int a, int b) (int) {
    return a + b;
}
