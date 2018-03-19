import ballerina.runtime;

function testUserId () (boolean) {
    string userId = "124876jk23i4";
    runtime:getAuthenticationContext().setUserId(userId);
    return userId == runtime:getAuthenticationContext().getUserId();
}

function testUsername () (boolean) {
    string username = "tom";
    runtime:getAuthenticationContext().setUsername(username);
    return username == runtime:getAuthenticationContext().getUsername();
}

function testUserGroups () (boolean) {
    string[] groups = ["admin", "publisher"];
    runtime:getAuthenticationContext().setUserGroups(groups);
    return "admin" == runtime:getAuthenticationContext().getUserGroups()[0];
}

function testUserClaims () (boolean) {
    map claim = {email:"tom@ballerina.com", org:"wso2"};
    runtime:getAuthenticationContext().setUserClaims(claim);
    var email1, _ = (string)runtime:getAuthenticationContext().getUserClaims().email;
    return "tom@ballerina.com" == email1;
}

function testAllowedScopes () (boolean) {
    string[] scopes = ["email", "profile"];
    runtime:getAuthenticationContext().setAllowedScopes(scopes);
    return "email" == runtime:getAuthenticationContext().getAllowedScopes()[0];
}

function testAuthType () (boolean) {
    string authType = "JWT";
    runtime:getAuthenticationContext().setAuthType(authType);
    return authType == runtime:getAuthenticationContext().getAuthType();
}

function testAuthToken () (boolean) {
    string authToken = "abc.xyz.pqr";
    runtime:getAuthenticationContext().setAuthToken(authToken);
    return authToken == runtime:getAuthenticationContext().getAuthToken();
}
