isolated int[] arr1 = [];
isolated (string|int)[] arr2 = [];
isolated int[][][] arr3 = [];
isolated [int, string] arr4 = [];
isolated [int[][][], map<string>[][]] arr5 = [];

function fn1(int[] a) {
    lock {
        arr1 = a;
    }
}

function fn2((string|int)[] a) {
    lock {
        arr2 = a;
    }
}

function fn3(int[][] a) {
    lock {
        arr3[0] = a;
    }
}

function fn4(int[] a) {
    lock {
        arr3[0][1] = a;
    }
}

function fn5([int, string] a) {
    lock {
        arr4 = a;
    }
}

function fn6(int[] a) {
    lock {
        arr5[0][0][1] = a;
    }
}

function fn7(map<string>[] a) {
    lock {
        arr5[1][0] = a;
    }
}
