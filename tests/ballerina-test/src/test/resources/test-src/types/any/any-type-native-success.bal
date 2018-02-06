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
