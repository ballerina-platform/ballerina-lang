import ballerina/http;
import ballerina/log;

listener http:Listener serverEP = new (9095);

@http:ServiceConfig {
    basePath: "/cookie-demo"
}

service cookieServer on serverEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/login"
    }
    resource function login(http:Caller caller, http:Request req) {
        // Retrieve the JSON payload from the request as it
        // contains the login details of a user.
        json|error details = req.getJsonPayload();

        if (details is json) {
            // Retrieve the username and password.
            json|error name = details.name;
            json|error password = details.password;

            if (name is json && password is json) {
                // Check the password value.
                if (password == "p@ssw0rd") {

                    // [Create a new cookie](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Cookie.html) by setting `name` as the `username`
                    // and `value` as the logged-in user's name.
                    http:Cookie cookie = new("username", name.toString());

                    // Set the cookies path as `/` to apply it to all the
                    // resources in the service.
                    cookie.path = "/";

                    http:Response response = new;

                    // [Add the created cookie to the response](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Response.html#addCookie).
                    response.addCookie(cookie);

                    // Set a message payload to inform that the login has
                    // been succeeded.
                    response.setTextPayload("Login succeeded");
                    var result = caller->respond(response);
                    if (result is error) {
                        log:printError("Failed to respond", result);
                    }
                }
            }
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/welcome"
    }
    resource function welcome(http:Caller caller, http:Request req) {
        // [Retrieve cookies from the request](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Request.html#getCookies).
        http:Cookie[] cookies = req.getCookies();

        // Get the cookie value of the `username`.
        http:Cookie[] usernameCookie = cookies.filter(function
                                (http:Cookie cookie) returns boolean {
            return cookie.name == "username";
        });

        if (usernameCookie.length() > 0) {
            string? user = usernameCookie[0].value;
            if (user is string) {
            // Respond with the username added to the welcome message.
            var result = caller->respond("Welcome back " + <@untainted> user);

            } else {
                // If the user is `nil`, send a login message.
                var result = caller->respond("Please login");
            }
        } else {
            // If the `username` cookie is not presented, send a login message.
            var result = caller->respond("Please login");
        }
    }
}
