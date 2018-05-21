import ballerina/http;

function testFunction() {
    endpoint http:Listener listener {
        post: 9090,
        
    };
}