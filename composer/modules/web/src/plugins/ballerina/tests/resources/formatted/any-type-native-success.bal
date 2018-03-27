import ballerina/lang.system;

function successfulXmlCasting () (xml) {
    any abc = jsonReturnFunction();
    json jsonVal;
    jsonVal, _ = (json)abc;
    xml xmlVal;
    xmlVal, _ = <xml>jsonVal;
    return xmlVal;
}

function jsonReturnFunction () (json) {
    json val = {"PropertyName":"Value"};
    return val;
}

function printlnAnyVal () {
    any val = jsonReturnFunction();
    system:println(val);
}

function printAnyVal () {
    any val = jsonReturnFunction();
    system:print(val);
}

function findBestNativeFunctionPrintln () {
    int val = 8;
    system:println(val);
}

function findBestNativeFunctionPrint () {
    int val = 7;
    system:print(val);
}
