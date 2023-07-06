import ballerina/lang.regexp;

function fixReturnTypeCodeAction3() {
    regexp:RegExp reg = re `abc`;
    return reg;
}
