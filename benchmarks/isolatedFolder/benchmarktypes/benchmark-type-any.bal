
import ballerina/io;

public function benchmarkTypeAnyJSONCasting() {
    var abc = jsonReturnFunction();
    string strVal = extractFieldValue(abc.PropertyName);
}

function extractFieldValue(json fieldValue) returns (string) {
    match fieldValue {
        string s => { return s; }
        json j => { return "Error"; }
    }
}

public function benchmarkPrintAnyVal() {
    any val = jsonReturnFunction();
    io:print(val);
}

public function benchmarkPrintlnAnyVal() {
    any val = jsonReturnFunction();
    io:println(val);
}

function jsonReturnFunction() returns (json) {
    json val = {PropertyName:"Value"};
    return val;
}

