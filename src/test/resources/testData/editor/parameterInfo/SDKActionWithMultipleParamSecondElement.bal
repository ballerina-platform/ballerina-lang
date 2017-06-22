import ballerina.net.http;

function main (string[] args) {
    http:ClientConnector con = create http:ClientConnector("http://localhost:8080");
    http:ClientConnector.get(con,<caret>)
}
