import <fold text='...'>ballerina/http;
import ballerina/log;</fold>

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service hello on new http:Listener(9090) <fold text='{...}'>{

    // Resource functions are invoked with the HTTP caller and the
    // incoming request as arguments.
    resource function sayHello(http:Caller caller, http:Request req);
}</fold>

service ChatMessageListener = service <fold text='{...}'>{

    // Resource registered to receive server messages.
    resource function onMessage(string message) <fold text='{...}'>{
        io:println("Response received from server: " + message);
    }</fold>

    // Resource registered to receive server error messages.
    resource function onError(error err) <fold text='{...}'>{
        io:println("Error reported from server: " + err.reason() + " - "
                + <string>err.detail().message);
    }</fold>

    // Resource registered to receive server completed message.
    resource function onComplete() <fold text='{...}'>{
        io:println("Server Complete Sending Responses.");
    }</fold>
}</fold>;
