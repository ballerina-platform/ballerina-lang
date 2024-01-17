
import ballerina/module1;


listener module1:Listener testListener = new(9090);


service on testListener {

}


function testFunction() {

}


type TestType int;


class TestClass {

}


int testModVar = 10;


const int testConst = 10;


enum testEnum {
    ONE = "1",
    TWO = "2"
}


xmlns "www.wso2.com" as ns;


annotation Person testAnnotation on class;
