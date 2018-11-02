import ballerina/io;
import ballerina/http;

endpoint http:Client client {
    url: "http://example.com/"
};

function main(string... args) {

    var res1 = client -> get("/", message = ());
    
    string s1 = check res1!
    io:println(s1);

    http:Response? res2;
    res2 = check res1;

    io:println(s1);
}