import ballerina/http;

//Defines a channel with json constrained type.
channel<json> jsonChannel;

service<http:Service> channelService bind { port: 9090 } {

receive (endpoint caller, http:Request request) {
       http:Response response = new;

       //A key can be associated with a channel action, this is used to correlate receivers and senders.
       //Specifying a key is optional, non key message receivers are matched with messages sent without a key.
       string key = "123";

       json result;
       //Receive a message from the channel with given key.
       //Execution waits here if the message is not available.
       result <- jsonChannel, key;
       //Received message is sent as the response.
       response.setJsonPayload(result, contentType = "application/json");
       _ = caller -> respond(response);
   }

send (endpoint caller, http:Request request) {
       http:Response response = new;

        //Extract message from the request.
       json message = check request.getJsonPayload();
       //Define the same key as the receiver's key.
       string key = "123";

       //Send a message to the channel. One of the receivers waiting on this key receives it.
       //If there is no receiver, message is stored and the execution continues.
       //A receiver can arrive later and fetch the message.
       message -> jsonChannel, key;

       response.setJsonPayload({"send":"Success!!"}, contentType = "application/json");

        _ = caller -> respond(response);
   }
}
