import ballerina/lang.test;

function testServiceType () returns (service) {
    service ts = HelloWorld;
    return ts;
}

service HelloWorld on new test:MockListener (9090) {
    resource function hello () {

    }

    resource function returnError () returns error? {
        error e = error("Service Error");
        return e;
    }
}
