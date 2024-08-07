isolated json jsonVal1 = {"a": [1, 2]};
isolated json jsonVal2 = "a";
isolated anydata anyVal1 = {"a": [1, 2]};
isolated anydata anyVal2 = false;
const NUM = 1;
isolated anydata anyVal3 = NUM;

function fn1(json a) {
    lock {
        jsonVal1 = a;
    }
}

function fn2(json a) {
    lock {
        jsonVal2 = a;
    }
}

function fn3(anydata a) {
    lock {
        anyVal1 = a;
    }
}

function fn4(anydata a) {
    lock {
        anyVal2 = a;
    }
}

function fn5(anydata a) {
    lock {
        anyVal3 = a;
    }
}
