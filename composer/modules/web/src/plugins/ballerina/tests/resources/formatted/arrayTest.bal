import ballerina/lang.arrays;
import ballerina/lang.xmls;

function testFloatArrayLength (float[] arg) (int, int) {
    float[] defined;
    defined = [10.1, 11.1];
    defined[2] = 12.1;
    return arg.length, defined.length;
}

function testIntArrayLength (int[] arg) (int, int) {
    int[] defined;
    defined = [1, 2, 3];
    defined[3] = 4;
    return arg.length, defined.length;
}

function testStringArrayLength (string[] arg) (int, int) {
    string[] defined;
    defined = ["hello", "world", "I", "am"];
    defined[4] = "Ballerina";
    return arg.length, defined.length;
}

function testXMLArrayLength (xml[] arg) (int, int) {
    xml[] defined;
    xml v1;
    xml v2;
    v1 = xmls:parse("<test>a</test>");
    v2 = xmls:parse("<test>b</test>");
    defined = [v1, v2];
    defined[2] = xmls:parse("<test>c</test>");
    return arg.length, defined.length;
}

function testJSONArrayLength (json[] arg) (int, int) {
    json[] defined;
    json v1;
    json v2;
    v1 = {"test":"1"};
    v2 = {"test":"2"};
    defined = [v1, v2];
    defined[2] = {"test":"3"};
    return arg.length, defined.length;
}

function testMessageArrayLength (message[] arg) (int, int) {
    message[] defined;
    message v1;
    message v2;
    v1 = {};
    v2 = {};
    defined = [v1, v2];
    defined[2] = {};
    return arg.length, defined.length;
}

function testFloatArrayCopy (float[] arg) (float[]) {
    float[] defined = [];
    arrays:copyOf(arg, defined);
    defined[0] = 1;
    defined[1] = 1.1;
    defined[2] = 1.2;
    return defined;
}

function testIntArrayCopy (int[] arg) (int[]) {
    int[] defined = [];
    arrays:copyOf(arg, defined);
    defined[0] = 1;
    defined[1] = 2;
    defined[2] = 3;
    return defined;
}

function testStringArrayCopy (string[] arg) (string[]) {
    string[] defined = [];
    arrays:copyOf(arg, defined);
    defined[0] = "hello";
    defined[1] = "world";
    defined[2] = "...!!!";
    return defined;
}

function testXMLArrayCopy (xml[] arg) (xml[]) {
    xml[] defined = [];
    arrays:copyOf(arg, defined);
    defined[0] = xmls:parse("<test>a</test>");
    defined[1] = xmls:parse("<test>b</test>");
    defined[2] = xmls:parse("<test>c</test>");
    return defined;
}

function testJSONArrayCopy (json[] arg) (json[]) {
    json[] defined = [];
    arrays:copyOf(arg, defined);
    defined[0] = {"test":"1"};
    defined[1] = {"test":"2"};
    defined[2] = {"test":"3"};
    return defined;
}

function testMessageArrayCopy (message[] arg) (message[]) {
    message[] defined = [];
    arrays:copyOf(arg, defined);
    defined[0] = {};
    defined[1] = {};
    defined[2] = {};
    return defined;
}

function testFloatArrayCopyRange (float[] arg, int from, int to) (float[]) {
    float[] defined = [];
    arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testIntArrayCopyRange (int[] arg, int from, int to) (int[]) {
    int[] defined = [];
    arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testStringArrayCopyRange (string[] arg, int from, int to) (string[]) {
    string[] defined = [];
    arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testXMLArrayCopyRange (xml[] arg, int from, int to) (xml[]) {
    xml[] defined = [];
    arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testJSONArrayCopyRange (json[] arg, int from, int to) (json[]) {
    json[] defined = [];
    arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testMessageArrayCopyRange (message[] arg, int from, int to) (message[]) {
    message[] defined = [];
    arrays:copyOfRange(arg, defined, from, to);
    return defined;
}

function testStringArraySort (string[] arg) (string[]) {
    string[] defined;
    defined = arrays:sort(arg);
    return defined;
}

