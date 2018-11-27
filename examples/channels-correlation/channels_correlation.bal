import ballerina/http;
import ballerina/log;

// Defines a channel with json constrained type.
channel<json> jsonChannel;

service<http:Service> channelService bind { port: 9090 } {

    receive(endpoint caller, http:Request request) {

        // A key can be associated with a channel action.
        // You can use a key to correlate receivers and senders.
        string key = "123";

        json result;
        // Receive a message from the channel with given key.
        // Execution waits here if the message is not available.
        result <- jsonChannel, key;
        // Send the received message as the response.
        var result = caller->respond(result);

        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }

    send(endpoint caller, http:Request request) {

        // Extract message from the request.
        json message = check request.getJsonPayload();
        // Define the same key as the receiver's key.
        string key = "123";

        // Send a message to the channel.
        // One of the receivers waiting on this key receives it.
        // If there is no receiver, the message is stored and execution continues.
        // A receiver can arrive later and fetch the message.
        message -> jsonChannel, key;

        var result = caller->respond({ "send": "Success!!" });
        if (result is error) {
           log:printError("Error sending response", err = result);
        }
    }
}
