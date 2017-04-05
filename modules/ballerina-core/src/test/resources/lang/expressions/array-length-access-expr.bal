function arrayLengthAccessTest(int x, int y) (int) {
    int[] arr = [];

    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    arr[3] = arr.length;

    return arr.length;
}
