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

function benchmarkPrintAnyVal() {
    any val = jsonReturnFunction();
    io:print(val);
}

function benchmarkPrintlnAnyVal() {
    any val = jsonReturnFunction();
    io:println(val);
}
