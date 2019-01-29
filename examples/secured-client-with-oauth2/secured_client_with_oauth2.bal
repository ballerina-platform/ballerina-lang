import ballerina/http;
import ballerina/log;

// Define the OAuth2 client endpoint to call the backend services.
// OAuth2 authentication is enabled by setting the `scheme: http:OAUTH2`
// If `accessToken` is invalid, it will be automatically refreshed with the
// provided `clientId`, `clientSecret`, `refreshToken`, `refreshUrl`.
http:Client httpEndpoint = new("https://www.googleapis.com/tasks/v1", config = {
    auth: {
        scheme: http:OAUTH2,
        accessToken: "ya29.GlufBimE7JZdiB_FpFtZn7p1WMtloVeMlqiYXDGF97068VvJCyK8rEFqBBkxT10E0qudipwxTjJTkU4we0hbOcHKjNTXz6JTEZYoRVn7F3-0O_bL9g71Rwek7TFI",
        clientId: "833478926540-va43h2lhdhfc06i9eivlmaehl3o5uk1i.apps.googleusercontent.com",
        clientSecret: "4ZsV4gwSuIoRdy6TKUXTanlw",
        refreshToken: "1/XUtrd8DaeoopmX5xpIvGdXY09VAY6_h8fVVj9xCaKJE",
        refreshUrl: "https://www.googleapis.com/oauth2/v4/token"
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
