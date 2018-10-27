function arrayLengthAccessNullArrayCase(int x, int y) returns (int) {
    int z = x + y;
    int[] arr;
    int lengthVal;
    lengthVal = (lengthof arr);
    return lengthVal;
}


function arrayLengthAccessTestJSONArrayNegativeNullCase(int x, int y) returns (int) {
    json arr;
    int lengthVal;
    lengthVal = (lengthof arr);
    return lengthVal;
}

function arrayLengthAccessNullMapCase(int x, int y) returns (int) {
    map m;
    int lengthVal;
    lengthVal = (lengthof m);
    return lengthVal;
}
