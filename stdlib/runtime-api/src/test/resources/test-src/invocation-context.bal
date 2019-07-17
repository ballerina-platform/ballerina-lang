import ballerina/runtime;

function testInvocationId() returns (string) {
    return runtime:getInvocationContext().id;
}

function testUserId() returns (boolean) {
    string userId = "124876jk23i4";
    runtime:getInvocationContext().principal.userId = userId;
    runtime:Principal? testPrincipal = runtime:getInvocationContext()?.principal;
    if (testPrincipal is runtime:Principal) {
        return userId == testPrincipal.userId;
    }
    return false;
}

function testUsername() returns (boolean) {
    string username = "tom";
    runtime:getInvocationContext().principal.username = username;
    runtime:Principal? testPrincipal = runtime:getInvocationContext()?.principal;
    if (testPrincipal is runtime:Principal) {
        return username == testPrincipal.username;
    }
    return false;
}

function testUserClaims() returns (boolean) {
    map<any> claims = { email: "tom@ballerina.com", org: "wso2" };
    runtime:getInvocationContext().principal.claims = claims;
    runtime:Principal? testPrincipal = runtime:getInvocationContext()?.principal;
    if (testPrincipal is runtime:Principal) {
        if(testPrincipal.claims.hasKey("email")) {
            string emailInContext = "";
            var result = testPrincipal.claims["email"];
            emailInContext = <string>result;
            return "tom@ballerina.com" == emailInContext;
        }
        return false;
    }
    return false;
}

function testAllowedScopes() returns (boolean) {
    string[] scopes = ["email", "profile"];
    runtime:getInvocationContext().principal.scopes = scopes;
    runtime:Principal? testPrincipal = runtime:getInvocationContext()?.principal;
    if (testPrincipal is runtime:Principal) {
        return "email" == testPrincipal.scopes[0];
    }
    return false;
}

function testAuthType() returns (boolean) {
    string authType = "JWT";
    runtime:getInvocationContext().authenticationContext.scheme = authType;
    runtime:AuthenticationContext? authContext = runtime:getInvocationContext()?.authenticationContext;
    if (authContext is runtime:AuthenticationContext) {
        return authType == authContext.scheme;
    }
    return false;
}

function testAuthToken() returns (boolean) {
    string authToken = "abc.xyz.pqr";
    runtime:getInvocationContext().authenticationContext.authToken = authToken;
    runtime:AuthenticationContext? authContext = runtime:getInvocationContext()?.authenticationContext;
    if (authContext is runtime:AuthenticationContext) {
        return authToken == authContext.authToken;
    }
    return false;
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
