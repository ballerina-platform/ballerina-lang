import orgName/modName;

function testFunction() {
    int|modName:TestType testVar = 123;
    string message;
    if (testVar is modName:TestType) {
        message = "Hit within If";
    } else {
        message = "Hit within Else";
    }
}
