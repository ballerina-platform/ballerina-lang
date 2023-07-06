import ballerina/module1;

function name() {
    module1:Client cli = new("http://localhost:9090");
    
    var resp = check cli->
}
