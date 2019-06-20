import ballerina/io;

type Response record {
    Status|error status;
};

type Status record {
    string message;
    int code;
};

public function main() {
    error e = error("response error");
    Response|error firstResponse = e;

    // Navigates through the fields by lifting the `error`.
    // Using the `!` operator stops the navigation if the value returned is `error`. 
    // Then, assigns the `error` to the `statusCode1` variable.
    int|error statusCode1 = firstResponse!status!code;
    io:println("The status code: ", statusCode1);


    // The below is a scenario in which the `secondResponse` is `nil`.
    Response|error|() secondResponse = ();

    // The error-lifting operator lifts `nil` by default. If the `secondResponse`
    // is nil, it stops navigating to the rest of the fields and the value
    // of the `secondResponse!status!code` expression evaluates to `nil`.
    int|error|() statusCode2 = secondResponse!status!code;
    io:println("The status code: ", statusCode2);
}
