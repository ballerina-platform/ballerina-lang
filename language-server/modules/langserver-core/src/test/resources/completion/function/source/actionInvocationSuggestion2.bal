import ballerina/http;

endpoint http:Client nyseEP {
    url:"http://localhost:9090"
};

function testFunction () {
    _ = nyseEP->
}