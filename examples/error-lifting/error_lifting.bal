import ballerina/io;

type Response {
    Status|error status;
};

type Status {
    string message;
    int code;
};

function main(string... args) {
    error e = { message: "response error" };
    Response|error firstResponse = e;

    // This is how you can navigate the fields, by lifting the error. 
    // Using '!' operator will stop the navigation if the value is error, and 
    // will assign that to the 'code'.
    int|error statusCode1 = firstResponse!status!code;
    io:println("The status code: ", statusCode1);


    // Consider a scenario where the 'secondResponse' is nil.
    Response|error|() secondResponse = ();

    // Error lifting operator also lifts nil by default. If the 'secondResponse'
    // is nil, then it will stop navigating the rest of the field, and the value
    // of the expression 'secondResponse!status!code' will evaluate to nil.
    int|error|() statusCode2 = secondResponse!status!code;
    io:println("The status code: ", statusCode2);
}
