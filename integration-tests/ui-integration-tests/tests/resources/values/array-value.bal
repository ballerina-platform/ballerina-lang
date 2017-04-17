function lazyInitThrowArrayIndexOutOfBound() {
    int[] arr = [];
    int x;

    x = arr[0];
}

function lazyInitSizeZero() (string[]) {
    string[] names = [];

    return names;
}

function addValueToIntArray() (int[]) {
    int[] arr = [];
    int x;
    x = 4;
    arr[0] = -10;
    arr[15] = 2 + 18;
    arr[99] = 2147483647;
    arr[100] = -x;
    arr[115] = -2147483647;
    arr[199] = 6;

    return arr;
}

function addValueToFloatArray() (float[]) {
    float[] arr = [];
    arr[0] = -10.0;
    arr[15] = 2.5 ;
    arr[99] = 2147483647.1;
    arr[100] = 4.3;
    arr[115] = -2147483647.7;
    arr[199] = 6.9;

    return arr;
}


function commnetFunction() {
}