import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... a) {
    outputs[counter] = a[0];
    counter++;
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

    json jsonRes3 = {
        "args": {},
        "data": { "method": "PUT", "payload": "Hello World" },
        "files": {},
        "form": {},
        "json": {"method":"PUT","payload":"Hello World"},
        "url": "https://postman-echo.com/put"
    };

    json jsonRes4 = {
        "args": {},
        "data": "<request>\n                        <method>PATCH</method>\n                        <payload>Hello World!</payload>\n                      </request>",
        "files": {},
        "form": {},
        "json": null,
        "url": "https://postman-echo.com/patch"
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
    json res = check <json>outputs[1];
    res.remove("headers");
    test:assertEquals(res, jsonRes1);

    test:assertEquals(outputs[2], "\nPOST request:");

    res = check <json>outputs[3];
    res.remove("headers");
    test:assertEquals(res, jsonRes2);

    test:assertEquals(outputs[4], "\nPUT request:");

    res = check <json>outputs[5];
    res.remove("headers");
    test:assertEquals(res, jsonRes3);

    test:assertEquals(outputs[6], "\nPATCH request:");

    res = check <json>outputs[7];
    res.remove("headers");
    test:assertEquals(res, jsonRes4);

    test:assertEquals(outputs[8], "\nDELETE request:");

    res = check <json>outputs[9];
    res.remove("headers");
    test:assertEquals(res, jsonRes5);
}
