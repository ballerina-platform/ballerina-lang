import ballerina.io;
function successfulXmlCasting()(string) {
  any abc = jsonReturnFunction();
  json jsonVal;
  jsonVal, _ = (json)abc;
  string strVal;
  strVal, _ = (string)jsonVal.PropertyName;
  return strVal;
}

function jsonReturnFunction()(json) {
  json val = {"PropertyName" : "Value"};
  return val;
}

function printlnAnyVal() {
    any val = jsonReturnFunction();
    io:println(val);
}

function printAnyVal() {
    any val = jsonReturnFunction();
    io:print(val);
}

function findBestNativeFunctionPrintln() {
    int val = 8;
    io:println(val);
}

function findBestNativeFunctionPrint() {
    int val = 7;
    io:print(val);
}
