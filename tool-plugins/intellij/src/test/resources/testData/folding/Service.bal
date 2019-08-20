import <fold text='...'>ballerina/http;
import ballerina/log;</fold>

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service hello on new http:Listener(9090) <fold text='{...}'>{

    // Resource functions are invoked with the HTTP caller.
    resource function sayHello(http:Caller caller, http:Request req);
}</fold>
