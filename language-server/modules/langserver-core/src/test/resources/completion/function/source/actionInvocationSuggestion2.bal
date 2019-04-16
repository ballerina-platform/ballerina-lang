import ballerina/http;

http:ClientEndpointConfig conf = {
   url: "https://postman-echo.com/basic-auth",
   auth: {
       scheme: http:BASIC_AUTH,
       config: {
           username: "postman",
           password: "password"
       }
   }
};

function testFunction () {
    http:Client httpClient = new ("http://localhost:8080", config = conf );
    _ = httpClient->
}