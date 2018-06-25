import ballerina/config;
import ballerina/http;
import ballerina/test;
import ballerina/runtime;

boolean serviceStarted;

function startService() {
    serviceStarted = test:startServices("secured-service-with-jwt");
}

@test:Config {
    enable: true,
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Check whether the server has started. 
    test:assertTrue(serviceStarted, msg = "Unable to start the service");
    setJwtTokenToAuthContext();
    testAuthSuccess();
    clearTokenFromAuthContext();
    setInvalidJwtTokenToAuthContext();
    testAuthnFailure();
    clearTokenFromAuthContext();
    setJwtTokenWithNoScopesToAuthContext();
    testAuthzFailure();
    clearTokenFromAuthContext();
}

function testAuthSuccess() {
    // create client
    endpoint http:Client httpEndpoint {
        url: "https://localhost:9090",
        auth: { scheme: http:JWT_AUTH }
    };
    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.statusCode, 200,
                msg = "Expected status code 200 not received");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function testAuthnFailure() {
    // Create a client.
    endpoint http:Client httpEndpoint {
        url: "https://localhost:9090",
        auth: { scheme: http:JWT_AUTH }
    };
    // Send a `GET` request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.statusCode, 401,
                msg = "Expected status code 401 not received");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function testAuthzFailure() {
    // Create a client.
    endpoint http:Client httpEndpoint { url: "https://localhost:9090",
        auth: { scheme: http:JWT_AUTH } };
    // Send a `GET` request to the specified endpoint
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => {
            test:assertEquals(resp.statusCode, 403, msg =
                "Expected status code 403 not received");
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function setJwtTokenToAuthContext () {
    string token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJ" +
        "pbmEiLCJpc3MiOiJiYWxsZXJpbmEiLCJleHAiOjI4MTg0MTUwMTksImlhdCI6MTUyND" +
        "U3NTAxOSwianRpIjoiZjVhZGVkNTA1ODVjNDZmMmI4Y2EyMzNkMGMyYTNjOWQiLCJhdW" +
        "QiOlsiYmFsbGVyaW5hIiwiYmFsbGVyaW5hLm9yZyIsImJhbGxlcmluYS5pbyJdLCJzY" +
        "29wZSI6ImhlbGxvIn0.bNoqz9_DzgeKSK6ru3DnKL7NiNbY32ksXPYrh6Jp0_O3ST7W" +
        "fXMs9WVkx6Q2TiYukMAGrnMUFrJnrJvZwC3glAmRBrl4BYCbQ0c5mCbgM9qhhCjC1tB" +
        "A50rjtLAtRW-JTRpCKS0B9_EmlVKfvXPKDLIpM5hnfhOin1R3lJCPspJ2ey_Ho6fDhs" +
        "KE3DZgssvgPgI9PBItnkipQ3CqqXWhV-RFBkVBEGPDYXTUVGbXhdNOBSwKw5ZoVJrCU" +
        "iNG5XD0K4sgN9udVTi3EMKNMnVQaq399k6RYPAy3vIhByS6QZtRjOG8X93WJw-9GLiH" +
        "vcabuid80lnrs2-mAEcstgiHVw";
    runtime:getInvocationContext().authContext.scheme = "jwt";
    runtime:getInvocationContext().authContext.authToken = token;
}

function clearTokenFromAuthContext () {
    runtime:getInvocationContext().authContext.scheme = "jwt";
    runtime:getInvocationContext().authContext.authToken = "";
}

function setInvalidJwtTokenToAuthContext () {
    string token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJp" +
        "bmFJbnZhbGlkIiwiaXNzIjoiYmFsbGVyaW5hSW52YWxpZCIsImV4cCI6MjgxODQxNTAxOSwia" +
        "WF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTIzM2QwYzJhM2M5ZC" +
        "IsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInN" +
        "jb3BlIjoiaGVsbG8ifQ.OO2Etxd8oOo7igA4Cz2kida3ozO6ri6_YlrKf2IVBd2aNoNrub8Ax" +
        "fxpNJc_gUogOTrCGjTC-AmmGZc4if3yZqgeCogIg1sT-0fI3TAFih9r8FNeFAfb8e4KO8Yd0z" +
        "aPWGUnUoIExjYxrBMLGUTzMaM1knyI8agG7z6nKm0ZBMdti1AphGkqH50rDm9Arjvy256aNO-" +
        "cw6lWkDneZl5WdV63RGNNNSj8ElyRW6HMdLmHQ3HIkQ4f1K8tCshwgbyb19bw8nCeYihpPeOn" +
        "gVobfGY2yXm7QGjmiVInALAqisylo348WB6qOKduDrbDZYcFDKQuYConx5wF-7Wl9hg2HA";
    runtime:getInvocationContext().authContext.scheme = "jwt";
    runtime:getInvocationContext().authContext.authToken = token;
}

function setJwtTokenWithNoScopesToAuthContext () {
    string token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJp" +
        "bmEuaW8iLCJpc3MiOiJiYWxsZXJpbmEiLCJleHAiOjI4MTg0MTUwMTksImlhdCI6MTUy" +
        "NDU3NTAxOSwianRpIjoiZjVhZGVkNTA1ODVjNDZmMmI4Y2EyMzNkMGMyYTNjOWQiLCJh" +
        "dWQiOlsiYmFsbGVyaW5hIiwiYmFsbGVyaW5hLm9yZyIsImJhbGxlcmluYS5pbyJdfQ.R" +
        "y1ZJJzve3oTdF3PCvGDWXYWb4ab9CHzY6cghmqQ2h2epIuFVZOVsi1MqI_cqLa9ZJBZq3" +
        "aMznSRjz6hkOldibxi46j_ebGIyoyABTLeBS1P67oCG790TxdS1tThYGJXvkCeECYVH_i" +
        "NhJRyht0GSa59VhonCFIAL505_u5vfO4fhmCjslYCr6WcpYW1tLf-vDmRLIqshYX7RZkK" +
        "Es2a1pfjg5XkJiJSxqQ_-lLzeQfb-eMmZzT5ob-cE9qpBhjrXoYpYLy371TtuOdREdhXh" +
        "Ogu12RJMaCE1FlA1ZoyLrmzj2Mm3RHc_A88lKoGvaEBcGzJwllekuQeDUJ1P90SGA";
    runtime:getInvocationContext().authContext.scheme = "jwt";
    runtime:getInvocationContext().authContext.authToken = token;
}

function stopService() {
    test:stopServices("secured-service-with-jwt");
}
