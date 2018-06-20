import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {

    json jsonRes1 = {
        "args": {
            "test": "123"
        },
        "headers": {
            "host": "postman-echo.com",
            "user-agent": "ballerina/0.970.0-alpha4-SNAPSHOT",
            "x-forwarded-port": "443",
            "x-forwarded-proto": "https"
        },
        "url": "https://postman-echo.com/get?test=123"
    };

    json jsonRes2 = {
        "args": {},
        "data": "POST: Hello World",
        "files": {},
        "form": {},
        "headers": {
            "host": "postman-echo.com",
            "content-length": "17",
            "content-type": "text/plain",
            "user-agent": "ballerina/0.970.0-alpha4-SNAPSHOT",
            "x-forwarded-port": "443",
            "x-forwarded-proto": "https" },
        "json": null,
        "url": "https://postman-echo.com/post"
    };

    json jsonRes3 = {
        "args": {},
        "data": "{\"method\":\"PUT\",\"payload\":\"Hello World\"}",
        "files": {},
        "form": {},
        "headers": {
            "host": "postman-echo.com",
            "content-length": "40",
            "content-type": "text/plain",
            "user-agent": "ballerina/0.970.0-alpha4-SNAPSHOT",
            "x-forwarded-port": "443",
            "x-forwarded-proto": "https"
        },
        "json": null,
        "url": "https://postman-echo.com/put"
    };

    json jsonRes4 = {
        "args": {},
        "data": "{}",
        "files": {},
        "form": {},
        "headers": {
            "host": "postman-echo.com",
            "content-length": "2",
            "content-type": "text/plain",
            "user-agent": "ballerina/0.970.0-alpha4-SNAPSHOT",
            "x-forwarded-port": "443",
            "x-forwarded-proto": "https"
        },
        "json": null,
        "url": "https://postman-echo.com/patch"
    };

    json jsonRes5 = {
        "args": {},
        "data": "DE",
        "files": {},
        "form": {},
        "headers": {
            "host": "postman-echo.com",
            "content-length": "2",
            "content-type": "text/plain",
            "user-agent": "ballerina/0.970.0-alpha4-SNAPSHOT",
            "x-forwarded-port": "443",
            "x-forwarded-proto": "https"
        },
        "json": null,
        "url": "https://postman-echo.com/delete"
    };

    // Invoking the main function
    main();
    test:assertEquals("GET request:", outputs[0]);
    test:assertEquals(jsonRes1, outputs[1]);
    test:assertEquals("\nPOST request:", outputs[2]);
    test:assertEquals(jsonRes2, outputs[3]);
    test:assertEquals("\nPUT request:", outputs[4]);
    test:assertEquals(jsonRes3, outputs[5]);
    test:assertEquals("\nPATCH request:", outputs[6]);
    test:assertEquals(jsonRes4, outputs[7]);
    test:assertEquals("\nDELETE request:", outputs[8]);
    test:assertEquals(jsonRes5, outputs[9]);
}
