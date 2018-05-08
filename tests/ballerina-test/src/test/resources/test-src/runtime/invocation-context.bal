import ballerina/runtime;

function testInvocationId () returns (string) {
    return runtime:getInvocationContext().id;
}

function testUserId () returns (boolean) {
    string userId = "124876jk23i4";
    runtime:getInvocationContext().userPrincipal.userId = userId;
    return userId == runtime:getInvocationContext().userPrincipal.userId;
}

function testUsername () returns (boolean) {
    string username = "tom";
    runtime:getInvocationContext().userPrincipal.username = username;
    return username == runtime:getInvocationContext().userPrincipal.username;
}

function testUserClaims () returns (boolean) {
    map claims = {email:"tom@ballerina.com", org:"wso2"};
    runtime:getInvocationContext().userPrincipal.claims = claims;
    if (runtime:getInvocationContext().userPrincipal.claims.hasKey("email")) {
        string emailInContext = <string>runtime:getInvocationContext().userPrincipal.claims["email"];
        return "tom@ballerina.com" == emailInContext;
    }
    return false;
}

function testAllowedScopes () returns (boolean) {
    string[] scopes = ["email", "profile"];
    runtime:getInvocationContext().userPrincipal.scopes = scopes;
    return "email" == runtime:getInvocationContext().userPrincipal.scopes[0];
}

function testAuthType () returns (boolean) {
    string authType = "JWT";
    runtime:getInvocationContext().authContext.scheme = authType;
    return authType == runtime:getInvocationContext().authContext.scheme;
}

function testAuthToken () returns (boolean) {
    string authToken = "abc.xyz.pqr";
    runtime:getInvocationContext().authContext.authToken = authToken;
    return authToken == runtime:getInvocationContext().authContext.authToken;
}
