import ballerina/test;
import ballerina/io;

string expectedErrMsg = "'string' value cannot be converted to 'int'";

@test:Config{
    dataProvider:"invalidDataGen1"
}
function testFuncNeg1(string result) {
            
    io:println("Input params: ["+result.toString()+"]");
    int|error resultErr = trap int.constructFrom(result);

    if ((resultErr is error)) {
        test:assertTrue(resultErr.detail().toString().endsWith(expectedErrMsg));
    } else {
        test:assertFail("expected error not found.");
    }
}

function invalidDataGen1() returns (string[][]) {
    return [["hi"]];
}

// tests another invalid string data provider
@test:Config{
    dataProvider:"invalidDataGen2"
}
function testFuncNeg2(string fValue, string sValue, string result) {
            
   io:println("Input params: ["+fValue+","+sValue+","+result.toString()+"]");
    int|error fErr = trap int.constructFrom(fValue);
    int|error sErr = trap int.constructFrom(sValue);
    int|error resultErr = trap int.constructFrom(result);

    if ((fErr is error) && (sErr is error) && (resultErr is error)) {
        test:assertTrue(fErr.detail().toString().endsWith(expectedErrMsg));
    } else {
        test:assertFail("expected error not found.");
    }

}

function invalidDataGen2() returns (string[][]) {
    return [["1", "2", "3"]];
}


// tests an invalid tuple data provider

@test:Config{
    dataProvider:"invalidDataGen3"
}
function testFuncNeg3 ([string, string, [string, string]] result) {

    int|error fErr = trap int.constructFrom(result[0]);
    int|error sErr = trap int.constructFrom(result[1]);
    int|error resultErr = trap int.constructFrom(result);

    if ((fErr is error) && (sErr is error) && (resultErr is error)) {
        test:assertTrue(fErr.detail().toString().endsWith(expectedErrMsg));
    } else {
        test:assertFail("expected error not found.");
    }
}

function invalidDataGen3() returns ([string, string, [string, string]][]) {
    return [["hi", "20", ["30", "30"]]];
}
