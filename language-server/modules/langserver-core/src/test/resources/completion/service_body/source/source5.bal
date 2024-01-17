import ballerina/module1;

public listener module1:Listener l1 = new(9090);

service / on l1 {
    ini
}

function testFunction() {
}
