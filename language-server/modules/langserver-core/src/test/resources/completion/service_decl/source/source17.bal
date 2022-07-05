import ballerina/module1;
import ballerina/httpx;

public listener module1:Listener lst = new module1:Listener(23);

type TestObject object {
    
};

p

service TestObject on lst {} 

function testFunction() returns int {
    return 12;
}
