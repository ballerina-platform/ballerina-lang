import ballerina/http;

service foo on new http:Listener(8080) {
    resource function fooRes(http:Caller caller, http:Request request) {
        
    }
}

type TestRec record {
    
};

function name() {
    TestRec.
}