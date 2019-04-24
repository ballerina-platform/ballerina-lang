import ballerina/http;
import ballerina/log;

// Define the OAuth2 client endpoint to call the backend services.
// Setting the `scheme: http:OAUTH2` enables OAuth2 authentication.
// Setting the `grantType: http:CLIENT_CREDENTIALS_GRANT` configures the
// OAuth2 client credentials grant type.
http:Client clientEP1 = new("https://api.bitbucket.org/2.0", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:CLIENT_CREDENTIALS_GRANT,
                config: {
                    tokenUrl: "https://bitbucket.org/site/oauth2/access_token",
                    clientId: "mMNWS9PLmM93V5WHjC",
                    clientSecret: "jLY6xPY3ER4bNTspaGu6fb7kahhs7kUa"
                }
            }
        }
    });

// Define the OAuth2 client endpoint to call the backend services.
// Setting the `scheme: http:OAUTH2` to enable OAuth2 authentication.
// Setting the `grantType: http:PASSWORD_GRANT` configures the
// OAuth2 password grant type.
// If the access token expires or become invalid, then it will be automatically
// refreshed with the provided `refreshUrl`.
http:Client clientEP2 = new("https://api.bitbucket.org/2.0", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:PASSWORD_GRANT,
                config: {
                    tokenUrl: "https://bitbucket.org/site/oauth2/access_token",
                    username: "b7a.demo@gmail.com",
                    password: "ballerina",
                    clientId: "mMNWS9PLmM93V5WHjC",
                    clientSecret: "jLY6xPY3ER4bNTspaGu6fb7kahhs7kUa",
                    refreshConfig: {
                        refreshUrl: "https://bitbucket.org/site/oauth2/access_token"
                    }
                }
            }
        }
    });

// Define the OAuth2 client endpoint to call the backend services.
// Setting the `scheme: http:OAUTH2`enables OAuth2 authentication.
// Setting the `grantType: http:DIRECT_TOKEN` configures the
// OAuth2 direct token mode.
// If the`accessToken` is invalid or not provided, it will be automatically
// refreshed with the provided `clientId`, `clientSecret`, `refreshToken`,
// and `refreshUrl`.
http:Client clientEP3 = new("https://www.googleapis.com/tasks/v1", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:DIRECT_TOKEN,
                config: {
                    accessToken: "ya29.GlvQBkqJS0yn0zsZm4IIUUzLk3DH1rRiCMKnHiz6deycKmTFiDsuoFlFfrmXF8dCb0gyzLyXpnv3VcrIlauj3nMs61CbydaAqMl6RwVIU2r2qg1StVVvxRWT9_Or",
                    refreshConfig: {
                        clientId: "506144513496-dqm5vdqfrfhdjjom10rmvafb8e3h7rtm.apps.googleusercontent.com",
                        clientSecret: "3hw2XN4MfiIRrv6mghX6m5gM",
                        refreshToken: "1/UwH3YyYccKTrH9bqj35Y7hMYTK9f3HEC3uzlrleFwPE",
                        refreshUrl: "https://www.googleapis.com/oauth2/v4/token"
                    }
                }
            }
        }
    });

public function main() {
    // Send a `GET` request to the specified endpoint.
    var response1 = clientEP1->get("/repositories/b7ademo");
    if (response1 is http:Response) {
        var result = response1.getJsonPayload();
        log:printInfo(result is error ? "Failed to retrieve payload for clientEP1."
                                        : <string> result.values[0].uuid);
    } else {
        log:printError("Failed to call the endpoint.", err = response1);
    }

    // Send a `GET` request to the specified endpoint.
    var response2 = clientEP2->get("/repositories/b7ademo");
    if (response2 is http:Response) {
        var result = response2.getJsonPayload();
        log:printInfo((result is error) ? "Failed to retrieve payload for clientEP2."
                                        : <string> result.values[0].uuid);
    } else {
        log:printError("Failed to call the endpoint.", err = response2);
    }

    // Send a `GET` request to the specified endpoint.
    var response3 = clientEP3->get("/users/@me/lists");
    if (response3 is http:Response) {
        var result = response3.getJsonPayload();
        log:printInfo((result is error) ? "Failed to retrieve payload for clientEP3."
                                        : <string> result.kind);
    } else {
        log:printError("Failed to call the endpoint.", err = response3);
    }
}
