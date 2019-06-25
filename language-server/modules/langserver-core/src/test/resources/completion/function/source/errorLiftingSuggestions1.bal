import ballerina/io;
import ballerina/http;

http:ClientEndpointConfig conf = {
   auth: {
       scheme: http:BASIC_AUTH,
       config: {
           username: "postman",
           password: "password"
       }
   }

};

public function main(string... args) {
    http:Client httpClient = new ("http://localhost:8080", config = conf );
    var res1 = httpClient -> get("/", message = ());
    
    string s1 = check res1!
    io:println(s1);
}