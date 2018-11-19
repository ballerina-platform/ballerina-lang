@test:Config
function ${testServiceFunctionName}() {
    endpoint http:Client httpEndpoint {
    url: ${serviceUriStrName},
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

${resources}
}
