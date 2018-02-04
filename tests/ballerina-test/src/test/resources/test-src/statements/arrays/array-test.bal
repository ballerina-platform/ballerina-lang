import ballerina.util.arrays;

function testFloatArrayLength(float[] arg)(int, int){
    float[] defined;
    defined = [10.1, 11.1];
    defined[2] = 12.1;
    return lengthof arg , lengthof defined;
}

function testIntArrayLength(int[] arg)(int, int){
    int[] defined;
    defined = [ 1, 2, 3];
    defined[3] = 4;
    return lengthof arg , lengthof defined;
}

function testStringArrayLength(string[] arg)(int, int){
    string[] defined;
    defined = [ "hello" , "world", "I", "am"];
    defined[4] = "Ballerina";
    return lengthof arg , lengthof defined;
}

function testXMLArrayLength()(int){
    xml[] defined;
    xml v1;
    xml v2;
    v1, _ = <xml> "<test>a</test>";
    v2, _ = <xml> "<test>b</test>";
    defined = [v1, v2];
    defined[2], _ = <xml> "<test>c</test>";
    return lengthof defined;
}

function testJSONArrayLength()(int, int){
    json[] arg = [{"test": 1}, {"test" : 1}];
    json[] defined;
    json v1;
    json v2;
    v1 = { "test" : "1"};
    v2 = { "test" : "2"};
    defined = [v1, v2];
    defined[2] = { "test" : "3"};
    return lengthof arg , lengthof defined;
}

function testFloatArrayCopy(float[] arg)(float[]){
    float[] defined = [];
    _ = arrays:copyOf(arg, defined);
    defined[0] = 1;
    defined[1] = 1.1;
    defined[2] = 1.2;
    return defined;
}

function testIntArrayCopy(int[] arg)(int[]){
    int[] defined = [];
    _ = arrays:copyOf(arg, defined);
    defined[0] = 1;
    defined[1] = 2;
    defined[2] = 3;
    return defined;
}

function testStringArrayCopy(string[] arg)(string[]){
    string[] defined = [];
    _ = arrays:copyOf(arg, defined);
    defined[0] = "hello";
    defined[1] = "world";
    defined[2] = "...!!!";
    return defined;
}

function testXMLArrayCopy(xml[] arg)(xml[]){
    xml[] defined = [];
    _ = arrays:copyOf(arg, defined);
    defined[0], _ = <xml> "<test>a</test>";
    defined[1], _ = <xml> "<test>b</test>";
    defined[2], _ = <xml> "<test>c</test>";
    return defined;
}

function testJSONArrayCopy()(json[]){
    json original = [{"json" : "1"}, {"json" : "1"}];
    json[] defined = [];
    _ = arrays:copyOf(original, defined);
    defined[0] = { "test" : "1"};
    defined[1] = { "test" : "2"};
    defined[2] = { "test" : "3"};
    return defined;
}

function testFloatArrayCopyRange(float[] arg, int from, int to)(float[]){
    float[] defined = [];
    _ = arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testIntArrayCopyRange(int[] arg, int from, int to)(int[]){
    int[] defined = [];
    _ = arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testStringArrayCopyRange(string[] arg, int from, int to)(string[]){
    string[] defined = [];
    _ = arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testXMLArrayCopyRange(xml[] arg, int from, int to)(xml[]){
    xml[] defined = [];
    _ = arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testJSONArrayCopyRange(json[] arg, int from, int to)(json[]){
    json[] defined = [];
    _ = arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testStringArraySort(string[] arg)(string[]){
    string[] defined;
    defined = arrays:sort(arg);
    return defined;
}

