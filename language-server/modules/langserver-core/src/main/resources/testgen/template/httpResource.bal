    // Send a ${resourceMethodAllCaps} request to the specified endpoint
    log:printInfo("Test ${resourceMethodAllCaps} Request");
    var ${responseFieldName} = httpEndpoint->${resourceMethod}("${resourcePath}${additionalParams}");

    match ${responseFieldName} {
        http:Response resp => {
            var jsonRes = resp.getJsonPayload();
            json expected = {"Hello":"World"};
            test:assertEquals(jsonRes, expected);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint: " + ${serviceUriStrName});
    }
