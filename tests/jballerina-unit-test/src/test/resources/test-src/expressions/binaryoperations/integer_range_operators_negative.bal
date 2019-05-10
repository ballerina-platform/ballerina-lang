function testClosedIntRangeForUnsupportedTypes() {
    string[] strArray = "aa" ... "bbb";
    json j1 = {"type":"Closed"};
    json j2 = {"value":"json"};
    json[] jsonArray = j1 ... j2;
    float[] floatArray = 1.0 ... 1.2;
    int[] intArrayTwo = 1 ... 1.2;
}

function testHalfOpenIntRangeForUnsupportedTypes() {
    string[] strArray = "aa" ..< "bbb";
    json j1 = {"type":"Closed"};
    json j2 = {"value":"json"};
    json[] jsonArray = j1 ..< j2;
    float[] floatArray = 1.0 ..< 1.2;
    int[] intArrayTwo = 1 ..< 1.2;
}
