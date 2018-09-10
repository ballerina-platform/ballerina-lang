import ballerina/http;

xmlns "http://ballerina.com/aa" as ns0;

annotation<service, resource> customAnnotation;

endpoint http:Listener listener {
    port: 9090    
};

function customTestFunction () {
    int a = 12;
}

service<http:Service> customService bind { port: 9090 } {
    customResource (endpoint caller, http:Request request) {
        int b = 12;
    }
}

type customObject object {
    int field1;
    int field2;
};

type customRecord record {
    int rField1;
    int rField2;
};

function terminationFunction() {
    int fieldWithinFunction = 12;
    int num1 = 12;
    int testBinary = num1 + 10;
    int testBraced = (num1 + 12) * 10;
    strVal = "Sri Lanka";
    var intResult = <int>strVal;
    
}
