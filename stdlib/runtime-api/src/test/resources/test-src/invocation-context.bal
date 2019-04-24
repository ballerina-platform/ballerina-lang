import ballerina/runtime;

function testInvocationId() returns (string) {
    return runtime:getInvocationContext().id;
}

function testUserId() returns (boolean) {
    string userId = "124876jk23i4";
    runtime:getInvocationContext().principal.userId = userId;
    return userId == runtime:getInvocationContext().principal.userId;
}

function testUsername() returns (boolean) {
    string username = "tom";
    runtime:getInvocationContext().principal.username = username;
    return username == runtime:getInvocationContext().principal.username;
}

function testUserClaims() returns (boolean) {
    map<any> claims = { email: "tom@ballerina.com", org: "wso2" };
    runtime:getInvocationContext().principal.claims = claims;
    if (runtime:getInvocationContext().principal.claims.hasKey("email")) {
        string emailInContext = "";
        var result = runtime:getInvocationContext().principal.claims["email"];
        emailInContext = <string>result;

        return "tom@ballerina.com" == emailInContext;
    }
    return false;
}

function testAllowedScopes() returns (boolean) {
    string[] scopes = ["email", "profile"];
    runtime:getInvocationContext().principal.scopes = scopes;
    return "email" == runtime:getInvocationContext().principal.scopes[0];
}

function testAuthType() returns (boolean) {
    string authType = "JWT";
    runtime:getInvocationContext().authenticationContext.scheme = authType;
    return authType == runtime:getInvocationContext().authenticationContext.scheme;
}

function testAuthToken() returns (boolean) {
    string authToken = "abc.xyz.pqr";
    runtime:getInvocationContext().authenticationContext.authToken = authToken;
    return authToken == runtime:getInvocationContext().authenticationContext.authToken;
}

function testAttributes() returns boolean {
    string attributeName = "attributeName";
    string attributeValue = "attributeValue";
    string jsonAttributeName = "jsonAttribute";
    json jsonAttribute = { name: "value" };
    runtime:getInvocationContext().attributes[attributeName] = attributeValue;
    runtime:getInvocationContext().attributes[jsonAttributeName] = jsonAttribute;
    return (attributeValue === runtime:getInvocationContext().attributes[attributeName]) &&
        (jsonAttribute === runtime:getInvocationContext().attributes[jsonAttributeName]);
}
