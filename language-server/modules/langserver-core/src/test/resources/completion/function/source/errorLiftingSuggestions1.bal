import ballerina/io;
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

function main(string... args) {
    http:Client httpClient = new ("http://localhost:8080", config = conf );
    var res1 = httpClient -> get("/", message = ());
    
    string s1 = check res1!
    io:println(s1);
}