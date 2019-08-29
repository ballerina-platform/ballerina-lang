import ballerina/lang.'float as floats;
import ballerina/lang.'int as ints;

function intToFloat (int value) returns (float) {
    float result;
    result = <float>value;
    return result;
}

function intToString (int value) returns (string) {
    string result;
    result = value.toString();
    return result;
}

function intToAny (int value) returns (any) {
    any result;
    result = value;
    return result;
}

function floatToInt (float value) returns (int) {
    int result;
    result = <int>value;
    return result;
}

function floatToString(float value) returns (string) {
    string result;
    result = value.toString();
    return result;
}

function floatToAny (float value) returns (any) {
    any result;
    result = value;
    return result;
}

function stringToInt(string value) returns (int|error) {
    int result;
    result = check ints:fromString(value);
    return result;
}

function stringToFloat(string value) returns (float|error) {
    float result;
    result = check floats:fromString(value);
    return result;
}

function stringToAny(string value) returns (any) {
    any result;
    result = value;
    return result;
}

function booleanToString(boolean value) returns (string) {
    string result;
    result = value.toString();
    return result;
}

function booleanToAny(boolean value) returns (any) {
    any result;
    result = value;
    return result;
}

function anyToInt () returns (int|error) {
    int i = 5;
    any a = i;
    int value;
    value = check trap <int>a;
    return value;
}

function anyToFloat () returns (float|error) {
    float f = 5.0;
    any a = f;
    float value;
    value = check trap <float>a;
    return value;
}

function anyToString () returns (string) {
    string s = "test";
    any a = s;
    string value;
    value = <string>a;
    return value;
}

function anyToBoolean () returns (boolean|error) {
    boolean b = false;
    any a = b;
    boolean value;
    value = check trap <boolean>a;
    return value;
}

function booleanappendtostring(boolean value) returns (string) {
    string result;
    result = value.toString() + "-append-" + value.toString();
    return result;
}
