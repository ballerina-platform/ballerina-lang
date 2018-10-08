import ballerina/http;
import ballerina/log;

// Define the OAuth2 client endpoint to call the backend services.
// OAuth2 authentication is enabled by setting the `scheme: http:OAUTH2`
// If `accessToken` is invalid, it will be automatically refreshed with the
// provided `clientId`, `clientSecret`, `refreshToken`, `refreshUrl`.
endpoint http:Client httpEndpoint {
    url: "https://www.googleapis.com/tasks/v1",
    auth: {
        scheme: http:OAUTH2,
        accessToken: "ya29.GlsKBjW1zLmpJQOohUEMjfqn8m1MU_BhkIv4YsQHbLMb8XntaKdg6kkPIi4x22ZksJ2sYFIDtVWTxjiJnkDtUk3ZROe6AVq4EIQRrazNGfeXEGdjBkR0LxIo1D_C",
        clientId: "833478926540-a61oihs15lt3jsf7fq5roeiki0hrk4t9.apps.googleusercontent.com",
        clientSecret: "x3-rqEnDti6lX_tsVelyfjTm",
        refreshToken: "1/XlnjQH5Y4ueEggJWAfwZJUu74nAEwfBtFZNFfCXySYs",
        refreshUrl: "https://www.googleapis.com/oauth2/v4/token"
    }
};

public function main() {
    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/users/@me/lists/");
    match response {
        http:Response resp => log:printInfo(resp.getPayloadAsString() but {error => "Failed to retrieve payload."});
        error err => log:printError("Failed to call the endpoint.");
    }
}
