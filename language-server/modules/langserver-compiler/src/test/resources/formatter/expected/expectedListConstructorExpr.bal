import ballerina/http;

http:LoadBalanceClient lbBackendEP = new ({
    // Define the set of HTTP clients that need to be load balanced.
    targets: [
            {url: "http://localhost:8080/mock1"},
            {url: "http://localhost:8080/mock2"},
            {url: "http://localhost:8080/mock3"}
        ],
    timeoutInMillis: 5000
});
