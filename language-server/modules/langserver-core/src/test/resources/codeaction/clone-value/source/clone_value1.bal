isolated int[] arr1 = [];
isolated int[][] arr2 = [];
isolated map<string>[] arr3 = [];
isolated int[][][] arr4 = [];

isolated function fn1() returns int[] {
    lock {
        return arr1;
    }
}

function fn2() returns int[] {
    lock {
        return arr2[0];
    }
}

function fn3() returns map<string>[] {
    lock {
        return arr3;
    }
}

function fn4() returns int[] {
    lock {
        return arr4[0][0];
    }
}
