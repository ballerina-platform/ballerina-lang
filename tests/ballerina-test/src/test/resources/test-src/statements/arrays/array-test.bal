function testFloatArrayLength(float[] arg) returns (int, int){
    float[] defined;
    defined = [10.1, 11.1];
    defined[2] = 12.1;
    return (lengthof arg , lengthof defined);
}

function testIntArrayLength(int[] arg) returns (int, int){
    int[] defined;
    defined = [ 1, 2, 3];
    defined[3] = 4;
    return (lengthof arg , lengthof defined);
}

function testStringArrayLength(string[] arg) returns (int, int){
    string[] defined;
    defined = [ "hello" , "world", "I", "am"];
    defined[4] = "Ballerina";
    return (lengthof arg , lengthof defined);
}

function testXMLArrayLength() returns (int){
    xml[] defined;
    xml v1 = xml `<test>a</test>`;
    xml v2 = xml `<test>b</test>`;
    defined = [v1, v2];
    defined[2] = xml `<test>c</test>`;
    return lengthof defined;
}

function testJSONArrayLength() returns (int, int){
    json[] arg = [{"test": 1}, {"test" : 1}];
    json[] defined;
    json v1;
    json v2;
    v1 = { "test" : "1"};
    v2 = { "test" : "2"};
    defined = [v1, v2];
    defined[2] = { "test" : "3"};
    return (lengthof arg , lengthof defined);
}
