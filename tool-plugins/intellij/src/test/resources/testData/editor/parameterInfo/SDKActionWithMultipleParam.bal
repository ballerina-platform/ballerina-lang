import ballerina/http;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
         create http:HttpClient(<caret>);
    }
}
