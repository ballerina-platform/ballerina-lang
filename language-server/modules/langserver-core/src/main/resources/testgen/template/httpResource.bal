    // Send a ${resourceMethodAllCaps} request to the specified endpoint
    log:printInfo("Test ${resourceMethodAllCaps} Request");
    var ${responseFieldName} = ${endpointName}->${resourceMethod}("${resourcePath}${additionalParams}");

    if (${responseFieldName} is http:Response) {
        var res = ${responseFieldName}.getTextPayload();
        string expected = "hello";
        test:assertEquals(res, expected);
    } else {
        test:assertFail(msg = "Failed to call the endpoint: " + ${serviceUriStrName});
    }
