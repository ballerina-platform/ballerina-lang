import ballerina/module1;

function testFunction() {
    worker w1 returns module1:a {
        
    }
}

function getMessage() returns string {
    return "Hello World";
}
