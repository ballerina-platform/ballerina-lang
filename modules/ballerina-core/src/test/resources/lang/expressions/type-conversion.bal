import ballerina.lang.convertors;

function xmltojson(xml input)(json) {
    json result;
    result = (json)input;
    return result;
}

function jsontoxml(json input)(xml) {
    xml result;
    result = (xml)input;
    return result;
}

function doubletofloat(double value)(float) {
    float result;
    result = (float)value;
    return result;
}

function doubletolong(double value)(long) {
    long result;
    result = (long)value;
    return result;
}

function doubletoint(double value)(int) {
    int result;
    result = (int)value;
    return result;
}

function floattolong(float value)(long) {
    long result;
    result = (long)value;
    return result;
}

function floattoint(float value)(int) {
    int result;
    result = (int)value;
    return result;
}

function longtoint(long value)(int) {
    int result;
    result = (int)value;
    return result;
}

function inttolong(int value)(long) {
    long result;
    result = value;
    return result;
}

function inttofloat(int value)(float) {
    float result;
    result = value;
    return result;
}

function inttodouble(int value)(double) {
    double result;
    result = value;
    return result;
}

function longtofloat(long value)(float) {
    float result;
    result = value;
    return result;
}

function longtodouble(long value)(double) {
    double result;
    result = value;
    return result;
}

function floattodouble(float value)(double) {
    double result;
    result = value;
    return result;
}

function stringtoint(string value)(int) {
    int result;
    result = (int)value;
    return result;
}

function stringtolong(string value)(long) {
    long result;
    result = (long)value;
    return result;
}

function stringtofloat(string value)(float) {
    float result;
    result = (float)value;
    return result;
}

function stringtodouble(string value)(double) {
    double result;
    result = (double)value;
    return result;
}

function stringtoxml(string value)(xml) {
    xml result;
    result = (xml)value;
    return result;
}

function stringtojson(string value)(json) {
    json result;
    result = (json)value;
    return result;
}

function inttostring(int value)(string) {
    string result;
    result = (string)value;
    return result;
}

function longtostring(long value)(string) {
    string result;
    result = (string)value;
    return result;
}

function floattostring(float value)(string) {
    string result;
    result = (string)value;
    return result;
}

function doubletostring(double value)(string) {
    string result;
    result = (string)value;
    return result;
}

function xmltostring(xml value)(string) {
    string result;
    result = (string)value;
    return result;
}

function jsontostring(json value)(string) {
    string result;
    result = (string)value;
    return result;
}

function intarrtolongarr()(long[]) {
    long[] numbers;
    numbers = [999,95,889];
    return numbers;
}

function floatarrtodoublearr()(double[]) {
    double[] numbers;
    numbers = [99.99,4.5,23.56];
    return numbers;
}