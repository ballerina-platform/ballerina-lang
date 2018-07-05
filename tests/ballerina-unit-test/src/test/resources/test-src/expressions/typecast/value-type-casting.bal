function intToFloat (int value) returns (float) {
    float result;
    result = <float>value;
    return result;
}

function intToString (int value) returns (string) {
    string result;
    result = <string>value;
    return result;
}

function intToBoolean (int value) returns (boolean) {
    boolean result;
    result = <boolean>value;
    return result;
}

function intToAny (int value) returns (any) {
    any result;
    result = <any>value;
    return result;
}

function floatToInt (float value) returns (int) {
    int result;
    result = <int>value;
    return result;
}

function floatToString(float value) returns (string) {
    string result;
    result = <string>value;
    return result;
}

function floatToBoolean (float value) returns (boolean) {
    boolean result;
    result = <boolean>value;
    return result;
}

function floatToAny (float value) returns (any) {
    any result;
    result = <any>value;
    return result;
}

function stringToInt(string value) returns (int) {
    int result;
    result = check <int>value;
    return result;
}

function stringToFloat(string value) returns (float) {
    float result;
    result = check <float>value;
    return result;
}

function stringToBoolean(string value) returns (boolean) {
    boolean result;
    result = <boolean>value;
    return result;
}

function stringToAny(string value) returns (any) {
    any result;
    result = <any>value;
    return result;
}

function booleanToInt(boolean value) returns (int) {
    int result;
    result = <int>value;
    return result;
}

function booleanToFloat(boolean value) returns (float) {
    float result;
    result = <float>value;
    return result;
}

function booleanToString(boolean value) returns (string) {
    string result;
    result = <string>value;
    return result;
}

function booleanToAny(boolean value) returns (any) {
    any result;
    result = <any>value;
    return result;
}

function blobToAny(blob value) returns (any) {
    any result;
    result = <any>value;
    return result;
}

function anyToInt () returns (int) {
    int i = 5;
    any a = i;
    int value;
    value = check <int>a;
    return value;
}

function anyToFloat () returns (float) {
    float f = 5.0;
    any a = f;
    float value;
    value = check <float>a;
    return value;
}

function anyToString () returns (string) {
    string s = "test";
    any a = s;
    string value;
    value = <string>a;
    return value;
}

function anyToBoolean () returns (boolean) {
    boolean b;
    any a = b;
    boolean value;
    value = check <boolean>a;
    return value;
}

function anyToBlob (blob data) returns (blob) {
    blob b = data;
    any a = b;
    blob value;
    value = check <blob>a;
    return value;
}

function booleanappendtostring(boolean value) returns (string) {
    string result;
    result = value + "-append-" + value;
    return result;
}

function intarrtofloatarr() returns (float[]) {
    float[] numbers;
    numbers = [999,95,889];
    return numbers;
}

