import ballerina.net.http;

function main (string[] args) {
    http:ClientConnector con = create http:ClientConnector(<caret>)
}
