function arrayLengthAccessNullArrayCase(int x, int y) returns (int) {
    int z = x + y;
    int[] arr;
    int length;
    length = (lengthof arr);
    return length;
}


function arrayLengthAccessTestJSONArrayNegativeNullCase(int x, int y) returns (int) {
    json arr;
    int length;
    length = (lengthof arr);
    return length;
}

function arrayLengthAccessNullMapCase(int x, int y) returns (int) {
    map m;
    int length;
    length = (lengthof m);
    return length;
}
