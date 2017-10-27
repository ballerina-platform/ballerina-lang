function successfulXmlCasting()(xml) {
  any abc = jsonReturnFunction();
  json jsonVal;
  jsonVal, _ = (json)abc;
  xml xmlVal;
  xmlVal, _ = <xml>jsonVal;
  return xmlVal;
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
