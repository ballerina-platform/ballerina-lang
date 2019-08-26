import ballerina/runtime;

function testInvocationContextId() returns string {
    return runtime:getInvocationContext().id;
}

function testInvocationContextAttributes() returns map<any> {
    return runtime:getInvocationContext().attributes;
}

function testInvocationContextPrincipal(string userId) returns string? {
    runtime:Principal principal = {
        userId: userId,
        username: "ballerina"
    };
    var ctx = runtime:getInvocationContext();
    ctx.principal = principal;
    runtime:Principal? testPrincipal = runtime:getInvocationContext()?.principal;
    if (testPrincipal is runtime:Principal) {
        return testPrincipal?.userId;
    }
    return ();
}
