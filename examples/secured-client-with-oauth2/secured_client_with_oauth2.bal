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
        accessToken:
        "ya29.GlsKBjESXA-CeTgZjiKtwavNcVi9XNANoU4bjYRXFYR27NkrGTsAM3GoYh5lzrDh_qPej4vf5XI-CZfD7owM8UeVbw2g-C5Ut5qMsst7rdyLGHaHxjwJQe8StEK7",
        clientId: "833478926540-tc35uju9dh5a2286c9pm6u3slp8o2grp.apps.googleusercontent.com",
        clientSecret: "p2RCr49u--267IExHOdtuaMX",
        refreshToken: "1/T3PA_MJqleY6xDVrbAqbTNEZlVHw29V4rWawLN0gkQk",
        refreshUrl: "https://www.googleapis.com/oauth2/v4/token"
    }
};

function main(string... args) {
    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/users/@me/lists/");
    match response {
        http:Response resp => log:printInfo(check resp.getPayloadAsString());
        error err => log:printError("Failed to call the endpoint");
    }
}
