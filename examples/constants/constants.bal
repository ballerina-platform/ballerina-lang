import ballerina/io;

// Constants are defined using the `const` modifier.
const string GET = "GET";

// Constants can be defined without the type using the `var` keyword. Then the type is inferred from the right hand side.
const POST = "POST";

// Constants can be used to create type definitions. The type definition below specifies that a variable of the type
// `ACTION` can only hold either `GET` or `POST`.
type ACTION GET|POST;

// Integer constant.
const int I = 125;

public function main() {

    // This is identical to the code line `ACTION get = "GET";`.
    ACTION get = GET;
    testAction(get);

    ACTION post = "POST";
    testAction(POST);

    // We can concatenate `I` with any other integer value.
    int value = I + 10;
    io:println(value);

}

function testAction(ACTION action) {
    if (action == GET) {
        io:println("GET action");
    } else if (action == POST) {
        io:println("POST action");
    }
}
