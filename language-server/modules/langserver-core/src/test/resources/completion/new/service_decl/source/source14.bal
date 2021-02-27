import ballerina/module1;

public listener module1:Listener lst = new module1:Listener(23);

type TestObject object {
    
};

service / on lst {
    resource f
}

function testFunction() returns int {
    return 12;
}
