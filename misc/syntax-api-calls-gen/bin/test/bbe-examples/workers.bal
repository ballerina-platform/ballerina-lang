import ballerina/io;
import ballerina/http;

// The code outside the named workers belong to an implicit 
// default worker. The default worker in each function wil be 
// executed in the same strand as the caller function.
public function main() {
    io:println("Worker execution started");

    // This block belongs to the `w1` worker.
    worker w1 {
        http:Client httpClient = new ("https://api.mathjs.org");
        var response = httpClient->get("/v4/?expr=2*3");
        if response is http:Response {
            io:println("Worker 1 response: ", response.getTextPayload());
        }
    }

    worker w2 {
        http:Client httpClient = new ("https://api.mathjs.org");
        var response = httpClient->get("/v4/?expr=5*7");
        if response is http:Response {
            io:println("Worker 2 response: ", response.getTextPayload());
        }
    }

    // Waits for both workers to finish.
    _ = wait {w1, w2};

    io:println("Worker execution finished");
}
