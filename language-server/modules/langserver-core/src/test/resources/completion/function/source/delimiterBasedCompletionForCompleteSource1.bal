import ballerina/http;

function testFunction(http:Caller caller, http:Request request) {
    int var1 = 12;
    int var2 = 123;
    string sampleStr = "hello Ballerina";
    string modifiedStr = sampleStr.replace("hello", "Hello").toLower(); 
}