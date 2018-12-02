@test:Config
function ${testServiceFunctionName}() {
    http:Client httpEndpoint = new(${serviceUriStrName});

${resources}
}
