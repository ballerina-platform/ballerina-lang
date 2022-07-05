import ballerina/io;
import ballerina/http;

service / on new http:Listener(9090) {
    resource function get greeting(http:Request req) returns string|error? {
        http:Client clientEP = check new ("https://postman-echo.com/post");
        json clientResponse = check clientEP->forward("/", req);

        io:println("Worker execution started");

        worker w1 {
            do {
                json clientResponse2 = check clientEP->forward("/", req);

            } on fail var e {
                io:print(e);
            }

        }

        worker w2 {

            int n = 10000000;
            int sum = 0;
            foreach var i in 1 ... n {
                sum += i * i;
            }
            io:println("sum of squares of first ", n,
            " positive numbers = ", sum);
        }

        _ = wait {w1, w2};

        io:println("Worker execution finished");
        return "true";
    }
}
