import ballerina.net.http;

const int CONST_INT = 20;
const long CONST_LONG = 2560000;
const float CONST_FLOAT = 23.6;
const double CONST_DOUBLE = 23.6;
const boolean CONST_BOOLEAN = true;
const string CONST_STRING = "Ballerina";

@BasePath ("/echo")
service echo {
    @GET
    @Path ("/message")
    resource echo (message m) {
        testFunction();
        http:convertToResponse(m);
        reply m;
    }
}

function testFunction() {
    int i;
    i =0;
}
