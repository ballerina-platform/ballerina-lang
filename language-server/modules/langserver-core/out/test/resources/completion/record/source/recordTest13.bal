import ballerina/http;

http:Client simpleClient = new("http://localhost:9090", {
    secureSocket: {
        keyStore: {
            
        }
    }
});