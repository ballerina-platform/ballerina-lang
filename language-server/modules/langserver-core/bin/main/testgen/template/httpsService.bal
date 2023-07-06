@test:Config {}
function ${testServiceFunctionName}() {
    http:Client ${endpointName} = new(${serviceUriStrName}, config = {
        secureSocket: {
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        }
    });

${resources}
}
