function intToFloat (int value) (float) {
    float result;
    result = <float>value;
    return result;
}

function intToString (int value) (string) {
    string result;
    result = <string>value;
    return result;
}

function intToBoolean (int value) (boolean) {
    boolean result;
    result = <boolean>value;
    return result;
}

function intToAny (int value) (any) {
    any result;
    result = (any)value;
    return result;
}

function floatToInt (float value) (int) {
    int result;
    result, _ = <int>value;
    return result;
}

function floatToString(float value)(string) {
    string result;
    result = <string>value;
    return result;
}

function floatToBoolean (float value) (boolean) {
    boolean result;
    result = <boolean>value;
    return result;
}

function floatToAny (float value) (any) {
    any result;
    result = (any)value;
    return result;
}

function stringToInt(string value)(int) {
    int result;
    result, _ = <int>value;
    return result;
}

function stringToFloat(string value)(float) {
    float result;
    result, _ = <float>value;
    return result;
}

function stringToBoolean(string value)(boolean) {
    boolean result;
    result, _ = <boolean>value;
    return result;
}

function stringToAny(string value)(any) {
    any result;
    result = (any)value;
    return result;
}

function booleanToInt(boolean value)(int) {
    int result;
    result = <int>value;
    return result;
}

function booleanToFloat(boolean value)(float) {
    float result;
    result = <float>value;
    return result;
}

function booleanToString(boolean value)(string) {
    string result;
    result = <string>value;
    return result;
}

function booleanToAny(boolean value)(any) {
    any result;
    result = (any)value;
    return result;
}

function blobToAny(blob value)(any) {
    any result;
    result = (any)value;
    return result;
}

function anyToInt () (int) {
    int i = 5;
    any a = i;
    int value;
    value, _ = (int)a;
    return value;
}

function anyToFloat () (float) {
    float f = 5.0;
    any a = f;
    float value;
    value, _ = (float)a;
    return value;
}

function anyToString () (string) {
    string s = "test";
    any a = s;
    string value;
    value, _ = (string)a;
    return value;
}

function anyToBoolean () (boolean) {
    boolean b;
    any a = b;
    boolean value;
    value, _ = (boolean)a;
    return value;
}

function anyToBlob (blob data) (blob) {
    blob b = data;
    any a = b;
    blob value;
    value, _ = (blob)a;
    return value;
}

function booleanappendtostring(boolean value)(string) {
    string result;
    result = value + "-append-" + value;
    return result;
}

function intarrtofloatarr()(float[]) {
    float[] numbers;
    numbers = [999,95,889];
    return numbers;
}

