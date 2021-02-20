import ballerina/jballerina.java;

function successfulXmlCasting() returns (string)  {
  var abc = jsonReturnFunction();
  string strVal = extractFieldValue(checkpanic abc.PropertyName);
  return strVal;
}

function extractFieldValue(json fieldValue) returns (string) {
    any a = fieldValue;
    if a is string {
        return a;
    } else if a is json {
        return "Error";
    }
    return "";
}

function jsonReturnFunction() returns (json) {
  json val = {PropertyName : "Value"};
  return val;
}

function printlnAnyVal() {
    any val = jsonReturnFunction();
    println(val);
}

function printAnyVal() {
    any val = jsonReturnFunction();
    print(val);
}

function findBestNativeFunctionPrintln() {
    int val = 8;
    println(val);
}

function findBestNativeFunctionPrint() {
    int val = 7;
    print(val);
}

public function print(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
