import ballerina/io;

function successfulXmlCasting() returns (string)  {
  var abc = jsonReturnFunction();
  string strVal = extractFieldValue(abc.PropertyName);
  return strVal;
}

function extractFieldValue(json fieldValue) returns (string) {
  match fieldValue {
    string s => { return s; }
    json j => { return "Error"; }
  }
}

function jsonReturnFunction() returns (json) {
  json val = {PropertyName : "Value"};
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