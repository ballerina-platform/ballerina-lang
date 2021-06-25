import ballerina/http;
import ballerina/log;

// HTTP client configurations associated with [enabling cookies](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/records/CookieConfig.html).
http:ClientConfiguration clientEPConfig = {
    cookieConfig: {
        enabled: true
    }
};

public function main() {
    // Create a new HTTP client by giving the URL and the client configuration.
    http:Client httpClient = new("http://localhost:9095/cookie-demo",
                                  clientEPConfig);

    // Initialize an HTTP request.
    http:Request request = new;

    // Send a username and a password as a JSON payload to the backend.
    json jsonPart = {
        name: "John",
        password: "p@ssw0rd"
    };
    request.setJsonPayload(jsonPart);

    // Send an outbound request to the `login` backend resource.
    var loginResp = httpClient->post("/login", request);

    if (loginResp is http:Response) {
        // This response contains the cookies added by the backend server.
        // Get the login response message.
        string|error loginMessage = loginResp.getTextPayload();

        if (loginMessage is error) {
            log:printError("Login failed", loginMessage);
        } else {
            // When the login is successful, make another request to the
            // `/welcome` resource of the backend service.
            // As cookies are enabled in the HTTP client, it automatically handles cookies
            // received with the login response and sends the relevant cookies
            // to the `welcome` service resource.
            var welcomeResp = httpClient->get("/welcome");

            if (welcomeResp is http:Response) {
                string|error textPayload = welcomeResp.getTextPayload();
                if (textPayload is string) {
                    // A welcome message with the sent username
                    // will get printed.
                    log:printInfo(textPayload);
                }
            }
        }
    } else {
        log:printError((<error>loginResp).message());
    }
}
