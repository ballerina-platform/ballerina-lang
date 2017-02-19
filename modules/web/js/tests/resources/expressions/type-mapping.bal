function stringtoint(string value)(int) {
    int result;
    result = (int)value;
    return result;
}

function inttostring(int value)(string) {
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