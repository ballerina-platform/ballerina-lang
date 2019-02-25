import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... a) {
    outputs[counter] = a[0];
    counter += 1;
}

@test:Config
function testFunc() {

    json jsonRes1 = {
        "args": {
            "test": "123"
        },
        "url": "https://postman-echo.com/get?test=123"
    };

    json jsonRes2 = {
        "args": {},
        "data": "POST: Hello World",
        "files": {},
        "form": {},
        "json": null,
        "url": "https://postman-echo.com/post"
    };

    json jsonRes5 = {
        "args": {},
        "data": "DELETE: Hello World",
        "files": {},
        "form": {},
        "json": null,
        "url": "https://postman-echo.com/delete"
    };

    // Invoking the main function
    main();

    test:assertEquals(outputs[0], "GET request:");

    // Remove the headers since the user-agent will be different
    // from ballerina version to version.
    json res = <json>outputs[1];
    res.remove("headers");
    test:assertEquals(res, jsonRes1);

    test:assertEquals(outputs[2], "\nPOST request:");

    res = <json>outputs[3];
    res.remove("headers");
    test:assertEquals(res, jsonRes2);

    test:assertEquals(outputs[4], "\nDELETE request:");

    res = <json>outputs[5];
    res.remove("headers");
    test:assertEquals(res, jsonRes5);

    test:assertEquals(outputs[6], "\nUse custom HTTP verbs:");
    test:assertEquals(outputs[7], "Content-Type: application/json; charset=utf-8");
    test:assertEquals(outputs[8], "Status code: 200");
}
