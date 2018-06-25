import ballerina/http;

endpoint http:Client homer {
    url: "http://www.simpsonquotes.xyz",
    timeoutMillis: 500,
    circuitBreaker: {
        rollingWindow: {
            
        }
    }
};