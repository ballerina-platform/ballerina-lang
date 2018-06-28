import ballerina/io;

type Response record {
    Status|error status;
};

type Status record {
    string message;
    int code;
};

function main(string... args) {
    error e = { message: "response error" };
    Response|error firstResponse = e;

    // Navigate the fields, by lifting the error. 
    // Using the `!` operator stops the navigation if the value returned is `error`, and then
    // assigns that to the `code` variable.
    int|error statusCode1 = firstResponse!status!code;
    io:println("The status code: ", statusCode1);


    // Consider a scenario where the `secondResponse` is null.
    Response|error|() secondResponse = ();

    // The error lifting operator lifts null by default. If the `secondResponse`
    // is null, it stops navigating the rest of the fields and the value
    // of the `secondResponse!status!code` expression evaluates to null.
    int|error|() statusCode2 = secondResponse!status!code;
    io:println("The status code: ", statusCode2);
}
