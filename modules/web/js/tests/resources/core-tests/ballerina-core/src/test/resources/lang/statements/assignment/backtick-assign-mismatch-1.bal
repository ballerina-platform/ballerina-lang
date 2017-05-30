function testTemplateJSONAssignTypeMismatch() (int, string, int) {
    int a;
    string name;
    int b;

    name = `<name>john</name>`;


    a, name, b = testMultiReturnInternal();
    return a, name, b;
}

function testMultiReturnInternal() (int, string, int) {
    return 5, "john", 6;
}