    // Send a ${resourceMethodAllCaps} request to the specified endpoint
    log:printInfo("Test ${resourceMethodAllCaps} Request");
    var ${responseFieldName} = httpEndpoint->${resourceMethod}("${resourcePath}${additionalParams}");

    if (${responseFieldName} is http:Response) {
        var jsonRes = ${responseFieldName}.getJsonPayload();
        json expected = {"Hello":"World"};
        test:assertEquals(jsonRes, expected);
    } else {
        test:assertFail(msg = "Failed to call the endpoint: " + ${serviceUriStrName});
    }
