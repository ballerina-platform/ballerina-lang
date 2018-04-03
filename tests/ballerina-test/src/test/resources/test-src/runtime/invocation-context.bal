import ballerina/runtime;

function testInvocationId () returns (string) {
    return runtime:getInvocationContext().invocationId;
}

function testUserId () returns (boolean) {
    string userId = "124876jk23i4";
    runtime:getInvocationContext().authenticationContext.userId = userId;
    return userId == runtime:getInvocationContext().authenticationContext.userId;
}

function testUsername () returns (boolean) {
    string username = "tom";
    runtime:getInvocationContext().authenticationContext.username = username;
    return username == runtime:getInvocationContext().authenticationContext.username;
}

function testUserGroups () returns (boolean) {
    string[] groups = ["admin", "publisher"];
    runtime:getInvocationContext().authenticationContext.groups = groups;
    return "admin" == runtime:getInvocationContext().authenticationContext.groups[0];
}

function testUserClaims () returns (boolean) {
    map claims = {email:"tom@ballerina.com", org:"wso2"};
    runtime:getInvocationContext().authenticationContext.claims = claims;
    if (runtime:getInvocationContext().authenticationContext.claims.hasKey("email")) {
        string emailInContext = <string>runtime:getInvocationContext().authenticationContext.claims["email"];
        return "tom@ballerina.com" == emailInContext;
    }
    return false;
}

function testAllowedScopes () returns (boolean) {
    string[] scopes = ["email", "profile"];
    runtime:getInvocationContext().authenticationContext.scopes = scopes;
    return "email" == runtime:getInvocationContext().authenticationContext.scopes[0];
}

function testAuthType () returns (boolean) {
    string authType = "JWT";
    runtime:getInvocationContext().authenticationContext.authType = authType;
    return authType == runtime:getInvocationContext().authenticationContext.authType;
}

function testAuthToken () returns (boolean) {
    string authToken = "abc.xyz.pqr";
    runtime:getInvocationContext().authenticationContext.authToken = authToken;
    return authToken == runtime:getInvocationContext().authenticationContext.authToken;
}
