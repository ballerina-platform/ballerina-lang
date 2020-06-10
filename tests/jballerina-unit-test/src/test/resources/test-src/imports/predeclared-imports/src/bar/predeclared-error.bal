
function testErrorStackTrace('error:CallStackElement elem) returns string {
    return elem.callableName + ":" + elem.fileName;
}
