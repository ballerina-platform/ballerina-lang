function fixReturnTypeCodeAction1() {
    return re `abc`;
}

function fixReturnTypeCodeAction2() {
    string:RegExp reg = re `abc`;
    return reg;
}
