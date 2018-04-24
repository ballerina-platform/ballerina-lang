import ballerina/io;

type Response {
    Status|error status;
};

type Status {
    string message;
    int code;
};

function main(string... args) {
    error e = {message: "response error"};
    Response|error firstResponse = e;

    // This is how you can navigate the fields, by lifting the error. 
    // Using `!` operator will stop the navigation if the value is error, and will assign that to the `code`.
    int|error code = firstResponse!status!code;

    int statusCode = code but {
        error => -1,
        int a => a
    };
    io:println("The status code: ", statusCode);


    // Consider a
    Response|error|() secondResponse = null;

    // Error lifting operator also lifts nil by default. If the `secondResponse` is null, the it will stop 
    // navigating the rest of the field, and the value of the expression `secondResponse!status!code` will 
    // evaluate to null.
    statusCode = secondResponse!status!code but {
        error => -1,
        () => -2,
        int a => a
    };
    io:println("The status code: ", statusCode);
}
