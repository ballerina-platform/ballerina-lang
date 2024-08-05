isolated [int, int] tup1 = [1, 2];
isolated [[int, string], int] tup2 = [[1, "a"], 2];
isolated [[[boolean, record {int id;}, int], string]] tup3 = [[[true, {id: 1}, 2], "a"]];

function fn1([int, int] a)  {
    lock {
        tup1 = a;
    }
}

function fn2([int, string] a)  {
    lock {
        tup2[0] = a;
    }
}

function fn3(record {int id;} a)  {
    lock {
        tup3[0][0][1] = a;
    }
}
