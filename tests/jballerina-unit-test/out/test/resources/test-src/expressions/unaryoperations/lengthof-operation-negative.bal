function arrayLengthAccessNullArrayCase(int x, int y) returns (int) {
    int z = x + y;
    int[] arr = [];
    int length;
    length = (arr.length());
    return length;
}

function arrayLengthAccessNullMapCase(int x, int y) returns (int) {
    map<any> m = {};
    int length;
    length = (m.length());
    return length;
}
