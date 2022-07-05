import ballerina/module1;

public listener module1:Listener l1 = new(9090);

type TestObject object {
    
};

service TestObject on module1:

function testFunction() {
}
