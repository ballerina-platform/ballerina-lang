@test:Config {}
function ${testServiceFunctionName}() {
    http:Client ${endpointName} = new(${serviceUriStrName});

${resources}
}
