import ballerina/io;

type Response record {
    Status|error status;
};

type Status record {
    string message;
    int code;
};

public function main() {
    error e = { message: "response error" };
    Response|error firstResponse = e;

    // Navigate the fields, by lifting the error. 
    // Using the `!` operator stops the navigation if the value returned is `error`, and then
    // assigns that to the `statusCode1` variable.
    int|error statusCode1 = firstResponse!status!code;
    io:println("The status code: ", statusCode1);


    // Consider a scenario where the `secondResponse` is nil.
    Response|error|() secondResponse = ();

    // The error lifting operator lifts nil by default. If the `secondResponse`
    // is nil, it stops navigating the rest of the fields and the value
    // of the `secondResponse!status!code` expression evaluates to nil.
    int|error|() statusCode2 = secondResponse!status!code;
    io:println("The status code: ", statusCode2);
}
