import ballerina/lang.'runtime as runtime;
import ballerina/test;

function invalidByteLiteral1() returns byte|error {
    int a = -12;
    byte|error b = trap <byte>a;
    return b;
}


function invalidByteLiteral2() returns error? {
    int c = -257;
    byte d = check trap <byte>c;
    return;
}


function invalidByteLiteral3() returns error? {
    int e = 12345;
    byte f = check trap <byte>e;
    return;
}

function testInvalidByteLiteral() {
    int a = 265;
    byte|error b = trap <byte> a;

    test:assertTrue(b is runtime:NumberConversionError);
    if (b is runtime:NumberConversionError) {
        test:assertEquals(b.message(), "{ballerina/lang.runtime}NumberConversionError");
        var detailMessage = b.detail()["message"];
        string detailMessageString = detailMessage is error? detailMessage.toString(): detailMessage.toString();
        test:assertEquals(detailMessageString, "'int' value '265' cannot be converted to 'byte'");
    }
}
