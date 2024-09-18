isolated [int, int] tup1 = [1, 2];
isolated [[int, string], int] tup2 = [[1, "a"], 2];
isolated [[[boolean, record {int id;}, int], string]] tup3 = [[[true, {id: 1}, 2], "a"]];

function fn1() returns [int, int] {
    lock {
        return tup1;
    }
}

function fn2() returns [int, string] {
    lock {
        return tup2[0];
    }
}

function fn3() returns record {int id;} {
    lock {
        return tup3[0][0][1];
    }
}
