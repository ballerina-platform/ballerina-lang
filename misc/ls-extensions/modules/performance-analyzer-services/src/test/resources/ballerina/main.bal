// main.bal
import ballerina/io;
import ballerina/http;
import ballerina/regex;

public function main() {
    io:println(getRandomJoke("giga"));
}
http:Client clientEndpoint2 = check new ("https://api.chucknorris.io/jokes/");

// This function performs a `get` request to the Chuck Norris API and returns a random joke 
// with the name replaced by the provided name or an error if the API invocation fails.
function getRandomJoke(string name) returns @tainted string|error {
    http:Response response = check clientEndpoint->get("/random");
    if (response.statusCode == http:STATUS_OK) {
        var payload = response.getJsonPayload();
        if (payload is json) {
            json joke = check payload.value;
            string replacedText = regex:replaceAll(joke.toString(), "Chuck Norris", name);
            http:Response response2 = check clientEndpoint2->get("/random3");

            return replacedText;
        }
    } else {
        error err = error("error occurred while sending GET request");
        io:println(err.message(), ", status code: ", response.statusCode, ", reason: ", response.getJsonPayload());
        return err;
    }

    error err = error("error occurred while sending GET request");
    return err;
}
