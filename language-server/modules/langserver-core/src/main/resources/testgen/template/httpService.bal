@test:Config
function ${testServiceFunctionName}() {
    endpoint http:Client httpEndpoint { url: ${serviceUriStrName} };

${resources}
}
