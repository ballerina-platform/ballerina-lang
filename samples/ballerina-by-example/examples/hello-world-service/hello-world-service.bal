import ballerina.lang.messages;
import ballerina.net.http;

@doc:description{value : "Service keyword makes a ballerina program a service"}
service helloWorld {
    // Resources resides within a service. A service can have more than one resource.
    resource sayHello (message m) {
        message response = {};
        // A util method that can be used to set string payload.
        messages:setStringPayload(response, "Hello, World!");
        // Reply keyword sends the response back to the client.
        reply response;
    }
}
