import ballerina/http;
import ballerina/log;

// Define the OAuth2 client endpoint to call the backend services.
// OAuth2 authentication is enabled by setting the `scheme: http:OAUTH2`
// If `accessToken` is invalid, it will be automatically refreshed with the
// provided `clientId`, `clientSecret`, `refreshToken`, `refreshUrl`.
http:Client httpEndpoint = new("https://www.googleapis.com/tasks/v1", config = {
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
    var response = httpEndpoint->get("/users/@me/lists/");
    if (response is http:Response) {
        var result = response.getPayloadAsString();
        log:printInfo((result is error) ? "Failed to retrieve payload."
                                        : result);
    } else {
        log:printError("Failed to call the endpoint.", err = response);
    }
}
