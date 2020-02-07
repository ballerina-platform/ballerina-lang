import ballerina/http;

xmlns "http://ballerina.com/aa" as ns0;

annotation customAnnotation on service, resource function;

http:Listener ep = new(9090);

function customTestFunction() {
    int a = 12;
}

service customService on new http:Listener(8080) {
    resource function customResource(http:Caller caller, http:Request request) {
    }
}

type customObject object {
    int field1 = 0;
    int field2 = 0;
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
    string strVal = "Sri Lanka";
    
}
