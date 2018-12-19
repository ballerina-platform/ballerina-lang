import ballerina/http;

function testTypeGuard() {
    int| string| http:Response| TestRecord testUnion = 1;

    if (testUnion is int) {
        
    } else if (testUnion is string) {
        
    } else if (testUnion is http:Response) {
    
    } else {
        int testInt = 111;
        testUnion.
    }
}

type TestRecord record {
    int field1 = 12;
    string field2 = "hello";
};
