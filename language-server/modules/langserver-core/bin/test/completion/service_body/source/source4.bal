import ballerina/module1;

service  on new module1:Listener(8080) {
    resource function get foo/[string bar]() {
        string s = bar;
        
    }
}

function testFunction() {
}
