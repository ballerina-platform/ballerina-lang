import ballerina/lang.errors;

function testVarDeclarationWithAllDeclaredSymbols () (int, errors:TypeConversionError) {
    int a;
    errors:TypeConversionError err;
    float f = 10.0;
    var a, err = <int>f;
    return a, err;
}