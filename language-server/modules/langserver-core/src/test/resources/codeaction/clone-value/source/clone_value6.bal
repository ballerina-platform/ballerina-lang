isolated json jsonVal1 = {"a": [1, 2]};
isolated json jsonVal2 = "a";
isolated anydata anyVal1 = {"a": [1, 2]};
isolated anydata anyVal2 = false;
const NUM = 1;
isolated anydata anyVal3 = NUM;

function fn1() returns json {
    lock {
        return jsonVal1;
    }
}

function fn2() returns json {
    lock {
        return jsonVal2;
    }
}

function fn3() returns anydata {
    lock {
        return anyVal1;
    }
}

function fn4() returns anydata {
    lock {
        return anyVal2;
    }
}

function fn5() returns anydata {
    lock {
        return anyVal3;
    }
}
