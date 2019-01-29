import ballerina/http;
import ballerina/log;

// Defines a `channel` with `json` constraint type.
channel<json> jsonChannel = new;

service channelService on new http:Listener(9090) {

    resource function receive(http:Caller caller, http:Request request) {

        // A key can be associated with a channel action.
        // You can use a key to correlate receivers and senders.
        string key = "123";

        json jsonMsg;
        // Receive a message from the channel with given key.
        // Execution waits here if the message is not available.
        jsonMsg = <- jsonChannel, key;
        // Send the received message as the response.
        var result = caller->respond(jsonMsg);

        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }

    resource function send(http:Caller caller, http:Request request) {

        // Extract message from the request.
        json|error message = request.getJsonPayload();
        // Define the same key as the receiver's key.
        string key = "123";

        // Send a message to the channel.
        // One of the receivers waiting on this key receives it.
        // If there is no receiver, the message is stored and execution continues.
        // A receiver can arrive later and fetch the message.

        json jsonMessage = {};
        if (message is json) {
            jsonMessage = message;
        } else {
            log:printError("Invalid message content", err = message);
        }

        jsonMessage -> jsonChannel, key;

        var result = caller->respond({ "send": "Success!!" });
        if (result is error) {
           log:printError("Error sending response", err = result);
        }
    }
}
