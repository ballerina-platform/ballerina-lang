isolated int[] arr1 = [];
isolated (string|int)[] arr2 = [];
isolated int[][][] arr3 = [];
isolated [int, string] arr4 = [];
isolated [int[][][], map<string>[][]] arr5 = [];

function fn1() returns int[] {
    lock {
        return arr1;
    }
}

function fn2() returns (string|int)[] {
    lock {
        return arr2;
    }
}

function fn3() returns int[][] {
    lock {
        return arr3[0];
    }
}

function fn4() returns int[] {
    lock {
        return arr3[0][1];
    }
}

function fn5() returns [int, string] {
    lock {
        return arr4;
    }
}

function fn6() returns int[] {
    lock {
        return arr5[0][0][1];
    }
}

function fn7() returns map<string>[] {
    lock {
        return arr5[1][0];
    }
}
