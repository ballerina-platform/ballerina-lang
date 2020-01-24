import ballerina/http;
import ballerina/log;
import ballerina/oauth2;

// Define the OAuth2 client endpoint to call the backend services.
// The OAuth2 authentication with client credentials grant type is enabled by
// creating an `oauth2:OutboundOAuth2Provider` with the relevant configurations
// passed as a record.
oauth2:OutboundOAuth2Provider oauth2Provider1 = new({
    tokenUrl: "<Token URL for the authorization endpoint>",
    clientId: "<Client ID for the client credentials grant authentication>",
    clientSecret: "<Client secret for the client credentials grant authentication>",
    clientConfig: {
        secureSocket: {
            trustStore: {
                 path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                 password: "ballerina"
            }
        }
    }
});
http:BearerAuthHandler oauth2Handler1 = new(oauth2Provider1);

http:Client clientEP1 = new("<URL of the secured endpoint>", {
    auth: {
        authHandler: oauth2Handler1
    },
    secureSocket: {
        trustStore: {
             path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
             password: "ballerina"
        }
    }
});

// Defines the OAuth2 client endpoint to call the backend services.
// The OAuth2 authentication with the password grant type is enabled by
// creating an `oauth2:OutboundOAuth2Provider` with the relevant
// configurations passed as a record. If the access token expires or
// becomes invalid, then it will be automatically refreshed with the provided
// `refreshConfig`.
oauth2:OutboundOAuth2Provider oauth2Provider2 = new({
    tokenUrl: "<Token URL for the authorization endpoint>",
    username: "<Username for password grant authentication>",
    password: "<Password for password grant authentication>",
    clientId: "<Client ID for password grant authentication>",
    clientSecret: "<Client secret for password grant authentication>",
    clientConfig: {
        secureSocket: {
            trustStore: {
                 path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                 password: "ballerina"
            }
        }
    },
    refreshConfig: {
        refreshUrl: "<Refresh token URL for the refresh token server>",
        clientConfig: {
            secureSocket: {
                trustStore: {
                     path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                     password: "ballerina"
                }
            }
        }
    }
});
http:BearerAuthHandler oauth2Handler2 = new(oauth2Provider2);

http:Client clientEP2 = new("<URL of the secured endpoint>", {
    auth: {
        authHandler: oauth2Handler2
    },
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

// Defines the OAuth2 client endpoint to call the backend services.
// The OAuth2 authentication with direct token mode is enabled by creating
// an `oauth2:OutboundOAuth2Provider` with the relevant configurations passed
// as a record. If the `accessToken` is invalid or not provided, it will
// be automatically refreshed with the provided `refreshConfig`.
oauth2:OutboundOAuth2Provider oauth2Provider3 = new({
    accessToken: "<Access token for the authorization endpoint>",
    refreshConfig: {
        clientId: "<Client ID for authentication with the authorization endpoint>",
        clientSecret: "<Client secret for authentication with the authorization endpoint>",
        refreshToken: "<Refresh token for the refresh token server>",
        refreshUrl: "<Refresh token URL for the refresh token server>",
        clientConfig: {
            secureSocket: {
                trustStore: {
                     path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                     password: "ballerina"
                }
            }
        }
    }
});
http:BearerAuthHandler oauth2Handler3 = new(oauth2Provider3);

http:Client clientEP3 = new("<URL of the secured endpoint>", {
    auth: {
        authHandler: oauth2Handler3
    },
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

public function main() {
    // Sends a `GET` request to the specified endpoint.
    var response1 = clientEP1->get("/");
    if (response1 is http:Response) {
        var result = response1.getJsonPayload();
        if (result is json) {
            log:printInfo(result.toJsonString());
        } else {
            log:printError("Failed to retrieve payload for clientEP1.");
        }
    } else {
        log:printError("Failed to call the endpoint from clientEP1.", response1);
    }

    // Send a `GET` request to the specified endpoint.
    var response2 = clientEP2->get("/");
    if (response2 is http:Response) {
        var result = response2.getJsonPayload();
        if (result is json) {
            log:printInfo(result.toJsonString());
        } else {
            log:printError("Failed to retrieve payload for clientEP2.");
        }
    } else {
        log:printError("Failed to call the endpoint from clientEP2.", response2);
    }

    // Send a `GET` request to the specified endpoint.
    var response3 = clientEP3->get("/");
    if (response3 is http:Response) {
        var result = response3.getJsonPayload();
        if (result is error) {
            log:printInfo("Failed to retrieve payload for clientEP3.");
        } else {
            log:printInfo(<string> result.kind);
        }
    } else {
        log:printError("Failed to call the endpoint from clientEP3.", response3);
    }
}
