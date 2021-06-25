import ballerina/module1;

function createIntWithError() returns int|error {
    return 10;
}

public listener module1:Listener lst = new module1:Listener(23);

service / on lst {
    resource function get getResource() {
        return createIntWithError()
    }
}
