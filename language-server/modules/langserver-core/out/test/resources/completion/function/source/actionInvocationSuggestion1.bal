import ballerina/auth;
import ballerina/http;

auth:OutboundBasicAuthProvider basicAuthProvider = new({ username: "postman", password: "password" });
http:BasicAuthHandler basicAuthHandler = new(basicAuthProvider);

http:ClientConfiguration conf = {
   url: "https://postman-echo.com/basic-auth",
   auth: {
       authHandler: basicAuthHandler
   }
};

function testFunction () {
    http:Client httpClient = new ("http://localhost:8080", config = conf );
    httpClient->
}