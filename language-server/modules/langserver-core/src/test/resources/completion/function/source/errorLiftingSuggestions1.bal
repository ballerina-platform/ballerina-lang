import ballerina/auth;
import ballerina/http;
import ballerina/io;

auth:OutboundBasicAuthProvider basicAuthProvider = new({ username: "postman", password: "password" });
http:BasicAuthHandler basicAuthHandler = new(basicAuthProvider);

http:ClientConfiguration conf = {
   auth: {
       authHandler: basicAuthHandler
   }

};

public function main(string... args) {
    http:Client httpClient = new ("http://localhost:8080", config = conf );
    var res1 = httpClient->get("/", message = ());
    
    string s1 = check res1!
    io:println(s1);
}