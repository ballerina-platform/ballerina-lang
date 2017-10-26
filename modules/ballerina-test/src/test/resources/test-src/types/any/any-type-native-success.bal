import ballerina.lang.system;

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
    system:println(val);
}

function printAnyVal() {
    any val = jsonReturnFunction();
    system:print(val);
}

function findBestNativeFunctionPrintln() {
    int val = 8;
    system:println(val);
}

function findBestNativeFunctionPrint() {
    int val = 7;
    system:print(val);
}
