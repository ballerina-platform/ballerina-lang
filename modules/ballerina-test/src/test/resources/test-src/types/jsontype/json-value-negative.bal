function testJsonArrayWithUnsupportedtypes() (json) {
    datatable dt;
    json j = ["a", "b", "c", dt];
    return j;
}

function testJsonInitWithUnsupportedtypes() (json) {
    datatable dt;
    json j = {"name":"Supun", "value":dt};
    return j;
}

function testIntArrayToJsonAssignment() (json) {
    int[] a = [1, 5, 9];
    json j = a;
    return j;
}

function testFloatArrayToJsonAssignment() (json) {
    float[] f = [1.3, 5.4, 9.4];
    json j = f;
    return j;
}

function testStringArrayToJsonAssignment() (json) {
    string[] s = ["apple", "orange"];
    json j = s;
    return j;
}

function testBooleanArrayToJsonAssignment() (json) {
    boolean[] b = [true, true, false];
    json j = b;
    return j;
}
