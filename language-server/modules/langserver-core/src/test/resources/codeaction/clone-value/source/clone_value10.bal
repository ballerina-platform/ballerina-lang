isolated int[] arr1 = [];
isolated int[][] arr2 = [];
isolated map<string>[] arr3 = [];
isolated int[][][] arr4 = [];

isolated function fn1(int[] a) {
    lock {
        arr1 = a;
    }
}

function fn2(int[] a) {
    lock {
        arr2[0] = a;
    }
}

function fn3(map<string> a) {
    lock {
        arr3[0] = a;
    }
}

function fn4(int[] a) {
    lock {
        arr4[0][0] = a;
    }
}
