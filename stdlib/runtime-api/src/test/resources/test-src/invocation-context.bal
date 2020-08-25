import ballerina/runtime;

function testInvocationContextId() returns string {
    return runtime:getInvocationContext().id;
}

function testInvocationContextAttributes() returns map<any> {
    return runtime:getInvocationContext().attributes;
}

