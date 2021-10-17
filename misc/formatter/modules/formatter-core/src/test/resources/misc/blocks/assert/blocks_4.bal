function bar() {
    // hello
}

type Person record {|
    string school;
    // hello
|};

function foo() {
    http:LoadBalanceClient lbBackendEP = new ({
        // Define the set of HTTP clients that need to be load balanced.
        targets: [
            {url: "http://localhost:8080/mock3"}
            // hello
        ],
        timeoutInMillis: 5000
    });
}
