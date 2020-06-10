import ballerina/lang.'error as err;

function testErrorStackTrace('err:CallStackElement elem) returns string {
    return elem.callableName + ":" + elem.fileName;
}
function testErrorStackTraceWithAutoImports('err:CallStackElement elem) returns string {
    return elem.callableName + ":" + elem.fileName;
}
