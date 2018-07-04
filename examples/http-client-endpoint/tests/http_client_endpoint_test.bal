import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/log",
    functionName: "printInfo"
}
public function mockPrint(string msg) {
    outputs[counter] = msg;
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
    test:assertEquals("GET request:", outputs[0]);
    test:assertEquals(jsonRes1.toString(), outputs[1]);
    test:assertEquals("\nPOST request:", outputs[2]);
    test:assertEquals(jsonRes2.toString(), outputs[3]);
    test:assertEquals("\nPUT request:", outputs[4]);
    test:assertEquals(jsonRes3.toString(), outputs[5]);
    test:assertEquals("\nPATCH request:", outputs[6]);
    test:assertEquals(jsonRes4.toString(), outputs[7]);
    test:assertEquals("\nDELETE request:", outputs[8]);
    test:assertEquals(jsonRes5.toString(), outputs[9]);
}
