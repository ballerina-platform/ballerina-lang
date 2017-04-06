function floattoint(float value)(int) {
    int result;
    result = (int)value;
    return result;
}

function inttofloat(int value)(float) {
    float result;
    result = value;
    return result;
}

function stringtoint(string value)(int) {
    int result;
    result = (int)value;
    return result;
}

function stringtofloat(string value)(float) {
    float result;
    result = (float)value;
    return result;
}

function inttostring(int value)(string) {
    string result;
    result = (string)value;
    return result;
}

function floattostring(float value)(string) {
    string result;
    result = (string)value;
    return result;
}

function booleantostring(boolean value)(string) {
    string result;
    result = (string)value;
    return result;
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
