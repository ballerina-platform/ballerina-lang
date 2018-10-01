function test1() returns int {
    function (int, int) returns int pow = calculatePow;
    return pow.call(10, 20);
}

function test2() returns int {
    Test1 t1 = new;
    return t1.pow.call(10, 20);
}

function test3() returns int {
    Test2 t2 = new;
    return t2.t1.pow.call(10, 20);
}

function calculatePow(int i, int j) returns int {
    return i * j;
}


type Test1 object {

    function (int, int) returns int pow = calculatePow;
};

type Test2 object {

    Test1 t1 = new;
};